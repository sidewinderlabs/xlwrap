/**
 * Copyright 2009 Andreas Langegger, andreas@langegger.at, Austria
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); 
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package at.jku.xlwrap.exec;

import java.util.Iterator;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import at.jku.xlwrap.common.Utils;
import at.jku.xlwrap.common.XLWrapException;
import at.jku.xlwrap.map.MapTemplate;
import at.jku.xlwrap.map.XLWrapMapping;
import at.jku.xlwrap.map.expr.XLExpr;
import at.jku.xlwrap.map.expr.val.XLExprValue;
import at.jku.xlwrap.spreadsheet.XLWrapEOFException;
import at.jku.xlwrap.vocab.XLWrap;

import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.mem.GraphMem;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import com.hp.hpl.jena.rdf.model.impl.ModelCom;

/**
 * @author dorgon
 *
 * generates models from XLWrapMapping
 * can be used multiple times efficiently sharing a single WorkbookRegistry
 *
 */
public class XLWrapMaterializer {
	private static final Logger log = LoggerFactory.getLogger(XLWrapMaterializer.class);

	private final ExecutionContext context;

	/**
	 * constructor
	 */
	public XLWrapMaterializer() {
		context = new ExecutionContext();
	}
	
	/**
	 * constructor for server environments
	 * 
	 * @param hostname
	 * @param port
	 * @param pubbyPathPrefix
	 */
	public XLWrapMaterializer(String hostname, int port, String pubbyPathPrefix) {
		context = new ExecutionContext(hostname, port, pubbyPathPrefix);
	}
	
	public Model generateModel(XLWrapMapping mapping) throws XLWrapException {
		// ModelFactory.createDefaultModel() creates a GraphMemFaster, which contains a bug and
		// may lead to an ArrayIndexOutOfBoundsException. Therefore, instantiate ModelCom manually.
		//return generateModel(mapping, ModelFactory.createDefaultModel());
		return generateModel(mapping, new ModelCom( new GraphMem( ModelFactory.Standard ) ) );
	}
	
	/**
	 * generate into target model
	 * 
	 * @param mapping
	 * @param targetModel
	 * @return
	 * @throws XLWrapException
	 */
	public Model generateModel(XLWrapMapping mapping, Model targetModel) throws XLWrapException {
		context.setTargetModel(targetModel);
		
		// for each template
		Iterator<MapTemplate> it = mapping.getMapTemplatesIterator();
		MapTemplate nextTmpl;
		while (it.hasNext()) {
			nextTmpl = it.next();
			nextTmpl.initAndValidate(context);
			context.setActiveTemplate(nextTmpl);
			
			// generate triples for template model in each stage
			Model tmplModel;
			TemplateModelGenerator mIt = new TemplateModelGenerator(context);
			while (mIt.hasNext()) {
				tmplModel = mIt.next();
				try {
					addStatements(context, tmplModel);
					Model constModel = nextTmpl.getConstantModel();
					if (constModel != null)
						addStatements(context, constModel);
				} catch (XLWrapEOFException e) {
					log.warn("End of file reached, skipping template.");
					break;
				}
			}
		}
		
		return context.getTargetModel();
	}

	/**
	 * add statements based on template model to target model
	 * 
	 * @param context
	 * @param tmplModel
	 * @throws XLWrapException
	 * @throws XLWrapEOFException 
	 */
	private void addStatements(ExecutionContext context, Model tmplModel) throws XLWrapException, XLWrapEOFException {
		Model target = context.getTargetModel();
		
		Resource s;
		Resource p;
		RDFNode o;

		NodeReplacer replacer = context.getNodeReplacer();
		replacer.scanXLWrapBNodes(tmplModel, context); // throws XLWrapEOFException
		
		StmtIterator sIt = tmplModel.listStatements();
		Statement tmplStatement;
		Triple triple;
		
		while (sIt.hasNext()) {
			tmplStatement = sIt.nextStatement();

			s = tmplStatement.getSubject();
			p = tmplStatement.getPredicate();
			o = tmplStatement.getObject();
			
			// TODO: don't copy model each time to get distinct bnodes, apply BNodeReplacer here 
			
			// skip if property is xl:uri or xl:id (already processed by NodeReplacer before)
			if (p.isURIResource() && ( p.getURI().equals(XLWrap.uri.getURI()) || p.getURI().equals(XLWrap.id.getURI()) ) )
				continue;

			// skip if the Expr for one of the resources evaluated to null
			if (replacer.isDeleted(s) || replacer.isDeleted(p) || replacer.isDeleted(o))
				continue;
			
			// if anonymous, try to replace by NodeReplacer
			if (s.isAnon())
				s = replacer.replace(s);
			if (p.isAnon())
				p = replacer.replace(p);
			if (o.isAnon())
				o = replacer.replace(o.as(Resource.class)); // also for object, (:x :p [ xl:uri "http://ex.com"^^xl:Expr ]) == (:x :p "http://ex.com"^^xl:Expr)
			
			// evaluate XLWrap expressions
			try {
				RDFNode newS = Utils.getEvaluatedNode(s, target, context);
				RDFNode newP = Utils.getEvaluatedNode(p, target, context);
				RDFNode newO = Utils.getEvaluatedNode(o, target, context);
				if (newS == null || newP == null || newO == null) {
					if (log.isTraceEnabled())
						log.trace("Skip triple: " + newS + " " + newP + " " + newO);
					continue;
				}
				if (!newS.isResource()) {
					throw new XLWrapException("Literal in subject position: '" + newS + "'");
				}
				if (!newP.isURIResource()) {
					throw new XLWrapException("Predicate must be URI: '" + newP + "'");
				}
				triple = new Triple(newS.asNode(), newP.asNode(), newO.asNode());
				if (log.isTraceEnabled())
					log.trace("Add triple: " + triple);
				
				target.getGraph().add(triple);
			} catch (XLWrapEOFException eof) {
				log.warn("End of file reached, skipping statement: " + tmplStatement);
			}
		}
		sIt.close();
	}
}

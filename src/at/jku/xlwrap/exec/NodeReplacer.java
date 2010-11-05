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

import java.util.HashSet;
import java.util.Hashtable;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import at.jku.xlwrap.common.Utils;
import at.jku.xlwrap.common.XLWrapException;
import at.jku.xlwrap.map.expr.TypeCast;
import at.jku.xlwrap.map.expr.XLExpr;
import at.jku.xlwrap.map.expr.val.XLExprValue;
import at.jku.xlwrap.spreadsheet.XLWrapEOFException;
import at.jku.xlwrap.vocab.XLWrap;

import com.hp.hpl.jena.datatypes.DatatypeFormatException;
import com.hp.hpl.jena.rdf.model.AnonId;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;


/**
 * URI/Bnode replacer, replaces blank nodes in templates with concrete URIs if a valid xl:uri specification exists
 * furthermore, for blank nodes which are annotated with a xl:id, it maintains equal anonymous IDs 
 * 
 * @author dorgon
 */
public class NodeReplacer {
	private static final Logger log = LoggerFactory.getLogger(NodeReplacer.class);
	private final Map<Resource, Resource> replacedBnodes; // one global
	private final Set<Resource> deletedResources = new HashSet<Resource>();
	private Map<Resource, Resource> replacedURIs; // new one each time a template model is applied
		
	/**
	 * create a new node replacer
	 * creates URIs and equal bnode labels for equal IDs 
	 */
	public NodeReplacer() {
		replacedBnodes = new Hashtable<Resource, Resource>();
	}
	
	/**
	 * scan statements of the form (?s xl:uri ?o) and (?s xl:id ?o) where ?o can be an xl:Expr literal
	 * creates internal nodes for future replacing, does not modify tmplModel
	 * 
	 * @param tmplModel the model to scan
	 * @param context the execution context for xl:Expr evaluation
	 * @throws XLWrapException
	 * @throws XLWrapEOFException
	 */
	public void scanXLWrapBNodes(Model tmplModel, ExecutionContext context) throws XLWrapException, XLWrapEOFException {
		replacedURIs = new Hashtable<Resource, Resource>(); // new one for URI replacements

		StmtIterator it = tmplModel.listStatements();
		Statement s;
		Resource p;
		
		while (it.hasNext()) {
			s = it.next();
			p = s.getPredicate();
			
			if (p.isURIResource() && (p.getURI().equals(XLWrap.uri.getURI()) || p.getURI().equals(XLWrap.id.getURI()))) {
				XLExpr expr = null;
				try {
					expr = Utils.getExpression(s.getObject().asNode());
				} catch (DatatypeFormatException e) {
					log.warn("Ignoring invalid xl:Expr for " + p.getURI() + ": '" + s.getObject() + "'.", e);
					deletedResources.add(s.getSubject());
					continue;
				}
				
				if (expr == null) {
					log.warn("Ignoring invalid xl:Expr for " + p.getURI() + ": '" + s.getObject() + "', object must be a literal with xl:Expr datatype.");
					deletedResources.add(s.getSubject());
					continue;
				}

				if (s.getSubject().isURIResource())
					log.warn("Overriding URI of " + s.getSubject().getURI() + " with " + expr + " specified by " + p.getURI() + "!");

				XLExprValue<?> ev = expr.eval(context);
				if (ev == null) {
					if (log.isDebugEnabled())
						log.debug("Ignoring xl:Expr for xl:uri or xl:id because it evaluated to null: " + expr + ".");
					deletedResources.add(s.getSubject());
				} else {
					String val = TypeCast.toString(ev);

					// replace URIs now
					if (p.getURI().equals(XLWrap.uri.getURI())) {
						replacedURIs.put(s.getSubject(), tmplModel.createResource(val));
						
					} else if (p.getURI().equals(XLWrap.id.getURI()))
						replacedBnodes.put(s.getSubject(), tmplModel.createResource(AnonId.create("id_" + val)));
				}
			}
		}		
		it.close();
	}
	
	/**
	 * replace orig by cached node or return orig
	 * @param orig
	 * @return replaced node or orig
	 */
	public Resource replace(Resource orig) {
		Resource r = replacedBnodes.get(orig);
		if (r == null)
			r = replacedURIs.get(orig);
		if (r == null)
			return orig;
		
		if (log.isTraceEnabled())
			log.trace("NodeReplacer: replacing " + orig.toString() + " with " + r.toString());
		return r;
	}
	
	/**
	 * Returns true if the resource had an attached xl:Expr that evaluated to null
	 * @param r a node (usually blank)
	 * @return true if it is deleted
	 */
	public boolean isDeleted(RDFNode r) {
		return deletedResources.contains(r);
	}
}

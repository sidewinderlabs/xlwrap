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
package at.jku.xlwrap.map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import at.jku.xlwrap.common.Constants;
import at.jku.xlwrap.common.XLWrapException;
import at.jku.xlwrap.map.expr.func.FunctionRegistry;
import at.jku.xlwrap.map.range.AnyRange;
import at.jku.xlwrap.map.transf.ColumnShift;
import at.jku.xlwrap.map.transf.FileRepeat;
import at.jku.xlwrap.map.transf.RowShift;
import at.jku.xlwrap.map.transf.SheetRepeat;
import at.jku.xlwrap.map.transf.SheetShift;
import at.jku.xlwrap.map.transf.Transformation;
import at.jku.xlwrap.vocab.XLWrap;

import com.hp.hpl.jena.datatypes.xsd.XSDDatatype;
import com.hp.hpl.jena.graph.BulkUpdateHandler;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.NodeIterator;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Seq;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import com.hp.hpl.jena.shared.impl.JenaParameters;
import com.hp.hpl.jena.vocabulary.RDF;
import com.hp.hpl.jena.vocabulary.XSD;

import de.fuberlin.wiwiss.ng4j.NamedGraph;
import de.fuberlin.wiwiss.ng4j.NamedGraphSet;
import de.fuberlin.wiwiss.ng4j.impl.NamedGraphSetImpl;

/**
 * @author dorgon
 *
 * Parses XL mapping specifications in TriG syntax (http://www4.wiwiss.fu-berlin.de/bizer/TriG/)
 */
public class MappingParser {
	private static final Logger log = LoggerFactory.getLogger(MappingParser.class);
	
	private final XLWrapMapping mapping;
	private final NamedGraphSet spec;
	private final Model specModel;
	
	/**
	 * parse a mapping specification and return the corresponding XLWrapMapping object
	 * 
	 * @param url of the specification
	 * @return
	 * @throws XLWrapException
	 */
	public static XLWrapMapping parse(String url) throws XLWrapException {
		// ensure XLExprDatatype is registered
		XLExprDatatype.register();
		
		return new MappingParser(url).getMapping();
	}
	
	/**
	 * private constructor
	 * 
	 * @param specification model
	 * @throws XLWrapException 
	 */
	private MappingParser(String url) throws XLWrapException {
		if (log.isDebugEnabled())
			log.debug("Reading TriG file from " + url + "...");
		
		spec = new NamedGraphSetImpl();
		spec.read(url, "TRIG");
		
		// search for xl:Mapping instance
		specModel = spec.asJenaModel("");
		
		StmtIterator mIt = specModel.listStatements(null, RDF.type, XLWrap.Mapping);
		if (mIt.hasNext()) {
			Resource mRes = mIt.next().getSubject();
			
			mapping = new XLWrapMapping(); // create XLWrap mapping
			if (mIt.hasNext())
				log.warn("More than one specifications found, ignoring further mappings (resources of type " + XLWrap.Mapping.getURI() + ").");

// TODO: loading functions now is already too late, XLExprDatatype literals are already parsed when reading the model
//			// load function libraries
//			StmtIterator fIt = mRes.listProperties(XLWrap.functionLib);
//			while(fIt.hasNext())
//				FunctionRegistry.registerPackage(fIt.next().getString());
//			fIt.close();
			
			// parse map templates
			StmtIterator tIt = mRes.listProperties(XLWrap.template);
			while (tIt.hasNext()) {
				Resource m = tIt.next().getResource();
				String fileName = null;
				Statement fn = m.getProperty(XLWrap.fileName);
				if (fn == null)
					throw new XLWrapException("Required property " + XLWrap.fileName + " not found for xl:Mapping " + m + ".");
				else
					fileName = fn.getString();
				// handle multiple files (comma-separated)
				if (fileName.contains(",")) {
					String[] fileNames = fileName.split(",");
					for (String file : fileNames) {
						m.removeAll(XLWrap.fileName);
						m.addProperty(XLWrap.fileName, file.trim());
						mapping.add(parseMapTemplate(m));
					}
				} else {
					// single file
					mapping.add(parseMapTemplate(m));
				}
			}
			tIt.close();
			
			// offline flag set?
			Statement offline = mRes.getProperty(XLWrap.offline);
			if (offline != null)
				mapping.setOffline(offline.getBoolean());
			
			mIt.close();
		} else
			throw new XLWrapException("No mapping specification found (resource of type " + XLWrap.Mapping.getURI() + ").");
		
		specModel.close();
		spec.close();

		XLExprDatatype.register();
	}
	
	/**
	 * parse map templates
	 * @param m the resource specifying the mapping
	 * @return another parsed MapTemplate
	 * @throws XLWrapException 
	 */
	private MapTemplate parseMapTemplate(Resource m) throws XLWrapException {
		String fileName = null;
		Resource parseProfile = null;
		String sheetName = null;
		Integer sheetNumber = null;
		Model tmplModel = null;
		
		Statement fn = m.getProperty(XLWrap.fileName);
		if (fn == null)
			throw new XLWrapException("Required property " + XLWrap.fileName + " not found for xl:Mapping " + m + ".");
		else
			fileName = fn.getString();
		
		Statement pp = m.getProperty(XLWrap.parseProfile);
		if (pp != null)
			parseProfile = pp.getResource();
		
		Statement sh = m.getProperty(XLWrap.sheetName);
		if (sh == null) {
			sh = m.getProperty(XLWrap.sheetNumber);
			if (sh == null)
				sheetNumber = 0; // default to 1st sheet - throw new XLWrapException("xl:Mapping " + m + " has neither a " + XLWrap.sheetName + " nor " + XLWrap.sheetNumber + ".");
			else
				sheetNumber = sh.getInt();
		} else
			sheetName = sh.getString();

		Statement tg = m.getProperty(XLWrap.templateGraph);
		if (tg == null)
			throw new XLWrapException("Required property " + XLWrap.templateGraph + " not found for xl:Mapping " + m + ".");
		else {
			NamedGraph g = spec.getGraph(tg.getResource().getURI());
			tmplModel = ModelFactory.createDefaultModel();
			tmplModel.setNsPrefixes(Constants.DEFAULT_PREFIXES); // add default prefixes
			tmplModel.setNsPrefixes(specModel.getNsPrefixMap()); // take prefixes from mapping file, possibly override defaults
			BulkUpdateHandler bh = tmplModel.getGraph().getBulkUpdateHandler();
			bh.add(g);
		}

		Model constModel = null;
		StmtIterator cgIt = m.listProperties(XLWrap.constantGraph);
		if (cgIt.hasNext())
			constModel = ModelFactory.createDefaultModel();
		String graphName;
		while (cgIt.hasNext()) {
			graphName = cgIt.next().getResource().getURI();
			NamedGraph g = spec.getGraph(graphName);
			if (g == null)
				throw new XLWrapException("Named graph <" + graphName + "> not found.");
			BulkUpdateHandler bh = constModel.getGraph().getBulkUpdateHandler();
			bh.add(g);
		}
		
		MapTemplate mapTmpl = null;
		if (sheetName != null)
			mapTmpl = new MapTemplate(fileName, parseProfile, sheetName, tmplModel, constModel);
		else
			mapTmpl = new MapTemplate(fileName, parseProfile, sheetNumber, tmplModel, constModel);

		StmtIterator constDefsIt = m.listProperties();
		while (constDefsIt.hasNext()) {
			Statement statement = constDefsIt.nextStatement();
			if (!statement.getPredicate().getNameSpace().equals(Constants.VARIABLE_NS)) continue;
			mapTmpl.addConstantDef(statement.getPredicate().getLocalName(), statement.getObject());
		}
		
		// add transformations if any
		Statement tr = m.getProperty(XLWrap.transform);
		if (tr != null) {
			Seq seq = m.getModel().getSeq(tr.getResource()); // get associated sequence
			if (seq != null) {
				NodeIterator el = seq.iterator();
				if (!el.hasNext() && tr.getObject().isResource()) {
					// interpret as single transform without a sequence
					Transformation tf = parseTransformation(tr.getResource());
					mapTmpl.repeatTransform(tf);
					return mapTmpl;
				} else {
					while (el.hasNext()) {
						Transformation tf = parseTransformation(el.next().as(Resource.class));
						mapTmpl.repeatTransform(tf);
					}
				}
				return mapTmpl;				
			}			
			throw new XLWrapException("Vaue of property " + XLWrap.transform + " must be an RDF Sequence or a single xl:Transformation.");			
		}		
		return mapTmpl;
	}

	/**
	 * @param resource
	 * @return
	 * @throws XLWrapException 
	 */
	private Transformation parseTransformation(Resource resource) throws XLWrapException {
		Statement s = resource.getProperty(RDF.type);
		if (s == null)
			throw new XLWrapException("Untyped transformation found for map template, please specify the type with rdf:type.");
		
		String type = s.getResource().getURI();
		if (type.equals(XLWrap.ColShift.getURI())) {
			int steps = getSteps(resource);
			int repeat = getRepeat(resource, ColumnShift.class.getName());
			String restriction = getRestriction(resource, ColumnShift.class.getName());
			String skipCondition = getSkipCondition(resource);
			String breakCondition = getBreakCondition(resource);
			return new ColumnShift(steps, repeat, restriction, skipCondition, breakCondition);
			
		} else if (type.equals(XLWrap.RowShift.getURI())) {
			int steps = getSteps(resource);
			int repeat = getRepeat(resource, RowShift.class.getName());
			String restriction = getRestriction(resource, RowShift.class.getName());
			String skipCondition = getSkipCondition(resource);
			String breakCondition = getBreakCondition(resource);
			return new RowShift(steps, repeat, restriction, skipCondition, breakCondition);
			
		} else if (type.equals(XLWrap.SheetShift.getURI())) {
			int steps = getSteps(resource);
			int repeat = getRepeat(resource, SheetShift.class.getName());
			String restriction = getRestriction(resource, SheetShift.class.getName());
			String skipCondition = getSkipCondition(resource);
			String breakCondition = getBreakCondition(resource);
			return new SheetShift(steps, repeat, restriction, skipCondition, breakCondition);
			
		} else if (type.equals(XLWrap.SheetRepeat.getURI())) {
			String sheets = getTargetSheetNames(resource, SheetRepeat.class.getName());
			String restriction = getRestriction(resource, SheetRepeat.class.getName());
			String skipCondition = getSkipCondition(resource);
			String breakCondition = getBreakCondition(resource);
			return new SheetRepeat(sheets, restriction, skipCondition, breakCondition);
			
		} else if (type.equals(XLWrap.FileRepeat.getURI())) {
			String files = getTargetFileNames(resource, FileRepeat.class.getName());
			String restriction = getRestriction(resource, FileRepeat.class.getName());
			String skipCondition = getSkipCondition(resource);
			String breakCondition = getBreakCondition(resource);
			return new FileRepeat(files, restriction, skipCondition, breakCondition);
			
		} else
			throw new XLWrapException("Unknown transformation type: " + type);
	}

	/**
	 * @param resource
	 * @param typeInfo
	 * @return
	 * @throws XLWrapException 
	 */
	private String getTargetSheetNames(Resource resource, String typeInfo) throws XLWrapException {
		Statement s = resource.getProperty(XLWrap.sheetNames);
		if (s == null)
			throw new XLWrapException(typeInfo + " has no " + XLWrap.sheetName + " property.");
		else
			return s.getString();
	}

	/**
	 * @param resource
	 * @param typeInfo
	 * @return
	 * @throws XLWrapException 
	 */
	private String getTargetFileNames(Resource resource, String typeInfo) throws XLWrapException {
		Statement s = resource.getProperty(XLWrap.fileNames);
		if (s == null)
			throw new XLWrapException(typeInfo + " has no " + XLWrap.fileNames + " property.");
		else
			return s.getString();
	}
	
	/**
	 * @param resource
	 * @return
	 */
	private String getBreakCondition(Resource resource) {
		Statement s = resource.getProperty(XLWrap.breakCondition);
		if (s == null)
			return null;
		else
			return s.getString();
	}

	/**
	 * @param resource
	 * @return
	 */
	private String getSkipCondition(Resource resource) {
		Statement s = resource.getProperty(XLWrap.skipCondition);
		if (s == null)
			return null;
		else
			return s.getString();
	}

	/**
	 * @param resource
	 * @param typeInfo
	 * @return
	 */
	private String getRestriction(Resource resource, String typeInfo) {
		Statement s = resource.getProperty(XLWrap.restriction);
		if (s == null)
			return AnyRange.SYMBOL;
		else
			return s.getString();
	}

	/**
	 * @param resource
	 * @param typeInfo
	 * @return
	 * @throws XLWrapException 
	 */
	private int getRepeat(Resource resource, String typeInfo) throws XLWrapException {
		Statement s = resource.getProperty(XLWrap.repeat);
		if (s == null)
			return Integer.MAX_VALUE;
		else
			return getInt(s);
	}

	/**
	 * @param resource
	 * @return
	 * @throws XLWrapException 
	 */
	private int getSteps(Resource resource) throws XLWrapException {
		Statement s = resource.getProperty(XLWrap.steps);
		if (s == null)
			return Constants.DEFAULT_SHIFT_STEPS;
		else
			return getInt(s);
	}

	/**
	 * @param object
	 * @return
	 * @throws XLWrapException 
	 */
	private int getInt(Statement st) throws XLWrapException {
		if (!st.getObject().isLiteral())
			throw new XLWrapException(st.getObject() + " is not a literal/number.");
		
		String dt = st.getLiteral().getDatatypeURI();
		if (dt == null || dt.equals(XSDDatatype.XSDstring))
			return Integer.parseInt(st.getString());
		if (dt.equals(XSD.xint.getURI()) || dt.equals(XSD.integer.getURI()))
			return st.getInt();
		else
			throw new XLWrapException(st.getObject() + " is not a number.");
	}

	public XLWrapMapping getMapping() {
		return mapping;
	}
	
}

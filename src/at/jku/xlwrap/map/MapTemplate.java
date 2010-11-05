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

import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import at.jku.xlwrap.common.Constants;
import at.jku.xlwrap.common.Utils;
import at.jku.xlwrap.common.XLWrapException;
import at.jku.xlwrap.exec.ExecutionContext;
import at.jku.xlwrap.map.expr.E_RangeRef;
import at.jku.xlwrap.map.expr.XLExpr;
import at.jku.xlwrap.map.expr.XLExprVisitor;
import at.jku.xlwrap.map.expr.XLExprWalker;
import at.jku.xlwrap.map.expr.val.E_Boolean;
import at.jku.xlwrap.map.range.AnyRange;
import at.jku.xlwrap.map.range.MultiRange;
import at.jku.xlwrap.map.range.Range;
import at.jku.xlwrap.map.transf.Transformation;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;

/**
 * @author dorgon
 *
 * formal specification of a map template, is not validated before execution
 * expressions and ranges are parsed however
 */
public class MapTemplate {
	private static final Logger log = LoggerFactory.getLogger(MapTemplate.class);
	
	private final String fileName;
	private final Resource parseProfile;
	
	// sheet can be set by name or number (if both are set, name has priority)
	private String sheetName;
	private Integer sheetNum;
	
	private final Model tmplModel;
	private final Model constModel; // can be null
	
	private final List<Transformation> transformations;
	private final Map<String, RDFNode> constantDefs = new HashMap<String, RDFNode>();
	
	/**
	 * Important note: When this constructor is used, before creating the Model tmplModel 
	 * XLExprDatatype.register() must have been called to register the custom xl:Expr data type
	 * 
	 * @param fileName
	 * @param parseProfile may be null
	 * @param sheetName
	 * @param tmplModel
	 * @param constModel
	 */
	public MapTemplate(String fileName, Resource parseProfile, String sheetName, Model tmplModel, Model constModel) {
		this.fileName = fileName;
		this.parseProfile = parseProfile;
		this.sheetName = sheetName;
		this.sheetNum = null;
		this.tmplModel = tmplModel;
		this.constModel = constModel;
	
		this.transformations = new ArrayList<Transformation>();
	}

	/**
	 * @param fileName
	 * @param parseProfile may be null
	 * @param sheetName
	 * @param tmplModel
	 */
	public MapTemplate(String fileName, Resource parseProfile, String sheetName, Model tmplModel) {
		this(fileName, parseProfile, sheetName, tmplModel, null);
	}
	
	public MapTemplate(String fileName, Resource parseProfile, String sheetName, String tmplModel, String constModel) {
		this(fileName, parseProfile, sheetName,
				Utils.createModel(Constants.DEFAULT_PREFIXES_N3 + tmplModel, "N3"),
				(constModel != null) ? Utils.createModel(Constants.DEFAULT_PREFIXES_N3 + constModel, "N3") : ModelFactory.createDefaultModel());
	}

	public MapTemplate(String fileName, Resource parseProfile, String sheetName, String tmplModel) {
		this(fileName, parseProfile, sheetName, tmplModel, null);
	}
		
	public MapTemplate(String fileName, Resource parseProfile, Integer sheetNum, Model tmplModel, Model constModel) {
		this.fileName = fileName;
		this.parseProfile = parseProfile;
		this.sheetName = null;
		this.sheetNum = (sheetNum == null) ? 0 : sheetNum;
		this.tmplModel = tmplModel;
		this.constModel = constModel;
	
		this.transformations = new ArrayList<Transformation>();
	}

	public MapTemplate(String fileName, Resource parseProfile, Integer sheetNum, Model tmplModel) {
		this(fileName, parseProfile, sheetNum, tmplModel, null);
	}
	
	/**
	 * @param fileName
	 * @param parseProfile
	 * @param sheetNum zero-based (if sheetNum is null => 0)
	 * @param tmplModel as string in Trutle syntax
	 * @param constModel
	 */
	public MapTemplate(String fileName, Resource parseProfile, Integer sheetNum, String tmplModel, String constModel) {
		this(fileName, parseProfile, sheetNum,
				Utils.createModel(Constants.DEFAULT_PREFIXES_N3 + tmplModel, "N3"),
				(constModel != null) ? Utils.createModel(Constants.DEFAULT_PREFIXES_N3 + constModel, "N3") : ModelFactory.createDefaultModel());
	}
	
	public MapTemplate(String fileName, Resource parseProfile, Integer sheetNum, String tmplModel) {
		this(fileName, parseProfile, sheetNum, tmplModel, null);
	}
	
	/**
	 * adds a repeat transformation operation
	 * 
	 * @param transformation
	 */
	public void repeatTransform(Transformation transformation) {
//		transformation.setTargetTemplate(this);
		this.transformations.add(transformation);
	}

	/**
	 * @return the fileName
	 */
	public String getFileName() {
		return fileName;
	}
	
	/**
	 * @return the parseProfile
	 */
	public Resource getParseProfile() {
		return parseProfile;
	}
	
	/**
	 * @return the sheetName
	 */
	public String getSheetName() {
		return sheetName;
	}
	
	/**
	 * @return the sheetNum (zero-based)
	 */
	public Integer getSheetNum() {
		return sheetNum;
	}
	
	/**
	 * @return the tmplModel
	 */
	public Model getTemplateModel() {
		return tmplModel;
	}

	public String getTemplateModelAsString() {
		StringWriter w = new StringWriter();
		tmplModel.write(w, "N3");
		return w.toString();
	}
	
	/**
	 * @return the constModel
	 */
	public Model getConstantModel() {
		return constModel;
	}
	
	public String getConstantModelAsString() {
		if (constModel == null)
			return "";
		StringWriter w = new StringWriter();
		constModel.write(w, "N3");
		return w.toString();
	}
	
	/**
	 * @return the transformations
	 */
	public List<Transformation> getTransformations() {
		return transformations;
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("template graph:\n").append(getTemplateModelAsString());
		sb.append("constant graph:\n").append(getConstantModelAsString());
		sb.append("\n").append("initial source: '").append(fileName).append("'");
		if (sheetName != null)
			sb.append(" (Sheet '").append(sheetName).append("')");
		else if (sheetNum != null)
			sb.append(" (Sheet #").append(sheetNum).append(")");
		else
			sb.append(" (first sheet)");
		sb.append("\n").append(transformations.size()).append(" repeat transformation(s): \n");
		for (Transformation t : transformations)
			sb.append(t).append("\n");
		return sb.toString();
	}

	/**
	 * @return
	 */
	public boolean hasTransformations() {
		return transformations.size() > 0;
	}

	/**
	 * initialize and validate
	 * sets both, sheetName and sheetNum
	 * @param context
	 * @throws XLWrapException 
	 */
	public void initAndValidate(ExecutionContext context) throws XLWrapException {
		try {
			if (sheetNum != null)
				this.sheetName = context.getSheet(fileName, sheetNum).getName();
			else if (sheetName != null)
				this.sheetNum = context.getSheetNumber(fileName, sheetName);
			else
				throw new XLWrapException("No sheet set.");
		} catch (XLWrapException e) {
			log.error("Failed to initialize and validate map template. The transformation process may not work correctly.", e);
		}
		
		if (this.parseProfile != null)
			log.error("CSV parse profiles not supported yet, will try to auto-detect as usual.");

		// replace *.* (AnyRange) in conditions with actual ranges mentioned in the template graph
		for (Transformation t : transformations) {
			// skip if boolean (occurs frequently = default when conditions are not set)
			if (!(t.getBreakCondition() instanceof E_Boolean)) {
				// replace all *.* ranges by ranges referred in the tmplModel (special behavior of *.* for conditions)
				Set<Range> ranges = getReferredRanges();
				MultiRange multi = new MultiRange();
				for (Range r : ranges)
					multi.addRange(r);
				XLExprWalker.walkPostOrder(t.getBreakCondition(), new ReplaceAnyRange(multi));
			}
		}

		// Auto-detect intersection of box ranges and warn/throw exception or auto-split into multi-ranges
		
		// TODO validation
		
		// e.g. checking:
		// sheet ranges: sheet2 > sheet1 (if specified by name, for numbers Util.parseRange() checks already)
	}
	
	private Set<Range> getReferredRanges() throws XLWrapException {
		CollectRanges collector = new CollectRanges();
		StmtIterator it = tmplModel.listStatements();
		while (it.hasNext()) {
			Statement st = it.nextStatement();
			XLExpr expr = Utils.getExpression(st.getObject().asNode());
			if (expr != null)
				XLExprWalker.walkPostOrder(expr, collector);
		}
		return collector.getRanges();
	}

	public void addConstantDef(String constantName, RDFNode constantValue) throws XLWrapException {
		if (constantDefs.containsKey(constantName)) {
			throw new XLWrapException("Re-defined constant: $" + constantName);
		}
		constantDefs.put(constantName, constantValue);
	}
	
	public RDFNode getConstantDef(String constantName) {
		return constantDefs.get(constantName);
	}
	
	private class CollectRanges implements XLExprVisitor {

		private final Set<Range> list = new HashSet<Range>();
		
		@Override
		public void visiting0(XLExpr expr0) throws XLWrapException {
			if (expr0 instanceof E_RangeRef) {
				Range r = ((E_RangeRef) expr0).getRange();
				list.add(r);
			}
		}
		
		/**
		 * @return the list
		 */
		public Set<Range> getRanges() {
			return list;
		}
		
		@Override
		public void visiting1(XLExpr expr) throws XLWrapException {}

		@Override
		public void visiting2(XLExpr expr) throws XLWrapException {}

		@Override
		public void visitingFunction(XLExpr expr) throws XLWrapException {}		
	}

	
	private class ReplaceAnyRange implements XLExprVisitor {
		private final Range replacement;
		
		/**
		 * @param range replace all AnyRange ranges with this range
		 */
		public ReplaceAnyRange(Range replacement) {
			this.replacement = replacement;
		}

		@Override
		public void visiting0(XLExpr expr) throws XLWrapException {
			if (expr instanceof E_RangeRef && ((E_RangeRef) expr).getRange() instanceof AnyRange)
				((E_RangeRef) expr).setRange(replacement);
		}

		@Override
		public void visiting1(XLExpr expr) throws XLWrapException {}

		@Override
		public void visiting2(XLExpr expr) throws XLWrapException {}

		@Override
		public void visitingFunction(XLExpr expr) throws XLWrapException {}
	}
	
}

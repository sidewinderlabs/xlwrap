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

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import at.jku.xlwrap.common.Utils;
import at.jku.xlwrap.common.XLWrapException;
import at.jku.xlwrap.map.MapTemplate;
import at.jku.xlwrap.map.XLExprDatatype;
import at.jku.xlwrap.map.expr.E_RangeRef;
import at.jku.xlwrap.map.expr.TypeCast;
import at.jku.xlwrap.map.expr.XLExpr;
import at.jku.xlwrap.map.expr.XLExpr1;
import at.jku.xlwrap.map.expr.XLExpr2;
import at.jku.xlwrap.map.expr.XLExprVisitor;
import at.jku.xlwrap.map.expr.XLExprWalker;
import at.jku.xlwrap.map.expr.func.XLExprFunction;
import at.jku.xlwrap.map.range.AnyRange;
import at.jku.xlwrap.map.range.Range;
import at.jku.xlwrap.map.transf.Transformation;
import at.jku.xlwrap.spreadsheet.XLWrapEOFException;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;

/**
 * @author dorgon
 *
 * A transformation stage produces multiple template models for each iteration
 * in a cascaded transformation specification.
 * 
 * It is organized as a cascaded sequence of multiple iterable transformation stages whose
 * implementation of the TransformExector interface is implemented by the actual transform operation.
 * 
 */
public abstract class TransformationStage implements TransformationExector {
	private static final Logger log = LoggerFactory.getLogger(TransformationStage.class);
	
	/** parent stage (null for root) */
	protected TransformationStage parent = null;
	
	/** sub stage (null for leaf) */
	protected TransformationStage sub = null;
	
	/** the execution context */
	protected final ExecutionContext context;
	
	/** template model of current stage */
	private Model stageTmpl;

	/** this = stage restriction + base restrictions of subs */
	private Map<TransformationStage, Range> restrictions;
	
	/** this = stage break condition + base break conditions of subs */
	private Map<TransformationStage, XLExpr> breakConditions;
	
	/** this = stage skip condition + base skip conditions of subs */
	private Map<TransformationStage, XLExpr> skipConditions;
	
	
	/**
	 * constructor
	 */
	protected TransformationStage(ExecutionContext context) {
		this.context = context;
		this.restrictions = new Hashtable<TransformationStage, Range>();
		this.breakConditions = new Hashtable<TransformationStage, XLExpr>();
		this.skipConditions = new Hashtable<TransformationStage, XLExpr>();
	}
	
	/**
	 * creates a cascaded sequence of TransformationStages
	 * 
	 * @param context
	 * @return
	 * @throws XLWrapException 
	 */
	public static TransformationStage create(ExecutionContext context) throws XLWrapException {		
		MapTemplate activeTmpl = context.getActiveTemplate();
		
		TransformationStage exec = null;
		TransformationStage parent = null;

		List<Transformation> t = activeTmpl.getTransformations();
		
		// create cascaded sequence of stages and build up baseRestrictions map
		Transformation transf;		
		if (t.size() > 0) {
			for (int i = t.size() - 1; i >= 0; i--) {
				transf = t.get(i);

				exec = transf.getExecutor(context);
				
				// need to initially apply transform to make replacements in case of Sheet/FileRepeat/etc.
				exec.initStage(activeTmpl.getTemplateModel(),
						transf.getRestriction(),
						transf.getBreakCondition(),
						transf.getSkipCondition()
						);
				
				if (parent != null)
					parent.sub = exec;
				
				exec.parent = parent;
				parent = exec;
			}
		
			exec.setSubRestrictionsAndConditions(new Hashtable<TransformationStage, Range>(), new Hashtable<TransformationStage, XLExpr>(), new Hashtable<TransformationStage, XLExpr>());
			return exec;
		} else
			return null; // no transformations
	}
	
	/**
	 * set restrictions and break/skip conditions of sub stages (bottom-up recursion)
	 * 
	 * @param baseRestrictionsOfSubs collected restrictions of subs
	 * @param baseBreakConditionsOfSubs collected break conditions of subs
	 * @param baseSkipConditionsOfSubs collected skip conditions of subs
	 */
	private void setSubRestrictionsAndConditions(Map<TransformationStage, Range> baseRestrictionsOfSubs, Map<TransformationStage, XLExpr> baseBreakConditionsOfSubs, Map<TransformationStage, XLExpr> baseSkipConditionsOfSubs) {
		for (TransformationStage exec : baseRestrictionsOfSubs.keySet()) {
			restrictions.put(exec, baseRestrictionsOfSubs.get(exec).copy());
			breakConditions.put(exec, baseBreakConditionsOfSubs.get(exec).copy());
			if (baseSkipConditionsOfSubs.containsKey(exec))
				skipConditions.put(exec, baseSkipConditionsOfSubs.get(exec).copy());
		}
		
		baseRestrictionsOfSubs.put(this, getStageRestriction()); 	// can use reference now, will copy later
		baseBreakConditionsOfSubs.put(this, getStageBreakCondition());		// can use reference now, will copy later
		if (getStageSkipCondition() != null)
			baseSkipConditionsOfSubs.put(this, getStageSkipCondition());		// can use reference now, will copy later
		if (parent != null) // recurse
			parent.setSubRestrictionsAndConditions(baseRestrictionsOfSubs, baseBreakConditionsOfSubs, baseSkipConditionsOfSubs);
	}

	/**
	 * 
	 * @param stageTmpl
	 * @param stageRestriction
	 * @param stageBreakCondition
	 * @param stageSkipCondition
	 * @throws XLWrapException
	 */
	private void initStage(Model stageTmpl, Range stageRestriction, XLExpr stageBreakCondition, XLExpr stageSkipCondition) throws XLWrapException {
		this.stageTmpl = Utils.copyModel(stageTmpl);
		setStageRestriction(stageRestriction.copy());
		setStageBreakCondition(stageBreakCondition.copy());
		setStageSkipCondition(stageSkipCondition.copy());
		
		// post-order:
		init();
		proceed();
	}
	
	/**
	 * proceed if there are more templates in this or higher stages, otherwise return false
	 * also checks break condition and continues with next higher stage if true
	 * 
	 * @return true if this has more or any parent has more transformations left
	 * @throws XLWrapException 
	 */
	public boolean proceed() throws XLWrapException {
		if (hasMoreTransformations() &&
			applyTransformation() &&
			!breakConditionTrue(getStageBreakCondition(), context)) {
			
			if (skipConditionTrue(getStageSkipCondition(), context)) {
				if (log.isTraceEnabled())
					log.trace("Skipping transformation due to skip condition: " + getStageBreakCondition());
				return proceed();
			}
			
			return true;

			// proceed with next higher stage
		} else {
			// if parent has next re-init this
			if (parent != null) {
				if (parent.proceed()) {
					
					// copy down for this stage
					stageTmpl = Utils.copyModel(parent.getStageTemplate());
					
					// copy down base restrictions and break conditions
					for (TransformationStage key : restrictions.keySet()) {
						restrictions.put(key, parent.restrictions.get(key).copy());
						breakConditions.put(key, parent.breakConditions.get(key).copy());
						if (parent.skipConditions.containsKey(key))
							skipConditions.put(key, parent.skipConditions.get(key).copy());
					}
					
					init();
					proceed(); // must have more now
				return true;
				}
			}
		}

		return false;
	}

	/**
	 * transform the stage template, restriction, and break condition
	 * return false if an expression is out of sheet bounds
	 * 
	 * @throws XLWrapException 
	 */
	private boolean applyTransformation() throws XLWrapException {
		Range stageRestriction = getStageRestriction();
		
		XLExprVisitor exprTransformer = new TransformRangeReferences(this, stageRestriction);
		SheetBoundsChecker boundsCheck = new SheetBoundsChecker(context);

		stageTmpl = Utils.copyModel(stageTmpl); //TODO no need to copy if we use a bnode generator in XLWrapMaterializer.addStatements()
		
		Statement st;
		Node o;
		XLExpr expr;
		XLExpr transformed;
		
		// collect changes first
		List<ObjectChange> changes = new ArrayList<ObjectChange>();
		
		StmtIterator sIt = stageTmpl.listStatements();
		while (sIt.hasNext()) {
			st = sIt.nextStatement();
			o = st.getObject().asNode();
			
			expr = Utils.getExpression(o);
			
			if (expr != null) {
				transformed = expr.copy(); // clone before, must not change original one
				XLExprWalker.walkPostOrder(transformed, exprTransformer);
				
				// check sheet bounds
				if (!boundsCheck.withinSheetBounds(transformed)) {
					log.debug("At least one expression out of sheet bounds, proceeding with next stage.");
					return false;
				}
				
				// throws ConcurrentModificationException: st.changeObject(stageTmpl.createTypedLiteral(expr, XLExprDatatype.instance));
				changes.add(new ObjectChange(st, stageTmpl.createTypedLiteral(transformed, XLExprDatatype.instance)));

				if (log.isTraceEnabled())
					log.trace(expr + " => " + transformed);
			}
		}
		
		// apply changes now
		for (ObjectChange ch : changes)
			ch.statement.changeObject(ch.object);
		sIt.close();

		// transform stage break condition
		XLExprWalker.walkPostOrder(getStageBreakCondition(), new TransformRangeReferences(this, stageRestriction));
		// transform stage skip condition
		if (getStageSkipCondition() != null)
			XLExprWalker.walkPostOrder(getStageSkipCondition(), new TransformRangeReferences(this, stageRestriction));
		// copy previous restriction
		Range prevRestriction = stageRestriction.copy();
		// transform stage restriction
		setStageRestriction(transform(stageRestriction, AnyRange.INSTANCE));		
		
		// transform sub ranges and stage break conditions
		if (sub != null)
			transformSubRestrictionsAndConditions(sub, prevRestriction);
		
		// not returned false earlier; all ranges within sheet bounds: true
		return true;
	}

	/**
	 * @param key
	 * @param restriction
	 * @throws XLWrapException 
	 */
	private void transformSubRestrictionsAndConditions(TransformationStage key, Range restriction) throws XLWrapException {
		XLExprWalker.walkPostOrder(breakConditions.get(key), new TransformRangeReferences(this, restriction));
		XLExprWalker.walkPostOrder(skipConditions.get(key), new TransformRangeReferences(this, restriction));
		restrictions.put(key, transform(restrictions.get(key), restriction));

		if (key.sub != null)
			transformSubRestrictionsAndConditions(key.sub, restriction);
	}

	/**
	 * get transformed template model
	 * 
	 * @return template model
	 */
	public Model getStageTemplate() {
		return stageTmpl;
	}

	public Range getStageRestriction() {
		return restrictions.get(this);
	}
	
	public void setStageRestriction(Range restriction) {
		restrictions.put(this, restriction);
	}
	
	public XLExpr getStageBreakCondition() {
		return breakConditions.get(this);
	}
	
	public void setStageBreakCondition(XLExpr condition) {
		breakConditions.put(this, condition);
	}
	
	public XLExpr getStageSkipCondition() {
		return skipConditions.get(this);
	}
	
	public void setStageSkipCondition(XLExpr condition) {
		skipConditions.put(this, condition);
	}
	
	/**
	 * common method for checking the break condition,
	 * 
	 * @param breakCondition
	 * @param context
	 * @return
	 * @throws XLWrapException 
	 */
	public boolean breakConditionTrue(XLExpr breakCondition, ExecutionContext context) throws XLWrapException {
		try {
			return TypeCast.toBoolean(breakCondition.eval(context), context);
		} catch (XLWrapEOFException e) {
			log.debug("End of spreadsheet file reached, break condition forced to TRUE.");
			return true;
		}
	}

	/**
	 * common method for checking the skip condition,
	 * 
	 * @param skipCondition
	 * @param context
	 * @return
	 * @throws XLWrapException 
	 */
	public boolean skipConditionTrue(XLExpr skipCondition, ExecutionContext context) throws XLWrapException {
		try {
			return TypeCast.toBoolean(skipCondition.eval(context), context);
		} catch (XLWrapEOFException e) {
			log.warn("Unable to evaluate skip condition, skip condition forced to FALSE.");
			return false;
		}
	}
	
	/**
	 * @return a status string
	 */
	public abstract String getThisStatus();
	
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return  ((parent != null) ? parent + "\n   ^\n   |\n" : "") + getThisStatus();
	}
	
	/**
	 * a simple data structure to track object changes
	 * 
	 * @author dorgon
	 */
	private class ObjectChange {
		Statement statement;
		RDFNode object;
		
		public ObjectChange(Statement s, RDFNode o) {
			this.statement = s;
			this.object = o;
		}
	}

	class SheetBoundsChecker {
		private ExecutionContext context;
		
		/**
		 * @param context
		 */
		public SheetBoundsChecker(ExecutionContext context) {
			this.context = context;
		}

		public boolean withinSheetBounds(XLExpr expr) {
			if (expr instanceof E_RangeRef) {
				Range r = ((E_RangeRef) expr).getRange();
				if (!r.withinSheetBounds(context))
					return false;
				
			} else if (expr instanceof XLExpr1) {
				return withinSheetBounds(((XLExpr1) expr).getArg());
				
			} else if (expr instanceof XLExpr2) {
				return withinSheetBounds(((XLExpr2) expr).getArg1()) && withinSheetBounds(((XLExpr2) expr).getArg2());
				
			} else if (expr instanceof XLExprFunction) {
				for (XLExpr arg : ((XLExprFunction) expr).getArgs())
					if (!withinSheetBounds(arg))
						return false;
				return true;
			}
			
			return true;
		}
		
	}

}

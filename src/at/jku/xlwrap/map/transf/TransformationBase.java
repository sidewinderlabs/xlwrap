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
package at.jku.xlwrap.map.transf;

import at.jku.xlwrap.common.Utils;
import at.jku.xlwrap.common.XLWrapException;
import at.jku.xlwrap.map.expr.XLExpr;
import at.jku.xlwrap.map.expr.parser.ParseException;
import at.jku.xlwrap.map.expr.parser.XLExpression;
import at.jku.xlwrap.map.expr.val.E_Boolean;
import at.jku.xlwrap.map.range.AnyRange;
import at.jku.xlwrap.map.range.Range;


/**
 * @author dorgon
 *
 */
public abstract class TransformationBase implements Transformation {
	
	/** a condition, TransformationExector skips stage if condition evaluates to true */
	protected final XLExpr breakCondition;

	/** a condition, skips appying template and generation of triples if evaluates true */ 
	protected final XLExpr skipCondition;

	/** restrict transformation to this range */
	protected final Range restriction;
	
	/**
	 * @param restriction
	 * @param shiftRange
	 * @param skipCondition
	 * @param breakCondition
	 * @throws XLWrapException 
	 */
	public TransformationBase(String restriction, String skipCondition, String breakCondition) throws XLWrapException {
		if (restriction == null)
			this.restriction = AnyRange.INSTANCE;
		else
			this.restriction = Utils.parseRange(restriction);			
			
		try {
			if (skipCondition == null)
				this.skipCondition = E_Boolean.FALSE; // no condition => always false
			else
				this.skipCondition = XLExpression.parse(skipCondition);
		} catch (ParseException e) {
			throw new XLWrapException("Failed to parse skip condition: '" + skipCondition + "' as an xl:Expr.", e);
		}

		try {
			if (breakCondition == null)
				this.breakCondition = E_Boolean.FALSE; // no condition => always false
			else
				this.breakCondition = XLExpression.parse(breakCondition);	
		} catch (ParseException e) {
			throw new XLWrapException("Failed to parse break condition: '" + breakCondition + "' as an xl:Expr.", e);
		}
	}

	@Override
	public XLExpr getBreakCondition() {
		return breakCondition;
	}

	@Override
	public XLExpr getSkipCondition() {
		return skipCondition;
	}
	
	@Override
	public Range getRestriction() {
		return restriction;
	}
	
	@Override
	public String getName() {
		return getClass().getSimpleName();
	}
	
	/**
	 * @return additional arguments as a string for toString()
	 */
	public abstract String getArgsAsString();
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(getName());
		sb.append("(\n" +
				"\tcondition: ").append(breakCondition).append("\n");
		sb.append("\t").append(getArgsAsString()).append("\n");
		sb.append(")");
		return sb.toString();
	}
	
}

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

import at.jku.xlwrap.common.XLWrapException;
import at.jku.xlwrap.map.expr.E_RangeRef;
import at.jku.xlwrap.map.expr.XLExpr;
import at.jku.xlwrap.map.expr.XLExprVisitor;
import at.jku.xlwrap.map.range.Range;

/**
 * @author dorgon
 *
 */
public class TransformRangeReferences implements XLExprVisitor {
	private final TransformationStage exec;
	private final Range restriction;
	
	/**
	 * constructor, creates cascaded sequence of the MapTemplate's transform executors
	 */
	public TransformRangeReferences(TransformationStage exec, Range restriction) {
		this.exec = exec;
		this.restriction = restriction;
	}

	@Override
	public void visiting0(XLExpr expr) throws XLWrapException {
		if (expr instanceof E_RangeRef) {
			E_RangeRef rangeRef = (E_RangeRef) expr;
			rangeRef.setRange(exec.transform(rangeRef.getRange(), restriction));
		}		
	}

	@Override
	public void visiting1(XLExpr expr1) throws XLWrapException {}

	@Override
	public void visiting2(XLExpr expr2) throws XLWrapException {}

	@Override
	public void visitingFunction(XLExpr func) throws XLWrapException {}

}

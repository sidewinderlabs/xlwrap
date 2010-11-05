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
package at.jku.xlwrap.map.expr.func.spreadsheet;

import at.jku.xlwrap.common.XLWrapException;
import at.jku.xlwrap.exec.ExecutionContext;
import at.jku.xlwrap.map.expr.E_RangeRef;
import at.jku.xlwrap.map.expr.XLExpr;
import at.jku.xlwrap.map.expr.func.FunctionRegistry;
import at.jku.xlwrap.map.expr.func.XLExprFunction;
import at.jku.xlwrap.map.expr.val.E_Long;
import at.jku.xlwrap.map.expr.val.XLExprValue;
import at.jku.xlwrap.map.range.CellRange;
import at.jku.xlwrap.map.range.Range;

/**
 * @author dorgon
 *
 */
public class E_FuncSHEETNUM extends XLExprFunction {

	/**
	 * default constructor
	 */
	public E_FuncSHEETNUM() {
	}
	
	public E_FuncSHEETNUM(XLExpr arg) {
		addArg(arg);
	}
	
	@Override
	public XLExprValue<?> eval(ExecutionContext context) throws XLWrapException {
		// ignores actual cell value, just use the range reference to determine sheet
		
		if (args.size() == 0)
			return new E_Long(context.getActiveTemplate().getSheetNum().longValue());
		
		else {
			if (!(args.get(0) instanceof E_RangeRef))
				throw new XLWrapException("Argument " + args.get(0) + " of " + FunctionRegistry.getFunctionName(E_FuncSHEETNUM.class) + " must be a cell range reference.");
			Range absolute = ((E_RangeRef) args.get(0)).getRange().getAbsoluteRange(context);
			if (!(absolute instanceof CellRange))
				throw new XLWrapException("Argument " + args.get(0) + " of " + FunctionRegistry.getFunctionName(E_FuncSHEETNUM.class) + " must be a cell range reference.");
			
			return new E_Long(((CellRange) absolute).getSheetNum().longValue()+1); // zero-based, add 1
		}
	}

}

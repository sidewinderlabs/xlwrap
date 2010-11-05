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

import at.jku.xlwrap.common.Utils;
import at.jku.xlwrap.common.XLWrapException;
import at.jku.xlwrap.exec.ExecutionContext;
import at.jku.xlwrap.map.expr.E_RangeRef;
import at.jku.xlwrap.map.expr.XLExpr;
import at.jku.xlwrap.map.expr.func.XLExprFunction;
import at.jku.xlwrap.map.expr.val.E_Boolean;
import at.jku.xlwrap.map.expr.val.XLExprValue;
import at.jku.xlwrap.map.range.Range;
import at.jku.xlwrap.map.range.Range.CellIterator;
import at.jku.xlwrap.spreadsheet.XLWrapEOFException;

/**
 * @author dorgon
 *
 */
public class E_FuncEMPTY extends XLExprFunction {

	/**
	 * default constructor
	 */
	public E_FuncEMPTY() {
	}

	/**
	 * single argument (a range)
	 */
	public E_FuncEMPTY(XLExpr arg) {
		args.add(arg);
	}

	@Override
	public XLExprValue<Boolean> eval(ExecutionContext context) throws XLWrapException, XLWrapEOFException {
		if (!(args.get(0) instanceof E_RangeRef))
			throw new XLWrapException("Argument of function EMPTY() is not a range reference: " + args.get(0).toString());
		
		// every cell must be empty (conjunction)
		Range range = ((E_RangeRef) args.get(0)).getRange();
		CellIterator it = range.getCellIterator(context);
		while (it.hasNext()) {
			if (Utils.getXLExprValue(it.next()) != null)
				return E_Boolean.FALSE;
		}
		
		return E_Boolean.TRUE;
	}
	
}

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
package at.jku.xlwrap.map.expr;

import at.jku.xlwrap.common.XLWrapException;
import at.jku.xlwrap.exec.ExecutionContext;
import at.jku.xlwrap.map.expr.val.E_Double;
import at.jku.xlwrap.map.expr.val.E_Long;
import at.jku.xlwrap.map.expr.val.XLExprNumber;
import at.jku.xlwrap.map.expr.val.XLExprValue;
import at.jku.xlwrap.spreadsheet.XLWrapEOFException;



/**
 * @author dorgon
 *
 */
public class E_Percent extends XLExpr1 {

	/**
	 * constructor
	 */
	public E_Percent(XLExpr arg) {
		super(arg);
	}
	
	@Override
	public XLExprValue<?> eval(ExecutionContext context) throws XLWrapException, XLWrapEOFException {
		XLExprValue<?> val = arg.eval(context);
		if (val == null)
			return null;

		XLExprNumber<?> number = TypeCast.toExprNumber(val);
		if (number instanceof E_Long)
			return new E_Long(((E_Long) number).getValue() / 100);
		else if (number instanceof E_Double)
			return new E_Double(((E_Double) number).getValue() / 100);
		else throw new XLWrapException("Cannot calculate percent value for " + number + ".");
	}

	@Override
	public XLExpr copy() {
		return new E_Percent(arg.copy());
	}

	@Override
	public String toString() {
		return arg + " %";
	}
}

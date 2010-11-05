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
import at.jku.xlwrap.map.expr.val.E_String;
import at.jku.xlwrap.map.expr.val.XLExprValue;
import at.jku.xlwrap.spreadsheet.XLWrapEOFException;



/**
 * @author dorgon
 *
 */
public class E_Concat extends XLExpr2 {
	
	/**
	 * constructor
	 */
	public E_Concat(XLExpr arg1, XLExpr arg2) {
		super(arg1, arg2);
	}

	@Override
	public XLExpr copy() {
		return new E_Concat(arg1.copy(), arg2.copy());
	}
	
	@Override
	public String toString() {
		return "(" + arg1 + " & " + arg2 + ")";
	}

	@Override
	public XLExprValue<String> eval(ExecutionContext context) throws XLWrapException, XLWrapEOFException {
		XLExprValue<?> s1 = arg1.eval(context);
		XLExprValue<?> s2 = arg2.eval(context);		
		String s = "";
		if (s1 != null) s += s1.getValue();
		if (s2 != null) s += s2.getValue();
		return new E_String(s);
	}
}

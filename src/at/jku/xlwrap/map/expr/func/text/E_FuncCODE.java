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
package at.jku.xlwrap.map.expr.func.text;

import at.jku.xlwrap.common.XLWrapException;
import at.jku.xlwrap.exec.ExecutionContext;
import at.jku.xlwrap.map.expr.TypeCast;
import at.jku.xlwrap.map.expr.XLExpr;
import at.jku.xlwrap.map.expr.func.XLExprFunction;
import at.jku.xlwrap.map.expr.val.E_Long;
import at.jku.xlwrap.map.expr.val.XLExprValue;
import at.jku.xlwrap.spreadsheet.XLWrapEOFException;

/**
 * @author Richard Cyganiak (richard@cyganiak.de)
 *
 */
public class E_FuncCODE extends XLExprFunction {

	/**
	 * default constructor 
	 */
	public E_FuncCODE() {
	}

	public E_FuncCODE(XLExpr string) {
		args.add(string);
	}
	
	/* (non-Javadoc)
	 * @see at.langegger.xlwrap.map.expr.XLExpr#eval(at.langegger.xlwrap.exec.ExecutionContext)
	 */
	@Override
	public XLExprValue<?> eval(ExecutionContext context) throws XLWrapException, XLWrapEOFException {
		XLExprValue<?> v1 = args.get(0).eval(context);
		if (v1 == null) return null;
		String s = TypeCast.toString(v1);
		if ("".equals(s)) return null;	// Undefined
		return new E_Long(new Long(Character.codePointAt(s, 0)));
	}

}

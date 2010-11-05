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
package at.jku.xlwrap.map.expr.func.logic;

import at.jku.xlwrap.common.XLWrapException;
import at.jku.xlwrap.exec.ExecutionContext;
import at.jku.xlwrap.map.expr.TypeCast;
import at.jku.xlwrap.map.expr.XLExpr;
import at.jku.xlwrap.map.expr.func.XLExprFunction;
import at.jku.xlwrap.map.expr.val.XLExprValue;
import at.jku.xlwrap.spreadsheet.XLWrapEOFException;

/**
 * @author dorgon
 *
 */
public class E_FuncIF extends XLExprFunction {

	/**
	 * default constructor 
	 */
	public E_FuncIF() {
	}
	
	/**
	 * constructor
	 * 
	 * @param condition
	 * @param then
	 */
	public E_FuncIF(XLExpr cond, XLExpr then) {
		args.add(cond);			// 0
		args.add(then);			// 1
	}
	
	/**
	 * constructor
	 * 
	 * @param condition
	 * @param then
	 * @param otherwise
	 */
	public E_FuncIF(XLExpr cond, XLExpr then, XLExpr otherwise) {
		args.add(cond);			// 0
		args.add(then);			// 1
		args.add(otherwise);	// 2
	}

	@Override
	public XLExprValue<?> eval(ExecutionContext context) throws XLWrapException, XLWrapEOFException {
		if (TypeCast.toBoolean(args.get(0).eval(context), context))
			return args.get(1).eval(context); // may return null
		else
			return (args.size() == 2) ? null : args.get(2).eval(context); // may return null
	}

}

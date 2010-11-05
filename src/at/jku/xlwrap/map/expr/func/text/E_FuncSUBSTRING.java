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
import at.jku.xlwrap.map.expr.val.E_String;
import at.jku.xlwrap.map.expr.val.XLExprValue;
import at.jku.xlwrap.spreadsheet.XLWrapEOFException;

/**
 * Returns a substring of the first argument. The second
 * argument specifies the index of the first character to
 * be returned. The first character of the string is at
 * index 0. The third argument is optional and indicates the
 * index of the last character to be returned. If unspecified,
 * everything from the start character to the end of the string
 * will be returned. Negative indexes indicate positions counted
 * from the end of the string, e.g., -1 is the last character of the
 * string.
 * 
 * @author Richard Cyganiak (richard@cyganiak.de)
 */
public class E_FuncSUBSTRING extends XLExprFunction {

	/**
	 * 
	 */
	public E_FuncSUBSTRING() {
	}
	
	public E_FuncSUBSTRING(XLExpr str, XLExpr start) {
		args.add(str);
		args.add(start);
	}
	
	public E_FuncSUBSTRING(XLExpr str, XLExpr start, XLExpr end) {
		args.add(str);
		args.add(start);
		args.add(end);
	}
	
	/* (non-Javadoc)
	 * @see at.langegger.xlwrap.map.expr.XLExpr#eval(at.langegger.xlwrap.exec.ExecutionContext)
	 */
	@Override
	public XLExprValue<?> eval(ExecutionContext context) throws XLWrapException, XLWrapEOFException {
		XLExprValue<?> v1 = args.get(0).eval(context);
		if (v1 == null)
			return null;
		XLExprValue<?> v2 = args.get(1).eval(context);
		if (v2 == null)
			return null;

		String str = TypeCast.toString(v1.eval(context));
		int start = TypeCast.toInteger(v2, false);
		if (start < 0) {
			start = str.length() + start;
			if (start < 0) {
				start = 0;
			}
		}
		int end = str.length();
		if (args.size() == 3) {
			XLExprValue<?> v3 = args.get(2).eval(context);
			if (v3 == null)
				return null;
			end = TypeCast.toInteger(v3, false);
			if (end < 0) {
				end = str.length() + end;
			}
			if (end > str.length()) {
				end = str.length();
			}
		}
		if (start > end) return null;
		return new E_String(str.substring(start, end));
	}
}

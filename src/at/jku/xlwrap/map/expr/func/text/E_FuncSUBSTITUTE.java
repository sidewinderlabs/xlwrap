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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import at.jku.xlwrap.common.XLWrapException;
import at.jku.xlwrap.exec.ExecutionContext;
import at.jku.xlwrap.map.expr.TypeCast;
import at.jku.xlwrap.map.expr.XLExpr;
import at.jku.xlwrap.map.expr.func.FunctionRegistry;
import at.jku.xlwrap.map.expr.func.XLExprFunction;
import at.jku.xlwrap.map.expr.val.E_String;
import at.jku.xlwrap.map.expr.val.XLExprValue;
import at.jku.xlwrap.spreadsheet.XLWrapEOFException;

/**
 * @author dorgon
 *
 */
public class E_FuncSUBSTITUTE extends XLExprFunction {
	private static final Logger log = LoggerFactory.getLogger(E_FuncSUBSTITUTE.class);
	
	/**
	 * default constructor
	 */
	public E_FuncSUBSTITUTE() {
	}
	
	/**
	 * constructor
	 * 
	 * @param orig
	 * @param search
	 * @param replace
	 */
	public E_FuncSUBSTITUTE(XLExpr orig, XLExpr search, XLExpr replace) {
		addArg(orig);
		addArg(search);
		addArg(replace);
	}

	@Override
	public XLExprValue<?> eval(ExecutionContext context) throws XLWrapException, XLWrapEOFException {
		if (args.size() < 3)
			throw new XLWrapException("Too few arguments for " + FunctionRegistry.getFunctionName(E_FuncSUBSTITUTE.class) + "(string, find, replaceBy).");
		
		XLExprValue<?> string = args.get(0).eval(context);
		if (string == null)
			return null; // silently as usual
		
		XLExprValue<?> find = args.get(1).eval(context);
		if (find == null) {
			log.warn("Find string is null: " + toString() + ", skipping...");
			return null;
		}
		
		XLExprValue<?> replaceBy = args.get(2).eval(context);
		if (replaceBy == null) {
			log.warn("Replace string is null: " + toString() + ", skipping...");
			return null;
		}
		
		// optional forth argument (replace number of occurrences)
		if (args.size() > 3) {
			int occur = TypeCast.toInteger(args.get(3).eval(context), false);
			String result = TypeCast.toString(string);
			for (int i = 0; i < occur; i++)
				result = TypeCast.toString(string)
					.replaceFirst(
						TypeCast.toString(find),
						TypeCast.toString(replaceBy));
			return new E_String(result);

		} else
			return new E_String(TypeCast.toString(string)
				.replaceAll(
					TypeCast.toString(find),
					TypeCast.toString(replaceBy)));
	}

}

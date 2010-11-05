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
package at.jku.xlwrap.map.expr.func.info;

import at.jku.xlwrap.common.XLWrapException;
import at.jku.xlwrap.common.XMLConfigFile;
import at.jku.xlwrap.exec.ExecutionContext;
import at.jku.xlwrap.map.expr.TypeCast;
import at.jku.xlwrap.map.expr.XLExpr;
import at.jku.xlwrap.map.expr.func.XLExprFunction;
import at.jku.xlwrap.map.expr.val.E_String;
import at.jku.xlwrap.map.expr.val.XLExprValue;
import at.jku.xlwrap.spreadsheet.XLWrapEOFException;

/**
 * Returns the result of evaluating an XPath expression
 * against an XML file named as one of the arguments.
 * Useful for loading configuration values into
 * spreadsheet conversions.
 * 
 * @author Richard Cyganiak (richard@cyganiak.de)
 */
public class E_FuncXPATH extends XLExprFunction {

	/**
	 * 
	 */
	public E_FuncXPATH() {
	}
	
	/**
	 * 
	 * @param fileName
	 * @param xPath
	 */
	public E_FuncXPATH(XLExpr fileName, XLExpr xPath) {
		args.add(fileName);			// 0
		args.add(xPath);			// 1
	}
	
	/* (non-Javadoc)
	 * @see at.langegger.xlwrap.map.expr.XLExpr#eval(at.langegger.xlwrap.exec.ExecutionContext)
	 */
	@Override
	public XLExprValue<?> eval(ExecutionContext context) throws XLWrapException, XLWrapEOFException {
		XLExprValue<?> a0 = args.get(0).eval(context);
		if (a0 == null) return null;
		XLExprValue<?> a1 = args.get(1).eval(context);
		if (a1 == null) return null;

		String fileName = TypeCast.toString(a0.eval(context));
		String xPath = TypeCast.toString(a1.eval(context));
		return new E_String(XMLConfigFile.getXPath(fileName, xPath));
	}
}

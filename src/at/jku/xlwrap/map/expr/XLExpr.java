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

import at.jku.xlwrap.common.Copy;
import at.jku.xlwrap.common.XLWrapException;
import at.jku.xlwrap.exec.ExecutionContext;
import at.jku.xlwrap.map.expr.val.XLExprValue;
import at.jku.xlwrap.spreadsheet.XLWrapEOFException;



/**
 * @author dorgon
 *
 */
public interface XLExpr extends Copy<XLExpr>{

	/**
	 * @param context the execution context
	 * @return the evaluated value
	 * @throws XLWrapException 
	 * @throws XLWrapEOFException
	 */
	public XLExprValue<?> eval(ExecutionContext context) throws XLWrapException, XLWrapEOFException;

}

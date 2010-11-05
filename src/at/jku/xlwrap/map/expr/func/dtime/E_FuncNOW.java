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
package at.jku.xlwrap.map.expr.func.dtime;

import java.util.Date;

import at.jku.xlwrap.common.XLWrapException;
import at.jku.xlwrap.exec.ExecutionContext;
import at.jku.xlwrap.map.expr.func.XLExprFunction;
import at.jku.xlwrap.map.expr.val.E_Date;
import at.jku.xlwrap.map.expr.val.XLExprValue;

/**
 * @author dorgon
 *
 */
public class E_FuncNOW extends XLExprFunction {

	/**
	 * constructor
	 */
	public E_FuncNOW() {
	}
	
	@Override
	public XLExprValue<?> eval(ExecutionContext context) throws XLWrapException {
		return new E_Date(new Date());
	}

}

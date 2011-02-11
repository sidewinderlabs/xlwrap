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
package at.jku.xlwrap.map.expr.func.cast;

import at.jku.xlwrap.common.XLWrapException;
import at.jku.xlwrap.exec.ExecutionContext;
import at.jku.xlwrap.map.expr.TypeCast;
import at.jku.xlwrap.map.expr.XLExpr;
import at.jku.xlwrap.map.expr.val.E_Date;
import at.jku.xlwrap.map.expr.val.XLExprValue;
import at.jku.xlwrap.map.expr.val.E_String;
import at.jku.xlwrap.spreadsheet.XLWrapEOFException;
import at.jku.xlwrap.map.expr.func.XLExprFunction;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.text.ParseException;
import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author johngriffin
 *
 */
public class E_FuncDATE_FORMAT extends XLExprFunction {

	private static final Logger log = LoggerFactory.getLogger(E_FuncDATE_FORMAT.class);

	/**
	 * default constructor 
	 */
	public E_FuncDATE_FORMAT() {
	}
	
	public E_FuncDATE_FORMAT(XLExpr arg) {
		args.add(arg);
	}
	
	
	@Override
	public XLExprValue<?> eval(ExecutionContext context) throws XLWrapException, XLWrapEOFException {
		XLExprValue<?> v1 = args.get(0).eval(context);   // date
		XLExprValue<?> v2 = args.get(1).eval(context);   // format
		
		if (v1 == null) {
  		log.warn("DATE_FORMAT, arg0 is null");
		  return null;
		}
    
    String result = null;
    E_Date date = TypeCast.toDateExpr(v1);
		date.setCastedFlag();
		String format = TypeCast.toString(v2);
		
		SimpleDateFormat formatter;
		formatter = new SimpleDateFormat(format);
		result = formatter.format(date.getValue());

		String sDate = TypeCast.toString(date);
//		log.warn("format = " + format + " date = " + date.toString() + "result =" + result);
			
		return new E_String(result);
	}

}

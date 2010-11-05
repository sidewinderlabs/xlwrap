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
package at.jku.xlwrap.map.expr.val;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import at.jku.xlwrap.map.expr.XLExpr;

/**
 * @author dorgon
 *
 */
public class E_Date extends XLExprValue<Date> {
	public static final DateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	
	/**
	 * constructor which parses date from strings based on constant DATE_FORMAT
	 * @throws ParseException
	 */
	public E_Date(String value) throws ParseException {
		super(DATE_FORMAT.parse(value));
	}
	
	/**
	 * native value constructor
	 */
	public E_Date(Date value) {
		super(value);
	}
	
	/**
	 * static parse method
	 * 
	 * @param date
	 * @return
	 * @throws ParseException
	 */
	public static Date parse(String date) throws ParseException {
		return DATE_FORMAT.parse(date);
	}
	
	@Override
	public XLExpr copy() {
		return new E_Date((Date) value.clone());
	}

}

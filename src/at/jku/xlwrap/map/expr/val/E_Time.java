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
public class E_Time extends XLExprValue<Date> {
	public static final DateFormat TIME_FORMAT = new SimpleDateFormat("HH:mm:ss");
	
	/**
	 * parse string to time
	 * @param time as string
	 * @throws ParseException 
	 */
	public E_Time(String time) throws ParseException {
		super(TIME_FORMAT.parse(time));
	}
	
	/**
	 * native value constructor
	 */
	public E_Time(Date time) {
		super(time);
	}
	
	/**
	 * static parse method
	 * 
	 * @param time
	 * @return
	 * @throws ParseException
	 */
	public static Date parse(String time) throws ParseException {
		return TIME_FORMAT.parse(time);
	}

	@Override
	public XLExpr copy() {
		return new E_Time((Date) value.clone());
	}

}

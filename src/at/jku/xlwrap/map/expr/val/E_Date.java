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

import org.pojava.datetime.DateTime;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import at.jku.xlwrap.map.expr.XLExpr;
import java.util.GregorianCalendar;

/**
 * @author dorgon
 *
 */
public class E_Date extends XLExprValue<Date> {
	public static final DateTime DATE_TIME = new DateTime();
	
	/**
	 * constructor which parses date from strings based on constant DATE_FORMAT
	 * @throws ParseException
	 */
	public E_Date(String value) throws ParseException {
		super(DATE_TIME.parse(value).toDate());
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
            try {
		return DATE_TIME.parse(date).toDate();
            } catch (Exception e){
                //Ok now lets try it as a day in numbers.
                int days = Integer.parseInt(date);
                if (days > 60){
                    days--; //Fix the Excell bug of counting none existing 29 Febuary 1900 as day 60
                }
                GregorianCalendar calendar = new GregorianCalendar (1900, 0, days, 0 , 0, 0);
                return calendar.getTime();
            }
	}
	
	@Override
	public XLExpr copy() {
		return new E_Date((Date) value.clone());
	}

    public static void main(String[] args) throws ParseException {
        Date date = parse ("1");
        System.out.println(date);
        date = parse ("100");
        System.out.println(date);
        date = parse ("40337");
        System.out.println(date);
        date = parse ("2345");
        System.out.println(date);
        date = parse ("12345");
        System.out.println(date);
        date = parse ("40338");
        System.out.println(date);
    }
}

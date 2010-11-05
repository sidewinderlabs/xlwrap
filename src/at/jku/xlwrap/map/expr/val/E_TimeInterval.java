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

import java.util.Hashtable;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import at.jku.xlwrap.common.XLWrapException;
import at.jku.xlwrap.map.expr.XLExpr;

/**
 * @author dorgon
 *
 */
public class E_TimeInterval extends XLExprValue<Integer> {
	public static final String YEARS = "YEAR"; // also used for toString()
	public static final String MONTHS = "MONTH";
	public static final String WEEKS = "WEEK";
	public static final String DAYS = "DAY";
	public static final String HOURS = "HOUR";
	public static final String MINUTES = "MINUTE";
	public static final String SECONDS = "SECOND";
	public static final String MILISECONDS = "MILISECOND";
	
	public static final String REGEX_MATCH_INTERVAL = "^([+-])?(?:\\s*)(\\d+)(?:\\s*)?([A-Za-z]+)$";
	public static final int REGEX_SIGN = 1;
	public static final int REGEX_VALUE = 2;
	public static final int REGEX_TYPE = 3;
	public static final Hashtable<String, String> typeMap;
	
	static {
		typeMap = new Hashtable<String, String>();
		typeMap.put("Y", YEARS);
		typeMap.put("YEAR", YEARS);
		typeMap.put("YEARS", YEARS);
		typeMap.put("MO", MONTHS); // M => MINUTE
		typeMap.put("MONTH", MONTHS);
		typeMap.put("MONTHS", MONTHS);
		typeMap.put("W", WEEKS);
		typeMap.put("WEEK", WEEKS);
		typeMap.put("WEEKS", WEEKS);
		typeMap.put("D", DAYS);
		typeMap.put("DAY", DAYS);
		typeMap.put("DAYS", DAYS);
		typeMap.put("H", HOURS);
		typeMap.put("HOUR", HOURS);
		typeMap.put("HOURS", HOURS);
		typeMap.put("M", MINUTES);
		typeMap.put("MINUTE", MINUTES);
		typeMap.put("MINUTES", MINUTES);
		typeMap.put("S", SECONDS);
		typeMap.put("SECOND", SECONDS);
		typeMap.put("SECONDS", SECONDS);
		typeMap.put("MS", MILISECONDS);
		typeMap.put("MILISECOND", MILISECONDS);
		typeMap.put("MILISECONDS", MILISECONDS);
	}
	
	private String intervalType;
	
	/**
	 * @param value
	 * @return E_TimeInterval for parsed value
	 * @throws XLWrapException 
	 */
	public static E_TimeInterval parse(String value) throws XLWrapException {
		Matcher m = Pattern.compile(REGEX_MATCH_INTERVAL).matcher(value.trim().toUpperCase());
		if (m.find()) {
			Integer val = Integer.parseInt(m.group(REGEX_VALUE));
			if (m.group(REGEX_SIGN) != null && m.group(REGEX_SIGN).equals("-"))
				val = -val;
			String type = typeMap.get(m.group(REGEX_TYPE));
			if (type == null)
				throw new XLWrapException("Unknown interval type: " + m.group(REGEX_TYPE));
			
			return new E_TimeInterval(val, type); 
		} else
			throw new XLWrapException("Invalid time interval: " + value);
	}
	
	/**
	 * constructor 
	 */
	public E_TimeInterval(int interval, String intervalType) {
		super(interval);
		this.intervalType = intervalType;
	}
	
	/**
	 * @return the interval
	 */
	public int getInterval() {
		return value;
	}
	
	/**
	 * @return the intervalType
	 */
	public String getIntervalType() {
		return intervalType;
	}
	
	@Override
	public Integer getValue() {
		throw new UnsupportedOperationException("Time interval has no simple value.");
	}
	
	@Override
	public XLExpr copy() {
		return new E_TimeInterval(new Integer(value), new String(intervalType));
	}
	
	@Override
	public String toString() {
		return value + " " + intervalType + ((value > 1 || value < 1) ? "S" : ""); 
	}
}

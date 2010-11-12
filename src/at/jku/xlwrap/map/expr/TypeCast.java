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

import java.text.ParseException;
import java.text.NumberFormat;
import java.util.Date;
import java.util.Locale;

import at.jku.xlwrap.common.XLWrapException;
import at.jku.xlwrap.exec.ExecutionContext;
import at.jku.xlwrap.map.expr.val.E_BlankNode;
import at.jku.xlwrap.map.expr.val.E_Boolean;
import at.jku.xlwrap.map.expr.val.E_Date;
import at.jku.xlwrap.map.expr.val.E_Double;
import at.jku.xlwrap.map.expr.val.E_Long;
import at.jku.xlwrap.map.expr.val.E_String;
import at.jku.xlwrap.map.expr.val.E_URI;
import at.jku.xlwrap.map.expr.val.XLExprNumber;
import at.jku.xlwrap.map.expr.val.XLExprValue;

/**
 * @author dorgon
 *
 */
public class TypeCast {
	
	/**
	 * @param value
	 * @param context
	 * @return
	 * @throws XLWrapException
	 */
	@SuppressWarnings("unchecked")
	public static Boolean toBoolean(XLExprValue<?> value, ExecutionContext context) throws XLWrapException {
		if (value == null)
			return false; // interpret null as false
		
		if (value instanceof E_Boolean)
			return ((E_Boolean) value).getValue();
		
		else if (value instanceof E_String) {
			String norm = ((E_String) value).getValue().trim().toLowerCase();
			if (norm.equals("true") || norm.equals("wahr") || norm.equals("1") || norm.equals("yes") || norm.equals("on"))
				return true;
			else if (norm.length() == 0 || // empty string
				norm.equals("false") || norm.equals("falsch") || norm.equals("0") || norm.equals("no") || norm.equals("off"))
				return false;
		
		} else if (value instanceof XLExprNumber)
			return ((XLExprNumber) value).compareTo(new E_Long(0L)) > 0;
		
		else if (value instanceof E_BlankNode ||
				value instanceof E_URI ||
				value instanceof E_Date)
			return true;		
		
		throw new XLWrapException("Cannot cast " + value + " to boolean.");
	}

	public static E_Boolean toBooleanExpr(XLExprValue<?> value, ExecutionContext context) throws XLWrapException {
		return toBoolean(value, context) ? E_Boolean.TRUE : E_Boolean.FALSE;
	}
	
	/**
	 * @param value - already evaluated(!)
	 * @return
	 * @throws XLWrapException 
	 */
	public static String toString(XLExprValue<?> value) throws XLWrapException {
		if (value == null) return null;
		if (value.getValue() instanceof String)
			return (String) value.getValue();
		else
			return value.getValue().toString(); // use Java's implicit toString() behavior
	}

	/**
	 * @param value - already evaluated(!)
	 * @return
	 * @throws XLWrapException
	 */
	public static E_String toStringExpr(XLExprValue<?> value) throws XLWrapException {
		return new E_String(toString(value));
	}
	
	/**
	 * @param string
	 * @return
	 * @throws XLWrapException 
	 */
	public static XLExprNumber<?> toExprNumber(XLExprValue<?> value) throws XLWrapException {
		if (value instanceof XLExprNumber)
			return (XLExprNumber<?>) value;
		
		else if (value instanceof E_Boolean)
			return new E_Long(((E_Boolean) value).getValue() ? 1L : 0L);
		
		else if (value instanceof E_String) {
			try {
				Number number = NumberFormat.getNumberInstance(Locale.UK).parse(((E_String) value).getValue());
				if (number instanceof Long) {
					return new E_Long(number.longValue());
				} else
					return new E_Double(number.doubleValue());
			} catch (Exception e) {
				throw new XLWrapException("Cannot cast " + value + " to number.", e);
			}
		}
		
		throw new XLWrapException("Cannot cast " + value + " to number.");
	}

	/**
	 * @param value
	 * @return
	 * @throws XLWrapException
	 */
	public static Date toDate(XLExprValue<?> value) throws XLWrapException {
		if (value instanceof E_Date)
			return ((E_Date) value).getValue();
		
		else if (value instanceof E_String) {
			try {
				return E_Date.parse(((E_String) value).getValue());
			} catch (ParseException e) {
				throw new XLWrapException("Cannot cast " + value + " to date.", e);
			}
		}
		
		throw new XLWrapException("Cannot cast " + value + " to date.");
	}

	/**
	 * @param value
	 * @return
	 * @throws XLWrapException
	 */
	public static E_Date toDateExpr(XLExprValue<?> value) throws XLWrapException {
		return new E_Date(toDate(value));
	}
	
	
	/**
	 * @param value
	 * @return
	 * @throws XLWrapException 
	 */
	public static Double toDouble(XLExprValue<?> value) throws XLWrapException {
		value = toExprNumber(value);
		
		if (value instanceof E_Double)
			return ((E_Double) value).getValue();
		else if (value instanceof E_Long)
			return ((E_Long) value).getValue().doubleValue();

		throw new XLWrapException("Cannot cast " + value + " to double.");
	}
	
	/**
	 * @param value
	 * @return
	 * @throws XLWrapException 
	 */
	public static E_Double toDoubleExpr(XLExprValue<?> value) throws XLWrapException {
		return new E_Double(toDouble(value));
	}
	
	/**
	 * @param value
	 * @return
	 * @throws XLWrapException
	 */
	public static Long toLong(XLExprValue<?> value) throws XLWrapException {
		value = toExprNumber(value);
		
		if (value instanceof E_Long)
			return ((E_Long) value).getValue();
		else if (value instanceof E_Double)
			return ((E_Double) value).getValue().longValue();

		throw new XLWrapException("Cannot cast " + value + " to integer.");
	}
	
	/**
	 * @param value
	 * @return
	 * @throws XLWrapException 
	 */
	public static E_Long toLongExpr(XLExprValue<?> value) throws XLWrapException {
		return new E_Long(toLong(value));
	}
	
	/**
	 * @param value
	 * @param lossy if true, cast down bigger values
	 * @return
	 * @throws XLWrapException
	 */
	public static Integer toInteger(XLExprValue<?> value, boolean lossy) throws XLWrapException {
		value = toExprNumber(value);
		
		if (value instanceof E_Long) {
			long l = ((E_Long) value).getValue();
			if (lossy || (int) l == l)
				return (int) l;
			else
				throw new XLWrapException("Value " + l + " is to big for casting down to integer."); 

		} else if (value instanceof E_Double) {
			double d = ((E_Double) value).getValue();
			if (lossy || d == (int) d)
				return (int) d;
			else
				throw new XLWrapException("Value " + d + " is not an integer and cannot be losslessly cast to integer.");
		}
		
		throw new XLWrapException("Cannot cast " + value + " to integer.");
	}


}

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
package test.xlwrap.mapping.expr;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.text.ParseException;

import org.junit.Test;

import test.xlwrap.XLWrapTestCase;
import at.jku.xlwrap.map.expr.val.E_Boolean;
import at.jku.xlwrap.map.expr.val.E_Date;
import at.jku.xlwrap.map.expr.val.E_Double;
import at.jku.xlwrap.map.expr.val.E_Long;
import at.jku.xlwrap.map.expr.val.E_String;

/**
 * @author dorgon
 *
 * compareTo() is defined for equal types, hence, TypeCast.* will be used before calling compareTo()
 */
public class TestCompareTo extends XLWrapTestCase {
		
	@Test
	public void testCompareBoolean() {
		assertEquals(0, E_Boolean.TRUE.compareTo(E_Boolean.TRUE));
		assertEquals(1, E_Boolean.TRUE.compareTo(E_Boolean.FALSE));
		assertEquals(-1, E_Boolean.FALSE.compareTo(E_Boolean.TRUE));
	}
	
	@Test
	public void testCompareInteger() {
		assertEquals(0, new E_Long(23L).compareTo(new E_Long(23L)));
		assertEquals(1, new E_Long(26L).compareTo(new E_Long(23L)));
		assertEquals(-1, new E_Long(20L).compareTo(new E_Long(23L)));
	}

	@Test
	public void testCompareDouble() {
		assertEquals(0, new E_Double(23.23d).compareTo(new E_Double(23.23d)));
		assertEquals(1, new E_Double(26.2d).compareTo(new E_Double(23.23d)));
		assertEquals(-1, new E_Double(20.34d).compareTo(new E_Double(23.23d)));
	}
	
	@Test
	public void testCompareDate() throws ParseException {
		assertEquals(0, new E_Date("2009-12-23 14:23:09").compareTo(new E_Date("2009-12-23 14:23:09")));
		assertEquals(1, new E_Date("2009-12-23 16:23:09").compareTo(new E_Date("2009-12-23 12:23:09")));
		assertEquals(-1, new E_Date("2009-12-23 12:23:09").compareTo(new E_Date("2009-12-23 16:23:09")));
	}

//	@Test
//	public void testCompareTime() throws ParseException {
//		assertEquals(0, new E_Time("14:23:09").compareTo(new E_Date("14:23:09")));
//		assertEquals(1, new E_Time("16:23:09").compareTo(new E_Date("12:23:09")));
//		assertEquals(-1, new E_Time("12:23:09").compareTo(new E_Date("16:23:09")));		
//	}

//	@Test TODO
//	public void testCompareTimeInterval() throws XLWrapException {
//		assertEquals(0, new E_TimeInterval("23d")) //TODO time interval should also support values like "+1w 1h 20m"...
//	}
	
	@Test
	public void testCompareString() {
		assertEquals(0, new E_String("abra").compareTo(new E_String("abra")));
		assertTrue(new E_String("abra").compareTo(new E_String("aaaa")) > 0);
		assertTrue(new E_String("abra").compareTo(new E_String("cadabra")) < 0);
	}
	
}

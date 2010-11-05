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

import java.text.ParseException;

import org.junit.Before;
import org.junit.Test;

import test.xlwrap.XLWrapTestCase;
import at.jku.xlwrap.common.XLWrapException;
import at.jku.xlwrap.exec.ExecutionContext;
import at.jku.xlwrap.map.expr.TypeCast;
import at.jku.xlwrap.map.expr.val.E_BlankNode;
import at.jku.xlwrap.map.expr.val.E_Boolean;
import at.jku.xlwrap.map.expr.val.E_Double;
import at.jku.xlwrap.map.expr.val.E_Long;
import at.jku.xlwrap.map.expr.val.E_String;
import at.jku.xlwrap.map.expr.val.E_URI;

/**
 * @author dorgon
 *
 */
public class TestTypeCast extends XLWrapTestCase {
	private ExecutionContext context;
	
	@Before
	public void setUp() {
		context = new ExecutionContext();
	}

	@Test
	public void testToBoolean() throws XLWrapException, ParseException {
		assertEquals(true, TypeCast.toBoolean(E_Boolean.TRUE, context));
		assertEquals(false, TypeCast.toBoolean(E_Boolean.FALSE, context));
		
		assertEquals(true, TypeCast.toBoolean(new E_Long(23L), context));
		assertEquals(false, TypeCast.toBoolean(new E_Long(-2L), context));
		assertEquals(false, TypeCast.toBoolean(new E_Long(0L), context));

		assertEquals(true, TypeCast.toBoolean(new E_Double(23.23d), context));
		assertEquals(false, TypeCast.toBoolean(new E_Double(-23.2d), context));
		assertEquals(false, TypeCast.toBoolean(new E_Double(0d), context));

		assertEquals(true, TypeCast.toBoolean(new E_String("true"), context));
		assertEquals(false, TypeCast.toBoolean(new E_String("false"), context));
		assertEquals(true, TypeCast.toBoolean(new E_String("1"), context));
		assertEquals(false, TypeCast.toBoolean(new E_String("0"), context));
		assertEquals(true, TypeCast.toBoolean(new E_String("yes"), context));
		assertEquals(false, TypeCast.toBoolean(new E_String("no"), context));

		assertEquals(true, TypeCast.toBoolean(new E_URI("http://example.com/"), context));
		assertEquals(true, TypeCast.toBoolean(new E_BlankNode(), context));

//		assertEquals(true, TypeCast.toBoolean(new E_Date("2009-12-21"), context));
//		assertEquals(true, TypeCast.toBoolean(new E_Time("12:34:23"), context));
//		assertEquals(true, TypeCast.toBoolean(new E_TimeInterval(2, E_TimeInterval.DAYS), context));
	}
	
	@Test
	public void testToExprNumber() throws XLWrapException {
		// integer
		assertEquals(23L, TypeCast.toExprNumber(new E_Long(23L)).getValue());
		assertEquals(23L, TypeCast.toExprNumber(new E_String("23")).getValue());
		assertEquals(0L, TypeCast.toExprNumber(E_Boolean.FALSE).getValue());
		assertEquals(1L, TypeCast.toExprNumber(E_Boolean.TRUE).getValue());
		
		// double
		assertEquals(23.12, TypeCast.toExprNumber(new E_Double("23.12d")).getValue());
		assertEquals(23.12, TypeCast.toExprNumber(new E_String("23.12")).getValue());
		
		//TODO date, time time interval ???
	}
	
	@Test
	public void testToString() throws XLWrapException {
		assertEquals("true", TypeCast.toString(E_Boolean.TRUE));
		assertEquals("false", TypeCast.toString(E_Boolean.FALSE));

		assertEquals("12", TypeCast.toString(new E_Long(12L)));
		assertEquals("12.342", TypeCast.toString(new E_Double(12.342d)));

		assertEquals("foo", TypeCast.toString(new E_String("foo")));
		assertEquals("http://example.org/foo", TypeCast.toString(new E_URI("http://example.org/foo")));
		
		// TODO date, time, time interval
	}
	
//	@Test
//	public void testToDate() throws XLWrapException {
////		assertEquals("") //TODO
//	}
}

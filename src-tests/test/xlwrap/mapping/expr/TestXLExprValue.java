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
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertSame;

import java.text.ParseException;

import org.junit.Before;
import org.junit.Test;

import test.xlwrap.XLWrapTestCase;
import at.jku.xlwrap.common.XLWrapException;
import at.jku.xlwrap.exec.ExecutionContext;
import at.jku.xlwrap.map.expr.E_Add;
import at.jku.xlwrap.map.expr.E_Multiply;
import at.jku.xlwrap.map.expr.E_Substract;
import at.jku.xlwrap.map.expr.val.E_BlankNode;
import at.jku.xlwrap.map.expr.val.E_Boolean;
import at.jku.xlwrap.map.expr.val.E_Date;
import at.jku.xlwrap.map.expr.val.E_Double;
import at.jku.xlwrap.map.expr.val.E_Long;
import at.jku.xlwrap.map.expr.val.E_String;
import at.jku.xlwrap.map.expr.val.E_Time;
import at.jku.xlwrap.map.expr.val.E_URI;
import at.jku.xlwrap.map.expr.val.XLExprValue;
import at.jku.xlwrap.spreadsheet.XLWrapEOFException;

import com.hp.hpl.jena.rdf.model.AnonId;


/**
 * @author dorgon
 * 
 * Testing:
 * E_Boolean
 * E_Integer
 * E_Double
 * E_Date
 * E_Time
 * E_String
 * E_URI
 * E_BlankNode
 */
public class TestXLExprValue extends XLWrapTestCase {
	private ExecutionContext context;
	
	@Before
	public void setUp() {
		context = new ExecutionContext();
	}
	
	@Test
	public void testEvalAndGetValue() throws XLWrapException, ParseException {
		assertEquals(false, E_Boolean.FALSE.eval(context).getValue());
		assertEquals(true, E_Boolean.TRUE.eval(context).getValue());
		assertEquals(-123L, new E_Long(-123L).eval(context).getValue());
		assertEquals(-.341d, new E_Double(-.341).eval(context).getValue());
		assertEquals(E_Date.parse("2009-12-23 23:31:39"), new E_Date("2009-12-23 23:31:39").eval(context).getValue());
		assertEquals(E_Time.parse("21:31:39"), new E_Time("21:31:39").eval(context).getValue());
		assertEquals("asdf jklö 1234 ><^°!\"§$%&/()=?`´", new E_String("asdf jklö 1234 ><^°!\"§$%&/()=?`´").eval(context).getValue());
		assertEquals("http://foo.com/asdf.html?foo=34&foo2=abc", new E_URI("http://foo.com/asdf.html?foo=34&foo2=abc").eval(context).getValue());
		AnonId id = AnonId.create();
		assertEquals(id, new E_BlankNode(id).eval(context).getValue());
	}
	
	@Test
	public void testToString() throws ParseException, XLWrapException {
		String[] expected = new String[] { "foo", "false", "true", 
//				E_Date.parse("2007-12-31 12:03:34").toString(),
//				E_Time.parse("20:21:20").toString(),
//				E_TimeInterval.parse("2d").toString(),
				"asdf jklö 1234 ><^°!\"§$%&/()=?`´", "http://example.com/path#foo?x=y&foo=true", ""+Double.MAX_VALUE, ""+Double.MIN_VALUE, ""+Long.MAX_VALUE, ""+Long.MIN_VALUE };
		int i = 0;
		for (XLExprValue<?> val : createTestValues())
			assertEquals(expected[i++], val.getValue().toString());
	}
	
	@Test
	public void testEval() throws ParseException, XLWrapException {
		for (XLExprValue<?> val : createTestValues())
			assertEquals(val.eval(context).getValue(), val.getValue());
	}
	
	@Test
	public void testAdd() throws XLWrapException, ParseException, XLWrapEOFException {

		// first arg is integer
		
		assertEquals(100L, new E_Add(new E_Long(32L), new E_Long(68L)).eval(context).getValue());
		assertEquals(52.43d, new E_Add(new E_Long(20L), new E_Double(32.43)).eval(context).getValue());
//		assertEquals(E_Date.parse("2009-12-24 23:21:01"), new E_Add(new E_Integer(3), new E_Date("2009-12-21 23:21:01")).eval(context).getValue());
//		assertEquals(E_Time.parse("21:34:20"), new E_Add(new E_Integer(20), new E_Time("21:54:20")).eval(context).getValue());
		assertEquals(63L, new E_Add(new E_Long(20L), new E_String("43")).eval(context).getValue());
		assertEquals(63.342d, new E_Add(new E_Long(20L), new E_String("43.342")).eval(context).getValue());
		
		// first arg is double
		
		assertEquals(64.43d, new E_Add(new E_Double(32.43), new E_Long(32L)).eval(context).getValue());
		assertEquals(423.321d, (Double) new E_Add(new E_Double(400.32), new E_Double(23.001)).eval(context).getValue(), 0.001d);
//		assertEquals(E_Date.parse("2009-12-22 20:21:01"), new E_Add(new E_Double(1.5), new E_Date("2009-12-21 08:21:01")).eval(context).getValue());
//		assertEquals(E_Time.parse("21:34:50"), new E_Add(new E_Double(1.5), new E_Time("21:33:20")).eval(context).getValue());
		assertEquals(63.23d, (Double) new E_Add(new E_Double(20.23), new E_String("43")).eval(context).getValue(), 0.001d);
		assertEquals(63.572d, (Double) new E_Add(new E_Double(20.23), new E_String("43.342")).eval(context).getValue(), 0.001d);
		
		// TODO first arg is date/time

		// first arg is string

		assertEquals(64.43d, new E_Add(new E_String("32.43"), new E_Long(32L)).eval(context).getValue());
		assertEquals(423.321d, (Double) new E_Add(new E_String("400.32"), new E_Double(23.001)).eval(context).getValue(), 0.001d);
//		assertEquals(E_Date.parse("2009-12-22 20:21:01"), new E_Add(new E_String("1.5"), new E_Date("2009-12-21 08:21:01")).eval(context).getValue());
//		assertEquals(E_Time.parse("21:34:50"), new E_Add(new E_String("1.5"), new E_Time("21:33:20")).eval(context).getValue());
		try { new E_Add(new E_String("foo"), new E_String("bar")).eval(context).getValue();
		throw new AssertionError("Exception expected."); } catch (XLWrapException e) {}		
	}
	
	@Test
	public void testSubstract() throws XLWrapException, XLWrapEOFException {

		// first arg is integer
		
		assertEquals(100L, new E_Substract(new E_Long(32L), new E_Long(-68L)).eval(context).getValue());
		assertEquals(52.43d, new E_Substract(new E_Long(20L), new E_Double(-32.43)).eval(context).getValue());
//		try { new E_Substract(new E_Integer(3), new E_Date("2009-12-21 23:21:01")).eval(context).getValue();
//		throw new AssertionError("Exception expected."); } catch (XLWrapException e) {}
//		try { new E_Substract(new E_Integer(20), new E_Time("21:54:20")).eval(context).getValue();
//		throw new AssertionError("Exception expected."); } catch (XLWrapException e) {}
		assertEquals(63L, new E_Substract(new E_Long(20L), new E_String("-43")).eval(context).getValue());
		assertEquals(63.342d, new E_Substract(new E_Long(20L), new E_String("-43.342")).eval(context).getValue());
		
		// first arg is double
		
		assertEquals(64.43d, new E_Substract(new E_Double(32.43), new E_Long(-32L)).eval(context).getValue());
		assertEquals(423.321d, (Double) new E_Substract(new E_Double(400.32), new E_Double(-23.001)).eval(context).getValue(), 0.001d);
//		try { new E_Substract(new E_Double(1.5), new E_Date("2009-12-21 08:21:01")).eval(context).getValue();
//		throw new AssertionError("Exception expected."); } catch (XLWrapException e) {}
//		try { new E_Substract(new E_Double(1.5), new E_Time("21:33:20")).eval(context).getValue();
//		throw new AssertionError("Exception expected."); } catch (XLWrapException e) {}
		assertEquals(63.23d, (Double) new E_Substract(new E_Double(20.23), new E_String("-43")).eval(context).getValue(), 0.001d);
		assertEquals(63.572d, (Double) new E_Substract(new E_Double(20.23), new E_String("-43.342")).eval(context).getValue(), 0.001d);
		
		// TODO first arg is date/time

		// first arg is string

		assertEquals(64.43d, (Double) new E_Substract(new E_String("32.43"), new E_Long(-32L)).eval(context).getValue(), 0.001d);
		assertEquals(423.321d, (Double) new E_Substract(new E_String("400.32"), new E_Double(-23.001)).eval(context).getValue(), 0.001d);
//		try { new E_Add(new E_String("1.5"), new E_Date("2009-12-21 08:21:01")).eval(context).getValue();
//		throw new AssertionError("Exception expected."); } catch (XLWrapException e) {}
//		try { new E_Add(new E_String("1.5"), new E_Time("21:33:20")).eval(context).getValue();
//		throw new AssertionError("Exception expected."); } catch (XLWrapException e) {}
		try { new E_Substract(new E_String("foo"), new E_String("bar")).eval(context).getValue();
		throw new AssertionError("Exception expected."); } catch (XLWrapException e) {}
	}
	
	@Test
	public void testMultiply() throws XLWrapException, XLWrapEOFException {
		// first arg is integer
		
		
		// TODO all
		
		assertEquals(-2176L, new E_Multiply(new E_Long(32L), new E_Long(-68L)).eval(context).getValue());
		assertEquals(-648.6d, (Double) new E_Multiply(new E_Long(20L), new E_Double(-32.43)).eval(context).getValue(), 0.001d);
//		try { new E_Substract(new E_Integer(3), new E_Date("2009-12-21 23:21:01")).eval(context).getValue();
//		throw new AssertionError("Exception expected."); } catch (XLWrapException e) {}
//		try { new E_Substract(new E_Integer(20), new E_Time("21:54:20")).eval(context).getValue();
//		throw new AssertionError("Exception expected."); } catch (XLWrapException e) {}
		assertEquals(-860L, new E_Multiply(new E_Long(20L), new E_String("-43")).eval(context).getValue());
		assertEquals(-866.84d,(Double) new E_Multiply(new E_Long(20L), new E_String("-43.342")).eval(context).getValue(), 0.001d);
		
		// first arg is double
		
		assertEquals(-1037.76d, (Double) new E_Multiply(new E_Double(32.43), new E_Long(-32L)).eval(context).getValue(), 0.001d);
		assertEquals(-9207.76032d, (Double) new E_Multiply(new E_Double(400.32), new E_Double(-23.001)).eval(context).getValue(), 0.001d);
//		try { new E_Substract(new E_Double(1.5), new E_Date("2009-12-21 08:21:01")).eval(context).getValue();
//		throw new AssertionError("Exception expected."); } catch (XLWrapException e) {}
//		try { new E_Substract(new E_Double(1.5), new E_Time("21:33:20")).eval(context).getValue();
//		throw new AssertionError("Exception expected."); } catch (XLWrapException e) {}
		assertEquals(-869.89d, (Double) new E_Multiply(new E_Double(20.23), new E_String("-43")).eval(context).getValue(), 0.001d);
		assertEquals(876.80866d, (Double) new E_Multiply(new E_Double(-20.23), new E_String("-43.342")).eval(context).getValue(), 0.001d);
		
		// TODO first arg is date/time

		// first arg is string

		assertEquals(-1037.76d, (Double) new E_Multiply(new E_String("32.43"), new E_Long(-32L)).eval(context).getValue(), 0.001d);
		assertEquals(9207.76032d, (Double) new E_Multiply(new E_String("-400.32"), new E_Double(-23.001)).eval(context).getValue(), 0.001d);
//		try { new E_Add(new E_String("1.5"), new E_Date("2009-12-21 08:21:01")).eval(context).getValue();
//		throw new AssertionError("Exception expected."); } catch (XLWrapException e) {}
//		try { new E_Add(new E_String("1.5"), new E_Time("21:33:20")).eval(context).getValue();
//		throw new AssertionError("Exception expected."); } catch (XLWrapException e) {}
		try { new E_Multiply(new E_String("foo"), new E_String("bar")).eval(context).getValue();
		throw new AssertionError("Exception expected."); } catch (XLWrapException e) {}		
	}
	
	@Test
	public void testCopy() throws ParseException, XLWrapException {
		for (XLExprValue<?> val : createTestValues()) {
			XLExprValue<?> copy = (XLExprValue<?>) val.copy();
			assertEquals(val.toString(), copy.toString());
			if (val instanceof E_Boolean) // except for boolean, copy must be different
				assertSame(val, copy);
			else
				assertNotSame(val, copy);
		}
	}
	
	private XLExprValue<?>[] createTestValues() throws ParseException, XLWrapException {
		return new XLExprValue<?>[] {
				new E_BlankNode(AnonId.create("foo")),
				E_Boolean.FALSE, E_Boolean.TRUE,
//				new E_Date("2007-12-31 12:03:34"),
//				new E_Time("20:21:20"),
//				E_TimeInterval.parse("2d"),
				new E_String("asdf jklö 1234 ><^°!\"§$%&/()=?`´"),
				new E_URI("http://example.com/path#foo?x=y&foo=true"),
				// XLExprNumber
				new E_Double(Double.MAX_VALUE), new E_Double(Double.MIN_VALUE),
				new E_Long(Long.MAX_VALUE), new E_Long(Long.MIN_VALUE)
			};
	}
}

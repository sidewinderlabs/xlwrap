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
package test.xlwrap.mapping.range;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;

import test.xlwrap.XLWrapTestCase;
import at.jku.xlwrap.common.Utils;
import at.jku.xlwrap.common.XLWrapException;
import at.jku.xlwrap.exec.ExecutionContext;
import at.jku.xlwrap.map.range.AnyRange;
import at.jku.xlwrap.map.range.BoxRange;
import at.jku.xlwrap.map.range.CellRange;
import at.jku.xlwrap.map.range.FullSheetRange;
import at.jku.xlwrap.map.range.MultiRange;
import at.jku.xlwrap.map.range.NullRange;
import at.jku.xlwrap.map.range.Range;
import at.jku.xlwrap.map.range.Range.CellIterator;
import at.jku.xlwrap.spreadsheet.XLWrapEOFException;

/**
 * @author dorgon
 *
 */
public class TestMultiRange extends XLWrapTestCase {
	private ExecutionContext context;
	private MultiRange MX;
	
	@Before
	public void setUp() throws XLWrapException {
		context = new ExecutionContext();
		context.setActiveTemplate(createPersonTemplate());
		MX = mr("A3:B4; H8; '" + TEST_FILE_EMPTY + "'#$Sheet1.A6; Sheet2.*");
	}
	
	private MultiRange mr(String str) throws XLWrapException {
		Range r = Utils.parseRange(str);
		return (MultiRange) r;
	}
	
	@Test
	public void testAbsoluteRange() throws XLWrapException {
		assertEquals("'" + TEST_FILE_DATA1 + "'#$#1.A3:#1.B4; " +
					"'" + TEST_FILE_DATA1 + "'#$#1.H8; " +
					"'" + TEST_FILE_EMPTY + "'#$#1.A6; " +	// was explicitly set
					"'" + TEST_FILE_DATA1 + "'#$#2.*; ", MX.getAbsoluteRange(context).toString());
	}
	
	@Test
	public void testCellIterator() throws XLWrapException, XLWrapEOFException {
		MultiRange mr = mr("'" + TEST_FILE_DATA1 + "'#$'" + TEST_SHEET_DATA1_1 + "'.A17:B18; " +
				"'" + TEST_FILE_DATA1 + "'#$'" + TEST_SHEET_DATA1_1 + "'.D17:D18; " +
				"'" + TEST_FILE_DATA1 + "'#$'" + TEST_SHEET_DATA1_1 + "'.D21; " +
				"Sheet2.*");
		CellIterator it = mr.getCellIterator(context);
		
		// default sheet 'Tests 1'.A17:B18
		assertEquals("Product1", it.next().getText());
		assertEquals("342", it.next().getText());
		assertEquals("Product2", it.next().getText());
		assertEquals("4333", it.next().getText());
		
		// D17:D18
		assertEquals("376", it.next().getText());
		assertEquals("5655", it.next().getText());
		
		// D21
		assertEquals("10653", it.next().getText());
		
		// Sheet2.*
		assertEquals("first name", it.next().getText());
		assertEquals("last name", it.next().getText());
		assertEquals("email", it.next().getText());
		assertEquals("Tom", it.next().getText());
		assertEquals("", it.next().getText());
		assertEquals("th@ex.com", it.next().getText());
		assertEquals("", it.next().getText());
		assertEquals("Presley", it.next().getText());
		assertEquals("jp@ex.com", it.next().getText());

		assertFalse(it.hasNext());
	}
	
	@Test
	public void testSubsumes() throws XLWrapException {
		// null range
		assertTrue(MX.subsumes(NullRange.INSTANCE, context)); // any range subsumes NullRange
		
		// cell range
		CellRange cellOk = (CellRange) Utils.parseRange("A4");
		CellRange cellOk2 = (CellRange) Utils.parseRange("H8");
		CellRange cellOk3 = (CellRange) Utils.parseRange("'" + TEST_FILE_DATA1 + "'#$#2.ZZZ348");
		assertTrue(MX.subsumes(cellOk, context));
		assertTrue(MX.subsumes(cellOk2, context));
		assertTrue(MX.subsumes(cellOk3, context));
		
		CellRange cellOther = (CellRange) Utils.parseRange("A2");
		CellRange cellOther2 = (CellRange) Utils.parseRange("H9");
		CellRange cellOther3 = (CellRange) Utils.parseRange("Sheet3.A4");
		CellRange cellOther4 = (CellRange) Utils.parseRange("'" + TEST_FILE_EMPTY + "'#$'" + "Sheet2"+ "'.A4");
		assertFalse(MX.subsumes(cellOther, context));
		assertFalse(MX.subsumes(cellOther2, context));
		assertFalse(MX.subsumes(cellOther3, context));
		assertFalse(MX.subsumes(cellOther4, context));
		
		// box range
		BoxRange boxOk = (BoxRange) Utils.parseRange("A3:B4");
		BoxRange boxOk2 = (BoxRange) Utils.parseRange("A3:B3");
		BoxRange boxOk3 = (BoxRange) Utils.parseRange("Sheet2.A5:Z4523");
		BoxRange boxOk4 = (BoxRange) Utils.parseRange("'" + TEST_FILE_DATA1 + "'#$'" + TEST_SHEET_DATA1_1 + "'.H8:H8");
		assertTrue(MX.subsumes(boxOk, context));
		assertTrue(MX.subsumes(boxOk2, context));
		assertTrue(MX.subsumes(boxOk3, context));
		assertTrue(MX.subsumes(boxOk4, context));
		
		BoxRange boxOther = (BoxRange) Utils.parseRange("A4:H9");
		BoxRange boxOther2 = (BoxRange) Utils.parseRange("A3:B5");
		BoxRange boxOther3 = (BoxRange) Utils.parseRange("A1:Z10");
		BoxRange boxOther4 = (BoxRange) Utils.parseRange("Sheet3.A4:B7");
		BoxRange boxOther5 = (BoxRange) Utils.parseRange("'" + TEST_FILE_DATA1 + "'#$'" + TEST_SHEET_DATA1_1 + "'.A4:Sheet2.H8");
		assertFalse(MX.subsumes(boxOther, context));
		assertFalse(MX.subsumes(boxOther2, context));
		assertFalse(MX.subsumes(boxOther3, context));
		assertFalse(MX.subsumes(boxOther4, context));
		assertFalse(MX.subsumes(boxOther5, context));
		
		// full sheet range
		FullSheetRange sheet = (FullSheetRange) Utils.parseRange("#2.*");
		FullSheetRange sheetOther = (FullSheetRange) Utils.parseRange("#0.*");
		assertTrue(MX.subsumes(sheet, context));
		assertFalse(MX.subsumes(sheetOther, context));
		
		// any range
		assertFalse(MX.subsumes(AnyRange.INSTANCE, context)); // never
		
		// multi range
		assertTrue(MX.subsumes(MX, context)); // itself

		MultiRange multi = new MultiRange();
		multi.addRange(cellOk);
		multi.addRange(cellOk2);
		multi.addRange(cellOk3);
		multi.addRange(boxOk);
		multi.addRange(boxOk2);
		multi.addRange(boxOk3);
		multi.addRange(boxOk4);
		multi.addRange(sheet);
		multi.addRange(NullRange.INSTANCE);
		assertTrue(MX.subsumes(multi, context));
		
		multi.addRange(boxOther);
		assertFalse(MX.subsumes(multi, context));
	}
	
	@Test
	public void testFileSheetChanging() throws XLWrapException {
		MultiRange copy1 = (MultiRange) MX.copy();
		assertEquals(MX.toString(), copy1.toString());
		
		// changing copy1
		copy1.changeFileName("foo.xls", AnyRange.INSTANCE, context);
		assertEquals("'foo.xls'#$A3:B4; " +
				"'foo.xls'#$H8; " +
				"'foo.xls'#$'Sheet1'.A6; " +
				"'foo.xls'#$'Sheet2'.*; ", copy1.toString());

		copy1 = (MultiRange) MX.copy();
		copy1.changeSheetName("Sheet2", AnyRange.INSTANCE, context);
		assertEquals("'Sheet2'.A3:'Sheet2'.B4; " +
					"'Sheet2'.H8; " +
					"'" + TEST_FILE_EMPTY + "'#$'Sheet2'.A6; " +
					"'Sheet2'.*; ", copy1.toString());

		copy1.changeSheetNumber(3, AnyRange.INSTANCE, context);
		assertEquals("#4.A3:#4.B4; " +
					"#4.H8; " +
					"'" + TEST_FILE_EMPTY + "'#$#4.A6; " +
					"#4.*; ", copy1.toString());
		
		MultiRange m = mr("Sheet2.A3:Sheet3.B5; B5");
		m.changeSheetName("Sheet4", AnyRange.INSTANCE, context);
		assertEquals("'Sheet2'.A3:'Sheet3'.B5; 'Sheet4'.B5; ", m.toString()); // do not change sheet of box ranges over multiple sheets
	}
	
	@Test
	public void testShifting() throws XLWrapException {
		// MX = A3:B4; H8; '" + TEST_FILE_EMPTY + "'#$Sheet1.A6; Sheet2.*
		
		MultiRange colShifted = (MultiRange) MX.copy();
		colShifted.shiftCols(26, MX, context);
		assertEquals("AA3:AB4; AH8; '" + TEST_FILE_EMPTY + "'#$'Sheet1'.AA6; 'Sheet2'.*; ", colShifted.toString());
		colShifted.shiftCols(5, NullRange.INSTANCE, context); // test outofscope shift, must remain the same
		assertEquals("AA3:AB4; AH8; '" + TEST_FILE_EMPTY + "'#$'Sheet1'.AA6; 'Sheet2'.*; ", colShifted.toString());

		// shift only parts back
		colShifted.shiftCols(-25, Utils.parseRange("AA3:AB4; '" + TEST_FILE_EMPTY + "'#$'Sheet1'.*"), context);
		assertEquals("B3:C4; AH8; '" + TEST_FILE_EMPTY + "'#$'Sheet1'.B6; 'Sheet2'.*; ", colShifted.toString());
		
		MultiRange rowShifted = (MultiRange) MX.copy();
		rowShifted.shiftRows(3, MX, context); // initially 3
		assertEquals("A6:B7; H11; '" + TEST_FILE_EMPTY + "'#$'Sheet1'.A9; 'Sheet2'.*; ", rowShifted.toString());
		rowShifted.shiftRows(5, NullRange.INSTANCE, context); // test outofscope shift, must remain the same
		assertEquals("A6:B7; H11; '" + TEST_FILE_EMPTY + "'#$'Sheet1'.A9; 'Sheet2'.*; ", rowShifted.toString());

		rowShifted.shiftRows(-2, Utils.parseRange("A1:B100; '" + TEST_FILE_EMPTY + "'#$'Sheet1'.*"), context);
		assertEquals("A4:B5; H11; '" + TEST_FILE_EMPTY + "'#$'Sheet1'.A7; 'Sheet2'.*; ", rowShifted.toString());
		
		MultiRange sheetShifted = (MultiRange) MX.copy();
		sheetShifted.shiftSheets(2, MX, context); // shift because MX is def on sheet #1 and sheetShifted is also #1.xx:#1.yy
		assertEquals("#3.A3:#3.B4; #3.H8; '" + TEST_FILE_EMPTY + "'#$#3.A6; #4.*; ", sheetShifted.toString());
		sheetShifted.shiftSheets(2, NullRange.INSTANCE, context); // test outofscope shift, because sheetShifted is already #3.xx:#3.yy
		assertEquals("#3.A3:#3.B4; #3.H8; '" + TEST_FILE_EMPTY + "'#$#3.A6; #4.*; ", sheetShifted.toString());
		
		sheetShifted.shiftSheets(-1, Utils.parseRange("#3.F1:K100; #4.*"), context);
		assertEquals("#3.A3:#3.B4; #2.H8; '" + TEST_FILE_EMPTY + "'#$#3.A6; #3.*; ", sheetShifted.toString());
	}
}

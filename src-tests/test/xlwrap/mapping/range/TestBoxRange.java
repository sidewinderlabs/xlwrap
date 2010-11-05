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
import at.jku.xlwrap.map.range.Range.CellIterator;
import at.jku.xlwrap.spreadsheet.XLWrapEOFException;

/**
 * @author dorgon
 *
 */
public class TestBoxRange extends XLWrapTestCase {
	private ExecutionContext context;
	private BoxRange A4H8;
	
	@Before
	public void setUp() throws XLWrapException {
		context = new ExecutionContext();
		context.setActiveTemplate(createPersonTemplate());
		A4H8 = (BoxRange) Utils.parseRange("A4:H8");
	}
	
	@Test
	public void testGetters() throws XLWrapException {
		assertEquals("A4:H8", A4H8.toString());
		assertEquals(0, A4H8.getColumn1());
		assertEquals(3, A4H8.getRow1());
		assertEquals(7, A4H8.getColumn2());
		assertEquals(7, A4H8.getRow2());
		assertEquals(null, A4H8.getFileName());
		assertEquals(null, A4H8.getSheet1());
		assertEquals(null, A4H8.getSheet2());
		assertEquals(null, A4H8.getSheetNumber1());
		assertEquals(null, A4H8.getSheetNumber2());
	}

	@Test
	public void testSetters() throws XLWrapException {
		A4H8.setFileName(TEST_FILE_EMPTY);
		assertEquals(TEST_FILE_EMPTY, A4H8.getFileName());
		
		A4H8.setSheetName("Sheet4");
		assertEquals("Sheet4", A4H8.getSheet1());
		assertEquals("Sheet4", A4H8.getSheet2());
		A4H8.setSheet2("Sheet5");
		assertEquals("'" + TEST_FILE_EMPTY + "'#$'Sheet4'.A4:'Sheet5'.H8", A4H8.toString());
		assertEquals("'" + TEST_FILE_EMPTY + "'#$#4.A4:#5.H8", A4H8.getAbsoluteRange(context).toString());
		
		A4H8.setSheetNumber(2);
		assertEquals(2, (int) A4H8.getSheetNumber1());
		assertEquals(2, (int) A4H8.getSheetNumber2());
		assertEquals(null, A4H8.getSheet1());
		assertEquals(null, A4H8.getSheet2());
		A4H8.setSheetNumber2(4);
		assertEquals("'" + TEST_FILE_EMPTY + "'#$#3.A4:#5.H8", A4H8.toString());
		assertEquals("'" + TEST_FILE_EMPTY + "'#$#3.A4:#5.H8", A4H8.getAbsoluteRange(context).toString());
	}
	
	@Test
	public void testAbsoluteRange() throws XLWrapException {
		BoxRange abs = (BoxRange) A4H8.getAbsoluteRange(context);
		assertEquals("'" + TEST_FILE_DATA1 + "'#$#1.A4:#1.H8", abs.toString());
		assertEquals(0, A4H8.getColumn1());
		assertEquals(3, A4H8.getRow1());
		assertEquals(7, A4H8.getColumn2());
		assertEquals(7, A4H8.getRow2());
		assertEquals(TEST_FILE_DATA1, abs.getFileName());
		assertEquals(TEST_SHEET_DATA1_1, abs.getSheet1());
		assertEquals(TEST_SHEET_DATA1_1, abs.getSheet2());
		assertEquals(0, (int) abs.getSheetNumber1());
		assertEquals(0, (int) abs.getSheetNumber2());

		// test absolute range with template where sheetNum is specified instead of name
		ExecutionContext cxtNum = new ExecutionContext();
		cxtNum.setActiveTemplate(createEmptyTemplate()); // zero-based
		BoxRange absNum = (BoxRange) A4H8.getAbsoluteRange(cxtNum);
		assertEquals("'" + TEST_FILE_EMPTY + "'#$#5.A4:#5.H8", absNum.toString());
		assertEquals("Sheet5", absNum.getSheet1());
		assertEquals("Sheet5", absNum.getSheet1());
		assertEquals(4, (int) absNum.getSheetNumber1());
		assertEquals(4, (int) absNum.getSheetNumber2());
	}
	
	@Test
	public void testCellIterator() throws XLWrapException, XLWrapEOFException {
		CellIterator it = ((BoxRange) Utils.parseRange("A4:H5")).getCellIterator(context);
		assertEquals("Tom", it.next().getText());
		assertEquals("Houston", it.next().getText());
		assertEquals("th@ex.com", it.next().getText());
		assertEquals("41", it.next().getText());
		assertEquals("1968", it.next().getText());
		assertEquals("false", it.next().getText());
		assertEquals("50230.23", it.next().getText());
		assertEquals("Tim Presley", it.next().getText());
		
		assertEquals("Tim", it.next().getText());
		assertEquals("Presley", it.next().getText());
		assertEquals("jp@ex.com", it.next().getText());
		assertEquals("66", it.next().getText());
		assertEquals("1943", it.next().getText());
		assertEquals("true", it.next().getText());
		assertEquals("69234.43", it.next().getText());
		assertEquals("Tim Presley", it.next().getText());
		
		assertFalse(it.hasNext());
	}
	
	@Test
	public void testCellIteratorMultiSheet() throws XLWrapException, XLWrapEOFException {
		CellIterator it = ((BoxRange) Utils.parseRange("Sheet2.A2:Sheet3.C3")).getCellIterator(context);
		// Sheet2
		assertEquals("Tom", it.next().getText());
		assertEquals("", it.next().getText());
		assertEquals("th@ex.com", it.next().getText());
		
		assertEquals("", it.next().getText());
		assertEquals("Presley", it.next().getText());
		assertEquals("jp@ex.com", it.next().getText());
		
		// Sheet3
		assertEquals("Betty", it.next().getText());
		assertEquals("B.", it.next().getText());
		assertEquals("bb@ex.com", it.next().getText());
		
		assertEquals("Maria", it.next().getText());
		assertEquals("", it.next().getText());
		assertEquals("mary@ex.com", it.next().getText());
		
		assertFalse(it.hasNext());
	}
	
	@Test
	public void testSubsumes() throws XLWrapException {
		// null range
		assertTrue(A4H8.subsumes(NullRange.INSTANCE, context)); // any box range subsumes NullRange
		
		// cell range
		CellRange cellOk = (CellRange) Utils.parseRange("A4");
		CellRange cellOk2 = (CellRange) Utils.parseRange("H8");
		CellRange cellOk3 = (CellRange) Utils.parseRange("'" + TEST_FILE_DATA1 + "'#$'" + TEST_SHEET_DATA1_1 + "'.H8");
		assertTrue(A4H8.subsumes(cellOk, context));
		assertTrue(A4H8.subsumes(cellOk2, context));
		assertTrue(A4H8.subsumes(cellOk3, context));
		
		CellRange cellOther = (CellRange) Utils.parseRange("A3");
		CellRange cellOther2 = (CellRange) Utils.parseRange("H9");
		CellRange cellOther3 = (CellRange) Utils.parseRange("Sheet2.A4");
		CellRange cellOther4 = (CellRange) Utils.parseRange("'" + TEST_FILE_EMPTY + "'#$'" + "Sheet2"+ "'.A4");
		assertFalse(A4H8.subsumes(cellOther, context));
		assertFalse(A4H8.subsumes(cellOther2, context));
		assertFalse(A4H8.subsumes(cellOther3, context));
		assertFalse(A4H8.subsumes(cellOther4, context));
		
		// other box range
		assertTrue(A4H8.subsumes(A4H8, context)); // itself
		BoxRange boxOk = (BoxRange) Utils.parseRange("A4:H7");
		BoxRange boxOk2 = (BoxRange) Utils.parseRange("B5:E6");
		BoxRange boxOk3 = (BoxRange) Utils.parseRange("H8:H8");
		BoxRange boxOk4 = (BoxRange) Utils.parseRange("'" + TEST_FILE_DATA1 + "'#$'" + TEST_SHEET_DATA1_1 + "'.B5:H8");
		assertTrue(A4H8.subsumes(boxOk, context));
		assertTrue(A4H8.subsumes(boxOk2, context));
		assertTrue(A4H8.subsumes(boxOk3, context));
		assertTrue(A4H8.subsumes(boxOk4, context));
		
		BoxRange boxOther = (BoxRange) Utils.parseRange("A4:H9");
		BoxRange boxOther2 = (BoxRange) Utils.parseRange("A3:B4");
		BoxRange boxOther3 = (BoxRange) Utils.parseRange("A1:Z10");
		BoxRange boxOther4 = (BoxRange) Utils.parseRange("Sheet2.A4:B7");
		BoxRange boxOther5 = (BoxRange) Utils.parseRange("'" + TEST_FILE_DATA1 + "'#$'" + TEST_SHEET_DATA1_1 + "'.A4:Sheet2.H8");
		assertFalse(A4H8.subsumes(boxOther, context));
		assertFalse(A4H8.subsumes(boxOther2, context));
		assertFalse(A4H8.subsumes(boxOther3, context));
		assertFalse(A4H8.subsumes(boxOther4, context));
		assertFalse(A4H8.subsumes(boxOther5, context));
		
		// full sheet range
		FullSheetRange sheet = (FullSheetRange) Utils.parseRange("#0.*");
		assertFalse(A4H8.subsumes(sheet, context)); // a box range can never subsume a full sheet range
		
		// any range
		assertFalse(A4H8.subsumes(AnyRange.INSTANCE, context)); // never
		
		// multi range
		MultiRange multi = new MultiRange();
		multi.addRange(A4H8);
		multi.addRange(cellOk);
		multi.addRange(cellOk2);
		multi.addRange(cellOk3);
		multi.addRange(boxOk);
		multi.addRange(boxOk2);
		multi.addRange(boxOk3);
		multi.addRange(boxOk4);
		multi.addRange(NullRange.INSTANCE);
		assertTrue(A4H8.subsumes(multi, context));
		
		multi.addRange(boxOther);
		assertFalse(A4H8.subsumes(multi, context));
	}
	
	@Test
	public void testFileSheetChanging() throws XLWrapException {
		BoxRange copy1 = (BoxRange) A4H8.copy();
		assertEquals(A4H8.toString(), copy1.toString());
		
		// changing copy1
		copy1.changeFileName(TEST_FILE_EMPTY, AnyRange.INSTANCE, context);
		assertEquals(TEST_FILE_EMPTY, copy1.getFileName());
		assertEquals(TEST_FILE_EMPTY, ((BoxRange) copy1.getAbsoluteRange(context)).getFileName());

		copy1.changeSheetName("Sheet2", AnyRange.INSTANCE, context);
		assertEquals("Sheet2", copy1.getSheet1());
		assertEquals("Sheet2", ((BoxRange) copy1.getAbsoluteRange(context)).getSheet1());

		copy1.changeSheetNumber(3, AnyRange.INSTANCE, context);
		assertEquals(3, (int) copy1.getSheetNumber1());
		assertEquals(3, (int) copy1.getSheetNumber2());
		assertEquals(null, copy1.getSheet1()); // now null
		assertEquals(null, copy1.getSheet2()); // now null
		assertEquals(3, (int) ((BoxRange) copy1.getAbsoluteRange(context)).getSheetNumber1());
		assertEquals(3, (int) ((BoxRange) copy1.getAbsoluteRange(context)).getSheetNumber2());
		assertEquals("Sheet4", ((BoxRange) copy1.getAbsoluteRange(context)).getSheet1());
		assertEquals("Sheet4", ((BoxRange) copy1.getAbsoluteRange(context)).getSheet2());

		// if box spans multiple sheets, do not change sheet (regardless of restrict range)!
		BoxRange multiSheet = (BoxRange) Utils.parseRange("Sheet2.A6:Sheet3.B7");
		multiSheet.changeSheetName("sheetX", AnyRange.INSTANCE, context);
		assertEquals("Sheet2", multiSheet.getSheet1());
		assertEquals("Sheet3", multiSheet.getSheet2());

		multiSheet.changeSheetNumber(4, AnyRange.INSTANCE, context);
		assertEquals(null, multiSheet.getSheetNumber1()); // still null
		assertEquals(null, multiSheet.getSheetNumber2());

		// change file of copy2 OUTSIDE of restrict range
		BoxRange copy2 = (BoxRange) A4H8.copy();

		copy2.changeFileName("foo.xls", NullRange.INSTANCE, context); // must have no effect
		assertEquals(null, copy2.getFileName());
		assertEquals(TEST_FILE_DATA1, ((BoxRange) copy2.getAbsoluteRange(context)).getFileName());

		copy2.changeSheetName("sheetX", NullRange.INSTANCE, context); // must have no effect
		assertEquals(null, copy2.getSheet1());
		assertEquals(null, copy2.getSheet2());
		assertEquals(TEST_SHEET_DATA1_1, ((BoxRange) copy2.getAbsoluteRange(context)).getSheet1());
		assertEquals(TEST_SHEET_DATA1_1, ((BoxRange) copy2.getAbsoluteRange(context)).getSheet2());
		
		copy2.changeSheetNumber(5, NullRange.INSTANCE, context); // must have no effect
		assertEquals(null, copy2.getSheetNumber1());
		assertEquals(null, copy2.getSheetNumber2());
		assertEquals(TEST_SHEET_DATA1_1, ((BoxRange) copy2.getAbsoluteRange(context)).getSheet1());
		assertEquals(TEST_SHEET_DATA1_1, ((BoxRange) copy2.getAbsoluteRange(context)).getSheet2());
	}
	
	@Test
	public void testShifting() throws XLWrapException {
		BoxRange colShifted = (BoxRange) A4H8.copy();
		colShifted.shiftCols(5, A4H8, context);
		assertEquals(5, colShifted.getColumn1());
		assertEquals(12, colShifted.getColumn2());
		colShifted.shiftCols(5, A4H8, context); // test outofscope shift, must remain the same
		assertEquals(5, colShifted.getColumn1());
		assertEquals(12, colShifted.getColumn2());
		
		BoxRange rowShifted = (BoxRange) A4H8.copy();
		rowShifted.shiftRows(3, A4H8, context); // initially 3
		assertEquals(6, rowShifted.getRow1());
		assertEquals(10, rowShifted.getRow2());
		rowShifted.shiftRows(5, A4H8, context); // test outofscope shift, must remain the same
		assertEquals(6, rowShifted.getRow1());
		assertEquals(10, rowShifted.getRow2());

		BoxRange sheetShifted = (BoxRange) A4H8.copy();
		sheetShifted.shiftSheets(2, A4H8, context); // shift because A4H8 is def on sheet #1 and sheetShifted is also #1.xx:#1.yy
		assertEquals(2, (int) sheetShifted.getSheetNumber1());
		assertEquals(2, (int) sheetShifted.getSheetNumber2());
		sheetShifted.shiftSheets(2, A4H8, context); // test outofscope shift, because sheetShifted is already #3.xx:#3.yy
		assertEquals(2, (int) sheetShifted.getSheetNumber1());
		assertEquals(2, (int) sheetShifted.getSheetNumber2());
	}
}

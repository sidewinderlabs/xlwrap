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
public class TestCellRange extends XLWrapTestCase {
	private CellRange A4;
	private ExecutionContext context;
	
	@Before
	public void setUp() throws XLWrapException {
		context = new ExecutionContext();
		context.setActiveTemplate(createPersonTemplate());
		A4 = (CellRange) Utils.parseRange("A4");
	}
	
	@Test
	public void testGetters() throws XLWrapException {
		assertEquals("A4", A4.toString());
		assertEquals(0, A4.getColumn());
		assertEquals(3, A4.getRow());
		assertEquals(null, A4.getFileName());
		assertEquals(null, A4.getSheetName());
		assertEquals(null, A4.getSheetNum());

	}

	@Test
	public void testSetters() throws XLWrapException {
		A4.setSheetName("Sheet3");
		assertEquals("Sheet3", A4.getSheetName());
		assertEquals(null, A4.getSheetNum());
		assertEquals("'Sheet3'.A4", A4.toString());
		assertEquals("'" + TEST_FILE_DATA1 + "'#$#3.A4", A4.getAbsoluteRange(context).toString());
		
		A4.setSheetNumber(4);
		assertEquals(4, (int) A4.getSheetNum());
		assertEquals(null, A4.getSheetName());
		assertEquals("#5.A4", A4.toString());
		assertEquals("'" + TEST_FILE_DATA1 + "'#$#5.A4", A4.getAbsoluteRange(context).toString());
	}
	
	@Test
	public void testAbsoluteRange() throws XLWrapException {
		CellRange abs = (CellRange) A4.getAbsoluteRange(context);
		assertEquals("'" + TEST_FILE_DATA1 + "'#$#1.A4", abs.toString());
		assertEquals(0, abs.getColumn());
		assertEquals(3, abs.getRow());
		assertEquals(TEST_FILE_DATA1, abs.getFileName());
		assertEquals(TEST_SHEET_DATA1_1, abs.getSheetName());
		assertEquals(0, (int) abs.getSheetNum());
		
		// test absolute range with template where sheetNum is specified instead of name
		ExecutionContext cxtNum = new ExecutionContext();
		cxtNum.setActiveTemplate(createEmptyTemplate()); // zero-based
		CellRange absNum = (CellRange) A4.getAbsoluteRange(cxtNum);
		assertEquals("'" + TEST_FILE_EMPTY + "'#$#5.A4", absNum.toString());
		assertEquals("Sheet5", absNum.getSheetName());
		assertEquals(4, (int) absNum.getSheetNum());
	}
	
	@Test
	public void testCellIterator() throws XLWrapException, XLWrapEOFException {
		CellIterator it = A4.getCellIterator(context);
		assertEquals("Tom", it.next().getText());
		assertFalse(it.hasNext());
	}

	@Test
	public void testSubsumes() throws XLWrapException {
		// null range
		assertTrue(A4.subsumes(NullRange.INSTANCE, context)); // any cell range subsumes NullRange
		
		// other cell range
		CellRange cellOther = (CellRange) Utils.parseRange("Z10");
		assertTrue(A4.subsumes(A4, context));
		assertFalse(A4.subsumes(cellOther, context));
		
		// box range
		BoxRange boxOk = (BoxRange) Utils.parseRange("A4:A4");
		assertTrue(A4.subsumes(boxOk, context));
		BoxRange boxOk2 = (BoxRange) Utils.parseRange("'" + TEST_FILE_DATA1 + "'#$'" + TEST_SHEET_DATA1_1 + "'.A4:A4");
		assertTrue(A4.subsumes(boxOk2, context));
		
		BoxRange boxOther = (BoxRange) Utils.parseRange("A4:B4");
		assertFalse(A4.subsumes(boxOther, context));
		BoxRange boxOther2 = (BoxRange) Utils.parseRange("Sheet3.A4:A4");
		assertFalse(A4.subsumes(boxOther2, context));
		BoxRange boxOther3 = (BoxRange) Utils.parseRange("'" + TEST_FILE_DATA1 + "'#$'" + TEST_SHEET_DATA1_1 + "'.A4:Sheet2.A4");
		assertFalse(A4.subsumes(boxOther3, context));
		
		// full sheet range
		FullSheetRange sheet = (FullSheetRange) Utils.parseRange("#0.*");
		assertFalse(A4.subsumes(sheet, context)); // a single cell can never subsume a full sheet range
		
		// any range
		assertFalse(A4.subsumes(AnyRange.INSTANCE, context)); // never
		
		// multi range
		MultiRange multi = new MultiRange();
		multi.addRange(A4);
		multi.addRange(boxOk);
		multi.addRange(boxOk2);
		multi.addRange(NullRange.INSTANCE);
		assertTrue(A4.subsumes(multi, context));
		
		multi.addRange(boxOther);
		assertFalse(A4.subsumes(multi, context));
	}

	@Test
	public void testFileSheetChanging() throws XLWrapException {
		CellRange copy1 = (CellRange) A4.copy();
		assertEquals(A4.toString(), copy1.toString());
		
		// changing copy1
		copy1.changeFileName(TEST_FILE_EMPTY, AnyRange.INSTANCE, context);
		assertEquals(TEST_FILE_EMPTY, copy1.getFileName());
		assertEquals(TEST_FILE_EMPTY, ((CellRange) copy1.getAbsoluteRange(context)).getFileName());

		copy1.changeSheetName("Sheet3", AnyRange.INSTANCE, context);
		assertEquals("Sheet3", copy1.getSheetName());
		assertEquals("Sheet3", ((CellRange) copy1.getAbsoluteRange(context)).getSheetName());
		assertEquals(2, (int) ((CellRange) copy1.getAbsoluteRange(context)).getSheetNum());

		copy1.changeSheetNumber(2, AnyRange.INSTANCE, context);
		assertEquals(2, (int) copy1.getSheetNum());
		assertEquals(null, copy1.getSheetName()); // now null
		assertEquals("Sheet3", ((CellRange) copy1.getAbsoluteRange(context)).getSheetName());
		
		// change file of copy2 OUTSIDE of restrict range
		CellRange copy2 = (CellRange) A4.copy();

		copy2.changeFileName(TEST_FILE_EMPTY, NullRange.INSTANCE, context); // must have no effect
		assertEquals(null, copy2.getFileName());
		assertEquals(TEST_FILE_DATA1, ((CellRange) copy2.getAbsoluteRange(context)).getFileName());

		copy2.changeSheetName("Sheet2", NullRange.INSTANCE, context); // must have no effect
		assertEquals(null, copy2.getSheetName());
		assertEquals(TEST_SHEET_DATA1_1, ((CellRange) copy2.getAbsoluteRange(context)).getSheetName());
		
		copy2.changeSheetNumber(5, NullRange.INSTANCE, context); // must have no effect
		assertEquals(null, copy2.getSheetNum());
		assertEquals(TEST_SHEET_DATA1_1, ((CellRange) copy2.getAbsoluteRange(context)).getSheetName());
	}
	
	@Test
	public void testShifting() throws XLWrapException {
		CellRange colShifted = (CellRange) A4.copy();
		colShifted.shiftCols(5, A4, context);
		assertEquals(5, colShifted.getColumn());
		colShifted.shiftCols(5, A4, context); // test outofscope shift, must remain the same
		assertEquals(5, colShifted.getColumn());
		
		CellRange rowShifted = (CellRange) A4.copy();
		rowShifted.shiftRows(5, A4, context); // initially 3
		assertEquals(8, rowShifted.getRow());
		rowShifted.shiftRows(5, A4, context); // test outofscope shift, must remain the same
		assertEquals(8, rowShifted.getRow());

		CellRange sheetShifted = (CellRange) A4.copy();
		sheetShifted.shiftSheets(4, A4, context); // initially 0
		assertEquals(4, (int) sheetShifted.getSheetNum());
		sheetShifted.shiftSheets(4, A4, context); // test outofscope shift, must remain the same
		assertEquals(4, (int) sheetShifted.getSheetNum());
	}
	
}

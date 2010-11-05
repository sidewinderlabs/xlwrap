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
public class TestFullSheetRange extends XLWrapTestCase {

	private ExecutionContext context;
	private FullSheetRange S3;
	
	@Before
	public void setUp() throws XLWrapException {
		context = new ExecutionContext();
		context.setActiveTemplate(createPersonTemplate());
		S3 = (FullSheetRange) Utils.parseRange("Sheet3.*");
	}
	
	@Test
	public void testGetters() throws XLWrapException {
		assertEquals("'Sheet3'.*", S3.toString());
		assertEquals(null, S3.getFileName());
		assertEquals("Sheet3", S3.getSheetName());
		assertEquals(null, S3.getSheetNumber());
	}

	@Test
	public void testSetters() throws XLWrapException {
		S3.setFileName(TEST_FILE_EMPTY);
		assertEquals(TEST_FILE_EMPTY, S3.getFileName());
		
		S3.setSheetName("Sheet4");
		assertEquals("Sheet4", S3.getSheetName());
		assertEquals("'" + TEST_FILE_EMPTY + "'#$'Sheet4'.*", S3.toString());
		assertEquals("'" + TEST_FILE_EMPTY + "'#$#4.*", S3.getAbsoluteRange(context).toString());
		
		S3.setSheetNumber(2);
		assertEquals(2, (int) S3.getSheetNumber());
		assertEquals(null, S3.getSheetName());
		assertEquals("'" + TEST_FILE_EMPTY + "'#$#3.*", S3.toString());
		assertEquals("'" + TEST_FILE_EMPTY + "'#$#3.*", S3.getAbsoluteRange(context).toString());
	}
	
	@Test
	public void testAbsoluteRange() throws XLWrapException {
		FullSheetRange abs = (FullSheetRange) S3.getAbsoluteRange(context);
		assertEquals("'" + TEST_FILE_DATA1 + "'#$#3.*", abs.toString());
		assertEquals(TEST_FILE_DATA1, abs.getFileName());
		assertEquals("Sheet3", abs.getSheetName());
		assertEquals(2, (int) abs.getSheetNumber());
		
		// test absolute range with template where sheetNum is specified instead of name
		ExecutionContext cxtNum = new ExecutionContext();
		cxtNum.setActiveTemplate(createEmptyTemplate()); // zero-based
		FullSheetRange absNum = (FullSheetRange) S3.getAbsoluteRange(cxtNum);
		assertEquals("'" + TEST_FILE_EMPTY + "'#$#3.*", absNum.toString());
		assertEquals("Sheet3", absNum.getSheetName());
		assertEquals(2, (int) absNum.getSheetNumber());
	}
	
	@Test
	public void testCellIterator() throws XLWrapException, XLWrapEOFException {
		S3.setSheetName("Sheet2"); // use this sheet for the CellIterator test
		CellIterator it = S3.getCellIterator(context);
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
		assertTrue(S3.subsumes(NullRange.INSTANCE, context)); // any box range subsumes NullRange
		
		// cell range
		CellRange cellOk = (CellRange) Utils.parseRange("Sheet3.A4");
		CellRange cellOk2 = (CellRange) Utils.parseRange("Sheet3.ZZ800");
		CellRange cellOk3 = (CellRange) Utils.parseRange("'" + TEST_FILE_DATA1 + "'#$'" + "Sheet3" + "'.H8");
		assertTrue(S3.subsumes(cellOk, context));
		assertTrue(S3.subsumes(cellOk2, context));
		assertTrue(S3.subsumes(cellOk3, context));
		
		CellRange cellOther = (CellRange) Utils.parseRange("Sheet2.A4");
		CellRange cellOther2 = (CellRange) Utils.parseRange("'" + TEST_FILE_EMPTY + "'#$'" + "Sheet2"+ "'.A4");
		assertFalse(S3.subsumes(cellOther, context));
		assertFalse(S3.subsumes(cellOther2, context));
		
		// box range
		BoxRange boxOk = (BoxRange) Utils.parseRange("Sheet3.A4:H7");
		BoxRange boxOk2 = (BoxRange) Utils.parseRange("Sheet3.B5:Sheet3.E6");
		BoxRange boxOk3 = (BoxRange) Utils.parseRange("Sheet3.H8:H8");
		BoxRange boxOk4 = (BoxRange) Utils.parseRange("'" + TEST_FILE_DATA1 + "'#$'Sheet3'.B5:Sheet3.H8");
		assertTrue(S3.subsumes(boxOk, context));
		assertTrue(S3.subsumes(boxOk2, context));
		assertTrue(S3.subsumes(boxOk3, context));
		assertTrue(S3.subsumes(boxOk4, context));
		
		BoxRange boxOther = (BoxRange) Utils.parseRange("SheetA4:H9");
		BoxRange boxOther2 = (BoxRange) Utils.parseRange("Sheet3.A3:Sheet4.B4");
		BoxRange boxOther3 = (BoxRange) Utils.parseRange("Sheet2.A1:Sheet4.Z10");
		BoxRange boxOther4 = (BoxRange) Utils.parseRange("Sheet2.A4:B7");
		BoxRange boxOther5 = (BoxRange) Utils.parseRange("'" + TEST_FILE_DATA1 + "'#$'" + TEST_SHEET_DATA1_1 + "'.A4:Sheet2.H8");
		assertFalse(S3.subsumes(boxOther, context));
		assertFalse(S3.subsumes(boxOther2, context));
		assertFalse(S3.subsumes(boxOther3, context));
		assertFalse(S3.subsumes(boxOther4, context));
		assertFalse(S3.subsumes(boxOther5, context));
		
		// full sheet range
		assertTrue(S3.subsumes(S3, context)); // itself

		FullSheetRange sheet = (FullSheetRange) Utils.parseRange("Sheet3.*");
		FullSheetRange sheet1 = (FullSheetRange) Utils.parseRange("#3.*");
		FullSheetRange sheet2 = (FullSheetRange) Utils.parseRange("'" + TEST_FILE_DATA1 + "'#$#3.*");
		assertTrue(S3.subsumes(sheet, context));
		assertTrue(S3.subsumes(sheet1, context));
		assertTrue(S3.subsumes(sheet2, context));

		FullSheetRange sheetOther = (FullSheetRange) Utils.parseRange("Sheet2.*");
		FullSheetRange sheetOther1 = (FullSheetRange) Utils.parseRange("#2.*");
		FullSheetRange sheetOther2 = (FullSheetRange) Utils.parseRange("'" + TEST_FILE_EMPTY + "'#$Sheet3.*");
		assertFalse(S3.subsumes(sheetOther, context));
		assertFalse(S3.subsumes(sheetOther1, context));
		assertFalse(S3.subsumes(sheetOther2, context));

		// any range
		assertFalse(S3.subsumes(AnyRange.INSTANCE, context)); // never
		
		// multi range
		MultiRange multi = new MultiRange();
		multi.addRange(S3);
		multi.addRange(cellOk);
		multi.addRange(cellOk2);
		multi.addRange(cellOk3);
		multi.addRange(boxOk);
		multi.addRange(boxOk2);
		multi.addRange(boxOk3);
		multi.addRange(boxOk4);
		multi.addRange(sheet);
		multi.addRange(sheet1);
		multi.addRange(sheet2);
		multi.addRange(NullRange.INSTANCE);
		assertTrue(S3.subsumes(multi, context));
		
		multi.addRange(boxOther);
		assertFalse(S3.subsumes(multi, context));
	}
	
	@Test
	public void testFileSheetChanging() throws XLWrapException {
		FullSheetRange copy1 = (FullSheetRange) S3.copy();
		assertEquals(S3.toString(), copy1.toString());
		
		// changing copy1
		copy1.changeFileName(TEST_FILE_EMPTY, AnyRange.INSTANCE, context);
		assertEquals(TEST_FILE_EMPTY, copy1.getFileName());
		assertEquals(TEST_FILE_EMPTY, ((FullSheetRange) copy1.getAbsoluteRange(context)).getFileName());

		copy1.changeSheetName("Sheet2", AnyRange.INSTANCE, context);
		assertEquals("Sheet2", copy1.getSheetName());
		assertEquals("Sheet2", ((FullSheetRange) copy1.getAbsoluteRange(context)).getSheetName());

		copy1.changeSheetNumber(1, AnyRange.INSTANCE, context);
		assertEquals(1, (int) copy1.getSheetNumber());
		assertEquals(null, copy1.getSheetName()); // now null
		assertEquals(1, (int) ((FullSheetRange) copy1.getAbsoluteRange(context)).getSheetNumber());
		assertEquals("Sheet1", ((FullSheetRange) copy1.getAbsoluteRange(context)).getSheetName());

		// change file of copy2 OUTSIDE of restrict range
		FullSheetRange copy2 = (FullSheetRange) S3.copy();

		copy2.changeFileName(TEST_FILE_EMPTY, NullRange.INSTANCE, context); // must have no effect
		assertEquals(null, copy2.getFileName());
		assertEquals(TEST_FILE_DATA1, ((FullSheetRange) copy2.getAbsoluteRange(context)).getFileName());

		copy2.changeSheetName("Sheet5", NullRange.INSTANCE, context); // must have no effect
		assertEquals("Sheet3", copy2.getSheetName());
		
		copy2.changeSheetNumber(5, NullRange.INSTANCE, context); // must have no effect
		assertEquals(null, copy2.getSheetNumber());
		assertEquals(2, (int) ((FullSheetRange) copy2.getAbsoluteRange(context)).getSheetNumber());
	}
	
	@Test
	public void testShifting() throws XLWrapException {
		FullSheetRange sheetShifted = (FullSheetRange) S3.copy();
		sheetShifted.shiftSheets(2, S3, context);
		assertEquals(4, (int) sheetShifted.getSheetNumber()); // 3+2 zero-based = 4
		sheetShifted.shiftSheets(2, S3, context); // test outofscope shift, because sheetShifted is already #5
		assertEquals(4, (int) sheetShifted.getSheetNumber());
	}
	

}

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
package test.xlwrap.spreadsheet;


import static org.junit.Assert.assertEquals;

import org.junit.Test;

import test.xlwrap.XLWrapTestCase;
import at.jku.xlwrap.common.XLWrapException;
import at.jku.xlwrap.spreadsheet.Sheet;
import at.jku.xlwrap.spreadsheet.Workbook;
import at.jku.xlwrap.spreadsheet.WorkbookFactory;
import at.jku.xlwrap.spreadsheet.XLWrapEOFException;
import at.jku.xlwrap.spreadsheet.csv.CSVParseConfig;

/**
 * @author dorgon
 *
 */
public class TestPoiFormulas {


    private void testFormulas(Workbook wb) throws XLWrapException, XLWrapEOFException {
        Sheet sh = wb.getSheet(0);
        assertEquals("text get","This is text", sh.getCell(0, 0).getText());
        assertEquals("int as text get","123", sh.getCell(0, 1).getText());
        assertEquals("int get",123, sh.getCell(0, 1).getInteger());
        assertEquals("float as text","123.123", sh.getCell(0, 2).getText());
        assertEquals("float get", 123.123, sh.getCell(0, 2).getFloat(),0.00001);
        assertEquals("long as text","1234567890000000", sh.getCell(0, 3).getText());
        long p1 =  1234567890;
        long p2 = 1000000;
        long theLong = p1 * p2;
        assertEquals("long get",theLong, sh.getCell(0, 3).getLong());
        assertEquals("formula text get","This is text more", sh.getCell(2, 0).getText());
        assertEquals("formula int as text get","135", sh.getCell(2, 1).getText());
        assertEquals("formula int get",135, sh.getCell(2, 1).getInteger());
        assertEquals("formula float as text","135.123", sh.getCell(2, 2).getText());
        assertEquals("formula float get", 135.123, sh.getCell(2, 2).getFloat(),0.00001);
        assertEquals("formula long as text","1234567890000012", sh.getCell(2, 3).getText());
        p1 =  12;
        theLong = theLong + p1;
        assertEquals("formula long get",theLong, sh.getCell(2, 3).getLong());
    }

    @Test
    public void testPoiFormulas() throws XLWrapException, XLWrapEOFException {
        Workbook wb = WorkbookFactory.getWorkbook("testing/formulas.xlsx");
        testFormulas(wb);
        wb = WorkbookFactory.getWorkbook("testing/formulas.xls");
        testFormulas(wb);
    }
        //	@Test
//	public void testBigFileLoading() throws XLWrapException {
//		XLWrapMapping map = MappingParser.parse("mappings/geonames.trig");
//		XLWrapMaterializer mat = new XLWrapMaterializer();
//		Model m = mat.generateModel(map);
//		m.write(System.out);
//		
//	}

}

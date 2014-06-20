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
public class TestPoiWorkbookLoading {

	static {
		XLWrapTestCase.TEST_FILE_DATA1 = "testing/test-data1.xlsx";
	}
	
	@Test
	public void testPoiLoading() throws XLWrapException, XLWrapEOFException {
		Workbook wb = WorkbookFactory.getWorkbook(XLWrapTestCase.TEST_FILE_DATA1_POI);
		Sheet sh = wb.getSheet(0);
		assertEquals("Person", sh.getCell(0, 0).getText());
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

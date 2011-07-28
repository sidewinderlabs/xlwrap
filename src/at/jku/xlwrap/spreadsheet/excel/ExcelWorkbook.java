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
package at.jku.xlwrap.spreadsheet.excel;

import at.jku.xlwrap.common.XLWrapException;
import java.io.IOException;
import java.io.InputStream;

import jxl.WorkbookSettings;
import jxl.read.biff.BiffException;

import at.jku.xlwrap.spreadsheet.Sheet;
import at.jku.xlwrap.spreadsheet.Workbook;

/**
 * @author dorgon
 *
 */
public class ExcelWorkbook implements Workbook {
	private final jxl.Workbook wb;
	private final String file;
	
	/**
	 * @throws IOException 
	 * @throws BiffException 
	 * 
	 */
	public ExcelWorkbook(InputStream is, String file) throws BiffException, IOException {
		WorkbookSettings settings = new WorkbookSettings();
		settings.setEncoding("iso-8859-1"); // TODO encoding cfg support - use hard-coded latin1 by now...
		wb = jxl.Workbook.getWorkbook(is, settings);
		this.file = file;
	}
	
	@Override
	public boolean supportsMultipleSheets() {
		return true;
	}
	
	@Override
	public Sheet getSheet(int sheetNum) throws XLWrapException {
            return new ExcelSheet(wb.getSheet(sheetNum), file);
	}

	@Override
	public Sheet getSheet(String sheetName) throws XLWrapException {
            try{
		return new ExcelSheet(wb.getSheet(sheetName), file);
            } catch (Exception e){
                throw new  XLWrapException ("Unable to get sheet " + sheetName + " form file " + file, e);
            }
	}

	@Override
	public String[] getSheetNames() {
		return wb.getSheetNames();
	}

	@Override
	public String getWorkbookInfo() {
		return file;
	}
	
	@Override
	public void close() {
		wb.close();
	}
}

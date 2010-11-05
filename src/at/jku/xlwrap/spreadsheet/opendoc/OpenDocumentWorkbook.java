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
package at.jku.xlwrap.spreadsheet.opendoc;

import java.io.File;
import java.io.IOException;

import org.jopendocument.dom.spreadsheet.SpreadSheet;

import at.jku.xlwrap.spreadsheet.Sheet;
import at.jku.xlwrap.spreadsheet.Workbook;

/**
 * @author dorgon
 *
 */
public class OpenDocumentWorkbook implements Workbook {
	private final SpreadSheet spread;
	private final String fileName;
	
	/**
	 * constructor
	 * @throws IOException 
	 */
	public OpenDocumentWorkbook(File file, String fileName) throws IOException {
		this.spread = SpreadSheet.createFromFile(file);
		this.fileName = fileName;
	}
	
	@Override
	public boolean supportsMultipleSheets() {
		return true;
	}
	
	@Override
	public Sheet getSheet(int sheetNum) {
		return new OpenDocumentSheet(spread.getSheet(sheetNum), fileName);
	}

	@Override
	public Sheet getSheet(String sheetName) {
		return new OpenDocumentSheet(spread.getSheet(sheetName), fileName);
	}

	@Override
	public String[] getSheetNames() {
		return spread.getSheetNames();
	}

	@Override
	public String getWorkbookInfo() {
		return fileName;
	}

	@Override
	public void close() {}
}

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

import at.jku.xlwrap.spreadsheet.Cell;
import at.jku.xlwrap.spreadsheet.Sheet;
import at.jku.xlwrap.spreadsheet.XLWrapEOFException;

/**
 * @author dorgon
 *
 */
public class ExcelSheet implements Sheet {
	private final jxl.Sheet sheet;
	private final String file;
	
	/**
	 * @param sheet
	 */
	public ExcelSheet(jxl.Sheet sheet, String file) {
		this.sheet = sheet;
		this.file = file;
	}

	@Override
	public Cell getCell(int column, int row) throws XLWrapEOFException {
		try {
			jxl.Cell cell = sheet.getCell(column, row);
			return new ExcelCell(cell, file, sheet.getName());
		} catch (ArrayIndexOutOfBoundsException e) {
			throw new XLWrapEOFException();
		}
	}

	@Override
	public int getColumns() {
		return sheet.getColumns();
	}

	@Override
	public String getName() {
		return sheet.getName();
	}

	@Override
	public int getRows() {
		return sheet.getRows();
	}

	@Override
	public String getSheetInfo() {
		return file + ", sheet '" + sheet.getName() + "'";
	}
}

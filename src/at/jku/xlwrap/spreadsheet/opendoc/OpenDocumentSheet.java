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

import org.jopendocument.dom.spreadsheet.MutableCell;
import org.jopendocument.dom.spreadsheet.SpreadSheet;

import at.jku.xlwrap.spreadsheet.Cell;
import at.jku.xlwrap.spreadsheet.Sheet;
import at.jku.xlwrap.spreadsheet.XLWrapEOFException;

/**
 * @author dorgon
 *
 */
public class OpenDocumentSheet implements Sheet {
	private final org.jopendocument.dom.spreadsheet.Sheet sheet;
	private final String file;
	
	/**
	 * @param sheet
	 * @param fileName
	 */
	public OpenDocumentSheet(org.jopendocument.dom.spreadsheet.Sheet sheet, String fileName) {
		this.sheet = sheet;
		this.file = fileName;
	}

	@Override
	public Cell getCell(int column, int row) throws XLWrapEOFException {
		try {
			MutableCell<SpreadSheet> cell = sheet.getCellAt(column, row);
			return new OpenDocumentCell(cell, column, row, file, sheet.getName());
		} catch (NullPointerException e) {
			throw new XLWrapEOFException();
		}
	}

	@Override
	public int getColumns() {
		return sheet.getColumnCount();
	}

	@Override
	public String getName() {
		return sheet.getName();
	}

	@Override
	public int getRows() {
		return sheet.getRowCount();
	}

	@Override
	public String getSheetInfo() {
		return file + ", sheet '" + sheet.getName() + "'";
	}

}

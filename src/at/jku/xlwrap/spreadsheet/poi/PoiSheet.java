/**
 * Copyright 2009 Andreas Langegger, andreas@langegger.at, Austria
 * Copyright 2011 Christian Brenninkmeijer, brenninc@cs.man.ac.uk, Manchester England
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
package at.jku.xlwrap.spreadsheet.poi;

import at.jku.xlwrap.common.XLWrapException;
import at.jku.xlwrap.spreadsheet.Cell;
import at.jku.xlwrap.spreadsheet.Sheet;
import at.jku.xlwrap.spreadsheet.XLWrapEOFException;
import org.apache.poi.ss.usermodel.Row;

/**
 * @author Christian based on code by dorgon
 *
 */
public class PoiSheet implements Sheet {
    private final org.apache.poi.ss.usermodel.Sheet sheet;
    private final String file;
	
    /**
     * @param sheet
     */
    public PoiSheet(org.apache.poi.ss.usermodel.Sheet sheet, String file) throws XLWrapException {
        if (sheet == null){
            throw new XLWrapException("Unable to create a PoiSheet around a null sheet");
        }
        this.sheet = sheet;
        this.file = file;
    }

    @Override
    public Cell getCell(int column, int row) throws XLWrapException {
        org.apache.poi.ss.usermodel.Row wholeRow = sheet.getRow(row);
        if (wholeRow == null){
            //TODO is this really the best way?
            return new NullCell();
        }
        org.apache.poi.ss.usermodel.Cell cell = wholeRow.getCell(column);
        if (cell == null){
            return new NullCell();
        }
        return new PoiCell(cell, file, sheet.getSheetName());
    }

    @Override
    public int getColumns() {
        int maxColumn = 0;
        for (Row row : sheet) {
           if (row.getLastCellNum() >  maxColumn){
               maxColumn = row.getLastCellNum();
           }
        }
        return maxColumn;
    }

    @Override
    public String getName() {
        return sheet.getSheetName();
    }

    @Override
    public int getRows() {
        //Last row is zero based so add 1
        return sheet.getLastRowNum() + 1;
    }

    @Override
    public String getSheetInfo() {
        return file + ", sheet '" + sheet.getSheetName() + "'";
    }
}

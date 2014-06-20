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
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;

import java.io.IOException;
import java.io.InputStream;

import at.jku.xlwrap.spreadsheet.Sheet;
import at.jku.xlwrap.spreadsheet.Workbook;

/**
 * @author Christian based on code by dorgon
 *
 */
public class PoiWorkbook implements Workbook {
    private final org.apache.poi.ss.usermodel.Workbook wb;
    private final String file;
	
    /**
     * @throws IOException
     * @throws BiffException
     *
     */
    public PoiWorkbook(InputStream is, String file) throws IOException, XLWrapException {
        org.apache.poi.ss.usermodel.WorkbookFactory workbookFactory = new org.apache.poi.ss.usermodel.WorkbookFactory();
        try {
            wb = org.apache.poi.ss.usermodel.WorkbookFactory.create(is);
        } catch (InvalidFormatException ex) {
            throw new XLWrapException("Unable to convert " + file, ex);
        }
	this.file = file;
    }
	
    @Override
    public boolean supportsMultipleSheets() {
        return true;
    }
	
    @Override
    public Sheet getSheet(int sheetNum) throws XLWrapException {
        org.apache.poi.ss.usermodel.Sheet inner = wb.getSheetAt(sheetNum);
        if (inner == null){
            throw new XLWrapException("Workbook does not conatin a sheet "+ sheetNum);
        }
        return new PoiSheet(inner, file);
    }

    @Override
    public Sheet getSheet(String sheetName) throws XLWrapException {
        org.apache.poi.ss.usermodel.Sheet inner = wb.getSheet(sheetName);
        if (inner == null){
            throw new XLWrapException("Workbook does not conatin a sheet "+ sheetName);
        }
        return new PoiSheet(inner, file);
    }

    @Override
    public String[] getSheetNames() {
        int number = wb.getNumberOfSheets();
        String[] sheets = new String[number];
        for (int i = 0; i< number; i++){
            sheets[i] = wb.getSheetName(i);
        }
        return sheets;
    }

    @Override
    public String getWorkbookInfo() {
        return file;
    }
	
    @Override
    public void close() {
        //no nothing as there is on close method.
    }
}

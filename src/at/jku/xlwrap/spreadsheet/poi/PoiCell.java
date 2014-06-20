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

import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import at.jku.xlwrap.common.Constants;
import at.jku.xlwrap.common.Utils;
import at.jku.xlwrap.common.XLWrapException;
import at.jku.xlwrap.spreadsheet.Cell;
import at.jku.xlwrap.spreadsheet.FormatAnnotation;
import at.jku.xlwrap.spreadsheet.TypeAnnotation;
import org.apache.poi.ss.usermodel.CellStyle;

/**
 * @author Christian based on code by dorgon
 *
 */
public class PoiCell implements Cell {
    private static final Logger log = LoggerFactory.getLogger(PoiCell.class);
	
    private final org.apache.poi.ss.usermodel.Cell cell;
    private final String file;
    private final String sheet;
	
    /**
     * constructor
     */
    public PoiCell(org.apache.poi.ss.usermodel.Cell cell, String file, String sheet) throws XLWrapException {
        if (cell == null){
            throw new XLWrapException("Unable to create a PoiCell around a null cell");
        }
        this.cell = cell;
        this.file = file;
        this.sheet = sheet;
    }
	
    @Override
    public boolean getBoolean() throws XLWrapException {
        return cell.getBooleanCellValue();
    }

    @Override
    public Date getDate() throws XLWrapException {
        return cell.getDateCellValue();
    }

    public String getDateFormat() throws XLWrapException {
        CellStyle cellStyle = cell.getCellStyle();
        return cellStyle.getDataFormatString();
    }

    @Override
    public double getDouble() throws XLWrapException {
        return cell.getNumericCellValue();
    }

    @Override
    public float getFloat() throws XLWrapException {
        return (float)cell.getNumericCellValue();
    }

    @Override
    public int getInteger() throws XLWrapException {
        return (int)cell.getNumericCellValue();
    }

    @Override
    public long getLong() throws XLWrapException {
        return (long)cell.getNumericCellValue();
    }

    @Override
    public String getText() throws XLWrapException {
        TypeAnnotation type = this.getType();
        switch (type){
            case TEXT: 
                return cell.getStringCellValue();
            case NULL:
                return "";
            case NUMBER:
                double doubleValue = cell.getNumericCellValue();
                //ystem.out.println(doubleValue);
                long longValue = Math.round(doubleValue);
                if (longValue == doubleValue){
                    return longValue + "";
                } else {
                    return doubleValue + "";
                }
            case BOOLEAN:
                boolean booleanValue = cell.getBooleanCellValue();
                return booleanValue + "";
            case DATE:
                Date dateValue = cell.getDateCellValue();
                return dateValue.toString();
            default:
                throw new XLWrapException ("Unexpected Type");
        }
    }
	
    @Override
    public double getNumber() throws XLWrapException {
        return cell.getNumericCellValue();
    }

    private TypeAnnotation convertType(int type) throws XLWrapException {
        if (type == cell.CELL_TYPE_NUMERIC) {
            return TypeAnnotation.NUMBER;
        } else if (type == cell.CELL_TYPE_STRING) {
            if (Constants.EMPTY_STRING_AS_NULL && cell.getStringCellValue().isEmpty()){
                return TypeAnnotation.NULL;
            } else {
                return TypeAnnotation.TEXT;
            }
        } else if (type == cell.CELL_TYPE_FORMULA) {
            int formulaType = cell.getCachedFormulaResultType();
            if (formulaType == cell.CELL_TYPE_FORMULA){
                throw new XLWrapException("Unexpected result of CELL_TYPE_FORMULA from getCachedFormulaResultType()");
            }
            return convertType(formulaType);
        } else if (type == cell.CELL_TYPE_BLANK) {
            return TypeAnnotation.NULL;
        } else if (type == cell.CELL_TYPE_BOOLEAN) {
            return TypeAnnotation.BOOLEAN;
        } else if (type == cell.CELL_TYPE_ERROR) {
            log.warn("Error in cell " + getCellInfo() + ".");
            return TypeAnnotation.NULL;
        } else {
            throw new XLWrapException("Unknown cell type: " + getCellInfo());
        }
    }

    @Override
    public TypeAnnotation getType() throws XLWrapException {
        int type = cell.getCellType();
        return convertType(type);
    }

    //@Override
    //June 2011 To the best of Christian_B's knoweldge this method is never actually called.
    public FormatAnnotation getFormat() {
        return new PoiFormat(cell.getCellType());
    }
	
    @Override
    public String getFormula() {
        return cell.getCellFormula();
    }
	
    @Override
    public String getCellInfo() {
        return file + ", sheet '" + sheet + "', " + Utils.indexToAlpha(cell.getColumnIndex()) + (cell.getRowIndex()+1);
    }

}

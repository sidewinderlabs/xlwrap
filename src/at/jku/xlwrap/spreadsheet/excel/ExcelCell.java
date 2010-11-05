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

import java.util.Date;

import jxl.BooleanCell;
import jxl.CellType;
import jxl.DateCell;
import jxl.FormulaCell;
import jxl.NumberCell;
import jxl.biff.formula.FormulaException;
import jxl.format.CellFormat;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import at.jku.xlwrap.common.Constants;
import at.jku.xlwrap.common.Utils;
import at.jku.xlwrap.common.XLWrapException;
import at.jku.xlwrap.spreadsheet.Cell;
import at.jku.xlwrap.spreadsheet.FormatAnnotation;
import at.jku.xlwrap.spreadsheet.TypeAnnotation;

/**
 * @author dorgon
 *
 */
public class ExcelCell implements Cell {
	private static final Logger log = LoggerFactory.getLogger(ExcelCell.class);
	
	private final jxl.Cell cell;
	private final String file;
	private final String sheet;
	
	/**
	 * constructor
	 */
	public ExcelCell(jxl.Cell cell, String file, String sheet) {
		this.cell = cell;
		this.file = file;
		this.sheet = sheet;
	}
	
	@Override
	public boolean getBoolean() throws XLWrapException {
		return ((BooleanCell) cell).getValue();
	}

	@Override
	public Date getDate() throws XLWrapException {
		return ((DateCell) cell).getDate();
	}

	@Override
	public double getDouble() throws XLWrapException {
		return ((NumberCell) cell).getValue();
	}

	@Override
	public float getFloat() throws XLWrapException {
		return (float) ((NumberCell) cell).getValue();
	}

	@Override
	public int getInteger() throws XLWrapException {
		return (int) ((NumberCell) cell).getValue();
	}

	@Override
	public long getLong() throws XLWrapException {
		return (long) ((NumberCell) cell).getValue();
	}

	@Override
	public String getText() throws XLWrapException {
		return cell.getContents();
	}
	
	@Override
	public double getNumber() throws XLWrapException {
		return ((NumberCell) cell).getValue();
	}

	@Override
	public TypeAnnotation getType() throws XLWrapException {
		CellType t = cell.getType();
		
		if (t == CellType.BOOLEAN || t == CellType.BOOLEAN_FORMULA)
			return TypeAnnotation.BOOLEAN;
		else if (t == CellType.NUMBER || t == CellType.NUMBER_FORMULA)
			return TypeAnnotation.NUMBER;
		else if (t == CellType.DATE || t == CellType.DATE_FORMULA)
			return TypeAnnotation.DATE;
		else if (t == CellType.EMPTY || t == CellType.EMPTY)
			return TypeAnnotation.NULL;
		else if (t == CellType.LABEL || t == CellType.STRING_FORMULA) {
			if (Constants.EMPTY_STRING_AS_NULL && cell.getContents().length() == 0)
				return TypeAnnotation.NULL;
			else
				return TypeAnnotation.TEXT;
		} else if (t == CellType.ERROR || t == CellType.FORMULA_ERROR) {
			log.warn("Error in cell " + getCellInfo() + ".");
			return TypeAnnotation.NULL;
		} else
			throw new XLWrapException("Unknown cell type: " + getCellInfo());
	}

	@Override
	public FormatAnnotation getFormat() {
		CellFormat cf = cell.getCellFormat();
		if (cf != null)
			return new ExcelCellFormat(cf.getFormat());
		else
			return null;
	}
	
	@Override
	public String getFormula() throws FormulaException {
		return ((FormulaCell) cell).getFormula();
	}
	
	@Override
	public String getCellInfo() {
		return file + ", sheet '" + sheet + "', " + Utils.indexToAlpha(cell.getColumn()) + (cell.getRow()+1);
	}
}

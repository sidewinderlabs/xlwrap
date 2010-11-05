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

import java.util.Date;

import jxl.biff.formula.FormulaException;

import org.jopendocument.dom.spreadsheet.MutableCell;
import org.jopendocument.dom.spreadsheet.SpreadSheet;

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
public class OpenDocumentCell implements Cell {
	private final org.jopendocument.dom.spreadsheet.Cell<SpreadSheet> cell;
	private final String file;
	private final String sheet;
	private final int col;
	private final int row;
	
	private Object val;
	
	/**
	 * @param cellAt
	 * @param file
	 * @param sheetNum
	 */
	public OpenDocumentCell(MutableCell<SpreadSheet> cell, int col, int row, String file, String sheet) {
		this.cell = cell;
		this.col = col;
		this.row = row;
		this.file = file;
		this.sheet = sheet;
	}

	@Override
	public boolean getBoolean() throws XLWrapException {
		return (Boolean) cell.getValue();
	}

	@Override
	public Date getDate() throws XLWrapException {
		return (Date) cell.getValue();
	}

	@Override
	public double getDouble() throws XLWrapException {
		return (Double) cell.getValue();
	}

	@Override
	public float getFloat() throws XLWrapException {
		return (Float) cell.getValue();
	}

	@Override
	public FormatAnnotation getFormat() {
		return new OpenDocumentFormat(cell.getStyle());
	}

	@Override
	public String getFormula() throws FormulaException {
		return null; // TODO: not supported by jOpenDocument
	}

	@Override
	public int getInteger() throws XLWrapException {
		return (Integer) cell.getValue();
	}

	@Override
	public long getLong() throws XLWrapException {
		return (Long) cell.getValue();
	}

	@Override
	public double getNumber() throws XLWrapException {
		obtainValue();
		if (val instanceof Long)
			return ((Long) val).doubleValue();
		else if (val instanceof Integer)
			return ((Integer) val).doubleValue();
		else if (val instanceof Short)
			return ((Short) val).doubleValue();
		else if (val instanceof Byte)
			return ((Byte) val).doubleValue();
		
		else if (val instanceof Double)
			return (Double) val;
		else if (val instanceof Float)
			return new Double("" + (Float) val);
		else
			throw new RuntimeException("Cannot cast " + val + " to double.");
	}

	@Override
	public String getText() throws XLWrapException {
		obtainValue();
		if (val instanceof String)
			return (String) val;
		else
			return null;
	}
	
	@Override
	public TypeAnnotation getType() throws XLWrapException {
		obtainValue();
		if (val == null)
			return TypeAnnotation.NULL;
		else if (val instanceof String) {
			if (Constants.EMPTY_STRING_AS_NULL && ((String) val).length() == 0)
				return TypeAnnotation.NULL;
			else
				return TypeAnnotation.TEXT;
		} else if (val instanceof Float || val instanceof Double ||
				val instanceof Integer || val instanceof Long || val instanceof Short || val instanceof Byte)
			return TypeAnnotation.NUMBER;
		else if (val instanceof Date)
			return TypeAnnotation.DATE;
		else if (val instanceof Boolean)
			return TypeAnnotation.BOOLEAN;
		else
			return TypeAnnotation.NULL;
	}

	private void obtainValue() {
		if (val == null) {
			val = cell.getValue();
			if (val == null || (val instanceof String && ((String) val).length() == 0)) { // maybe content is XML?
				val = cell.getElement().getValue();
			}
		}
	}

	@Override
	public String getCellInfo() {
		return file + ", sheet '" + sheet + "', " + Utils.indexToAlpha(col) + (row+1);
	}
}


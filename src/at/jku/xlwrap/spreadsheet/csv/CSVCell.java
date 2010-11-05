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
package at.jku.xlwrap.spreadsheet.csv;

import java.text.ParseException;
import java.util.Date;

import jxl.biff.formula.FormulaException;
import at.jku.xlwrap.common.Constants;
import at.jku.xlwrap.common.Utils;
import at.jku.xlwrap.common.XLWrapException;
import at.jku.xlwrap.map.expr.val.E_Date;
import at.jku.xlwrap.spreadsheet.Cell;
import at.jku.xlwrap.spreadsheet.FormatAnnotation;
import at.jku.xlwrap.spreadsheet.TypeAnnotation;

/**
 * @author dorgon
 *
 */
public class CSVCell implements Cell {
//	private static final Logger log = LoggerFactory.getLogger(CSVCell.class);
	
	private final String file;
	private final int col;
	private final int row;
	private final String lexValue;
	
	/**
	 * @param string
	 */
	public CSVCell(String file, int col, int row, String string) {
		this.file = file;
		this.col = col;
		this.row = row;
		this.lexValue = string;
	}

	@Override
	public boolean getBoolean() throws XLWrapException {
		String norm = lexValue.trim().toLowerCase();
		if (norm.equals("true") || norm.equals("1") || norm.equals("yes") || norm.equals("on"))
			return true;
		else if (norm.equals("false") || norm.equals("0") || norm.equals("no") || norm.equals("off"))
			return false;
		else {
			throw new XLWrapException("Failed to parse boolean from CSV file " + getCellInfo() + ": " + lexValue + ".");
		}
	}

	@Override
	public Date getDate() throws XLWrapException {
		try {
			return E_Date.parse(lexValue);
		} catch (ParseException e) {
			throw new XLWrapException("Failed to parse date from CSV file " + getCellInfo() + ": " + lexValue + ".");
		}
	}

	@Override
	public double getDouble() throws XLWrapException {
		try {
			return Double.parseDouble(lexValue);
		} catch (Exception e) {
			throw new XLWrapException("Failed to parse double value from CSV file " + getCellInfo() + ": " + lexValue + ".", e);
		}
	}

	@Override
	public float getFloat() throws XLWrapException {
		try {
			return Float.parseFloat(lexValue);
		} catch (Exception e) {
			throw new XLWrapException("Failed to parse float value from CSV file " + getCellInfo() + ": " + lexValue + ".", e);
		}
	}

	@Override
	public int getInteger() throws XLWrapException {
		try {
			return Integer.parseInt(lexValue);
		} catch (Exception e) {
			throw new XLWrapException("Failed to parse integer value from CSV file " + getCellInfo() + ": " + lexValue + ".", e);
		}
	}

	@Override
	public long getLong() throws XLWrapException {
		try {
			return Long.parseLong(lexValue);
		} catch (Exception e) {
			throw new XLWrapException("Failed to parse long from CSV file " + getCellInfo() + ": " + lexValue + ".", e);
		}
	}

	@Override
	public double getNumber() throws XLWrapException {
		// try long first, if string contains a comma, it will fail
		try {
			Long l = Long.parseLong(lexValue);
			return l;
		} catch (Exception ignore) {	}

		// try double now, provide detailed exception e if fails
		try {
			Double d = Double.parseDouble(lexValue);
			return d;
		} catch (Exception e) {
			throw new XLWrapException("Failed to parse value as number from CSV file " + getClass() + ": " + lexValue + ".", e);
		}
	}

	@Override
	public String getText() throws XLWrapException {
		if (Constants.EMPTY_STRING_AS_NULL && lexValue.length() == 0)
			return null;
		else
			return lexValue;
	}

	@Override
	public FormatAnnotation getFormat() {
		return null;
	}

	@Override
	public String getFormula() throws FormulaException {
		return null;
	}
	
	@Override
	public TypeAnnotation getType() throws XLWrapException {
		// handle all source values as text
		if (lexValue == null || Constants.EMPTY_STRING_AS_NULL && lexValue.length() == 0)
			return TypeAnnotation.NULL;
		else
			return TypeAnnotation.TEXT;
	}

	@Override
	public String getCellInfo() {
		return file + ", " + Utils.indexToAlpha(col) + (row+1) + " (" + lexValue + ")";
	}
	
	/* (non-Javadoc)
		 * @see java.lang.Object#toString()
		 */
		@Override
		public String toString() {
			return getCellInfo();
		}
}

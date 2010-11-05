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
package at.jku.xlwrap.map.range;

import java.util.Iterator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import at.jku.xlwrap.common.Utils;
import at.jku.xlwrap.common.XLWrapException;
import at.jku.xlwrap.exec.ExecutionContext;
import at.jku.xlwrap.map.MapTemplate;
import at.jku.xlwrap.spreadsheet.Cell;
import at.jku.xlwrap.spreadsheet.Sheet;
import at.jku.xlwrap.spreadsheet.XLWrapEOFException;

/**
 * @author dorgon
 *
 * single cell range
 * col, row must be set
 * optional fileName and (sheetName or sheetNum)
 */
public class CellRange extends Range {
	private static final Logger log = LoggerFactory.getLogger(CellRange.class);
	
	protected String fileName;
	protected String sheetName;
	protected Integer sheetNum;
	protected int col, row;
	
	public CellRange(String fileName, String sheetName, int col, int row) {
		this.fileName = fileName;
		this.sheetName = sheetName;
		this.sheetNum = null;
		this.col = col;
		this.row = row;
	}
	
	public CellRange(String fileName, Integer sheetNum, int col, int row) {
		this.fileName = fileName;
		this.sheetNum = sheetNum;
		this.sheetName = null;
		this.col = col;
		this.row = row;
	}
	
	public CellRange(int col, int row) {
		this.col = col;
		this.row = row;
		this.fileName = null;
		this.sheetName = null;
		this.sheetNum = null;
	}

	@Override
	public Range getAbsoluteRange(ExecutionContext context) throws XLWrapException {
		MapTemplate tmpl = context.getActiveTemplate();
		CellRange r = new CellRange(col, row);
		if (fileName != null)
			r.fileName = fileName;
		else
			r.fileName = tmpl.getFileName(); // get base filename from template

		if (sheetNum != null || sheetName != null) {
			if (sheetNum != null) { // if both were accidentally set, sheetNum has priority and we explicitly determine the name by context 
				r.sheetNum = sheetNum;
				r.sheetName = context.getSheet(r.fileName, sheetNum).getName();
			} else {
				r.sheetName = sheetName;
				r.sheetNum = context.getSheetNumber(r.fileName, sheetName); // determine sheet number by context
			}
		} else { // get from template
			r.sheetNum = tmpl.getSheetNum();
			r.sheetName = tmpl.getSheetName();
		}

		return r;
	}

// getters and setters
	
	public int getColumn() {
		return col;
	}
	
	public int getRow() {
		return row;
	}
	
	public String getFileName() {
		return fileName;
	}
	
	public String getSheetName() {
		return sheetName;
	}
	
	/**
	 * @return zero-based sheet number
	 */
	public Integer getSheetNum() {
		return sheetNum;
	}

	public void setSheetName(String sheetName) {
		this.sheetName = sheetName;
		this.sheetNum = null;
	}
	
	public void setSheetNumber(int n) {
		this.sheetNum = n;
		this.sheetName = null;
	}

// transformation helpers
	
	@Override
	public Range shiftCols(int n, Range restrict, ExecutionContext context) throws IndexOutOfBoundsException, XLWrapException {
		if (restrict.subsumes(this, context)) {
			if (this.col + n < 0)
				throw new IndexOutOfBoundsException("Attempt to shift column below zero.");
			if (this.col + n > Integer.MAX_VALUE)
				throw new IndexOutOfBoundsException("Attempt to shift column above Integer.MAX_VALUE(" + Integer.MAX_VALUE + ").");
			this.col += n;
		}
		return this; 
	}

	@Override
	public Range shiftRows(int n, Range restrict, ExecutionContext context) throws IndexOutOfBoundsException, XLWrapException {
		if (restrict.subsumes(this, context)) {
			if (this.row + n < 0)
				throw new IndexOutOfBoundsException("Attempt to shift row below zero.");
			if (this.row + n > Integer.MAX_VALUE)
				throw new IndexOutOfBoundsException("Attempt to shift row above Integer.MAX_VALUE(" + Integer.MAX_VALUE + ").");
			this.row += n;
		}		
		return this; 
	}
	
	@Override
	public Range shiftSheets(int n, Range restrict, ExecutionContext context) throws XLWrapException {
		if (restrict.subsumes(this, context)) {
			if (sheetNum != null)
				sheetNum += n; // local sheet number exists, shift this
			else {
				CellRange abs = (CellRange) getAbsoluteRange(context);
				sheetNum = abs.sheetNum + n; // absolute range has always sheet number set
			}
		}
		return this; 
	}
	
	@Override
	public Range changeFileName(String fileName, Range restrict, ExecutionContext context) throws XLWrapException {
		if (restrict.subsumes(this, context))
			this.fileName = fileName;
		return this; 
	}
	
	@Override
	public Range changeSheetName(String sheetName, Range restrict, ExecutionContext context) throws XLWrapException {
		if (restrict.subsumes(this, context))
			setSheetName(sheetName);
		return this; 
	}
	
	@Override
	public Range changeSheetNumber(int n, Range restrict, ExecutionContext context) throws XLWrapException {
		if (restrict.subsumes(this, context))
			setSheetNumber(n);
		return this;
	}
	
	@Override
	public Range copy() {
		if (sheetNum != null) // if both accidentally set, sheetNum has priority
			return new CellRange(fileName, sheetNum, col, row);
		else
			return new CellRange(fileName, sheetName, col, row);
	}

	@Override
	public boolean subsumes(Range other, ExecutionContext context) throws XLWrapException {
		if (other == NullRange.INSTANCE)
			return true; // any ranges subsumes NullRange
		
		else if (other instanceof CellRange) {
			CellRange absThis = (CellRange) getAbsoluteRange(context);
			CellRange absOther = (CellRange) other.getAbsoluteRange(context);
			
			return absThis.fileName.equals(absOther.fileName) &&						// same file
			  absThis.sheetNum.equals(absOther.sheetNum) && // same sheet
			  absThis.col == absOther.col && absThis.row == absOther.row;				// same cell
		
		} else if (other instanceof BoxRange) {
			CellRange absThis = (CellRange) getAbsoluteRange(context);
			BoxRange box = (BoxRange) other.getAbsoluteRange(context);
			
			return absThis.fileName.equals(box.fileName) &&																		// same file
				absThis.sheetNum != null && absThis.sheetNum.equals(box.sheetNum1) && absThis.sheetNum.equals(box.sheetNum2) &&	// same sheet
				absThis.col == box.col1 && absThis.col == box.col2 &&	// box is equal single cell, e.g. A2:A2
				absThis.row == box.row1 && absThis.row == box.row2;
			
		} else if (other instanceof FullSheetRange) {
			return false; // single cell cannot subsume a full sheet

		} else if (other instanceof MultiRange) {
			Iterator<Range> it = ((MultiRange) other).getRangeIterator();	// must subsume all sub ranges
			while (it.hasNext())
				if (!subsumes(it.next(), context))
					return false;
			return true;

		} else
			return false;
	}
	
	@Override
	public CellIterator getCellIterator(ExecutionContext context) throws XLWrapException {
		return new CellIterator(getAbsoluteRange(context), context) {
			private CellRange range;
			
			@Override
			public void init(Range range) throws XLWrapException {
				this.range = (CellRange) range;
			}
			
			@Override
			public boolean hasNext() {
				return range != null;
			}
			
			@Override
			public Cell next() throws XLWrapException, XLWrapEOFException {
				if (range != null) {
					Cell c = context.getCell(range);
					range = null;
					return c;
				} else
					return null;
			}			
		};
	}
	
	@Override
	public boolean withinSheetBounds(ExecutionContext context) {
		try {
			CellRange abs = (CellRange) getAbsoluteRange(context);
			
			Sheet sheet = context.getSheet(abs.getFileName(), abs.getSheetNum());
			if (abs.getColumn() >= sheet.getColumns() ||
				abs.getRow() >= sheet.getRows())
				return false;

			return true;
		} catch (XLWrapException e) {
			log.debug("Range out of bounds: " + this);
			return false;
		}
	}
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder(100);
		if (fileName != null)
			sb.append("'").append(fileName).append("'#$");
		
		if (sheetNum != null)
			sb.append("#").append(sheetNum+1).append(".");
		else if (sheetName != null)
			sb.append("'").append(sheetName).append("'.");
		
		sb.append(Utils.indexToAlpha(col)).append(row+1);
		return sb.toString();
	}

}

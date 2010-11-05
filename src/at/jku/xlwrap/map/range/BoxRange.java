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
 * a 3D box range can span multiple cols, rows, and sheets in a single workbook
 */
public class BoxRange extends Range {
	private static final Logger log = LoggerFactory.getLogger(BoxRange.class);
	
	protected String fileName = null;
	
	// sheets can be set either by name or number, absolute range contains sheet number
	protected String sheet1 = null;
	protected String sheet2 = null;
	protected Integer sheetNum1 = null;
	protected Integer sheetNum2 = null;
	
	protected int col1, col2;
	protected int row1, row2;
	
	public BoxRange(String fileName, String sheet1, int col1, int row1, String sheet2, int col2, int row2) {
		this.fileName = fileName;
		this.sheet1 = sheet1;
		this.col1 = col1;
		this.row1 = row1;
		this.sheet2 = sheet2;
		this.col2 = col2;
		this.row2 = row2;
	}
	
	/**
	 * 
	 * @param fileName
	 * @param sheetNum1, zero-based
	 * @param col1
	 * @param row1
	 * @param sheetNum2, zero-based
	 * @param col2
	 * @param row2
	 */
	public BoxRange(String fileName, Integer sheetNum1, int col1, int row1, Integer sheetNum2, int col2, int row2) {
		this.fileName = fileName;
		this.sheetNum1 = sheetNum1;
		this.col1 = col1;
		this.row1 = row1;
		this.sheetNum2 = sheetNum2;
		this.col2 = col2;
		this.row2 = row2;
	}
	
	public BoxRange(int col1, int row1, int col2, int row2) {
		this.col1 = col1;
		this.row1 = row1;
		this.col2 = col2;
		this.row2 = row2;
	}
	
// getters and setters
	
	public int getColumn1() {
		return col1;
	}
	
	public int getColumn2() {
		return col2;
	}
	
	public int getRow1() {
		return row1;
	}
	
	public int getRow2() {
		return row2;
	}

	public String getSheet1() {
		return sheet1;
	}
	
	public String getSheet2() {
		return sheet2;
	}
	
	/**
	 * @return zero-based sheet number 1
	 */
	public Integer getSheetNumber1() {
		return sheetNum1;
	}
	
	/**
	 * @return zero-based sheet number 2
	 */
	public Integer getSheetNumber2() {
		return sheetNum2;
	}
	
	public String getFileName() {
		return fileName;
	}
	
	public void setFileName(String fileName) {
		this.fileName = fileName;
	}
	
	public void setSheetName(String sheetName) {
		this.sheetNum1 = null;
		this.sheet1 = sheetName;
		this.sheetNum2 = null;
		this.sheet2 = sheetName;
	}
	
	public void setSheet1(String s1) {
		this.sheetNum1 = null;
		this.sheet1 = s1;
	}

	public void setSheet2(String s2) {
		this.sheetNum2 = null;
		this.sheet2 = s2;
	}

	public void setSheetNumber(int n) {
		this.sheet1 = null;
		this.sheetNum1 = n;
		this.sheet2 = null;
		this.sheetNum2 = n;
	}
	
	public void setSheetNumber1(int n) {
		this.sheet1 = null;
		this.sheetNum1 = n;
	}
	
	public void setSheetNumber2(int n) {
		this.sheet2 = null;
		this.sheetNum2 = n;
	}
	
// transformation helpers

	@Override
	public Range shiftCols(int n, Range restrict, ExecutionContext context) throws IndexOutOfBoundsException, XLWrapException {
		if (restrict.subsumes(this, context)) {
			if (this.col1 + n < 0)
				throw new IndexOutOfBoundsException("Attempt to shift col1 below zero.");
			if (this.col2 + n > Integer.MAX_VALUE)
				throw new IndexOutOfBoundsException("Attempt to shift col2 above Integer.MAX_VALUE(" + Integer.MAX_VALUE + ").");
			this.col1 += n;
			this.col2 += n;
		}
		return this;
	}
	
	@Override
	public Range shiftRows(int n, Range restrict, ExecutionContext context) throws IndexOutOfBoundsException, XLWrapException {
		if (restrict.subsumes(this, context)) {
			if (this.row1 + n < 0)
				throw new IndexOutOfBoundsException("Attempt to shift row1 below zero.");
			if (this.row2 + n > Integer.MAX_VALUE)
				throw new IndexOutOfBoundsException("Attempt to shift row2 above Integer.MAX_VALUE(" + Integer.MAX_VALUE + ").");
			this.row1 += n;
			this.row2 += n;
		}		
		return this;
	}
	
	@Override
	public Range shiftSheets(int n, Range restrict, ExecutionContext context) throws XLWrapException {
		if (restrict.subsumes(this, context)) {
			if (sheetNum1 != null && sheetNum2 != null) {
				sheetNum1 += n; // local sheet numbers exist, shift them
				sheetNum2 += n;
			} else {
				BoxRange abs = (BoxRange) getAbsoluteRange(context);
				sheetNum1 = abs.sheetNum1 + n; // absolute range has always sheet number set
				sheetNum2 = abs.sheetNum2 + n;
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
		if (restrict.subsumes(this, context)) {
			BoxRange abs = (BoxRange) getAbsoluteRange(context);
			if (abs.sheetNum1.equals(abs.sheetNum2)) // spans across same sheet
				setSheetName(sheetName);
		}
		return this;
	}
	
	@Override
	public Range changeSheetNumber(int n, Range restrict, ExecutionContext context) throws XLWrapException {
		if (restrict.subsumes(this, context)) {
			BoxRange abs = (BoxRange) getAbsoluteRange(context);
			if (abs.sheetNum1.equals(abs.sheetNum2)) // spans across same sheet
				setSheetNumber(n);
		}
		return this;
	}
	
// misc functions
	
	@Override
	public CellIterator getCellIterator(ExecutionContext context) throws XLWrapException {
		return new CellIterator(getAbsoluteRange(context), context) {
			private BoxRange range;
			
			private int sheetPointer;
			private int colPointer;
			private int rowPointer;
			
			@Override
			public void init(Range r) throws XLWrapException {
				this.range = (BoxRange) r;
				
				colPointer = range.col1;
				rowPointer = range.row1;
				sheetPointer = range.sheetNum1;
			}
			
			@Override
			public boolean hasNext() {
				return colPointer <= range.col2 && rowPointer <= range.row2
						&& sheetPointer <= range.sheetNum2;
			}
			
			@Override
			public Cell next() throws XLWrapException, XLWrapEOFException {
				Cell cell = context.getCell(range.fileName, sheetPointer, colPointer, rowPointer);
				
				if (colPointer < range.col2)
					colPointer++;
				else {
					if (rowPointer < range.row2) {
						rowPointer++;
						colPointer = range.col1;
					} else {
						sheetPointer++;
						rowPointer = range.row1;
						colPointer = range.col1;
					}
				}	
				return cell;
			}
		};
	}

	@Override
	public Range getAbsoluteRange(ExecutionContext context) throws XLWrapException {
		MapTemplate tmpl = context.getActiveTemplate();
		
		BoxRange r = new BoxRange(col1, row1, col2, row2);
		if (fileName != null)
			r.fileName = fileName;
		else
			r.fileName = tmpl.getFileName();
		
		if (sheetNum1 != null || sheet1!= null) {
			if (sheetNum1 != null) { // if both were accidentally set, sheetNum has priority and we explicitly determine the name by context 
				r.sheetNum1 = sheetNum1;
				r.sheet1 = context.getSheet(r.fileName, sheetNum1).getName();
			} else {
				r.sheet1 = sheet1;
				r.sheetNum1 = context.getSheetNumber(r.fileName, sheet1); // determine sheet number by context
			}
		} else { // get from template
			r.sheetNum1 = tmpl.getSheetNum();
			r.sheet1 = tmpl.getSheetName();
		}

		if (sheetNum2 != null || sheet2!= null) {
			if (sheetNum2 != null) { // if both were accidentally set, sheetNum has priority and we explicitly determine the name by context 
				r.sheetNum2 = sheetNum2;
				r.sheet2 = context.getSheet(r.fileName, sheetNum2).getName();
			} else {
				r.sheet2 = sheet1;
				r.sheetNum2 = context.getSheetNumber(r.fileName, sheet2); // determine sheet number by context
			}
		} else { // get from template
			r.sheetNum2 = tmpl.getSheetNum();
			r.sheet2 = tmpl.getSheetName();
		}
			
		return r;
	}

	@Override
	public boolean subsumes(Range other, ExecutionContext context) throws XLWrapException {
		if (other == NullRange.INSTANCE)
			return true; // any ranges subsumes NullRange
		
		else if (other instanceof CellRange) {
			BoxRange absThis = (BoxRange) getAbsoluteRange(context);
			CellRange absOther = (CellRange) other.getAbsoluteRange(context);
			
			return absThis.fileName.equals(absOther.fileName) &&	// same file
				absThis.sheetNum1 <= absOther.sheetNum && absThis.sheetNum2 >= absOther.sheetNum && // subsumes sheet
				absThis.col1 <= absOther.col && absThis.col2 >= absOther.col && // column range subsumes cell
				absThis.row1 <= absOther.row && absThis.row2 >= absOther.row; // row range subsumes cell
		
		} else if (other instanceof BoxRange) {
			BoxRange absThis = (BoxRange) getAbsoluteRange(context);
			BoxRange absOther = (BoxRange) other.getAbsoluteRange(context);
			
			return absThis.fileName.equals(absOther.fileName) &&	// same file
				absThis.sheetNum1 <= absOther.sheetNum1 && absThis.sheetNum2 >= absOther.sheetNum2 && // subsumes sheet
				absThis.col1 <= absOther.col1 && absThis.col2 >= absOther.col2 && // column range subsumes cell
				absThis.row1 <= absOther.row1 && absThis.row2 >= absOther.row2; // row range subsumes cell

		} else if (other instanceof FullSheetRange) {
			return false; // always false since we don't capture the box limits
			
		} else if (other instanceof MultiRange) {
			Iterator<Range> it = ((MultiRange) other).getRangeIterator();	// must subsume all sub ranges
			while (it.hasNext()) {
				if (!subsumes(it.next(), context))
					return false;
			}
			return true;
			
		} else
			return false;
	}
	
	@Override
	public boolean withinSheetBounds(ExecutionContext context) {
		try {
			BoxRange abs = (BoxRange) getAbsoluteRange(context);
			
			// iterate sheets: column/row box must always be within sheet bounds
			for (int i = abs.getSheetNumber1(); i <= abs.getSheetNumber2(); i++) {
				Sheet sheet = context.getSheet(abs.getFileName(), i);
				if (abs.getColumn2() >= sheet.getColumns()-1 || // we can safely assume that col2 >= col1 and row2 >= row2, if col1/row1 would fail, col2/row2 also does
					abs.getRow2() >= sheet.getRows()-1)
					return false;
			}
			
			return true;
		} catch (XLWrapException e) {
			log.debug("Range out of bounds: " + this);
			return false;
		}
	}
	
	@Override
	public Range copy() {
		BoxRange r = new BoxRange(fileName, sheet1, col1, row1, sheet2, col2, row2);
		r.sheetNum1 = sheetNum1;
		r.sheetNum2 = sheetNum2;
		return r;
	}
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder(100);
		if (fileName != null)
			sb.append("'").append(fileName).append("'#$");
		
		if (sheetNum1 != null)
			sb.append("#").append(sheetNum1+1).append(".");
		else if (sheet1 != null)
			sb.append("'").append(sheet1).append("'.");
		
		sb.append(Utils.indexToAlpha(col1)).append(row1+1).append(":");
		
		if (sheetNum2 != null)
			sb.append("#").append(sheetNum2+1).append(".");
		else if (sheet2 != null)
			sb.append("'").append(sheet2).append("'.");
		
		sb.append(Utils.indexToAlpha(col2)).append(row2+1);
		
		return sb.toString();
	}

}

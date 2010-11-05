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

import at.jku.xlwrap.common.XLWrapException;
import at.jku.xlwrap.exec.ExecutionContext;
import at.jku.xlwrap.map.MapTemplate;
import at.jku.xlwrap.spreadsheet.Cell;
import at.jku.xlwrap.spreadsheet.Sheet;
import at.jku.xlwrap.spreadsheet.XLWrapEOFException;

/**
 * @author dorgon
 *
 */
public class FullSheetRange extends Range {
//	private static final Logger log = LoggerFactory.getLogger(FullSheetRange.class);
	
	protected String fileName = null;
	protected String sheetName = null;
	protected Integer sheetNum = null;
	
	public FullSheetRange(String fileName, String sheetName) {
		this.fileName = fileName;
		this.sheetName = sheetName;
	}
	
	public FullSheetRange(String fileName, Integer sheetNum) {
		this.fileName = fileName;
		this.sheetNum = sheetNum;
	}
	
	
	/**
	 * default constructor
	 */
	public FullSheetRange() {
	}

// getters and setters
	
	public String getFileName() {
		return fileName;
	}
	
	public Integer getSheetNumber() {
		return sheetNum;
	}
	
	public String getSheetName() {
		return sheetName;
	}
	
	public void setFileName(String fileName) {
		this.fileName = fileName;
	}
	
	public void setSheetName(String sheetName) {
		this.sheetNum = null;
		this.sheetName = sheetName;
	}
	
	public void setSheetNumber(Integer sheetNum) {
		this.sheetName = null;
		this.sheetNum = sheetNum;
	}

// transformation helpers
	
	@Override
	public Range shiftCols(int n, Range restrict, ExecutionContext context) throws IndexOutOfBoundsException, XLWrapException {
		return this;
	}
	
	@Override
	public Range shiftRows(int n, Range restrict, ExecutionContext context) throws IndexOutOfBoundsException, XLWrapException {
		return this;
	}
	
	@Override
	public Range shiftSheets(int n, Range restrict, ExecutionContext context) throws XLWrapException {
		if (restrict.subsumes(this, context)) {
			if (sheetNum != null)
				sheetNum += n; // local sheet numbers exist, shift them
			else
				sheetNum = ((FullSheetRange) getAbsoluteRange(context)).sheetNum + n;
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

// misc functions
	
	@Override
	public CellIterator getCellIterator(ExecutionContext context) throws XLWrapException {
		return new CellIterator(getAbsoluteRange(context), context) {
			private FullSheetRange range;
			
			private int colPointer;
			private int rowPointer;
			private int colMax;
			private int rowMax;

			@Override
			public void init(Range r) throws XLWrapException {
				this.range = (FullSheetRange) r;
				
				colPointer = 0;
				rowPointer = 0;
				
				Sheet s = context.getSheet(range.fileName, range.sheetNum);
				colMax = s.getColumns() - 1;
				rowMax = s.getRows() - 1;
			}
		
			@Override
			public boolean hasNext() {
				return colPointer <= colMax && rowPointer <= rowMax;
			}
			
			@Override
			public Cell next() throws XLWrapException, XLWrapEOFException {
				Cell cell = context.getCell(range.fileName, range.sheetNum, colPointer, rowPointer);
				if (colPointer < colMax)
					colPointer++;
				else {
					colPointer = 0;
					rowPointer++;
				}
				return cell;
			}
		};
	}
	
	@Override
	public Range getAbsoluteRange(ExecutionContext context) throws XLWrapException {
		MapTemplate tmpl = context.getActiveTemplate();
		
		FullSheetRange r = new FullSheetRange();
		if (fileName != null)
			r.fileName = fileName;
		else
			r.fileName = tmpl.getFileName();
		
		if (sheetNum != null || sheetName != null) {
			if (sheetNum != null) { // if both were accidentally set, sheetNum has priority and we explicitly determine the name by context 
				r.sheetNum = sheetNum;
				r.sheetName = context.getSheet(r.fileName, sheetName).getName();
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

	@Override
	public Range copy() {
		if (sheetNum != null)
			return new FullSheetRange(fileName, sheetNum);
		else
			return new FullSheetRange(fileName, sheetName);
	}
	
	@Override
	public boolean subsumes(Range other, ExecutionContext context) throws XLWrapException {
		if (other == NullRange.INSTANCE)
			return true; // any ranges subsumes NullRange
		
		else if (other instanceof CellRange) {
			FullSheetRange absThis = (FullSheetRange) getAbsoluteRange(context);
			CellRange absOther = (CellRange) other.getAbsoluteRange(context);
			return absThis.fileName.equals(absOther.fileName) &&		// same file
					absThis.sheetNum.equals(absOther.sheetNum);  		// if same sheet
		
		} else if (other instanceof BoxRange) {
			FullSheetRange absThis = (FullSheetRange) getAbsoluteRange(context);
			BoxRange absOther = (BoxRange) other.getAbsoluteRange(context);
			return absThis.fileName.equals(absOther.fileName) &&										// same file
					absOther.sheetNum1 >= absThis.sheetNum && absOther.sheetNum2 <= absThis.sheetNum;	// subsumes sheet range of box
					
		} else if (other instanceof FullSheetRange) {
			FullSheetRange absThis = (FullSheetRange) getAbsoluteRange(context);
			FullSheetRange absOther = (FullSheetRange) other.getAbsoluteRange(context);
			return absThis.fileName.equals(absOther.fileName) &&		// same file
			absThis.sheetNum == absOther.sheetNum;				// same sheet
			
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
		return true;
	}
	
	@Override
	public boolean equals(Object obj) {
		FullSheetRange other = (FullSheetRange) obj;
		return sheetName != null && other.sheetName != null &&	sheetName.equals(other.sheetName) || // if same sheet (name or num)
				sheetNum != null && other.sheetNum != null && sheetNum.equals(other.sheetNum);
	}
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		if (fileName != null)
			sb.append("'").append(fileName).append("'#$");
		
		if (sheetNum != null)
			sb.append("#").append(sheetNum+1).append(".");
		else if (sheetName != null)
			sb.append("'").append(sheetName).append("'.");
		
		sb.append("*");
		return sb.toString();
	}

}

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

import at.jku.xlwrap.common.Copy;
import at.jku.xlwrap.common.XLWrapException;
import at.jku.xlwrap.exec.ExecutionContext;
import at.jku.xlwrap.spreadsheet.Cell;
import at.jku.xlwrap.spreadsheet.XLWrapEOFException;


/**
 * @author dorgon
 *
 * common super class for ranges
 */
public abstract class Range implements Copy<Range>{
	
// functions
	
	/**
	 * returns the absolute range including fileName, sheetName and sheetNum, if sheetNum was not set it is determined based on context
	 * thus, the absolute range contains both, sheetNum and sheetName
	 * 
	 * @param context
	 * @return
	 * @throws XLWrapException
	 */
	public abstract Range getAbsoluteRange(ExecutionContext context) throws XLWrapException;

	/**
	 * @param context
	 * @return an iterator over all the cells in this range
	 * @throws XLWrapException 
	 */
	public abstract CellIterator getCellIterator(ExecutionContext context) throws XLWrapException;
	
// transformation helpers
	
	/**
	 * @param n rows to shift
	 * @param restrict only shift sub-ranges within this range
	 * @param context
	 * @return resulting Range
	 * @throws XLWrapException 
	 */
	public abstract Range shiftRows(int n, Range restrict, ExecutionContext context) throws IndexOutOfBoundsException, XLWrapException;
	
	/**
	 * @param n columns to shift
	 * @param restrict only shift sub-ranges within this range
	 * @param context
	 * @return resulting Range
	 * @throws XLWrapException 
	 */
	public abstract Range shiftCols(int n, Range restrict, ExecutionContext context) throws IndexOutOfBoundsException, XLWrapException;

	/**
	 * @param n sheets to shift
	 * @param restrict only shift sub-ranges within this range
	 * @param context
	 * @return resulting Range
	 * @throws XLWrapException
	 */
	public abstract Range shiftSheets(int n, Range restrict, ExecutionContext context) throws XLWrapException;

	/**
	 * @param fileName the absolute file name
	 * @param restrict restrict changes to this range
	 * @param context
	 * @return resulting Range
	 * @throws XLWrapException 
	 */
	public abstract Range changeFileName(String fileName, Range restrict, ExecutionContext context) throws XLWrapException;
	
	/**
	 * @param sheetName the absolute sheet name 
	 * @param restrict restrict changes to this range
	 * @param context
	 * @return resulting Range
	 * @throws XLWrapException 
	 */
	public abstract Range changeSheetName(String sheetName, Range restrict, ExecutionContext context) throws XLWrapException;

	/**
	 * @param sheetNumber the sheet number, zero-based
	 * @param restrict restrict changes to this range
	 * @param context
	 * @return resulting Range
	 * @throws XLWrapException 
	 */
	public abstract Range changeSheetNumber(int n, Range restrict, ExecutionContext context) throws XLWrapException;


// misc methods

	/**
	 * @param other
	 * @param context
	 * @return true if this subsumes other, false otherwise
	 * @throws XLWrapException 
	 */
	public abstract boolean subsumes(Range other, ExecutionContext context) throws XLWrapException;

	/**
	 * check if range is in range of sheet bounds
	 * @return
	 */
	public abstract boolean withinSheetBounds(ExecutionContext context);

	/**
	 * @see java.lang.Object#toString()
	 * @return the Excel string representation of the range 
	 */
	public abstract String toString();
	
	@Override
	public int hashCode() {
		return toString().hashCode();
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		return this.hashCode() == obj.hashCode();
	}
	
	/**
	 * iterator over ranges
	 * @author dorgon
	 */
	public abstract class CellIterator {
		protected final ExecutionContext context;
		
		/**
		 * constructor
		 * @param range must be absolute range!
		 * @param context
		 * @throws XLWrapException 
		 */
		public CellIterator(Range range, ExecutionContext context) throws XLWrapException {
			this.context = context;
			init(range); // initialization call-back
		}

		/** 
		 * override to add init code
		 * 
		 * @param the range specified in the constructor 
		 * @throws XLWrapException */
		public void init(Range range) throws XLWrapException {}
		
		public abstract boolean hasNext() throws XLWrapException;
		public abstract Cell next() throws XLWrapException, XLWrapEOFException;
		
	}

}

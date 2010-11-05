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

import at.jku.xlwrap.common.XLWrapException;
import at.jku.xlwrap.exec.ExecutionContext;
import at.jku.xlwrap.spreadsheet.Cell;

/**
 * @author dorgon
 *
 */
public class NullRange extends Range {
	public final static NullRange INSTANCE = new NullRange();
	
	/**
	 * private constructor (singleton)
	 */
	private NullRange() {}

	@Override
	public Range shiftCols(int n, Range restrict, ExecutionContext context) throws IndexOutOfBoundsException { return this;}

	@Override
	public Range shiftRows(int n, Range restrict, ExecutionContext context) throws IndexOutOfBoundsException { return this;}

	@Override
	public Range shiftSheets(int n, Range restrict, ExecutionContext context) throws XLWrapException { return this;}

	@Override
	public Range changeFileName(String fileName, Range restrict, ExecutionContext context) { return this;}

	@Override
	public Range changeSheetName(String sheetName, Range restrict, ExecutionContext context) { return this;}
	
	@Override
	public Range changeSheetNumber(int n, Range restrict, ExecutionContext context) { return this;}

	@Override
	public Range getAbsoluteRange(ExecutionContext context) {
		return INSTANCE;
	}
	
	@Override
	public CellIterator getCellIterator(ExecutionContext context) throws XLWrapException {
		return new CellIterator(this, context) {
			@Override
			public boolean hasNext() {
				return false;
			}
			
			@Override
			public Cell next() throws XLWrapException {
				return null;
			}
		};
	}
	
	@Override
	public boolean withinSheetBounds(ExecutionContext context) {
		return true;
	}
	
	@Override
	public Range copy() {
		return INSTANCE;
	}
	
	@Override
	public boolean subsumes(Range other, ExecutionContext context) {
		return equals(other); // subsumes no other range except itself
	}
	
	@Override
	public String toString() {
		return "{}";
	}
	
}

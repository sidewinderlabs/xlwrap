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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import at.jku.xlwrap.common.XLWrapException;
import at.jku.xlwrap.exec.ExecutionContext;
import at.jku.xlwrap.spreadsheet.Cell;
import at.jku.xlwrap.spreadsheet.XLWrapEOFException;

/**
 * @author dorgon
 *
 * may contain other cell and box ranges (must NOT contain another MultiRange)
 */
public class MultiRange extends Range {
	protected final List<Range> ranges;
	
	public MultiRange() {
		this.ranges = new ArrayList<Range>();
	}
	
	@Override
	public Range getAbsoluteRange(ExecutionContext context) throws XLWrapException {
		MultiRange mr = new MultiRange();
		for (Range r : ranges)
			mr.addRange(r.getAbsoluteRange(context));
		return mr;
	}

	public void addRange(Range range) {
		if (range instanceof MultiRange)
			throw new IllegalArgumentException("Cannot add another multirange into a multirange.");
		ranges.add(range);
	}
	
	/**
	 * @return an iterator over single ranges
	 */
	public Iterator<Range> getRangeIterator() {
		return ranges.iterator();
	}
	
	@Override
	public Range shiftCols(int n, Range restrict, ExecutionContext context) throws IndexOutOfBoundsException, XLWrapException {
		MultiRange result = new MultiRange();
		for (Range sub : ranges)
			result.addRange(sub.shiftCols(n, restrict, context));
		return result;
	}
	
	@Override
	public Range shiftRows(int n, Range restrict, ExecutionContext context) throws IndexOutOfBoundsException, XLWrapException {
		MultiRange result = new MultiRange();
		for (Range sub : ranges)
			result.addRange(sub.shiftRows(n, restrict, context));
		return result;
	}
	
	@Override
	public Range shiftSheets(int n, Range restrict, ExecutionContext context) throws XLWrapException {
		MultiRange result = new MultiRange();
		for (Range sub : ranges)
			result.addRange(sub.shiftSheets(n, restrict, context));
		return result;
	}
	
	@Override
	public Range changeFileName(String fileName, Range restrict, ExecutionContext context) throws XLWrapException {
		MultiRange result = new MultiRange();
		for (Range sub : ranges)
			result.addRange(sub.changeFileName(fileName, restrict, context));
		return result;
	}
	
	@Override
	public Range changeSheetName(String sheetName, Range restrict, ExecutionContext context) throws XLWrapException {
		MultiRange result = new MultiRange();
		for (Range sub : ranges)
			result.addRange(sub.changeSheetName(sheetName, restrict, context));
		return result;
	}
	
	@Override
	public Range changeSheetNumber(int n, Range restrict, ExecutionContext context) throws XLWrapException {
		MultiRange result = new MultiRange();
		for (Range sub : ranges)
			result.addRange(sub.changeSheetNumber(n, restrict, context));
		return result;
	}
	
	@Override
	public CellIterator getCellIterator(ExecutionContext context) throws XLWrapException {
		return new CellIterator(getAbsoluteRange(context), context) {
			Iterator<Range> rangeIt;
			CellIterator current;

			@Override
			public void init(Range range) throws XLWrapException {
				rangeIt = ((MultiRange) range).getRangeIterator(); // iterator over absolute ranges
				nextIterator();
			}

			private void nextIterator() throws XLWrapException {
				if (rangeIt.hasNext())
					current = rangeIt.next().getCellIterator(context);
				else
					current = null;
			}
			
			@Override
			public boolean hasNext() throws XLWrapException {
				return current != null;
			}
			
			@Override
			public Cell next() throws XLWrapException, XLWrapEOFException {
				Cell next = current.next();
				
				// current has no more items, prepare next iterator or set current to null
				if (!current.hasNext())
					nextIterator();
				
				return next;
			}
			
		};
	}
	
	@Override
	public boolean subsumes(Range other, ExecutionContext context) throws XLWrapException {
		if (other == NullRange.INSTANCE)
			return true;

		// both are multi ranges
		else if (other instanceof MultiRange) {
			// any other's sub range must be subsumed by at least one of these sub ranges
			Iterator<Range> it = ((MultiRange) other).getRangeIterator();
			while (it.hasNext())
				if (!subsumes(it.next(), context)) // recursive call, will eval else clause for each other's sub range
					return false;
			return true;
			
		} else {	// at least one sub range must subsume other
			Iterator<Range> it = getRangeIterator();
			while (it.hasNext())
				if (it.next().subsumes(other, context))
					return true;
			return false;
		}
	}
	
	@Override
	public boolean withinSheetBounds(ExecutionContext context) {
		for (Range r : ranges)
			if (!r.withinSheetBounds(context))
				return false;
		return true;
	}
	
	@Override
	public Range copy() {
		MultiRange mr = new MultiRange();
		for (Range r : ranges)
			mr.addRange(r.copy());
		return mr;
	}
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		for (Range r : ranges)
			sb.append(r.toString()).append("; ");
		return sb.toString();
	}

	/**
	 * @return the number of single ranges
	 */
	public int size() {
		return ranges.size();
	}
}

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
package at.jku.xlwrap.map.expr;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import at.jku.xlwrap.common.Utils;
import at.jku.xlwrap.common.XLWrapException;
import at.jku.xlwrap.exec.ExecutionContext;
import at.jku.xlwrap.map.expr.val.XLExprValue;
import at.jku.xlwrap.map.range.CellRange;
import at.jku.xlwrap.map.range.Range;
import at.jku.xlwrap.spreadsheet.Cell;
import at.jku.xlwrap.spreadsheet.XLWrapEOFException;


/**
 * @author dorgon
 *
 */
public class E_RangeRef extends XLExpr0 {
	private static final Logger log = LoggerFactory.getLogger(E_RangeRef.class);
	private Range range;
	
	/**
	 * parse string to Range
	 * @param range as string
	 * @throws XLWrapException 
	 */
	public E_RangeRef(String range) throws XLWrapException {
		this.range = Utils.parseRange(range);
	}
	
	/**
	 * @param range
	 */
	public E_RangeRef(Range range) {
		this.range = range;
	}

	/**
	 * @return the range
	 */
	public Range getRange() {
		return range;
	}
	
	/**
	 * @param range the range to set
	 */
	public void setRange(Range range) {
		this.range = range;
	}
	
	/**
	 * may return null
	 */
	@Override
	public XLExprValue<?> eval(ExecutionContext context) throws XLWrapException, XLWrapEOFException {
		Range absolute = range.getAbsoluteRange(context);
		if (!(absolute instanceof CellRange))
			throw new XLWrapException("Cannot get value from multi range: " + range + ", needs to be wrapped by another function such as SUM() producing a single value.");
		else {
			Cell cell;
			CellRange cr = (CellRange) absolute;
			try {
				cell = context.getCell(cr);
			} catch (XLWrapEOFException eof) {
				throw eof;
			} catch (Exception e) {
				throw new XLWrapException("Failed to get cell " + cr + ".", e); 
			}
			return Utils.getXLExprValue(cell);
		}
	}

	@Override
	public XLExpr copy() {
		return new E_RangeRef(range.copy());
	}

	@Override
	public String toString() {
		return range.toString();
	}
}

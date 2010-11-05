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
package at.jku.xlwrap.map.expr.func.math;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import at.jku.xlwrap.common.Utils;
import at.jku.xlwrap.common.XLWrapException;
import at.jku.xlwrap.exec.ExecutionContext;
import at.jku.xlwrap.map.expr.E_RangeRef;
import at.jku.xlwrap.map.expr.TypeCast;
import at.jku.xlwrap.map.expr.XLExpr;
import at.jku.xlwrap.map.expr.func.XLExprFunction;
import at.jku.xlwrap.map.expr.val.XLExprNumber;
import at.jku.xlwrap.map.expr.val.XLExprValue;
import at.jku.xlwrap.map.range.Range;
import at.jku.xlwrap.map.range.Range.CellIterator;
import at.jku.xlwrap.spreadsheet.Cell;
import at.jku.xlwrap.spreadsheet.XLWrapEOFException;

/**
 * @author dorgon
 *
 */
public class E_FuncSUM extends XLExprFunction {
	private static final Logger log = LoggerFactory.getLogger(E_FuncSUM.class);
	
	/**
	 * default constructor 
	 */
	public E_FuncSUM() {
	}
	
	/** 
	 * constructor
	 *
	 * @param arg typically a E_RangeRef, but others also valid
	 */
	public E_FuncSUM(XLExpr arg) {
		args.add(arg);
	}

	@Override
	public XLExprValue<?> eval(ExecutionContext context) throws XLWrapException, XLWrapEOFException {
		XLExprNumber<?> total = null;
		
		// SUM() may have multiple arguments
		for (XLExpr arg : args) {
			if (arg instanceof E_RangeRef) {
				Range range = ((E_RangeRef) arg).getRange();
				
				// iterate over cells and sum up values string with an integer
				CellIterator it = range.getCellIterator(context);
				while (it.hasNext()) {
					Cell cell = it.next();
					XLExprNumber<?> other = TypeCast.toExprNumber(Utils.getXLExprValue(cell));
					if (other == null) {
						log.warn("Null value encountered in " + toString() + " for cell " + cell.getCellInfo() + ", skipping...");
						return null;
					}
					total = (total != null) ? total.add(other) : other;
				}
			} else if (arg instanceof XLExprNumber)
				total = (total != null) ? total.add((XLExprNumber<?>) arg) : (XLExprNumber<?>) arg;
				
			throw new XLWrapException("Invalid arguments: " + toString() + ".");
		}
		return total;
	}	
	
}
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
package at.jku.xlwrap.map.expr.val;

import at.jku.xlwrap.common.XLWrapException;
import at.jku.xlwrap.exec.ExecutionContext;
import at.jku.xlwrap.map.expr.XLExpr0;


/**
 * @author dorgon
 *
 */
public abstract class XLExprValue<T> extends XLExpr0 {
	
	/** the primitive Java value */
	protected T value;

	/** casted flag, set if a type cast function was applied by the user */
	protected boolean casted = false;

	public XLExprValue(T value) {
		if (value == null)
			throw new IllegalArgumentException("Null value supplied for constructor of " + getClass().getName() + ".");
		this.value = value;
	}
	
	public T getValue() {
		return value;
	}
	
	@Override
	public XLExprValue<?> eval(ExecutionContext context) throws XLWrapException {
		return this;
	}
	
	@Override
	public String toString() {
		return value.toString();
	}

	@SuppressWarnings("unchecked")
	public int compareTo(XLExprValue<?> other) {
		if (value instanceof Comparable && other.value instanceof Comparable) {
			if (value instanceof Long && other.value instanceof Double)
				return ((Double) ((Long) value).doubleValue()).compareTo((Double) other.value);
			else if (value instanceof Double && other.value instanceof Long)
				return ((Double) value).compareTo((Double) ((Long) other.value).doubleValue());
			else
				return ((Comparable) value).compareTo(other.value);
		} else {
			if (value.equals(other.value)) return 0;
			else return -1;
		}
	}

	/**
	 * sets the casted flag
	 */
	public void setCastedFlag() {
		casted = true;
	}
	
	/**
	 * @return the casted
	 */
	public boolean isCasted() {
		return casted;
	}
}

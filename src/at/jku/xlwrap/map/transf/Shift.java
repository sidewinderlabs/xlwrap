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
package at.jku.xlwrap.map.transf;

import at.jku.xlwrap.common.XLWrapException;

/**
 * @author dorgon
 *
 */
public abstract class Shift extends TransformationBase {
	/** the shift factor */
	protected final int shift;
	
	/** times to repeat including initial state (no shift) */
	protected final int repeat;


	/**
	 * @param shift
	 * @param repeat
	 * @param restriction
	 * @param skipCondition
	 * @param breakCondition
	 * @throws XLWrapException
	 */
	public Shift(int shift, int repeat, String restriction, String skipCondition, String breakCondition) throws XLWrapException {
		super(restriction, skipCondition, breakCondition);
		this.shift = shift;
		this.repeat = repeat;
	}
	
	/**
	 * @return the repeat
	 */
	public int getRepeat() {
		return repeat;
	}
	
	/**
	 * @return the shift
	 */
	public int getShift() {
		return shift;
	}	

}

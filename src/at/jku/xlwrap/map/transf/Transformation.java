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

import at.jku.xlwrap.exec.ExecutionContext;
import at.jku.xlwrap.exec.TransformationStage;
import at.jku.xlwrap.map.expr.XLExpr;
import at.jku.xlwrap.map.range.Range;

/**
 * @author dorgon
 *
 */
public interface Transformation {
	
	/** get restriction range, may not be null */
	public Range getRestriction();
	
	/** get break condition, may not be null */
	public XLExpr getBreakCondition();
	
	/** get skip condition, may not be null */
	public XLExpr getSkipCondition();

	public String getName();
	
	public String toString();

	/**
	 * @param context
	 * @return an implementation for the corresponding transformation executor
	 */
	public TransformationStage getExecutor(ExecutionContext context);

}

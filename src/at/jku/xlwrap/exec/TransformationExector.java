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
package at.jku.xlwrap.exec;

import at.jku.xlwrap.common.XLWrapException;
import at.jku.xlwrap.map.range.Range;

/**
 * @author dorgon
 *
 */
public interface TransformationExector {
	
	/**
	 * initialize the TransformationExecutor
	 */
	public void init();

	/**
	 * check for next and transform if possible - this will change the internal state (e.g. iterators)
	 * a transformation executor must at least provide one transformation
	 * 
	 * @return true if executor has more
	 * @throws XLWrapException 
	 */
	public boolean hasMoreTransformations() throws XLWrapException;
	
	/**
	 * apply transformation to a range
	 * this needs to be called multiple times for each range reference, thus, the
	 * implementation must not change the internal state (any iterators) until next call to thisHasMoreTransformations()
	 * 
	 * @param range
	 * @param restriction only apply transformation if range is subsumed by restriction range or to parts of a multi range
	 * @return transformed range object
	 * @throws XLWrapException
	 */
	public Range transform(Range range, Range restriction) throws XLWrapException;

}

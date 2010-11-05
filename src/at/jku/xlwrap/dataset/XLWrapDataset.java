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
package at.jku.xlwrap.dataset;

import at.jku.xlwrap.engine.XLWrapEngine;

import com.hp.hpl.jena.sparql.core.DatasetImpl;

/**
 * @author dorgon
 *
 * XLWrapDatasetGraph will register a custom query engine.
 */
public class XLWrapDataset extends DatasetImpl {
	
	/**
	 * @param union
	 */
	public XLWrapDataset(String cacheDir, String watchDir) {
		super(new XLWrapDatasetGraph(cacheDir, watchDir));
	}

	/**
	 * @return
	 */
	public XLWrapEngine getXLWrapEngine() {
		return ((XLWrapDatasetGraph) dsg).getXLWrapEngine();
	}
	
}

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

import at.jku.xlwrap.common.Constants;
import at.jku.xlwrap.vocab.XLWrap;

import org.pojava.datetime.DateTimeConfig;

import com.hp.hpl.jena.assembler.Assembler;
import com.hp.hpl.jena.assembler.Mode;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.sparql.core.assembler.DatasetAssembler;
import com.hp.hpl.jena.tdb.TDBFactory;
import com.hp.hpl.jena.util.FileUtils;

/**
 * @author dorgon
 *
 */
public class XLWrapDatasetAssembler extends DatasetAssembler {
	
	@Override
	public Object open(Assembler ass, Resource description, Mode mode) {
		// get cacheDir property value
		String cacheDir;
		Statement s = description.getProperty(XLWrap.cacheDir);
		if (s != null) {
			if (s.getObject().isLiteral())
				cacheDir = s.getString();
			else
				cacheDir = FileUtils.toFilename(s.getResource().getURI());
		} else if (System.getProperty(Constants.SYSTEMPROPERTY_CACHEDIR) != null)
			cacheDir = System.getProperty(Constants.SYSTEMPROPERTY_CACHEDIR);
		else
			cacheDir = Constants.DEFAULT_CACHE_DIR;
		
		// get watchDir property value
		s = description.getProperty(XLWrap.watchDir);
		String watchDir;
		if (s != null) {
			if (s.getObject().isLiteral())
				watchDir = s.getString();
			else
				watchDir = FileUtils.toFilename(s.getResource().getURI());			
		} else if (System.getProperty(Constants.SYSTEMPROPERTY_WATCHDIR) != null)
			watchDir = System.getProperty(Constants.SYSTEMPROPERTY_WATCHDIR);
		else
			watchDir = Constants.DEFAULT_WATCH_DIR;

		DateTimeConfig.globalEuropeanDateFormat();
		try {
			return new XLWrapDataset(cacheDir, watchDir);
		} catch (Exception e) {
			throw new RuntimeException("Failed to assemble XLWrap dataset.", e);
		} 
	}
}

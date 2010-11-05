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
package at.jku.xlwrap.engine;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.Date;
import java.util.Set;

import at.jku.xlwrap.common.XLWrapException;
import at.jku.xlwrap.map.XLWrapMapping;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.util.FileUtils;

/**
 * implements a single cache storing the generated triples for a XLWrapMapping instance
 * 
 * @author dorgon
 */
public class Cache {
	private final Model model;
	private final Set<String> referredFiles;
	private final long created;
	
	public Cache(XLWrapMapping mapping, Model model) throws XLWrapException {
		this.model = model;
		this.referredFiles = mapping.getReferredFiles();
		this.created = new Date().getTime();
	}
	
	/**
	 * check if one of the referred files in the mapping has changed
	 * @return
	 * @throws IOException 
	 * @throws MalformedURLException 
	 */
	public boolean referredFilesChanged() throws MalformedURLException, IOException {
		for (String file : referredFiles) {
			if (FileUtils.isURI(file)) {
				URLConnection url = new URL(file).openConnection();
				if (url.getLastModified() > created)
					return true;
			} else if (FileUtils.isFile(file)) {
				File f = new File(file);
				if (f.lastModified() > created)
					return true;
			}
		}
		
		return false;
	}

	/**
	 * @return the model
	 */
	public Model getModel() {
		return model;
	}
	
	/**
	 * @return the created
	 */
	public long getCreated() {
		return created;
	}

	/**
	 * @return the referredFiles
	 */
	public Set<String> getReferredFiles() {
		return referredFiles;
	}
	
	/**
	 * free resources
	 */
	public void close() {
		model.close();
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "Cache for " + referredFiles + " (created: " + new Date(created).toString() + ")";
	}
}

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
import java.io.FilenameFilter;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import at.jku.semwiq.endpoint.JosekiInstance;
import at.jku.semwiq.rmi.SpawnedEndpointMetadata;
import at.jku.xlwrap.common.Constants;
import at.jku.xlwrap.common.XLWrapException;
import at.jku.xlwrap.exec.XLWrapMaterializer;
import at.jku.xlwrap.map.MappingParser;
import at.jku.xlwrap.map.XLWrapMapping;

import com.hp.hpl.jena.datatypes.xsd.XSDDateTime;
import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.ontology.OntModelSpec;
import com.hp.hpl.jena.query.Dataset;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import com.hp.hpl.jena.rdf.model.impl.RDFReaderFImpl;
import com.hp.hpl.jena.tdb.TDB;
import com.hp.hpl.jena.tdb.TDBFactory;
import com.hp.hpl.jena.util.FileUtils;
import com.hp.hpl.jena.vocabulary.DC;

/**
 * @author dorgon
 *
 */
public class XLWrapEngine {
	private static final Logger log = LoggerFactory.getLogger(XLWrapEngine.class);
	
	/** source file property */
	public static final Property SOURCE_FILE_PROPERTY = ModelFactory.createDefaultModel().createProperty("http://open.vocab.org/terms/sourcefile");
	
	/** cache direcotry */
	private final String cacheDir;
	
	/** watch directory */
	private final String watchDir;
	
	/** TDB dataset */
	private final Dataset dataset;
	
	/** the master model over all sub graphs, target for queries, union model */
	private OntModel masterCache;

	/** prefixes */
	private final Map<String, String> cachedPrefixes = new Hashtable<String, String>();
	
	/** materializer */
	private XLWrapMaterializer materializer;

	/** updating flag, indicates if server is currently updating caches */
	private boolean updating = false;
	
	/**
	 * @param cacheDirectory TDB cache directory
	 * @param watchDirectory the directory to observe for mapping files (any files with .xml, .n3, or .ttl prefixes)
	 * @throws XLWrapException 
	 */
	public XLWrapEngine(String cacheDir, String watchDir) {
		this.cacheDir = cacheDir;
		this.watchDir = watchDir;

		this.dataset = TDBFactory.createDataset(cacheDir);
		RDFReaderFImpl.setBaseReaderClassName("N3", com.hp.hpl.jena.n3.turtle.TurtleReader.class.getName()); // use old Jena reader for N3
		RDFReaderFImpl.setBaseReaderClassName("TTL", com.hp.hpl.jena.n3.turtle.TurtleReader.class.getName()); // use old Jena reader for N3

		init();
		checkForChanges();
	}

	/** initialize */
	public void init() {
		log.info("Initializing XLWrap engine (watch dir: " + watchDir + ", TDB cache dir: " + cacheDir + ")");
		
		TDB.sync(this.dataset);
		masterCache = ModelFactory.createOntologyModel(OntModelSpec.RDFS_MEM);
		cachedPrefixes.clear();
		for (Iterator<String> it = listCachedNames() ; it.hasNext() ;) {
			Model model = getCachedModel(it.next());
			masterCache.addSubModel(model);
			cachedPrefixes.putAll(model.getNsPrefixMap());
		}
	}
	
	/**
	 * @return the watchDirectory
	 */
	public String getWatchDirectory() {
		return watchDir;
	}

	/**
	 * @return the watchDirectory as absolute path
	 * @throws IOException 
	 */
	public String getWatchDirectoryAbsolute() throws IOException {
		return new File(watchDir).getCanonicalPath();
	}

	/**
	 * @return the dataset
	 */
	public Dataset getDataset() {
		return dataset;
	}
	
	/**
	 * @return
	 */
	public Iterator<String> listCachedNames() {
		return dataset.listNames();
	}
	
	/**
	 * get a specific model for a given mapping file URI
	 * 
	 * @param canonical
	 * @return
	 */
	public Model getCachedModel(String canonical) {
		return dataset.getNamedModel(canonical);
	}

	/**
	 * check for changes in mapping files and referred spreadsheet files
	 */
	public void checkForChanges() {
		updating = true;
		try {
			File f = new File(watchDir);
			if (f.isFile())
				checkFileChanged(f);
			else if (f.isDirectory()) {
				File[] mappingFiles = f.listFiles(new FilenameFilter() {
					@Override
					public boolean accept(File dir, String name) {
						return name.endsWith(".trig"); // mapping files use TriG syntax (http://www4.wiwiss.fu-berlin.de/bizer/TriG/)
					}
				});
				
				// for each file in the directory
				for (File file : mappingFiles)
					checkFileChanged(file);
			}
			
			// check for deleted mapping files
			for (Iterator<String> it = listCachedNames() ; it.hasNext() ;) {
				String fileUri = it.next();
				if (!new File(FileUtils.toFilename(fileUri)).exists())
					removeFromCache(fileUri);
			}
		} catch (Throwable e) {
			log.error(e.getMessage(), e);
			
		} finally {
			updating = false;			
			materializer = null;
		}		
	}
	
	
	/** check if a file has changed and rebuild the cached model
	 * 
	 * @param file
	 * @throws XLWrapException 
	 */
	private void checkFileChanged(File file) throws XLWrapException {
		try {
			String fileUri = "file:" + file.getCanonicalPath();
			if (!isCached(fileUri) ||
				file.lastModified() > getTimestamp(fileUri).getTimeInMillis()	// mapping file has been modified
				|| referredFilesChanged(fileUri))								// some of the referred spreadsheet files have been modified
					reloadIntoCache(fileUri);
		} catch (IOException e) {
			log.error("Failed to check " + file.getAbsolutePath() + " for changes.", e);
		}
	}

	/**
	 * @param canonical
	 * @return
	 */
	private Resource getMetadataResource(String canonical) {
		return dataset.getDefaultModel().getResource(canonical);
	}
	
	/**
	 * @param fileUri
	 * @return
	 * @throws XLWrapException 
	 * @throws IOException 
	 * @throws MalformedURLException 
	 */
	private boolean referredFilesChanged(String fileUri) throws XLWrapException, MalformedURLException, IOException {
		Resource meta = getMetadataResource(fileUri);

		long created = getTimestamp(fileUri).getTimeInMillis();
		StmtIterator it = meta.listProperties(SOURCE_FILE_PROPERTY);
		if (!it.hasNext())
			log.warn("No information on associated spreadsheet files found in meta data for <" + fileUri + ">!");

		String file;
		while (it.hasNext()) {
			file = it.nextStatement().getResource().getURI();
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
	 * 
	 * @param canonical
	 * @return
	 */
	public List<String> getReferredFiles(String canonical) {
		List<String> files = new ArrayList<String>();
		StmtIterator it = getMetadataResource(canonical).listProperties(SOURCE_FILE_PROPERTY);
		while (it.hasNext())
			files.add(it.next().getResource().getURI());
		return files;
	}
	
	/**
	 * 
	 * @param fileUri
	 * @return
	 */
	public Calendar getTimestamp(String fileUri) throws XLWrapException {
		Resource meta = getMetadataResource(fileUri);
		Statement s = meta.getProperty(DC.date);
		if (s == null)
			throw new XLWrapException("Could not find a timestamp for <" + fileUri + "> in meta data!");
		
		Object o = s.getLiteral().getValue();
		XSDDateTime dt = (XSDDateTime) o;
		return dt.asCalendar();
	}
	
	/**
	 * @param fileUri mapping file URI
	 * @return
	 */
	private boolean isCached(String fileUri) {
		boolean modelExists = dataset.containsNamedModel(fileUri);
		boolean metaExists = getMetadataResource(fileUri).listProperties().hasNext();

		if (modelExists && !metaExists)
			log.warn("Mapping <" + fileUri + "> is cached but no meta data found.");
		if (metaExists && !modelExists)
			log.warn("Meta data found for mapping <" + fileUri + ">  but no cached model exists.");
		
		if (modelExists && metaExists)
			return true;
		else
			return false;			
	}
	
	/**
	 * @param fileUri
	 */
	public void reloadIntoCache(String fileUri) {
		// create new materializer
		if (materializer == null) {
			SpawnedEndpointMetadata meta = JosekiInstance.getLastSpawnedEndpointMetadata();
			materializer = new XLWrapMaterializer(meta.getHostname(), meta.getPort(), JosekiInstance.getLastPubbyPathPrefix());
		}
		
		try {
			XLWrapMapping map = MappingParser.parse(fileUri);
			if (map.isOffline()) {
				if (isCached(fileUri))
					removeFromCache(fileUri);
				log.warn("Offline flag set in mapping '" + fileUri + "', skipped.");
				return;
			}
			
			if (isCached(fileUri))
				log.info("Processing changed XLWrap mapping: '" + fileUri + "'...");
			else
				log.info("Processing new XLWrap mapping: '" + fileUri + "'...");
			
			// TODO: inference / forward rules into TDB model - if (Config.inferenceEnabled())
			Model model = dataset.getNamedModel(fileUri);
			model.removeAll();
			materializer.generateModel(map, model);
			
			// create meta data entry
			Resource meta = getMetadataResource(fileUri);
			meta.removeProperties();
			for (String f : map.getReferredFiles())
				meta.addProperty(SOURCE_FILE_PROPERTY, meta.getModel().createResource(f));
			meta.addLiteral(DC.date, Calendar.getInstance());
			
			masterCache.addSubModel(model);
			cachedPrefixes.putAll(model.getNsPrefixMap());
			TDB.sync(dataset);
			
			log.info("Mapping <" + fileUri + "> now in cache (" + model.size() + " triples)");
		} catch (XLWrapException e) {
			log.error("Processing failed for mapping '" + fileUri + "'.", e);
		}
	}

	/**
	 * @param file
	 */
	private void removeFromCache(String canonicalFileName) {
		log.info("Cleaning cache for mapping '" + canonicalFileName + "' ...");

		Model model = dataset.getNamedModel(canonicalFileName);
		masterCache.removeSubModel(model);
		masterCache.rebind();

		// clear cached data
		model.removeAll();
		model.close();
		Resource meta = getMetadataResource(canonicalFileName);
		meta.removeProperties();
		
		// TODO - improve: currently cached prefixes remain in cache until server is restarted
	}

	/**
	 * blocks if server is currently updating caches
	 * this is only part of the story and should prevent concurrency issues in some cases
	 * to fully support locking, the requester (e.g. query engine) would have to care about locking also
	 * 
	 * @return the masterCache for queries
	 */
	public OntModel getModel() {
		if (Constants.CHECK_FOR_CHANGES)
			checkForChanges();
		
		while (updating) {
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {}
		}
		return masterCache;
	}

	/**
	 * @return
	 */
	public Map<String, String> getNsPrefixMap() {
		return cachedPrefixes;
	}
	
	/**
	 * clean resources
	 */
	public void shutdown() {
		dataset.close();
		masterCache.close();
		
		log.info("XLWrap-Server shutdown cleanly.");
	}

}

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
package at.jku.xlwrap.map.expr.func.text;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import at.jku.xlwrap.common.XLWrapException;
import at.jku.xlwrap.exec.ExecutionContext;
import at.jku.xlwrap.map.expr.TypeCast;
import at.jku.xlwrap.map.expr.XLExpr;
import at.jku.xlwrap.map.expr.func.XLExprFunction;
import at.jku.xlwrap.map.expr.val.E_URI;
import at.jku.xlwrap.map.expr.val.XLExprValue;
import at.jku.xlwrap.spreadsheet.XLWrapEOFException;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import com.hp.hpl.jena.shared.JenaException;
import com.hp.hpl.jena.sparql.vocabulary.FOAF;

/**
 * @author Richard Cyganiak (richard@cyganiak.de)
 *
 */
public class E_FuncNAME2URI extends XLExprFunction {
	private static final Logger log = LoggerFactory.getLogger(E_FuncNAME2URI.class);
	
	private final static Map<String,CharacterTranslationTable> tables = 
		new HashMap<String,CharacterTranslationTable>();
	private final static Map<String,Model> consolidationModels =
		new HashMap<String,Model>();
	private final static Set<String> newURIs = new HashSet<String>();
	
	/**
	 * default constructor
	 */
	public E_FuncNAME2URI() {
	}
	
	/**
	 * constructor
	 * 
	 * @param namespace a URI prefix
	 * @param name a string to be converted into the URI's local part
	 * @param diacriticsFile file with character translations
	 * @param consolidationFile file with existing URI-to-name triples
	 */
	public E_FuncNAME2URI(XLExpr namespace, XLExpr name, 
			XLExpr diacriticsFile, XLExpr consolidationFile) {
		addArg(namespace);
		addArg(name);
		addArg(diacriticsFile);
		addArg(consolidationFile);
	}

	@Override
	public XLExprValue<String> eval(ExecutionContext context) throws XLWrapException, XLWrapEOFException {
		String namespace = TypeCast.toString(args.get(0).eval(context));
		String name = TypeCast.toString(args.get(1).eval(context));
		String diacriticsFile = TypeCast.toString(args.get(2).eval(context));
		String consolidationFile = TypeCast.toString(args.get(3).eval(context));
		if (name == null) return null;
		String existingURI = findExistingURI(namespace, name, consolidationFile);
		if (existingURI == null) {
			String newURI = namespace + handleSpecialCharacters(name, diacriticsFile);
			if (!newURIs.contains(newURI)) {
				log.info("New URI: <" + newURI + "> for '" + name + "'");
				newURIs.add(newURI);
			}
			return new E_URI(newURI);
		} else {
			return new E_URI(existingURI);
		}
	}

	public String findExistingURI(String namespace, String name, 
			String consolidationFile) throws XLWrapException {
		if (!consolidationModels.containsKey(consolidationFile)) {
			consolidationModels.put(consolidationFile, loadConsolidationModel(consolidationFile));
		}
		Model m = consolidationModels.get(consolidationFile);
		StmtIterator it = m.listStatements(null, FOAF.name, m.createLiteral(name));
		while (it.hasNext()) {
			Statement stmt = it.nextStatement();
			if (!stmt.getSubject().isURIResource()) continue;
			if (!stmt.getSubject().getURI().startsWith(namespace)) continue;
			return stmt.getSubject().getURI();
		}
		return null;
	}
	
	private Model loadConsolidationModel(String fileName) throws XLWrapException {
		try {
			log.info("Loading consolidation file: " + fileName);
			Model result = ModelFactory.createDefaultModel();
			result.read(new FileInputStream(fileName), "file:" + fileName, "RDF/XML");
			return result;
		} catch (IOException ex) {
			throw new XLWrapException(ex);
		} catch (JenaException ex) {
			throw new XLWrapException(ex);
		}
	}
	
	public static String handleSpecialCharacters(String s, String diacriticsFile) throws XLWrapException {
		if (!tables.containsKey(diacriticsFile)) {
			tables.put(diacriticsFile, new CharacterTranslationTable(diacriticsFile));
		}
		s = tables.get(diacriticsFile).translate(s);
		s = s.toLowerCase().replaceAll("'", "").replaceAll("[-+().,&\"/’;\\\\]", " ").trim().replaceAll("\\s+", "-");
		try {
			s = URLEncoder.encode(s, "UTF-8");
			if (s.length() > 50) {
				s = s.substring(0, 50);
			}
			return s;
		} catch (UnsupportedEncodingException ex) {
			throw new RuntimeException("Can't happen, UTF-8 always supported");
		}
	}

	public static class CharacterTranslationTable {
		private final Translation[] entries;
		public CharacterTranslationTable(String translationFile) throws XLWrapException {
			try {
				entries = parseTranslationFile(translationFile);
			} catch (IOException ex) {
				throw new XLWrapException("Error reading from translation file '" + translationFile + "': " + ex.getMessage(), ex);
			}
		}
		private Translation[] parseTranslationFile(String filename) throws IOException {
			log.info("Loading character translation file: " + filename);
			BufferedReader in = new BufferedReader(new InputStreamReader(new FileInputStream(filename), "UTF-8"));
			List<Translation> results = new ArrayList<Translation>();
			String line;
			int lineNumber = 0;
			while ((line = in.readLine()) != null) {
				lineNumber++;
				if (line.startsWith("#")) continue;		// comment
				if ("".equals(line)) continue;			// empty line
				int firstTab = line.indexOf('\t');
				if (firstTab == -1) {
					throw new IOException("Line " + lineNumber + " not a tab-separated pair: '" + line + "'");
				}
				results.add(new Translation(line.substring(0, firstTab), line.substring(firstTab + 1)));
			}
			return results.toArray(new Translation[results.size()]);
		}
		public String translate(String s) {
			for (Translation t: entries) {
				s = t.apply(s);
			}
			return s;
		}
		private class Translation {
			private final String original;
			private final String replacement;
			Translation(String original, String replacement) {
				this.original = original;
				this.replacement = replacement;
			}
			String apply(String s) {
				return s.replace(original, replacement);
			}
		}
	}
}

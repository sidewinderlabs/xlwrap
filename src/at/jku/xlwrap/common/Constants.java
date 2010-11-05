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
package at.jku.xlwrap.common;

import java.util.Hashtable;
import java.util.Map;

import at.jku.xlwrap.vocab.XLWrap;

/**
 * @author dorgon
 *
 */
public class Constants {

	public static final Map<String, String> DEFAULT_PREFIXES = new Hashtable<String, String>() {
		private static final long serialVersionUID = -6167513556069402751L;
		{
			put("rdfs",	"http://www.w3.org/2000/01/rdf-schema#");
			put("rdf",	"http://www.w3.org/1999/02/22-rdf-syntax-ns#");
			put("xsd",	"http://www.w3.org/2001/XMLSchema#");
			put("foaf",	"http://xmlns.com/foaf/0.1/");
			put("xl",	XLWrap.getURI());
			put("ex",	"http://example.org/");
		}};

	public static final String DEFAULT_PREFIXES_N3;
	public static final String DEFAULT_PREFIXES_SPARQL;

	static {
		StringBuilder sbN3 = new StringBuilder();
		StringBuilder sbSPARQL= new StringBuilder();
		for (String pfx : DEFAULT_PREFIXES.keySet()) {
			sbN3.append("@prefix ").append(pfx).append(":\t\t<").append(DEFAULT_PREFIXES.get(pfx)).append("> .\n");
			sbSPARQL.append("PREFIX ").append(pfx).append(":\t\t<").append(DEFAULT_PREFIXES.get(pfx)).append(">\n");
		}
		DEFAULT_PREFIXES_N3 = sbN3.toString();
		DEFAULT_PREFIXES_SPARQL = sbSPARQL.toString();
	}

	// server
	public static final String SYSTEMPROPERTY_WATCHDIR = "xlwrap.watchdir";
	public static final String DEFAULT_WATCH_DIR = "mappings";
	public static final String SYSTEMPROPERTY_CACHEDIR = "xlwrap.cachedir";
	public static final String DEFAULT_CACHE_DIR = "data";
	public static final boolean CHECK_FOR_CHANGES = true;
	
	// materializer
	public static final int DEFAULT_SHIFT_STEPS = 1;
	public static final boolean EMPTY_STRING_AS_NULL = true;

	/** namespace for variables suggested by Richard Cyganiak */
	public static final String VARIABLE_NS = "http://purl.org/NET/xlwrap/var#";


	
}

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
package at.jku.xlwrap.spreadsheet.csv;

import java.util.Hashtable;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.hp.hpl.jena.rdf.model.Resource;

import at.jku.xlwrap.vocab.XLWrap;

/**
 * @author dorgon
 *
 */
public class CSVParseConfig {
	public static final CSVParseConfig TAB_QUOTED = new CSVParseConfig("\t", "\"", true, new char[] {'\f', '\r'});
	public static final CSVParseConfig TAB_UNQUOTED = new CSVParseConfig("\t", "", true, new char[] {'\f', '\r'});
	public static final CSVParseConfig COMMA_QUOTED = new CSVParseConfig(",", "\"", true, new char[] {'\t', '\f', '\r'});
	public static final CSVParseConfig COMMA_UNQUOTED = new CSVParseConfig(",", "", true, new char[] {'\t', '\f', '\r'});
	public static final CSVParseConfig SEMICOLON_QUOTED = new CSVParseConfig(";", "\"", true, new char[] {'\t', '\f', '\r'});
	public static final CSVParseConfig SEMICOLON_UNQUOTED = new CSVParseConfig(";", "", true, new char[] {'\t', '\f', '\r'});
	public static final CSVParseConfig DEFAULT_IF_UNDETECTABLE = SEMICOLON_QUOTED;
	
	public static final Map<Resource, CSVParseConfig> parseProfiles;
	
	static {
		parseProfiles = new Hashtable<Resource, CSVParseConfig>();
		parseProfiles.put(XLWrap.tab_quoted, TAB_QUOTED);
		parseProfiles.put(XLWrap.tab_unquoted, TAB_UNQUOTED);
		parseProfiles.put(XLWrap.comma_quoted, COMMA_QUOTED);
		parseProfiles.put(XLWrap.comma_unquoted, COMMA_UNQUOTED);
		parseProfiles.put(XLWrap.semicolon_quoted, SEMICOLON_QUOTED);
		parseProfiles.put(XLWrap.semicolon_unquoted, SEMICOLON_UNQUOTED);
	}
	
	private final String colSep;
	private final String valDelimiter;
	private final boolean valDelimOptional;
	
	/** white spaces between valDelimiter and colSep */
	private final char[] whiteSpaces;
	
	/**
	 * constructor
	 */
	public CSVParseConfig(String colSep, String valDelimiter, boolean valDelimOptional, char[] whiteSpaces) {
		this.colSep = colSep;
		this.valDelimiter = valDelimiter;
		this.valDelimOptional = valDelimOptional;
		this.whiteSpaces = whiteSpaces;
	}
	
	public static CSVParseConfig getParseConfig(Resource profile) {
		return parseProfiles.get(profile);
	}
	
	/**
	 * simple auto detection
	 * @param line
	 * @return
	 */
	public static CSVParseConfig autoDetectConfig(String line) {
		if (line == null || line.length() == 0)
			return null;
		
		// detection: if TAB found, use TAB; else count COMMA and SEMICOLON and use most frequent one
		String colSep;
		if (line.contains("\t"))
			colSep = "\t";
		else {
			int countComma = count(line, ",");
			int countSemicolon = count(line, ";");
			colSep = (countComma > countSemicolon) ? "," : ";";
		}
		
		String first = line.substring(0, 1);
		String valDelimiter = "";
		if (first.equals("\""))
			valDelimiter = "\"";
		else if (first.equals("'"))
			valDelimiter = "'";
		char[] ws = new char[] { '\f', '\r' };
		return new CSVParseConfig(colSep, valDelimiter, true, ws);
	}
	
	/** count occurrences of pattern in string
	 * 
	 * @param string
	 * @param pattern
	 * @return
	 */
	private static int count(String string, String pattern) {
		Matcher m = Pattern.compile(pattern).matcher(string);
		int count = 0;
		while (m.find()) count++;
		return count;
	}
	
	/**
	 * @return the valDelimiter
	 */
	public String getValDelimiter() {
		return valDelimiter;
	}

	/**
	 * @return the valDelimOptional
	 */
	public boolean isValDelimOptional() {
		return valDelimOptional;
	}
	
	/**
	 * @return
	 */
	public String getColSeperator() {
		return colSep;
	}
	
	/**
	 * @return the whiteSpaces
	 */
	public char[] getWhiteSpaces() {
		return whiteSpaces;
	}

}

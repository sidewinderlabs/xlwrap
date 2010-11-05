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

import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import at.jku.xlwrap.common.Constants;
import at.jku.xlwrap.common.XLWrapException;
import at.jku.xlwrap.spreadsheet.Cell;
import at.jku.xlwrap.spreadsheet.Sheet;
import at.jku.xlwrap.spreadsheet.XLWrapEOFException;

/**
 * @author dorgon
 *
 * CSV is opened as a stream, new rows are read in on-demand and trashed if the cache reaches a specified MAX_CACHE_SIZE rows.
 */
public class CSVSheet implements Sheet {
	private static final Logger log = LoggerFactory.getLogger(CSVSheet.class);
	
	private static final int DEFAULT_CACHE_SIZE = 500;
	
	private final BufferedReader in;
	private final String file;
	private CSVParseConfig cfg = null;

	/** cache of rows */
	private final Row[] cache;
	
	/** cache pointer */
	private int pt = 0;
	
	/** cache re-write iterations */
	private int iterations = 0;

	/** done */
	private boolean finished = false;
	
	/** lower cache boundary */
	private int firstRow = 0;
	
	/** upper cache boundary */
	private int lastRow = 0;
	
	
	/** number of columns, determined at creation */
	private final int columns;
	
	/** number of rows, unknown, assume maximum */
	private final int rows = Integer.MAX_VALUE;
	
	/**
	 * @param in
	 * @param file
	 * @param parseConfig, if null CSVParseConfig.DEFAULT is used
	 * @param cacheSize, if null DEFAULT_CACHE_SIZE is used
	 * @throws IOException 
	 */
	public CSVSheet(BufferedReader in, String file, Integer cacheSize) throws XLWrapException {
		this.in = in;
		this.file = file;
		this.cache = new Row[cacheSize != null ? cacheSize : DEFAULT_CACHE_SIZE];
		
		// init
		readRow();
		this.columns = getRow(0).columns();		
	}

	/**
	 * get row from cache or dynamically fetch more rows as needed
	 * @param i
	 * @return row
	 * @throws XLWrapException
	 */
	private Row getRow(int i) throws XLWrapException {
		if (i < firstRow)
			throw new XLWrapException("Row " + i + " has already been removed from the cache, please increase the cache size for the CSVWorkbook.");
		while (lastRow <= i && !finished)
			readRow();
		
		if (finished && i > lastRow) // EOF
			return null;
		else
			return cache[i % cache.length];
	}

	/**
	 * read next row into cache
	 * @throws IOException 
	 */
	private void readRow() throws XLWrapException {
		try {
			String line;
			while (true) {	// Skip empty lines
				line = in.readLine();
				if (line == null) {
					finished = true;
					in.close();
					return;
				}
				if (line.length() > 0) break;
			}
			
			// auto-detect parse config
			if (cfg == null)
				cfg = CSVParseConfig.autoDetectConfig(line);
			if (cfg == null)
				cfg = CSVParseConfig.DEFAULT_IF_UNDETECTABLE;
			
			cache[pt] = new Row(line, lastRow);
			lastRow++;			// move last row of boundary
			if (iterations > 0)
				firstRow++;		// move lower boundary starting with second iteration

			if (pt < cache.length-1) {
				pt++;			// move cache pointer
			} else {
				pt = 0;
				iterations++;
				log.debug("Read " + lastRow + " rows from " + file);
			}
		} catch (IOException e) {
			throw new XLWrapException("Failed to read next line from CSV file " + getSheetInfo() + ".", e);
		}
	}

	@Override
	public Cell getCell(int column, int row) throws XLWrapException, XLWrapEOFException {
		Row r = getRow(row);
		if (r != null)
			return r.getCell(column);
		else
			throw new XLWrapEOFException();
	}

	@Override
	public int getColumns() {
		return columns;
	}

	@Override
	public String getName() {
		return null;
	}

	@Override
	public int getRows() {
		return rows;
	}

	@Override
	public String getSheetInfo() {
		return file;
	}

	/**
	 * class storing row data
	 * 
	 * @author dorgon
	 */
	private class Row {
		private final int row;
		private final CSVCell[] cells;
		
		/**
		 * constructor
		 * @throws XLWrapException 
		 */
		public Row(String line, int row) throws XLWrapException {
			this.row = row;
			List<CSVCell> coll = new SimpleLineParser(line, row).parse();
			
			int size = Math.max(coll.size(), columns); 
			cells = new CSVCell[size];
			int i;
			for (i = 0; i < coll.size(); i++)
				cells[i] = coll.get(i);
			for (; i < size; i++)
				cells[i] = null;
		}
		
		/**
		 * @return the cells
		 */
		public CSVCell getCell(int col) {
			return cells[col];
		}
		
		/**
		 * number of columns
		 * @return
		 */
		public int columns() {
			return cells.length;
		}
		
		/* (non-Javadoc)
		 * @see java.lang.Object#toString()
		 */
		@Override
		public String toString() {
			StringBuilder sb = new StringBuilder();
			sb.append("row ").append(row).append(": (");
			for (CSVCell c : cells)
				sb.append("'").append(c.toString()).append("'").append(", ");
			sb.append(")");
			return sb.toString();
		}
	}

	private class SimpleLineParser {
		private String line;
		private int row;
		
		private final char[] buf;
		private int idx = 0;
		private char next;

		private final String delim;
		private final boolean delimOpt;
		private final char delimStart;
		private final String sep;
		private final char sepStart;
		private final char[] ws;

		private List<CSVCell> coll = new ArrayList<CSVCell>();
		private int cols = 0;
		
		public SimpleLineParser(String s, int row) {
			this.line = s;
			this.row = row;
			
			this.buf = s.toCharArray();
			this.delim = cfg.getValDelimiter();
			this.delimOpt = cfg.isValDelimOptional();
			this.delimStart = (delim != null && delim.length() > 0) ? delim.charAt(0) : 0; 
			this.sep = cfg.getColSeperator();
			this.sepStart = (sep != null && sep.length() > 0) ? sep.charAt(0) : 0;
			this.ws = cfg.getWhiteSpaces();			
		}
		
		public List<CSVCell> parse() throws XLWrapException {
			if (hasNext())
				next();
			cell();
			while (hasNext()) {
				expect(sep, false);
				cell();
			}
			return coll;
		}

//		private void cell() throws XLWrapException {
//			whitespace();
//			boolean usingDelim = expect(delim, delimOpt);
//			parseValue(usingDelim);
//			expect(delim, delimOpt);
//			whitespace();
//		}
		
		// read over single quote if !usingDelim, or over two quotes if usingDelim
		private void cell() throws XLWrapException {
			StringBuffer text = new StringBuffer();
			whitespace();
			boolean usingDelim = expect(delim, delimOpt);
			text.append(value(usingDelim));
			expect(delim, delimOpt);
			if (usingDelim) {
				// delimiter escaping by doubling the delimiter
				while (nextIsDelimStart()) {
					text.append(delim);
					expect(delim, true);
					text.append(value(false));
					expect(delim, true);
				}
			}
			whitespace();
			String lex = (text.length() == 0 && Constants.EMPTY_STRING_AS_NULL) ? null : text.toString();
			coll.add(new CSVCell(file, cols++, row, lex));
		}
		
		private boolean hasNext() {
			return idx < buf.length;
		}
		
//		private boolean expectNext(char c) {
//			return hasNext() && buf[idx+1] == c;
//		}
		
		private void next() {
			next = buf[idx++];
		}
		
		private void whitespace() {
			while (hasNext()) {
				for (char c : ws)
					if (next == c) {
						next(); // skip whitespace
						break;
					}
				return; // no whitespace, return
			}
		}
		
		/**
		 * @param s
		 * @param optional if true, pass but don't throw exception if it does not occur
		 * @return true if found, false otherwise
		 * @throws XLWrapException
		 */
		private boolean expect(String s, boolean optional) throws XLWrapException {
			if (s == null || s.length() == 0)
				return true; // empty string always "found"
			for (char c : s.toCharArray()) {
				if (next != c) {
					if (optional)
						return false; // not found, was optional
					else
						throw new XLWrapException("Invalid line, was expecting '" + s + "' at position " + idx + " in line " + row + ":" + line);					
				}
				if (hasNext()) // if has next, proceed (otherwise we are at the end of a line)
					next();
			}
			return true; // s found
		}

		private boolean nextIsDelimStart() {
			if (delim == null || delim.length() == 0) return false;
			return hasNext() && next == delimStart;
		}
		
		private String value(boolean ignoreSep) {
			int start = idx-1;
			for (;;) {
				if (delim != null && next == delimStart ||
					!ignoreSep && next == sepStart) { // delim or sep reached
					return new String(buf, start, idx-1-start);					
				}
				
				if (hasNext())
					next();
				
				// end of line
				else return new String(buf, start, idx-start); // +1
			}			
		}

	}
}

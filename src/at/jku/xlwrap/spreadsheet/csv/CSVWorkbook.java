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
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import at.jku.xlwrap.common.XLWrapException;
import at.jku.xlwrap.spreadsheet.Sheet;
import at.jku.xlwrap.spreadsheet.Workbook;

/**
 * @author dorgon
 *
 */
public class CSVWorkbook implements Workbook {
	private static final Logger log = LoggerFactory.getLogger(CSVWorkbook.class);
	
	private final BufferedReader in;
	private final String file;
	
	/**
	 * @param stream
	 * @param fileName
	 */
	public CSVWorkbook(InputStream stream, String fileName) throws UnsupportedEncodingException {
		this.in = new BufferedReader(new InputStreamReader(stream, "utf-8"));
		this.file = fileName;
	}

	@Override
	public boolean supportsMultipleSheets() {
		return false;
	}
	
	@Override
	public void close() {
		try {
			in.close();
		} catch (IOException e) {
			log.error("Failed to close open CSV input stream.", e);
		}
	}

	@Override
	public Sheet getSheet(int sheet) throws XLWrapException {
		return new CSVSheet(in, file, null);
	}

	@Override
	public Sheet getSheet(String sheetName) throws XLWrapException {
		return new CSVSheet(in, file, null);
	}

	@Override
	public String[] getSheetNames() {
		return new String[] { "Default" };
	}

	@Override
	public String getWorkbookInfo() {
		return file;
	}

}

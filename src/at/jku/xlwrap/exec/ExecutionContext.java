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

import java.io.IOException;
import java.util.Hashtable;
import java.util.Map;

import jxl.read.biff.BiffException;
import at.jku.xlwrap.common.XLWrapException;
import at.jku.xlwrap.map.MapTemplate;
import at.jku.xlwrap.map.range.CellRange;
import at.jku.xlwrap.spreadsheet.Cell;
import at.jku.xlwrap.spreadsheet.Sheet;
import at.jku.xlwrap.spreadsheet.Workbook;
import at.jku.xlwrap.spreadsheet.WorkbookFactory;
import at.jku.xlwrap.spreadsheet.XLWrapEOFException;
import at.jku.xlwrap.spreadsheet.csv.CSVParseConfig;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;

/**
 * @author dorgon
 *
 */
public class ExecutionContext {
	private MapTemplate activeTemplate;
	private Model targetModel;
	private final NodeReplacer nodeReplacer;
	
	private final Map<String, Workbook> openWorkbooks;
	private final Map<String, Sheet> openSheetsByNumber;
	private final Map<String, Sheet> openSheetsByName;
	private final Map<String, Map<String, Integer>> sheetNumbers;

	// server info if running in web server
	private String hostname = null;
	private Integer port = null;
	private String pubbyPathPrefix = null;
	
	/**
	 * constructor for server environments
	 * 
	 * @param hostname
	 * @param port
	 */
	public ExecutionContext(String hostname, int port, String pubbyPathPrefix) {
		this();
		
		this.hostname = hostname;
		this.port = port;
		this.pubbyPathPrefix = pubbyPathPrefix;
	}
	
	/**
	 * constructor
	 */
	public ExecutionContext() {
		nodeReplacer = new NodeReplacer();
		targetModel = ModelFactory.createDefaultModel();

		openWorkbooks = new Hashtable<String, Workbook>();
		openSheetsByNumber = new Hashtable<String, Sheet>();
		openSheetsByName = new Hashtable<String, Sheet>();
		sheetNumbers = new Hashtable<String, Map<String, Integer>>();
	}

	/**
	 * sets the active template
	 * @param activeTemplate 
	 */
	public void setActiveTemplate(MapTemplate currentTemplate) {
		this.activeTemplate = currentTemplate;
	}
	
	/**
	 * @return the active template
	 */
	public MapTemplate getActiveTemplate() {
		return activeTemplate;
	}

	/**
	 * @param targetModel the targetModel to set
	 */
	public void setTargetModel(Model targetModel) {
		this.targetModel = targetModel;
	}
	
	/**
	 * @return the targetModel
	 */
	public Model getTargetModel() {
		return targetModel;
	}
	
	private String getKey(String fileName, Integer sheetNum) {
		return fileName + "#$" + sheetNum;
	}

	private String getKey(String fileName, String sheetName) {
		return fileName + "#$" + sheetName;
	}
	
	/**
	 * get sheet by name
	 * 
	 * @param fileName
	 * @param sheetName if null => first sheet
	 * @return
	 * @throws XLWrapException
	 */
	public Sheet getSheet(String fileName, String sheetName) throws XLWrapException {
		if (sheetName == null || sheetName.equals(""))
			return getSheet(fileName, 0); // return first sheet by number #0
		
		else {
			String key = getKey(fileName, sheetName);
			Sheet s = openSheetsByName.get(key);
			if (s == null) {
				s = getWorkbook(fileName).getSheet(sheetName);
				if (s == null)
					throw new XLWrapException("Sheet '" + sheetName + "' does not exist in '" + fileName + "'.");

				openSheetsByNumber.put(key, s);
			}
			return s;
		}
	}

	/**
	 * get sheet by number
	 * 
	 * @param fileName
	 * @param sheetNum if null => first sheet
	 * @return
	 * @throws XLWrapException
	 */
	public Sheet getSheet(String fileName, Integer sheetNum) throws XLWrapException {
		if (sheetNum == null)
			sheetNum = 0;
		
		String key = getKey(fileName, sheetNum);
		Sheet s = openSheetsByNumber.get(key);
		if (s == null) {
			try {
				s = getWorkbook(fileName).getSheet(sheetNum);
			} catch (ArrayIndexOutOfBoundsException e) {
				throw new XLWrapException("Sheet #" + (sheetNum+1) + " does not exist in '" + fileName + "'.", e);
			}

			openSheetsByNumber.put(key, s);
		}
		return s;
	}
	
	/**
	 * get the sheet number (zero-based) of a sheet
	 * @param fileName
	 * @param sheetName
	 * @return
	 * @throws XLWrapException
	 */
	public Integer getSheetNumber(String fileName, String sheetName) throws XLWrapException {
		Workbook wb = getWorkbook(fileName);
		if (!wb.supportsMultipleSheets())
			return 0; // e.g. CSVWorkbook
		
		Integer id = null;
		Map<String, Integer> sheets = sheetNumbers.get(fileName);
		if (sheets != null) {
			id = sheets.get(sheetName);
			if (id != null) return id;
		}
		
		// build new sheetNumberMap for workbook
		Map<String, Integer> sheetNumberMap = new Hashtable<String, Integer>();
		String[] names = wb.getSheetNames();
		Integer thisNumber = null;
		for (int i = 0; i < names.length; i++) {
			sheetNumberMap.put(names[i], i);
			if (names[i].equals(sheetName))
				thisNumber = i;
		}
		sheetNumbers.put(fileName, sheetNumberMap);
		
		if (thisNumber == null)
			throw new XLWrapException("Sheet '" + sheetName + "' does not exist in '" + fileName + "'.");
		else
			return thisNumber;
	}
	
	/**
	 * @param fileName
	 * @return
	 * @throws IOException 
	 * @throws BiffException 
	 */
	public Workbook getWorkbook(String fileName) throws XLWrapException {
		Workbook wb = openWorkbooks.get(fileName);
		if (wb == null) {
			try {
				wb = WorkbookFactory.getWorkbook(fileName);
				openWorkbooks.put(fileName, wb);
			} catch (XLWrapException e) {
				throw new XLWrapException("Failed to open workbook: " + fileName + ".", e);
			}
		}
		return wb;
	}

	/**
	 * @param absolute cell range
	 * @return the sheet for the absolute cell range
	 * @throws XLWrapException 
	 */
	public Sheet getSheet(CellRange absolute) throws XLWrapException {
		if (absolute.getSheetNum() != null)
			return getSheet(absolute.getFileName(), absolute.getSheetNum());
		else
			return getSheet(absolute.getFileName(), absolute.getSheetName());
	}

	/**
	 * @param absolute cell range
	 * @return the cell for the absolute cell range
	 * @throws XLWrapException
	 * @throws XLWrapEOFException 
	 */
	public Cell getCell(CellRange absolute) throws XLWrapException, XLWrapEOFException {
		return getSheet(absolute).getCell(absolute.getColumn(), absolute.getRow());
	}

	/**
	 * @param fileName
	 * @param sheetPointer
	 * @param colPointer
	 * @param rowPointer
	 * @return
	 * @throws XLWrapException 
	 * @throws XLWrapEOFException 
	 */
	public Cell getCell(String fileName, int sheetPointer, int colPointer, int rowPointer) throws XLWrapException, XLWrapEOFException {
		return getSheet(fileName, sheetPointer).getCell(colPointer, rowPointer);
	}

	/**
	 * destroy the context and close all opened workbooks
	 */
	public void destroy() {
		for (Workbook wb : openWorkbooks.values())
			wb.close();
	}

	/**
	 * @return nodeReplacer
	 */
	public NodeReplacer getNodeReplacer() {
		return nodeReplacer;
	}

	/**
	 * @return the hostname
	 */
	public String getHostname() {
		return hostname;
	}
	
	/**
	 * @return the port
	 */
	public Integer getPort() {
		return port;
	}
	
	/**
	 * @return the pubbyPathPrefix
	 */
	public String getPubbyPathPrefix() {
		return pubbyPathPrefix;
	}
}

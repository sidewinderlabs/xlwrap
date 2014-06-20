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
package at.jku.xlwrap.spreadsheet;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Hashtable;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import at.jku.xlwrap.common.XLWrapException;
import at.jku.xlwrap.spreadsheet.csv.CSVParseConfig;
import at.jku.xlwrap.spreadsheet.csv.CSVWorkbook;
import at.jku.xlwrap.spreadsheet.excel.ExcelWorkbook;
import at.jku.xlwrap.spreadsheet.opendoc.OpenDocumentWorkbook;
import at.jku.xlwrap.spreadsheet.poi.PoiWorkbook;

import com.hp.hpl.jena.util.FileUtils;

import org.apache.tika.metadata.Metadata;
import org.apache.tika.parser.AutoDetectParser;
import org.apache.tika.parser.ParseContext;
import org.apache.tika.parser.Parser;
import org.apache.tika.sax.BodyContentHandler;
import org.xml.sax.ContentHandler;

/**
 * @author dorgon
 *
 */
public class WorkbookFactory {
	public static enum Type {
		MSEXCEL, OPENDOCUMENT, OOFICE, OFFICEOPENXML, CSV, UNKNOWN
	}

	public static final Map<String, Type> extToType= new Hashtable<String, Type>();
	static {
		extToType.put("xls", Type.MSEXCEL);
		extToType.put("xlsx", Type.OFFICEOPENXML);
		extToType.put("ods", Type.OPENDOCUMENT);
		extToType.put("sxc", Type.OOFICE);
		extToType.put("csv", Type.CSV);
		extToType.put("txt", Type.CSV);
	}

	public static final Map<String, Type> mimeToType= new Hashtable<String, Type>();
	static {
		mimeToType.put("application/vnd.ms-excel", Type.MSEXCEL);
		mimeToType.put("application/x-tika-msoffice", Type.MSEXCEL);
		mimeToType.put("application/vnd.oasis.opendocument.spreadsheet", Type.OPENDOCUMENT);
		mimeToType.put("application/vnd.sun.xml.calc", Type.OOFICE);
		mimeToType.put("application/x-tika-ooxml", Type.OFFICEOPENXML);
		mimeToType.put("text/plain", Type.CSV);
		mimeToType.put("text/csv", Type.CSV);
	}
	
	/**
	 * @param fileName
	 * @return
	 * @throws XLWrapException
	 */
	public static Workbook getWorkbook(String fileName) throws XLWrapException {
		String ext = fileName.substring(fileName.lastIndexOf(".")+1, fileName.length());
		Type t = extToType.get(ext);
		if (t == null) {
			t = getTypeFromContent(fileName);			
		}
		try {
			switch(t) {
			case MSEXCEL:
                                //Suggest to still use jxl as Apachee poi can not handle Excel 95 files.
				return new ExcelWorkbook(open(fileName), fileName);
                        case OFFICEOPENXML:
                                //Use apache Poi
                                return new PoiWorkbook(open(fileName), fileName);
			case OPENDOCUMENT:
				File f = null;
				if (FileUtils.isURI(fileName))
					f = downloadToTemp(fileName); //TODO test
				else
					f = new File(fileName);
				return new OpenDocumentWorkbook(f, fileName);
			
			case CSV:
				return new CSVWorkbook(open(fileName), fileName);
				
			default:
				throw new XLWrapException("Cannot open document '" + fileName + "', file type is not recognized.");
			}
		} catch (MalformedURLException e) {
			throw new XLWrapException("Failed to open spreadsheet from <" + fileName + ">.", e);
		} catch (Throwable e) {
			throw new XLWrapException("Failed to open spreadsheet file '" + fileName + "'.", e);
		}
	}
	
	/**
	 * @param fileName
	 * @return Type
	 * @throws XLWrapException 
	 */
	private static Type getTypeFromContent(String fileName) throws XLWrapException {
		Type type = null;
	    InputStream is = null;
	    try {
	      is = open(fileName);
	      ContentHandler contenthandler = new BodyContentHandler();
	      Metadata metadata = new Metadata();
	      metadata.set(Metadata.RESOURCE_NAME_KEY, fileName);
	      Parser parser = new AutoDetectParser();
	      parser.parse(is, contenthandler, metadata, new ParseContext());
	      String mimeType = metadata.get(Metadata.CONTENT_TYPE);
	      if (mimeType != null)
	    	  type = mimeToType.get(mimeType);
	    } catch (Exception e) {
	    	throw new XLWrapException("Unable to read file " + fileName, e);
	    } finally {
	        if (is != null) {
	        	try {
	        		is.close();
	        	} catch (Exception exc) {
	        		// ignore
	        	}
	        }
	    }	
	    if (type == null) {
	    	type = Type.UNKNOWN;
	    }
	    return type;
	}

	/**
	 * @param fileName
	 * @return
	 * @throws XLWrapException 
	 */
	private static File downloadToTemp(String fileName) throws IOException, XLWrapException {
		String tmpDir = System.getProperty("java.io.tmpdir");
		if (!tmpDir.endsWith(File.pathSeparator))
			tmpDir = "/" + tmpDir;
		
		URL url = new URL(fileName);
		InputStream in = url.openStream();
		
		String file = null;
		Matcher m = Pattern.compile("^.*\\/(.*)$").matcher(fileName);
    	if (m.find())
    		file = m.group(1);
		
		File tmp = new File(tmpDir + file);
		BufferedOutputStream out;
		try {
			out = new BufferedOutputStream(new FileOutputStream(tmp));
		} catch (IOException e) {
			throw new XLWrapException("Failed to download " + fileName + ", cannot write into temp directory (" + tmpDir + file + ".");
		}
		if (!tmp.canWrite())
			throw new XLWrapException("Failed to download " + fileName + ", cannot write into temp directory " + tmpDir + ".");

		int b;
		while ((b = in.read()) >= 0)
			out.write(b);
		in.close();
		out.close();
		
		return tmp;
	}

	private static InputStream open(String url) throws MalformedURLException, IOException {
		if (FileUtils.isURI(url))
			return new URL(url).openStream();
		else
			return new FileInputStream(url);
	}
}

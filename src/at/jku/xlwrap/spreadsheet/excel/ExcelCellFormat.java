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
package at.jku.xlwrap.spreadsheet.excel;

import java.text.DateFormat;
import java.text.NumberFormat;

import jxl.format.Format;
import at.jku.xlwrap.spreadsheet.FormatAnnotation;

/**
 * @author dorgon
 *
 */
public class ExcelCellFormat implements FormatAnnotation {
	private final Format format;
	
	/**
	 * constructor
	 */
	public ExcelCellFormat(Format format) {
		this.format = format;
	}
	
	@Override
	public boolean isNumberFormat() {
		return format instanceof jxl.write.NumberFormat;
	}

	@Override
	public boolean isDateFormat() {
		return format instanceof jxl.write.DateFormat;
	}

	@Override
	public NumberFormat getNumberFormat() {
		if (format instanceof jxl.write.NumberFormat)
			 return ((jxl.write.NumberFormat) format).getNumberFormat();
		else
			return null;
	}
	
	@Override
	public DateFormat getDateFormat() {
		if (format instanceof jxl.write.DateFormat)
			 return ((jxl.write.DateFormat) format).getDateFormat();
		else
			return null;
	}
}

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
package at.jku.xlwrap.spreadsheet.opendoc;

import java.text.DateFormat;
import java.text.NumberFormat;

import org.jopendocument.dom.spreadsheet.CellStyle;

import at.jku.xlwrap.spreadsheet.FormatAnnotation;

/**
 * @author dorgon
 *
 */
public class OpenDocumentFormat implements FormatAnnotation {
	private final CellStyle style;
	
	/**
	 * @param style
	 */
	public OpenDocumentFormat(CellStyle style) {
		this.style = style;
	}

	@Override
	public DateFormat getDateFormat() {
		return null;
	}

	@Override
	public NumberFormat getNumberFormat() {
		return null;
	}

	@Override
	public boolean isDateFormat() {
		return false;
	}

	@Override
	public boolean isNumberFormat() {
		return false;
	}

	/**
	 * @return the style
	 */
	public CellStyle getStyle() {
		return style;
	}
}

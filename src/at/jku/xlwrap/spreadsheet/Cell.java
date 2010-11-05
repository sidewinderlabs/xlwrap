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

import java.util.Date;

import jxl.biff.formula.FormulaException;
import at.jku.xlwrap.common.XLWrapException;

/**
 * @author dorgon
 *
 */
public interface Cell {

	/**
	 * @return
	 * @throws XLWrapException 
	 */
	public TypeAnnotation getType() throws XLWrapException;

	/**
	 * @return
	 */
	public FormatAnnotation getFormat();
	
	/**
	 * @return
	 * @throws FormulaException
	 */
	public String getFormula() throws FormulaException;
	
	/**
	 * @return
	 * @throws XLWrapException 
	 */
	public double getNumber() throws XLWrapException;
	
	/**
	 * @return
	 * @throws XLWrapException 
	 */
	public boolean getBoolean() throws XLWrapException;

	/**
	 * @return
	 * @throws XLWrapException 
	 */
	public Date getDate() throws XLWrapException;

	/**
	 * @return
	 */
	public String getText() throws XLWrapException;

	/**
	 * @return
	 * @throws XLWrapException 
	 */
	public int getInteger() throws XLWrapException;

	/**
	 * @return
	 * @throws XLWrapException 
	 */
	public float getFloat() throws XLWrapException;

	/**
	 * @return
	 * @throws XLWrapException 
	 */
	public long getLong() throws XLWrapException;
	
	/**
	 * @return
	 * @throws XLWrapException 
	 */
	public double getDouble() throws XLWrapException;

	/**
	 * get info about the location of the cell used for error messages
	 * @return
	 */
	public String getCellInfo();

}

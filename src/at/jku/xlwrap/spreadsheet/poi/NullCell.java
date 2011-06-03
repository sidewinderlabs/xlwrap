/**
 * Copyright 2009 Andreas Langegger, andreas@langegger.at, Austria
 * Copyright 2011 Christian Brenninkmeijer, brenninc@cs.man.ac.uk, Manchester England
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
package at.jku.xlwrap.spreadsheet.poi;

import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import at.jku.xlwrap.common.Constants;
import at.jku.xlwrap.common.Utils;
import at.jku.xlwrap.common.XLWrapException;
import at.jku.xlwrap.spreadsheet.Cell;
import at.jku.xlwrap.spreadsheet.FormatAnnotation;
import at.jku.xlwrap.spreadsheet.TypeAnnotation;

/**
 * @author Christian based on code by dorgon
 *
 */
public class NullCell implements Cell {
    private static final Logger log = LoggerFactory.getLogger(NullCell.class);
	
    /**
     * constructor
     */
    public NullCell() {
    }
	
    @Override
    public boolean getBoolean() throws XLWrapException {
        throw new XLWrapException("getBoolean is not supported for a NULL cell");
    }

    @Override
    public Date getDate() throws XLWrapException {
        throw new XLWrapException("getDate is not supported for a NULL cell");
    }

    @Override
    public double getDouble() throws XLWrapException {
        throw new XLWrapException("getDouble is not supported for a NULL cell");
    }

    @Override
    public float getFloat() throws XLWrapException {
        throw new XLWrapException("getFloat is not supported for a NULL cell");
    }

    @Override
    public int getInteger() throws XLWrapException {
        throw new XLWrapException("getInteger is not supported for a NULL cell");
    }

    @Override
    public long getLong() throws XLWrapException {
        throw new XLWrapException("getLong is not supported for a NULL cell");
    }

    @Override
    public String getText() throws XLWrapException {
        //TODO or should this be null
        return "";
    }
	
    @Override
    public double getNumber() throws XLWrapException {
        throw new XLWrapException("getNumber is not supported for a NULL cell");
    }

    @Override
    public TypeAnnotation getType() throws XLWrapException {
        return TypeAnnotation.NULL;
    }

    //@Override
    //To the best of my knoweldge this method is never atcuall called.
    public FormatAnnotation getFormat() {
        return null;
    }
	
    @Override
    public String getFormula() {
        return null;
    }
	
    @Override
    public String getCellInfo() {
        return "Null Cell";
    }
}

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

import java.text.DateFormat;
import java.text.NumberFormat;

import at.jku.xlwrap.spreadsheet.FormatAnnotation;

/**
 * @author Christian based on code by dorgon
 * To the best of my knowledge this is never actually used.
 */
public class PoiFormat implements FormatAnnotation {

    private int cellType;

    /**
     * constructor
     */
    public PoiFormat(int cellType) {
        this.cellType = cellType;
    }

    @Override
    public boolean isNumberFormat() {
        //TODO handle org.apache.poi.ss.usermodel.Cell..CELL_TYPE_FORMULA
        return cellType == org.apache.poi.ss.usermodel.Cell.CELL_TYPE_NUMERIC;
    }

    @Override
    public boolean isDateFormat() {
        //TODO handle org.apache.poi.ss.usermodel.Cell..CELL_TYPE_FORMULA
        return cellType == org.apache.poi.ss.usermodel.Cell.CELL_TYPE_BOOLEAN;
    }

    @Override
    public NumberFormat getNumberFormat() {
        //TODO handle org.apache.poi.ss.usermodel.Cell..CELL_TYPE_FORMULA
        //TODO work out how this is used to get best.
        return null;
    }
	
    @Override
    public DateFormat getDateFormat() {
        //TODO work out how this is used to get best.
        return null;
    }
}

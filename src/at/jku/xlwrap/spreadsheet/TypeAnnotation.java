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

import at.jku.xlwrap.common.Constants;

/**
 * @author dorgon
 *
 * Excel stores all numbers and dates as double values.
 * 
 * A cell type is NULL either if the cell is empty or if it is a zero-length text cell and {@link Constants}.EMPTY_STRING_AS_NULL is true. 
 */
public enum TypeAnnotation {
	BOOLEAN, NUMBER, TEXT, DATE, NULL
}

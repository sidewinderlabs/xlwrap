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
package test.xlwrap.spreadsheet;

import test.xlwrap.exec.TestShifting;
import at.jku.xlwrap.common.XLWrapException;
import at.jku.xlwrap.exec.ExecutionContext;
import at.jku.xlwrap.map.MapTemplate;
import at.jku.xlwrap.spreadsheet.csv.CSVParseConfig;
import at.jku.xlwrap.vocab.XLWrap;

/**
 * @author dorgon
 *
 */
public class TestCSVWorkbookShifting extends TestShifting {

	static {
		TEST_FILE_DATA1 = "testing/test-data1.csv";
		TEST_FILE_EMPTY = "testing/test-empty.csv";
	}
	
	// currently we only support E_Long and E_Double => explicit cast only possible to LONG/DOUBLE:	
	private static final String TEST_GRAPH_PERSONS =
		"[ xl:uri \"'http://example.org/' & A4 & '_' & B4\"^^xl:Expr ] a \"URI(('http://xmlns.com/foaf/0.1/' & A1))\"^^xl:Expr ; " +
		"foaf:name \"A4 & ' ' & B4\"^^xl:Expr ; " +
		"foaf:mbox \"C4\"^^xl:Expr ; " +
		"ex:age \"LONG(D4)\"^^xl:Expr ; " +
		"ex:birthday \"LONG(E4)\"^^xl:Expr ; " +
		"ex:retired \"BOOLEAN(F4)\"^^xl:Expr ; " +
		"ex:salary \"DOUBLE(G4)\"^^xl:Expr ; " +
		"ex:boss [ xl:uri \"'http://example.org/' & SUBSTITUTE(H4, ' ', '_')\"^^xl:Expr ] .";

	// currently we only support E_Long and E_Double => explicit cast only possible to LONG/DOUBLE:	
	public static final String TEST_GRAPH_REVENUES =
		"[] a ex:Revenue ; " +
		"ex:units \"LONG(B17)\"^^xl:Expr ;" +
		"ex:revenue \"DOUBLE(C17)\"^^xl:Expr ;" +
		"ex:product \"A17\"^^xl:Expr ;" +
		"ex:year \"LONG(B15)\"^^xl:Expr ;" +
		"ex:country \"A14\"^^xl:Expr .";
	

	private static final String CHECK_MODEL_PERSONS =
		"<http://example.org/{0}> a foaf:Person ; " +
		"foaf:name \"{1}\" ; " +
		"foaf:mbox \"{2}\" ; " +
		"ex:age \"{3}\"^^xsd:long; " +
		"ex:birthday \"{4}\"^^xsd:long; " +
		"ex:retired \"{5}\"^^xsd:boolean ; " +
		"ex:salary \"{6}\"^^xsd:double ; " +
		"ex:boss <http://example.org/{7}> ; ";

	private static final String CHECK_MODEL_REVENUES =
		"[] a ex:Revenue ; " +
		"ex:units \"{0}\"^^xsd:long ; " +
		"ex:revenue \"{1}\"^^xsd:double; " +
		"ex:product \"{2}\" ; " +
		"ex:year \"{3}\"^^xsd:long ;" +
		"ex:country \"{4}\" ; ";
	
	@Override
	protected MapTemplate createPersonTemplate() throws XLWrapException {
		MapTemplate t = new MapTemplate(TEST_FILE_DATA1, XLWrap.tab_unquoted, TEST_SHEET_DATA1_1, TEST_GRAPH_PERSONS);
		t.initAndValidate(new ExecutionContext());
		return t;
	}
	
	@Override
	protected MapTemplate createReveneueTemplate() throws XLWrapException {
		MapTemplate t = new MapTemplate(TEST_FILE_DATA1, XLWrap.tab_unquoted, TEST_SHEET_DATA1_1, TEST_GRAPH_REVENUES);
		t.initAndValidate(new ExecutionContext());
		return t;
	}

	/* (non-Javadoc)
	 * @see test.xlwrap.exec.TestShifting#getCheckModelPersons()
	 */
	@Override
	protected String getCheckModelPersons() {
		return CHECK_MODEL_PERSONS;
	}
	
	/* (non-Javadoc)
	 * @see test.xlwrap.exec.TestShifting#getCheckModelRevenues()
	 */
	@Override
	protected String getCheckModelRevenues() {
		return CHECK_MODEL_REVENUES;
	}
}

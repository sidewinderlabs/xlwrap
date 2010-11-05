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
package test.xlwrap;

import at.jku.xlwrap.common.XLWrapException;
import at.jku.xlwrap.exec.ExecutionContext;
import at.jku.xlwrap.map.MapTemplate;
import at.jku.xlwrap.map.XLExprDatatype;
import at.jku.xlwrap.map.XLWrapMapping;
import at.jku.xlwrap.vocab.XLWrap;

import com.hp.hpl.jena.datatypes.TypeMapper;


/**
 * @author dorgon
 *
 */
public class XLWrapTestCase {
	
	static {
		// register xl:Expr (a custom RDF data type)
		TypeMapper.getInstance().registerDatatype(XLExprDatatype.instance);
	}
	
	public static String TEST_FILE_DATA1 = "testing/test-data1.xls";
	public static String TEST_FILE_DATA1_CSV = "testing/test-data1.csv";
	public static String TEST_FILE_DATA1_ODS = "testing/test-data1.ods";
	public static String TEST_SHEET_DATA1_1 = "Tests 1";
	
	public static String TEST_FILE_EMPTY = "testing/test-empty.xls";
	public static String TEST_FILE_EMPTY_ODS = "testing/test-empty.ods";
	
	public static final String TEST_GRAPH_PERSONS =
			"[ xl:uri \"'http://example.org/' & A4 & '_' & B4\"^^xl:Expr ] a \"URI(('http://xmlns.com/foaf/0.1/' & A1))\"^^xl:Expr ; " +
			"foaf:name \"A4 & ' ' & B4\"^^xl:Expr ; " +
			"foaf:mbox \"C4\"^^xl:Expr ; " +
			"ex:age \"D4\"^^xl:Expr ; " +
			"ex:birthday \"E4\"^^xl:Expr ; " +
			"ex:retired \"F4\"^^xl:Expr ; " +
			"ex:salary \"G4\"^^xl:Expr ; " +
			"ex:boss [ xl:uri \"'http://example.org/' & SUBSTITUTE(H4, ' ', '_')\"^^xl:Expr ] .";

	public static final String TEST_GRAPH_REVENUES =
		"[] a ex:Revenue ; " +
		"ex:units \"B17\"^^xl:Expr ;" +
		"ex:revenue \"DOUBLE(C17)\"^^xl:Expr ;" +
		"ex:product \"A17\"^^xl:Expr ;" +
		"ex:year \"B15\"^^xl:Expr ;" +
		"ex:country \"A14\"^^xl:Expr .";
	
	protected MapTemplate createEmptyTemplate() throws XLWrapException {
		MapTemplate t = new MapTemplate(TEST_FILE_EMPTY, XLWrap.tab_unquoted, 4, "");
		t.initAndValidate(new ExecutionContext());
		return t;
	}
	
	protected MapTemplate createPersonTemplate() throws XLWrapException {
		MapTemplate t = new MapTemplate(TEST_FILE_DATA1, XLWrap.tab_unquoted, TEST_SHEET_DATA1_1, TEST_GRAPH_PERSONS);
		t.initAndValidate(new ExecutionContext());
		return t;
	}
	
	protected MapTemplate createReveneueTemplate() throws XLWrapException {
		MapTemplate t = new MapTemplate(TEST_FILE_DATA1, XLWrap.tab_unquoted, TEST_SHEET_DATA1_1, TEST_GRAPH_REVENUES);
		t.initAndValidate(new ExecutionContext());
		return t;
	}
	
	protected XLWrapMapping createMapping(MapTemplate tmpl) {
		XLWrapMapping m = new XLWrapMapping();
		m.add(tmpl);
		return m;
	}
	
}

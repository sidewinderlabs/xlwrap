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
package test.xlwrap.exec;

import static org.junit.Assert.assertEquals;

import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.junit.Test;

import test.xlwrap.XLWrapTestCase;
import at.jku.xlwrap.common.Constants;
import at.jku.xlwrap.common.XLWrapException;
import at.jku.xlwrap.exec.XLWrapMaterializer;
import at.jku.xlwrap.map.MapTemplate;
import at.jku.xlwrap.map.transf.ColumnShift;
import at.jku.xlwrap.map.transf.RowShift;

import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;

/**
 * @author dorgon
 *
 */
public class TestShifting extends XLWrapTestCase {

	private static final String CHECK_MODEL_PERSONS =
		"<http://example.org/{0}> a foaf:Person ; " +
		"foaf:name \"{1}\" ; " +
		"foaf:mbox \"{2}\" ; " +
		"ex:age \"{3}\"^^xsd:byte ; " +
		"ex:birthday \"{4}\"^^xsd:short ; " +
		"ex:retired \"{5}\"^^xsd:boolean ; " +
		"ex:salary \"{6}\"^^xsd:double ; " +
		"ex:boss <http://example.org/{7}> ; ";

	private static final String CHECK_MODEL_REVENUES =
		"[] a ex:Revenue ; " +
		"ex:units \"{0}\"^^xsd:int ; " +
		"ex:revenue \"{1}\"^^xsd:double; " +
		"ex:product \"{2}\" ; " +
		"ex:year \"{3}\"^^xsd:int ;" +
		"ex:country \"{4}\" ; ";

	protected String getCheckModelPersons() {
		return CHECK_MODEL_PERSONS;
	}
	
	protected String getCheckModelRevenues() {
		return CHECK_MODEL_REVENUES;
	}
	
	@Test
	public void testPersonSingle() throws XLWrapException {
		XLWrapMaterializer mat = new XLWrapMaterializer();
		Model m = mat.generateModel(createMapping(createPersonTemplate()));
//		m.write(System.out, "N3");

		assertContains(m, getCheckModelPersons(), new Object[] { "Tom_Houston", "Tom Houston", "th@ex.com", 41, 1968, false, 50230.23, "Tim_Presley" });
		assertEquals(8, m.size());
	}
	
	@Test
	public void testPersonRowShift() throws XLWrapException {
		MapTemplate tmpl = createPersonTemplate();		
		tmpl.repeatTransform(new RowShift(1, 5, "A4:H4", null, null));
		
		XLWrapMaterializer mat = new XLWrapMaterializer();
		Model m = mat.generateModel(createMapping(tmpl));
		m.write(System.out, "N3");
		
		assertContains(m, getCheckModelPersons(), new Object[] { "_Smith", " Smith", "tim@ex.com", 29, 1980, true, 39340.03, "Tim_Presley" });
		assertContains(m, getCheckModelPersons(), new Object[] { "Tom_Houston", "Tom Houston", "th@ex.com", 41, 1968, false, 50230.23, "Tim_Presley" });
		assertContains(m, getCheckModelPersons(), new Object[] { "Tim_Presley", "Tim Presley", "jp@ex.com", 66, 1943, true, 69234.43, "Tim_Presley" });
		assertContains(m, getCheckModelPersons(), new Object[] { "Elvis_Carter", "Elvis Carter", "elvis@ex.com", 18, 1991, null, 30590.3, "Tim_Presley" });
		assertContains(m, getCheckModelPersons(), new Object[] { "Michael_", "Michael " });
		
		assertEquals(34, m.size());
	}
	
	@Test
	public void testRevenueSimple() throws XLWrapException {
		MapTemplate tmpl = createReveneueTemplate();
		XLWrapMaterializer mat = new XLWrapMaterializer();
		Model m = mat.generateModel(createMapping(tmpl));
//		m.write(System.out, "N3");
		
		assertContains(m, getCheckModelRevenues(), new Object[] { 342, 7866.0, "Product1", 2007, "Austria" });
	}

	@Test
	public void testRevenueRowShift() throws XLWrapException {
		MapTemplate tmpl = createReveneueTemplate();
		tmpl.repeatTransform(new RowShift(1, 4, "A17; B17:C17", null, null)); //TODO check condition and use Integer.MAX_VALUE for repeats
		
		XLWrapMaterializer mat = new XLWrapMaterializer();
		Model m = mat.generateModel(createMapping(tmpl));
		
		assertContains(m, getCheckModelRevenues(), new Object[] { 342, 7866.0, "Product1", 2007, "Austria" });
		assertContains(m, getCheckModelRevenues(), new Object[] { 4333, 1005256.00, "Product2", 2007, "Austria" });
		assertContains(m, getCheckModelRevenues(), new Object[] { 3312, 1136016.00, "Product3", 2007, "Austria" });
		assertContains(m, getCheckModelRevenues(), new Object[] { 45, 19350.00, "Product4", 2007, "Austria" });
	}

	@Test
	public void testRevenueRowColShift() throws XLWrapException {
		MapTemplate tmpl = createReveneueTemplate();
		tmpl.repeatTransform(new RowShift(1, 4, "A17; B17:C17", null, null)); //TODO check condition and use Integer.MAX_VALUE for repeats
		tmpl.repeatTransform(new ColumnShift(2, 3, "B15; B17:C20", null, null));
		
		XLWrapMaterializer mat = new XLWrapMaterializer();
		Model m = mat.generateModel(createMapping(tmpl));
//		m.write(System.out, "N3");
		
		assertContains(m, getCheckModelRevenues(), new Object[] { 342, 7866.0, "Product1", 2007, "Austria" });
		assertContains(m, getCheckModelRevenues(), new Object[] { 4333, 1005256.00, "Product2", 2007, "Austria" });
		assertContains(m, getCheckModelRevenues(), new Object[] { 3312, 1136016.00, "Product3", 2007, "Austria" });
		assertContains(m, getCheckModelRevenues(), new Object[] { 45, 19350.00, "Product4", 2007, "Austria" });

		assertContains(m, getCheckModelRevenues(), new Object[] { 376, 8648.00, "Product1", 2008, "Austria" });
		assertContains(m, getCheckModelRevenues(), new Object[] { 5655, 1328925.00, "Product2", 2008, "Austria" });
		assertContains(m, getCheckModelRevenues(), new Object[] { 4566, 1598100.00, "Product3", 2008, "Austria" });
		assertContains(m, getCheckModelRevenues(), new Object[] { 56, 24304.00, "Product4", 2008, "Austria" });

		assertContains(m, getCheckModelRevenues(), new Object[] { 490, 11760.00, "Product1", 2009, "Austria" });
		assertContains(m, getCheckModelRevenues(), new Object[] { 3493, 838320.00, "Product2", 2009, "Austria" });
		assertContains(m, getCheckModelRevenues(), new Object[] { 5993, 1917760.00, "Product3", 2009, "Austria" });
		assertContains(m, getCheckModelRevenues(), new Object[] { 54, 23328.00, "Product4", 2009, "Austria" });
	}

	private void assertContains(Model model, String checkModelBase, Object[] values) {
		String checkModel = createCheckModel(checkModelBase, values);
		String q = Constants.DEFAULT_PREFIXES_SPARQL + "ASK WHERE { " + checkModel + "}";
		Query query = QueryFactory.create(q);
		QueryExecution qe = QueryExecutionFactory.create(query, model);
		if (!qe.execAsk())
			throw new AssertionError("Model does not entail: " + checkModel);
		qe.close();
	}
	
	private String createCheckModel(String checkModelBase, Object[] values) {
		String out = "";
		Model tmp = ModelFactory.createDefaultModel();
		StringTokenizer tok = new StringTokenizer(checkModelBase, ";");
		String t;
		while (tok.hasMoreTokens()) {
			t = tok.nextToken().trim();
			Matcher m = Pattern.compile("\\{(\\d)\\}").matcher(t);
			if (m.find()) {
				int i = Integer.parseInt(m.group(1));
				if (values.length > i) {
					Object o = values[i];
					if (o != null) {
						String str = tmp.createTypedLiteral(o).getLexicalForm();
						out += t.replaceFirst("(?:\\{(" + i + ")\\})", str) + "; \n";
					}
				}
				continue;
			}
			
			if (t.length() > 0)
				out += t + "; \n";
		}
		return out + ".\n";
	}
}

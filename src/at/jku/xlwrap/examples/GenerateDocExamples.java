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
package at.jku.xlwrap.examples;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;

import at.jku.xlwrap.common.XLWrapException;
import at.jku.xlwrap.exec.XLWrapMaterializer;
import at.jku.xlwrap.map.MappingParser;
import at.jku.xlwrap.map.XLWrapMapping;

import com.hp.hpl.jena.ontology.OntModelSpec;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;

/**
 * @author dorgon
 *
 */
public class GenerateDocExamples {

	public static void main(String[] args) throws XLWrapException, FileNotFoundException {
		XLWrapMapping map = MappingParser.parse("mappings/iswc09-example-scovo.trig");
		
		XLWrapMaterializer mat = new XLWrapMaterializer();
		Model m = mat.generateModel(map);
		m.setNsPrefix("ex", "http://example.org/");
		m.write(new FileOutputStream("docs/website/example/revenues-scovo.n3"), "N3");
		m.write(new FileOutputStream("docs/website/example/revenues-scovo.xml"), "RDF/XML");
		
		map = MappingParser.parse("mappings/hierarchy-example.trig");
		m = mat.generateModel(map);
		m.write(new FileOutputStream("docs/website/example/hierarchy-example.n3"), "N3");
		m.write(new FileOutputStream("docs/website/example/hierarchy-example.xml"), "RDF/XML");
	}
}

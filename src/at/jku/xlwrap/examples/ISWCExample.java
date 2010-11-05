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

import at.jku.xlwrap.common.XLWrapException;
import at.jku.xlwrap.dataset.XLWrapDatasetAssembler;
import at.jku.xlwrap.exec.XLWrapMaterializer;
import at.jku.xlwrap.map.MappingParser;
import at.jku.xlwrap.map.XLWrapMapping;

import com.hp.hpl.jena.rdf.model.Model;

/**
 * @author dorgon
 *
 */
public class ISWCExample {
	
	public static void main(String[] args) throws XLWrapException {
		
		XLWrapMapping map = MappingParser.parse("mappings/iswc09-example.trig");
		
		XLWrapMaterializer mat = new XLWrapMaterializer();
		Model m = mat.generateModel(map);
		m.setNsPrefix("ex", "http://example.org/");
		m.write(System.out, "N3");
	}
	
}

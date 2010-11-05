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

import java.io.StringReader;

import at.jku.xlwrap.dataset.XLWrapDataset;
import at.jku.xlwrap.vocab.XLWrap;

import com.hp.hpl.jena.assembler.Assembler;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.query.ResultSetFormatter;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Resource;

/**
 * @author dorgon
 *
 */
public class AssembledExample {

	public static void main(String[] args) {
		// example assembler configuration
		String assConf = 
			"@prefix ja:	<http://jena.hpl.hp.com/2005/11/Assembler#> .\n" +
			"@prefix xl:	<http://purl.org/NET/xlwrap#> .\n" +
			"@prefix :		<http://example.com/> .\n" +
			":dataset a xl:XLWrapDataset ; \n" +
			"	xl:cacheDir <file:data> ; \n" +
			"	xl:watchDir <file:mappings> . \n";
		
		Model conf = ModelFactory.createDefaultModel();
		conf.read(new StringReader(assConf), null, "N3");
		conf.read(XLWrap.getURI()); // load vocabulary (xl:XLWrapDataset rdfs:subClassOf ja:Object, etc.)
		Resource ass = conf.getResource("http://example.com/dataset");
		
		// assemble dataset
		XLWrapDataset dataset = (XLWrapDataset) Assembler.general.open(ass);
		
		QueryExecution qe = QueryExecutionFactory.create("SELECT DISTINCT ?t WHERE { ?s a ?t }", dataset);
		ResultSet r = qe.execSelect();
		ResultSetFormatter.out(r);
		
		dataset.close();
	}
	
}

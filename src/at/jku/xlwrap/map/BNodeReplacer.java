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
package at.jku.xlwrap.map;

import java.util.Hashtable;

import com.hp.hpl.jena.rdf.model.AnonId;

/** 
 * bnode replacer: used for replacing bnodes with new ones
 * 
 * @author dorgon
 *
 */
public class BNodeReplacer {
	private final Hashtable<AnonId, AnonId> old2new = new Hashtable<AnonId, AnonId>();
	
	public AnonId getNew(AnonId old) {
		if (old2new.keySet().contains(old))
			return old2new.get(old);
		else {
			// new entry
			AnonId newId = AnonId.create();
			old2new.put(old, newId);
			return newId;
		}
	}
}
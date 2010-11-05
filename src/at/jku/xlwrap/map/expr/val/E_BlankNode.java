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
package at.jku.xlwrap.map.expr.val;

import at.jku.xlwrap.map.expr.XLExpr;

import com.hp.hpl.jena.rdf.model.AnonId;

/**
 * @author dorgon
 *
 */
public class E_BlankNode extends XLExprValue<AnonId> {

	/**
	 * default constructor, creates a new anonymous id
	 */
	public E_BlankNode() {
		super(AnonId.create());
	}
	
	/**
	 * constructor
	 */
	public E_BlankNode(AnonId id) {
		super(id);
	}
	
	@Override
	public XLExpr copy() {
		return new E_BlankNode(AnonId.create(value.getLabelString()));
	}
	
}

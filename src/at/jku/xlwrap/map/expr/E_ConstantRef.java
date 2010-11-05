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
package at.jku.xlwrap.map.expr;


import at.jku.xlwrap.common.XLWrapException;
import at.jku.xlwrap.exec.ExecutionContext;
import at.jku.xlwrap.map.expr.parser.ParseException;
import at.jku.xlwrap.map.expr.parser.XLExpression;
import at.jku.xlwrap.map.expr.val.E_BlankNode;
import at.jku.xlwrap.map.expr.val.E_String;
import at.jku.xlwrap.map.expr.val.E_URI;
import at.jku.xlwrap.map.expr.val.XLExprValue;
import at.jku.xlwrap.spreadsheet.XLWrapEOFException;
import at.jku.xlwrap.vocab.XLWrap;

import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;


/**
 * @author dorgon
 *
 */
public class E_ConstantRef extends XLExpr0 {
//	private static final Logger log = LoggerFactory.getLogger(E_ConstantRef.class);
	private String constantName;
	
	/**
	 * @param constantName
	 */
	public E_ConstantRef(String constantName) {
		this.constantName = constantName;
	}
	
	/**
	 * @return the range
	 */
	public String getConstantName() {
		return constantName;
	}
	
	/**
	 * may return null
	 */
	@Override
	public XLExprValue<?> eval(ExecutionContext context) throws XLWrapException, XLWrapEOFException {
		RDFNode constant = context.getActiveTemplate().getConstantDef(constantName);
		if (constant == null) {
			throw new XLWrapException("Undefined constant: $" + constantName);
		}
		if (constant.isURIResource()) {
			return new E_URI(constant.as(Resource.class).getURI());
		}
		if (constant.isAnon()) {
			return new E_BlankNode();
		}
		Literal l = constant.as(Literal.class);
		if (!XLWrap.Expr.getURI().equals(l.getDatatypeURI())) {
			// TODO we lose datatype and language tag here
			return new E_String(l.getLexicalForm());
		}
		try {
			return XLExpression.parse(l.getLexicalForm()).eval(context);
		} catch (ParseException ex) {
			throw new XLWrapException(
					"Syntax error in expression '" + l.getLexicalForm() + "'", ex);
		}
	}

	@Override
	public XLExpr copy() {
		return new E_ConstantRef(constantName);
	}

	@Override
	public String toString() {
		return "$" + constantName;
	}
}

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

import at.jku.xlwrap.map.expr.XLExpr;
import at.jku.xlwrap.map.expr.parser.ParseException;
import at.jku.xlwrap.map.expr.parser.XLExpression;
import at.jku.xlwrap.vocab.XLWrap;

import com.hp.hpl.jena.datatypes.BaseDatatype;
import com.hp.hpl.jena.datatypes.DatatypeFormatException;
import com.hp.hpl.jena.datatypes.RDFDatatype;
import com.hp.hpl.jena.datatypes.TypeMapper;
import com.hp.hpl.jena.graph.impl.LiteralLabel;

/**
 * @author dorgon
 *
 */
public class XLExprDatatype extends BaseDatatype {
	public static final String URI = XLWrap.Expr.getURI();
	public static final RDFDatatype instance = new XLExprDatatype();

	/**
	 * register
	 */
	public static void register() {
		TypeMapper.getInstance().registerDatatype(XLExprDatatype.instance);
	}
	
	/* (non-Javadoc)
	 * @see com.hp.hpl.jena.datatypes.BaseDatatype#isValid(java.lang.String)
	 */
	@Override
	public boolean isValid(String lexicalForm) {
		return true; // always true, if not valid we will get the parser exception anyway...
	}
	
	/**
	 * private constructor
	 */
	private XLExprDatatype() {
		super(URI);
	}
	
	/* (non-Javadoc)
	 * @see com.hp.hpl.jena.datatypes.BaseDatatype#unparse(java.lang.Object)
	 */
	@Override
	public String unparse(Object value) {
		XLExpr expr = (XLExpr) value;
		return expr.toString();
	}
	
	/* (non-Javadoc)
	 * @see com.hp.hpl.jena.datatypes.BaseDatatype#parse(java.lang.String)
	 */
	@Override
	public Object parse(String lexicalForm) throws DatatypeFormatException {
		try {
			return XLExpression.parse(lexicalForm);
		} catch (ParseException e) {
			throw new DatatypeFormatException(lexicalForm, instance, e.getMessage());
		}
	}
	
	/* (non-Javadoc)
	 * @see com.hp.hpl.jena.datatypes.BaseDatatype#isEqual(com.hp.hpl.jena.graph.impl.LiteralLabel, com.hp.hpl.jena.graph.impl.LiteralLabel)
	 */
	@Override
	public boolean isEqual(LiteralLabel value1, LiteralLabel value2) {
		return value1.getDatatype() == value2.getDatatype() &&
			unparse(value1).equals(unparse(value2));
	}

}

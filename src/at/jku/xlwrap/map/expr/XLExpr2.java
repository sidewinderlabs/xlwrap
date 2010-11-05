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

/**
 * @author dorgon
 *
 */
public abstract class XLExpr2 extends XLExprBase {
	protected XLExpr arg1, arg2;

	/**
	 * constructor
	 */
	public XLExpr2(XLExpr arg1, XLExpr arg2) {
		this.arg1 = arg1;
		this.arg2 = arg2;
	}
	
	/**
	 * @param e
	 */
	public void setArg1(XLExpr e) {
		this.arg1 = e;
	}
	
	/**
	 * @param e
	 */
	public void setArg2(XLExpr e) {
		this.arg2 = e;
	}
	
	/**
	 * @return the e1
	 */
	public XLExpr getArg1() {
		return arg1;
	}
	
	/**
	 * @return the e2
	 */
	public XLExpr getArg2() {
		return arg2;
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public abstract String toString();
}

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
public abstract class XLExpr1 extends XLExprBase {
	protected XLExpr arg;
	
	/**
	 * constructor
	 */
	public XLExpr1(XLExpr arg) {
		this.arg = arg;
	}

	/**
	 * @param arg the arg to set
	 */
	public void setArg(XLExpr arg) {
		this.arg = arg;
	}
	
	/**
	 * @return the arg
	 */
	public XLExpr getArg() {
		return arg;
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public abstract String toString();
}

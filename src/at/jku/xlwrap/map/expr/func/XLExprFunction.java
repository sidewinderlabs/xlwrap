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
package at.jku.xlwrap.map.expr.func;

import java.util.ArrayList;
import java.util.List;

import at.jku.xlwrap.map.expr.XLExpr;
import at.jku.xlwrap.map.expr.XLExprBase;

/**
 * @author dorgon
 *
 */
public abstract class XLExprFunction extends XLExprBase {
	protected List<XLExpr> args = new ArrayList<XLExpr>();

	/**
	 * @param args the args to set
	 */
	public void setArgs(List<XLExpr> args) {
		this.args = args;
	}
	
	/**
	 * add an argument
	 * @param arg
	 */
	public void addArg(XLExpr arg) {
		this.args.add(arg);
	}
	
	/**
	 * @param n
	 * @param arg
	 */
	public void setArg(int n, XLExpr arg) {
		this.args.set(n, arg);
	}
	
	/**
	 * @return the args
	 */
	public List<XLExpr> getArgs() {
		return args;
	}
	
	/**
	 * @param i
	 * @return
	 */
	public XLExpr getArg(int i) {
		return args.get(i);
	}
	
	@Override
	public final XLExpr copy() {
		XLExprFunction copy = FunctionRegistry.createInstance(this.getClass());
		for (XLExpr arg : args)
			copy.addArg(arg.copy());
		return copy;
	}
	
	/**
	 * @param i
	 * @return
	 */
	protected XLExpr copyArg(int idx) {
		if (idx > args.size())
			return null;
		else {
			XLExpr arg = args.get(idx);
			return (arg == null) ? null : arg.copy();
		}
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(FunctionRegistry.getFunctionName(getClass())).append("(");
		int n = args.size();
		for (int i = 0; i < n; i++) {
			sb.append(args.get(i));
			if (i < n-1) sb.append(", ");
		}
		sb.append(")");
		return sb.toString();
	}

}
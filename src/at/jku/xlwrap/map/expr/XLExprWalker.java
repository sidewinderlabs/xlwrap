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
import at.jku.xlwrap.map.expr.func.XLExprFunction;

/**
 * @author dorgon
 *
 */
public class XLExprWalker {

	public static void walkPostOrder(XLExpr expr, XLExprVisitor visitor) throws XLWrapException {
		if (expr instanceof XLExpr0) {
			visitor.visiting0(expr);
			
		} else if (expr instanceof XLExpr1) {
			walkPostOrder(((XLExpr1) expr).getArg(), visitor);
			visitor.visiting1(expr);
			
		} else if (expr instanceof XLExpr2) {
			walkPostOrder(((XLExpr2) expr).getArg1(), visitor);
			walkPostOrder(((XLExpr2) expr).getArg2(), visitor);
			visitor.visiting2(expr);
			
		} else if (expr instanceof XLExprFunction) {
			for (XLExpr arg : ((XLExprFunction) expr).getArgs())
				walkPostOrder(arg, visitor);
			visitor.visitingFunction(expr);
			
		} else if (expr == null)
			throw new XLWrapException("Null expression received.");
		else
			throw new XLWrapException("Invalid expression type: " + expr.getClass().getName());
	}
}

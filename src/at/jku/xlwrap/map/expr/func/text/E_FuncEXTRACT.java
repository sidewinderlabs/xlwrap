/**
 * Copyright 2011 Moritz Hoffmann, antiguru@gmail.com, Switzerland; Netlabs.org
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
package at.jku.xlwrap.map.expr.func.text;

import java.util.regex.Pattern;

import at.jku.xlwrap.common.XLWrapException;
import at.jku.xlwrap.exec.ExecutionContext;
import at.jku.xlwrap.map.expr.TypeCast;
import at.jku.xlwrap.map.expr.XLExpr;
import at.jku.xlwrap.map.expr.func.XLExprFunction;
import at.jku.xlwrap.map.expr.val.E_String;
import at.jku.xlwrap.map.expr.val.XLExprValue;
import at.jku.xlwrap.spreadsheet.XLWrapEOFException;

/**
 * Extract a part of a value. Need to specify the sorurce, a regex and the n-th
 * group.
 * 
 * @author Moritz Hoffmann
 * 
 */
public class E_FuncEXTRACT extends XLExprFunction {
    private Pattern pattern;

    /**
     * default constructor
     */
    public E_FuncEXTRACT() {
    }

    public E_FuncEXTRACT(XLExpr string, XLExpr regex, XLExpr group) {
        args.add(string);
        args.add(regex);
        args.add(group);
    }

    public E_FuncEXTRACT(XLExpr string, XLExpr regex) {
        args.add(string);
        args.add(regex);
    }

    /*
     * (non-Javadoc)
     * 
     * @see at.langegger.xlwrap.map.expr.XLExpr#eval(at.langegger.xlwrap.exec.
     * ExecutionContext)
     */
    @Override
    public XLExprValue<?> eval(ExecutionContext context)
            throws XLWrapException, XLWrapEOFException {
        String string = TypeCast.toString(args.get(0).eval(context));

        if (pattern == null) {
            String regex = TypeCast.toString(getArg(1).eval(context));
            pattern = Pattern.compile(regex);
        }

        E_String result;
        if (args.size() > 2) {
            int group = TypeCast.toInteger(getArg(2).eval(context), false);
            result = new E_String(pattern.matcher(string).group(group));
        } else {
            result = new E_String(pattern.matcher(string).group());
        }
        return result;
    }
}

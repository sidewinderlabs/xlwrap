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

import java.util.Arrays;

import at.jku.xlwrap.common.XLWrapException;
import at.jku.xlwrap.exec.ExecutionContext;
import at.jku.xlwrap.map.expr.TypeCast;
import at.jku.xlwrap.map.expr.XLExpr;
import at.jku.xlwrap.map.expr.func.XLExprFunction;
import at.jku.xlwrap.map.expr.val.E_List;
import at.jku.xlwrap.map.expr.val.E_String;
import at.jku.xlwrap.map.expr.val.XLExprValue;
import at.jku.xlwrap.spreadsheet.XLWrapEOFException;

/**
 * Split an input text into a bag of values. Supply text to split and regex.
 * 
 * @author Moritz Hoffmann
 * 
 */
public class E_FuncSPLIT extends XLExprFunction {

    /**
     * default constructor
     */
    public E_FuncSPLIT() {
    }

    public E_FuncSPLIT(XLExpr string, XLExpr separator) {
        args.add(string);
        args.add(separator);
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

        String[] parts = string.split(TypeCast
            .toString(getArg(1).eval(context)));
        // int index = TypeCast.toInteger(getArg(2).eval(context), false);
        E_String[] strings = new E_String[parts.length];
        for (int i = 0; i < parts.length; i = i + 1) {
            strings[i] = new E_String(parts[i]);
        }
        return new E_List(Arrays.asList(strings));
        // try {
        // return new E_String(parts[index]);
        // } catch (ArrayIndexOutOfBoundsException e) {
        // throw new XLWrapException(Integer.toString(index), e);
        // }
    }
}

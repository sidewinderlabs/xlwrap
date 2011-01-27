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
package at.jku.xlwrap.map.expr.val;

import java.util.ArrayList;
import java.util.List;

/**
 * A list of expressions.
 * @author Moritz Hoffmann
 * 
 */
public class E_List extends XLExprValue<List<? extends XLExprValue<?>>> {

    /**
     * @param value
     */
    public E_List(List<? extends XLExprValue<?>> value) {
        super(value);
    }

    /*
     * (non-Javadoc)
     * 
     * @see at.jku.xlwrap.common.Copy#copy()
     */
    @Override
    public XLExprValue<List<? extends XLExprValue<?>>> copy() {
        // TODO Auto-generated method stub
        return new E_List(new ArrayList<XLExprValue<?>>(getValue()));
    }

}

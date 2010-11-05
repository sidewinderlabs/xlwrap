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
package at.jku.xlwrap.map.expr.func.text;

import at.jku.xlwrap.common.XLWrapException;
import at.jku.xlwrap.exec.ExecutionContext;
import at.jku.xlwrap.map.expr.TypeCast;
import at.jku.xlwrap.map.expr.XLExpr;
import at.jku.xlwrap.map.expr.func.XLExprFunction;
import at.jku.xlwrap.map.expr.val.E_String;
import at.jku.xlwrap.map.expr.val.XLExprValue;
import at.jku.xlwrap.spreadsheet.XLWrapEOFException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * @author Knud Möller (knud.moeller@deri.org)
 */
public class E_FuncNAMECASE extends XLExprFunction {
	private static final Logger log = LoggerFactory.getLogger("NAMECASE");
	private static String[] name_particles = { "van", "von", "de", "der", "den", "zu"};

	/**
	 * default constructor 
	 */
	public E_FuncNAMECASE() {
	}

	public E_FuncNAMECASE(XLExpr string) {
		args.add(string);
	}
	
	/* (non-Javadoc)
	 * @see at.langegger.xlwrap.map.expr.XLExpr#eval(at.langegger.xlwrap.exec.ExecutionContext)
	 */
	@Override
	public XLExprValue<?> eval(ExecutionContext context) throws XLWrapException, XLWrapEOFException {
		XLExprValue<?> v1 = args.get(0).eval(context);
		if (v1 == null)
			return null;
		else {
			// first, split input string into words
			String[] words = TypeCast.toString(v1).split(" ");
			ArrayList new_words = new ArrayList();
			
			// iterate words
			for (String word : words) {
				if (!Arrays.asList(this.name_particles).contains(word.toLowerCase()) && (isUpper(word) || isLower(word))) {
					String new_word = capitalize(word);
					new_words.add(new_word);
					log.info("Changed: '" + word + "' to '" + new_word + "'");
				} else
					new_words.add(word);
			}
			
			return new E_String(join(new_words.toArray(), ' '));
		}
	}
	
	private boolean isUpper(String aString) {
		boolean isUpper = true;
		for (char c : aString.toCharArray()) {
			if (Character.isLowerCase(c)) {
				isUpper = false;
				break;
			}
		}
		return isUpper;
	}

	private boolean isLower(String aString) {
		boolean isLower = true;
		for (char c : aString.toCharArray()) {
			if (Character.isUpperCase(c)) {
				isLower = false;
				break;
			}
		}
		return isLower;
	}

	private String capitalize(String str) {
	    int strLen;
	    if (str == null || (strLen = str.length()) == 0) {
	        return str;
	    }
	    return new StringBuffer (strLen)
	        .append(Character.toTitleCase(str.charAt(0)))
	        .append(str.substring(1))
	        .toString();
	}
	
	private String join(Object[] array, char separator) {
	    if (array == null) {
	        return null;
	    }
	    int arraySize = array.length;
	    int bufSize = (arraySize == 0 ? 0 : ((array[0] == null ? 16 : array[0].toString().length()) + 1) * arraySize);
	    StringBuffer   buf = new StringBuffer  (bufSize);

	    for (int i = 0; i < arraySize; i++) {
	        if (i > 0) {
	            buf.append(separator);
	        }
	        if (array[i] != null) {
	            buf.append(array[i]);
	        }
	    }
	    return buf.toString();
	}
	
}

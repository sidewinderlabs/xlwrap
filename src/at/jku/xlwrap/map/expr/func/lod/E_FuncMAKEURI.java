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
package at.jku.xlwrap.map.expr.func.lod;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import at.jku.xlwrap.common.XLWrapException;
import at.jku.xlwrap.exec.ExecutionContext;
import at.jku.xlwrap.map.expr.TypeCast;
import at.jku.xlwrap.map.expr.func.XLExprFunction;
import at.jku.xlwrap.map.expr.val.E_String;
import at.jku.xlwrap.map.expr.val.XLExprValue;
import at.jku.xlwrap.spreadsheet.XLWrapEOFException;

/**
 * @author dorgon
 *
 */
public class E_FuncMAKEURI extends XLExprFunction {
	private static final Logger log = LoggerFactory.getLogger(E_FuncMAKEURI.class);
	
	/**
	 * 
	 */
	public E_FuncMAKEURI() {
	}
	
	@Override
	public XLExprValue<?> eval(ExecutionContext context) throws XLWrapException, XLWrapEOFException {
		Integer p = context.getPort();
		String hostname = context.getHostname();
		String prefix = context.getPubbyPathPrefix();
		if (p == null || hostname == null || prefix == null) {
			log.warn("Cannot evaluate function " + this + ", not running in server mode.");
			return null;
		}
		
		StringBuilder sb = new StringBuilder();
		sb.append("http://").append(hostname);
		if (p != 80)
			sb.append(":").append(p);
		
		// if one argument, URI-encode v1
		if (args.size() == 1) {
			XLExprValue<?> v1 = args.get(0).eval(context);
			if (v1 == null)
				return null;

			try {
				sb.append("/").append(prefix).append(URLEncoder.encode(TypeCast.toString(v1), "UTF-8"));
			} catch (UnsupportedEncodingException e) {
				throw new XLWrapException(e.getMessage(), e);
			}

		// if two arguments, plain append v1, and URI-encode v2
		} else if (args.size() == 2) {			
			XLExprValue<?> v1 = args.get(0).eval(context);
			if (v1 == null)
				return null;
			XLExprValue<?> v2 = args.get(1).eval(context);
			if (v2 == null)
				return null;
			
			String plain = TypeCast.toString(v1);
			sb.append("/").append(prefix);
			if (plain.length() > 0)
				sb.append(plain).append("/");
			
			try {
				sb.append(URLEncoder.encode(TypeCast.toString(v2), "UTF-8"));
			} catch (UnsupportedEncodingException e) {
				throw new XLWrapException(e.getMessage(), e);
			}
			
		}
				
		return new E_String(sb.toString());
	}

}

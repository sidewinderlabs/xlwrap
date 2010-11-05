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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.Hashtable;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import at.jku.xlwrap.common.XLWrapException;
import at.jku.xlwrap.exec.ExecutionContext;
import at.jku.xlwrap.map.expr.TypeCast;
import at.jku.xlwrap.map.expr.XLExpr;
import at.jku.xlwrap.map.expr.val.E_String;
import at.jku.xlwrap.map.expr.val.XLExprValue;
import at.jku.xlwrap.spreadsheet.XLWrapEOFException;

/**
 * @author dorgon
 *
 */
public class E_FuncDBP_LINK extends DBPedia_Function {
	private static final Logger log = LoggerFactory.getLogger(E_FuncDBP_LINK.class);
	
	private static Map<String, E_String> cache = new Hashtable<String, E_String>();
	
	public E_FuncDBP_LINK() {
	}
	
	public E_FuncDBP_LINK(XLExpr arg) {
		addArg(arg);
	}
	
	@Override
	public XLExprValue<?> eval(ExecutionContext context) throws XLWrapException, XLWrapEOFException {
		XLExprValue<?> v1 = args.get(0).eval(context);
		if (v1 == null)
			return null;
		
		String s = TypeCast.toString(v1);
		E_String cached = cache.get(s);
		E_String uri = null;
		
		if (cached == null) {
			HttpURLConnection c = null;
			BufferedReader io = null;
			try {
				URL url = new URL("http://swse.deri.org/list?keyword=" + URLEncoder.encode(s, "UTF-8"));
				c = (HttpURLConnection) url.openConnection();
				c.setConnectTimeout(5000); // 5 seconds
	            c.setReadTimeout(30000); // 30 seconds
	            c.setDoInput(true);
	            c.connect();
	            int code = c.getResponseCode();
	
	            // 1xx: Informational 
	            // 2xx: Success 
	            // 3xx: Redirection 
	            // 4xx: Client Error 
	            // 5xx: Server Error 
	            
	            if (code > 300)
	                throw new XLWrapException(URLDecoder.decode(c.getResponseMessage(), "UTF-8"));
	  
	            // Request suceeded
	            io = new BufferedReader(new InputStreamReader(c.getInputStream()));
	            String line;
	            while ((line = io.readLine()) != null) {
	            	Matcher m = Pattern.compile("\\<\\/info\\>\\<list\\>\\<entry rdf\\:about\\=\\\"([^\\\"]*)\\\"").matcher(line);
	            	if (m.find() && m.group(1) != null)
	            		uri = new E_String(m.group(1));
	            }
	     
	            // add to cache
	            cache.put(s, uri);
	            log.debug("New DBpedia link: '" + s + "' => <" + uri + ">");
			} catch (Exception e) {
				throw new XLWrapException("Failed to generate DBpedia URI from argument: " + v1, e);
			} finally {
				if (c != null)
					c.disconnect();
				if (io != null)
					try { io.close(); } catch (IOException e) {}
			}
		}
		
		return uri;
	}

}

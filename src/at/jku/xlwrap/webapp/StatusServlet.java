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
package at.jku.xlwrap.webapp;

import java.io.IOException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Iterator;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.joseki.DatasetDesc;
import org.joseki.Service;
import org.joseki.servlets.MetadataBaseServlet;

import at.jku.xlwrap.common.XLWrapException;
import at.jku.xlwrap.dataset.XLWrapDataset;
import at.jku.xlwrap.engine.XLWrapEngine;

/**
 * @author dorgon
 *
 */
public class StatusServlet extends MetadataBaseServlet {
	
	/* (non-Javadoc)
	 * @see org.joseki.servlets.MetadataBaseServlet#getServletMappingPrefix()
	 */
	@Override
	protected String getServletMappingPrefix() {
		return "/status/";
	}
	
	/* (non-Javadoc)
	 * @see org.joseki.servlets.MetadataBaseServlet#service(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse, org.joseki.Service, org.joseki.DatasetDesc)
	 */
	@Override
	protected void service(HttpServletRequest req, HttpServletResponse res, Service s, DatasetDesc ds) throws IOException, ServletException {	
		ServletOutputStream out = res.getOutputStream();
		out.println("<?xml version=\"1.0\" encoding=\"UTF-8\" ?>\n");
		
		String root = getServletContext().getContextPath();
		XLWrapEngine xl = ((XLWrapDataset) ds.acquireDataset()).getXLWrapEngine();
		SimpleDateFormat df = new SimpleDateFormat();

		String reload = (String) req.getParameter("reload");
		if (reload != null)
			xl.reloadIntoCache(URLDecoder.decode(reload, "UTF-8"));
		
		out.println("<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Transitional//EN\" \"http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd\">\n");
		out.println("<html xmlns=\"http://www.w3.org/1999/xhtml\">\n");
		out.println("<head>\n");
		out.println("<meta http-equiv=\"Content-Type\" content=\"text/html; charset=ISO-8859-1\" />\n");
		out.println("<title>XLWrap Spreadsheet Wrapper</title>\n");
		out.println("<link rel=\"stylesheet\" title=\"default style\" href=\"" + root + "/styles.css\"/>\n");
		out.println("</head>\n");
		out.println("<body>\n");
		out.println("<h1>XLWrap Server</h1>\n");
		out.println("<h2>Status Information</h2>\n");
		out.println("<table class=\"l\">\n");
		out.println("<tr class=\"l\">\n");
		out.println("<th class=\"l\">Server status:</th>\n");
		out.println("<td class=\"l\">" + req.getLocalAddr() + ":" + req.getLocalPort()  + "</td>\n");
		out.println("</tr>\n");
		out.println("<tr class=\"l\">\n");
		out.println("<th class=\"l\">Client status:</th>\n");
		out.println("<td class=\"l\">" + req.getRemoteAddr() + "<br />" + req.getHeaders("User-agent").nextElement().toString() + "</td>\n");
		out.println("</tr>\n");
		out.println("<tr class=\"l\">\n");
		out.println("<th class=\"l\">Watch directory:</th>\n");
		out.println("<td class=\"l\">");
		try {
			out.println(xl.getWatchDirectoryAbsolute());
		} catch (IOException e) {
			out.println("invalid: " + xl.getWatchDirectory());
		}
		out.println("</td>\n");
		out.println("</tr>\n");
		out.println("<tr class=\"l\">\n");
		out.println("<th class=\"l\">Wrapped spreadsheets in cache:</th>\n");
		out.println("<td class=\"l\">\n");
		
		Iterator<String> cached = xl.listCachedNames();
		while (cached.hasNext()) {
			String file = cached.next();
			String date = "n/a";
			try {
				Calendar c = xl.getTimestamp(file);
				date = df.format(c.getTime());
			} catch (XLWrapException e) {}
			
			out.println("<a href=\"?reload=" + URLEncoder.encode(file, "UTF-8") + "\">reload</a> " + file + " " + xl.getReferredFiles(file) + " cached at: " + date + "<br />\n");
		}
		out.println("</td>\n");
		out.println("</tr>\n");
		out.println("<tr class=\"l\">\n");
		out.println("<th class=\"l\">SPARQL endpoint:</th>\n");
		out.println("<td class=\"l\"><a href=\"" + root + "/sparql\">" + root + "/sparql</a></td>\n");
		out.println("</tr>\n");
		out.println("<tr class=\"l\">\n");
		out.println("<th class=\"l\">Further links:</th>\n");
		out.println("<td class=\"l\">\n");
		out.println("<a href=\"" + root + "/snorql\">Snorql interface</a> (Snorql is part of <a href=\"http://www4.wiwiss.fu-berlin.de/bizer/d2r-server/\">D2R-Server</a>)<br />\n");
		out.println("<a href=\"" + root + "/logs\">Server logs</a><br />\n");
		out.println("</td>\n");
		out.println("</tr>\n");
		out.println("</table>\n");

		out.println("<p class=\"footer\">\n");
		out.println("&copy; Andreas Langegger, Institute for Application-Oriented Knowledge Processing, Johannes Kepler University Linz, Austria</p>\n");
		out.println("</body>\n");
		out.println("</html>\n");
	}
}

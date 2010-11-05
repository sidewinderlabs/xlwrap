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
package at.jku.xlwrap.common;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;
import org.xml.sax.SAXException;

/**
 * @author Richard Cyganiak (richard@cyganiak.de)
 */
public class XMLConfigFile {
	private final static Map<String, XMLConfigFile> configFiles =
		new HashMap<String, XMLConfigFile>();
	
	public static String getXPath(String fileName, String xPath) 
	throws XLWrapException{
		if (!configFiles.containsKey(fileName)) {
			configFiles.put(fileName, new XMLConfigFile(fileName));
		}
		return configFiles.get(fileName).evaluateXPath(xPath);
	}
	
	private final Document dom;
	
	public XMLConfigFile(String fileName) throws XLWrapException {
		if (!fileName.contains(":")) {
			fileName = "file:" + fileName;
		}
		try {
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			factory.setNamespaceAware(true);
			dom = factory.newDocumentBuilder().parse(fileName);
		} catch (IOException ex) {
			throw new XLWrapException("Error reading XML file '" + fileName + "'", ex);
		} catch (SAXException ex) {
			throw new XLWrapException("Error reading XML file '" + fileName + "'", ex);
		} catch (ParserConfigurationException ex) {
			throw new XLWrapException("Error reading XML file '" + fileName + "'", ex);
		}
	}
	
	public String evaluateXPath(String xPath) throws XLWrapException {
		XPathFactory factory = XPathFactory.newInstance();
		XPath xpath = factory.newXPath();	
		try {
			return xpath.compile(xPath).evaluate(dom);
		} catch (XPathExpressionException ex) {
			throw new XLWrapException("Error in XPath expression '" + xPath + "'", ex);
		}
	}
}

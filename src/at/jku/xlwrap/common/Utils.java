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

import java.io.StringReader;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Calendar;
import java.util.Hashtable;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import at.jku.xlwrap.exec.ExecutionContext;
import at.jku.xlwrap.map.BNodeReplacer;
import at.jku.xlwrap.map.XLExprDatatype;
import at.jku.xlwrap.map.expr.E_ConstantRef;
import at.jku.xlwrap.map.expr.XLExpr;
import at.jku.xlwrap.map.expr.val.E_BlankNode;
import at.jku.xlwrap.map.expr.val.E_Boolean;
import at.jku.xlwrap.map.expr.val.E_Date;
import at.jku.xlwrap.map.expr.val.E_Double;
import at.jku.xlwrap.map.expr.val.E_List;
import at.jku.xlwrap.map.expr.val.E_Long;
import at.jku.xlwrap.map.expr.val.E_String;
import at.jku.xlwrap.map.expr.val.E_URI;
import at.jku.xlwrap.map.expr.val.XLExprValue;
import at.jku.xlwrap.map.range.AnyRange;
import at.jku.xlwrap.map.range.BoxRange;
import at.jku.xlwrap.map.range.CellRange;
import at.jku.xlwrap.map.range.FullSheetRange;
import at.jku.xlwrap.map.range.MultiRange;
import at.jku.xlwrap.map.range.NullRange;
import at.jku.xlwrap.map.range.Range;
import at.jku.xlwrap.spreadsheet.Cell;
import at.jku.xlwrap.spreadsheet.TypeAnnotation;
import at.jku.xlwrap.spreadsheet.XLWrapEOFException;

import com.hp.hpl.jena.datatypes.DatatypeFormatException;
import com.hp.hpl.jena.datatypes.RDFDatatype;
import com.hp.hpl.jena.datatypes.xsd.XSDDatatype;
import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.rdf.model.AnonId;
import com.hp.hpl.jena.rdf.model.Bag;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import com.hp.hpl.jena.vocabulary.XSD;

/**
 * @author dorgon
 *
 */
public class Utils {
	
	private static final String REGEX_MATCH_RANGE = 
		"^" +								// start of line
		"(?:" +	 							// optional file with suffix "#"
			"(?:" +							// single-, double-, or unquoted
				"(?:'([^']+)')" +
				"|(?:\"([^\"]+)\")" +
				"|([^ \\#\"'\\*\\:]+)" +
			")" +
			"\\#" +							// "#" suffix
		")?" +
		"(?:" +								// optional sheet1
			"\\$?" +						// optional $-prefix
			"(?:" +							// single-, double-, or unquoted sheet1
				"(?:'([^'\\.]+)')" + 
				"|(?:\"([^\"\\.]+)\")" +
				"|([^ \\.\"'\\*\\:\\#]+)" +
				"|(?:\\#(\\d)+)" +			// by number, e.g. "#4"
				"|(\\*)" +					// special symbol for "any" sheet
			")" +
			"\\." +							// "." suffix
		")?" +
		"(?:" +								// either A5[:[[$]Sheet2.]B9] or "*" for any range
			"(?:" +
				"([A-Za-z]+)([\\d]+)" +			// col1/row1
				"(?:" +							// optional target range
					"\\:" +						// ":" separator
					"(?:" +						// optional sheet2
						"\\$?" +				// optional $-prefix
						"(?:" +					// single-, double-, or unquoted sheet2
							"(?:'([^'\\.]+)')" +
							"|(?:\"([^\"\\.]+)\")" +
							"|([^ \\.\"'\\*\\:\\#]+)" +
							"|(?:\\#(\\d)+)" +	// by number
						")" +
						"\\." +					// "." suffix
					")?" +
					"([A-Za-z]+)([\\d]+)" +		// col2/row2
				")?" +
			")" +
			"|(\\*)" +						// or "*" for any range
		")$";
	
	private static final int FILENAME_SINGLEQUOTED = 1;
	private static final int FILENAME_UNQUOTED = 2;
	private static final int FILENAME_DOUBLEQUOTED = 3;
	private static final int SHEET1_SINGLEQUOTED = 4;
	private static final int SHEET1_DOUBLEQUOTED = 5;
	private static final int SHEET1_UNQUOTED = 6;
	private static final int SHEET1_NUMERIC = 7;
	private static final int ANY_SHEET = 8;
	private static final int COL1 = 9; 
	private static final int ROW1 = 10;
	private static final int SHEET2_SINGLEQUOTED = 11;
	private static final int SHEET2_DOUBLEQUOTED = 12;
	private static final int SHEET2_UNQUOTED = 13;
	private static final int SHEET2_NUMERIC = 14;
	private static final int COL2 = 15; 
	private static final int ROW2 = 16;
	private static final int ANY_RANGE = 17;

	/**
	 * Multibox range syntax:   BoxRange (';' BoxRange )*
	 * for single BoxRange see XLExpressionParser and constant REGEX_MATCH_RANGE
	 * 
	 * - if "any" sheet is specified using the "*" wildcard, also "any" range must be specified, thus: *.A3 is invalid, *.* and SheetX.* are valid
	 * - if external file name given, first sheet name must be present also
	 * - if sheet1 is given but no sheet2 in target range, sheet2 = sheet1 (same with sheetNum1/2)
	 * 
	 * Example: file://foo.xls#$Sheet1.A1:Sheet1.B30 or *.* for any range in a workbook
	 * 
	 * @param range string according to syntax above
	 * @return a range object
	 * @throws XLWrapException
	 */
	public static Range parseRange(String multiRange) throws XLWrapException {
		if (multiRange == null || multiRange.length() == 0)
			return NullRange.INSTANCE;
		
		StringTokenizer tok = new StringTokenizer(multiRange, ";");
		MultiRange mbr = new MultiRange();
		
		// split at ";" to parse multibox ranges
		String range;
		while (tok.hasMoreTokens()) {
			range = tok.nextToken().trim();
			Matcher m  = Pattern.compile(REGEX_MATCH_RANGE).matcher(range);
			int col1, row1, col2, row2;
			String fileName;
			String sheet1, sheet2;
			Integer sheet1Num = null;
			Integer sheet2Num = null;
			
			if (m.find()) {
				fileName = m.group(FILENAME_DOUBLEQUOTED);
				if (fileName == null) fileName = m.group(FILENAME_SINGLEQUOTED);
				if (fileName == null) fileName = m.group(FILENAME_UNQUOTED);
				
				sheet1 = m.group(SHEET1_DOUBLEQUOTED);
				if (sheet1 == null) sheet1 = m.group(SHEET1_SINGLEQUOTED);
				if (sheet1 == null) sheet1 = m.group(SHEET1_UNQUOTED);
				
				if (sheet1 == null && m.group(SHEET1_NUMERIC) != null)
					sheet1Num = Integer.parseInt(m.group(SHEET1_NUMERIC))-1; // zero-based internally
					
				if (fileName != null && sheet1 == null && sheet1Num == null)
					throw new XLWrapException("Invalid range: " + range + ", when specifying an external file, the sheet must be specified!");
				
				// any sheet?
				if (m.group(ANY_SHEET) != null) {
					if (m.group(ANY_RANGE) != null) { // "*.*"
						mbr.addRange(AnyRange.INSTANCE);
						continue;
					} else // *.A3 not allowed
						throw new XLWrapException("Selecting a specific range for 'any' sheet is not allowed: " + range + " is invalid, either use *.* or [sheet].*");

				} else {
					if (m.group(ANY_RANGE) != null) {
						if (sheet1 != null && sheet1.length() > 0)
							mbr.addRange(new FullSheetRange(fileName, sheet1));
						else
							mbr.addRange(new FullSheetRange(fileName, sheet1Num));
						continue;
					}
					// normal range given...
					
					col1 = alphaToIndex(m.group(COL1));
					row1 = Integer.parseInt(m.group(ROW1))-1;
	
					// target range part given?
					if (m.group(COL2) != null && m.group(ROW2) != null) {
						sheet2 = m.group(SHEET2_DOUBLEQUOTED);
						if (sheet2 == null) sheet2 = m.group(SHEET2_SINGLEQUOTED);
						if (sheet2 == null) sheet2 = m.group(SHEET2_UNQUOTED);
						
						if (sheet2 == null && m.group(SHEET2_NUMERIC) != null)
							sheet2Num = Integer.parseInt(m.group(SHEET2_NUMERIC))-1; // zero-based internally
						
						// check sheets
						if (sheet1Num != null && sheet2Num != null && sheet2Num < sheet1Num) {
							Integer x = sheet2Num;
							sheet2Num = sheet1Num;
							sheet1Num = x;
						}
						
						col2 = alphaToIndex(m.group(COL2));
						row2 = Integer.parseInt(m.group(ROW2))-1;

						// check col2/row2
						if (col2 < col1) {
							int x = col2;
							col2 = col1;
							col1 = x;
						}
						if (row2 < row1) {
							int x = row2;
							row2 = row1;
							row1 = x;
						}
						
						// new box range
						BoxRange box = new BoxRange(col1, row1, col2, row2);
						if (fileName != null && fileName.length() > 0)
							box.setFileName(fileName);
						
						if (sheet1 != null && sheet1.length() > 0)
							box.setSheet1(sheet1);
						else if (sheet1Num != null)
							box.setSheetNumber1(sheet1Num);
						
						if (sheet2 != null && sheet2.length() > 0)
							box.setSheet2(sheet2);
						else if (sheet2Num != null)
							box.setSheetNumber2(sheet2Num);
						else if (sheet1 != null && sheet1.length() > 0)
							box.setSheet2(sheet1); // set sheet2 = sheet1
						else if (sheet1Num != null)
							box.setSheetNumber2(sheet1Num); // set sheetNum2 = sheetNum1
						
						mbr.addRange(box);
						
					// simple cell range like "A5", "Sheet1.A5", or "#2.A5"
					} else {
						if (sheet1 != null && sheet1.length() > 0)
							mbr.addRange(new CellRange(fileName, sheet1, col1, row1));
						else
							mbr.addRange(new CellRange(fileName, sheet1Num, col1, row1)); // zero-based internally
					}
				}
			} else
				throw new XLWrapException("Invalid range: " + range + ".");
		}
		
		if (mbr.size() > 1)
			return mbr; // return constructed multi range
		else if (mbr.size() == 1)
			return mbr.getRangeIterator().next(); // return single range only
		else
			return NullRange.INSTANCE;
	}
	
	/**
	 * @param alpha index
	 * @return zero-based numerical index
	 */
	public static int alphaToIndex(String alpha) {
		char[] letters = alpha.toUpperCase().toCharArray();
		int index = 0;
		for (int i = 0; i < letters.length; i++)
			index += ((letters[letters.length-i-1]) - 64) * (Math.pow(26, i)); // A is 64
		return --index;
	}

	/**
	 * @param zero-based numerical index
	 * @return alpha index
	 */
	public static String indexToAlpha(int index) {
		char[] letters = new char[10]; // 10 is sufficient
		int pos = 9; // fill from right to left
		index++;
		do {
			letters[pos--] = ((char) (((index-1) % 26) + 65)); // A is 65
			index = (index-1)/26;
		} while (index > 0 && pos > 0);
		
		StringBuilder sb = new StringBuilder(10);
		for (int i=pos+1; i<10; i++)
			sb.append(letters[i]);
		return sb.toString();
	}
	
	/**
	 * try to get XLExpr from a node or return null if it is not a valid XLExpr
	 * 
	 * @param node
	 * @return
	 * @throws DatatypeFormatException
	 */
	public static XLExpr getExpression(Node node) throws DatatypeFormatException {
		// URIs from the variable namespace (v:foo etc) are translated to
		// variable references
		if (node.isURI() && node.getURI().startsWith(Constants.VARIABLE_NS)) {
			return new E_ConstantRef(
					node.getURI().substring(Constants.VARIABLE_NS.length()));
		}
		if (!node.isLiteral()) return null;
		RDFDatatype dt = node.getLiteralDatatype();
		if (dt == null || dt.getURI().equals(XSD.xstring.getURI())) {
			return new E_String(node.getLiteralLexicalForm());
		}
		if (dt.equals(XLExprDatatype.instance)) {
			return (XLExpr) node.getLiteralValue();
		}
		return null;
	}

	public static RDFNode getEvaluatedNode(RDFNode node, Model target, ExecutionContext context) throws XLWrapEOFException, XLWrapException {
		XLExpr expr = Utils.getExpression(node.asNode());
		if (expr == null) return node;
		XLExprValue<?> val = expr.eval(context);
		if (val == null) return null;
		return Utils.createNode(target, val);
	}
	
	/**
	 * @param cell
	 * @return XLExprValue wrapping a java object for the cell's value (never null)
	 * @throws XLWrapException
	 */
	public static XLExprValue<?> getXLExprValue(Cell cell) throws XLWrapException {
		if (cell == null)
			return null;
		
		TypeAnnotation type = cell.getType();
		
		if (type == TypeAnnotation.NUMBER) {

			// we use long and double also for integer/short/byte/float... here and cast down
			// later when creating RDF nodes (see RDFNoe createNode() below)
			// reason: calculations may increase the required size of numbers during the process
			double d = cell.getNumber();
			if ((long) d == d)
				return new E_Long((long) d);
			else
				return new E_Double(d);
			
		} else if (type == TypeAnnotation.TEXT) {
			return new E_String(cell.getText());

		} else if (type == TypeAnnotation.NULL) {
			return null;
				
		} else if (type == TypeAnnotation.DATE) {
			Calendar c = Calendar.getInstance();
			c.setTime(cell.getDate());
			return new E_Date(c.getTime());
			
		} else if (type == TypeAnnotation.BOOLEAN) {
			return cell.getBoolean() ? E_Boolean.TRUE : E_Boolean.FALSE;
			
		} else // unknown or other type, get as text label
			return new E_String(cell.getText());
	}
	
	/**
	 * 
	 * @param the target model
	 * @param value the XLExprValue
	 * @return a Jena node
	 */
	public static RDFNode createNode(Model target, XLExprValue<?> ev) {
		if (ev instanceof E_String)
			return target.createLiteral((String) ev.getValue());
		else if (ev instanceof E_Long) {
			long l = ((E_Long) ev).getValue();

			// if expression value was explicitly casted, don't try to cast down numbers
			if (ev.isCasted())
				return target.createTypedLiteral(l);
			else if ((byte) l == l)
				return target.createTypedLiteral((byte) l, XSDDatatype.XSDbyte); // byte actually
			else if ((short) l == l)
				return target.createTypedLiteral((short) l, XSDDatatype.XSDshort); // short actually
			else if ((int) l == l)
				return target.createTypedLiteral((int) l); // integer actually
			else
				return target.createTypedLiteral(l); // leave as long
		
		} else if (ev instanceof E_Double) {
			double d = ((E_Double) ev).getValue();
			
			if (ev.isCasted())
				return target.createTypedLiteral(d);
			
			else if ((float) d == d)
				return target.createTypedLiteral((float) d); // float actually
			else
				return target.createTypedLiteral(d);
			
		} else if (ev instanceof E_URI)
			return target.createResource((String) ev.getValue());
		
		else if (ev instanceof E_BlankNode)
			return target.createResource((AnonId) ev.getValue());
        else if (ev instanceof E_List) {
            Bag list = target.createBag();
            E_List eList = (E_List) ev;
            for (XLExprValue<?> value : eList.getValue()) {
                list.add(createNode(target, value));
            }
            return list;
        }		else
			return target.createTypedLiteral(ev.getValue());
	}
	
	/**
	 * @param graph as a string
	 * @param lang the serialization format (e.g. "N3")
	 * @return a Jena model
	 */
	public static Model createModel(String graph, String lang) {
		Model m = ModelFactory.createDefaultModel();
		m.read(new StringReader(graph), null, lang);
		return m;
	}
	
	/**
	 * copy a model but use distinct bnode labels
	 * 
	 * @param base
	 * @return
	 */
	public static Model copyModel(Model base) {
		BNodeReplacer rep = new BNodeReplacer();
		Model newModel = ModelFactory.createDefaultModel();
		StmtIterator it = base.listStatements();
		Statement st;
		Node s;
		Node p;
		Node o;
		
		while (it.hasNext()) {
			st = it.nextStatement();
			s = st.getSubject().asNode();
			p = st.getPredicate().asNode();
			o = st.getObject().asNode();
			
			if (s.isBlank())
				s = newModel.createResource(rep.getNew(s.getBlankNodeId())).asNode();

			if (p.isBlank())
				p = newModel.createResource(rep.getNew(p.getBlankNodeId())).asNode();
			
			if (o.isBlank())
				o = newModel.createResource(rep.getNew(o.getBlankNodeId())).asNode();
			
			newModel.getGraph().add(new Triple(s, p, o));
		}
		it.close();
		
		newModel.setNsPrefixes(base.getNsPrefixMap());
		return newModel;
	}

	/**
	 * compute SHA-1 message digest for string
	 * @param string
	 * @return
	 * @throws XLWrapException
	 */
    public static String SHA1(String string) throws XLWrapException {
		MessageDigest md;
		try {
			md = MessageDigest.getInstance("SHA-1");
		} catch (NoSuchAlgorithmException e) {
			throw new XLWrapException("SHA-1 message digest is not available.");
		}
		
		byte[] data = new byte[40];
		md.update(string.getBytes());
		data = md.digest();
		
        StringBuffer buf = new StringBuffer();
        for (int i = 0; i < data.length; i++) {
        	int halfbyte = (data[i] >>> 4) & 0x0F;
        	int two_halfs = 0;
        	do {
	            if ((0 <= halfbyte) && (halfbyte <= 9))
	                buf.append((char) ('0' + halfbyte));
	            else
	            	buf.append((char) ('a' + (halfbyte - 10)));
	            halfbyte = data[i] & 0x0F;
        	} while(two_halfs++ < 1);
        }
        return buf.toString();
	}

	/**
	 * deep copy, copies also values
	 * 
	 * @param source hashtable
	 * @return copy
	 */
	public static <K, V extends Copy<V>> Hashtable<K, V> deepCopy(Map<K, V> map) {
		Hashtable<K, V> copy = new Hashtable<K, V>();
		for (K key : map.keySet())
			copy.put(key, map.get(key).copy());
		return copy;
	}
	/**
	 * flat copy, does not copy values
	 * 
	 * @param source hashtable
	 * @return copy
	 */
	public static <K, V> Hashtable<K, V> copy(Map<K, V> map) {
		Hashtable<K, V> copy = new Hashtable<K, V>();
		for (K key : map.keySet())
			copy.put(key, map.get(key));
		return copy;
	}
}

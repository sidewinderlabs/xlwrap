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
package test.xlwrap.map;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import at.jku.xlwrap.common.Utils;
import at.jku.xlwrap.common.XLWrapException;

/**
 * @author dorgon
 *
 */
public class TestUtils {
	
	@Test
	public void testAlphaToIndex() {
		assertEquals(0, Utils.alphaToIndex("A"));
		assertEquals(25, Utils.alphaToIndex("Z"));
		assertEquals(26, Utils.alphaToIndex("AA"));
		assertEquals(26+24, Utils.alphaToIndex("AY"));
		assertEquals(26*27, Utils.alphaToIndex("AAA"));
	}
	
	@Test
	public void testIndexToAlpha() {
		assertEquals("A", Utils.indexToAlpha(0));
		assertEquals("Z", Utils.indexToAlpha(25));
		assertEquals("AA", Utils.indexToAlpha(26));
		assertEquals("AY", Utils.indexToAlpha(26+24));
		assertEquals("AAA", Utils.indexToAlpha(26*27));
	}
	
	@Test
	public void testRangeParsing() throws XLWrapException {
		String[] orig = new String[] {
				"A4", "ZZZ123",
				"A5:B6", "B6:A5",
				"Sheet1.A4", "'Sheet 1'.A4", "\"Sheet 1\".A4",
				"Sheet1.A4:Sheet2.B6", "'Sheet 1'.A4:'Sheet 2'.B6", "\"Sheet 1\".A4:\"Sheet 2\".B6",
				"Sheet1.*", "*.*",
				"file.xls#$Sheet1.A3", "'file with spaces.xls'#$Sheet1.A3", "\"file with spaces.xls\"#$Sheet1.A3",
				"file.xls#$Sheet1.A3:Sheet2.B6", "'file with spaces.xls'#$Sheet1.A3:'Sheet 2'.B6", "\"file with spaces.xls\"#$Sheet1.A3:'Sheet 2'.B6",
				"file.xls#$#1.A3", "file.xls#$#1.A3:#4.B6", "file.xls#$#1.A3:'Sheet 2'.B6", "file.xls#$'Sheet 1'.A3:#3.B6",
				"file.xls#$#4.A3:#1.B6", "file.xls#$#4.B6:#1.A3"
		};
		String[] result = new String[] {
				"A4", "ZZZ123",
				"A5:B6", "A5:B6",
				"'Sheet1'.A4", "'Sheet 1'.A4", "'Sheet 1'.A4",
				"'Sheet1'.A4:'Sheet2'.B6", "'Sheet 1'.A4:'Sheet 2'.B6", "'Sheet 1'.A4:'Sheet 2'.B6",
				"'Sheet1'.*", "*.*",
				"'file.xls'#$'Sheet1'.A3", "'file with spaces.xls'#$'Sheet1'.A3", "'file with spaces.xls'#$'Sheet1'.A3",
				"'file.xls'#$'Sheet1'.A3:'Sheet2'.B6", "'file with spaces.xls'#$'Sheet1'.A3:'Sheet 2'.B6", "'file with spaces.xls'#$'Sheet1'.A3:'Sheet 2'.B6",
				"'file.xls'#$#1.A3", "'file.xls'#$#1.A3:#4.B6", "'file.xls'#$#1.A3:'Sheet 2'.B6", "'file.xls'#$'Sheet 1'.A3:#3.B6",
				"'file.xls'#$#1.A3:#4.B6", "'file.xls'#$#1.A3:#4.B6"
		};
		
		for (int i=0; i<orig.length; i++)
			assertEquals(result[i], Utils.parseRange(orig[i]).toString());
	}
}

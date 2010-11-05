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
import static org.junit.Assert.assertTrue;

import java.util.Set;

import org.junit.Test;

import test.xlwrap.XLWrapTestCase;
import at.jku.xlwrap.common.XLWrapException;
import at.jku.xlwrap.map.MapTemplate;
import at.jku.xlwrap.map.XLWrapMapping;
import at.jku.xlwrap.map.transf.FileRepeat;

/**
 * @author dorgon
 *
 */
public class TestXLWrapMapping extends XLWrapTestCase {

	@Test
	public void testGetReferedFiles() throws XLWrapException {
		MapTemplate tmpl = new MapTemplate(TEST_FILE_DATA1, null, 0, "[] ex:p1 \"'" + TEST_FILE_EMPTY + "'#Sheet1.*\"^^xl:Expr ; ex:p2 \"'file://path/foo1.xls'#$#4.A3:#6.B6\"^^xl:Expr .");
		tmpl.repeatTransform(new FileRepeat(TEST_FILE_DATA1 + ", 'foo2.xls'", "*.*", null, null));
		XLWrapMapping map = createMapping(tmpl);
		Set<String> files = map.getReferredFiles();
		assertTrue(files.contains(TEST_FILE_DATA1));
		assertTrue(files.contains(TEST_FILE_EMPTY));
		assertTrue(files.contains("file://path/foo1.xls"));
		assertTrue(files.contains("foo2.xls"));
		assertEquals(4, files.size());
	}
	
}

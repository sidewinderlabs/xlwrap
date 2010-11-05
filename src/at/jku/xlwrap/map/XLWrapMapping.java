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
package at.jku.xlwrap.map;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import at.jku.xlwrap.common.Utils;
import at.jku.xlwrap.common.XLWrapException;
import at.jku.xlwrap.map.expr.E_RangeRef;
import at.jku.xlwrap.map.expr.XLExpr;
import at.jku.xlwrap.map.expr.XLExprVisitor;
import at.jku.xlwrap.map.expr.XLExprWalker;
import at.jku.xlwrap.map.range.BoxRange;
import at.jku.xlwrap.map.range.CellRange;
import at.jku.xlwrap.map.range.FullSheetRange;
import at.jku.xlwrap.map.range.MultiRange;
import at.jku.xlwrap.map.range.Range;
import at.jku.xlwrap.map.transf.FileRepeat;
import at.jku.xlwrap.map.transf.Transformation;

import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;

/**
 * @author dorgon
 *
 */
public class XLWrapMapping {
	private final List<MapTemplate> templates = new ArrayList<MapTemplate>();
	private boolean offline = false;
	
	static {
		XLExprDatatype.register();
	}

	/**
	 * @param template
	 */
	public void add(MapTemplate template) {
		templates.add(template);
	}

	/**
	 * @return iterator over map templates
	 */
	public Iterator<MapTemplate> getMapTemplatesIterator() {
		return templates.iterator();
	}
	
	/**
	 * @param offline the offline to set
	 */
	public void setOffline(boolean offline) {
		this.offline = offline;
	}
	
	/**
	 * @return the offline
	 */
	public boolean isOffline() {
		return offline;
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("### XLWrapMapping ###\n");
		int n = templates.size();
		
		int i = 1;
		for (MapTemplate t : templates)
			sb.append("---<template ").append(i++).append("/").append(n).append(">-----------------------------------------------\n").append(t.toString()).append("\n");
		return sb.toString();
	}

	/**
	 * @return prefixes used in the mapping
	 */
	public Map<String, String> getNsPrefixMap() {
		Map<String, String> map = new Hashtable<String, String>();
		for (MapTemplate tmpl : templates)
			map.putAll(tmpl.getTemplateModel().getNsPrefixMap());
		return map;
	}
	
	/**
	 * @return
	 * @throws XLWrapException 
	 */
	public Set<String> getReferredFiles() throws XLWrapException {
		Set<String> referredFiles = new HashSet<String>();
		for (MapTemplate tmpl : templates) {
			
			// base file
			referredFiles.add(tmpl.getFileName());
			
			// files referred by FileRepeat
			for (Transformation t : tmpl.getTransformations()) {
				if (t instanceof FileRepeat)
					referredFiles.addAll(((FileRepeat) t).getTargetFiles());
			}
			
			// files referred by absolute cell references in xl:Expr
			CollectFileNames coll = new CollectFileNames();
			StmtIterator it = tmpl.getTemplateModel().listStatements();
			while (it.hasNext()) {
				Statement st = it.nextStatement();
				XLExpr expr = Utils.getExpression(st.getObject().asNode());
				if (expr != null)
					XLExprWalker.walkPostOrder(expr, coll);
			}
			it.close();
			
			// add collected file names from xl:Expr ranges
			referredFiles.addAll(coll.list);
		}

		return referredFiles;
	}
		
	private class CollectFileNames implements XLExprVisitor {
		private final List<String> list = new ArrayList<String>();
		
		@Override
		public void visiting0(XLExpr expr0) throws XLWrapException {
			if (expr0 instanceof E_RangeRef) {
				Range r = ((E_RangeRef) expr0).getRange();
				addFileName(r);
			}
		}
		
		/**
		 * recursively adds file name of a range in case of MultiRange
		 * @param r
		 */
		private void addFileName(Range r) {
			String f = null;
			if (r instanceof CellRange)
				f = ((CellRange) r).getFileName();
			else if (r instanceof BoxRange)
				f = ((BoxRange) r).getFileName();
			else if (r instanceof FullSheetRange)
				f = ((FullSheetRange) r).getFileName();
			else if (r instanceof MultiRange) {
				Iterator<Range> it = ((MultiRange) r).getRangeIterator();
				while (it.hasNext())
					addFileName(it.next());
			}

			if (f != null)
				list.add(f);
		}
		
		/**
		 * @return the list
		 */
		public List<String> getFileNames() {
			return list;
		}
		
		@Override
		public void visiting1(XLExpr expr) throws XLWrapException {	}

		@Override
		public void visiting2(XLExpr expr) throws XLWrapException {	}

		@Override
		public void visitingFunction(XLExpr expr) throws XLWrapException {	}

	}
}

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
package at.jku.xlwrap.exec;

import at.jku.xlwrap.common.XLWrapException;

import com.hp.hpl.jena.rdf.model.Model;

/**
 * @author dorgon
 *
 * Generates a sequence of template models based on the transformations
 * of the active MapTemplate of the ExecutionContext.
 * 
 */
public class TemplateModelGenerator {

	/** next template */
	private Model next;
	
	/** stacked transformation executor sequence */
	private TransformationStage transExec;
	
	/**
	 * constructor
	 * 
	 * @param wbRegistry 
	 * @throws XLWrapException 
	 */
	public TemplateModelGenerator(ExecutionContext context) throws XLWrapException {
		if (context.getActiveTemplate().getTransformations().size() > 0) {
			transExec = TransformationStage.create(context);
			next = transExec.getStageTemplate();
			
		// no transformations, just use base template once
		} else {
			transExec = null;
			next = context.getActiveTemplate().getTemplateModel();
		}
	}
	
	public boolean hasNext() throws XLWrapException {
		return next != null;
	}

	/**
	 * get next template model, the base model transformed by the sequence of transformation executors
	 * 
	 * @return
	 * @throws XLWrapException
	 */
	public Model next() throws XLWrapException {
		Model current = next;
		
		// try to get next
		if (transExec == null) {
			next = null;
		} else if (transExec.proceed()) {
			next = transExec.getStageTemplate();
		} else
			next = null;
		
		return current;
	}

}

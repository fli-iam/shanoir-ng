/**
 * Shanoir NG - Import, manage and share neuroimaging data
 * Copyright (C) 2009-2019 Inria - https://www.inria.fr/
 * Contact us on https://project.inria.fr/shanoir/
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see https://www.gnu.org/licenses/gpl-3.0.html
 */

package org.shanoir.ng.utils;

import org.shanoir.ng.preclinical.contrast_agent.ContrastAgent;

/**
 * Utility class for test.
 * Generates contrast agent.
 * 
 * @author sloury
 *
 */
public final class ContrastAgentModelUtil {

	// ContrastAgent data
	public static final Long AGENT_GADO_ID = 1L;
	public static final String AGENT_GADO_MANUFACTURED_NAME = "Gadolinium";
	public static final String AGENT_GADO_REFERENCE_NAME = "Gadolinium";
	
	/**
	 * Create a contrast agent.
	 * 
	 * @return contrast agent.
	 */
	public static ContrastAgent createContrastAgentGado() {
		ContrastAgent agent = new ContrastAgent();
		agent.setId(AGENT_GADO_ID);
		agent.setName(ReferenceModelUtil.createReferenceContrastAgentGado());
		agent.setManufacturedName(AGENT_GADO_MANUFACTURED_NAME);
		return agent;
	}
	
	
}

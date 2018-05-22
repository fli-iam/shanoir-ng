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

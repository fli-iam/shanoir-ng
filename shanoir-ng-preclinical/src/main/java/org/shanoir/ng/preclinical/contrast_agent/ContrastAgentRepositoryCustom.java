package org.shanoir.ng.preclinical.contrast_agent;

import java.util.List;

/**
 * Custom repository for contrast agents.
 * 
 * @author sloury
 *
 */
public interface ContrastAgentRepositoryCustom {
	
	List<ContrastAgent> findBy(String fieldName, Object value);
	
	

}

package org.shanoir.ng.preclinical.contrast_agent;

import java.util.List;

import org.shanoir.ng.preclinical.references.Reference;
import org.shanoir.ng.shared.exception.ShanoirException;
import org.shanoir.ng.shared.validation.UniqueCheckableService;



/**
 * Contrast Agent service.
 *
 * @author sloury
 *
 */
public interface ContrastAgentService extends UniqueCheckableService<ContrastAgent> {

	/**
	 * Delete a contrast agent.
	 * 
	 * @param id
	 *            contrast agent id.
	 * @throws ShanoirException
	 */
	void deleteById(Long id) throws ShanoirException;

	/**
	 * Get all the contrast agents.
	 * 
	 * @return a list of contrast agents.
	 */
	List<ContrastAgent> findAll();

	
	/**
	 * Find contrast agent by its id.
	 *
	 * @param id
	 *            contrast agent id.
	 * @return a contrast agent or null.
	 */
	ContrastAgent findById(Long id);
	
	/**
	 * Find contrast agent by its protocol id.
	 *
	 * @param id
	 *            protocol id.
	 * @return a contrast agent or null.
	 */
	ContrastAgent findByProtocolId(Long protocolId);
	
	/**
	 * Find contrast agent by its name
	 *
	 * @param name
	 *            contrast agent name.
	 * @return a contrast agent or null.
	 */
	ContrastAgent findByName(Reference name);
	
	
	/**
	 * Save a contrast agent.
	 *
	 * @param contrast agent
	 *            contrast agent to create.
	 * @return created contrast agent.
	 * @throws ShanoirException
	 */
	ContrastAgent save(ContrastAgent agent) throws ShanoirException;

	/**
	 * Update a contrast agent.
	 *
	 * @param contrast agent
	 *            contrast agent to update.
	 * @return updated contrast agent.
	 * @throws ShanoirException
	 */
	ContrastAgent update(ContrastAgent agent) throws ShanoirException;

	

}

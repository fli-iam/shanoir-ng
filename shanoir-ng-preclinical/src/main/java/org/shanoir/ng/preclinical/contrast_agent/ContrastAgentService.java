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

package org.shanoir.ng.preclinical.contrast_agent;

import java.util.List;

import org.shanoir.ng.preclinical.references.Reference;
import org.shanoir.ng.shared.exception.ShanoirException;



/**
 * Contrast Agent service.
 *
 * @author sloury
 *
 */
public interface ContrastAgentService {

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

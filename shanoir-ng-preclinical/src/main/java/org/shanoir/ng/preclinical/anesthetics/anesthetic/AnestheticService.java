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

package org.shanoir.ng.preclinical.anesthetics.anesthetic;

import java.util.List;

import org.shanoir.ng.shared.exception.ShanoirException;



/**
 * Anesthetics service.
 *
 * @author sloury
 *
 */
public interface AnestheticService {

	/**
	 * Delete an anesthetic.
	 * 
	 * @param id
	 *            anesthetic id.
	 * @throws ShanoirException
	 */
	void deleteById(Long id) throws ShanoirException;

	/**
	 * Get all the anesthetics.
	 * 
	 * @return a list of anesthetics.
	 */
	List<Anesthetic> findAll();
	
	/**
	 * Get all the anesthetics by type.
	 * 
	 * @return a list of anesthetics.
	 */
	List<Anesthetic> findAllByAnestheticType(AnestheticType type);

	
	/**
	 * Find anesthetic by its id.
	 *
	 * @param id
	 *            anesthetic id.
	 * @return a anesthetic or null.
	 */
	Anesthetic findById(Long id);
	
	
	/**
	 * Save an anesthetic.
	 *
	 * @param anesthetic
	 *            anesthetic to create.
	 * @return created anesthetic.
	 * @throws ShanoirException
	 */
	Anesthetic save(Anesthetic anesthetic) throws ShanoirException;

	/**
	 * Update a anesthetic.
	 *
	 * @param anesthetic
	 *            anesthetic to update.
	 * @return updated anesthetic.
	 * @throws ShanoirException
	 */
	Anesthetic update(Anesthetic anesthetic) throws ShanoirException;

	

}

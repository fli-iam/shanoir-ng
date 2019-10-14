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

package org.shanoir.ng.preclinical.therapies;

import java.util.List;

import org.shanoir.ng.shared.exception.ShanoirException;
import org.shanoir.ng.shared.validation.UniqueCheckableService;



/**
 * Therapies service.
 *
 * @author sloury
 *
 */
public interface TherapyService extends UniqueCheckableService<Therapy> {

	/**
	 * Delete a therapy.
	 * 
	 * @param id
	 *            therapy id.
	 * @throws ShanoirException
	 */
	void deleteById(Long id) throws ShanoirException;

	/**
	 * Get all the therapies.
	 * 
	 * @return a list of therapies.
	 */
	List<Therapy> findAll();

	
	/**
	 * Find Therapy by its id.
	 *
	 * @param id
	 *            Therapy id.
	 * @return a Therapy or null.
	 */
	Therapy findById(Long id);
	
	/**
	 * Find Therapy by its name.
	 *
	 * @param id
	 *            Therapy name.
	 * @return a Therapy or null.
	 */
	Therapy findByName(String name);
	
	/**
	 * Find Therapies by their type.
	 *
	 * @param type
	 *            Therapy type.
	 * @return a list of therapies or null.
	 */
	List<Therapy> findByTherapyType(TherapyType therapyType);

	/**
	 * Save a therapy.
	 *
	 * @param therapy
	 *            therapy to create.
	 * @return created therapy.
	 * @throws ShanoirException
	 */
	Therapy save(Therapy therapy) throws ShanoirException;

	/**
	 * Update a therapy.
	 *
	 * @param therapy
	 *            therapy to update.
	 * @return updated therapy.
	 * @throws ShanoirException
	 */
	Therapy update(Therapy therapy) throws ShanoirException;

	

}

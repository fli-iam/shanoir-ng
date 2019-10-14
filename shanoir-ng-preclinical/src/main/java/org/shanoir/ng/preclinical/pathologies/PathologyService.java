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

package org.shanoir.ng.preclinical.pathologies;

import java.util.List;
import java.util.Optional;

import org.shanoir.ng.shared.exception.ShanoirException;
import org.shanoir.ng.shared.validation.UniqueCheckableService;



/**
 * Pathologies service.
 *
 * @author sloury
 *
 */
public interface PathologyService extends UniqueCheckableService<Pathology> {

	/**
	 * Delete a reference value.
	 * 
	 * @param id
	 *            template id.
	 * @throws ShanoirException
	 */
	void deleteById(Long id) throws ShanoirException;

	/**
	 * Get all the references.
	 * 
	 * @return a list of references.
	 */
	List<Pathology> findAll();

	
	/**
	 * Find reference by its id.
	 *
	 * @param id
	 *            reference id.
	 * @return a reference or null.
	 */
	Pathology findById(Long id);
	
	Optional<Pathology> findByName(String name);

	/**
	 * Save a reference.
	 *
	 * @param reference
	 *            reference to create.
	 * @return created reference.
	 * @throws ShanoirException
	 */
	Pathology save(Pathology pathology) throws ShanoirException;

	/**
	 * Update a reference.
	 *
	 * @param reference
	 *            reference to update.
	 * @return updated reference.
	 * @throws ShanoirException
	 */
	Pathology update(Pathology pathology) throws ShanoirException;

	/*
	 * Update Shanoir Old with new pathology.
	 *
	 * @param Pathology pathology.
	 *
	 * @return false if it fails, true if it succeed.
	 */
	boolean updateFromShanoirOld(final Pathology pathology);
	

}

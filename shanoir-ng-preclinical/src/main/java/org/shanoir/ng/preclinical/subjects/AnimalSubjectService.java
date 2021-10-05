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

package org.shanoir.ng.preclinical.subjects;

import java.util.List;

import org.shanoir.ng.preclinical.references.Reference;
import org.shanoir.ng.shared.exception.ShanoirException;

/**
 * Refs service.
 *
 * @author sloury
 *
 */
public interface AnimalSubjectService {

	/**
	 * Delete a animalSubject value.
	 * 
	 * @param id
	 *            animalSubject id.
	 * @throws ShanoirException
	 */
	void deleteById(Long id) throws ShanoirException;

	/**
	 * Get all the AnimalSubject.
	 * 
	 * @return a list of AnimalSubject.
	 */
	List<AnimalSubject> findAll();

	/**
	 * Find AnimalSubject by its id.
	 *
	 * @param id
	 *            AnimalSubject id.
	 * @return a AnimalSubject or null.
	 */
	AnimalSubject findById(Long id);

	/**
	 * Save a AnimalSubject.
	 *
	 * @param AnimalSubject
	 *            AnimalSubject to create.
	 * @return created AnimalSubject.
	 * @throws ShanoirException
	 */
	AnimalSubject save(AnimalSubject subject) throws ShanoirException;

	/**
	 * Update a AnimalSubject.
	 *
	 * @param AnimalSubject
	 *            AnimalSubject to update.
	 * @return updated AnimalSubject.
	 * @throws ShanoirException
	 */
	AnimalSubject update(AnimalSubject subject) throws ShanoirException;

	List<AnimalSubject> findByReference(Reference reference);

	List<AnimalSubject> findBySubjectId(Long id);

}

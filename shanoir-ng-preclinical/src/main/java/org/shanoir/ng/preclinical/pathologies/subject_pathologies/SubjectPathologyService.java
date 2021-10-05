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

package org.shanoir.ng.preclinical.pathologies.subject_pathologies;

import java.util.List;

import org.shanoir.ng.preclinical.pathologies.Pathology;
import org.shanoir.ng.preclinical.pathologies.pathology_models.PathologyModel;
import org.shanoir.ng.preclinical.references.Reference;
import org.shanoir.ng.preclinical.subjects.AnimalSubject;
import org.shanoir.ng.shared.exception.ShanoirException;

/**
 * Subject Pathology service.
 *
 * @author sloury
 *
 */
public interface SubjectPathologyService {

	/**
	 * Delete a reference value.
	 * 
	 * @param id
	 *            template id.
	 * @throws ShanoirException
	 */
	void deleteById(Long id) throws ShanoirException;

	/**
	 * delete all subject pathologies for a given animalSubject
	 * 
	 * @param animalSubject
	 * @throws ShanoirException
	 */
	public void deleteByAnimalSubject(AnimalSubject animalSubject) throws ShanoirException;

	/**
	 * Get all the references.
	 * 
	 * @return a list of references.
	 */
	List<SubjectPathology> findAll();

	List<SubjectPathology> findByAnimalSubject(AnimalSubject animalSubject);

	List<SubjectPathology> findAllByPathology(Pathology pathology);

	List<SubjectPathology> findAllByPathologyModel(PathologyModel model);

	List<SubjectPathology> findAllByLocation(Reference location);

	/**
	 * Find reference by its id.
	 *
	 * @param id
	 *            reference id.
	 * @return a reference or null.
	 */
	SubjectPathology findById(Long id);

	/**
	 * Save a reference.
	 *
	 * @param reference
	 *            reference to create.
	 * @return created reference.
	 * @throws ShanoirException
	 */
	SubjectPathology save(SubjectPathology pathos) throws ShanoirException;

	/**
	 * Update a reference.
	 *
	 * @param reference
	 *            reference to update.
	 * @return updated reference.
	 * @throws ShanoirException
	 */
	SubjectPathology update(SubjectPathology pathos) throws ShanoirException;

	List<SubjectPathology> findByPathologyModel(PathologyModel patMod);

}

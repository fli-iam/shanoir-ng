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

package org.shanoir.ng.preclinical.therapies.subject_therapies;

import java.util.List;

import org.shanoir.ng.preclinical.subjects.AnimalSubject;
import org.shanoir.ng.preclinical.therapies.Therapy;
import org.shanoir.ng.shared.exception.ShanoirException;

/**
 * Subject Therapy service.
 *
 * @author sloury
 *
 */
public interface SubjectTherapyService {

	/**
	 * Delete a subject therapy.
	 * 
	 * @param id
	 *            subject therapy id.
	 * @throws ShanoirException
	 */
	void deleteById(Long id) throws ShanoirException;

	/**
	 * delete all subject therapies for a given animalSubject
	 * 
	 * @param animalSubject
	 * @throws ShanoirException
	 */
	public void deleteByAnimalSubject(AnimalSubject animalSubject) throws ShanoirException;

	/**
	 * Get all the subject therapies.
	 * 
	 * @return a list of subject therapies.
	 */
	List<SubjectTherapy> findAll();

	List<SubjectTherapy> findAllByAnimalSubject(AnimalSubject animalSubject);

	List<SubjectTherapy> findAllByTherapy(Therapy therapy);

	/**
	 * Find subject therapy by its id.
	 *
	 * @param id
	 *            subject therapy id.
	 * @return a subject therapy or null.
	 */
	SubjectTherapy findById(Long id);

	/**
	 * Save a subject therapy.
	 *
	 * @param subject
	 *            therapy subject therapy to create.
	 * @return created SubjectTherapy.
	 * @throws ShanoirException
	 */
	SubjectTherapy save(SubjectTherapy subtherapy) throws ShanoirException;

	/**
	 * Update a subject therapy.
	 *
	 * @param subject
	 *            therapy subject therapy to update.
	 * @return updated SubjectTherapy.
	 * @throws ShanoirException
	 */
	SubjectTherapy update(SubjectTherapy subtherapy) throws ShanoirException;

}

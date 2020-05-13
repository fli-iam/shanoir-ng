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

package org.shanoir.ng.preclinical.anesthetics.examination_anesthetics;

import java.util.List;

import org.shanoir.ng.preclinical.anesthetics.anesthetic.Anesthetic;
import org.shanoir.ng.shared.exception.ShanoirException;
import org.shanoir.ng.shared.validation.UniqueCheckableService;

/**
 * Examination anesthetic service.
 *
 * @author sloury
 *
 */
public interface ExaminationAnestheticService extends UniqueCheckableService<ExaminationAnesthetic> {

	/**
	 * Delete an examination anesthetic
	 * 
	 * @param id
	 *            examination anesthetic id.
	 * @throws ShanoirException
	 */
	void deleteById(Long id) throws ShanoirException;

	/**
	 * Get all the examination anesthetics
	 * 
	 * @return a list of examination anesthetics
	 */
	List<ExaminationAnesthetic> findAll();

	/**
	 * Get all the examination anesthetics by examination id
	 * 
	 * @return a list of examination anesthetics
	 */
	List<ExaminationAnesthetic> findByExaminationId(Long examinationId);

	/**
	 * Find examination anesthetic by its id.
	 *
	 * @param id
	 *            examination anesthetic id.
	 * @return a examination anesthetic or null.
	 */
	ExaminationAnesthetic findById(Long id);

	/**
	 * Save an examination anesthetic
	 *
	 * @param examination
	 *            anesthetic examination anesthetic to create.
	 * @return created ExaminationAnesthetic.
	 * @throws ShanoirException
	 */
	ExaminationAnesthetic save(ExaminationAnesthetic examAnesthetic) throws ShanoirException;

	/**
	 * Update a examination anesthetic
	 *
	 * @param examination
	 *            anesthetic examination anesthetic to update.
	 * @return updated ExaminationAnesthetic.
	 * @throws ShanoirException
	 */
	ExaminationAnesthetic update(ExaminationAnesthetic examAnesthetic) throws ShanoirException;

	/**
	 * Get all the examination anesthetics by anesthetic
	 * 
	 * @return a list of examination anesthetics
	 */
	List<ExaminationAnesthetic> findByAnesthetic(Anesthetic anesthetic);

}

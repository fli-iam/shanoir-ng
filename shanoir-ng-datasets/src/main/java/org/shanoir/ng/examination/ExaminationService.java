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

package org.shanoir.ng.examination;

import java.util.List;

import org.shanoir.ng.shared.exception.ShanoirDatasetsException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * Examination service.
 *
 * @author ifakhfakh
 *
 */
public interface ExaminationService {

	/**
	 * Get number of examinations reachable by connected user.
	 * 
	 * @return number of examinations.
	 * @throws ShanoirDatasetsException
	 */
	long countExaminationsByUserId() throws ShanoirDatasetsException;

	/**
	 * Delete an examination.
	 * 
	 * @param id
	 *            examination id.
	 * @throws ShanoirDatasetsException
	 */
	void deleteById(Long id) throws ShanoirDatasetsException;

	/**
	 * Get a paginated list of examinations reachable by connected user.
	 * 
	 * @param pageable
	 *            pagination data.
	 * @return list of examinations.
	 * @throws ShanoirDatasetsException
	 */
	Page<Examination> findPage(Pageable pageable) throws ShanoirDatasetsException;;

	/**
	 * Find examination by its id.
	 *
	 * @param id
	 *            examination id.
	 * @return an examination or null.
	 * @throws ShanoirDatasetsException
	 */
	Examination findById(Long id) throws ShanoirDatasetsException;

	/**
	 * Save an examination.
	 *
	 * @param examination
	 *            examination to create.
	 * @return created examination.
	 * @throws ShanoirDatasetsException
	 */
	Examination save(Examination examination) throws ShanoirDatasetsException;

	/**
	 * Save an examination.
	 *
	 * @param examinationDTO
	 *            examination to create.
	 * @return created examination.
	 * @throws ShanoirDatasetsException
	 */
	Examination save(ExaminationDTO examinationDTO) throws ShanoirDatasetsException;
	
	/**
	 * Update an examination.
	 *
	 * @param examination
	 *            examination to update.
	 * @return updated examination.
	 * @throws ShanoirDatasetsException
	 */
	Examination update(Examination examination) throws ShanoirDatasetsException;

	/**
	 * Update an examination from the old Shanoir
	 * 
	 * @param examination
	 *            examination.
	 * @throws ShanoirDatasetsException
	 */
	void updateFromShanoirOld(Examination examination) throws ShanoirDatasetsException;

	/**
	 * @param subjectId
	 * @return
	 * @author yyao
	 */
	List<Examination> findBySubjectId(Long subjectId);

	/**
	 * Find examinations related to particular subject and study
	 * 
	 * @param subjectId: the id of the subject
	 * @param studyId: the id of the study
	 * @return list of examinations.
	 */
	List<Examination> findBySubjectIdStudyId(Long subjectId, Long studyId);
	

}

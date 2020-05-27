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

package org.shanoir.ng.examination.service;

import java.util.List;

import org.shanoir.ng.examination.dto.ExaminationDTO;
import org.shanoir.ng.examination.model.Examination;
import org.shanoir.ng.shared.exception.EntityNotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PostAuthorize;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.multipart.MultipartFile;

/**
 * Examination service.
 *
 * @author ifakhfakh
 *
 */
public interface ExaminationService {

	/**
	 * Delete an examination.
	 * 
	 * @param id examination id.
	 * @throws EntityNotFoundException
	 */
	@PreAuthorize("hasRole('ADMIN') or (hasRole('EXPERT') and @datasetSecurityService.hasRightOnExamination(#id, 'CAN_ADMINISTRATE'))")
	void deleteById(Long id) throws EntityNotFoundException;

	/**
	 * Get a paginated list of examinations reachable by connected user.
	 * 
	 * @param pageable pagination data.
	 * @return list of examinations.
	 */
	@PreAuthorize("hasAnyRole('ADMIN', 'EXPERT', 'USER')")
	@PostAuthorize("hasRole('ADMIN') or @datasetSecurityService.filterExaminationPage(returnObject, 'CAN_SEE_ALL')")
	Page<Examination> findPage(Pageable pageable);

	/**
	 * Find examination by its id.
	 *
	 * @param id examination id.
	 * @return an examination or null.
	 */
	@PreAuthorize("hasAnyRole('ADMIN', 'EXPERT', 'USER')")
	@PostAuthorize("hasRole('ADMIN') or @datasetSecurityService.hasRightOnStudy(returnObject.getStudyId(), 'CAN_SEE_ALL')")
	Examination findById(Long id);

	/**
	 * @param subjectId
	 * @return
	 * @author yyao
	 */
	@PreAuthorize("hasAnyRole('ADMIN', 'EXPERT', 'USER')")
	@PostAuthorize("hasRole('ADMIN') or @datasetSecurityService.filterExaminationList(returnObject, 'CAN_SEE_ALL')")
	List<Examination> findBySubjectId(Long subjectId);

	/**
	 * Find examinations related to particular subject and study
	 * 
	 * @param subjectId: the id of the subject
	 * @param studyId: the id of the study
	 * @return list of examinations.
	 */
	@PreAuthorize("hasRole('ADMIN') or (hasAnyRole('EXPERT', 'USER') and @datasetSecurityService.hasRightOnStudy(#studyId, 'CAN_SEE_ALL'))")
	List<Examination> findBySubjectIdStudyId(Long subjectId, Long studyId);

	/**
	 * Save an examination.
	 *
	 * @param examination  examination to create.
	 * @return created examination.
	 */
	@PreAuthorize("hasRole('ADMIN') or (hasAnyRole('EXPERT', 'USER') and @datasetSecurityService.hasRightOnStudy(#examination.getStudyId(), 'CAN_IMPORT'))")
	Examination save(Examination examination);

	/**
	 * Save an examination.
	 *
	 * @param examinationDTO examination to create.
	 * @return created examination.
	 */
	@PreAuthorize("hasRole('ADMIN') or (hasAnyRole('EXPERT', 'USER') and @datasetSecurityService.hasRightOnStudy(#examinationDTO.getStudy().getId(), 'CAN_IMPORT'))")
	Examination save(ExaminationDTO examinationDTO);
	
	/**
	 * Update an examination.
	 *
	 * @param examination  examination to update.
	 * @return updated examination.
	 * @throws EntityNotFoundException
	 */
	@PreAuthorize("hasRole('ADMIN')")
	Examination update(Examination examination) throws EntityNotFoundException;

	/**
	 * Get a paginated list of preclinical examinations reachable by connected user, for a given
	 * preclinical value.
	 * 
	 * @param isPreclinical preclinical examination
	 * @param pageable pagination data.
	 * @return list of preclinical examinations.
	 */
	@PreAuthorize("hasAnyRole('ADMIN', 'EXPERT', 'USER')")
	@PostAuthorize("hasRole('ADMIN') or @datasetSecurityService.filterExaminationPage(returnObject, 'CAN_SEE_ALL')")
	Page<Examination> findPreclinicalPage(boolean isPreclinical, Pageable pageable);

	/**
	 * Add an extra data file to examination
	 * @param examinationId the examination ID
	 * @param file the file to add
	 * @return true if it's a success, false otherwise
	 */
	String addExtraData(Long examinationId, MultipartFile file);

	String getExtraDataFilePath(Long examinationId, String fileName);
}

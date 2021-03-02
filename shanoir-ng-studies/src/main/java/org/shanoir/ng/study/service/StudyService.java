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

package org.shanoir.ng.study.service;

import java.util.List;

import org.shanoir.ng.shared.exception.AccessDeniedException;
import org.shanoir.ng.shared.exception.EntityNotFoundException;
import org.shanoir.ng.shared.exception.MicroServiceCommunicationException;
import org.shanoir.ng.study.model.Study;
import org.shanoir.ng.study.model.StudyUser;
import org.springframework.security.access.prepost.PostAuthorize;
import org.springframework.security.access.prepost.PostFilter;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

/**
 * Study service.
 *
 */
@Service
public interface StudyService {

	/**
	 * Delete a study. Do check before if current user can delete study!
	 *
	 * @param id study id.
	 * @throws EntityNotFoundException
	 * @throws AccessDeniedException
	 */
	@PreAuthorize("hasAnyRole('ADMIN', 'EXPERT') and @studySecurityService.hasRightOnStudy(#id, 'CAN_ADMINISTRATE')")
	void deleteById(Long id) throws EntityNotFoundException;


	/**
	 * Find study by its id. Check if current user can see study.
	 *
	 * @param id study id.
	 * @return a study or null.
	 * @throws AccessDeniedException
	 */
	@PreAuthorize("hasAnyRole('USER', 'ADMIN', 'EXPERT')")
	@PostAuthorize("@studySecurityService.hasRightOnTrustedStudy(returnObject, 'CAN_SEE_ALL') or @studySecurityService.hasRightOnTrustedStudy(returnObject, 'CAN_ADMINISTRATE')")
	Study findById(Long id);


	/**
	 * Get all the studies
	 * 
	 * @return a list of studies
	 */
	@PreAuthorize("hasAnyRole('USER', 'ADMIN', 'EXPERT')")
	@PostFilter("@studySecurityService.hasRightOnTrustedStudy(filterObject, 'CAN_SEE_ALL')")
	List<Study> findAll();

	/**
	 * Get all the challenges
	 * 
	 * @return a list of challenges
	 */
	List<Study> findChallenges();

	/**
	 * add new study
	 * 
	 * @param study
	 * @return created Study
	 * @throws MicroServiceCommunicationException
	 * @throws ShanoirStudiesException
	 */
	@PreAuthorize("hasAnyRole('ADMIN', 'EXPERT')  and @studySecurityService.studyUsersStudyNull(#study)")
	Study create(Study study) throws MicroServiceCommunicationException;

	
	/**
	 * Update a study
	 * 
	 * @param study
	 * @return updated study
	 * @throws ShanoirStudiesException
	 * @throws EntityNotFoundException
	 * @throws MicroServiceCommunicationException
	 * @throws AccessDeniedException
	 */
	@PreAuthorize("hasAnyRole('ADMIN', 'EXPERT') and @studySecurityService.hasRightOnStudy(#study.id, 'CAN_ADMINISTRATE') and @studySecurityService.studyUsersMatchStudy(#study)")
	Study update(Study study) throws EntityNotFoundException, MicroServiceCommunicationException;

	/**
	 * Adds one studyUser to a study.
	 * @param studyUser
	 * @param study
	 */
	void addStudyUserToStudy(StudyUser studyUser, Study study);

	/**
	 * Gets the protocol or data user agreement file path
	 * 
	 * @param studyId
	 *            id of the study
	 * @param fileName
	 *            name of the file
	 * @return the file path of the file
	 */
	String getStudyFilePath(Long studyId, String fileName);

}

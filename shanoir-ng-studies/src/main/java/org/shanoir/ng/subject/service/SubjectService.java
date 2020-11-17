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

package org.shanoir.ng.subject.service;

import java.util.List;

import org.shanoir.ng.shared.core.model.IdName;
import org.shanoir.ng.shared.exception.EntityNotFoundException;
import org.shanoir.ng.shared.exception.MicroServiceCommunicationException;
import org.shanoir.ng.shared.exception.RestServiceException;
import org.shanoir.ng.shared.exception.ShanoirException;
import org.shanoir.ng.subject.dto.SimpleSubjectDTO;
import org.shanoir.ng.subject.model.Subject;
import org.springframework.security.access.prepost.PostAuthorize;
import org.springframework.security.access.prepost.PostFilter;
import org.springframework.security.access.prepost.PreAuthorize;

/**
 * Subject service.
 *
 * @author msimon
 *
 */
public interface SubjectService {
	
	/**
	 * Get all the subjects.
	 *
	 * @return a list of subjects.
	 */
	@PreAuthorize("hasAnyRole('ADMIN', 'EXPERT', 'USER')")
	@PostFilter("hasRole('ADMIN') or @studySecurityService.hasRightOnTrustedSubjectForOneStudy(filterObject, 'CAN_SEE_ALL')")
	List<Subject> findAll();
	
	
	/**
	 * Get all the subjects.
	 *
	 * @return a list of subjects.
	 */
	@PreAuthorize("hasAnyRole('ADMIN', 'EXPERT', 'USER')")
	List<IdName> findNames();

	
	/**
	 * Get all the subjects of a study
	 *
	 * @param studyId
	 * @return list of subjects
	 */
	@PreAuthorize("hasAnyRole('ADMIN', 'EXPERT', 'USER')")
	@PostFilter("hasRole('ADMIN') or @studySecurityService.hasRightOnSubjectForOneStudy(filterObject.getId(), 'CAN_SEE_ALL')")
	public List<SimpleSubjectDTO> findAllSubjectsOfStudy(final Long studyId);
	
	
	/**
	 * Get all the subjects of a study
	 *
	 * @param studyId
	 * @param preclinical is the subject preclinical or not
	 * @return list of subjects
	 */
	@PreAuthorize("hasAnyRole('ADMIN', 'EXPERT', 'USER')")
	@PostFilter("hasRole('ADMIN') or @studySecurityService.hasRightOnSubjectForOneStudy(filterObject.getId(), 'CAN_SEE_ALL')")
	List<SimpleSubjectDTO> findAllSubjectsOfStudyAndPreclinical(Long studyId, boolean preclinical);
	
	/**
	 * Find subject by data.
	 *
	 * @param data data.
	 * @return a subject.
	 */
	@PreAuthorize("hasAnyRole('ADMIN', 'EXPERT', 'USER')")
	@PostAuthorize("hasRole('ADMIN') or @studySecurityService.hasRightOnTrustedSubjectForOneStudy(returnObject, 'CAN_SEE_ALL')")
	Subject findByData(String data);

	/**
	 * Find subject by its id.
	 *
	 * @param id template id.
	 * @return a template or null.
	 */
	@PreAuthorize("hasAnyRole('ADMIN', 'EXPERT', 'USER')")
	@PostAuthorize("hasRole('ADMIN') or @studySecurityService.hasRightOnTrustedSubjectForOneStudy(returnObject, 'CAN_SEE_ALL')")
	Subject findById(Long id);

	/**
	 * Find subject by its identifier.
	 *
	 * @param indentifier
	 * @return the subject or null
	 */
	@PreAuthorize("hasAnyRole('ADMIN', 'EXPERT', 'USER')")
	@PostAuthorize("hasRole('ADMIN') or @studySecurityService.hasRightOnTrustedSubjectForOneStudy(returnObject, 'CAN_SEE_ALL')")
	Subject findByIdentifier(String indentifier);
	
	/**
	 * Find a subject by its subject-study relationship id.
	 * 
	 * @param id id
	 * @return a subject or null
	 */
	@PreAuthorize("hasAnyRole('ADMIN', 'EXPERT', 'USER')")
	@PostAuthorize("hasRole('ADMIN') or @studySecurityService.hasRightOnTrustedSubjectForOneStudy(returnObject, 'CAN_SEE_ALL')")
	Subject findByIdWithSubjecStudies(Long subjectStudyId);

	/**
	 * Find a subject from a center code
	 * @param centerCode
	 * @return a subject or null
	 */
	@PreAuthorize("hasAnyRole('ADMIN', 'EXPERT', 'USER')")
	@PostAuthorize("hasRole('ADMIN') or @studySecurityService.hasRightOnTrustedSubjectForOneStudy(returnObject, 'CAN_SEE_ALL')")
	Subject findSubjectFromCenterCode(String centerCode);
	
	/**
	 * Save a subject.
	 *
	 * @param subject subject to create.
	 * @return created subject.
	 */
	@PreAuthorize("hasRole('ADMIN') or (hasAnyRole('EXPERT', 'USER') and @studySecurityService.checkRightOnEverySubjectStudyList(#subject.getSubjectStudyList(), 'CAN_IMPORT'))")
	Subject create(Subject subject);
	
	/**
	 * Save a subject and auto-increment the common name on using the centerId.
	 *
	 * @param subject subject to create.
	 * @return created subject.
	 */
	@PreAuthorize("hasRole('ADMIN') or (hasAnyRole('EXPERT', 'USER') and @studySecurityService.checkRightOnEverySubjectStudyList(#subject.getSubjectStudyList(), 'CAN_IMPORT'))")
	Subject createAutoIncrement(Subject subject, Long centerId);
	
	/**
	 * Update a subject.
	 *
	 * @param subject subject to update.
	 * @return updated subject.
	 * @throws EntityNotFoundException
	 * @throws MicroServiceCommunicationException
	 * @throws ShanoirException
	 * @throws RestServiceException
	 */
	@PreAuthorize("hasRole('ADMIN') or (hasAnyRole('EXPERT', 'USER') and @studySecurityService.checkRightOnEverySubjectStudyList(#subject.getSubjectStudyList(), 'CAN_IMPORT'))")
	Subject update(Subject subject) throws ShanoirException;

	/**
	 * Delete a subject.
	 *
	 * @param id subject id.
	 * @throws EntityNotFoundException
	 */
	@PreAuthorize("hasAnyRole('ADMIN', 'EXPERT') and @studySecurityService.hasRightOnSubjectForEveryStudy(#id, 'CAN_ADMINISTRATE')")
	void deleteById(Long id) throws EntityNotFoundException;


	/**
	 * This method should not be used directly. Same as findAllSubjectsOfStudy but with no roles
	 * @param studyId
	 * @return
	 */
	List<SimpleSubjectDTO> findAllSubjectsOfStudyId(Long studyId);

}

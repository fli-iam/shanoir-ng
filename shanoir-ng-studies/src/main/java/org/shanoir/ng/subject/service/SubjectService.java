package org.shanoir.ng.subject.service;

import java.util.List;

import org.shanoir.ng.shared.exception.EntityNotFoundException;
import org.shanoir.ng.shared.validation.UniqueCheckableService;
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
public interface SubjectService extends UniqueCheckableService<Subject> {

	@Override
	@PreAuthorize("hasAnyRole('ADMIN', 'EXPERT', 'USER')")
	@PostAuthorize("@studySecurityService.hasRightOnTrustedSubject(returnObject, 'CAN_SEE_ALL')")
	List<Subject> findBy(String fieldName, Object value);
	
	
	/**
	 * Get all the subjects.
	 *
	 * @return a list of subjects.
	 */
	@PreAuthorize("hasAnyRole('ADMIN', 'EXPERT', 'USER')")
	@PostFilter("hasRole('ADMIN') or @studySecurityService.hasRightOnTrustedSubject(filterObject, 'CAN_SEE_ALL')")
	List<Subject> findAll();

	
	/**
	 * Get all the subjects of a study
	 *
	 * @param studyId
	 * @return list of subjects
	 */
	@PreAuthorize("hasAnyRole('ADMIN', 'EXPERT', 'USER')")
	@PostFilter("hasRole('ADMIN') or @studySecurityService.hasRightOnSubject(filterObject.getId(), 'CAN_SEE_ALL')")
	public List<SimpleSubjectDTO> findAllSubjectsOfStudy(final Long studyId);
	
	/**
	 * Find subject by data.
	 *
	 * @param data data.
	 * @return a subject.
	 */
	@PreAuthorize("hasAnyRole('ADMIN', 'EXPERT', 'USER')")
	@PostAuthorize("@studySecurityService.hasRightOnTrustedSubject(returnObject, 'CAN_SEE_ALL')")
	Subject findByData(String data);

	/**
	 * Find subject by its id.
	 *
	 * @param id template id.
	 * @return a template or null.
	 */
	@PreAuthorize("hasAnyRole('ADMIN', 'EXPERT', 'USER')")
	@PostAuthorize("@studySecurityService.hasRightOnTrustedSubject(returnObject, 'CAN_SEE_ALL')")
	Subject findById(Long id);

	/**
	 * Find subject by its identifier.
	 *
	 * @param indentifier
	 * @return the subject or null
	 */
	@PreAuthorize("hasAnyRole('ADMIN', 'EXPERT', 'USER')")
	@PostAuthorize("@studySecurityService.hasRightOnTrustedSubject(returnObject, 'CAN_SEE_ALL')")
	Subject findByIdentifier(String indentifier);
	
	/**
	 * Find a subject by its subject-study relationship id.
	 * 
	 * @param id id
	 * @return a subject or null
	 */
	@PreAuthorize("hasAnyRole('ADMIN', 'EXPERT', 'USER')")
	@PostAuthorize("@studySecurityService.hasRightOnTrustedSubject(returnObject, 'CAN_SEE_ALL')")
	Subject findByIdWithSubjecStudies(Long subjectStudyId);

	/**
	 * Find a subject from a center code
	 * @param centerCode
	 * @return a subject or null
	 */
	@PreAuthorize("hasAnyRole('ADMIN', 'EXPERT', 'USER')")
	@PostAuthorize("@studySecurityService.hasRightOnTrustedSubject(returnObject, 'CAN_SEE_ALL')")
	Subject findSubjectFromCenterCode(String centerCode);
	
	
	/**
	 * Save a subject.
	 *
	 * @param subject subject to create.
	 * @return created subject.
	 */
	@PreAuthorize("hasRole('ADMIN') or (hasAnyRole('EXPERT', 'USER') and @studySecurityService.checkRightOnSubjectStudyList(#subject.getSubjectStudyList(), 'CAN_IMPORT'))")
	Subject create(Subject subject);
	
	
	
	/**
	 * Update a subject.
	 *
	 * @param subject subject to update.
	 * @return updated subject.
	 * @throws EntityNotFoundException 
	 */
	@PreAuthorize("hasRole('ADMIN')")
	Subject update(Subject subject) throws EntityNotFoundException;

	/**
	 * Delete a subject.
	 *
	 * @param id subject id.
	 * @throws EntityNotFoundException
	 */
	@PreAuthorize("hasAnyRole('ADMIN', 'EXPERT') and @studySecurityService.hasRightOnSubject(#id, 'CAN_ADMINISTRATE')")
	void deleteById(Long id) throws EntityNotFoundException;


}

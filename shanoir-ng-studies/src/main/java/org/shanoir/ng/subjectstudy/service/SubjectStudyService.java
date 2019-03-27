package org.shanoir.ng.subjectstudy.service;

import org.shanoir.ng.shared.exception.EntityNotFoundException;
import org.shanoir.ng.subjectstudy.model.SubjectStudy;
import org.springframework.security.access.prepost.PostAuthorize;
import org.springframework.security.access.prepost.PreAuthorize;

/**
 * Subject study service.
 *
 */
public interface SubjectStudyService {
	
	/**
	 * Find subject study by its id.
	 *
	 * @param id subject study id.
	 * @return a subject study or null.
	 */
	@PreAuthorize("hasAnyRole('ADMIN', 'EXPERT')")
	@PostAuthorize("hasPermission(returnObject.getStudy(), 'CAN_SEE_ALL')")
	SubjectStudy findById(Long id);
	
	/**
	 * Update subject study.
	 *
	 * @param subject study subject study to update.
	 * @return updated subject study.
	 * @throws EntityNotFoundException 
	 */
	@PreAuthorize("hasAnyRole('ADMIN', 'EXPERT') and (hasPermission(#subjectStudy.getStudy(), 'CAN_IMPORT') || hasPermission(#subjectStudy.getStudy(), 'CAN_ADMINISTRATE'))")
	SubjectStudy update(SubjectStudy subjectStudy) throws EntityNotFoundException;

}

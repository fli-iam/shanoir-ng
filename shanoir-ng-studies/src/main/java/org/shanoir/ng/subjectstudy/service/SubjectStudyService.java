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
	@PostAuthorize("@studySecurityService.hasRightOnTrustedStudy(returnObject.getStudy(), 'CAN_SEE_ALL')")
	SubjectStudy findById(Long id);
	
	/**
	 * Update subject study.
	 *
	 * @param subject study subject study to update.
	 * @return updated subject study.
	 * @throws EntityNotFoundException 
	 */
	@PreAuthorize("hasRole('ADMIN') or (hasRole('EXPERT') and (@studySecurityService.hasRightOnStudy(#subjectStudy.getStudy(), 'CAN_IMPORT') || @studySecurityService.hasRightOnStudy(#subjectStudy.getStudy(), 'CAN_ADMINISTRATE')))")
	SubjectStudy update(SubjectStudy subjectStudy) throws EntityNotFoundException;

}

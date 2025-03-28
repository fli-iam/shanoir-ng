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

package org.shanoir.ng.shared.service;

import java.util.List;

import org.shanoir.ng.shared.exception.EntityNotFoundException;
import org.shanoir.ng.shared.exception.MicroServiceCommunicationException;
import org.shanoir.ng.shared.model.SubjectStudy;
import org.springframework.security.access.prepost.PreAuthorize;

/**
 * Subject study service.
 *
 */
public interface SubjectStudyService {
    
    /**
     * Update subject study.
     *
     * @param subject study subject study to update.
     * @return updated subject study.
     * @throws EntityNotFoundException
     */
    @PreAuthorize("hasRole('ADMIN') or (hasAnyRole('EXPERT', 'USER') and (@datasetSecurityService.hasRightOnSubjectStudies(#subjectStudies, 'CAN_IMPORT') || @datasetSecurityService.hasRightOnSubjectStudies(#subjectStudies, 'CAN_ADMINISTRATE')))")
    List<SubjectStudy> update(Iterable<SubjectStudy> subjectStudies) throws EntityNotFoundException, MicroServiceCommunicationException;

    /**
     * get subject-studies.
     *
     * @param subjectId
     * @param studyId
     * @return subject studies
     * @throws EntityNotFoundException
     */
    @PreAuthorize("hasRole('ADMIN') or (hasAnyRole('EXPERT', 'USER') and (@datasetSecurityService.hasRightOnSubjectStudies(#subjectStudies, 'CAN_SEE_ALL') || @datasetSecurityService.hasRightOnSubjectStudies(#subjectStudies, 'CAN_ADMINISTRATE')))")
    List<SubjectStudy> get(Long subjectId, Long studyId);
}

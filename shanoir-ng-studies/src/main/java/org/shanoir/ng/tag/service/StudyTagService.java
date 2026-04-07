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

package org.shanoir.ng.tag.service;

import org.shanoir.ng.shared.exception.EntityNotFoundException;
import org.shanoir.ng.study.model.Study;
import org.shanoir.ng.tag.model.StudyTag;
import org.shanoir.ng.tag.model.StudyTagDTO;
import org.springframework.security.access.prepost.PreAuthorize;

public interface StudyTagService {

    @PreAuthorize("hasAnyRole('ADMIN', 'EXPERT') and @studySecurityService.hasRightOnStudy(#study.id, 'CAN_ADMINISTRATE') and @studySecurityService.studyUsersMatchStudy(#study)")
    StudyTag create(Study study, StudyTagDTO dto);

    @PreAuthorize("hasAnyRole('ADMIN', 'EXPERT') and @studySecurityService.hasRightOnStudyTag(#dto.id, 'CAN_ADMINISTRATE')")
    void update(StudyTagDTO dto) throws EntityNotFoundException;

    @PreAuthorize("hasAnyRole('ADMIN', 'EXPERT') and @studySecurityService.hasRightOnStudyTag(#id, 'CAN_ADMINISTRATE')")
    void delete(Long id);
}

package org.shanoir.ng.tag.service;

import org.shanoir.ng.shared.exception.EntityNotFoundException;
import org.shanoir.ng.study.model.Study;
import org.shanoir.ng.tag.model.StudyTag;
import org.shanoir.ng.tag.model.StudyTagDTO;
import org.springframework.security.access.prepost.PreAuthorize;

public interface StudyTagService {

    @PreAuthorize("hasAnyRole('ADMIN', 'EXPERT') and @studySecurityService.hasRightOnStudy(#study.id, 'CAN_ADMINISTRATE') and @studySecurityService.studyUsersMatchStudy(#study)")
    StudyTag create(Study study, StudyTagDTO dto);

    void update(StudyTagDTO dto) throws EntityNotFoundException;

    void delete(Long id);
}

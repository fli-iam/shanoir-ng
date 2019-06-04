package org.shanoir.ng.study.service;

import java.util.List;

import org.shanoir.ng.shared.exception.AccessDeniedException;
import org.shanoir.ng.shared.exception.EntityNotFoundException;
import org.shanoir.ng.study.model.Study;
import org.shanoir.ng.study.model.StudyUser;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

/**
 * Study service.
 *
 */
@Service
public interface StudyUserService {

	@PreAuthorize("hasAnyRole('ADMIN', 'EXPERT') and @studySecurityService.hasRightOnStudy(#study.id, 'CAN_ADMINISTRATE')")
	Study updateStudyUsers(Study study, List<StudyUser> studyUsers) throws EntityNotFoundException, AccessDeniedException;

}

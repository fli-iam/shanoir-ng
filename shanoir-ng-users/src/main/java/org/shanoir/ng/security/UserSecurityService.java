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

package org.shanoir.ng.security;

import java.util.Optional;

import org.shanoir.ng.accessrequest.model.AccessRequest;
import org.shanoir.ng.accessrequest.repository.AccessRequestRepository;
import org.shanoir.ng.shared.security.rights.StudyUserRight;
import org.shanoir.ng.study.rights.StudyUser;
import org.shanoir.ng.study.rights.StudyUserRightsRepository;
import org.shanoir.ng.utils.KeycloakUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import jakarta.persistence.EntityNotFoundException;

@Service
public class UserSecurityService {

    @Autowired
    private AccessRequestRepository accessRequestRepository;

    @Autowired
    private StudyUserRightsRepository studyUserRightsRepository;


    /**
     * Check that the connected user has the given right for the given access request linked study.
     *
     * @param accessRequestId
     *            the access request id
     * @param rightStr
     *            the right
     * @return true or false
     * @throws EntityNotFoundException
     */
    public boolean hasRightOnAccessRequest(Long accessRequestId, String rightStr) throws EntityNotFoundException {
        Optional<AccessRequest> accessRequest = accessRequestRepository.findById(accessRequestId);
        if (accessRequest.isPresent()) {
            return hasRightOnStudy(accessRequest.get().getStudyId(), rightStr);
        } else {
            throw new EntityNotFoundException("Cannot find access request with id " + accessRequestId);
        }
    }

    /**
     * Check that the connected user has the given right for the given study.
     *
     * @param studyId
     *            the study id
     * @param rightStr
     *            the right
     * @return true or false
     * @throws EntityNotFoundException
     */
    public boolean hasRightOnStudy(Long studyId, String rightStr) throws EntityNotFoundException {
        StudyUserRight right = StudyUserRight.valueOf(rightStr);
        Long userId = KeycloakUtil.getTokenUserId();
        if (userId == null) {
            throw new IllegalStateException("UserId should not be null. Cannot check rights on the study " + studyId);
        }
        StudyUser studyUser = studyUserRightsRepository.findByUserIdAndStudyId(userId, studyId);
        return studyUser != null && studyUser.getStudyUserRights().contains(right);
    }

}

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

package org.shanoir.ng.study.rights;

import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import org.shanoir.ng.shared.security.rights.StudyUserRight;
import org.shanoir.ng.utils.KeycloakUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

@Service
public class StudyRightsService {

    private static final Logger LOG = LoggerFactory.getLogger(StudyRightsService.class);

    @Autowired
    private StudyRightsCacheService cache;

    /**
     * Check that the connected user has the given right for the given study.
     *
     * @param studyId the study id
     * @param rightStr the right
     * @return true or false
     */
    public boolean hasRightOnStudy(Long studyId, String rightStr) {
        Long userId = KeycloakUtil.getTokenUserId();
        if (userId == null) {
            throw new IllegalStateException("UserId should not be null. Cannot check rights on the study " + studyId);
        }
        return cache.hasRightOnStudyCached(userId, studyId, rightStr);
    }

    public boolean hasRightOnCenter(Long studyId, Long centerId) {
        Long userId = KeycloakUtil.getTokenUserId();
        if (userId == null) {
            throw new IllegalStateException("UserId should not be null. Cannot check rights on the study " + studyId);
        }
        StudyUser founded = cache.findByUserIdAndStudyIdCached(userId, studyId);
        List<Long> centerIds = cache.findCenterIdsByStudyUserIdCached(founded.getId());
        founded.setCenterIds(centerIds);
        return
                founded != null
                &&
                (founded.getCenterIds().isEmpty() || founded.getCenterIds().contains(centerId));
    }

    /*
     * Checks that the user has at least the right on one study
     */
    public boolean hasRightOnCenter(Set<Long> studies, Long centerId) {
        Long userId = KeycloakUtil.getTokenUserId();
        if (userId == null) {
            throw new IllegalStateException("UserId should not be null. Cannot check rights");
        }
        List<StudyUser> founded = studies.stream()
                .map(studyId -> cache.findByUserIdAndStudyIdCached(userId, studyId))
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
        if (CollectionUtils.isEmpty(founded)) {
            return false;
        }
        boolean hasRight = false;
        for (StudyUser su  : founded) {
            List<Long> centerIds = cache.findCenterIdsByStudyUserIdCached(su.getId());
            su.setCenterIds(centerIds);
            hasRight = hasRight || CollectionUtils.isEmpty(su.getCenterIds()) || su.getCenterIds().contains(centerId);
        }
        return hasRight;
    }

    /**
     * Check that the connected user has one of the given rights for the given study.
     *
     * @param studyId the study id
     * @param rightStr the right
     * @return true or false
     */
    public boolean hasOneRightOnStudy(Long studyId, String... rightStrs) {
        Long userId = KeycloakUtil.getTokenUserId();
        if (userId == null) throw new IllegalStateException("UserId should not be null. Cannot check rights on the study " + studyId);
        StudyUser founded = cache.findByUserIdAndStudyIdCached(userId, studyId);
        if (founded != null && founded.getStudyUserRights() != null) {
            for (String rightStr : rightStrs) {
                if (founded.getStudyUserRights().contains(StudyUserRight.valueOf(rightStr)) && founded.isConfirmed()) return true;
            }
        }
        return false;
    }

    /**
     * Check that the connected user has the given right for the given studies.
     *
     * @param studyIds the study ids.
     * @param rightStr the right
     * @return ids that have the right, removes others.
     */
    public Set<Long> hasRightOnStudies(Set<Long> studyIds, String rightStr) {
        Long userId = KeycloakUtil.getTokenUserId();
        if (userId == null) {
            throw new IllegalStateException("UserId should not be null. Cannot check rights on the studies " + studyIds);
        }
        List<StudyUser> founded = studyIds.stream()
                .map(studyId -> cache.findByUserIdAndStudyIdCached(userId, studyId))
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
        Set<Long> validIds = new HashSet<>();
        if (founded != null) {
            for (StudyUser su : founded) {
                if (su.getStudyUserRights().contains(StudyUserRight.valueOf(rightStr)) && su.isConfirmed()) {
                    validIds.add(su.getStudyId());
                }
            }
        }
        return validIds;
    }

    /**
     * Check that the connected user has the given right for one study at least.
     *
     * @param rightStr
     * @return true or false
     */
    public boolean hasRightOnAtLeastOneStudy(String rightStr) {
        Long userId = KeycloakUtil.getTokenUserId();
        if (userId == null) {
            throw new IllegalStateException("UserId should not be null. Cannot check rights.");
        }
        Iterable<StudyUser> founded = cache.getUserRightsCached(userId);
        if (founded != null) {
            for (StudyUser su : founded) {
                if (su.getStudyUserRights().contains(StudyUserRight.valueOf(rightStr)) && su.isConfirmed()) {
                    return true;
                }
            }
        }
        return false;
    }

    public UserRights getUserRights() {
        Long userId = KeycloakUtil.getTokenUserId();
        List<StudyUser> studyUsers = cache.getUserRightsCached(userId);
        return new UserRights(studyUsers);
    }

}

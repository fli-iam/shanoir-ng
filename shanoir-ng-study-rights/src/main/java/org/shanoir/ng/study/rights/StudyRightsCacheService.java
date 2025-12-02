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

import java.util.Collections;
import java.util.List;

import org.shanoir.ng.shared.configuration.CacheNames;
import org.shanoir.ng.shared.security.rights.StudyUserRight;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

@Service
public class StudyRightsCacheService {

    private static final Logger LOG = LoggerFactory.getLogger(StudyRightsCacheService.class);

    @Autowired
    private StudyUserRightsRepository repo;

    @Cacheable(value = CacheNames.STUDY_RIGHTS, key = "#userId + '-' + #studyId + '-' + #rightStr")
    public boolean hasRightOnStudyCached(Long userId, Long studyId, String rightStr) {
        LOG.info("CACHE MISS - Database query executed for userId={}, studyId={}, right={}",
                 userId, studyId, rightStr);
        StudyUser founded = repo.findByUserIdAndStudyId(userId, studyId);
        return founded != null
                && founded.getStudyUserRights() != null
                && founded.getStudyUserRights().contains(StudyUserRight.valueOf(rightStr))
                && founded.isConfirmed();
    }

    @Cacheable(value = CacheNames.STUDY_USER, key = "#userId + '-' + #studyId")
    public StudyUser findByUserIdAndStudyIdCached(Long userId, Long studyId) {
        LOG.info("CACHE MISS - Database query executed for userId={}, studyId={}",
                userId, studyId);
        StudyUser studyUser = repo.findByUserIdAndStudyId(userId, studyId);
        studyUser.getCenterIds();
        return studyUser;
    }

    @Cacheable(value = CacheNames.USER_RIGHTS, key = "#userId")
    public List<StudyUser> getUserRightsCached(Long userId) {
        LOG.info("CACHE MISS - Database query executed for userId={}", userId);
        List<StudyUser> studyUsers = repo
                .findAllByUserId(userId)
                .orElseGet(Collections::emptyList);
        studyUsers.stream().forEach(su -> su.getCenterIds());
        return studyUsers;
    }

    @Cacheable(value = CacheNames.STUDY_USER_CENTER_IDS, key = "#studyUserId")
    public List<Long> findCenterIdsByStudyUserIdCached(Long studyUserId) {
        LOG.info("CACHE MISS - Database query executed for studyUserId={}", studyUserId);
        return repo.findCenterIdsByStudyUserId(studyUserId);
    }

}

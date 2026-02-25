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

package org.shanoir.ng.study.security;

import java.util.List;

import org.shanoir.ng.shared.configuration.CacheNames;
import org.shanoir.ng.study.model.StudyUser;
import org.shanoir.ng.study.repository.StudyUserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import jakarta.transaction.Transactional;

@Service
public class StudySecurityCacheService {

    private static final Logger LOG = LoggerFactory.getLogger(StudySecurityCacheService.class);

    @Autowired
    private StudyUserRepository repo;

    @Cacheable(value = CacheNames.USER_ID_STUDY_ID, key = "#userId + '-' + #studyId")
    @Transactional
    public StudyUser findByUserIdAndStudyIdCached(Long userId, Long studyId) {
        LOG.info("Cache miss - query executed for userId={}, studyId={}",
                userId, studyId);
        StudyUser studyUser = repo.findByUserIdAndStudy_Id(userId, studyId);
        if (studyUser == null) {
            return null;
        }
        return studyUser;
    }

    @Cacheable(value = CacheNames.USER_ID_RIGHTS, key = "#userId")
    @Transactional
    public List<StudyUser> getUserRightsCached(Long userId) {
        LOG.info("Cache miss - query executed for userId={}", userId);
        return repo.findByUserId(userId);
    }

}

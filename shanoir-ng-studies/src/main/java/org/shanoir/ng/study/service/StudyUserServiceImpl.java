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

package org.shanoir.ng.study.service;

import java.util.ArrayList;
import java.util.List;

import org.shanoir.ng.shared.security.rights.StudyUserRight;
import org.shanoir.ng.study.model.StudyUser;
import org.shanoir.ng.study.repository.StudyUserRepository;
import org.shanoir.ng.utils.KeycloakUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Implementation of study service.
 * 
 * @author msimon
 *
 */
@Component
public class StudyUserServiceImpl implements StudyUserService {

	private static final Logger LOG = LoggerFactory.getLogger(StudyUserServiceImpl.class);
	
	@Autowired
	private StudyUserRepository studyUserRepository;

	@Override
	public List<StudyUserRight> getRightsForStudy(Long studyId) {
		Long userId = KeycloakUtil.getTokenUserId();
		StudyUser studyUser = studyUserRepository.findByUserIdAndStudy_Id(userId, studyId);
		return studyUser.getStudyUserRights();
	}

	
}

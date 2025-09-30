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

import org.shanoir.ng.shared.model.Study;
import org.shanoir.ng.shared.repository.StudyRepository;
import org.shanoir.ng.shared.security.rights.StudyUserRight;
import org.shanoir.ng.study.rights.StudyUser;
import org.shanoir.ng.study.rights.StudyUserRightsRepository;
import org.shanoir.ng.utils.KeycloakUtil;
import org.shanoir.ng.utils.Utils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.Set;

@Service
public class SecurityService {

	@Autowired
	StudyUserRightsRepository rightsRepository;

	@Autowired
	StudyRepository studyRepository;

	/**
	 * Get study center rights for the current user as two separate variable.
	 *
	 * @param studyCenterIds is to be populated with the current user's study-centers
	 * @param unrestrictedStudies is to be populated with the current user's unrestricted studies
	 */
	public void getStudyCentersAndUnrestrictedStudies(List<Pair<Long, Long>> studyCenters, Set<Long> unrestrictedStudies) {
		Long userId = KeycloakUtil.getTokenUserId();
		// Check if user has restrictions.
		if (KeycloakUtil.getTokenRoles().contains("ROLE_ADMIN")) {
			for (Study stud : studyRepository.findAll()) {
				unrestrictedStudies.add(stud.getId());
			}
		} else {
			List<StudyUser> studyUsers = Utils.toList(rightsRepository.findByUserIdAndRight(userId, StudyUserRight.CAN_SEE_ALL.getId()));
			for (StudyUser studyUser : studyUsers) {
				if (CollectionUtils.isEmpty(studyUser.getCenterIds()) && studyUser.isConfirmed()) {
					unrestrictedStudies.add(studyUser.getStudyId());
				} else {
					for (Long centerId : studyUser.getCenterIds()) {
						studyCenters.add(Pair.of(studyUser.getStudyId(), centerId));
					}
				}
			}
		}
	}
}
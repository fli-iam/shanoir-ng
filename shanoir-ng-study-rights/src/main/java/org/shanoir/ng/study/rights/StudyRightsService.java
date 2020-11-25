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
import java.util.Set;

import org.shanoir.ng.shared.security.rights.StudyUserRight;
import org.shanoir.ng.utils.KeycloakUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class StudyRightsService {
		
	@Autowired
	private StudyUserRightsRepository repo;
	
	
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
		StudyUser founded = repo.findByUserIdAndStudyId(userId, studyId);
		return
				founded.getStudyUserRights() != null
				&& founded.getStudyUserRights().contains(StudyUserRight.valueOf(rightStr));
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
		StudyUser founded = repo.findByUserIdAndStudyId(userId, studyId);
		if (founded.getStudyUserRights() != null) {
			for (String rightStr : rightStrs) {
				if (founded.getStudyUserRights().contains(StudyUserRight.valueOf(rightStr))) return true;
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
		Iterable<StudyUser> founded = repo.findByUserIdAndStudyIdIn(userId, studyIds);
		Set<Long> validIds = new HashSet<>();
		for (StudyUser su : founded) {
			if (su.getStudyUserRights().contains(StudyUserRight.valueOf(rightStr))) {
				validIds.add(su.getStudyId());
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
		Iterable<StudyUser> founded = repo.findByUserId(userId);
		for (StudyUser su : founded) {
			if (su.getStudyUserRights().contains(StudyUserRight.valueOf(rightStr))) {
				return true;
			}
		}
		return false;
	}
    

}
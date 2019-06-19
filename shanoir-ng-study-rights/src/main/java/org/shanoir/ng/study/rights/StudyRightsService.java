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
		if (userId == null) throw new IllegalStateException("UserId should not be null. Cannot check rights on the study " + studyId);
		StudyUser founded = repo.findByUserIdAndStudyId(userId, studyId);
		return 
				founded.getStudyUserRights() != null 
				&& founded.getStudyUserRights().contains(StudyUserRight.valueOf(rightStr));
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
		if (userId == null) throw new IllegalStateException("UserId should not be null. Cannot check rights on the studies " + studyIds);
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
		if (userId == null) throw new IllegalStateException("UserId should not be null. Cannot check rights.");
		Iterable<StudyUser> founded = repo.findByUserId(userId);
		for (StudyUser su : founded) {
			if (su.getStudyUserRights().contains(StudyUserRight.valueOf(rightStr))) {
				return true;
			}
		}
		return false;	
	}
    

}
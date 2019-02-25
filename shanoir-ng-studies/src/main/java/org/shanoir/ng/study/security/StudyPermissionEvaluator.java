package org.shanoir.ng.study.security;

import java.io.Serializable;

import org.shanoir.ng.study.model.Study;
import org.shanoir.ng.study.repository.StudyRepository;
import org.shanoir.ng.studyuser.StudyUser;
import org.shanoir.ng.studyuser.StudyUserRight;
import org.shanoir.ng.utils.KeycloakUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.PermissionEvaluator;
import org.springframework.security.core.Authentication;

public class StudyPermissionEvaluator implements PermissionEvaluator {
	
	@Autowired
	private StudyRepository studyRepository;
	
    @Override
    public boolean hasPermission(
      Authentication auth, Object targetDomainObject, Object permission) {
        if ((auth == null) || (targetDomainObject == null) || !(permission instanceof StudyUserRight)){
            return false;
        }         
        Study study = (Study) targetDomainObject;
        StudyUserRight neededRight = (StudyUserRight) permission;
        return hasPrivilege(study, neededRight);
    }
 
    @Override
    public boolean hasPermission(
      Authentication auth, Serializable targetId, String targetType, Object permission) {
        if ((auth == null) || (targetType == null) || !(permission instanceof StudyUserRight)) {
            return false;
        }
        Study study = studyRepository.findOne((Long)targetId);
        if (study == null) return true; 
        StudyUserRight neededRight = (StudyUserRight) permission;
        return hasPrivilege(study, neededRight);
    }
    
    private boolean hasPrivilege(Study study, StudyUserRight neededRight) {
    	if (KeycloakUtil.getTokenRoles().contains("ROLE_ADMIN")) return true;
		Long userId = KeycloakUtil.getTokenUserId();
		StudyUser studyUser = study.getStudyUserList().stream()
			.filter(su -> userId.equals(su.getUserId()))
			.findAny().orElse(null);
		if (studyUser == null) return false;
		return neededRight.equals(studyUser.getStudyUserType());
    }
}
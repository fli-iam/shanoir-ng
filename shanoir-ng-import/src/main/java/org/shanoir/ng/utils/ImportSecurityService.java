package org.shanoir.ng.utils;

import org.shanoir.ng.study.rights.StudyRightsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

@Service
public class ImportSecurityService {
	
	@Autowired
	StudyRightsService rightsService;
		
	
	/**
	 * Check that the connected user has the given right for the given study.
	 * 
	 * @param studyId the study id
	 * @param rightStr the right
	 * @return true or false
	 */
    public boolean hasRightOnStudy(Long studyId, String rightStr) {
    	if (KeycloakUtil.getTokenRoles().contains("ROLE_ADMIN")) return true;
    	if (studyId == null) return false;
        return rightsService.hasRightOnStudy(studyId, rightStr);
    }
    
    /**
	 * Check that the connected user has the given right for at least one study.
	 * 
	 * @param rightStr the right
	 * @return true or false
	 */
    public boolean hasRightOnOneStudy(String rightStr) {
    	if (KeycloakUtil.getTokenRoles().contains("ROLE_ADMIN")) return true;
        return rightsService.hasRightOnAtLeastOneStudy(rightStr);
    }
}
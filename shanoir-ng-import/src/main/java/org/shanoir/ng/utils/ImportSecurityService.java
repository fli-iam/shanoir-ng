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

package org.shanoir.ng.utils;

import org.shanoir.ng.study.rights.StudyRightsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ImportSecurityService {
	
	private static final String ROLE_ADMIN = "ROLE_ADMIN";
	
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
    	if (KeycloakUtil.getTokenRoles().contains(ROLE_ADMIN)) {
			return true;
		} else if (studyId == null) {
			return false;
		} else {
			return rightsService.hasRightOnStudy(studyId, rightStr);
		}
    }
    
    /**
	 * Check that the connected user has the given right for at least one study.
	 * 
	 * @param rightStr the right
	 * @return true or false
	 */
    public boolean hasRightOnOneStudy(String rightStr) {
    	if (KeycloakUtil.getTokenRoles().contains(ROLE_ADMIN)) {
			return true;
		}
        return rightsService.hasRightOnAtLeastOneStudy(rightStr);
    }
    
    /**
	 * Know if connected user can import from PACS
	 * 
	 * @return a boolean
	 */
    public boolean canImportFromPACS() {
    	if (KeycloakUtil.getTokenRoles().contains(ROLE_ADMIN)) {
			return true;
		}
    	return KeycloakUtil.canImportFromPACS();
    }
}
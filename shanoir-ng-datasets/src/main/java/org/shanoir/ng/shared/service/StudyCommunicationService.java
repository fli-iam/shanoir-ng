package org.shanoir.ng.shared.service;

import java.util.List;
import java.util.Set;

import org.shanoir.ng.utils.KeycloakUtil;
import org.springframework.stereotype.Service;

@Service
public class StudyCommunicationService {
	
	
	/**
	 * Check that the connected user has the given right for the given study.
	 * 
	 * @param studyId the study id
	 * @param rightStr the right
	 * @return true or false
	 */
    public boolean hasRightOnStudy(Long studyId, String rightStr) {
    	
    	
		Long userId = KeycloakUtil.getTokenUserId();
    	
    	// TODO : send request to the Study MS ?
    	
        return true;
    }

    /**
     * Check that the connected user has the given right for the given studies.
     * 
     * @param studyIds the study ids.
     * @param rightStr the right
     * @return ids that have the right, removes others.
     */
	public Set<Long> hasRightOnStudies(Set<Long> studyIds, String rightStr) {
		
		// TODO : send request to the Study MS ?
		
		return studyIds;
	}
    

}
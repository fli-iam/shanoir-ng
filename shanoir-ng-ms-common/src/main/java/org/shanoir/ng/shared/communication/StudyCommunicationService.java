package org.shanoir.ng.shared.communication;

import java.util.Set;

import org.shanoir.ng.utils.KeycloakUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class StudyCommunicationService {
	
	@Autowired
	private RestTemplate restTemplate;
	
	@Value("${ms.url.shanoir-ng-studies}")
	private String studiesMsUrl;
	
	
	/**
	 * Check that the connected user has the given right for the given study.
	 * 
	 * @param studyId the study id
	 * @param rightStr the right
	 * @return true or false
	 */
    public boolean hasRightOnStudy(Long studyId, String rightStr) {
		HttpEntity<Long> entity = new HttpEntity<>(KeycloakUtil.getKeycloakHeader());
		ResponseEntity<Void> response = restTemplate.exchange(studiesMsUrl + "/studyUser/hasRightOnStudy/" + studyId, HttpMethod.GET, entity, Void.class);
    	return response.getStatusCode() == HttpStatus.NO_CONTENT;
    }

    /**
     * Check that the connected user has the given right for the given studies.
     * 
     * @param studyIds the study ids.
     * @param rightStr the right
     * @return ids that have the right, removes others.
     */
	public Set<Long> hasRightOnStudies(Set<Long> studyIds, String rightStr) {
		HttpEntity<Long> entity = new HttpEntity<>(KeycloakUtil.getKeycloakHeader());
		ResponseEntity<Set<Long>> response = restTemplate.exchange(studiesMsUrl + "/studyUser/hasRightOnStudies/" + studyIds, 
				HttpMethod.GET, entity, new ParameterizedTypeReference<Set<Long>>() {});
		return response.getBody();
	}

	/**
	 * Check that the connected user has the given right for one study at least.
	 * 
	 * @param rightStr
	 * @return true or false
	 */
	public boolean hasRightOnAtLeastOneStudy(String rightStr) {
		HttpEntity<Long> entity = new HttpEntity<>(KeycloakUtil.getKeycloakHeader());
		ResponseEntity<Void> response = restTemplate.exchange(studiesMsUrl + "/studyUser/hasRightOnOneStudy", HttpMethod.GET, entity, Void.class);
    	return response.getStatusCode() == HttpStatus.NO_CONTENT;
	}
    

}
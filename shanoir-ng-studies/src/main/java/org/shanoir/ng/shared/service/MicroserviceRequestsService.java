package org.shanoir.ng.shared.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * Service used to generate microservices requests.
 * 
 * @author msimon
 *
 */
@Service
public class MicroserviceRequestsService {

	/**
	 * Search request part.
	 */
	public static final String SEARCH = "/search";
	
	@Value("${ms.url.shanoir-ng-studycard}")
	private String studycardMsUrl;

	/**
	 * @return the shanoirNgStudycardUrl
	 */
	public String getStudycardMsUrl() {
		return studycardMsUrl;
	}

}

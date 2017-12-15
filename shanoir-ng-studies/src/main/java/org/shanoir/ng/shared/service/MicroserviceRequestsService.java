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
	
	public static final String CENTERID = "/centerid";
	
	@Value("${ms.url.shanoir-ng-studycards}")
	private String studycardsMsUrl;

	@Value("${ms.url.shanoir-ng-users}")
	private String usersMsUrl;

	/**
	 * @return the studycardsMsUrl
	 */
	public String getStudycardsMsUrl() {
		return studycardsMsUrl;
	}

	/**
	 * @return the usersMsUrl
	 */
	public String getUsersMsUrl() {
		return usersMsUrl;
	}

}

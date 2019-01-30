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
	
	
	@Value("${ms.url.shanoir-ng-studies}")
	private String studiesMsUrl;

	/**
	 * @return the studiesMsUrl
	 */
	public String getStudiesMsUrl() {
		return studiesMsUrl;
	}

}

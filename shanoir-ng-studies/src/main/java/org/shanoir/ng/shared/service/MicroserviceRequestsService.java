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
	
	public static final String SUBJECT = "/subject/";
	
	public static final String STUDY = "/study/";
	
	
	@Value("${ms.url.shanoir-ng-studycards}")
	private String studycardsMsUrl;

	@Value("${ms.url.shanoir-ng-users}")
	private String usersMsUrl;
	
	@Value("${ms.url.shanoir-ng-import}")
	private String importMsUrl;
	
	@Value("${ms.url.shanoir-ng-examinations}")
	private String examinationsMsUrl;

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

	public String getImportMsUrl() {
		return importMsUrl;
	}
	
	public String getExaminationMsUrl() {
		return examinationsMsUrl;
	}

	public void setImportMsUrl(String importMsUrl) {
		this.importMsUrl = importMsUrl;
	}

}

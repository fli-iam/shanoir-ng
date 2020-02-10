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
	
	public static final String SUBJECT_NAME = "/subjectName/";
	
	public static final String STUDY_NAME = "/studyName/";
	
	public static final String STUDY_ID = "/studyId/";
	
	@Value("${ms.url.shanoir-ng-studycards}")
	private String studycardsMsUrl;
	
	@Value("${ms.url.shanoir-ng-examinations}")
	private String examinationsMsUrl;

	@Value("${ms.url.shanoir-ng-bids}")
	private String bidsMsUrl;
	
	/**
	 * @return the studycardsMsUrl
	 */
	public String getStudycardsMsUrl() {
		return studycardsMsUrl;
	}
	
	public String getExaminationMsUrl() {
		return examinationsMsUrl;
	}

	/**
	 * @return the bidsMsUrl
	 */
	public String getBidsMsUrl() {
		return bidsMsUrl;
	}
	
}

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
	public static final String SUBJECT = "/subject";
	public static final String STUDY = "/studies";
	public static final String CENTER = "/centers";

	public static final String COMMON = "/common";

	public static final String CENTERID = "/centerid";

	@Value("${ms.url.shanoir-ng-studies}")
	private String studiesMsUrl;

	/**
	 * @return the studiesMsUrl
	 */
	public String getStudiesMsUrl() {
		return studiesMsUrl;
	}
}

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

package org.shanoir.ng.subject.dto.mapper;

import java.text.DecimalFormat;

import org.shanoir.ng.shared.exception.MicroServiceCommunicationException;
import org.shanoir.ng.shared.service.MicroserviceRequestsService;
import org.shanoir.ng.study.service.StudyService;
import org.shanoir.ng.subject.model.Subject;
import org.shanoir.ng.subject.service.SubjectService;
import org.shanoir.ng.utils.KeycloakUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

// TODO : Implement those mappings into SubjectMapper ?
@Service
public class SubjectMappingUtilsService {
	
	@Autowired
	StudyService studyService;
	
	@Autowired
	SubjectService subjectService;
	
	@Autowired
	private RestTemplate restTemplate;
	
	@Autowired
	private MicroserviceRequestsService microservicesRequestsService;
	
	/**
	 * Get the OFSEP name from a study card
	 * 
	 * @param studyCardId
	 * @return
	 * @throws MicroServiceCommunicationException
	 */
	public String getOfsepCommonName(Long studyCardId) throws MicroServiceCommunicationException {
		Long idCenter = getCenterIdFromStudyCard(studyCardId);
		DecimalFormat formatterCenter = new DecimalFormat("000");
		String commonNameCenter = formatterCenter.format(idCenter);

		Subject subjectOfsepCommonNameMaxFoundByCenter = subjectService.findSubjectFromCenterCode(commonNameCenter);
		int maxCommonNameNumber = 0;

		if (subjectOfsepCommonNameMaxFoundByCenter != null) {
			String maxNameToIncrement = subjectOfsepCommonNameMaxFoundByCenter.getName().substring(3);
			maxCommonNameNumber = Integer.parseInt(maxNameToIncrement);
		}
		maxCommonNameNumber += 1;
		DecimalFormat formatterSubject = new DecimalFormat("0000");
		return commonNameCenter + formatterSubject.format(maxCommonNameNumber);
	}
	
	private Long getCenterIdFromStudyCard(Long studyCardId) throws MicroServiceCommunicationException {
		HttpEntity<Long> entity = new HttpEntity<>(KeycloakUtil.getKeycloakHeader());
		// Request to studycard MS to get center id
		ResponseEntity<Long> centerIdResponse = restTemplate.exchange(microservicesRequestsService.getStudycardsMsUrl()
					+ MicroserviceRequestsService.CENTERID + "/" + studyCardId, HttpMethod.GET, entity, Long.class);

		Long centerId = null;
		if (HttpStatus.OK.equals(centerIdResponse.getStatusCode())
				|| HttpStatus.NO_CONTENT.equals(centerIdResponse.getStatusCode())) {
			centerId = centerIdResponse.getBody();
		} else {
			throw new MicroServiceCommunicationException(
					"Cannot get the center id from the study card microservice (response status : " 
					+ centerIdResponse.getStatusCode() + " )");
		}
		return centerId;
	}
}

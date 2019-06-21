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

package org.shanoir.ng.examination.dto.mapper;

import org.shanoir.ng.examination.dto.ExaminationDTO;
import org.shanoir.ng.examination.dto.StudyIdsDTO;
import org.shanoir.ng.examination.dto.StudySubjectCenterNamesDTO;
import org.shanoir.ng.examination.model.Examination;
import org.shanoir.ng.shared.core.model.IdName;
import org.shanoir.ng.shared.paging.PageImpl;
import org.shanoir.ng.shared.service.MicroserviceRequestsService;
import org.shanoir.ng.utils.KeycloakUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

/**
 * Decorator for examinations mapper.
 * 
 * @author msimon
 *
 */
public abstract class ExaminationDecorator implements ExaminationMapper {

	/**
	 * Logger
	 */
	private static final Logger LOG = LoggerFactory.getLogger(ExaminationDecorator.class);

	@Autowired
	private ExaminationMapper delegate;

	@Autowired
	private MicroserviceRequestsService microservicesRequestsService;

	@Autowired
	private RestTemplate restTemplate;

	@Override
	public PageImpl<ExaminationDTO> examinationsToExaminationDTOs(Page<Examination> page) {

		Page<ExaminationDTO> mappedPage = page.map(new Converter<Examination, ExaminationDTO>() {
			@Override
			public ExaminationDTO convert(Examination entity) {
				return examinationToExaminationDTO(entity);
			}
		});
		return new PageImpl<ExaminationDTO>(mappedPage);
	}

	@Override
	public ExaminationDTO examinationToExaminationDTO(Examination examination) {
		final ExaminationDTO examinationDTO = delegate.examinationToExaminationDTO(examination);

		final StudyIdsDTO studyIds = new StudyIdsDTO();
		studyIds.setStudyId(examination.getStudyId());
		studyIds.setSubjectId(examination.getSubjectId());
		studyIds.setCenterId(examination.getCenterId());

		HttpEntity<StudyIdsDTO> entity = new HttpEntity<>(studyIds, KeycloakUtil.getKeycloakHeader());

		// Request to study MS to get study name, subject name and center name
		ResponseEntity<StudySubjectCenterNamesDTO> namesResponse = null;
		try {
			namesResponse = restTemplate.exchange(
					microservicesRequestsService.getStudiesMsUrl() + MicroserviceRequestsService.COMMON, HttpMethod.POST,
					entity, new ParameterizedTypeReference<StudySubjectCenterNamesDTO>() {
					});
		} catch (RestClientException e) {
			LOG.error("Error on study microservice request - " + e.getMessage());
		}

		if (namesResponse != null) {
			StudySubjectCenterNamesDTO names = null;
			if (HttpStatus.OK.equals(namesResponse.getStatusCode())
					|| HttpStatus.NO_CONTENT.equals(namesResponse.getStatusCode())) {
				names = namesResponse.getBody();
			} else {
				LOG.error("Error on study microservice response - status code: " + namesResponse.getStatusCode());
			}

			if (names != null) {
				if (names.getStudy() != null) {
					examinationDTO.setStudy(new IdName(studyIds.getStudyId(), names.getStudy().getName()));
				}

				examinationDTO.setSubject(names.getSubject());

				if (names.getCenter() != null) {
					examinationDTO.setCenter(new IdName(studyIds.getCenterId(), names.getCenter().getName()));
				}
			}
		}

		return examinationDTO;
	}

}

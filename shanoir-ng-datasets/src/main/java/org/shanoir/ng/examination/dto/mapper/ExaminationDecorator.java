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
import org.shanoir.ng.examination.model.Examination;
import org.shanoir.ng.shared.core.model.IdName;
import org.shanoir.ng.shared.model.Center;
import org.shanoir.ng.shared.model.Study;
import org.shanoir.ng.shared.model.Subject;
import org.shanoir.ng.shared.paging.PageImpl;
import org.shanoir.ng.shared.repository.CenterRepository;
import org.shanoir.ng.shared.repository.StudyRepository;
import org.shanoir.ng.shared.repository.SubjectRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.domain.Page;

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
	private CenterRepository centerRepository;
	
	@Autowired
	private StudyRepository studyRepository;
	
	@Autowired
	private SubjectRepository subjectRepository;

	@Autowired
	private ExaminationMapper delegate;

	@Override
	public PageImpl<ExaminationDTO> examinationsToExaminationDTOs(Page<Examination> page) {

		Page<ExaminationDTO> mappedPage = page.map(new Converter<Examination, ExaminationDTO>() {
			@Override
			public ExaminationDTO convert(Examination entity) {
				return examinationToExaminationDTO(entity);
			}
		});
		return new PageImpl<>(mappedPage);
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
			LOG.error("Error on study microservice request - {}", e.getMessage());
		}

		if (namesResponse != null) {
			StudySubjectCenterNamesDTO names = null;
			if (HttpStatus.OK.equals(namesResponse.getStatusCode())
					|| HttpStatus.NO_CONTENT.equals(namesResponse.getStatusCode())) {
				names = namesResponse.getBody();
			} else {
				LOG.error("Error on study microservice response - status code: {}", namesResponse.getStatusCode());
			}
		}
		
		if (examination.getSubjectId() != null) {
			final Subject subject = subjectRepository.findOne(examination.getSubjectId());
			if (subject != null) {
				examinationDTO.setSubject(new IdName(examination.getSubjectId(), subject.getName()));
			}
		}

		return examinationDTO;
	}

}

package org.shanoir.ng.examination;

import org.shanoir.ng.shared.dto.IdNameDTO;
import org.shanoir.ng.shared.exception.ShanoirException;
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

		HttpEntity<StudyIdsDTO> entity = null;
		try {
			entity = new HttpEntity<>(studyIds, KeycloakUtil.getKeycloakHeader());
		} catch (ShanoirException e) {
			LOG.error("Error on keycloak request - " + e.getMessage());
		}

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
					
					examinationDTO.setStudy(new IdNameDTO(studyIds.getStudyId(), names.getStudy().getName()));
				}

				examinationDTO.setSubject(names.getSubject());

				if (names.getCenter() != null) {
					examinationDTO.setCenter(new IdNameDTO(studyIds.getCenterId(), names.getCenter().getName()));
				}
			}
		}

		return examinationDTO;
	}

}

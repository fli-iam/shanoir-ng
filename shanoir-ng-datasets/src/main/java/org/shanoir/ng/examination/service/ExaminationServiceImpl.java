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

package org.shanoir.ng.examination.service;

import java.util.ArrayList;
import java.util.List;

import org.shanoir.ng.examination.dto.ExaminationDTO;
import org.shanoir.ng.examination.dto.mapper.ExaminationMapper;
import org.shanoir.ng.examination.model.Examination;
import org.shanoir.ng.examination.repository.ExaminationRepository;
import org.shanoir.ng.shared.core.model.IdName;
import org.shanoir.ng.shared.exception.EntityNotFoundException;
import org.shanoir.ng.shared.service.MicroserviceRequestsService;
import org.shanoir.ng.utils.KeycloakUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

/**
 * Examination service implementation.
 * 
 * @author ifakhfakh
 *
 */
@Service
public class ExaminationServiceImpl implements ExaminationService {

	/**
	 * Logger
	 */
	private static final Logger LOG = LoggerFactory.getLogger(ExaminationServiceImpl.class);

	@Autowired
	private ExaminationRepository examinationRepository;

	@Autowired
	private MicroserviceRequestsService microservicesRequestsService;


	@Autowired
	private RestTemplate restTemplate;
	
	@Autowired
	private ExaminationMapper examinationMapper;
	
	@Override
	public void deleteById(final Long id) throws EntityNotFoundException {
		examinationRepository.delete(id);
	}

	@Override
	public Page<Examination> findPage(final Pageable pageable) {
		// Get list of studies reachable by connected user
		return examinationRepository.findByStudyIdIn(getStudiesForUser(), pageable);
	}

	@Override
	public List<Examination> findBySubjectId(final Long subjectId) {
		return examinationRepository.findBySubjectId(subjectId);
	}

	@Override
	public Examination findById(final Long id) {
		return examinationRepository.findOne(id);
	}

	@Override
	public Examination save(final Examination examination) {
		Examination savedExamination = null;
		savedExamination = examinationRepository.save(examination);
		return savedExamination;
	}
	
	@Override
	public Examination save(final ExaminationDTO examinationDTO) {
		return save(examinationMapper.examinationDTOToExamination(examinationDTO));
	}

	@Override
	public Examination update(final Examination examination) throws EntityNotFoundException {
		final Examination examinationDb = examinationRepository.findOne(examination.getId());
		if (examinationDb == null) throw new EntityNotFoundException(Examination.class, examination.getId());
		updateExaminationValues(examinationDb, examination);
		examinationRepository.save(examinationDb);
		return examinationDb;
	}

	/**
	 * Get list of studies reachable by connected user.
	 * 
	 * @return list of study ids.
	 */
	private List<Long> getStudiesForUser() {
		HttpEntity<Object> entity = null;
		entity = new HttpEntity<>(KeycloakUtil.getKeycloakHeader());

		// Request to study MS to get list of studies reachable by connected user
		ResponseEntity<IdName[]> response = null;
		try {
			response = restTemplate.exchange(
					microservicesRequestsService.getStudiesMsUrl() + MicroserviceRequestsService.STUDY, HttpMethod.GET,
					entity, IdName[].class);
		} catch (RestClientException e) {
			LOG.error("Error on study microservice request - " + e.getMessage());
		}

		final List<Long> studyIds = new ArrayList<>();
		if (response != null) {
			IdName[] studies = null;
			if (HttpStatus.OK.equals(response.getStatusCode())
					|| HttpStatus.NO_CONTENT.equals(response.getStatusCode())) {
				studies = response.getBody();
			} else {
				LOG.error("Error on study microservice response - status code: " + response.getStatusCode());
			}

			if (studies != null) {
				for (IdName idNameDTO : studies) {
					studyIds.add(idNameDTO.getId());
				}
			}
		}
		return studyIds;
	}
	

	/**
	 * Update some values of examination to save them in database.
	 * 
	 * @param examinationDb examination found in database.
	 * @param examination examination with new values.
	 * @return database examination with new values.
	 */
	private Examination updateExaminationValues(final Examination examinationDb, final Examination examination) {

		examinationDb.setCenterId(examination.getCenterId());
		examinationDb.setComment(examination.getComment());
		// examinationDb.setDatasetAcquisitionList(examination.getDatasetAcquisitionList());
		// examinationDb.setExperimentalGroupOfSubjectsId(examination.getExperimentalGroupOfSubjectsId());
		examinationDb.setExaminationDate(examination.getExaminationDate());
		// examinationDb.setExtraDataFilePathList(examination.getExtraDataFilePathList());
		// examinationDb.setInstrumentBasedAssessmentList(examination.getInstrumentBasedAssessmentList());
		// examinationDb.setInvestigatorExternal(examination.isInvestigatorExternal());
		// examinationDb.setInvestigatorCenterId(examination.getInvestigatorCenterId());
		// examinationDb.setInvestigatorId(examination.getInvestigatorId());
		examinationDb.setNote(examination.getNote());
		examinationDb.setStudyId(examination.getStudyId());
		// examinationDb.setSubjectId(examination.getSubjectId());
		examinationDb.setSubjectWeight(examination.getSubjectWeight());
		// examinationDb.setTimepoint(examination.getTimepoint());
		// examinationDb.setWeightUnitOfMeasure(examination.getWeightUnitOfMeasure());
		return examinationDb;
	}

	@Override
	public List<Examination> findBySubjectIdStudyId(Long subjectId, Long studyId) {
		return examinationRepository.findBySubjectIdAndStudyId(subjectId, studyId);
	}

	@Override
	public Page<Examination> findPreclinicalPage(final boolean isPreclinical, final Pageable pageable) {
		// Get list of studies reachable by connected user
		return examinationRepository.findByStudyIdInAndPreclinical(getStudiesForUser(), isPreclinical, pageable);
	}

}

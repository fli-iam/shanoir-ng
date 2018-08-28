package org.shanoir.ng.examination;

import java.util.ArrayList;
import java.util.List;

import org.shanoir.ng.shared.dto.IdNameDTO;
import org.shanoir.ng.shared.exception.ShanoirDatasetsException;
import org.shanoir.ng.shared.exception.ShanoirException;
import org.shanoir.ng.shared.service.MicroserviceRequestsService;
import org.shanoir.ng.utils.KeycloakUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
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

	@Override
	public long countExaminationsByUserId() throws ShanoirDatasetsException {
		return examinationRepository.countByStudyIdIn(getStudiesForUser());
	}
	
	@Override
	public void deleteById(final Long id) throws ShanoirDatasetsException {
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
	public Examination findById(final Long id) throws ShanoirDatasetsException {
		return examinationRepository.findOne(id);
	}

	@Override
	public Examination save(final Examination examination) throws ShanoirDatasetsException {
		Examination savedExamination = null;
		try {
			savedExamination = examinationRepository.save(examination);
		} catch (DataIntegrityViolationException dive) {
			LOG.error("Error while creating examination", dive);
			throw new ShanoirDatasetsException("Error while creating examination");
		}
		//updateShanoirOld(savedExamination);
		return savedExamination;
	}

	@Override
	public Examination update(final Examination examination) throws ShanoirDatasetsException {
		final Examination examinationDb = examinationRepository.findOne(examination.getId());
		updateExaminationValues(examinationDb, examination);
		try {
			examinationRepository.save(examinationDb);
		} catch (Exception e) {
			LOG.error("Error while updating examination", e);
			throw new ShanoirDatasetsException("Error while updating examination");
		}
		//updateShanoirOld(examinationDb);
		return examinationDb;
	}

	@Override
	public void updateFromShanoirOld(final Examination examination) throws ShanoirDatasetsException {
		if (examination.getId() == null) {
			throw new IllegalArgumentException("Examination id cannot be null");
		} else {
			final Examination examinationDb = examinationRepository.findOne(examination.getId());
			if (examinationDb != null) {
				try {
					examinationRepository.save(examinationDb);
				} catch (Exception e) {
					LOG.error("Error while updating examination from Shanoir Old", e);
					throw new ShanoirDatasetsException("Error while updating examination from Shanoir Old");
				}
			}
		}
	}

	/*
	 * Get list of studies reachable by connected user.
	 * 
	 * @return list of study ids.
	 */
	private List<Long> getStudiesForUser() {
		HttpEntity<Object> entity = null;
		try {
			entity = new HttpEntity<>(KeycloakUtil.getKeycloakHeader());
		} catch (ShanoirException e) {
			LOG.error("Error on keycloak request - " + e.getMessage());
		}

		// Request to study MS to get list of studies reachable by connected
		// user
		ResponseEntity<IdNameDTO[]> response = null;
		try {
			response = restTemplate.exchange(
					microservicesRequestsService.getStudiesMsUrl() + MicroserviceRequestsService.STUDY, HttpMethod.GET,
					entity, IdNameDTO[].class);
		} catch (RestClientException e) {
			LOG.error("Error on study microservice request - " + e.getMessage());
		}

		final List<Long> studyIds = new ArrayList<>();
		if (response != null) {
			IdNameDTO[] studies = null;
			if (HttpStatus.OK.equals(response.getStatusCode())
					|| HttpStatus.NO_CONTENT.equals(response.getStatusCode())) {
				studies = response.getBody();
			} else {
				LOG.error("Error on study microservice response - status code: " + response.getStatusCode());
			}

			if (studies != null) {
				for (IdNameDTO idNameDTO : studies) {
					studyIds.add(idNameDTO.getId());
				}
			}
		}
		return studyIds;
	}

	/*
	 * Update Shanoir Old.
	 * 
	 * @param Examination examination.
	 * 
	 * @return false if it fails, true if it succeed.
	 */
//	private boolean updateShanoirOld(final Examination examination) {
//		try {
//			LOG.info("Send update to Shanoir Old");
//			rabbitTemplate.convertAndSend(RabbitMqConfiguration.examinationQueueOut().getName(),
//					new ObjectMapper().writeValueAsString(examination));
//			return true;
//		} catch (AmqpException e) {
//			LOG.error("Cannot send examination " + examination.getId() + " save/update to Shanoir Old on queue : "
//					+ RabbitMqConfiguration.examinationQueueOut().getName(), e);
//		} catch (JsonProcessingException e) {
//			LOG.error("Cannot send examination " + examination.getId()
//					+ " save/update because of an error while serializing examination.", e);
//		}
//		return false;
//	}

	/*
	 * Update some values of examination to save them in database.
	 * 
	 * @param examinationDb examination found in database.
	 * 
	 * @param examination examination with new values.
	 * 
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

}

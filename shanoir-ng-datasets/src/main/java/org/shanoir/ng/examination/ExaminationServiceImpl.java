package org.shanoir.ng.examination;

import java.util.ArrayList;
import java.util.List;

import org.shanoir.ng.configuration.amqp.RabbitMqConfiguration;
import org.shanoir.ng.shared.dto.IdNameDTO;
import org.shanoir.ng.shared.dto.StudySubjectCenterNamesDTO;
import org.shanoir.ng.shared.exception.ErrorModelCode;
import org.shanoir.ng.shared.exception.ShanoirDatasetException;
import org.shanoir.ng.shared.service.MicroserviceRequestsService;
import org.shanoir.ng.utils.KeycloakUtil;
import org.shanoir.ng.utils.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

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
	private MicroserviceRequestsService microservicesRequestsService;

	@Autowired
	private RestTemplate restTemplate;

	@Autowired
	private RabbitTemplate rabbitTemplate;

	@Autowired
	private ExaminationRepository examinationRepository;

	@Override
	public void deleteById(final Long id) throws ShanoirDatasetException {
		examinationRepository.delete(id);
	}

	@Override
	public List<ExaminationDTO> findAll() throws ShanoirDatasetException {

		List<Examination> examinations = Utils.toList(examinationRepository.findAll());
		if (CollectionUtils.isEmpty(examinations)) {
			return new ArrayList<>();
		}

		final List<ExaminationDTO> examinationsToFrontList = new ArrayList<ExaminationDTO>();

		for (final Examination examination : examinations) {

			final ExaminationDTO examinationToFront = new ExaminationDTO();

			examinationToFront.setId(examination.getId());
			examinationToFront.setExaminationDate(examination.getExaminationDate());

			final HttpEntity<Long> entity = new HttpEntity<>(KeycloakUtil.getKeycloakHeader());

			// Request to study MS to get subject Name
			ResponseEntity<IdNameDTO> subjectResponse = null;
			String subjectURL = microservicesRequestsService.getStudyMsUrl() + MicroserviceRequestsService.SUBJECT + "/"
					+ examination.getSubjectId();
			if (examination.getSubjectId() != null) {
				try {
					subjectResponse = restTemplate.exchange(subjectURL, HttpMethod.GET, entity,
							new ParameterizedTypeReference<IdNameDTO>() {
							});
				} catch (RestClientException e) {
					LOG.error("Error on study microservice request", e);
					throw new ShanoirDatasetException("Error while getting subject name",
							ErrorModelCode.SUBJECT_NOT_FOUND);
				}

				IdNameDTO subject = null;
				if (HttpStatus.OK.equals(subjectResponse.getStatusCode())
						|| HttpStatus.NO_CONTENT.equals(subjectResponse.getStatusCode())) {
					subject = subjectResponse.getBody();
				} else {
					throw new ShanoirDatasetException(ErrorModelCode.SUBJECT_NOT_FOUND);
				}
				examinationToFront.setSubject(subject);
			}
			// Request to study MS to get study Name

			ResponseEntity<IdNameDTO> studyResponse = null;
			String studyURL = microservicesRequestsService.getStudyMsUrl() + MicroserviceRequestsService.STUDY + "/"
					+ examination.getStudyId();
			try {
				studyResponse = restTemplate.exchange(studyURL, HttpMethod.GET, entity,
						new ParameterizedTypeReference<IdNameDTO>() {
						});
			} catch (RestClientException e) {
				LOG.error("Error on study microservice request", e);
				throw new ShanoirDatasetException("Error while getting study name", ErrorModelCode.STUDY_NOT_FOUND);
			}

			IdNameDTO study = null;
			if (HttpStatus.OK.equals(studyResponse.getStatusCode())
					|| HttpStatus.NO_CONTENT.equals(studyResponse.getStatusCode())) {
				study = studyResponse.getBody();
			} else {
				throw new ShanoirDatasetException(ErrorModelCode.STUDY_NOT_FOUND);
			}
			examinationToFront.setStudyId(examination.getStudyId());
			examinationToFront.setStudyName(study.getName());

			// Request to study MS to get center Name

			ResponseEntity<IdNameDTO> centerResponse = null;
			String centerURL = microservicesRequestsService.getStudyMsUrl() + MicroserviceRequestsService.CENTER + "/"
					+ examination.getCenterId();
			try {
				centerResponse = restTemplate.exchange(centerURL, HttpMethod.GET, entity,
						new ParameterizedTypeReference<IdNameDTO>() {
						});
			} catch (RestClientException e) {
				LOG.error("Error on study microservice request", e);
				throw new ShanoirDatasetException("Error while getting center name", ErrorModelCode.CENTER_NOT_FOUND);
			}

			IdNameDTO center = null;
			if (HttpStatus.OK.equals(centerResponse.getStatusCode())
					|| HttpStatus.NO_CONTENT.equals(centerResponse.getStatusCode())) {
				center = centerResponse.getBody();
			} else {
				throw new ShanoirDatasetException(ErrorModelCode.CENTER_NOT_FOUND);
			}
			examinationToFront.setCenterId(examination.getCenterId());
			examinationToFront.setCenterName(center.getName());

			// Add the examination result to the list of examinations to send to
			// front
			examinationsToFrontList.add(examinationToFront);

		}

		return examinationsToFrontList;

	}

	@Override
	public List<Examination> findBy(final String fieldName, final Object value) {
		return examinationRepository.findBy(fieldName, value);
	}

	@Override
	public List<Examination> findBySubjectId(final Long subjectId) {
		return examinationRepository.findBySubjectId(subjectId);
	}

	@Override
	public ExaminationDTO findById(final Long id) throws ShanoirDatasetException {
		Examination examination = examinationRepository.findOne(id);
		ExaminationDTO examinationToFront = new ExaminationDTO();
		examinationToFront.setId(examination.getId());
		examinationToFront.setExaminationDate(examination.getExaminationDate());
		examinationToFront.setComment(examination.getComment());
		examinationToFront.setNote(examination.getNote());
		examinationToFront.setSubjectWeight(examination.getSubjectWeight());
		examinationToFront.setInstrumentBasedAssessmentList(examination.getInstrumentBasedAssessmentList());

		final HttpEntity<Long> entity = new HttpEntity<>(KeycloakUtil.getKeycloakHeader());

		Long studyId = examination.getStudyId();
		Long subjectId = examination.getSubjectId();
		Long centerId = examination.getCenterId();

		// Request to study MS to get study name, subject name and center name
		ResponseEntity<StudySubjectCenterNamesDTO> namesResponse = null;
		String studySerciceURL;
		if (subjectId != null) {
			studySerciceURL = microservicesRequestsService.getStudyMsUrl() + MicroserviceRequestsService.STUDY_MS + "/"
					+ studyId + "/" + subjectId + "/" + centerId;
		} else {
			studySerciceURL = microservicesRequestsService.getStudyMsUrl() + MicroserviceRequestsService.STUDY_MS + "/"
					+ studyId + "/" + 0 + "/" + centerId;
		}
		try {
			namesResponse = restTemplate.exchange(studySerciceURL, HttpMethod.GET, entity,
					new ParameterizedTypeReference<StudySubjectCenterNamesDTO>() {
					});
		} catch (RestClientException e) {
			LOG.error("Error on study microservice request", e);
			throw new ShanoirDatasetException("Error while getting studyName, subjectName, centerName : ",
					ErrorModelCode.BAD_REQUEST);
		}

		StudySubjectCenterNamesDTO names = null;
		if (HttpStatus.OK.equals(namesResponse.getStatusCode())
				|| HttpStatus.NO_CONTENT.equals(namesResponse.getStatusCode())) {
			names = namesResponse.getBody();
		} else {
			throw new ShanoirDatasetException(ErrorModelCode.BAD_REQUEST);
		}

		IdNameDTO study = new IdNameDTO();
		study.setId(studyId);
		study.setName(names.getStudyName());
		examinationToFront.setStudyId(examination.getStudyId());
		examinationToFront.setStudyName(study.getName());

		IdNameDTO subject = new IdNameDTO();
		subject.setId(subjectId);
		subject.setName(names.getSubjectName());
		examinationToFront.setSubject(subject);
		

		IdNameDTO center = new IdNameDTO();
		center.setId(centerId);
		center.setName(names.getCenterName());
		examinationToFront.setCenterId(examination.getCenterId());
		examinationToFront.setCenterName(center.getName());

		return examinationToFront;
	}

	@Override
	public Examination save(final Examination examination) throws ShanoirDatasetException {
		Examination savedExamination = null;
		try {
			savedExamination = examinationRepository.save(examination);
		} catch (DataIntegrityViolationException dive) {
			ShanoirDatasetException.logAndThrow(LOG, "Error while creating examination: " + dive.getMessage());
		}
		updateShanoirOld(savedExamination);
		return savedExamination;
	}

	@Override
	public Examination update(final Examination examination) throws ShanoirDatasetException {
		final Examination examinationDb = examinationRepository.findOne(examination.getId());
		updateExaminationValues(examinationDb, examination);
		try {
			examinationRepository.save(examinationDb);
		} catch (Exception e) {
			ShanoirDatasetException.logAndThrow(LOG, "Error while updating examination: " + e.getMessage());
		}
		updateShanoirOld(examinationDb);
		return examinationDb;
	}

	@Override
	public void updateFromShanoirOld(final Examination examination) throws ShanoirDatasetException {
		if (examination.getId() == null) {
			throw new IllegalArgumentException("Examination id cannot be null");
		} else {
			final Examination examinationDb = examinationRepository.findOne(examination.getId());
			if (examinationDb != null) {
				try {
					examinationRepository.save(examinationDb);
				} catch (Exception e) {
					ShanoirDatasetException.logAndThrow(LOG,
							"Error while updating examination from Shanoir Old: " + e.getMessage());
				}
			}
		}
	}

	/*
	 * Update Shanoir Old.
	 * 
	 * @param Examination examination.
	 * 
	 * @return false if it fails, true if it succeed.
	 */
	private boolean updateShanoirOld(final Examination examination) {
		try {
			LOG.info("Send update to Shanoir Old");
			rabbitTemplate.convertAndSend(RabbitMqConfiguration.examinationQueueOut().getName(),
					new ObjectMapper().writeValueAsString(examination));
			return true;
		} catch (AmqpException e) {
			LOG.error("Cannot send examination " + examination.getId() + " save/update to Shanoir Old on queue : "
					+ RabbitMqConfiguration.examinationQueueOut().getName(), e);
		} catch (JsonProcessingException e) {
			LOG.error("Cannot send examination " + examination.getId()
					+ " save/update because of an error while serializing examination.", e);
		}
		return false;
	}

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

}

package org.shanoir.ng.study;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.shanoir.ng.shared.dto.IdListDto;
import org.shanoir.ng.shared.exception.ErrorModelCode;
import org.shanoir.ng.shared.exception.ShanoirStudiesException;
import org.shanoir.ng.shared.service.MicroserviceRequestsService;
import org.shanoir.ng.study.dto.SimpleStudyCardDTO;
import org.shanoir.ng.study.dto.SimpleStudyDTO;
import org.shanoir.ng.utils.KeycloakUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

@Service
public class StudyServiceImpl implements StudyService {

	/**
	 * Logger
	 */
	private static final Logger LOG = LoggerFactory.getLogger(StudyServiceImpl.class);

	@Autowired
	private MicroserviceRequestsService microservicesRequestsService;

	@Autowired
	private RestTemplate restTemplate;

	@Autowired
	private StudyRepository studyRepository;

	@Override
	public void deleteById(final Long id) throws ShanoirStudiesException {
		final Study study = studyRepository.findOne(id);
		if (study == null) {
			LOG.error("Study with id " + id + " not found");
			throw new ShanoirStudiesException(ErrorModelCode.STUDY_NOT_FOUND);
		}
		studyRepository.delete(id);
	}

	@Override
	public void deleteFromShanoirOld(final Study study) throws ShanoirStudiesException {
		if (study.getId() != null) {
			LOG.warn("Delete study with name " + study.getName() + " (id: " + study.getId() + ") from shanoir-old");
			try {
				studyRepository.delete(study);
			} catch (Exception e) {
				ShanoirStudiesException.logAndThrow(LOG,
						"Error while deleting study from Shanoir Old: " + e.getMessage());
			}
		}
	}

	@Override
	public List<Study> findAll() {
		return studyRepository.findAll();
	}

	@Override
	public Study findById(final Long id) {
		return studyRepository.findOne(id);
	}

	@Override
	public List<Study> findStudiesByUserId(final Long userId) {
		return studyRepository.findByStudyUsers_UserId(userId);
	}

	@Override
	public List<SimpleStudyDTO> findStudiesWithStudyCardsByUserId(final Long userId) throws ShanoirStudiesException {
		final List<Study> studies = findStudiesByUserId(userId);

		final IdListDto studyIds = new IdListDto();
		for (final Study study : studies) {
			studyIds.getIdList().add(study.getId());
		}
		final HttpEntity<IdListDto> entity = new HttpEntity<>(studyIds, KeycloakUtil.getKeycloakHeader());

		// Request to studycard MS to get cards for list of studies
		ResponseEntity<List<SimpleStudyCardDTO>> studyCardResponse = null;
		try {
			studyCardResponse = restTemplate.exchange(
					microservicesRequestsService.getStudycardMsUrl() + MicroserviceRequestsService.SEARCH,
					HttpMethod.POST, entity, new ParameterizedTypeReference<List<SimpleStudyCardDTO>>() {
					});
		} catch (RestClientException e) {
			LOG.error("Error on study card microservice request", e);
			throw new ShanoirStudiesException("Error while getting study card list", ErrorModelCode.SC_MS_COMM_FAILURE);
		}

		List<SimpleStudyCardDTO> studyCards = null;
		if (HttpStatus.OK.equals(studyCardResponse.getStatusCode())
				|| HttpStatus.NO_CONTENT.equals(studyCardResponse.getStatusCode())) {
			studyCards = studyCardResponse.getBody();
		} else {
			throw new ShanoirStudiesException(ErrorModelCode.SC_MS_COMM_FAILURE);
		}
		final Map<Long, SimpleStudyCardDTO> studyCardsmap = new HashMap<>();
		for (final SimpleStudyCardDTO simpleStudyCard : studyCards) {
			studyCardsmap.put(simpleStudyCard.getId(), simpleStudyCard);
		}

		final List<SimpleStudyDTO> simpleStudies = new ArrayList<>();
		for (final Study study : studies) {
			final SimpleStudyDTO simpleStudy = new SimpleStudyDTO(study.getId(), study.getName());
			for (final Long studyCardId : study.getStudyCardIds()) {
				simpleStudy.getStudyCards().add(studyCardsmap.get(studyCardId));
			}
			simpleStudies.add(simpleStudy);
		}

		return simpleStudies;
	}

	@Override
	public Study save(final Study study) throws ShanoirStudiesException {
		return studyRepository.save(study);
	}

	@Override
	public Study update(final Study study) throws ShanoirStudiesException {
		final Study studyDb = studyRepository.findOne(study.getId());
		studyDb.setName(study.getName());
		studyDb.setEndDate(study.getEndDate());
		studyDb.setClinical(study.isClinical());
		studyDb.setWithExamination(study.isWithExamination());
		studyDb.setVisibleByDefault(study.isVisibleByDefault());
		studyDb.setDownloadableByDefault(study.isDownloadableByDefault());
		studyDb.setStudyStatus(study.getStudyStatus());

		studyRepository.save(studyDb);

		return studyDb;
	}

	@Override
	public void updateFromShanoirOld(final Study study) throws ShanoirStudiesException {
		if (study.getId() == null) {
			LOG.info("Insert new Study with name " + study.getName() + " from shanoir-old");
			try {
				studyRepository.save(study);
			} catch (Exception e) {
				ShanoirStudiesException.logAndThrow(LOG,
						"Error while creating new study from Shanoir Old: " + e.getMessage());
			}
		} else {
			final Study studyDb = studyRepository.findOne(study.getId());
			if (studyDb != null) {
				try {
					LOG.info("Update existing Study with name " + study.getName() + " (id: " + study.getId()
							+ ") from shanoir-old");
					studyRepository.save(study);
				} catch (Exception e) {
					ShanoirStudiesException.logAndThrow(LOG,
							"Error while updating study from Shanoir Old: " + e.getMessage());
				}
			} else {
				LOG.warn("Import new study with name " + study.getName() + "  (id: " + study.getId()
						+ ") from shanoir-old");
				studyRepository.save(study);
			}
		}

	}

}

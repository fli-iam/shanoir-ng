package org.shanoir.ng.study;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.shanoir.ng.acquisitionequipment.AcquisitionEquipment;
import org.shanoir.ng.acquisitionequipment.AcquisitionEquipmentRepository;
import org.shanoir.ng.center.CenterRepository;
import org.shanoir.ng.shared.dto.IdListDTO;
import org.shanoir.ng.shared.dto.IdNameDTO;
import org.shanoir.ng.shared.exception.ShanoirException;
import org.shanoir.ng.shared.exception.ShanoirStudiesException;
import org.shanoir.ng.shared.exception.StudiesErrorModelCode;
import org.shanoir.ng.shared.service.MicroserviceRequestsService;
import org.shanoir.ng.study.dto.SimpleStudyCardDTO;
import org.shanoir.ng.study.dto.SimpleStudyDTO;
import org.shanoir.ng.study.dto.StudyStudyCardDTO;
import org.shanoir.ng.studycenter.StudyCenter;
import org.shanoir.ng.studycenter.StudyCenterRepository;
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
import org.springframework.util.CollectionUtils;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

/**
 * Implementation of study service.
 * 
 * @author msimon
 *
 */
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
	private StudyCenterRepository studyCenterRepository;

	@Autowired
	private StudyRepository studyRepository;
	
	@Autowired
	private CenterRepository centerRepository;
	
	@Autowired
	private AcquisitionEquipmentRepository acquisitionEquipmentRepository;

	@Override
	public boolean canUserUpdateStudy(final Long studyId, final Long userId) {
		final Study study = studyRepository.findOne(studyId);
		for (final StudyUser studyUser : study.getStudyUserList()) {
			if (userId.equals(studyUser.getUserId()) && (StudyUserType.RESPONSIBLE.equals(studyUser.getStudyUserType())
					|| StudyUserType.SEE_DOWNLOAD_IMPORT_MODIFY.equals(studyUser.getStudyUserType()))) {
				return true;
			}
		}
		LOG.warn("User with id " + userId + " can't update study with id " + studyId);
		return false;
	}

	@Override
	public void deleteById(final Long id, final Long userId) throws ShanoirStudiesException {
		final Study study = studyRepository.findOne(id);
		if (study == null) {
			LOG.error("Study with id " + id + " not found");
			throw new ShanoirStudiesException(StudiesErrorModelCode.STUDY_NOT_FOUND);
		}
		for (final StudyUser studyUser : study.getStudyUserList()) {
			if (userId.equals(studyUser.getUserId())
					&& StudyUserType.RESPONSIBLE.equals(studyUser.getStudyUserType())) {
				studyRepository.delete(id);
				return;
			}
		}
		LOG.error("User with id " + userId + " can't delete study with id " + id);
		throw new ShanoirStudiesException(StudiesErrorModelCode.NO_RIGHT_FOR_ACTION);
	}

	@Override
	public void deleteFromShanoirOld(final Study study) throws ShanoirStudiesException {
		if (study.getId() != null) {
			LOG.warn("Delete study with name " + study.getName() + " (id: " + study.getId() + ") from shanoir-old");
			try {
				studyRepository.delete(study);
			} catch (Exception e) {
				LOG.error("Error while deleting study from Shanoir Old", e);
				throw new ShanoirStudiesException("Error while deleting study from Shanoir Old");
			}
		}
	}

	@Override
	public List<Study> findAll() {
		return studyRepository.findAll();
	}

	@Override
	public List<Study> findBy(final String fieldName, final Object value) {
		return studyRepository.findBy(fieldName, value);
	}

	@Override
	public Study findById(Long id) {
		return studyRepository.findOne(id);
	}

	@Override
	public Study findById(final Long id, final Long userId) throws ShanoirStudiesException {
		final Study study = studyRepository.findOne(id);
		for (final StudyUser studyUser : study.getStudyUserList()) {
			if (userId.equals(studyUser.getUserId())
					&& !StudyUserType.NOT_SEE_DOWNLOAD.equals(studyUser.getStudyUserType())) {
				return studyRepository.findOne(id);
			}
		}
		LOG.error("User with id " + userId + " can't see study with id " + id);
		throw new ShanoirStudiesException(StudiesErrorModelCode.NO_RIGHT_FOR_ACTION);
	}

	@Override
	public List<IdNameDTO> findIdsAndNames() {
		return studyRepository.findIdsAndNames();
	}

	@Override
	public List<Study> findStudiesByUserId(final Long userId) {
		return studyRepository.findByStudyUserList_UserIdOrderByNameAsc(userId);
	}

	@Override
	public List<SimpleStudyDTO> findStudiesWithStudyCardsByUserAndEquipment(final Long userId, final EquipmentDicom equipment) throws ShanoirException {
		final List<Study> studies = findStudiesByUserId(userId);
		if (CollectionUtils.isEmpty(studies)) {
			return new ArrayList<>();
		}

		final IdListDTO studyIds = new IdListDTO();
		for (final Study study : studies) {
			studyIds.getIdList().add(study.getId());
		}
		final HttpEntity<IdListDTO> entity = new HttpEntity<>(studyIds, KeycloakUtil.getKeycloakHeader());

		// Request to studycard MS to get cards for list of studies
		ResponseEntity<List<StudyCardDTO>> studyCardResponse = null;
		try {
			studyCardResponse = restTemplate.exchange(
					microservicesRequestsService.getStudycardsMsUrl() + MicroserviceRequestsService.SEARCH,
					HttpMethod.POST, entity, new ParameterizedTypeReference<List<StudyCardDTO>>() {
					});
		} catch (RestClientException e) {
			LOG.error("Error on study card microservice request", e);
			throw new ShanoirStudiesException("Error while getting study card list", StudiesErrorModelCode.SC_MS_COMM_FAILURE);
		}

		List<StudyCardDTO> studyCards = null;
		if (HttpStatus.OK.equals(studyCardResponse.getStatusCode())
				|| HttpStatus.NO_CONTENT.equals(studyCardResponse.getStatusCode())) {
			studyCards = studyCardResponse.getBody();
		} else {
			throw new ShanoirStudiesException(StudiesErrorModelCode.SC_MS_COMM_FAILURE);
		}
		final Map<Long, SimpleStudyCardDTO> studyCardsmap = new HashMap<>();
		for (final StudyCardDTO studyCard : studyCards) {
			SimpleStudyCardDTO simpleStudyCard = new SimpleStudyCardDTO(studyCard.getId(), studyCard.getName());
			simpleStudyCard.setCenter(new IdNameDTO(studyCard.getCenterId(), centerRepository.findOne(studyCard.getCenterId()).getName()));
			AcquisitionEquipment acquisitionEquipment = acquisitionEquipmentRepository.findOne(studyCard.getAcquisitionEquipmentId());
			String serialNumber = acquisitionEquipment.getSerialNumber();
			String manufacturerModel = acquisitionEquipment.getManufacturerModel().getName();
			String manufacturer = acquisitionEquipment.getManufacturerModel().getManufacturer().getName();
			if (!serialNumber.equals(equipment.getDeviceSerialNumber())
					|| !manufacturerModel.equals(equipment.getManufacturerModelName())
					|| !manufacturer.equals(equipment.getManufacturer())) {
				simpleStudyCard.setCompatible(false);		
			} else {
				simpleStudyCard.setCompatible(true);
			}
			studyCardsmap.put(studyCard.getId(), simpleStudyCard);
		}

		final List<SimpleStudyDTO> simpleStudies = new ArrayList<>();
		for (final Study study : studies) {
			final SimpleStudyDTO simpleStudy = new SimpleStudyDTO(study.getId(), study.getName());
			
			for (final StudyCenter studyCenter : study.getStudyCenterList()) {
				IdNameDTO center = new IdNameDTO(studyCenter.getCenter().getId(), studyCenter.getCenter().getName());
				simpleStudy.getCenters().add(center);
			}
			
			for (final Long studyCardId : study.getStudyCardIds()) {
				simpleStudy.getStudyCards().add(studyCardsmap.get(studyCardId));
				simpleStudy.setCompatible(false);
				if (studyCardsmap.get(studyCardId).getCompatible()) {
					simpleStudy.setCompatible(true);
				}
			}
			simpleStudies.add(simpleStudy);
		}

		return simpleStudies;
	}

	@Override
	public Study save(final Study study) throws ShanoirStudiesException {
		for (final StudyCenter studyCenter : study.getStudyCenterList()) {
			studyCenter.setStudy(study);
		}
		return studyRepository.save(study);
	}

	@Override
	public Study update(final Study study) throws ShanoirStudiesException {
		final Study studyDb = studyRepository.findOne(study.getId());
		studyDb.setClinical(study.isClinical());
		studyDb.setDownloadableByDefault(study.isDownloadableByDefault());
		studyDb.setEndDate(study.getEndDate());
		studyDb.setName(study.getName());
		studyDb.setStudyStatus(study.getStudyStatus());
		studyDb.setVisibleByDefault(study.isVisibleByDefault());
		studyDb.setWithExamination(study.isWithExamination());

		// Copy list of database links study/center
		final List<StudyCenter> studyCenterDbList = new ArrayList<>(studyDb.getStudyCenterList());
		for (final StudyCenter studyCenter : study.getStudyCenterList()) {
			if (studyCenter.getId() == null) {
				// Add link study/center
				studyCenter.setStudy(studyDb);
				studyDb.getStudyCenterList().add(studyCenter);
			}
		}
		for (final StudyCenter studyCenterDb : studyCenterDbList) {
			boolean keepStudyCenter = false;
			for (final StudyCenter studyCenter : study.getStudyCenterList()) {
				if (studyCenterDb.getId().equals(studyCenter.getId())) {
					keepStudyCenter = true;
					break;
				}
			}
			if (!keepStudyCenter) {
				// Move link study/center
				studyDb.getStudyCenterList().remove(studyCenterDb);
				studyCenterRepository.delete(studyCenterDb.getId());
			}
		}

		studyRepository.save(studyDb);

		return studyDb;
	}

	@Override
	public void updateFromMsStudyCard(StudyStudyCardDTO studyStudyCardDTO) throws ShanoirStudiesException {
		if (studyStudyCardDTO.getNewStudyId() != null) {
			// Add link
			LOG.debug("Create new link between study (id: " + studyStudyCardDTO.getNewStudyId()
					+ ") and study card (id: " + studyStudyCardDTO.getStudyCardId() + ")");
			final Study study = studyRepository.findOne(studyStudyCardDTO.getNewStudyId());
			if (study == null) {
				LOG.error("Study with id " + studyStudyCardDTO.getNewStudyId() + " not found");
				throw new ShanoirStudiesException(StudiesErrorModelCode.STUDY_NOT_FOUND);
			}
			study.getStudyCardIds().add(studyStudyCardDTO.getStudyCardId());
		}
		if (studyStudyCardDTO.getOldStudyId() != null) {
			// Delete link
			LOG.debug("Delete link between study (id: " + studyStudyCardDTO.getOldStudyId() + ") and study card (id: "
					+ studyStudyCardDTO.getStudyCardId() + ")");
			final Study study = studyRepository.findOne(studyStudyCardDTO.getOldStudyId());
			if (study == null) {
				LOG.error("Study with id " + studyStudyCardDTO.getOldStudyId() + " not found");
				throw new ShanoirStudiesException(StudiesErrorModelCode.STUDY_NOT_FOUND);
			}
			study.getStudyCardIds().remove(studyStudyCardDTO.getOldStudyId());
		}
	}

	@Override
	public void updateFromShanoirOld(final Study study) throws ShanoirStudiesException {
		if (study.getId() == null) {
			LOG.info("Insert new Study with name " + study.getName() + " from shanoir-old");
			try {
				studyRepository.save(study);
			} catch (Exception e) {
				LOG.error("Error while creating study from Shanoir Old", e);
				throw new ShanoirStudiesException("Error while creating study from Shanoir Old");
			}
		} else {
			final Study studyDb = studyRepository.findOne(study.getId());
			if (studyDb != null) {
				try {
					LOG.info("Update existing Study with name " + study.getName() + " (id: " + study.getId()
							+ ") from shanoir-old");
					studyRepository.save(study);
				} catch (Exception e) {
					LOG.error("Error while updating study from Shanoir Old", e);
					throw new ShanoirStudiesException("Error while updating study from Shanoir Old");
				}
			} else {
				LOG.warn("Import new study with name " + study.getName() + "  (id: " + study.getId()
						+ ") from shanoir-old");
				studyRepository.save(study);
			}
		}

	}

}

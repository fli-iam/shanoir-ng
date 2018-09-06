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
import org.shanoir.ng.study.dto.NIfTIConverterDTO;
import org.shanoir.ng.study.dto.SimpleStudyCardDTO;
import org.shanoir.ng.study.dto.SimpleStudyDTO;
import org.shanoir.ng.studycenter.StudyCenter;
import org.shanoir.ng.studycenter.StudyCenterRepository;
import org.shanoir.ng.studyuser.StudyUser;
import org.shanoir.ng.studyuser.StudyUserRepository;
import org.shanoir.ng.studyuser.StudyUserType;
import org.shanoir.ng.subjectstudy.SubjectStudy;
import org.shanoir.ng.subjectstudy.SubjectStudyRepository;
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
	private AcquisitionEquipmentRepository acquisitionEquipmentRepository;

	@Autowired
	private CenterRepository centerRepository;

	@Autowired
	private MicroserviceRequestsService microservicesRequestsService;

	@Autowired
	private RestTemplate restTemplate;

	@Autowired
	private StudyCenterRepository studyCenterRepository;

	@Autowired
	private StudyUserRepository studyUserRepository;
	
	@Autowired
	private SubjectStudyRepository subjectStudyRepository;

	@Autowired
	private StudyRepository studyRepository;

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
	public void deleteById(final Long id) {
		studyRepository.delete(id);
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
	public List<SimpleStudyDTO> findStudiesWithStudyCardsByUserAndEquipment(final Long userId,
			final EquipmentDicom equipment) throws ShanoirException {
		final List<Study> studies = findStudiesByUserId(userId);
		if (CollectionUtils.isEmpty(studies)) {
			return new ArrayList<>();
		}

		final IdListDTO studyIds = new IdListDTO();
		for (final Study study : studies) {
			studyIds.getIdList().add(study.getId());
		}

		// 1. Construction of SimpleStudyCardDTO Map to send
		final Map<Long, SimpleStudyCardDTO> studyCardsmap = new HashMap<>();

		// 2. Construction of SimpleStudyDTO List to send
		final List<SimpleStudyDTO> simpleStudies = new ArrayList<>();

		// 3. Construction of studyCarIdStudyCardIdMap to link the studycard id with the
		// study id
		final Map<Long, Long> studyCarIdStudyCardIdMap = new HashMap<>();

		// Request to MS Datasets to get all study cards for the list of all studies for
		// user
		final HttpEntity<IdListDTO> studyIdsEntity = new HttpEntity<>(studyIds, KeycloakUtil.getKeycloakHeader());
		ResponseEntity<List<StudyCardDTO>> studyCardResponse = null;

		try {
			studyCardResponse = restTemplate.exchange(
					microservicesRequestsService.getStudycardsMsUrl() + MicroserviceRequestsService.SEARCH,
					HttpMethod.POST, studyIdsEntity, new ParameterizedTypeReference<List<StudyCardDTO>>() {
					});
			if (HttpStatus.OK.equals(studyCardResponse.getStatusCode())
					|| HttpStatus.NO_CONTENT.equals(studyCardResponse.getStatusCode())) {
				final List<StudyCardDTO> studyCardDTOs = studyCardResponse.getBody();

				if (studyCardDTOs != null) {
					for (final StudyCardDTO studyCard : studyCardDTOs) {

						// Id and name of studycard
						SimpleStudyCardDTO simpleStudyCard = new SimpleStudyCardDTO(studyCard.getId(),
								studyCard.getName());

						// Id and name of the center for studycard
						simpleStudyCard.setCenter(new IdNameDTO(studyCard.getCenterId(),
								centerRepository.findOne(studyCard.getCenterId()).getName()));

						// compatibility of studycard
						AcquisitionEquipment acquisitionEquipment = acquisitionEquipmentRepository
								.findOne(studyCard.getAcquisitionEquipmentId());
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

						// Request to import MS to get niftiConverter name for studycard
						HttpEntity<Long> entity = null;
						try {
							entity = new HttpEntity<>(KeycloakUtil.getKeycloakHeader());
						} catch (ShanoirException e) {
							throw ((ShanoirStudiesException) e);
						}
						ResponseEntity<NIfTIConverterDTO> niftiConverterResponse = null;
						try {
							niftiConverterResponse = restTemplate.exchange(
									microservicesRequestsService.getImportMsUrl() + "/"
											+ studyCard.getNiftiConverterId(),
									HttpMethod.GET, entity, new ParameterizedTypeReference<NIfTIConverterDTO>() {
									});
						} catch (RestClientException e) {
							LOG.error("Error on import microservice request for getting nifti converter by id", e);
							throw new ShanoirStudiesException("Error while getting niftiConverter",
									StudiesErrorModelCode.IMPORT_MS_COMM_FAILURE);
						}
						IdNameDTO niftiConverter;
						if (HttpStatus.OK.equals(studyCardResponse.getStatusCode())
								|| HttpStatus.NO_CONTENT.equals(studyCardResponse.getStatusCode())) {
							niftiConverter = new IdNameDTO(niftiConverterResponse.getBody().getId(),
									niftiConverterResponse.getBody().getName());
						} else {
							throw new ShanoirStudiesException(StudiesErrorModelCode.IMPORT_MS_COMM_FAILURE);
						}
						simpleStudyCard.setNiftiConverter(niftiConverter);

						// simpleStudyCard object is constructed, now we add it to the Map
						studyCardsmap.put(simpleStudyCard.getId(), simpleStudyCard);

						// Add the link of studycardId and studyId
						studyCarIdStudyCardIdMap.put(studyCard.getId(), studyCard.getStudyId());
					}
				}

				// 2. Construction of SimpleStudyDTO list to send
				for (final Study study : studies) {

					// Id and name of study
					final SimpleStudyDTO simpleStudy = new SimpleStudyDTO(study.getId(), study.getName());

					// Id and name list of the center list for study
					for (final StudyCenter studyCenter : study.getStudyCenterList()) {
						IdNameDTO center = new IdNameDTO(studyCenter.getCenter().getId(),
								studyCenter.getCenter().getName());
						simpleStudy.getCenters().add(center);
					}

					// StudyCard and its compatibility for study
					// The study is not compatible by default
					simpleStudy.setCompatible(false);
					for (Map.Entry<Long, Long> studyCarIdStudyCardIdPair : studyCarIdStudyCardIdMap.entrySet()) {
						// If studyId of the studyCarIdStudyCardIdPair is ok, add the studyCard to this
						// study
						if (studyCarIdStudyCardIdPair.getValue().equals(study.getId())) {
							simpleStudy.getStudyCards().add(studyCardsmap.get(studyCarIdStudyCardIdPair.getKey()));
							// If at least one of the studyCards of this study is compatible, then the study
							// is compatible
							if (studyCardsmap.get(studyCarIdStudyCardIdPair.getKey()).getCompatible()) {
								simpleStudy.setCompatible(true);
							}
						}
					}

					// simpleStudy object is constructed, now we add it to the Map
					simpleStudies.add(simpleStudy);
				}
			} else {
				throw new ShanoirStudiesException(StudiesErrorModelCode.SC_MS_COMM_FAILURE);
			}
		} catch (RestClientException e) {
			LOG.error("Error on study card microservice request", e);
			throw new ShanoirStudiesException("Error while getting study card list",
					StudiesErrorModelCode.SC_MS_COMM_FAILURE);
		}

		return simpleStudies;
	}

	@Override
	public boolean isUserResponsible(final Long studyId, final Long userId) throws ShanoirStudiesException {
		final Study study = studyRepository.findOne(studyId);
		if (study == null) {
			LOG.error("Study with id " + studyId + " not found");
			throw new ShanoirStudiesException(StudiesErrorModelCode.STUDY_NOT_FOUND);
		}
		for (final StudyUser studyUser : study.getStudyUserList()) {
			if (userId.equals(studyUser.getUserId())
					&& StudyUserType.RESPONSIBLE.equals(studyUser.getStudyUserType())) {
				return true;
			}
		}
		return false;
	}

	@Override
	public Study save(final Study study) throws ShanoirStudiesException {
		for (final StudyCenter studyCenter : study.getStudyCenterList()) {
			studyCenter.setStudy(study);
		} 
		for (final SubjectStudy subjectStudy : study.getSubjectStudyList()) {
			subjectStudy.setStudy(study);
		}
		for (final StudyUser studyUser: study.getStudyUserList()) {
			studyUser.setStudyId(study.getId());
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
		studyDb.setMonoCenter(study.isMonoCenter());

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
		
		// Copy list of database links subject/study
		final List<SubjectStudy> subjectStudyDbList = new ArrayList<>(studyDb.getSubjectStudyList());
		for (final SubjectStudy subjectStudy : study.getSubjectStudyList()) {
			if (subjectStudy.getId() == null) {
				// Add link subject/study
				subjectStudy.setStudy(studyDb);
				studyDb.getSubjectStudyList().add(subjectStudy);
			}
		}
		for (final SubjectStudy subjectStudyDb : subjectStudyDbList) {
			boolean keepSubjectStudy = false;
			for (final SubjectStudy subjectStudy : study.getSubjectStudyList()) {
				if (subjectStudyDb.getId().equals(subjectStudy.getId())) {
					keepSubjectStudy = true;
					break;
				}
			}
			if (!keepSubjectStudy) {
				// Move link subject/study
				studyDb.getSubjectStudyList().remove(subjectStudyDb);
				subjectStudyRepository.delete(subjectStudyDb.getId());
			}
		}
		
		// Copy list of database links study/user
		final List<StudyUser> studyUserDbList = new ArrayList<>(studyDb.getStudyUserList());
		for (final StudyUser studyUser : study.getStudyUserList()) {
			if (studyUser.getId() == null) {
				// Add link study/user
				studyUser.setStudyId(studyDb.getId());
				studyDb.getStudyUserList().add(studyUser);
			}
		}
		for (final StudyUser studyUserDb : studyUserDbList) {
			boolean keepStudyUser = false;
			for (final StudyUser studyUser : study.getStudyUserList()) {
				if (studyUserDb.getId().equals(studyUser.getId())) {
					keepStudyUser = true;
					break;
				}
			}
			if (!keepStudyUser) {
				// Move link study/user
				studyDb.getStudyUserList().remove(studyUserDb);
				studyUserRepository.delete(studyUserDb.getId());
			}
		}

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

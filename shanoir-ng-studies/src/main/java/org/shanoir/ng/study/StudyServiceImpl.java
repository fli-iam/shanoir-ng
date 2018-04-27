package org.shanoir.ng.study;

import java.util.ArrayList;
import java.util.List;

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
		// List of SimpleStudyDTO object to send
		final List<SimpleStudyDTO> simpleStudies = new ArrayList<>();
		
		for (final Study study : studies) {
			final SimpleStudyDTO simpleStudy = new SimpleStudyDTO(study.getId(), study.getName());
			simpleStudy.setCompatible(false);
			
			// Request to studycard MS to get cards for the study
			final IdListDTO studyIds = new IdListDTO();
			studyIds.getIdList().add(study.getId());
			final HttpEntity<IdListDTO> studyIdsEntity = new HttpEntity<>(studyIds, KeycloakUtil.getKeycloakHeader());
			ResponseEntity<List<StudyCardDTO>> studyCardResponse = null;
			try {
				studyCardResponse = restTemplate.exchange(
						microservicesRequestsService.getStudycardsMsUrl() + MicroserviceRequestsService.SEARCH,
						HttpMethod.POST, studyIdsEntity, new ParameterizedTypeReference<List<StudyCardDTO>>() {
						});
				List<StudyCardDTO> studyCards = null;
				if (HttpStatus.OK.equals(studyCardResponse.getStatusCode())
						|| HttpStatus.NO_CONTENT.equals(studyCardResponse.getStatusCode())) {
					studyCards = studyCardResponse.getBody();
				} else {
					throw new ShanoirStudiesException(StudiesErrorModelCode.SC_MS_COMM_FAILURE);
				}

				for (final StudyCardDTO studyCard : studyCards) {
					// Id and name of studycard
					SimpleStudyCardDTO simpleStudyCard = new SimpleStudyCardDTO(studyCard.getId(), studyCard.getName());
					// Id and name of the center for studycard
					simpleStudyCard.setCenter(new IdNameDTO(studyCard.getCenterId(), centerRepository.findOne(studyCard.getCenterId()).getName()));
					// compatibility of studycard
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
						simpleStudy.setCompatible(true);
					}
					//Request to import MS to get niftiConverter name for studycard
					HttpEntity<Long> entity = null;
					try {
						entity = new HttpEntity<>(KeycloakUtil.getKeycloakHeader());
					} catch (ShanoirException e) {
						throw ((ShanoirStudiesException) e);
					}
					ResponseEntity<NIfTIConverterDTO> niftiConverterResponse = null;
					try {
						niftiConverterResponse = restTemplate.exchange(
								microservicesRequestsService.getImportMsUrl() + "/" + studyCard.getNiftiConverterId(),
								HttpMethod.GET, entity, new ParameterizedTypeReference<NIfTIConverterDTO>() {
								});
					} catch (RestClientException e) {
						LOG.error("Error on import microservice request for getting nifti converter by id", e);
						throw new ShanoirStudiesException("Error while getting niftiConverter", StudiesErrorModelCode.IMPORT_MS_COMM_FAILURE);
					}
					IdNameDTO niftiConverter;
					if (HttpStatus.OK.equals(studyCardResponse.getStatusCode())
							|| HttpStatus.NO_CONTENT.equals(studyCardResponse.getStatusCode())) {
						niftiConverter = new IdNameDTO(niftiConverterResponse.getBody().getId(), niftiConverterResponse.getBody().getName());
					} else {
						throw new ShanoirStudiesException(StudiesErrorModelCode.IMPORT_MS_COMM_FAILURE);
					}
					simpleStudyCard.setNiftiConverter(niftiConverter);
					
					// Construction of Map SimpleStudyDTO to send
					simpleStudy.getStudyCards().add(simpleStudyCard);
					for (final StudyCenter studyCenter : study.getStudyCenterList()) {
						IdNameDTO center = new IdNameDTO(studyCenter.getCenter().getId(), studyCenter.getCenter().getName());
						simpleStudy.getCenters().add(center);
					}
					simpleStudies.add(simpleStudy);
				}
			} catch (RestClientException e) {
				LOG.error("Error on study card microservice request", e);
				throw new ShanoirStudiesException("Error while getting study card list", StudiesErrorModelCode.SC_MS_COMM_FAILURE);
			}
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

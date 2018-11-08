package org.shanoir.ng.study;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.shanoir.ng.acquisitionequipment.AcquisitionEquipmentDTO;
import org.shanoir.ng.shared.dto.IdNameDTO;
import org.shanoir.ng.shared.exception.ShanoirException;
import org.shanoir.ng.shared.exception.ShanoirStudiesException;
import org.shanoir.ng.shared.exception.StudiesErrorModelCode;
import org.shanoir.ng.study.dto.SimpleStudyDTO;
import org.shanoir.ng.studycenter.StudyCenter;
import org.shanoir.ng.studycenter.StudyCenterDTO;
import org.shanoir.ng.studycenter.StudyCenterMapper;
import org.shanoir.ng.studycenter.StudyCenterRepository;
import org.shanoir.ng.studyuser.StudyUser;
import org.shanoir.ng.studyuser.StudyUserRepository;
import org.shanoir.ng.studyuser.StudyUserType;
import org.shanoir.ng.subjectstudy.SubjectStudy;
import org.shanoir.ng.subjectstudy.SubjectStudyRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

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
	private StudyCenterMapper studyCenterMapper;

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
	
	public List<Study> findStudiesByUserIdAndStudyUserTypeLessThanEqual(final Long userId,final Integer studyUserTypeId) {
		return studyRepository.findByStudyUserList_UserIdAndStudyUserList_StudyUserTypeLessThanEqualOrderByNameAsc(userId,studyUserTypeId);		
	}

	@Override
	public List<SimpleStudyDTO> findStudiesByUserAndEquipment(final Long userId, 
			final EquipmentDicom equipment) throws ShanoirException {
		final List<Study> studies = findStudiesByUserIdAndStudyUserTypeLessThanEqual(userId, StudyUserType.SEE_DOWNLOAD_IMPORT.getId());
		if (CollectionUtils.isEmpty(studies)) {
			return new ArrayList<>();
		}
		
		// Construction of SimpleStudyDTO List to send
		final List<SimpleStudyDTO> simpleStudies = new ArrayList<>();
		
		for (final Study study : studies) {
			// Id and name of study
			final SimpleStudyDTO simpleStudy = new SimpleStudyDTO(study.getId(), study.getName());
			
			// centerDTO list for study
			for (final StudyCenter studyCenter : study.getStudyCenterList()) {
				StudyCenterDTO studyCenterDTO = studyCenterMapper.studyCenterToStudyCenterDTO(studyCenter);
				simpleStudy.getStudyCenterList().add(studyCenterDTO);
				
				// acquisition equipment for compatibility check
				for (final AcquisitionEquipmentDTO acquisitionEquipment : studyCenterDTO.getCenter().getAcquisitionEquipments()) {
					String serialNumber = acquisitionEquipment.getSerialNumber();
					String manufacturerModel = acquisitionEquipment.getManufacturerModel().getName();
					String manufacturer = acquisitionEquipment.getManufacturerModel().getManufacturer().getName();
					if (StringUtils.equals(serialNumber, equipment.getDeviceSerialNumber())
							&& StringUtils.equals(manufacturerModel, equipment.getManufacturerModelName())
							&& StringUtils.equals(manufacturer, equipment.getManufacturer())) {
						acquisitionEquipment.setCompatible(true);
						studyCenterDTO.setCompatible(true);
						simpleStudy.setCompatible(true);
						break;
					}
				}
			}
			simpleStudies.add(simpleStudy);
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

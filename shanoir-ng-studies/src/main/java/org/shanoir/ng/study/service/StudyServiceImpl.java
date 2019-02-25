package org.shanoir.ng.study.service;

import java.util.ArrayList;
import java.util.List;

import org.shanoir.ng.shared.dto.IdNameDTO;
import org.shanoir.ng.shared.exception.AccessDeniedException;
import org.shanoir.ng.shared.exception.EntityNotFoundException;
import org.shanoir.ng.study.model.Study;
import org.shanoir.ng.study.repository.StudyRepository;
import org.shanoir.ng.studycenter.StudyCenter;
import org.shanoir.ng.studycenter.StudyCenterRepository;
import org.shanoir.ng.studyuser.StudyUser;
import org.shanoir.ng.studyuser.StudyUserRepository;
import org.shanoir.ng.studyuser.StudyUserRight;
import org.shanoir.ng.subjectstudy.SubjectStudy;
import org.shanoir.ng.subjectstudy.SubjectStudyRepository;
import org.shanoir.ng.utils.KeycloakUtil;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Implementation of study service.
 * 
 * @author msimon
 *
 */
public class StudyServiceImpl implements StudyService {

	
	@Autowired
	private StudyCenterRepository studyCenterRepository;

	@Autowired
	private StudyUserRepository studyUserRepository;
	
	@Autowired
	private SubjectStudyRepository subjectStudyRepository;

	@Autowired
	private StudyRepository studyRepository;


	@Override
	public void deleteById(final Long id) throws EntityNotFoundException, AccessDeniedException {
		final Study study = studyRepository.findOne(id);
		if (study == null) throw new EntityNotFoundException(Study.class, id);
		studyRepository.delete(id);			
	}

	@Override
	public List<Study> findBy(final String fieldName, final Object value) {
		return studyRepository.findBy(fieldName, value);
	}

	@Override
	public Study findById(final Long id) {
		return studyRepository.findOne(id);
	}

	@Override
	public Study save(final Study study) {
		if (study.getStudyCenterList() != null) {
			for (final StudyCenter studyCenter : study.getStudyCenterList()) {
				studyCenter.setStudy(study);			} 

		}
		if (study.getSubjectStudyList() != null) {
			for (final SubjectStudy subjectStudy : study.getSubjectStudyList()) {
				subjectStudy.setStudy(study);
			}
		}
		if (study.getStudyUserList() != null) {
			for (final StudyUser studyUser: study.getStudyUserList()) {
				studyUser.setStudyId(study.getId());
			}			
		}
		return studyRepository.save(study);
	}

	@Override
	public Study update(final Study study) throws EntityNotFoundException, AccessDeniedException {
		final Study studyDb = studyRepository.findOne(study.getId());
		if (studyDb == null) throw new EntityNotFoundException(Study.class, study.getId());
		
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
		final List<SubjectStudy> subjectStudyDbList = studyDb.getSubjectStudyList() != null 
				? new ArrayList<>(studyDb.getSubjectStudyList())
				: new ArrayList<>();
		if (study.getSubjectStudyList() != null) {
			for (final SubjectStudy subjectStudy : study.getSubjectStudyList()) {
				if (subjectStudy.getId() == null) {
					// Add link subject/study
					subjectStudy.setStudy(studyDb);
					studyDb.getSubjectStudyList().add(subjectStudy);
				}
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
		final List<StudyUser> studyUserDbList = studyDb.getStudyUserList() != null 
				? new ArrayList<>(studyDb.getStudyUserList())
				: new ArrayList<>();
		if (study.getStudyUserList() != null) {
			for (final StudyUser studyUser : study.getStudyUserList()) {
				if (studyUser.getId() == null) {
					// Add link study/user
					studyUser.setStudyId(studyDb.getId());
					studyDb.getStudyUserList().add(studyUser);
				} else {
					for (final StudyUser studyUserDb : studyUserDbList) {
						if (studyUserDb.getId() == studyUser.getId()) {
							studyUserDb.setReceiveAnonymizationReport(studyUserDb.isReceiveAnonymizationReport());
							studyUserDb.setReceiveNewImportReport(studyUser.isReceiveNewImportReport());
							studyUserDb.setStudyUserType(studyUser.getStudyUserType());
						}
					}
				}
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
	public List<Study> findAll() {
		if (KeycloakUtil.getTokenRoles().contains("ROLE_ADMIN")) {
			return studyRepository.findAll();
		} else {
			return studyRepository.findByStudyUserList_UserIdAndStudyUserList_StudyUserTypeEqualOrderByNameAsc
					(KeycloakUtil.getTokenUserId(), StudyUserRight.CAN_SEE_ALL.getId());
		}
	}

	@Override
	public List<IdNameDTO> findIdsAndNames() {
		if (KeycloakUtil.getTokenRoles().contains("ROLE_ADMIN")) {
			return studyRepository.findIdsAndNames();
		} else {
			return studyRepository.findIdsAndNamesByStudyUserList_UserIdAndStudyUserList_StudyUserTypeEqualOrderByNameAsc
					(KeycloakUtil.getTokenUserId(), StudyUserRight.CAN_SEE_ALL.getId());
		}
	}

}

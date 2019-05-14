package org.shanoir.ng.study.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.shanoir.ng.messaging.StudyUserUpdateBroadcastService;
import org.shanoir.ng.shared.exception.EntityNotFoundException;
import org.shanoir.ng.shared.exception.MicroServiceCommunicationException;
import org.shanoir.ng.shared.security.rights.StudyUserRight;
import org.shanoir.ng.study.model.Study;
import org.shanoir.ng.study.repository.StudyRepository;
import org.shanoir.ng.study.repository.StudyUserRepository;
import org.shanoir.ng.study.rights.StudyUser;
import org.shanoir.ng.studycenter.StudyCenter;
import org.shanoir.ng.studycenter.StudyCenterRepository;
import org.shanoir.ng.subjectstudy.model.SubjectStudy;
import org.shanoir.ng.subjectstudy.repository.SubjectStudyRepository;
import org.shanoir.ng.utils.KeycloakUtil;
import org.shanoir.ng.utils.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Implementation of study service.
 * 
 * @author msimon
 *
 */
@Component
public class StudyServiceImpl implements StudyService {

	private static final Logger LOG = LoggerFactory.getLogger(StudyServiceImpl.class);
	
	@Autowired
	private StudyCenterRepository studyCenterRepository;

	@Autowired
	private StudyUserRepository studyUserRepository;
	
	@Autowired
	private SubjectStudyRepository subjectStudyRepository;

	@Autowired
	private StudyRepository studyRepository;
	
	@Autowired
	private StudyUserUpdateBroadcastService studyUserCom;


	@Override
	public void deleteById(final Long id) throws EntityNotFoundException {
		final Study study = studyRepository.findOne(id);
		if (study == null) throw new EntityNotFoundException(Study.class, id);
		studyRepository.delete(id);			
	}

	@Override
	public Study findById(final Long id) {
		return studyRepository.findOne(id);
	}

	@Override
	public Study create(final Study study) {
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
	public Study update(final Study study) throws EntityNotFoundException {
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

		updateStudyUsers(studyDb, study.getStudyUserList());
		studyRepository.save(studyDb);

		return studyDb;
	}

	@Override
	public List<Study> findAll() {
		// Utils.copyList is used to prevent a bug with @PostFilter
		if (KeycloakUtil.getTokenRoles().contains("ROLE_ADMIN")) {
			return Utils.copyList(studyRepository.findAll());
		} else {
			return Utils.copyList(studyRepository.findByStudyUserList_UserIdAndStudyUserList_StudyUserRights_OrderByNameAsc
					(KeycloakUtil.getTokenUserId(), StudyUserRight.CAN_SEE_ALL.getId()));
		}
	}

	private void updateStudyUsers(Study study, List<StudyUser> studyUsers) {
		if (studyUsers == null) return;
		// New lists of created / updated to send via RabbitMQ
		List<StudyUser> toBeCreated = new ArrayList<>();
		List<StudyUser> toBeUpdated = new ArrayList<>();
		List<StudyUser> toBeDeleted = new ArrayList<>();

		// Build maps of existing / replacing study users
		Map<Long, StudyUser> existing = new HashMap<>();
		for (StudyUser su : study.getStudyUserList()) existing.put(su.getId(), su);
		Map<Long, StudyUser> replacing = new HashMap<>();
		for (StudyUser su : studyUsers) {
			if (su.getId() == null) toBeCreated.add(su);
			else replacing.put(su.getId(), su);
		}
		
		// Buid sets of ids to know which ones need to be deleted / updated / created
		Set<Long> idsToBeDeleted = new HashSet<>(existing.keySet());
		idsToBeDeleted.removeAll(replacing.keySet());
		Set<Long> idsToBeUpdated = new HashSet<>(replacing.keySet());
		idsToBeUpdated.removeAll(idsToBeDeleted);		
		
		// For those which need an update, update them with the replacing values
		for (Long id : idsToBeUpdated) {
			StudyUser existingSu = existing.get(id);
			StudyUser replacingSu = replacing.get(id);
			existingSu.setReceiveAnonymizationReport(replacingSu.isReceiveAnonymizationReport());
			existingSu.setReceiveNewImportReport(replacingSu.isReceiveNewImportReport());
			existingSu.setStudyUserRights(replacingSu.getStudyUserRights());
			toBeUpdated.add(existingSu);
		}
		
		// For those which need to be added, add them.
		for (StudyUser su : toBeCreated) {
			su.setStudyId(study.getId());
		}
		studyUserRepository.save(toBeCreated);
		study.getStudyUserList().addAll(toBeCreated);

		// Deletes deleted here. Updates / creates will be done by saving the study
		for (Long id : idsToBeDeleted) {
			StudyUser su = studyUserRepository.findOne(id);
			su.setStudyId(null);
			su.setUserId(null);
			su.setStudyUserRights(new ArrayList<StudyUserRight>());
			toBeDeleted.add(su);
		}
		studyUserRepository.delete(toBeDeleted);
		
		// Send updates via RabbitMQ
		try {
			studyUserCom.broadcastDelete(idsToBeDeleted.toArray(new Long[0]));
			studyUserCom.broadcastCreate(toBeCreated.toArray(new StudyUser[0]));
			studyUserCom.broadcastUpdate(toBeUpdated.toArray(new StudyUser[0]));
		} catch (MicroServiceCommunicationException e) {
			LOG.error("Could not transmit study-user delete info through RabbitMQ");
		}
	}
}

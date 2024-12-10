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

package org.shanoir.ng.study.service;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.io.FileUtils;
import org.shanoir.ng.center.model.Center;
import org.shanoir.ng.center.repository.CenterRepository;
import org.shanoir.ng.messaging.StudyUserUpdateBroadcastService;
import org.shanoir.ng.shared.configuration.RabbitMQConfiguration;
import org.shanoir.ng.shared.email.EmailStudyUsersAdded;
import org.shanoir.ng.shared.event.ShanoirEvent;
import org.shanoir.ng.shared.event.ShanoirEventService;
import org.shanoir.ng.shared.event.ShanoirEventType;
import org.shanoir.ng.shared.exception.EntityNotFoundException;
import org.shanoir.ng.shared.exception.MicroServiceCommunicationException;
import org.shanoir.ng.shared.exception.ShanoirException;
import org.shanoir.ng.shared.security.rights.StudyUserRight;
import org.shanoir.ng.study.dto.StudyDTO;
import org.shanoir.ng.study.dto.StudyStatisticsDTO;
import org.shanoir.ng.study.dto.StudyStorageVolumeDTO;
import org.shanoir.ng.study.dto.mapper.StudyMapper;
import org.shanoir.ng.study.dua.DataUserAgreement;
import org.shanoir.ng.study.dua.DataUserAgreementService;
import org.shanoir.ng.study.model.Study;
import org.shanoir.ng.study.model.StudyUser;
import org.shanoir.ng.study.repository.StudyRepository;
import org.shanoir.ng.study.repository.StudyUserRepository;
import org.shanoir.ng.study.rights.command.CommandType;
import org.shanoir.ng.study.rights.command.StudyUserCommand;
import org.shanoir.ng.studycenter.StudyCenter;
import org.shanoir.ng.studycenter.StudyCenterRepository;
import org.shanoir.ng.studyexamination.StudyExamination;
import org.shanoir.ng.studyexamination.StudyExaminationRepository;
import org.shanoir.ng.subject.model.Subject;
import org.shanoir.ng.subject.repository.SubjectRepository;
import org.shanoir.ng.subject.service.SubjectService;
import org.shanoir.ng.subjectstudy.model.SubjectStudy;
import org.shanoir.ng.subjectstudy.model.SubjectStudyTag;
import org.shanoir.ng.subjectstudy.repository.SubjectStudyRepository;
import org.shanoir.ng.tag.model.StudyTag;
import org.shanoir.ng.tag.model.Tag;
import org.shanoir.ng.tag.repository.TagRepository;
import org.shanoir.ng.utils.KeycloakUtil;
import org.shanoir.ng.utils.ListDependencyUpdate;
import org.shanoir.ng.utils.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.transaction.Transactional;

/**
 * Implementation of study service.
 *
 * @author msimon
 * @author mkain
 *
 */
@Component
public class StudyServiceImpl implements StudyService {

	private static final Logger LOG = LoggerFactory.getLogger(StudyServiceImpl.class);

	@Autowired
	private StudyUserRepository studyUserRepository;

	@Autowired
	private StudyRepository studyRepository;

	@Autowired
	private CenterRepository centerRepository;

	@Autowired
	private SubjectRepository subjectRepository;

	@Autowired
	private StudyUserUpdateBroadcastService studyUserCom;

	@Autowired
	private DataUserAgreementService dataUserAgreementService;

	@Autowired
	private RabbitTemplate rabbitTemplate;

	@Autowired
	private StudyMapper studyMapper;

	@Value("${studies-data}")
	private String dataDir;

	@Autowired
	private ObjectMapper objectMapper;

	@Autowired
	private SubjectService subjectService;

	@Autowired
	private SubjectStudyRepository subjectStudyRepository;

	@Autowired
	private StudyExaminationRepository studyExaminationRepository;

	@Autowired
	private StudyCenterRepository studyCenterRepository;

	@Autowired
	private ShanoirEventService eventService;
  
	@Autowired
	private TagRepository tagRepository;

	@Override
	@Transactional
	public void deleteById(final Long id) throws EntityNotFoundException {
		final Study study = studyRepository.findById(id).orElse(null);
		if (study == null) {
			throw new EntityNotFoundException(Study.class, id);
		}

		if (study.getStudyUserList() != null) {
			List<StudyUserCommand> commands = new ArrayList<>();
			for (StudyUser su : study.getStudyUserList()) {
				commands.add(new StudyUserCommand(CommandType.DELETE, su.getId()));
			}
			try {
				studyUserCom.broadcast(commands);
			} catch (MicroServiceCommunicationException e) {
				LOG.error("Could not transmit study-user delete info through RabbitMQ", e);
			}
		}
		studyRepository.delete(study);
	}

	@Override
	@Transactional
	public Study findById(final Long id) {
		return studyRepository.findById(id).orElse(null);
	}

	@Override
	public Study create(final Study study) throws MicroServiceCommunicationException {
		if (study.getStudyCenterList() != null) {
			for (final StudyCenter studyCenter : study.getStudyCenterList()) {
				studyCenter.setStudy(study);
			}
		}

		if (study.getSubjectStudyList() != null) {
			for (SubjectStudy subjectStudy : study.getSubjectStudyList()) {
				subjectStudy.setStudy(study);
			}
		}

		if (study.getTags() != null) {
			for (final Tag tag : study.getTags()) {
				tag.setStudy(study);
			}
		}
		if (study.getStudyTags() != null) {
			for (final StudyTag tag : study.getStudyTags()) {
				tag.setStudy(study);
			}
		}

		if (study.getStudyUserList() != null) {
			for (final StudyUser studyUser : study.getStudyUserList()) {
				// if dua file exists, set StudyUser to confirmed false
				if (study.getDataUserAgreementPaths() != null && !study.getDataUserAgreementPaths().isEmpty()) {
					studyUser.setConfirmed(false);
				} else {
					studyUser.setConfirmed(true);
				}
				studyUser.setStudy(study);
				// The user that creates a study is confirmed (he's the one uploading the DUA, he doesn't have to sign it)
				if (KeycloakUtil.getTokenUserId().equals(studyUser.getUserId())) {
					studyUser.setConfirmed(true);
				}
			}
		}

		List<SubjectStudy> subjectStudyListSave = null;
		if (study.getSubjectStudyList() != null) {
			subjectStudyListSave = new ArrayList<SubjectStudy>(study.getSubjectStudyList());
		}
		Map<Long, List<SubjectStudyTag>> subjectStudyTagSave = new HashMap<>();
		study.setSubjectStudyList(null);
		Study studyDb = studyRepository.save(study);

		if (subjectStudyListSave != null && !subjectStudyListSave.isEmpty()) {
			updateTags(subjectStudyListSave, studyDb.getTags());
			studyDb.setSubjectStudyList(new ArrayList<>());
			for (SubjectStudy subjectStudy : subjectStudyListSave) {
				SubjectStudy newSubjectStudy = new SubjectStudy();
				newSubjectStudy.setPhysicallyInvolved(subjectStudy.isPhysicallyInvolved());
				newSubjectStudy.setSubject(subjectStudy.getSubject());
				newSubjectStudy.setSubjectStudyIdentifier(subjectStudy.getSubjectStudyIdentifier());
				newSubjectStudy.setSubjectType(subjectStudy.getSubjectType());
				newSubjectStudy.setStudy(studyDb);
				subjectStudyTagSave.put(subjectStudy.getSubject().getId(), subjectStudy.getSubjectStudyTags());
				studyDb.getSubjectStudyList().add(newSubjectStudy);
			}
			studyDb = studyRepository.save(studyDb);

			for (SubjectStudy subjectStudy : studyDb.getSubjectStudyList()) {
				subjectStudy.setSubjectStudyTags(subjectStudyTagSave.get(subjectStudy.getSubject().getId()));
				for (SubjectStudyTag ssTag : subjectStudy.getSubjectStudyTags()) {
					ssTag.setSubjectStudy(subjectStudy);
				}
			}
			studyDb = studyRepository.save(studyDb);
		}

		updateStudyName(studyMapper.studyToStudyDTODetailed(studyDb));

		if (studyDb.getStudyUserList() != null) {
			List<StudyUserCommand> commands = new ArrayList<>();
			for (final StudyUser studyUser : studyDb.getStudyUserList()) {
				// create a DUA for user in study, if dua file exists
				if (study.getDataUserAgreementPaths() != null && !study.getDataUserAgreementPaths().isEmpty()) {
					dataUserAgreementService.createDataUserAgreementForUserInStudy(study, studyUser.getUserId());
				}
				commands.add(new StudyUserCommand(CommandType.CREATE, studyUser));
			}
			try {
				studyUserCom.broadcast(commands);
			} catch (MicroServiceCommunicationException e) {
				LOG.error("Could not transmit study-user create info through RabbitMQ", e);
			}

			// Use newly created study "studyDb" to decide, to send email to which user
			sendStudyUserReport(studyDb, studyDb.getStudyUserList());
		}

		return studyDb;
	}

	@Override
	@Transactional(rollbackOn = {ShanoirException.class})
	public Study update(Study study) throws ShanoirException {
		Study studyDb = studyRepository.findById(study.getId()).orElse(null);

		List<Long> tagsToDelete = getTagsToDelete(study, studyDb);
		List<Long> studyTagsToDelete = getStudyTagsToDelete(study, studyDb);

		if (studyDb == null) {
			throw new EntityNotFoundException(Study.class, study.getId());
		}

		studyDb.setClinical(study.isClinical());
		studyDb.setDownloadableByDefault(study.isDownloadableByDefault());
		studyDb.setEndDate(study.getEndDate());
		studyDb.setChallenge(study.isChallenge());
		studyDb.setName(study.getName());
		studyDb.setProfile(study.getProfile());
		studyDb.setDescription(study.getDescription());
		studyDb.setLicense(study.getLicense());
		studyDb.setStudyStatus(study.getStudyStatus());
		studyDb.setVisibleByDefault(study.isVisibleByDefault());
		studyDb.setStudyCardPolicy(study.getStudyCardPolicy());
		studyDb.setWithExamination(study.isWithExamination());
		studyDb.setMonoCenter(study.isMonoCenter());

		if (study.getStudyCenterList() != null) {
			ListDependencyUpdate.updateWith(studyDb.getStudyCenterList(), study.getStudyCenterList());
			for (StudyCenter studyCenter : studyDb.getStudyCenterList()) {
				studyCenter.setStudy(studyDb);
			}
		}

		if (study.getTags() != null) {
			ListDependencyUpdate.updateWithNoRemove(studyDb.getTags(), study.getTags());
			for (Tag tag : studyDb.getTags()) {
				tag.setStudy(studyDb);
			}
		}
		if (study.getStudyTags() != null) {
			ListDependencyUpdate.updateWithNoRemove(studyDb.getStudyTags(), study.getStudyTags());
			for (StudyTag tag : studyDb.getStudyTags()) {
				tag.setStudy(studyDb);
			}
		}

		List<Subject> toBeDeleted = new ArrayList<Subject>();

		if (study.getSubjectStudyList() != null) {

			// Find all ids from new study
			Set<Long> updatedIds = new HashSet<>();
			for (SubjectStudy entity : study.getSubjectStudyList()) {
				updatedIds.add(entity.getId());
			}

			// Find deleted subject study so we can eventualy delete subjects
			List<Subject> removed = new ArrayList<Subject>();

			for (SubjectStudy subjectStudyDb : studyDb.getSubjectStudyList()) {
				if(!updatedIds.contains(subjectStudyDb.getId())) {
					Subject sub = subjectStudyDb.getSubject();
					removed.add(sub);

					eventService.publishEvent(
							new ShanoirEvent(
									ShanoirEventType.REMOVE_SUBJECT_FROM_STUDY_EVENT,
									sub.getId().toString(),
									KeycloakUtil.getTokenUserId(),
									"Subject " + sub.getName() + " (id: " + sub.getId() + ") removed from study " + study.getName() + " (id: " + study.getId() + ")",
									ShanoirEvent.SUCCESS,
									study.getId())
					);

				}
			}
			for (Subject subject : removed) {
				if (this.subjectStudyRepository.countBySubject(subject) == 1L) {
					toBeDeleted.add(subject);
				}
			}
		}

		if (studyDb.getProtocolFilePaths() != null) {
			for (String filePath : studyDb.getProtocolFilePaths()) {
				if (!study.getProtocolFilePaths().contains(filePath)) {
					// Delete file
					String filePathToDelete = getStudyFilePath(studyDb.getId(), filePath);
					FileUtils.deleteQuietly(new File(filePathToDelete));
				}
			}
		}

		studyDb.setProtocolFilePaths(study.getProtocolFilePaths());

		updateStudyUsers(studyDb, study);

		if (study.getDataUserAgreementPaths() != null) { // do this after updateStudyUsers
			studyDb.setDataUserAgreementPaths(study.getDataUserAgreementPaths());
		}

		studyDb = studyRepository.save(studyDb);

		if (study.getSubjectStudyList() != null) {
			List<SubjectStudy> subjectStudyListDb = studyDb.getSubjectStudyList();
			List<SubjectStudy> subjectStudyListNew = study.getSubjectStudyList();
			subjectStudyListDb.clear();
			subjectStudyListDb.addAll(subjectStudyListNew);
			for (SubjectStudy dbSubjectStudy : subjectStudyListDb) {
				dbSubjectStudy.setStudy(studyDb);
			}
			studyDb = studyRepository.save(studyDb);
		}

		if (studyDb.getTags() != null) {
			studyDb.getTags().removeIf(tag -> tagsToDelete.contains(tag.getId()));
			studyDb = studyRepository.save(studyDb);
		}
		if (studyDb.getStudyTags() != null) {
			studyDb.getStudyTags().removeIf(tag -> studyTagsToDelete.contains(tag.getId()));
			studyDb = studyRepository.save(studyDb);
		}

		String error = this.updateStudyName(studyMapper.studyToStudyDTODetailed(studyDb));

		// Actually delete subjects
		for (Subject subjectToDelete : toBeDeleted) {
			subjectService.deleteById(subjectToDelete.getId());
		}

		if (error != null && !error.isEmpty()) {
			LOG.error("Study [" + studyDb.getId() + "] couldn't be sync with datasets microservice : {}", error);
			throw new ShanoirException(error);
		}

		return studyDb;
	}

	/**
	 * For each subject study tag of study, set the fresh tag id by looking into studyDb tags, 
	 * then update db subject study tags lists with the given study
	 *
	 * @param subjectStudyList
	 * @param dbStudyTags
	 * @return updated study
	 */
	private void updateTags(List<SubjectStudy> subjectStudyList, List<Tag> dbStudyTags) {
		if (subjectStudyList == null || dbStudyTags == null) {
			return;
		}
		for (SubjectStudy subjectStudy : subjectStudyList) {
			if (subjectStudy.getTags() == null) {
				continue;
			}
			for (Tag tag : subjectStudy.getTags()) {
				if (tag.getId() == null) {
					Tag dbTag = dbStudyTags.stream().filter(
							upTag -> upTag.getColor().equals(tag.getColor())
									&& upTag.getName().equals(tag.getName())
					).findFirst().orElse(null);
					if (dbTag == null) {
						throw new IllegalStateException("Cannot link a new tag to a subject-study, this tag does not exist in the study");
					}
					tag.setId(dbTag.getId());
				}
			}
		}
	}

	private List<Long> getTagsToDelete(Study study, Study studyDb) {
		List<Long> tagsToDelete = new ArrayList<>();
		if (studyDb.getTags() != null && study.getTags() != null) {
			for (Tag dbTag : studyDb.getTags()) {
				boolean found = false;
				for (Tag tag : study.getTags()) {
					if (tag.getId() != null && tag.getId().equals(dbTag.getId())) {
						found = true;
						break;
					}
				}
				if (!found) tagsToDelete.add(dbTag.getId());
			}
		}
		return tagsToDelete;
	}

	private List<Long> getStudyTagsToDelete(Study study, Study studyDb) {
		List<Long> studyTagsToDelete = new ArrayList<>();
		if (studyDb.getStudyTags() != null && study.getStudyTags() != null) {
			for (StudyTag dbStudyTag : studyDb.getStudyTags()) {
				boolean found = false;
				for (StudyTag studyTag : study.getStudyTags()) {
					if (studyTag.getId() != null && studyTag.getId().equals(dbStudyTag.getId())) {
						found = true;
						break;
					}
				}
				if (!found) studyTagsToDelete.add(dbStudyTag.getId());
			}
		}
		return studyTagsToDelete;
	}

	/**
	 * Gets the protocol or data user agreement file path
	 *
	 * @param studyId  id of the study
	 * @param fileName name of the file
	 * @return the file path of the file
	 */
	@Override
	public String getStudyFilePath(Long studyId, String fileName) {
		return dataDir + "/study-" + studyId + "/" + fileName;
	}

	@Override
	@Transactional
	public List<Study> findAll() {
		List<Study> studies;
		if (KeycloakUtil.getTokenRoles().contains("ROLE_ADMIN")) {
			studies = studyRepository.findAll();
		} else {
			studies = studyRepository
					.findByStudyUserList_UserIdAndStudyUserList_StudyUserRightsAndStudyUserList_Confirmed_OrderByNameAsc(
							KeycloakUtil.getTokenUserId(), StudyUserRight.CAN_SEE_ALL.getId(), true);
		}
		setNumberOfSubjectsAndExaminations(studies);
		setFilePaths(studies);
		// Utils.copyList is used to prevent a bug with @PostFilter
		return Utils.copyList(studies);
	}

	/**
	 * This method optimizes the queries to the database, only two selects to get all counts.
	 * Instead of x00+ selects for all studies in Shanoir. Use HashMap to avoid N+N iteration.
	 * @param studies
	 */
	private void setNumberOfSubjectsAndExaminations(List<Study> studies) {
		List<Object[]> subjectsCount = subjectStudyRepository.countByStudyIdGroupBy();
		HashMap<Long, Long> studyIdSubjectsCountMap = new HashMap<>();
		for (Object[] row : subjectsCount) {
			Long studyId = (Long) row[0];
			Long count = (Long) row[1];
			studyIdSubjectsCountMap.put(studyId, count);
		}
		List<Object[]> examinationsCount = studyExaminationRepository.countByStudyIdGroupBy();
		HashMap<Long, Long> studyIdExaminationsCountMap = new HashMap<>();
		for (Object[] row : examinationsCount) {
			Long studyId = (Long) row[0];
			Long count = (Long) row[1];
			studyIdExaminationsCountMap.put(studyId, count);
		}
		studies.stream().forEach(s -> {
			Long nbSubjects = studyIdSubjectsCountMap.get(s.getId());
			if (nbSubjects != null) {
				s.setNbSubjects(nbSubjects.intValue());
			} else {
				s.setNbSubjects(0);
			}
			Long nbExaminations = studyIdExaminationsCountMap.get(s.getId());
			if (nbExaminations != null) {
				s.setNbExaminations(nbExaminations.intValue());
			} else {
				s.setNbExaminations(0);
			}
		});
	}

	private void setFilePaths(List<Study> studies) {
		studies.stream().forEach(s -> {
			s.setProtocolFilePaths(studyRepository.findProtocolFilePathsByStudyId(s.getId()));
			s.setDataUserAgreementPaths(studyRepository.findDataUserAgreementPathsByStudyId(s.getId()));
		});
	}

	@Transactional
	protected void updateStudyUsers(Study studyDb, Study study) {
		if (study.getStudyUserList() == null) {
			study.setStudyUserList(new ArrayList<>());
		}
		// New lists of created / updated to send via RabbitMQ
		List<StudyUser> toBeCreated = new ArrayList<>();
		List<StudyUser> toBeUpdated = new ArrayList<>();

		// Build maps of existing / replacing study users
		Map<Long, StudyUser> existing = new HashMap<>();
		for (StudyUser su : studyDb.getStudyUserList()) {
			existing.put(su.getId(), su);
		}

		boolean addNewDua = CollectionUtils.isEmpty(studyDb.getDataUserAgreementPaths()) && !CollectionUtils.isEmpty(study.getDataUserAgreementPaths());
		boolean deleteDua = !CollectionUtils.isEmpty(studyDb.getDataUserAgreementPaths()) && CollectionUtils.isEmpty(study.getDataUserAgreementPaths());
		boolean updateDua = !CollectionUtils.isEmpty(studyDb.getDataUserAgreementPaths())
				&& !CollectionUtils.isEmpty(study.getDataUserAgreementPaths())
				&& !study.getDataUserAgreementPaths().get(0).equals(studyDb.getDataUserAgreementPaths().get(0));

		Map<Long, StudyUser> replacing = new HashMap<>();
		for (StudyUser su : study.getStudyUserList()) {
			if (su.getId() == null) {
				toBeCreated.add(su);
			} else {
				replacing.put(su.getId(), su);
				if (addNewDua || updateDua) {
					// new / updated DUA added to study
					su.setConfirmed(false);
					// Remove current unfinished signing
					dataUserAgreementService.deleteIncompleteDataUserAgreementForUserInStudy(studyDb, su.getUserId());
					// Create a new dataset user agreement
					DataUserAgreement duaSigning = dataUserAgreementService.createDataUserAgreementForUserInStudy(studyDb, su.getUserId());
					// The user that creates a study is confirmed (he's the one uploading the DUA, he doesn't have to sign it)
					if (KeycloakUtil.getTokenUserId().equals(su.getUserId())) {
						su.setConfirmed(true);
						duaSigning.setTimestampOfAccepted(new Date());
						dataUserAgreementService.update(duaSigning);
					}
					this.archiveDuaFile(studyDb);
				} else if (deleteDua){
					// existing DUA removed from study
					this.archiveDuaFile(studyDb);

					su.setConfirmed(true);
					// without DUA all StudyUser are confirmed, set back to true, if false before
					dataUserAgreementService.deleteIncompleteDataUserAgreementForUserInStudy(studyDb, su.getUserId());
				}
			}
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
			existingSu.setReceiveStudyUserReport(replacingSu.isReceiveStudyUserReport());
			existingSu.setReceiveNewImportReport(replacingSu.isReceiveNewImportReport());
			existingSu.setStudyUserRights(replacingSu.getStudyUserRights());
			existingSu.setConfirmed(replacingSu.isConfirmed());
			existingSu.setCenters(replacingSu.getCenters());
			toBeUpdated.add(existingSu);
		}

		// For those which need to be added, add them.
		List<StudyUser> created = new ArrayList<>();
		if (!toBeCreated.isEmpty()) {
			for (StudyUser su : toBeCreated) {
				su.setStudy(studyDb);
			}
			// save them first to get their id
			for (StudyUser su : studyUserRepository.saveAll(toBeCreated)) {
				// add DUA only to newly added StudyUser, not to existing ones
				if (study.getDataUserAgreementPaths() != null && !study.getDataUserAgreementPaths().isEmpty()) {
					su.setConfirmed(false);
					dataUserAgreementService.createDataUserAgreementForUserInStudy(studyDb, su.getUserId());
				} else {
					su.setConfirmed(true);
				}
				created.add(su);
			}
			studyDb.getStudyUserList().addAll(created);
		}

		// Remove deleted: study user + data user agreements
		for (Long studyUserIdToBeDeleted : idsToBeDeleted) {
			StudyUser studyUser = studyUserRepository.findById(studyUserIdToBeDeleted).orElse(null);
			// delete a DUA for removed user in study, if not yet accepted, if dua file
			// exists
			if (studyDb.getDataUserAgreementPaths() != null && !studyDb.getDataUserAgreementPaths().isEmpty()) {
				dataUserAgreementService.deleteIncompleteDataUserAgreementForUserInStudy(studyDb,
						studyUser.getUserId());
			}
		}
		Utils.removeIdsFromList(idsToBeDeleted, studyDb.getStudyUserList());

		// Send updates via RabbitMQ
		try {
			List<StudyUserCommand> commands = new ArrayList<>();
			for (Long id : idsToBeDeleted) {
				commands.add(new StudyUserCommand(CommandType.DELETE, id));
			}
			for (StudyUser su : created) {
				commands.add(new StudyUserCommand(CommandType.CREATE, su));
			}
			for (StudyUser su : toBeUpdated) {
				commands.add(new StudyUserCommand(CommandType.UPDATE, su));
			}
			studyUserCom.broadcast(commands);
		} catch (MicroServiceCommunicationException e) {
			LOG.error("Could not transmit study-user update info through RabbitMQ", e);
		}

		// Use updated study "study" to decide, to send email to which user
		sendStudyUserReport(study, created);
	}

	private void archiveDuaFile(Study study) {
		if (CollectionUtils.isEmpty(study.getDataUserAgreementPaths())) {
			return;
		}
		// Archive old DUA -> Rename it with deletion date
		SimpleDateFormat formatter = new SimpleDateFormat("yyyymmddHHMM");
		File deletedFile = new File(this.getStudyFilePath(study.getId(), study.getDataUserAgreementPaths().get(0)));
		File archiveFile = new File(this.getStudyFilePath(study.getId(), "archive_" + formatter.format(new Date()) + "_" + study.getDataUserAgreementPaths().get(0)));
		deletedFile.renameTo(archiveFile);
	}

	private void sendStudyUserReport(Study study, List<StudyUser> created) {
		// Get all recipients
		List<Long> recipients = new ArrayList<Long>();
		List<StudyUser> studyUsers = study.getStudyUserList();
		for (StudyUser studyUser : studyUsers) {
			if (studyUser.isReceiveStudyUserReport()) {
				recipients.add(studyUser.getUserId());
			}
		}
		// do nothing, in case no users should receive study user report/mail
		if (!created.isEmpty()) {
			EmailStudyUsersAdded emailStudyUserAdded = new EmailStudyUsersAdded();
			emailStudyUserAdded.setRecipients(recipients);
			final Long userId = KeycloakUtil.getTokenUserId();
			emailStudyUserAdded.setUserId(userId);
			emailStudyUserAdded.setStudyId(study.getId().toString());
			emailStudyUserAdded.setStudyName(study.getName());
			List<Long> studyUserIds = created.stream().map(StudyUser::getUserId).collect(Collectors.toList());
			emailStudyUserAdded.setStudyUsers(studyUserIds);
			try {
				rabbitTemplate.convertAndSend(RabbitMQConfiguration.STUDY_USER_MAIL_QUEUE,
						objectMapper.writeValueAsString(emailStudyUserAdded));
			} catch (AmqpException | JsonProcessingException e) {
				LOG.error("Could not send email for study user report. ", e);
			}
		}
	}

	@Override
	public void addStudyUserToStudy(StudyUser studyUser, Study study) {
		studyUserRepository.save(studyUser);
		// Send updates via RabbitMQ
		try {
			List<StudyUserCommand> commands = new ArrayList<>();
			commands.add(new StudyUserCommand(CommandType.CREATE, studyUser));
			studyUserCom.broadcast(commands);
		} catch (MicroServiceCommunicationException e) {
			LOG.error("Could not transmit study-user create info through RabbitMQ", e);
		}

		// Use study "study" to decide, to send email to which user
		List<StudyUser> created = new ArrayList<>();
		created.add(studyUser);
		sendStudyUserReport(study, created);
	}

	@Override
	public void removeStudyUserFromStudy(Long studyId, Long userId) {
		StudyUser studyUser = studyUserRepository.findByUserIdAndStudy_Id(userId, studyId);
		try {
			List<StudyUserCommand> commands = new ArrayList<>();
			commands.add(new StudyUserCommand(CommandType.DELETE, studyUser.getId()));
			this.studyUserCom.broadcast(commands);
			this.studyUserRepository.delete(studyUser);

		} catch (MicroServiceCommunicationException e) {
			LOG.error("Failed to remove studyUser from study: ", e);
		}
	}

	public String updateStudyName(StudyDTO study) throws MicroServiceCommunicationException {
		try {
			return (String) rabbitTemplate.convertSendAndReceive(RabbitMQConfiguration.STUDY_NAME_UPDATE_QUEUE,
					objectMapper.writeValueAsString(study));
		} catch (AmqpException | JsonProcessingException e) {
			throw new MicroServiceCommunicationException(
					"Error while communicating with datasets MS to update study name.", e);
		}
	}

	@Override
	public void addExaminationToStudy(Long examinationId, Long studyId, Long centerId, Long subjectId) {
		// Update study_examination table
		Optional<Study> studyOpt = this.studyRepository.findById(studyId);
		Optional<Center> centerOpt = this.centerRepository.findById(centerId);
		Optional<Subject> subjectOpt = this.subjectRepository.findById(subjectId);

		if (studyOpt.isPresent() && centerOpt.isPresent() && subjectOpt.isPresent()) {
			Study study = studyOpt.get();
			Set<StudyExamination> exams = study.getExaminations();
			if (exams == null) {
				exams = new HashSet<>();
				study.setExaminations(exams);
			}
			StudyExamination studyExam = new StudyExamination(examinationId, study, centerOpt.get(), subjectOpt.get());
			exams.add(studyExam);
			this.studyRepository.save(study);
		}
	}

	@Override
	public void deleteExamination(Long examinationId, Long studyId) {
		// Update study_examination table
		Optional<Study> studyOpt = this.studyRepository.findById(studyId);
		if (studyOpt.isPresent()) {
			Study study = studyOpt.get();
			Set<StudyExamination> exams = study.getExaminations();
			if (exams == null) {
				exams = new HashSet<>();
			} else {
				exams = exams.stream().filter(studyExam -> !studyExam.getExaminationId().equals(examinationId)).collect(Collectors.toSet());
			}
			study.setExaminations(exams);
			this.studyRepository.save(study);
		}
	}

	@Override
	public List<Study> findChallenges() {
		// Utils.copyList is used to prevent a bug with @PostFilter
		return Utils.copyList(studyRepository.findByChallengeTrue());
	}

	@Override
	public List<Study> findPublicStudies() {
		List<Study> studies = this.studyRepository.findByVisibleByDefaultTrue();
		setNumberOfSubjectsAndExaminations(studies);
		return studies;
	}

	@Override
	public StudyStorageVolumeDTO getDetailedStorageVolume(Long studyId){
		StudyStorageVolumeDTO dto;
		try {
			String dtoAsString = (String) this.rabbitTemplate.convertSendAndReceive(RabbitMQConfiguration.STUDY_DATASETS_DETAILED_STORAGE_VOLUME, studyId);
			if(dtoAsString != null && !dtoAsString.isEmpty()){
				dto = objectMapper.readValue(dtoAsString, StudyStorageVolumeDTO.class);
			}else{
				dto = new StudyStorageVolumeDTO();
			}
		} catch (AmqpException | JsonProcessingException e) {
			LOG.error("Error while fetching study [{}] datasets volume storage details.", studyId, e);
			return null;
		}
		long filesSize = this.getStudyFilesSize(studyId);
		dto.setExtraDataSize(filesSize + dto.getExtraDataSize());
		dto.setTotal(filesSize + dto.getTotal());
		return dto;
	}

	@Override
	public Map<Long, StudyStorageVolumeDTO> getDetailedStorageVolumeByStudy(List<Long> studyIds) {

		Map<Long, StudyStorageVolumeDTO> detailedStorageVolumes;
		try {
			String resultAsString = (String) this.rabbitTemplate.convertSendAndReceive(RabbitMQConfiguration.STUDY_DATASETS_TOTAL_STORAGE_VOLUME, studyIds);
			if(resultAsString != null && !resultAsString.isEmpty()){
				detailedStorageVolumes = objectMapper.readValue(resultAsString,  new TypeReference<HashMap<Long, StudyStorageVolumeDTO>>() {});
			}else{
				return new HashMap<>();
			}
		} catch (AmqpException | JsonProcessingException e) {
			LOG.error("Error while fetching studies [{}] datasets volume storage details.", studyIds, e);
			return null;
		}

		this.studyRepository.findAllById(studyIds).forEach( study -> {
					if(!detailedStorageVolumes.containsKey(study.getId())){
						return;
					}
					Long filesSize = this.getStudyFilesSize(study);
					StudyStorageVolumeDTO dto = detailedStorageVolumes.get(study.getId());
					dto.setExtraDataSize(filesSize + dto.getExtraDataSize());
					dto.setTotal(filesSize + dto.getTotal());
				}
		);

		return detailedStorageVolumes;
	}

	private long getStudyFilesSize(Long studyId){
		Optional<Study> study = this.studyRepository.findById(studyId);
		return study.map(this::getStudyFilesSize).orElse(0L);

	}

	private long getStudyFilesSize(Study study){

		List<String> paths = Stream.of(study.getDataUserAgreementPaths(), study.getProtocolFilePaths())
				.flatMap(Collection::stream)
				.collect(Collectors.toList());

		long size = 0L;

		for (String path : paths) {
			File f = new File(this.getStudyFilePath(study.getId(), path));
			if(f.exists()){
				size += f.length();
			}
		}

		return size;
	}

	@Override
	@Transactional
	public List<StudyStatisticsDTO> queryStudyStatistics(Long studyId) throws Exception {
		return studyRepository.queryStudyStatistics(studyId);
	}

	@Override
	public List<Tag> getTagsFromStudy(Long studyId) {
		return tagRepository.findByStudyId(studyId);
	}
}

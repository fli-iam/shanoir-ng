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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.transaction.Transactional;

import org.apache.commons.io.FileUtils;
import org.shanoir.ng.messaging.StudyUserUpdateBroadcastService;
import org.shanoir.ng.shared.configuration.RabbitMQConfiguration;
import org.shanoir.ng.shared.core.model.IdName;
import org.shanoir.ng.shared.exception.EntityNotFoundException;
import org.shanoir.ng.shared.exception.MicroServiceCommunicationException;
import org.shanoir.ng.shared.security.rights.StudyUserRight;
import org.shanoir.ng.study.dua.DataUserAgreementService;
import org.shanoir.ng.study.model.Study;
import org.shanoir.ng.study.model.StudyUser;
import org.shanoir.ng.study.repository.StudyRepository;
import org.shanoir.ng.study.repository.StudyUserRepository;
import org.shanoir.ng.study.rights.command.CommandType;
import org.shanoir.ng.study.rights.command.StudyUserCommand;
import org.shanoir.ng.studycenter.StudyCenter;
import org.shanoir.ng.subjectstudy.model.SubjectStudy;
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
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

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
	private StudyUserUpdateBroadcastService studyUserCom;
	
	@Autowired
	private DataUserAgreementService dataUserAgreementService;

	@Autowired
	private RabbitTemplate rabbitTemplate;
	
	@Value("${studies-data}")
	private String dataDir;

	@Override
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
				LOG.error("Could not transmit study-user delete info through RabbitMQ");
			}
		}
		
		studyRepository.deleteById(id);
	}

	@Override
	public Study findById(final Long id) {
		return studyRepository.findById(id).orElse(null);
	}

	@Override
	public Study create(final Study study) throws MicroServiceCommunicationException {
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
				// if dua file exists, set StudyUser to confirmed false
				if (study.getDataUserAgreementPaths() != null && !study.getDataUserAgreementPaths().isEmpty()) {
					studyUser.setConfirmed(false);
				} else {
					studyUser.setConfirmed(true);
				}
				studyUser.setStudy(study);
			}
		}
		Study studyDb = studyRepository.save(study);
		
		updateStudyName(new IdName(study.getId(), study.getName()));
		
		if (studyDb.getStudyUserList() != null) {
			List<StudyUserCommand> commands = new ArrayList<>();
			for (final StudyUser studyUser: studyDb.getStudyUserList()) {
				// create a DUA for user in study, if dua file exists
				if (study.getDataUserAgreementPaths() != null && !study.getDataUserAgreementPaths().isEmpty()) {
					dataUserAgreementService.createDataUserAgreementForUserInStudy(study, studyUser.getUserId());
				}
				commands.add(new StudyUserCommand(CommandType.CREATE, studyUser));
			}
			try {
				studyUserCom.broadcast(commands);
			} catch (MicroServiceCommunicationException e) {
				LOG.error("Could not transmit study-user create info through RabbitMQ");
			}
		}
		
		return studyDb;
	}

	@Override
	public Study update(final Study study) throws EntityNotFoundException, MicroServiceCommunicationException {
		final Study studyDb = studyRepository.findById(study.getId()).orElse(null);
		if (studyDb == null) {
			throw new EntityNotFoundException(Study.class, study.getId());
		}
		
		studyDb.setClinical(study.isClinical());
		studyDb.setDownloadableByDefault(study.isDownloadableByDefault());
		studyDb.setEndDate(study.getEndDate());
		if (KeycloakUtil.getTokenRoles().contains("ROLE_ADMIN")) {
			studyDb.setChallenge(study.isChallenge());
		}
		if (!study.getName().equals(studyDb.getName())) {
			updateStudyName(new IdName(study.getId(), study.getName()));
		}
		studyDb.setName(study.getName());
		studyDb.setStudyStatus(study.getStudyStatus());
		studyDb.setVisibleByDefault(study.isVisibleByDefault());
		studyDb.setWithExamination(study.isWithExamination());
		studyDb.setMonoCenter(study.isMonoCenter());

		if (study.getStudyCenterList() != null) {
			ListDependencyUpdate.updateWith(studyDb.getStudyCenterList(), study.getStudyCenterList());
			for (StudyCenter studyCenter : studyDb.getStudyCenterList()) {
				studyCenter.setStudy(studyDb);
			}
		}
		
		if (study.getSubjectStudyList() != null) {
			ListDependencyUpdate.updateWith(studyDb.getSubjectStudyList(), study.getSubjectStudyList());
			for (SubjectStudy subjectStudy : studyDb.getSubjectStudyList()) {
				subjectStudy.setStudy(studyDb);
			}
		}

		if (studyDb.getProtocolFilePaths() != null) {
			for(String filePath : studyDb.getProtocolFilePaths()) {
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
		
		studyRepository.save(studyDb);

		return studyDb;
	}

	/**
	 * Gets the protocol or data user agreement file path
	 * 
	 * @param studyId
	 *            id of the study
	 * @param fileName
	 *            name of the file
	 * @return the file path of the file
	 */
	@Override
	public String getStudyFilePath(Long studyId, String fileName) {
		return dataDir + "/study-" + studyId + "/" + fileName;
	}

	@Override
	public List<Study> findAll() {
		// Utils.copyList is used to prevent a bug with @PostFilter
		if (KeycloakUtil.getTokenRoles().contains("ROLE_ADMIN")) {
			return Utils.copyList(studyRepository.findAll());
		} else {
			return Utils.copyList(
				studyRepository.findByStudyUserList_UserIdAndStudyUserList_StudyUserRightsAndStudyUserList_Confirmed_OrderByNameAsc(
					KeycloakUtil.getTokenUserId(), StudyUserRight.CAN_SEE_ALL.getId(), true
				)
			);
		}
	}

	@Transactional
	protected void updateStudyUsers(Study studyDb, Study study) {
		if (study.getStudyUserList() == null) {
			return;
		}
		// New lists of created / updated to send via RabbitMQ
		List<StudyUser> toBeCreated = new ArrayList<>();
		List<StudyUser> toBeUpdated = new ArrayList<>();

		// Build maps of existing / replacing study users
		Map<Long, StudyUser> existing = new HashMap<>();
		for (StudyUser su : studyDb.getStudyUserList()) {
			existing.put(su.getId(), su);
		}
		
		Map<Long, StudyUser> replacing = new HashMap<>();
		for (StudyUser su : study.getStudyUserList()) {
			if (su.getId() == null) {
				toBeCreated.add(su);
			} else {
				replacing.put(su.getId(), su);
				if (study.getDataUserAgreementPaths() != null && !study.getDataUserAgreementPaths().isEmpty()) {
					// new DUA added to study
					if (studyDb.getDataUserAgreementPaths() == null || studyDb.getDataUserAgreementPaths().isEmpty()) {
						su.setConfirmed(false);
						dataUserAgreementService.createDataUserAgreementForUserInStudy(studyDb, su.getUserId());
					}
				} else {
					// existing DUA removed from study
					if (studyDb.getDataUserAgreementPaths() != null && !studyDb.getDataUserAgreementPaths().isEmpty()) {
						su.setConfirmed(true); // without DUA all StudyUser are confirmed, set back to true, if false before
						dataUserAgreementService.deleteIncompleteDataUserAgreementForUserInStudy(studyDb, su.getUserId());
					}
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
			existingSu.setReceiveAnonymizationReport(replacingSu.isReceiveAnonymizationReport());
			existingSu.setReceiveNewImportReport(replacingSu.isReceiveNewImportReport());
			existingSu.setStudyUserRights(replacingSu.getStudyUserRights());
			existingSu.setConfirmed(replacingSu.isConfirmed());
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
			// delete a DUA for removed user in study, if not yet accepted, if dua file exists
			if (studyDb.getDataUserAgreementPaths() != null && !studyDb.getDataUserAgreementPaths().isEmpty()) {
				dataUserAgreementService.deleteIncompleteDataUserAgreementForUserInStudy(studyDb, studyUser.getUserId());
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
			LOG.error("Could not transmit study-user update info through RabbitMQ");
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
			LOG.error("Could not transmit study-user create info through RabbitMQ");
		}
	}
	
	private boolean updateStudyName(IdName study) throws MicroServiceCommunicationException{
		try {
			rabbitTemplate.convertAndSend(RabbitMQConfiguration.studyNameUpdateQueue().getName(),
					new ObjectMapper().writeValueAsString(study));
			return true;
		} catch (AmqpException | JsonProcessingException e) {
			throw new MicroServiceCommunicationException("Error while communicating with datasets MS to update study name.");
		}
	}

	@Override
	public List<Study> findChallenges() {
		// Utils.copyList is used to prevent a bug with @PostFilter
		return Utils.copyList(studyRepository.findByChallengeTrue());
	}
}

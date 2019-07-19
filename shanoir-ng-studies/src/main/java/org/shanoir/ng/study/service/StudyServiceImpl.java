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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.transaction.Transactional;

import org.shanoir.ng.messaging.StudyUserUpdateBroadcastService;
import org.shanoir.ng.shared.exception.EntityNotFoundException;
import org.shanoir.ng.shared.exception.MicroServiceCommunicationException;
import org.shanoir.ng.shared.security.rights.StudyUserRight;
import org.shanoir.ng.study.model.Study;
import org.shanoir.ng.study.model.StudyUser;
import org.shanoir.ng.study.repository.StudyRepository;
import org.shanoir.ng.study.repository.StudyUserRepository;
import org.shanoir.ng.study.rights.command.CommandType;
import org.shanoir.ng.study.rights.command.StudyUserCommand;
import org.shanoir.ng.studycenter.StudyCenter;
import org.shanoir.ng.studycenter.StudyCenterRepository;
import org.shanoir.ng.subjectstudy.model.SubjectStudy;
import org.shanoir.ng.subjectstudy.repository.SubjectStudyRepository;
import org.shanoir.ng.utils.KeycloakUtil;
import org.shanoir.ng.utils.ListDependencyUpdate;
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
		
		if (study.getStudyUserList() != null) {
			List<StudyUserCommand> commands = new ArrayList<>();
			for (StudyUser su : study.getStudyUserList()) 
				commands.add(new StudyUserCommand(CommandType.DELETE, su.getId()));	
			try {
				studyUserCom.broadcast(commands);
			} catch (MicroServiceCommunicationException e) {
				LOG.error("Could not transmit study-user delete info through RabbitMQ");
			}
		}
		
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
				studyUser.setStudy(study);
			}
		}
		Study studyDb = studyRepository.save(study);
		
		if (studyDb.getStudyUserList() != null) {
			List<StudyUserCommand> commands = new ArrayList<>();
			for (final StudyUser studyUser: studyDb.getStudyUserList()) {
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

		if (study.getStudyCenterList() != null) {
			ListDependencyUpdate.updateWith(studyDb.getStudyCenterList(), study.getStudyCenterList());
			for (StudyCenter studyCenter : studyDb.getStudyCenterList()) studyCenter.setStudy(studyDb);			
		}
		
		if (study.getSubjectStudyList() != null) {
			ListDependencyUpdate.updateWith(studyDb.getSubjectStudyList(), study.getSubjectStudyList());
			for (SubjectStudy subjectStudy : studyDb.getSubjectStudyList()) subjectStudy.setStudy(studyDb);			
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

	@Transactional
	private void updateStudyUsers(Study study, List<StudyUser> studyUsers) {
		if (studyUsers == null) return;
		// New lists of created / updated to send via RabbitMQ
		List<StudyUser> toBeCreated = new ArrayList<>();
		List<StudyUser> toBeUpdated = new ArrayList<>();

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
		List<StudyUser> created = new ArrayList<>();
		if (!toBeCreated.isEmpty()) {
			for (StudyUser su : toBeCreated) {
				su.setStudy(study);
			}
			// save them first to get their id
			for (StudyUser su : studyUserRepository.save(toBeCreated)) created.add(su);
			//studyUserRepository.save(toBeCreated);
			study.getStudyUserList().addAll(created);			
		}

		// Remove deleted
		Utils.removeIdsFromList(idsToBeDeleted, study.getStudyUserList()); 
		
		// Send updates via RabbitMQ
		try {
			List<StudyUserCommand> commands = new ArrayList<>();
			for (Long id : idsToBeDeleted) commands.add(new StudyUserCommand(CommandType.DELETE, id));
			for (StudyUser su : created) commands.add(new StudyUserCommand(CommandType.CREATE, su));
			for (StudyUser su : toBeUpdated) commands.add(new StudyUserCommand(CommandType.UPDATE, su));
			
			studyUserCom.broadcast(commands);
			
		} catch (MicroServiceCommunicationException e) {
			LOG.error("Could not transmit study-user update info through RabbitMQ");
		}
	}
}

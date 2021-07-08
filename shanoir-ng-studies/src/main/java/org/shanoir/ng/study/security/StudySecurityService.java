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

package org.shanoir.ng.study.security;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.shanoir.ng.shared.core.model.IdName;
import org.shanoir.ng.shared.exception.EntityNotFoundException;
import org.shanoir.ng.shared.security.rights.StudyUserRight;
import org.shanoir.ng.study.dto.StudyDTO;
import org.shanoir.ng.study.dua.DataUserAgreement;
import org.shanoir.ng.study.dua.DataUserAgreementRepository;
import org.shanoir.ng.study.model.Study;
import org.shanoir.ng.study.repository.StudyRepository;
import org.shanoir.ng.study.repository.StudyUserRepository;
import org.shanoir.ng.study.model.StudyUser;
import org.shanoir.ng.subject.dto.SimpleSubjectDTO;
import org.shanoir.ng.subject.dto.SubjectDTO;
import org.shanoir.ng.subject.model.Subject;
import org.shanoir.ng.subject.repository.SubjectRepository;
import org.shanoir.ng.subjectstudy.model.SubjectStudy;
import org.shanoir.ng.utils.KeycloakUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class StudySecurityService {

	@Autowired
	StudyRepository studyRepository;

	@Autowired
	SubjectRepository subjectRepository;

	@Autowired
	StudyUserRepository studyUserRepository;
	
	@Autowired
	DataUserAgreementRepository dataUserAgreementRepository;

	/**
	 * Check that the connected user has the given right for the given study.
	 * 
	 * @param studyId
	 *            the study id
	 * @param rightStr
	 *            the right
	 * @return true or false
	 * @throws EntityNotFoundException
	 */
	public boolean hasRightOnStudy(Long studyId, String rightStr) throws EntityNotFoundException {
		StudyUserRight right = StudyUserRight.valueOf(rightStr);
		Study study = studyRepository.findOne(studyId);
		if (study == null) {
			throw new EntityNotFoundException("Cannot find study with id " + studyId);
		}
		return hasPrivilege(study, right);
	}

	/**
	 * Check that the connected user has the given right for the given study.
	 * 
	 * @param study
	 *            the study
	 * @param rightStr
	 *            the right
	 * @return true or false
	 * @throws EntityNotFoundException
	 */
	public boolean hasRightOnStudy(Study study, String rightStr) throws EntityNotFoundException {
		if (study == null) {
			throw new IllegalArgumentException("study cannot be null here.");
		}
		return this.hasRightOnStudy(study.getId(), rightStr);
	}

	/**
	 * Check that the connected user has the given right for at least one study.
	 * 
	 * @param rightStr
	 *            the right
	 * @return true or false
	 */
	public boolean hasRightOnOneStudy(String rightStr) {
		StudyUserRight right = StudyUserRight.valueOf(rightStr);
		List<StudyUser> studyUsers = studyUserRepository.findByUserId(KeycloakUtil.getTokenUserId());
		for (StudyUser su : studyUsers) {
			if (su.getStudyUserRights().contains(right) && su.isConfirmed()) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Check that the connected user has the given right for the given study. !
	 * ATTENTION ! This method is meant to be used with a trusted Study, meaning it
	 * should not be used with a Study object that comes from the user API but most
	 * likely from a Study coming from the database.
	 * 
	 * @param study
	 *            the TRUSTED study
	 * @param rightStr
	 *            the right
	 * @return true or false
	 */
	public boolean hasRightOnTrustedStudy(Study study, String rightStr) {
		StudyUserRight right = StudyUserRight.valueOf(rightStr);
		return hasPrivilege(study, right);
	}

	/**
	 * Check that the connected user has the given right for the given study. !
	 * ATTENTION ! This method is meant to be used with a trusted Study, meaning it
	 * should not be used with a Study object that comes from the user API but most
	 * likely from a Study coming from the database.
	 * 
	 * @param study
	 *            the TRUSTED study
	 * @param rightStr
	 *            the right
	 * @return true or false
	 */
	public boolean hasRightOnTrustedStudyDTO(StudyDTO dto, String rightStr) {
		StudyUserRight right = StudyUserRight.valueOf(rightStr);
		return hasPrivilege(dto.getStudyUserList(), right);
	}

	/**
	 * Check that the connected user has the given right in at least one study to
	 * which the subject participates.
	 * 
	 * @param subjectId
	 *            the subject id
	 * @param rightStr
	 *            the right
	 * @return true or false
	 * @throws EntityNotFoundException
	 */
	public boolean hasRightOnSubjectForOneStudy(Long subjectId, String rightStr) throws EntityNotFoundException {
		Subject subject = subjectRepository.findOne(subjectId);
		if (subject == null) {
			throw new EntityNotFoundException("Cannot find subject with id " + subjectId);
		}
		if (subject.getSubjectStudyList() == null) {
			return false;
		}
		StudyUserRight right = StudyUserRight.valueOf(rightStr);
		for (SubjectStudy subjectStudy : subject.getSubjectStudyList()) {
			if (hasPrivilege(subjectStudy.getStudy(), right)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Check that the connected user has the given right in every study to which the
	 * subject participates.
	 * 
	 * @param subjectId
	 *            the subject id
	 * @param rightStr
	 *            the right
	 * @return true or false
	 * @throws EntityNotFoundException
	 */
	public boolean hasRightOnSubjectForEveryStudy(Long subjectId, String rightStr) throws EntityNotFoundException {
		Subject subject = subjectRepository.findOne(subjectId);
		if (subject == null) {
			throw new EntityNotFoundException("Cannot find subject with id " + subjectId);
		}
		StudyUserRight right = StudyUserRight.valueOf(rightStr);
		for (SubjectStudy subjectStudy : subject.getSubjectStudyList()) {
			if (!hasPrivilege(subjectStudy.getStudy(), right)) {
				return false;
			}
		}
		return true;
	}

	/**
	 * Check that the connected user has the given right in at least one study to
	 * which the subject participates. ! ATTENTION ! This method is meant to be used
	 * with a trusted Subject, meaning it should not be used with a Subject object
	 * that comes from the user API but most likely from a Subject coming from the
	 * database.
	 * 
	 * @param subject
	 *            the TRUSTED subject
	 * @param rightStr
	 * @return true or false
	 */
	public boolean hasRightOnTrustedSubjectForOneStudy(Subject subject, String rightStr) {
		StudyUserRight right = StudyUserRight.valueOf(rightStr);
		if (subject != null && subject.getSubjectStudyList() != null) {
			for (SubjectStudy subjectStudy : subject.getSubjectStudyList()) {
				if (hasPrivilege(subjectStudy.getStudy(), right)) {
					return true;
				}
			}
		}
		return false;
	}

	/**
	 * For every subject of the list, check that the connected user has the given
	 * right in at least one study to which the subject participates.
	 *
	 * @param dtos
	 * @param rightStr
	 * @return true or false
	 */
	public boolean filterSubjectDTOsHasRightInOneStudy(List<SubjectDTO> dtos, String rightStr) {
		if (dtos == null) {
			return true;
		}
		List<SubjectDTO> newList = new ArrayList<>();
		Map<Long, SubjectDTO> map = new HashMap<>();
		for (SubjectDTO dto : dtos) {
			map.put(dto.getId(), dto);
		}
		for (Subject subject : subjectRepository.findAll(new ArrayList<>(map.keySet()))) {
			if (hasRightOnTrustedSubjectForOneStudy(subject, rightStr)) {
				newList.add(map.get(subject.getId()));
			}
		}
		dtos = newList;
		return true;
	}

	/**
	 * For every subject of the list, check that the connected user has the given
	 * right in at least one study to which the subject participates.
	 * 
	 * @param dtos
	 * @param rightStr
	 * @return true or false
	 */
	public boolean filterSimpleSubjectDTOsHasRightInOneStudy(List<SimpleSubjectDTO> dtos, String rightStr) {
		if (dtos == null) {
			return true;
		}
		List<SimpleSubjectDTO> newList = new ArrayList<>();
		Map<Long, SimpleSubjectDTO> map = new HashMap<>();
		for (SimpleSubjectDTO dto : dtos) {
			map.put(dto.getId(), dto);
		}
		for (Subject subject : subjectRepository.findAll(new ArrayList<>(map.keySet()))) {
			if (hasRightOnTrustedSubjectForOneStudy(subject, rightStr)) {
				newList.add(map.get(subject.getId()));
			}
		}
		dtos = newList;
		return true;
	}

	/**
	 * For every subject of the list, check that the connected user has the given
	 * right in at least one study to which the subject participates.
	 * 
	 * @param dtos
	 * @param rightStr
	 * @return true or false
	 */
	public boolean filterSubjectIdNamesDTOsHasRightInOneStudy(List<IdName> dtos, String rightStr) {
		if (dtos == null) {
			return true;
		}
		List<IdName> newList = new ArrayList<>();
		Map<Long, IdName> map = new HashMap<>();
		for (IdName dto : dtos) {
			map.put(dto.getId(), dto);
		}
		for (Subject subject : subjectRepository.findAll(new ArrayList<>(map.keySet()))) {
			if (hasRightOnTrustedSubjectForOneStudy(subject, rightStr)) {
				newList.add(map.get(subject.getId()));
			}
		}
		dtos = newList;
		return true;
	}

	/**
	 * For every study of the list, check that the connected user has the given
	 * right.
	 *
	 * @param dtos
	 * @param rightStr
	 * @return true or false
	 */
	public boolean filterStudyDTOsHasRight(List<StudyDTO> dtos, String rightStr) {
		StudyUserRight right = StudyUserRight.valueOf(rightStr);
		if (dtos == null) {
			return true;
		}
		List<StudyDTO> newList = new ArrayList<>();
		Map<Long, StudyDTO> map = new HashMap<>();
		for (StudyDTO dto : dtos) {
			map.put(dto.getId(), dto);
		}
		for (Study study : studyRepository.findAll(new ArrayList<>(map.keySet()))) {
			if (hasPrivilege(study, right)) {
				newList.add(map.get(study.getId()));
			}
		}
		dtos = newList;
		return true;
	}

	/**
	 * For every study of the list, check that the connected user has the given
	 * right.
	 *
	 * @param dtos
	 * @param rightStr
	 * @return true or false
	 */
	public boolean filterStudyIdNameDTOsHasRight(List<IdName> dtos, String rightStr) {
		StudyUserRight right = StudyUserRight.valueOf(rightStr);
		if (dtos == null) {
			return true;
		}
		List<IdName> newList = new ArrayList<>();
		Map<Long, IdName> map = new HashMap<>();
		for (IdName dto : dtos) {
			map.put(dto.getId(), dto);
		}
		for (Study study : studyRepository.findAll(new ArrayList<>(map.keySet()))) {
			if (hasPrivilege(study, right)) {
				newList.add(map.get(study.getId()));
			}
		}
		dtos = newList;
		return true;
	}

	/**
	 * For every study of the list, check that the connected user has the given
	 * right.
	 *
	 * @param dtos
	 * @param rightStr
	 * @return true or false
	 */
	public boolean filterStudiesHasRight(List<Long> ids, String rightStr) {
		StudyUserRight right = StudyUserRight.valueOf(rightStr);
		if (ids == null) {
			return true;
		}
		List<Long> newList = new ArrayList<>();
		for (Study study : studyRepository.findAll(ids)) {
			if (hasPrivilege(study, right)) {
				newList.add(study.getId());
			}
		}
		ids = newList;
		return true;
	}

	/**
	 * Check that the connected user has the given right for all the studies linked
	 * inside the given list.
	 * 
	 * @param subjectStudyList
	 *            the list of subject-study relationship objects
	 * @param rightStr
	 *            the right
	 * @return true or false
	 */
	public boolean checkRightOnEverySubjectStudyList(Iterable<SubjectStudy> subjectStudyList, String rightStr) {
		if (subjectStudyList == null) {
			return false;
		}
		StudyUserRight right = StudyUserRight.valueOf(rightStr);
		List<Long> ids = new ArrayList<>();
		for (SubjectStudy subjectStudy : subjectStudyList) {
			ids.add(subjectStudy.getStudy().getId());
		}
		int nbStudies = 0;
		for (Study study : studyRepository.findAll(ids)) {
			nbStudies++;
			if (!hasPrivilege(study, right)) {
				return false;
			}
		}
		return nbStudies == ids.size();
	}
	
	/**
	 * Verify that DUA accepting user is the DUA user only.
	 * @param duaId
	 * @return
	 */
	public boolean checkUserOnDUA(Long duaId) {
		DataUserAgreement dataUserAgreement = dataUserAgreementRepository.findOne(duaId);
		// assure that only the user itself can accept its DUA
		if (dataUserAgreement != null && dataUserAgreement.getUserId().equals(KeycloakUtil.getTokenUserId())) {
			return true;
		}
		return false;
	}

	/**
	 * Verifies that study's studyUsers link to the correct study.
	 * 
	 * @param study
	 * @return
	 */
	public boolean studyUsersMatchStudy(Study study) {
		for (StudyUser su : study.getStudyUserList()) {
			if (su.getStudy() != null && su.getStudy().getId() != null && su.getStudy().getId() != study.getId())
				return false;
		}
		return true;
	}

	/**
	 * Verifies that study's studyUsers link to no study.
	 * 
	 * @param study
	 * @return
	 */
	public boolean studyUsersStudyNull(Study study) {
		for (StudyUser su : study.getStudyUserList()) {
			if (!(su.getStudy() == null || su.getStudy().getId() == null))
				return false;
		}
		return true;
	}

	/**
	 * Check that the connected user has this right on this study.
	 * 
	 * @param study
	 * @param neededRight
	 * @return true or false
	 */
	private boolean hasPrivilege(Study study, StudyUserRight neededRight) {
		if (study == null) {
			throw new IllegalArgumentException("study cannot be null");
		}
		return hasPrivilege(study.getStudyUserList(), neededRight);
	}

	/**
	 * Check that the connected user has this right on this study.
	 * 
	 * @param study
	 * @param neededRight
	 * @return true or false
	 */
	private boolean hasPrivilege(List<StudyUser> studyUserList, StudyUserRight neededRight) {
		if (KeycloakUtil.getTokenRoles().contains("ROLE_ADMIN")) {
			return true;
		}
		Long userId = KeycloakUtil.getTokenUserId();
		if (studyUserList == null) {
			return false;
		}
		StudyUser studyUser = studyUserList.stream().filter(su -> userId.equals(su.getUserId())).findAny().orElse(null);
		if (studyUser == null) {
			return false;
		}
		return studyUser.getStudyUserRights() != null && studyUser.getStudyUserRights().contains(neededRight) && studyUser.isConfirmed();
	}

}
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

package org.shanoir.ng.subject.service;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.shanoir.ng.shared.configuration.RabbitMQConfiguration;
import org.shanoir.ng.shared.core.model.AbstractEntity;
import org.shanoir.ng.shared.core.model.IdName;
import org.shanoir.ng.shared.event.ShanoirEvent;
import org.shanoir.ng.shared.event.ShanoirEventService;
import org.shanoir.ng.shared.event.ShanoirEventType;
import org.shanoir.ng.shared.exception.EntityNotFoundException;
import org.shanoir.ng.shared.exception.MicroServiceCommunicationException;
import org.shanoir.ng.shared.exception.ShanoirException;
import org.shanoir.ng.shared.security.rights.StudyUserRight;
import org.shanoir.ng.study.model.Study;
import org.shanoir.ng.study.repository.StudyRepository;
import org.shanoir.ng.study.repository.StudyUserRepository;
import org.shanoir.ng.studyexamination.StudyExaminationRepository;
import org.shanoir.ng.subject.dto.SimpleSubjectDTO;
import org.shanoir.ng.subject.dto.SubjectDTO;
import org.shanoir.ng.subject.dto.mapper.SubjectMapper;
import org.shanoir.ng.subject.model.Subject;
import org.shanoir.ng.subject.repository.SubjectRepository;
import org.shanoir.ng.subjectstudy.dto.mapper.SubjectStudyDecorator;
import org.shanoir.ng.subjectstudy.model.SubjectStudy;
import org.shanoir.ng.subjectstudy.repository.SubjectStudyRepository;
import org.shanoir.ng.utils.KeycloakUtil;
import org.shanoir.ng.utils.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Subject service implementation.
 *
 * @author msimon
 *
 */
@Service
public class SubjectServiceImpl implements SubjectService {

	private static final String FORMAT_CENTER_CODE = "000";

	private static final String FORMAT_SUBJECT_CODE = "0000";

	@Autowired
	private SubjectRepository subjectRepository;

	@Autowired
	private SubjectStudyRepository subjectStudyRepository;
	
	@Autowired
	private StudyRepository studyRepository;
	
	@Autowired
	private SubjectStudyDecorator subjectStudyMapper;

	@Autowired
	private RabbitTemplate rabbitTemplate;
	
	@Autowired
	private StudyUserRepository studyUserRepository;

	@Autowired
	private SubjectMapper subjectMapper;
	
	@Autowired
	private ObjectMapper objectMapper;

	@Autowired
	private StudyExaminationRepository studyExaminationRepository;
	
	private static final Logger LOG = LoggerFactory.getLogger(SubjectServiceImpl.class);

	@Override
	@Transactional
	public void deleteById(final Long id) throws EntityNotFoundException {
		Optional<Subject> subject = subjectRepository.findById(id);
		if (subject.isEmpty()) {
			throw new EntityNotFoundException(Subject.class, id);
		}
		// Delete all associated study_examination
		studyExaminationRepository.deleteBySubjectId(id);
		subjectRepository.deleteById(id);
		if (subject.get().isPreclinical())
			rabbitTemplate.convertAndSend(RabbitMQConfiguration.DELETE_ANIMAL_SUBJECT_QUEUE, id.toString());
		rabbitTemplate.convertAndSend(RabbitMQConfiguration.DELETE_SUBJECT_QUEUE, id.toString());
	}

	@Override
	public List<Subject> findAll() {
		// copyList is to prevent a bug with @postFilter
		return Utils.copyList(Utils.toList(subjectRepository.findAll()));
	}

	@Override
	public List<IdName> findAllNames() {
		Iterable<Subject> subjects;
		if (KeycloakUtil.getTokenRoles().contains("ROLE_ADMIN")) {
			subjects = subjectRepository.findAll();
		} else {
			Long userId = KeycloakUtil.getTokenUserId();
			List<Long> studyIds = studyUserRepository.findDistinctStudyIdByUserId(userId, StudyUserRight.CAN_SEE_ALL.getId());
			subjects = subjectRepository.findBySubjectStudyListStudyIdIn(studyIds);
		}
		return getIdNamesFromSubjects(subjects);
	}

	@Override
    public List<IdName> findNames(List<Long> subjectIds) {
		Iterable<Subject> subjects;
		if (KeycloakUtil.getTokenRoles().contains("ROLE_ADMIN")) {
			subjects = subjectRepository.findAllById(subjectIds);
		} else {
			Long userId = KeycloakUtil.getTokenUserId();
			List<Long> studyIds = studyUserRepository.findDistinctStudyIdByUserId(userId, StudyUserRight.CAN_SEE_ALL.getId());
			subjects = subjectRepository.findBySubjectStudyListStudyIdInAndIdIn(studyIds, subjectIds);
		}
		return getIdNamesFromSubjects(subjects);
	}

	private List<IdName> getIdNamesFromSubjects(Iterable<Subject> subjects) {
		if (subjects == null) {
			return new ArrayList<>();
		}
		List<IdName> names = new ArrayList<>();
		for (Subject subject : subjects) {
			IdName name = new IdName(subject.getId(), subject.getName());
			names.add(name);
		}
		return names;
	}

    @Override
	public Subject findByData(final String name) {
		return subjectRepository.findByName(name);
	}

	@Override
	public Subject findById(final Long id) {
		return subjectRepository.findById(id).orElse(null);
	}

	@Override
	public Subject findByIdWithSubjectStudies(final Long id) {
		return subjectRepository.findSubjectWithSubjectStudyById(id);
	}
	
	@Override
	public Subject create(final Subject subject) {
		manageSubjectStudy(subject);
		Subject subjectDb = subjectRepository.save(subject);
		try {
			updateSubjectName(subjectMapper.subjectToSubjectDTO(subjectDb));
		} catch (MicroServiceCommunicationException e) {
			LOG.error("Unable to propagate subject creation to dataset microservice: ", e);
		}
		return subjectDb;
	}

	@Override
	public Subject createAutoIncrement(final Subject subject, final Long centerId) {
		manageSubjectStudy(subject);
		// the first 3 numbers are the center code, search for highest existing subject with center code
		DecimalFormat formatterCenter = new DecimalFormat(FORMAT_CENTER_CODE);
		String commonNameCenter = formatterCenter.format(centerId);
		int maxCommonNameNumber = 0;
		Subject subjectOfsepCommonNameMaxFoundByCenter = findSubjectFromCenterCode(commonNameCenter);
		if (subjectOfsepCommonNameMaxFoundByCenter != null) {
			String maxNameToIncrement = subjectOfsepCommonNameMaxFoundByCenter.getName().substring(3);
			maxCommonNameNumber = Integer.parseInt(maxNameToIncrement);
		}
		maxCommonNameNumber += 1;
		DecimalFormat formatterSubject = new DecimalFormat(FORMAT_SUBJECT_CODE);
		String subjectName = commonNameCenter + formatterSubject.format(maxCommonNameNumber);
		subject.setName(subjectName);
		Subject subjectDb = subjectRepository.save(subject);
		try {
			updateSubjectName(subjectMapper.subjectToSubjectDTO(subjectDb));
		} catch (MicroServiceCommunicationException e) {
			LOG.error("Unable to propagate subject creation to dataset microservice: ", e);
		}
		return subjectDb;
	}

	private void manageSubjectStudy(final Subject subject) {
		if (subject.getSubjectStudyList() != null && !subject.getSubjectStudyList().isEmpty()) {
			for (final SubjectStudy subjectStudy : subject.getSubjectStudyList()) {
				subjectStudy.setSubject(subject);
			}
			// @todo: Manage create subject: multi studies here
			SubjectStudy subjectStudy = subject.getSubjectStudyList().get(0);
			subject.setStudy(subjectStudy.getStudy());
			subject.setStudyIdentifier(subjectStudy.getSubjectStudyIdentifier());
			subject.setPhysicallyInvolved(subjectStudy.isPhysicallyInvolved());
			subject.setQualityTag(subjectStudy.getQualityTag());
			subject.setSubjectType(subjectStudy.getSubjectType());
		}
	}	

	@Override
	@Transactional
	public Subject update(final Subject subject) throws ShanoirException {
		final Subject subjectDb = subjectRepository.findById(subject.getId()).orElse(null);
		if (subjectDb == null) {
			throw new EntityNotFoundException(Subject.class, subject.getId());
		}
		if (!subjectDb.getName().equals(subject.getName())) {
			throw new ShanoirException("You cannot update subject common name.", HttpStatus.FORBIDDEN.value());
		}
		updateSubjectValues(subjectDb, subject);
		Subject newSubject = subjectRepository.save(subjectDb);
		updateSubjectName(subjectMapper.subjectToSubjectDTO(newSubject));
		return newSubject;
	}

	/*
	 * Update some values of template to save them in database.
	 * Intentially this update method does not modify the pseudonymus
	 * hash values, that are only added by createSubject to avoid any
	 * manipulation.
	 *
	 * @param templateDb template found in database.
	 * @param template template with new values.
	 * @return database template with new values.
	 */
	private Subject updateSubjectValues(final Subject subjectDb, final Subject subject) throws MicroServiceCommunicationException {
		subjectDb.setName(subject.getName());
		subjectDb.setIdentifier(subject.getIdentifier());
		subjectDb.setSex(subject.getSex());
		subjectDb.setManualHemisphericDominance(subject.getManualHemisphericDominance());
		subjectDb.setLanguageHemisphericDominance(subject.getLanguageHemisphericDominance());
		subjectDb.setImagedObjectCategory(subject.getImagedObjectCategory());
		subjectDb.setUserPersonalCommentList(subject.getUserPersonalCommentList());
		// Update new columns for future clients
		subjectDb.setStudy(subject.getStudy());
		subjectDb.setStudyIdentifier(subject.getStudyIdentifier());
		subjectDb.setPhysicallyInvolved(subject.isPhysicallyInvolved());
		subjectDb.setQualityTag(subject.getQualityTag());
		subjectDb.setSubjectType(subject.getSubjectType());
		// Old clients: still send subject study list
		if (subject.getSubjectStudyList() != null) {
			List<SubjectStudy> subjectStudyListDb = subjectDb.getSubjectStudyList();
			List<SubjectStudy> subjectStudyListNew = subject.getSubjectStudyList();
			for (SubjectStudy subjectStudyNew : subjectStudyListNew) {
				Long studyIdNew = subjectStudyNew.getStudy().getId();
				for (SubjectStudy subjectStudyDb : subjectStudyListDb) {
					// Update values in any case
					subjectDb.setStudy(subjectStudyNew.getStudy());
					subjectDb.setStudyIdentifier(subjectStudyNew.getSubjectStudyIdentifier());
					subjectDb.setPhysicallyInvolved(subjectStudyNew.isPhysicallyInvolved());
					subjectDb.setQualityTag(subjectStudyNew.getQualityTag());
					subjectDb.setSubjectType(subjectStudyNew.getSubjectType());
					Long studyIdDb = subjectStudyDb.getStudy().getId();
					if (!studyIdNew.equals(studyIdDb)) {
						Subject newSubject = new Subject();
						newSubject.setName(subject.getName());
						newSubject.setIdentifier(subject.getIdentifier());
						newSubject.setSex(subject.getSex());
						newSubject.setManualHemisphericDominance(subject.getManualHemisphericDominance());
						newSubject.setLanguageHemisphericDominance(subject.getLanguageHemisphericDominance());
						newSubject.setImagedObjectCategory(subject.getImagedObjectCategory());
						newSubject.setUserPersonalCommentList(subject.getUserPersonalCommentList());
						newSubject.setStudy(subjectStudyNew.getStudy());
						newSubject.setStudyIdentifier(subjectStudyNew.getSubjectStudyIdentifier());
						newSubject.setPhysicallyInvolved(subjectStudyNew.isPhysicallyInvolved());
						newSubject.setQualityTag(subjectStudyNew.getQualityTag());
						newSubject.setSubjectType(subjectStudyNew.getSubjectType());
						// Create new subject without subject study, if new subject study
						subjectRepository.save(newSubject);
					}
				}
			}
			subjectStudyListDb.clear();
			subjectStudyListDb.addAll(subjectStudyListNew);
			for (SubjectStudy dbSubjectStudy : subjectStudyListDb) {
				dbSubjectStudy.setSubject(subjectDb);
				if (dbSubjectStudy.getSubjectStudyTags() == null) {
					dbSubjectStudy.setSubjectStudyTags(new ArrayList<>());
				}
			}
		}
		return subjectDb;
	}
	
	public boolean updateSubjectName(SubjectDTO subject) throws MicroServiceCommunicationException{
		try {
			rabbitTemplate.
					convertSendAndReceive(RabbitMQConfiguration.SUBJECT_NAME_UPDATE_QUEUE,
					objectMapper.writeValueAsString(subject));
			// If an error happens, an exception will be thrown
			return true;
		} catch (AmqpException | JsonProcessingException e) {
			throw new MicroServiceCommunicationException("Error while communicating with datasets MS to update subject name.");
		}
	}

	/**
	 * If preclinical is null, doesn't use it. Else it filters the subjects depending of the given value true/false.
	 */
	@Override
	public List<SimpleSubjectDTO> findAllSubjectsOfStudyAndPreclinical(final Long studyId, final Boolean preclinical) {
		List<SimpleSubjectDTO> simpleSubjectDTOList = new ArrayList<>();
		List<SubjectStudy> subjectStudyList;
		if (KeycloakUtil.getTokenRoles().contains("ROLE_ADMIN")) {
			subjectStudyList = subjectStudyRepository.findByStudyId(studyId);
		} else {
			Long userId = KeycloakUtil.getTokenUserId();
			subjectStudyList = subjectStudyRepository.findByStudyIdAndStudy_StudyUserList_UserId(studyId, userId);
		}
		Study studyWithTags = studyRepository.findStudyWithTagsById(studyId);
		if (subjectStudyList != null) {
			subjectStudyList.stream().forEach(ss -> {
				// after testing this seems to be useless :
				// ss.setSubjectStudyTags(subjectStudyRepository.findSubjectStudyTagsByStudyIdAndSubjectId(studyId, ss.getSubject().getId()));
				if (studyWithTags != null) {
					ss.getStudy().setTags(studyWithTags.getTags());
				}
			});
			for (SubjectStudy rel : subjectStudyList) {
				SimpleSubjectDTO simpleSubjectDTO = new SimpleSubjectDTO();
				if (studyId.equals(rel.getStudy().getId()) 
						&& preclinical == null || (preclinical.equals(rel.getSubject().isPreclinical()))) {
					Subject sub = rel.getSubject();
					simpleSubjectDTO.setId(sub.getId());
					simpleSubjectDTO.setName(sub.getName());
					simpleSubjectDTO.setIdentifier(sub.getIdentifier());
					simpleSubjectDTO.setImagedObjectCategory(sub.getImagedObjectCategory());
					simpleSubjectDTO.setLanguageHemisphericDominance(sub.getLanguageHemisphericDominance());
					simpleSubjectDTO.setManualHemisphericDominance(sub.getManualHemisphericDominance());
					simpleSubjectDTO.setSubjectStudy(subjectStudyMapper.subjectStudyToSubjectStudyDTO(rel));
					simpleSubjectDTOList.add(simpleSubjectDTO);
				}
			}
		}
		return simpleSubjectDTOList;
	}
	
	@Override
	public List<SimpleSubjectDTO> findAllSubjectsOfStudyId(final Long studyId) {
		return findAllSubjectsOfStudyAndPreclinical(studyId, null);
	}

	@Override
	public Subject findByIdentifierInStudiesWithRights(String identifier, List<Study> studies) {
		Iterable<Long> studyIds = studies.stream().map(AbstractEntity::getId).collect(Collectors.toList());
		Subject subject = subjectRepository.findFirstByIdentifierAndSubjectStudyListStudyIdIn(identifier, studyIds);
		loadSubjectStudyTags(subject);
		return subject;
	}

	@Override
	public Subject findSubjectFromCenterCode(final String centerCode) {
		if (centerCode == null || "".equals(centerCode)) {
			return null;
		}
		return subjectRepository.findSubjectFromCenterCode(centerCode + "%");
	}

	@Override
	public Page<Subject> getClinicalFilteredPageByStudies(Pageable page, String name, List<Study> studies) {
		Iterable<Long> studyIds = studies.stream().map(AbstractEntity::getId).collect(Collectors.toList());
		return subjectRepository.findDistinctByPreclinicalIsFalseAndNameContainingAndSubjectStudyListStudyIdIn(name, page, studyIds);
	}

	@Override
	public List<Subject> findByPreclinical(boolean preclinical) {
		List<Subject> subjects = subjectRepository.findByPreclinical(preclinical);
		subjects.stream().forEach(s -> {
			loadSubjectStudyTags(s);
		});
		return subjects;
	}

    @Override
    public boolean existsSubjectWithName(String name) {
        return this.subjectRepository.existsByName(name);
    }

	/**
	 * Use this method to avoid two bags violation exception and load subjectStudyTags.
	 * 
	 * @param subject
	 */
	private void loadSubjectStudyTags(Subject subject) {
		if (subject != null) {
			List<SubjectStudy> subjectStudyList = subject.getSubjectStudyList();
			if (subjectStudyList != null) {
				subjectStudyList.stream().forEach(ss -> {
					ss.getSubjectStudyTags().clear();
					ss.getSubjectStudyTags().addAll(subjectStudyRepository.findSubjectStudyTagsByStudyIdAndSubjectId(ss.getStudy().getId(), ss.getSubject().getId()));
					Study studyWithTags = studyRepository.findStudyWithTagsById(ss.getStudy().getId());
					if (studyWithTags != null) {
						ss.getStudy().setTags(studyWithTags.getTags());
					}
				});
			}
		}
	}

}

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
import org.shanoir.ng.utils.ListDependencyUpdate;
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
	private StudyRepository studyRepository;

	@Autowired
	private SubjectStudyRepository subjectStudyRepository;
	
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
	
	@Autowired
	private ShanoirEventService eventService;
	
	private static final Logger LOG = LoggerFactory.getLogger(SubjectServiceImpl.class);

	@Override
	@Transactional
	public void deleteById(final Long id) throws EntityNotFoundException {
		Optional<Subject> subject = subjectRepository.findById(id);
		if (subject.isEmpty()) {
			throw new EntityNotFoundException(Subject.class, id);
		}
		
		// Delete all associated study_examination
		studyExaminationRepository.deleteBySubject(subject.get());
		
		subjectRepository.deleteById(id);

		// Propagate deletion
		eventService.publishEvent(new ShanoirEvent(ShanoirEventType.DELETE_SUBJECT_EVENT, id.toString(), KeycloakUtil.getTokenUserId(), "", ShanoirEvent.SUCCESS));
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
	public Subject findByIdWithSubjecStudies(final Long id) {
		return subjectRepository.findSubjectWithSubjectStudyById(id);
	}
	
	@Override
	public Subject create(final Subject subject) {
		if (subject.getSubjectStudyList() != null) {
			for (final SubjectStudy subjectStudy : subject.getSubjectStudyList()) {
				subjectStudy.setSubject(subject);
			}
		}
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
		if (subject.getSubjectStudyList() != null) {
			for (final SubjectStudy subjectStudy : subject.getSubjectStudyList()) {
				subjectStudy.setSubject(subject);
			}
		}
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

	@Override
	public Subject update(final Subject subject) throws ShanoirException {
		final Subject subjectDb = subjectRepository.findById(subject.getId()).orElse(null);
		if (subjectDb == null) {
			throw new EntityNotFoundException(Subject.class, subject.getId());
		}
		if (!subjectDb.getName().equals(subject.getName())) {
			throw new ShanoirException("You cannot update subject common name.", HttpStatus.FORBIDDEN.value());
		}
		updateSubjectValues(subjectDb, subject);
		subjectRepository.save(subjectDb);
		updateSubjectName(subjectMapper.subjectToSubjectDTO(subjectDb));
		return subjectDb;
	}

	/*
	 * Update some values of template to save them in database.
	 *
	 * @param templateDb template found in database.
	 * @param template template with new values.
	 * @return database template with new values.
	 */
	private Subject updateSubjectValues(final Subject subjectDb, final Subject subject) throws MicroServiceCommunicationException {
		subjectDb.setName(subject.getName());
		//subjectDb.setBirthDate(subject.getBirthDate());
		subjectDb.setIdentifier(subject.getIdentifier());
		subjectDb.setPseudonymusHashValues(subject.getPseudonymusHashValues());
		subjectDb.setSex(subject.getSex());
		subjectDb.setManualHemisphericDominance(subject.getManualHemisphericDominance());
		subjectDb.setLanguageHemisphericDominance(subject.getLanguageHemisphericDominance());
		subjectDb.setImagedObjectCategory(subject.getImagedObjectCategory());
		subjectDb.setUserPersonalCommentList(subject.getUserPersonalCommentList());
		
		if (subject.getSubjectStudyList() != null) {
			ListDependencyUpdate.updateWith(subjectDb.getSubjectStudyList(), subject.getSubjectStudyList());
			for (SubjectStudy subjectStudy : subjectDb.getSubjectStudyList()) {
				subjectStudy.setSubject(subjectDb);
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

	@Override
	public List<SimpleSubjectDTO> findAllSubjectsOfStudy(final Long studyId) {
		return this.findAllSubjectsOfStudyId(studyId);
	}

	@Override
	public List<SimpleSubjectDTO> findAllSubjectsOfStudyId(final Long studyId) {
		List<SimpleSubjectDTO> simpleSubjectDTOList = new ArrayList<>();
		List<SubjectStudy> opt = subjectStudyRepository.findByStudy(studyRepository.findById(studyId).orElse(null));
		if (opt != null) {
			for (SubjectStudy rel : opt) {
				SimpleSubjectDTO simpleSubjectDTO = new SimpleSubjectDTO();
				if (studyId.equals(rel.getStudy().getId())) {
					Subject sub = rel.getSubject();
					simpleSubjectDTO.setId(sub.getId());
					simpleSubjectDTO.setName(sub.getName());
					simpleSubjectDTO.setIdentifier(sub.getIdentifier());
					simpleSubjectDTO.setSubjectStudy(subjectStudyMapper.subjectStudyToSubjectStudyDTO(rel));
					simpleSubjectDTOList.add(simpleSubjectDTO);
				}
			}
		}
		return simpleSubjectDTOList;
	}
	
	@Override
	public List<SimpleSubjectDTO> findAllSubjectsOfStudyAndPreclinical(final Long studyId, boolean preclinical) {
		List<SimpleSubjectDTO> simpleSubjectDTOList = new ArrayList<>();
		List<SubjectStudy> opt = subjectStudyRepository.findByStudy(studyRepository.findById(studyId).orElse(null));
		if (opt != null) {
			for (SubjectStudy rel : opt) {
				SimpleSubjectDTO simpleSubjectDTO = new SimpleSubjectDTO();
				if (studyId.equals(rel.getStudy().getId()) && preclinical == rel.getSubject().isPreclinical()) {
					Subject sub = rel.getSubject();
					simpleSubjectDTO.setId(sub.getId());
					simpleSubjectDTO.setName(sub.getName());
					simpleSubjectDTO.setIdentifier(sub.getIdentifier());
					simpleSubjectDTO.setSubjectStudy(subjectStudyMapper.subjectStudyToSubjectStudyDTO(rel));
					simpleSubjectDTOList.add(simpleSubjectDTO);
				}
			}
		}
		return simpleSubjectDTOList;
	}

	@Override
	public Subject findByIdentifier(String identifier) {
		return subjectRepository.findByIdentifier(identifier);
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
		return subjectRepository.findByPreclinical(preclinical);
	}

    @Override
    public boolean existsSubjectWithName(String name) {
        return this.subjectRepository.existsByName(name);
    }
}

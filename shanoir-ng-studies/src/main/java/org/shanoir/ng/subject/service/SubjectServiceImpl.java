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

import org.shanoir.ng.configuration.amqp.RabbitMQConfiguration;
import org.shanoir.ng.shared.core.model.IdName;
import org.shanoir.ng.shared.exception.EntityNotFoundException;
import org.shanoir.ng.shared.exception.MicroServiceCommunicationException;
import org.shanoir.ng.study.repository.StudyRepository;
import org.shanoir.ng.subject.dto.SimpleSubjectDTO;
import org.shanoir.ng.subject.model.Subject;
import org.shanoir.ng.subject.repository.SubjectRepository;
import org.shanoir.ng.subjectstudy.dto.mapper.SubjectStudyDecorator;
import org.shanoir.ng.subjectstudy.model.SubjectStudy;
import org.shanoir.ng.subjectstudy.repository.SubjectStudyRepository;
import org.shanoir.ng.utils.ListDependencyUpdate;
import org.shanoir.ng.utils.Utils;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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

	@Override
	public void deleteById(final Long id) throws EntityNotFoundException {
		if (subjectRepository.findOne(id) == null) throw new EntityNotFoundException(Subject.class, id);
		subjectRepository.delete(id);
	}

	@Override
	public List<Subject> findAll() {
		// copyList is to prevent a bug with @postFilter
		return Utils.copyList(Utils.toList(subjectRepository.findAll()));
	}
	

	@Override
	public List<IdName> findNames() {
		List<IdName> names = new ArrayList<>();
		for (Subject subject : subjectRepository.findAll()) {
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
		return subjectRepository.findOne(id);
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
		return subjectRepository.save(subject);
	}

	@Override
	public Subject update(final Subject subject) throws EntityNotFoundException, MicroServiceCommunicationException {
		final Subject subjectDb = subjectRepository.findOne(subject.getId());
		if (subjectDb == null) throw new EntityNotFoundException(Subject.class, subject.getId());
		updateSubjectValues(subjectDb, subject);
		subjectRepository.save(subjectDb);
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

		if (subject.getName() != subjectDb.getName()) {
			updateSubjectName(new IdName(subject.getId(), subject.getName()));
		}
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
			for (SubjectStudy subjectStudy : subjectDb.getSubjectStudyList()) subjectStudy.setSubject(subjectDb);			
		}
		
		return subjectDb;
	}
	
	private boolean updateSubjectName(IdName subject) throws MicroServiceCommunicationException{
		try {
			rabbitTemplate.convertAndSend(RabbitMQConfiguration.subjectNameUpdateQueue().getName(),
					new ObjectMapper().writeValueAsString(subject));
			return true;
		} catch (AmqpException | JsonProcessingException e) {
			throw new MicroServiceCommunicationException("Error while communicating with datasets MS to update subject name.");
		} 
	}

	@Override
	public List<SimpleSubjectDTO> findAllSubjectsOfStudy(final Long studyId) {
		List<SimpleSubjectDTO> simpleSubjectDTOList = new ArrayList<SimpleSubjectDTO>();
		List<SubjectStudy> opt = subjectStudyRepository.findByStudy(studyRepository.findOne(studyId));
		if (opt != null) {
			for (SubjectStudy rel : opt) {
				SimpleSubjectDTO simpleSubjectDTO = new SimpleSubjectDTO();
				if (rel.getStudy().getId() == studyId) {
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
}

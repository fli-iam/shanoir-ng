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

import java.util.ArrayList;
import java.util.List;

import org.shanoir.ng.shared.configuration.RabbitMQConfiguration;
import org.shanoir.ng.shared.core.model.IdName;
import org.shanoir.ng.study.model.Study;
import org.shanoir.ng.study.repository.StudyRepository;
import org.shanoir.ng.subject.model.Subject;
import org.shanoir.ng.subject.model.SubjectType;
import org.shanoir.ng.subject.repository.SubjectRepository;
import org.shanoir.ng.subjectstudy.model.SubjectStudy;
import org.shanoir.ng.subjectstudy.repository.SubjectStudyRepository;
import org.shanoir.ng.utils.SecurityContextUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.AmqpRejectAndDontRequeueException;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

@Service
public class RabbitMQSubjectService {

	private static final Logger LOG = LoggerFactory.getLogger(RabbitMQSubjectService.class);

	@Autowired
	SubjectRepository subjectRepository;

	@Autowired
	SubjectService subjectService;

	@Autowired
	StudyRepository studyRepository;

	@Autowired
	SubjectStudyRepository subjectStudyRepository;
	
	@Autowired
	ObjectMapper mapper;
	
	/**
	 * This methods returns a list of subjects for a given study ID
	 * @param studyId the study ID
	 * @return a list of subjects
	 */
	@RabbitListener(queues = RabbitMQConfiguration.DATASET_SUBJECT_QUEUE)
	@RabbitHandler
	@Transactional
	public String getSubjectsForStudy(String studyId) {
		try {
			return mapper.writeValueAsString(subjectService.findAllSubjectsOfStudyId(Long.valueOf(studyId)));
		} catch (Exception e) {
			LOG.error("Error while serializing subjects for participants.tsv file.", e);
			throw new AmqpRejectAndDontRequeueException(e);
		}
	}

	/**
	 * This methods allows to update a subject with a subjectStudy if not existing.
	 * @param message the IDName we are receiving containing 1) The subject id in the id 2) The study id in the name
	 * @return the study name
	 */
	@RabbitListener(queues = RabbitMQConfiguration.DATASET_SUBJECT_STUDY_QUEUE)
	@RabbitHandler
	@Transactional
	public String updateSubjectStudy(String message) {
		IdName idNameMessage;
		try {
			idNameMessage = mapper.readValue(message, IdName.class);
			if (idNameMessage == null) {
				throw new IllegalStateException("no rabbitmq message parsed from " + message);
			}
			Long subjectId = idNameMessage.getId();
			Long studyId = Long.valueOf(idNameMessage.getName());
			Subject subject = subjectRepository.findById(subjectId).orElseThrow();
			for (SubjectStudy subStud : subject.getSubjectStudyList()) {
				if (subStud.getStudy().getId().equals(studyId)) {
					// subject study already exists, don't create a new one.
					return subStud.getStudy().getName();
				}
			}
			SubjectStudy subStud = new SubjectStudy();
			subStud.setSubject(subject);
			Study study = studyRepository.findById(studyId).orElseThrow();

			// TODO: ask
			subStud.setSubjectType(SubjectType.PATIENT);
			subStud.setPhysicallyInvolved(true);
			subStud.setStudy(study);
			subjectStudyRepository.save(subStud);
			return study.getName();
		} catch (NullPointerException e) {
			LOG.error("Error while creating subjectStudy", e);
			return null;
		} catch (Exception e) {
			LOG.error("Error while creating subjectStudy", e);
			return null;
		}
	}

	/**
	 * This methods allows to get the particpants.tsv file from BIDS/SEF import and deserialize it into subjects
	 * Then the non existing ones are created
	 * We finally return the full list of subjects
	 * @param participantsFilePath the partcipants.tsv file given
	 * @return A list of subjects updated with their IDs.
	 * If an error occurs, a list of a single subject with no ID and only a name is sent back
	 * @throws JsonProcessingException
	 */
	@RabbitListener(queues = RabbitMQConfiguration.SUBJECTS_QUEUE)
	@RabbitHandler
	@Transactional
	public Long createSubject(String subjectAsString) throws JsonProcessingException {
		// create a subject
		try {
			SecurityContextUtil.initAuthenticationContext("ADMIN_ROLE");

			Subject subject = mapper.readValue(subjectAsString, Subject.class);

			// Be carefull, ID field is used to carry the study ID here.
			Long studyId = subject.getId();
			Study study = studyRepository.findById(studyId).get();
			subject.setId(null);

			// Check subject existence by name
			Subject existingSubject = this.subjectRepository.findByName(subject.getName());

			// If it exists, update subject study list
			if (existingSubject != null) {
				if(existingSubject.getSubjectStudyList() == null) {
					existingSubject.setSubjectStudyList(new ArrayList<>());
				} else if (!studyListContains(existingSubject.getSubjectStudyList(), studyId)) {
					SubjectStudy subjectStudy = new SubjectStudy();
					subjectStudy.setSubject(existingSubject);
					subjectStudy.setStudy(study);
					existingSubject.getSubjectStudyList().add(subjectStudy);
				}
				subject = existingSubject;
			} else {
				if(subject.getSubjectStudyList() == null) {
					subject.setSubjectStudyList(new ArrayList<>());
				}
				SubjectStudy subjectStudy = new SubjectStudy();
				subjectStudy.setSubject(existingSubject);
				subjectStudy.setStudy(study);
				subject.getSubjectStudyList().add(subjectStudy);
			}
			
			// Then create/update the subject
			subjectService.create(subject);

			// Return subject ID
			return subject.getId();
		} catch (Exception e) {
			throw new AmqpRejectAndDontRequeueException(e);
		}
	}

	private boolean studyListContains(List<SubjectStudy> subjectStudyList, Long studyId) {
		for (SubjectStudy sustu : subjectStudyList) {
			if (sustu.getStudy().getId().equals(studyId)) {
				return true;
			}
		}
		return false;
	}
}

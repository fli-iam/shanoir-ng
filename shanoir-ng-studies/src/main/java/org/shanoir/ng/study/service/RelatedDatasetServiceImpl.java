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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityManager;
import org.shanoir.ng.center.model.Center;
import org.shanoir.ng.center.repository.CenterRepository;
import org.shanoir.ng.manufacturermodel.service.ManufacturerModelServiceImpl;
import org.shanoir.ng.messaging.StudyUserUpdateBroadcastService;
import org.shanoir.ng.shared.configuration.RabbitMQConfiguration;
import org.shanoir.ng.shared.exception.EntityNotFoundException;
import org.shanoir.ng.shared.exception.MicroServiceCommunicationException;
import org.shanoir.ng.shared.security.rights.StudyUserRight;
import org.shanoir.ng.study.dto.RelatedDatasetDTO;
import org.shanoir.ng.study.model.Study;
import org.shanoir.ng.study.model.StudyUser;
import org.shanoir.ng.study.repository.StudyRepository;
import org.shanoir.ng.study.repository.StudyUserRepository;
import org.shanoir.ng.study.security.StudySecurityService;
import org.shanoir.ng.studycenter.StudyCenter;
import org.shanoir.ng.subject.model.Subject;
import org.shanoir.ng.subject.repository.SubjectRepository;
import org.shanoir.ng.subject.service.SubjectService;
import org.shanoir.ng.subjectstudy.model.SubjectStudy;
import org.shanoir.ng.subjectstudy.model.SubjectStudyTag;
import org.shanoir.ng.subjectstudy.repository.SubjectStudyRepository;
import org.shanoir.ng.subjectstudy.service.SubjectStudyService;
import org.shanoir.ng.tag.model.Tag;
import org.shanoir.ng.utils.KeycloakUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Implementation of RelatedDataset service.
 *
 */
@Component
public class RelatedDatasetServiceImpl implements RelatedDatasetService {

	@Autowired
	private StudyUserRepository studyUserRepository;
	@Autowired
	private StudyService studyService;
	@Autowired
	private CenterRepository centerRepository;
	@Autowired
	private StudyRepository studyRepository;
	@Autowired
	private SubjectRepository subjectRepository;
	@Autowired
	private RabbitTemplate rabbitTemplate;
	@Autowired
	private ObjectMapper objectMapper;
	private static final Logger LOG = LoggerFactory.getLogger(RelatedDatasetServiceImpl.class);

	@Transactional
	@Override
	public void addSubjectStudyToNewStudy(List<Long> subjectIds, Long studyId) {
		Study study = studyService.findById(Long.valueOf(studyId));
		Boolean toAdd = true;
		Iterable<Subject> subjects = subjectRepository.findAllById(subjectIds);
		for (Subject subject : subjects) {
			List<SubjectStudy> subjectStudyList = study.getSubjectStudyList();

			for (SubjectStudy subjectStudy : subjectStudyList) {
				if (subjectStudy.getSubject().equals(subject)) {
					toAdd = false;
					break;
				} else {
					toAdd = true;
				}
			}

			if (toAdd) {
				SubjectStudy ssToAdd = new SubjectStudy();
				ssToAdd.setStudy(study);
				ssToAdd.setSubject(subject);

				subjectStudyList.add(ssToAdd);
				study.setSubjectStudyList(subjectStudyList);
				studyRepository.save(study);
			}
		}
	}

	@Override
	public String addCenterAndCopyDatasetToStudy(List<Long> datasetIds, Long studyId, List<Long> centerIds) {
		String result = "";
		Long userId = KeycloakUtil.getTokenUserId();
		Study study = studyService.findById(studyId);
		StudyUser studyUser = studyUserRepository.findByUserIdAndStudy_Id(userId, studyId);
		if (studyUser == null) {
			LOG.error("You must be part of both studies to copy datasets.");
			return "You must be part of both studies to copy datasets.";
		} else {
			List<StudyUserRight> rights = studyUser.getStudyUserRights();

			if (rights.contains(StudyUserRight.CAN_ADMINISTRATE) || rights.contains(StudyUserRight.CAN_IMPORT)) {
				addCenterToStudy(study, centerIds);

				try {
					result = copyDatasetToStudy(datasetIds, studyId, userId);
				} catch (MicroServiceCommunicationException e) {
					throw new RuntimeException(e);
				}
			} else {
				LOG.error("Missing IMPORT or ADMIN rights on destination study " + study.getName());
				return "Missing IMPORT or ADMIN rights on destination study " + study.getName();
			}
			return result;
		}
	}

	private void addCenterToStudy(Study study, List<Long> centerIds) {
		Iterable<Center> centers = centerRepository.findAllById(centerIds);
		for (Center center : centers) {
			List<StudyCenter> studyCenterList = study.getStudyCenterList();

			if (center != null && !studyCenterList.contains(center)) {
				StudyCenter centerToAdd = new StudyCenter();
				centerToAdd.setStudy(study);
				centerToAdd.setCenter(center);
				centerToAdd.setSubjectNamePrefix(null);
				studyCenterList.add(centerToAdd);
				study.setMonoCenter(false);
				study.setStudyCenterList(studyCenterList);
				studyRepository.save(study);
			}
		}
	}

	private String copyDatasetToStudy(List<Long> datasetIds, Long studyId, Long userId) throws MicroServiceCommunicationException {
		RelatedDatasetDTO dto = new RelatedDatasetDTO();
		dto.setStudyId(studyId);
		dto.setDatasetIds(datasetIds);
		dto.setUserId(userId);
		try {
			return (String) rabbitTemplate.convertSendAndReceive(RabbitMQConfiguration.COPY_DATASETS_TO_STUDY_QUEUE, objectMapper.writeValueAsString(dto));
		} catch (AmqpException | JsonProcessingException e) {
			throw new MicroServiceCommunicationException(
					"Error while communicating with datasets MS to copy datasets to study.", e);
		}
	}
}

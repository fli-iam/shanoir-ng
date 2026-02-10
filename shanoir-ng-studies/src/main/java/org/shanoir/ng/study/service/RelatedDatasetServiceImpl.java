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
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.shanoir.ng.center.model.Center;
import org.shanoir.ng.center.repository.CenterRepository;
import org.shanoir.ng.shared.configuration.RabbitMQConfiguration;
import org.shanoir.ng.shared.dataset.RelatedDataset;
import org.shanoir.ng.shared.exception.MicroServiceCommunicationException;
import org.shanoir.ng.shared.exception.SecurityException;
import org.shanoir.ng.shared.exception.ShanoirException;
import org.shanoir.ng.shared.security.rights.StudyUserRight;
import org.shanoir.ng.study.model.Study;
import org.shanoir.ng.study.model.StudyUser;
import org.shanoir.ng.study.repository.StudyRepository;
import org.shanoir.ng.study.repository.StudyUserRepository;
import org.shanoir.ng.studycenter.StudyCenter;
import org.shanoir.ng.subject.model.Subject;
import org.shanoir.ng.subject.service.SubjectService;
import org.shanoir.ng.tag.model.Tag;
import org.shanoir.ng.tag.repository.TagRepository;
import org.shanoir.ng.utils.KeycloakUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

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
    private SubjectService subjectService;

    @Autowired
    private TagRepository tagRepository;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    private static final Logger LOG = LoggerFactory.getLogger(RelatedDatasetServiceImpl.class);

    @Transactional
    @Override
    public void createSubjectsInTargetStudy(List<String> subjectIdStudyIds, Long studyId,
            String subjectName) throws ShanoirException {
        LOG.info("Starting createSubjectsInTargetStudy");
        long startTime = System.currentTimeMillis();
        List<Long> subjectIds = new ArrayList<>();
        for (String s : subjectIdStudyIds) {
            subjectIds.add(Long.valueOf(s.substring(0, s.indexOf("/"))));
        }
        Study targetStudy = studyService.findById(studyId);
        List<Subject> createdSubjects = new ArrayList<>();
        for (Long subjectId : subjectIds) {
            Subject sourceSubject = subjectService.findById(subjectId);
            if (sourceSubject == null) {
                throw new SecurityException(
                        "Copy dataset(s): source subject with ID " + subjectId + " not found.");
            }
            Subject targetSubject = subjectService.findByStudyIdAndName(targetStudy.getId(), sourceSubject.getName());
            if (targetSubject == null) {
                Subject createdSubject = null;
                if (createdSubjects.isEmpty()) {
                    createdSubject = createNewSubjectInTargetStudy(targetStudy, sourceSubject, subjectName, false);
                } else {
                    createdSubject = createNewSubjectInTargetStudy(targetStudy, sourceSubject, null, false);
                }
                createdSubjects.add(createdSubject);
            }
        }
        if (!createdSubjects.isEmpty()) {
            subjectService.updateSubjectBatchInMicroservices(createdSubjects);
        }
        long stopTime = System.currentTimeMillis();
        long elapsedTime = stopTime - startTime;
        LOG.info("Finished createSubjectsInTargetStudy: " + elapsedTime + "ms");
    }

    private Subject createNewSubjectInTargetStudy(Study targetStudy, Subject sourceSubject, String newSubjectName,
            boolean withAMQP)
            throws ShanoirException {
        Subject clonedSubject = new Subject(sourceSubject, targetStudy);
        if (newSubjectName != null && !newSubjectName.trim().isEmpty()) {
            clonedSubject.setName(newSubjectName);
        }
        if (sourceSubject.getTags() != null && !sourceSubject.getTags().isEmpty()) {
            Set<Tag> clonedTags = new HashSet<>();
            List<Long> tagIds = sourceSubject.getTags().stream()
                    .map(Tag::getId)
                    .collect(Collectors.toList());
            Iterable<Tag> managedTagsIt = tagRepository.findAllById(tagIds);
            managedTagsIt.forEach(clonedTags::add);
            clonedSubject.setTags((clonedTags));
        }
        clonedSubject = subjectService.create(clonedSubject, withAMQP);
        return clonedSubject;
    }

    @Override
    public String addCenterAndCopyDatasetToStudy(List<Long> datasetIds, Long studyId, List<Long> centerIds) throws SecurityException {
        String result = "";
        Long userId = KeycloakUtil.getTokenUserId();
        Study study = studyService.findById(studyId);
        StudyUser studyUser = studyUserRepository.findByUserIdAndStudy_Id(userId, studyId);
        if (!KeycloakUtil.isAdmin() && studyUser == null) {
            throw new SecurityException("User not member of study " + study.getName() + ".");
        } else {
            List<StudyUserRight> rights = studyUser.getStudyUserRights();
            if (rights.contains(StudyUserRight.CAN_ADMINISTRATE) || rights.contains(StudyUserRight.CAN_IMPORT)) {
                addCentersToStudy(study, centerIds);
                try {
                    copyDatasetsToStudy(datasetIds, studyId, userId);
                } catch (MicroServiceCommunicationException e) {
                    throw new RuntimeException(e);
                }
            } else {
                throw new SecurityException("Missing IMPORT or ADMIN rights on destination study " + study.getName());
            }
            return result;
        }
    }

    private void addCentersToStudy(Study study, List<Long> centerIds) {
        Iterable<Center> centers = centerRepository.findAllById(centerIds);
        List<StudyCenter> studyCenterList = study.getStudyCenterList();
        for (Center center : centers) {
            boolean add = true;
            for (StudyCenter sc : studyCenterList) {
                if (center != null && sc.getCenter().getId().equals(center.getId())) {
                    add = false;
                    break;
                }
            }
            if (add) {
                StudyCenter centerToAdd = new StudyCenter();
                centerToAdd.setStudy(study);
                centerToAdd.setCenter(center);
                centerToAdd.setSubjectNamePrefix(null);
                studyCenterList.add(centerToAdd);
            }
        }
        study.setStudyCenterList(studyCenterList);
        studyRepository.save(study);
    }

    private void copyDatasetsToStudy(List<Long> datasetIds, Long studyId, Long userId) throws MicroServiceCommunicationException {
        // datasetIds order is : selected datasets in solr from top of the table to bottom
        // reverse that order so that the first dataset to be treated is the last selected in solr
        Collections.sort(datasetIds);
        RelatedDataset dto = new RelatedDataset();
        dto.setStudyId(studyId);
        dto.setDatasetIds(datasetIds);
        dto.setUserId(userId);
        try {
            rabbitTemplate.convertAndSend(RabbitMQConfiguration.COPY_DATASETS_TO_STUDY_QUEUE, objectMapper.writeValueAsString(dto));
        } catch (AmqpException | JsonProcessingException e) {
            throw new MicroServiceCommunicationException(
                    "Error while communicating with datasets MS to copy datasets to study.", e);
        }
    }

}

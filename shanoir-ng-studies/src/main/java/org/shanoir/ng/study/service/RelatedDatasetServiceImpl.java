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
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.shanoir.ng.center.model.Center;
import org.shanoir.ng.center.repository.CenterRepository;
import org.shanoir.ng.shared.configuration.RabbitMQConfiguration;
import org.shanoir.ng.shared.dataset.RelatedDataset;
import org.shanoir.ng.shared.event.ShanoirEvent;
import org.shanoir.ng.shared.event.ShanoirEventService;
import org.shanoir.ng.shared.event.ShanoirEventType;
import org.shanoir.ng.shared.exception.MicroServiceCommunicationException;
import org.shanoir.ng.shared.exception.ShanoirException;
import org.shanoir.ng.study.dto.CopyData;
import org.shanoir.ng.study.model.Study;
import org.shanoir.ng.studycenter.StudyCenter;
import org.shanoir.ng.subject.model.Subject;
import org.shanoir.ng.subject.repository.SubjectRepository;
import org.shanoir.ng.subject.service.SubjectService;
import org.shanoir.ng.tag.model.Tag;
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

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

/**
 * Implementation of RelatedDataset service.
 *
 */
@Component
public class RelatedDatasetServiceImpl implements RelatedDatasetService {

    @Autowired
    private StudyService studyService;

    @Autowired
    private CenterRepository centerRepository;

    @Autowired
    private SubjectService subjectService;

    @Autowired
    private SubjectRepository subjectRepository;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ShanoirEventService eventService;

    @PersistenceContext
    private EntityManager entityManager;

    private static final Logger LOG = LoggerFactory.getLogger(RelatedDatasetServiceImpl.class);


    @Override
    @Transactional
    public void copyData(CopyData copyData) throws ShanoirException {
        ShanoirEvent event = new ShanoirEvent(
                ShanoirEventType.COPY_DATASET_EVENT,
                String.valueOf(copyData.getTargetStudyId()),
                KeycloakUtil.getTokenUserId(),
                "Copy datasets, starting...",
                ShanoirEvent.IN_PROGRESS
        );
        eventService.publishEvent(event);
        Map<Long, Long> subjectMapping = createSubjectsInTargetStudy(copyData.getSubjects(), copyData.getTargetStudyId(), event);
        addCentersInTargetStudy(copyData.getCenterIds(), copyData.getTargetStudyId(), event);
        copyDatasetsToStudy(copyData.getDatasetIds(), copyData.getTargetStudyId(), subjectMapping, event);
    }

    private Map<Long, Long> createSubjectsInTargetStudy(List<CopyData.SubjectCopy> subjects, Long studyId, ShanoirEvent event) throws ShanoirException {
        LOG.info("Starting createSubjectsInTargetStudy");
        eventService.publishEvent(event, "Creating subjects in target study", 0f);
        long startTime = System.currentTimeMillis();
        deDuplicateSubjectNames(subjects);

        Study targetStudy = studyService.findById(studyId);
        Map<Long, Long> subjectMapping = new HashMap<>();
        List<Subject> createdSubjects = new ArrayList<>();
        int i = 0;
        Map<Long, Subject> sourceSubjects = subjectRepository.findWithTagsByIdIn(subjects.stream().map(CopyData.SubjectCopy::getId).toList()).stream()
                .collect(Collectors.toMap(Subject::getId, s -> s));
        List<String> names = subjects.stream()
                .map(CopyData.SubjectCopy::getNewName)
                .toList();
        Map<String, Subject> existingByName = subjectRepository.findByStudyIdAndNameIn(studyId, names).stream()
                .collect(Collectors.toMap(Subject::getName, s -> s));
        for (CopyData.SubjectCopy subjectCopy : subjects) {
            Subject sourceSubject = sourceSubjects.get(subjectCopy.getId());
            if (sourceSubject == null) {
                throw new IllegalArgumentException(
                    "Copy dataset(s): source subject with ID " + subjectCopy.getId() + " not found.");
            }
            Subject targetSubject = existingByName.get(subjectCopy.getNewName());
            if (targetSubject == null) {
                Subject createdSubject = createNewSubjectInTargetStudy(targetStudy, sourceSubject, subjectCopy.getNewName(), false);
                subjectMapping.put(sourceSubject.getId(), createdSubject.getId());
                createdSubjects.add(createdSubject);
                if (++i % 200 == 0) {
                    entityManager.flush();
                    entityManager.clear();
                }
            } else {
                subjectMapping.put(sourceSubject.getId(), targetSubject.getId());
            }
        }
        if (!createdSubjects.isEmpty()) {
            subjectService.updateSubjectBatchInMicroservices(createdSubjects);
        }
        long stopTime = System.currentTimeMillis();
        long elapsedTime = stopTime - startTime;
        LOG.info("Finished createSubjectsInTargetStudy: " + elapsedTime + "ms");
        return subjectMapping;
    }

    /**
     * If there are several subjects with the same new name, we add a suffix to make them unique, otherwise the copy will fail because of the unique constraint on subject name within a study.
     * We do that before creating any subject in the target study to avoid creating then deleting subjects if there are duplicates.
     * Suffix is added to all subjects with the same new name, even the first one, to make it clearer for users that there was a duplication and that names have been modified.
     */
    private void deDuplicateSubjectNames(List<CopyData.SubjectCopy> subjects) {
        if (subjects == null || subjects.isEmpty()) return;

        // Group by normalized newName (trim, case-sensitive kept; adjust if you want case-insensitive)
        Map<String, List<CopyData.SubjectCopy>> byName = new LinkedHashMap<>();
        for (CopyData.SubjectCopy sc : subjects) {
            if (sc == null) continue;
            String name = sc.getNewName();
            if (name == null) continue;
            String key = name.trim();
            byName.computeIfAbsent(key, k -> new ArrayList<>()).add(sc);
        }

        // For each duplicated group, suffix every entry (including the first)
        for (Map.Entry<String, List<CopyData.SubjectCopy>> e : byName.entrySet()) {
            String base = e.getKey();
            List<CopyData.SubjectCopy> group = e.getValue();
            if (group.size() <= 1) continue;

            for (int i = 0; i < group.size(); i++) {
                // Example: "John" -> "John (1)", "John (2)", ...
                group.get(i).setNewName(base + " (" + (i + 1) + ")");
            }
        }
    }

    private Subject createNewSubjectInTargetStudy(Study targetStudy, Subject sourceSubject, String newSubjectName,
            boolean withAMQP) throws ShanoirException {
        Subject clonedSubject = new Subject(sourceSubject, targetStudy);
        addTagsInTargetStudy(sourceSubject, targetStudy);
        if (newSubjectName != null && !newSubjectName.trim().isEmpty()) {
            clonedSubject.setName(newSubjectName);
        }
        if (!sourceSubject.getTags().isEmpty()) {
            clonedSubject.setTags(new HashSet<>(sourceSubject.getTags()));
        }
        clonedSubject = subjectService.create(clonedSubject, withAMQP);
        if (clonedSubject != null && clonedSubject.isPreclinical()) {
            createNewAnimalSubject(sourceSubject.getId(), clonedSubject.getId());
        }
        return clonedSubject;
    }

    private void addTagsInTargetStudy(Subject sourceSubject, Study targetStudy) {
        if (sourceSubject.getTags() != null) {
            Set<String> existingTagNames = targetStudy.getTags().stream()
                    .map(t -> t.getName())
                    .collect(Collectors.toSet());
            for (Tag tag : sourceSubject.getTags()) {
                if (existingTagNames.add(tag.getName())) {
                    Tag newTag = new Tag();
                    newTag.setName(tag.getName());
                    newTag.setStudy(targetStudy);
                }
            }
        }
    }

    private void createNewAnimalSubject(Long sourceSubjectId, Long targetSubjectId) throws MicroServiceCommunicationException {
        record CopyRequest(Long sourceId, Long targetId) { }
        try {
            rabbitTemplate.convertAndSend(
                    RabbitMQConfiguration.COPY_ANIMAL_SUBJECT_QUEUE,
                    objectMapper.writeValueAsString(new CopyRequest(sourceSubjectId, targetSubjectId)));
        } catch (AmqpException | JsonProcessingException e) {
            throw new MicroServiceCommunicationException(
                    "Error while communicating with datasets MS to copy datasets to study.", e);
        }

    }

    private void addCentersInTargetStudy(List<Long> centerIds, Long studyId, ShanoirEvent event) throws ShanoirException {
        eventService.publishEvent(event, "Adding centers in target study...", 0f);
        Study study = studyService.findById(studyId);
        addCentersToStudy(study, centerIds);
    }

    private void copyDatasetsToStudy(List<Long> datasetIds, Long studyId, Map<Long, Long> subjectMapping, ShanoirEvent event) throws ShanoirException {
        try {
            copyDatasetsToStudy(datasetIds, studyId, subjectMapping, event.getId());
        } catch (MicroServiceCommunicationException e) {
            throw new ShanoirException("Error while copying datasets to target study.", e);
        }
    }

    private void addCentersToStudy(Study study, List<Long> centerIds) {
        List<StudyCenter> list = Optional.ofNullable(study.getStudyCenterList()).orElseGet(ArrayList::new);
        Set<Long> existing = list.stream()
                .map(sc -> sc.getCenter().getId())
                .collect(Collectors.toSet());
        for (Center center : centerRepository.findAllById(centerIds)) {
            if (existing.add(center.getId())) {
                StudyCenter sc = new StudyCenter();
                sc.setStudy(study);
                sc.setCenter(center);
                sc.setSubjectNamePrefix(null);
                list.add(sc);
            }
        }
        study.setStudyCenterList(list);
    }

    private void copyDatasetsToStudy(List<Long> datasetIds, Long studyId, Map<Long, Long> subjectMapping, Long eventId) throws MicroServiceCommunicationException {
        // datasetIds order is : selected datasets in solr from top of the table to bottom
        // reverse that order so that the first dataset to be treated is the last selected in solr
        Collections.sort(datasetIds);
        RelatedDataset dto = new RelatedDataset();
        dto.setStudyId(studyId);
        dto.setDatasetIds(datasetIds);
        dto.setUserId(KeycloakUtil.getTokenUserId());
        dto.setUserRole(KeycloakUtil.getUserRole());
        dto.setEventId(eventId);
        dto.setSubjectMapping(subjectMapping);
        try {
            rabbitTemplate.convertSendAndReceive(RabbitMQConfiguration.COPY_DATASETS_TO_STUDY_QUEUE, objectMapper.writeValueAsString(dto));
        } catch (AmqpException | JsonProcessingException e) {
            throw new MicroServiceCommunicationException(
                    "Error while communicating with datasets MS to copy datasets to study.", e);
        }
    }

}

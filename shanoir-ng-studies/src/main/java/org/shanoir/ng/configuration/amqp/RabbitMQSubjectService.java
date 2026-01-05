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

package org.shanoir.ng.configuration.amqp;


import org.shanoir.ng.shared.configuration.RabbitMQConfiguration;
import org.shanoir.ng.shared.core.model.IdName;
import org.shanoir.ng.shared.exception.ShanoirException;
import org.shanoir.ng.shared.subjectstudy.SubjectType;
import org.shanoir.ng.study.model.Study;
import org.shanoir.ng.study.repository.StudyRepository;
import org.shanoir.ng.subject.model.Subject;
import org.shanoir.ng.subject.repository.SubjectRepository;
import org.shanoir.ng.subject.service.SubjectService;
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

import com.fasterxml.jackson.databind.ObjectMapper;

@Service
public class RabbitMQSubjectService {

    private static final Logger LOG = LoggerFactory.getLogger(RabbitMQSubjectService.class);

    @Autowired
    private SubjectRepository subjectRepository;

    @Autowired
    private SubjectService subjectService;

    @Autowired
    private StudyRepository studyRepository;

    @Autowired
    private SubjectStudyRepository subjectStudyRepository;

    @Autowired
    private ObjectMapper mapper;

    /**
     * This methods returns a list of subjects for a given study ID
     * @param studyId the study ID
     * @return a list of subjects
     */
    @RabbitListener(queues = RabbitMQConfiguration.DATASET_SUBJECT_QUEUE, containerFactory = "multipleConsumersFactory")
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
    @RabbitListener(queues = RabbitMQConfiguration.DATASET_SUBJECT_STUDY_QUEUE, containerFactory = "multipleConsumersFactory")
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

    @RabbitListener(queues = RabbitMQConfiguration.SUBJECTS_NAME_QUEUE, containerFactory = "multipleConsumersFactory")
    @RabbitHandler
    @Transactional
    public boolean existsSubjectName(String name) {
        return this.subjectService.existsSubjectWithName(name);
    }

    @RabbitListener(queues = RabbitMQConfiguration.SUBJECTS_QUEUE, containerFactory = "multipleConsumersFactory")
    @RabbitHandler
    public Long createOrUpdateSubject(String subjectAsString) {
        try {
            SecurityContextUtil.initAuthenticationContext("ROLE_ADMIN");
            Subject subject = mapper.readValue(subjectAsString, Subject.class);
            subject = manageSubject(subject);
            return subject.getId();
        } catch (Exception e) {
            LOG.error("Error while creating the new subject: ", e);
            throw new AmqpRejectAndDontRequeueException(e);
        }
    }

    @Transactional
    private Subject manageSubject(Subject subject) throws ShanoirException {
        Long studyId = null;
        if (subject.getStudy() != null) {
            studyId = subject.getStudy().getId();
        }
        // @todo: to be removed later
        if (subject.getSubjectStudyList() != null && !subject.getSubjectStudyList().isEmpty()) {
            studyId = subject.getSubjectStudyList().get(0).getStudy().getId();
        }
        Subject subjectOld = subjectRepository.findByStudyIdAndName(studyId, subject.getName());
        if (subjectOld == null) {
            return subjectService.create(subject, true);
        } else {
            return subjectOld;
        }
    }

}

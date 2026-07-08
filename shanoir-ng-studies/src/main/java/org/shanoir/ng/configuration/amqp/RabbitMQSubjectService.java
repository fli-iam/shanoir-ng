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
import org.shanoir.ng.subject.model.Subject;
import org.shanoir.ng.subject.repository.SubjectRepository;
import org.shanoir.ng.subject.service.SubjectService;
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
    private SubjectRepository subjectRepository;

    @Autowired
    private SubjectService subjectService;

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

    @RabbitListener(queues = RabbitMQConfiguration.SUBJECTS_NAME_QUEUE, containerFactory = "multipleConsumersFactory")
    @RabbitHandler
    @Transactional
    public boolean existsSubjectName(String subjectNameInStudyString) {
        IdName subjectNameInStudy;
        try {
            subjectNameInStudy = mapper.readValue(subjectNameInStudyString, IdName.class);
            return this.subjectService.existsSubjectWithNameInStudy(subjectNameInStudy.getName(), subjectNameInStudy.getId());
        } catch (JsonProcessingException e) {
            LOG.error("Error while checking subject name existence", e);
            throw new AmqpRejectAndDontRequeueException(e);
        }
    }

    @RabbitListener(queues = RabbitMQConfiguration.SUBJECTS_QUEUE_WITH_DATASETS, containerFactory = "multipleConsumersFactory")
    @RabbitHandler
    public Long createOrUpdateSubjectWithAMQP(String subjectAsString) {
        try {
            SecurityContextUtil.initAuthenticationContext("ROLE_ADMIN");
            Subject subject = mapper.readValue(subjectAsString, Subject.class);
            subject = manageSubject(subject, true);
            return subject.getId();
        } catch (Exception e) {
            LOG.error("Error while creating the new subject: ", e);
            throw new AmqpRejectAndDontRequeueException(e);
        }
    }

    @RabbitListener(queues = RabbitMQConfiguration.SUBJECTS_QUEUE_WITHOUT_DATASETS, containerFactory = "multipleConsumersFactory")
    @RabbitHandler
    public Long createOrUpdateSubjectWithoutAMQP(String subjectAsString) {
        try {
            SecurityContextUtil.initAuthenticationContext("ROLE_ADMIN");
            Subject subject = mapper.readValue(subjectAsString, Subject.class);
            subject = manageSubject(subject, false);
            return subject.getId();
        } catch (Exception e) {
            LOG.error("Error while creating the new subject: ", e);
            throw new AmqpRejectAndDontRequeueException(e);
        }
    }

    @Transactional
    private Subject manageSubject(Subject subject, boolean withAMQP) throws ShanoirException {
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
            return subjectService.create(subject, withAMQP);
        } else {
            return subjectOld;
        }
    }

}

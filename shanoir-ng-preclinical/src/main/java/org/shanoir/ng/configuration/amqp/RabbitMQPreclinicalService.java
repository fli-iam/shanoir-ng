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

import java.util.Arrays;

import org.shanoir.ng.preclinical.pathologies.subject.SubjectPathologyService;
import org.shanoir.ng.preclinical.subjects.model.AnimalSubject;
import org.shanoir.ng.preclinical.subjects.service.AnimalSubjectService;
import org.shanoir.ng.preclinical.therapies.subject.SubjectTherapyService;
import org.shanoir.ng.shared.configuration.RabbitMQConfiguration;
import org.shanoir.ng.study.rights.ampq.StudyUserUpdateService;
import org.shanoir.ng.study.rights.command.StudyUserCommand;
import org.shanoir.ng.utils.SecurityContextUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.AmqpRejectAndDontRequeueException;
import org.springframework.amqp.core.ExchangeTypes;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import tools.jackson.databind.ObjectMapper;

@Component
public class RabbitMQPreclinicalService {

    private static final String RABBIT_MQ_ERROR = "Something went wrong deserializing the event.";

    @Autowired
    private AnimalSubjectService animalSubjectService;

    @Autowired
    private SubjectPathologyService subjectPathologyService;

    @Autowired
    private SubjectTherapyService subjectTherapyService;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private StudyUserUpdateService studyUserUpdateService;


    private static final Logger LOG = LoggerFactory.getLogger(RabbitMQPreclinicalService.class);

    public record CopyRequest(Long sourceId, Long targetId) { }

    /**
     * Keeps the local copy of the study members up to date, to check the study rights.
     * @param commandArrStr the study user commands broadcast by ms studies, as a json array
     */
    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(value = RabbitMQConfiguration.STUDY_USER_QUEUE_PRECLINICAL, durable = "true"),
            exchange = @Exchange(value = RabbitMQConfiguration.STUDY_USER_EXCHANGE, ignoreDeclarationExceptions = "true",
                autoDelete = "false", durable = "true", type = ExchangeTypes.FANOUT)), containerFactory = "multipleConsumersFactory"
    )
    public void receiveStudyUsers(String commandArrStr) throws AmqpRejectAndDontRequeueException {
        try {
            StudyUserCommand[] commands = objectMapper.readValue(commandArrStr, StudyUserCommand[].class);
            studyUserUpdateService.processCommands(Arrays.asList(commands));
        } catch (Exception e) {
            LOG.error("Study user update rejected: {}", e.getMessage());
            throw new AmqpRejectAndDontRequeueException("Study user update rejected: " + e.getMessage(), e);
        }
    }

    /**
     * Receives a shanoirEvent as a json object, concerning a subject deletion
     * @param subjectIdAsStr the subject's id to delete, as string
     */
    @RabbitListener(queues = RabbitMQConfiguration.DELETE_ANIMAL_SUBJECT_QUEUE)
    @Transactional
    public void deleteAnimalSubject(String subjectIdAsStr) throws AmqpRejectAndDontRequeueException {
        SecurityContextUtil.initAuthenticationContext("ADMIN_ROLE");
        try {
            Long subjectId = Long.valueOf(subjectIdAsStr);

            AnimalSubject animalSubject = animalSubjectService.getById(subjectId);

            if (animalSubject == null) {
                return;
            }
            Long id = animalSubject.getId();

            subjectPathologyService.deleteByAnimalSubject(animalSubject);
            subjectTherapyService.deleteByAnimalSubject(animalSubject);
            animalSubjectService.deleteById(subjectId);

            LOG.info("Animal subject [{}] has been deleted following deletion of subject [{}]", id, subjectId);

        } catch (Exception e) {
            LOG.error("Something went wrong deserializing the event. {}", e.getMessage());
            throw new AmqpRejectAndDontRequeueException(RABBIT_MQ_ERROR + e.getMessage(), e);
        }
    }


    @RabbitListener(queues = RabbitMQConfiguration.COPY_ANIMAL_SUBJECT_QUEUE)
    @Transactional
    public void createAnimalSubject(String copyRequestStr) throws AmqpRejectAndDontRequeueException {
        SecurityContextUtil.initAuthenticationContext("ADMIN_ROLE");
        try {
            CopyRequest copyRequest = objectMapper.readValue(copyRequestStr, CopyRequest.class);
            AnimalSubject sourceAnimalSubject = animalSubjectService.getById(copyRequest.sourceId());
            if (sourceAnimalSubject == null) {
                return;
            }
            AnimalSubject newAnimalSubject = new AnimalSubject(sourceAnimalSubject);
            newAnimalSubject.setId(copyRequest.targetId());
            animalSubjectService.save(newAnimalSubject);

        } catch (Exception e) {
            LOG.error("Something went wrong deserializing the event. {}", e.getMessage());
            throw new AmqpRejectAndDontRequeueException(RABBIT_MQ_ERROR + e.getMessage(), e);
        }
    }

}

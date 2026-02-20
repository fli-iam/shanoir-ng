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

import org.shanoir.ng.preclinical.pathologies.subject_pathologies.SubjectPathologyService;
import org.shanoir.ng.preclinical.subjects.model.AnimalSubject;
import org.shanoir.ng.preclinical.subjects.service.AnimalSubjectService;
import org.shanoir.ng.preclinical.therapies.subject_therapies.SubjectTherapyService;
import org.shanoir.ng.shared.configuration.RabbitMQConfiguration;
import org.shanoir.ng.shared.event.ShanoirEventService;
import org.shanoir.ng.utils.SecurityContextUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.AmqpRejectAndDontRequeueException;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.databind.ObjectMapper;

@Component
public class RabbitMQPreclinicalService {

    private static final String RABBIT_MQ_ERROR = "Something went wrong deserializing the event.";
    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private AnimalSubjectService animalSubjectService;

    @Autowired
    private SubjectPathologyService subjectPathologyService;

    @Autowired
    private SubjectTherapyService subjectTherapyService;

    @Autowired
    private ShanoirEventService eventService;

    private static final Logger LOG = LoggerFactory.getLogger(RabbitMQPreclinicalService.class);

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

}

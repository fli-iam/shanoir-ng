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
package org.shanoir.ng.examination.service;

import java.io.File;
import java.util.Optional;

import org.shanoir.ng.examination.model.Examination;
import org.shanoir.ng.examination.repository.ExaminationRepository;
import org.shanoir.ng.shared.configuration.RabbitMQConfiguration;
import org.shanoir.ng.shared.core.model.IdName;
import org.shanoir.ng.shared.model.Subject;
import org.shanoir.ng.shared.repository.SubjectRepository;
import org.springframework.amqp.AmqpRejectAndDontRequeueException;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Service for rabbit MQ communications concerning Examination.
 * @author fli
 *
 */
@Service
public class RabbitMqExaminationService {

    @Autowired
    private ExaminationRepository examRepo;

    @Autowired
    private ObjectMapper mapper;

    @Autowired
    private ExaminationService examinationService;

    @Autowired
    private SubjectRepository subjectRepository;

    @RabbitListener(queues = RabbitMQConfiguration.EXAMINATION_CREATION_QUEUE, containerFactory = "multipleConsumersFactory")
    @RabbitHandler
    @Transactional()
    public Long createExamination(Message message) {
        try {
            Examination exam = mapper.readValue(message.getBody(), Examination.class);
            Subject subj = exam.getSubject();
            subj.setStudy(exam.getStudy());
            Optional<Subject> dbSubject = subjectRepository.findById(subj.getId());
            if (!dbSubject.isPresent()) {
                subjectRepository.save(subj);
            }
            exam = examRepo.save(exam);
            return exam.getId();
        } catch (Exception e) {
            throw new AmqpRejectAndDontRequeueException(e);
        }
    }

    @RabbitListener(queues = RabbitMQConfiguration.EXAMINATION_EXTRA_DATA_QUEUE, containerFactory = "multipleConsumersFactory")
    @RabbitHandler
    @Transactional
    public void addExaminationExtraData(String path) {
        try {
            IdName examExtradata = mapper.readValue(path, IdName.class);
            // add examination extra-data
            examinationService.addExtraDataFromFile(examExtradata.getId(), new File(examExtradata.getName()));
        } catch (Exception e) {
            throw new AmqpRejectAndDontRequeueException(e);
        }
    }

}

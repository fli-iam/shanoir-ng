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

import java.io.IOException;

import org.shanoir.ng.examination.model.Examination;
import org.shanoir.ng.examination.repository.ExaminationRepository;
import org.shanoir.ng.shared.configuration.RabbitMQConfiguration;
import org.shanoir.ng.shared.core.model.IdName;
import org.shanoir.ng.shared.event.ShanoirEvent;
import org.shanoir.ng.shared.model.Study;
import org.shanoir.ng.shared.model.Subject;
import org.shanoir.ng.shared.repository.StudyRepository;
import org.shanoir.ng.shared.repository.SubjectRepository;
import org.shanoir.ng.study.rights.ampq.StudyUserListener;
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

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

/**
 * RabbitMQ configuration.
 */
@Component
public class RabbitMQDatasetsService {
	
	@Autowired
	private StudyUserListener listener;

	@Autowired
	private ExaminationRepository examRepo;
	
	@Autowired 
	private StudyRepository studyRepository;
	
	@Autowired 
	private SubjectRepository subjectRepository;

	private static final Logger LOG = LoggerFactory.getLogger(RabbitMQDatasetsService.class);

	@RabbitListener(bindings = @QueueBinding(
	        value = @Queue(value = RabbitMQConfiguration.STUDY_USER_QUEUE_DATASET, durable = "true"),
	        exchange = @Exchange(value = RabbitMQConfiguration.STUDY_USER_EXCHANGE, ignoreDeclarationExceptions = "true",
	        	autoDelete = "false", durable = "true", type=ExchangeTypes.FANOUT))
	)
    public void receiveMessage(String commandArrStr) {
		listener.receiveMessageImport(commandArrStr);
    }
	
	
	@RabbitListener(bindings = @QueueBinding(
	        value = @Queue(value = RabbitMQConfiguration.STUDY_NAME_UPDATE, durable = "true"),
	        exchange = @Exchange(value = RabbitMQConfiguration.STUDY_NAME_UPDATE, ignoreDeclarationExceptions = "true",
	        	autoDelete = "false", durable = "true", type=ExchangeTypes.DIRECT))
	)
    public void receiveStudyNameUpdate(final String studyStr) {
		ObjectMapper objectMapper = new ObjectMapper();
		IdName receivedStudy = new IdName();
		try {
			receivedStudy = objectMapper.readValue(studyStr, IdName.class);
			Study existingStudy = studyRepository.findOne(receivedStudy.getId());
			if (existingStudy != null) {
				// update existing study's name
				existingStudy.setName(receivedStudy.getName());
				studyRepository.save(existingStudy);
			} else {
				// create new study
				Study newStudy = new Study(receivedStudy.getId(), receivedStudy.getName());
				studyRepository.save(newStudy);
			}
		} catch (IOException e) {
			LOG.error("Could not read value transmit as Study class through RabbitMQ");
		}
    }

	@RabbitListener(bindings = @QueueBinding(
	        value = @Queue(value = RabbitMQConfiguration.SUBJECT_NAME_UPDATE, durable = "true"),
	        exchange = @Exchange(value = RabbitMQConfiguration.SUBJECT_NAME_UPDATE, ignoreDeclarationExceptions = "true",
	        	autoDelete = "false", durable = "true", type=ExchangeTypes.DIRECT))
	)
    public void receiveSubjectNameUpdate(final String subjectStr) {
		
		ObjectMapper objectMapper = new ObjectMapper();
		IdName receivedSubject = new IdName();
		try {
			receivedSubject = objectMapper.readValue(subjectStr, IdName.class);
			Subject existingSubject = subjectRepository.findOne(receivedSubject.getId());
			if (existingSubject != null) {
				existingSubject.setName(receivedSubject.getName());
				subjectRepository.save(existingSubject);
			} else {
				Subject newSubject = new Subject(receivedSubject.getId(), receivedSubject.getName());
				subjectRepository.save(newSubject);
			}
		} catch (IOException e) {
			LOG.error("Could not read value transmit as Subject class through RabbitMQ");
		}
	}

	/**
	 * Receives a shanoirEvent as a json object, thus create a event in the queue
	 * @param commandArrStr the task as a json string.
	 */
	@RabbitListener(bindings = @QueueBinding(
			key = "deleteSubject.event",
	        value = @Queue(value = RabbitMQConfiguration.DELETE_USER_QUEUE, durable = "true"),
	        exchange = @Exchange(value = RabbitMQConfiguration.EVENTS_EXCHANGE, ignoreDeclarationExceptions = "true",
	        	autoDelete = "false", durable = "true", type=ExchangeTypes.TOPIC))
	)	
	public void receiveEvent(String eventAsString) throws AmqpRejectAndDontRequeueException {
		ObjectMapper mapper = new ObjectMapper();
		mapper.registerModule(new JavaTimeModule());
		try {
			ShanoirEvent event = mapper.readValue(eventAsString, ShanoirEvent.class);
			// Delete associated examinations
			for (Examination exam : examRepo.findBySubjectId(Long.valueOf(event.getObjectId()))) {
				examRepo.delete(exam.getId());
			}
		} catch (Exception e) {
			LOG.error("Something went wrong deserializing the event. {}", e.getMessage());
			throw new AmqpRejectAndDontRequeueException("Something went wrong deserializing the event." + e.getMessage());
		}
	}
}

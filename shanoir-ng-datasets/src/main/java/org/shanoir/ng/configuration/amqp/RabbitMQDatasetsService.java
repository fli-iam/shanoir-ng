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

import org.shanoir.ng.dataset.model.Dataset;
import org.shanoir.ng.datasetacquisition.model.DatasetAcquisition;
import org.shanoir.ng.datasetacquisition.service.DatasetAcquisitionService;
import org.shanoir.ng.examination.model.Examination;
import org.shanoir.ng.examination.repository.ExaminationRepository;
import org.shanoir.ng.shared.configuration.RabbitMQConfiguration;
import org.shanoir.ng.shared.core.model.IdName;
import org.shanoir.ng.shared.event.ShanoirEvent;
import org.shanoir.ng.shared.event.ShanoirEventType;
import org.shanoir.ng.shared.model.Center;
import org.shanoir.ng.shared.model.Study;
import org.shanoir.ng.shared.model.Subject;
import org.shanoir.ng.shared.repository.CenterRepository;
import org.shanoir.ng.shared.repository.StudyRepository;
import org.shanoir.ng.shared.repository.SubjectRepository;
import org.shanoir.ng.solr.service.SolrService;
import org.shanoir.ng.study.rights.ampq.RabbitMqStudyUserService;
import org.shanoir.ng.studycard.model.StudyCard;
import org.shanoir.ng.studycard.repository.StudyCardRepository;
import org.shanoir.ng.utils.SecurityContextUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.AmqpRejectAndDontRequeueException;
import org.springframework.amqp.core.ExchangeTypes;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

/**
 * RabbitMQ configuration.
 */
@Component
public class RabbitMQDatasetsService {
	
	@Autowired
	private RabbitMqStudyUserService listener;

	@Autowired
	private StudyRepository studyRepository;

	@Autowired
	private SubjectRepository subjectRepository;

	@Autowired
	private CenterRepository centerRepository;

	@Autowired
	private SolrService solrService;

	@Autowired
	private DatasetAcquisitionService datasetAcquisitionService;

	@Autowired
	private ExaminationRepository examinationRepository;
	
	@Autowired
	private StudyCardRepository studyCardRepository;
	
	private static final Logger LOG = LoggerFactory.getLogger(RabbitMQDatasetsService.class);

	@RabbitListener(bindings = @QueueBinding(
			value = @Queue(value = RabbitMQConfiguration.STUDY_USER_QUEUE_DATASET, durable = "true"),
			exchange = @Exchange(value = RabbitMQConfiguration.STUDY_USER_EXCHANGE, ignoreDeclarationExceptions = "true",
			autoDelete = "false", durable = "true", type=ExchangeTypes.FANOUT))
			)
	public void receiveMessage(String commandArrStr) {
		listener.receiveMessageImport(commandArrStr);
	}

	@Transactional
	@RabbitListener(queues = RabbitMQConfiguration.STUDY_NAME_UPDATE_QUEUE)
	@RabbitHandler
	public void receiveStudyNameUpdate(final String studyStr) {
		receiveAndUpdateIdNameEntity(studyStr, Study.class, studyRepository);
	}

	@Transactional
	@RabbitListener(queues = RabbitMQConfiguration.SUBJECT_NAME_UPDATE_QUEUE)
	@RabbitHandler
	public void receiveSubjectNameUpdate(final String subjectStr) {
		receiveAndUpdateIdNameEntity(subjectStr, Subject.class, subjectRepository);
	}
	
	@Transactional
	@RabbitListener(queues = RabbitMQConfiguration.CENTER_NAME_UPDATE_QUEUE)
	@RabbitHandler
	public void receiveCenterNameUpdate(final String centerStr) {
		receiveAndUpdateIdNameEntity(centerStr, Center.class, centerRepository);
	}
	
	private <T extends IdName> void receiveAndUpdateIdNameEntity(final String receivedStr, final Class<T> clazz, final CrudRepository<T, Long> repository) {
		ObjectMapper objectMapper = new ObjectMapper();
		IdName received = new IdName();
		try {
			received = objectMapper.readValue(receivedStr, IdName.class);
			T existing = repository.findById(received.getId()).orElse(null);
			if (existing != null) {
				// update existing entity's name
				existing.setName(received.getName());
				repository.save(existing);
			} else {
				// create new entity
				try {
					T newOne = clazz.newInstance();
					newOne.setId(received.getId());
					newOne.setName(received.getName());
					repository.save(newOne);
				} catch ( SecurityException | InstantiationException | IllegalAccessException | IllegalArgumentException e) {
					throw new IllegalStateException("Cannot instanciate " + clazz.getSimpleName() + " class through reflection. It is a programming error.", e);
				}
			}
		} catch (IOException e) {
			LOG.error("Could not read value transmit as Subject class through RabbitMQ", e);
			throw new AmqpRejectAndDontRequeueException("Something went wrong deserializing the event." + e.getMessage());
		}
	}

	/**
	 * Receives a shanoirEvent as a json object, concerning a dataset acquisition to create
	 * @param commandArrStr the task as a json string.
	 */
	@RabbitListener(bindings = @QueueBinding(
			key = ShanoirEventType.CREATE_DATASET_ACQUISITION_EVENT,
			value = @Queue(value = RabbitMQConfiguration.CREATE_DATASET_ACQUISITION_QUEUE, durable = "true"),
			exchange = @Exchange(value = RabbitMQConfiguration.EVENTS_EXCHANGE, ignoreDeclarationExceptions = "true",
			autoDelete = "false", durable = "true", type=ExchangeTypes.TOPIC))
			)
	@Transactional(isolation = Isolation.READ_UNCOMMITTED,  propagation = Propagation.REQUIRES_NEW)
	public void createDatasetAcquisition(final String studyStr) {
		SecurityContextUtil.initAuthenticationContext("ADMIN_ROLE");
		ObjectMapper objectMapper = new ObjectMapper();
		try {
			ShanoirEvent event =  objectMapper.readValue(studyStr, ShanoirEvent.class);
			DatasetAcquisition acq = datasetAcquisitionService.findById(Long.valueOf(event.getObjectId()));
			if (acq != null) {
				for (Dataset ds : acq.getDatasets()) {
					solrService.indexDataset(ds.getId());
				}
			}
		} catch (Exception e) {
			LOG.error("Could not index datasets while creating new Dataset acquisition: ", e);
			throw new AmqpRejectAndDontRequeueException("Something went wrong deserializing the event." + e.getMessage());
		}
	}

	/**
	 * Receives a shanoirEvent as a json object, concerning a subject deletion
	 * @param commandArrStr the task as a json string.
	 */
	@RabbitListener(bindings = @QueueBinding(
			key = ShanoirEventType.DELETE_SUBJECT_EVENT,
			value = @Queue(value = RabbitMQConfiguration.DELETE_SUBJECT_QUEUE, durable = "true"),
			exchange = @Exchange(value = RabbitMQConfiguration.EVENTS_EXCHANGE, ignoreDeclarationExceptions = "true",
			autoDelete = "false", durable = "true", type=ExchangeTypes.TOPIC))
			)
	@Transactional
	public void deleteSubject(String eventAsString) throws AmqpRejectAndDontRequeueException {
		ObjectMapper mapper = new ObjectMapper();
		mapper.registerModule(new JavaTimeModule());
		SecurityContextUtil.initAuthenticationContext("ADMIN_ROLE");
		try {
			ShanoirEvent event = mapper.readValue(eventAsString, ShanoirEvent.class);

			// Delete associated examinations and datasets from solr repository
			for (Examination exam : examinationRepository.findBySubjectId(Long.valueOf(event.getObjectId()))) {
				for (DatasetAcquisition dsAcq : exam.getDatasetAcquisitions()) {
					for (Dataset ds : dsAcq.getDatasets())  {
						solrService.deleteFromIndex(ds.getId());
					}
				}
				examinationRepository.deleteById(exam.getId());
			}
			// Delete subject from datasets database
			subjectRepository.deleteById(Long.valueOf(event.getObjectId()));
		} catch (Exception e) {
			LOG.error("Something went wrong deserializing the event. {}", e.getMessage());
			throw new AmqpRejectAndDontRequeueException("Something went wrong deserializing the event." + e.getMessage());
		}
	}

	/**
	 * Receives a shanoirEvent as a json object, concerning a subject deletion
	 * @param commandArrStr the task as a json string.
	 */
	@RabbitListener(bindings = @QueueBinding(
			key = "deleteStudy.event",
			value = @Queue(value = RabbitMQConfiguration.DELETE_STUDY_QUEUE, durable = "true"),
			exchange = @Exchange(value = RabbitMQConfiguration.EVENTS_EXCHANGE, ignoreDeclarationExceptions = "true",
			autoDelete = "false", durable = "true", type=ExchangeTypes.TOPIC))
			)
	@Transactional
	public void deleteStudy(String eventAsString) throws AmqpRejectAndDontRequeueException {
		ObjectMapper mapper = new ObjectMapper();
		mapper.registerModule(new JavaTimeModule());
		SecurityContextUtil.initAuthenticationContext("ADMIN_ROLE");

		try {
			ShanoirEvent event = mapper.readValue(eventAsString, ShanoirEvent.class);

			// Delete associated examinations and datasets from solr repository then from database
			for (Examination exam : examinationRepository.findByStudyId(Long.valueOf(event.getObjectId()))) {
				for (DatasetAcquisition dsAcq : exam.getDatasetAcquisitions()) {
					for (Dataset ds : dsAcq.getDatasets())  {
						solrService.deleteFromIndex(ds.getId());
					}
				}
				examinationRepository.deleteById(exam.getId());
			}
			// also delete associated study cards
			for (StudyCard sc : studyCardRepository.findByStudyId(Long.valueOf(event.getObjectId()))) {
				studyCardRepository.delete(sc);
			}

			// Delete study from datasets database
			studyRepository.deleteById(Long.valueOf(event.getObjectId()));
		} catch (Exception e) {
			LOG.error("Something went wrong deserializing the event. {}", e.getMessage());
			throw new AmqpRejectAndDontRequeueException("Something went wrong deserializing the event." + e.getMessage());
		}
	}
}

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

import com.fasterxml.jackson.databind.ObjectMapper;

import org.shanoir.ng.email.EmailService;
import org.shanoir.ng.events.ShanoirEvent;
import org.shanoir.ng.events.ShanoirEventsService;
import org.shanoir.ng.shared.configuration.RabbitMQConfiguration;
import org.shanoir.ng.shared.email.DuaDraftWrapper;
import org.shanoir.ng.shared.email.EmailDatasetImportFailed;
import org.shanoir.ng.shared.email.EmailDatasetsImported;
import org.shanoir.ng.shared.email.EmailStudyUsersAdded;
import org.shanoir.ng.study.rights.ampq.RabbitMqStudyUserService;
import org.shanoir.ng.utils.SecurityContextUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.AmqpRejectAndDontRequeueException;
import org.springframework.amqp.core.ExchangeTypes;
import org.springframework.amqp.rabbit.annotation.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class RabbitMQUserService {

	private static final Logger LOG = LoggerFactory.getLogger(RabbitMQUserService.class);

	@Autowired
	ShanoirEventsService eventsService;

	@Autowired
	EmailService emailService;

	@Autowired
	private ObjectMapper mapper;

	@Autowired
	private RabbitMqStudyUserService listener;
	@RabbitListener(bindings = @QueueBinding(
			value = @Queue(value = RabbitMQConfiguration.STUDY_USER_QUEUE_USERS, durable = "true"),
			exchange = @Exchange(value = RabbitMQConfiguration.STUDY_USER_EXCHANGE, ignoreDeclarationExceptions = "true",
					autoDelete = "false", durable = "true", type = ExchangeTypes.FANOUT)), containerFactory = "multipleConsumersFactory"
	)
	public void receiveMessage(String commandArrStr) {
		listener.receiveStudyUsers(commandArrStr);
	}

	/**
	 * Receives a shanoirEvent as a json object, thus create a event in the queue
	 * @param commandArrStr the task as a json string.
	 */
	@RabbitListener(bindings = @QueueBinding(
			key = "*.event",
			value = @Queue(value = RabbitMQConfiguration.SHANOIR_EVENTS_QUEUE, durable = "true"),
	        exchange = @Exchange(value = RabbitMQConfiguration.EVENTS_EXCHANGE, ignoreDeclarationExceptions = "true",
	        	autoDelete = "false", durable = "true", type = ExchangeTypes.TOPIC)), containerFactory = "singleConsumerFactory"
	)
	public void receiveEvent(String eventAsString) throws AmqpRejectAndDontRequeueException {
		LOG.info("Receiving event: " + eventAsString);
		try {
			ShanoirEvent event = mapper.readValue(eventAsString, ShanoirEvent.class);
			eventsService.addEvent(event);
		} catch (Exception e) {
			LOG.error("Something went wrong deserializing the event.", e);
			throw new AmqpRejectAndDontRequeueException("Something went wrong deserializing the event.", e);
		}
	}

	/**
	 * Receives an import end event as a json object, thus send a mail to study manager to notice him
	 * @param commandArrStr the task as a json string.
	 */
	@RabbitListener(queues = RabbitMQConfiguration.IMPORT_DATASET_MAIL_QUEUE, containerFactory = "multipleConsumersFactory")
	@RabbitHandler
	public void receiveImportEvent(String generatedMailAsString) throws AmqpRejectAndDontRequeueException {
		SecurityContextUtil.initAuthenticationContext("ROLE_ADMIN");
		try {
			EmailDatasetsImported mail = mapper.readValue(generatedMailAsString, EmailDatasetsImported.class);
			this.emailService.notifyStudyManagerDataImported(mail);
		} catch (Exception e) {
			LOG.error("Something went wrong deserializing the import event.", e);
			throw new AmqpRejectAndDontRequeueException("Something went wrong deserializing the event.", e);
		}
	}

	/**
	 * Receives an import end event as a json object, thus send a mail to study manager to notice him
	 * that the import failed
	 * @param commandArrStr the task as a json string.
	 */
	@RabbitListener(queues = RabbitMQConfiguration.IMPORT_DATASET_FAILED_MAIL_QUEUE, containerFactory = "multipleConsumersFactory")
	@RabbitHandler
	public void receiveImportFailedEvent(String generatedMailAsString) throws AmqpRejectAndDontRequeueException {
		SecurityContextUtil.initAuthenticationContext("ROLE_ADMIN");
		try {
			EmailDatasetImportFailed mail = mapper.readValue(generatedMailAsString, EmailDatasetImportFailed.class);
			this.emailService.notifyStudyManagerImportFailure(mail);

		} catch (Exception e) {
			LOG.error("Something went wrong deserializing the import event.", e);
			throw new AmqpRejectAndDontRequeueException("Something went wrong deserializing the event.", e);
		}
	}

	/**
	 * Receives an study user report as a json object, thus send a mail to study manager to notice him
	 * @param commandArrStr the task as a json string.
	 */
	@RabbitListener(queues = RabbitMQConfiguration.STUDY_USER_MAIL_QUEUE, containerFactory = "multipleConsumersFactory")
	@RabbitHandler
	public void receiveStudyUserReport(String generatedMailAsString) throws AmqpRejectAndDontRequeueException {
		SecurityContextUtil.initAuthenticationContext("ROLE_ADMIN");
		try {
			EmailStudyUsersAdded mail = mapper.readValue(generatedMailAsString, EmailStudyUsersAdded.class);
			this.emailService.notifyStudyManagerStudyUsersAdded(mail);
		} catch (Exception e) {
			LOG.error("Something went wrong deserializing the import event.", e);
			throw new AmqpRejectAndDontRequeueException("Something went wrong deserializing the event.", e);
		}
	}

	/**
	 * Receives an study user report as a json object, thus send a mail to study manager to notice him
	 * @param commandArrStr the task as a json string.
	 */
	@RabbitListener(queues = RabbitMQConfiguration.DUA_DRAFT_MAIL_QUEUE, containerFactory = "multipleConsumersFactory")
	@RabbitHandler
	public void receiveDuaDraft(String duaDraftStr) throws AmqpRejectAndDontRequeueException {
		SecurityContextUtil.initAuthenticationContext("ROLE_ADMIN");
		try {
			DuaDraftWrapper mail = mapper.readValue(duaDraftStr, DuaDraftWrapper.class);
			this.emailService.notifyDuaDraftCreation(mail);
		} catch (Exception e) {
			LOG.error("Something went wrong deserializing the dua draft event.", e);
			throw new AmqpRejectAndDontRequeueException("Something went wrong deserializing the dua draft event.", e);
		}
	}
}

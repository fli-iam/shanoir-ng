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

import org.shanoir.ng.email.EmailService;
import org.shanoir.ng.events.ShanoirEvent;
import org.shanoir.ng.events.ShanoirEventsService;
import org.shanoir.ng.shared.configuration.RabbitMQConfiguration;
import org.shanoir.ng.shared.email.EmailDatasetsImported;
import org.shanoir.ng.shared.email.EmailStudyUsersAdded;
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
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

@Component
public class RabbitMQUserService {

	private static final Logger LOG = LoggerFactory.getLogger(RabbitMQUserService.class);

	@Autowired
	ShanoirEventsService eventsService;

	@Autowired
	EmailService emailService;

	/**
	 * Receives a shanoirEvent as a json object, thus create a event in the queue
	 * @param commandArrStr the task as a json string.
	 */
	@RabbitListener(bindings = @QueueBinding(
			key = "*.event",
			value = @Queue( value = RabbitMQConfiguration.SHANOIR_EVENTS_QUEUE, durable = "true"),
	        exchange = @Exchange(value = RabbitMQConfiguration.EVENTS_EXCHANGE, ignoreDeclarationExceptions = "true",
	        	autoDelete = "false", durable = "true", type=ExchangeTypes.TOPIC))
	)
	public void receiveEvent(String eventAsString) throws AmqpRejectAndDontRequeueException {
		ObjectMapper mapper = new ObjectMapper();
		mapper.registerModule(new JavaTimeModule());
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
	@RabbitListener(queues = RabbitMQConfiguration.IMPORT_DATASET_MAIL_QUEUE)
	@RabbitHandler
	public void receiveImportEvent(String generatedMailAsString) throws AmqpRejectAndDontRequeueException {
		SecurityContextUtil.initAuthenticationContext("ADMIN_ROLE");
		ObjectMapper mapper = new ObjectMapper();
		mapper.registerModule(new JavaTimeModule());
		try {
			EmailDatasetsImported mail = mapper.readValue(generatedMailAsString, EmailDatasetsImported.class);
			if (mail.getErrorMessage() == null) {
				this.emailService.notifyStudyManagerDataImported(mail);
			} else {
				this.emailService.notifyStudyManagerImportFailure(mail);
			}
		} catch (Exception e) {
			LOG.error("Something went wrong deserializing the import event.", e);
			throw new AmqpRejectAndDontRequeueException("Something went wrong deserializing the event.", e);
		}
	}
	
	/**
	 * Receives an study user report as a json object, thus send a mail to study manager to notice him
	 * @param commandArrStr the task as a json string.
	 */
	@RabbitListener(queues = RabbitMQConfiguration.STUDY_USER_MAIL_QUEUE)
	@RabbitHandler
	public void receiveStudyUserReport(String generatedMailAsString) throws AmqpRejectAndDontRequeueException {
		SecurityContextUtil.initAuthenticationContext("ADMIN_ROLE");
		ObjectMapper mapper = new ObjectMapper();
		mapper.registerModule(new JavaTimeModule());
		try {
			EmailStudyUsersAdded mail = mapper.readValue(generatedMailAsString, EmailStudyUsersAdded.class);
			this.emailService.notifyStudyManagerStudyUsersAdded(mail);
		} catch (Exception e) {
			LOG.error("Something went wrong deserializing the import event.", e);
			throw new AmqpRejectAndDontRequeueException("Something went wrong deserializing the event.", e);
		}
	}
	
}

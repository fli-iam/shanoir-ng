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

package org.shanoir.ng.messaging;

import org.shanoir.ng.configuration.amqp.RabbitMQConfiguration;
import org.shanoir.ng.events.UserDeleteEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/**
 * This class handles on using Spring Application Events
 * all communications with other microservices within Shanoir-NG.
 * 
 * @author mkain
 *
 */
@Component
public class InterMicroservicesCommunicator {

	/**
	 * Logger
	 */
	private static final Logger LOG = LoggerFactory.getLogger(InterMicroservicesCommunicator.class);
	
	@Autowired
	private RabbitTemplate rabbitTemplate;
	
	@EventListener
    public void handleUserDeleteEvent(UserDeleteEvent event) {
		try {
			LOG.debug("Start sending UserDeleteEvent...");
			rabbitTemplate.convertAndSend(RabbitMQConfiguration.getMSUsersToMSStudiesUserDelete().getName(), event.getUserId());
			LOG.debug("Sending UserDeleteEvent finished...");
		} catch (AmqpException e) {
			LOG.error("Error while sending message to RabbitMQ", e);
		}
    }
	
}

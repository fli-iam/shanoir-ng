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

import org.shanoir.ng.shared.exception.MicroServiceCommunicationException;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * RabbitMQ configuration.
 */
@Component
public class RabbitMQSendService {

	@Autowired
    private RabbitTemplate rabbitTemplate;
	
	@Autowired
	private ObjectMapper objectMapper;

	public void send(Object obj, String queue) throws MicroServiceCommunicationException {
	    try {
	        rabbitTemplate.convertAndSend(queue, objectMapper.writeValueAsString(obj));
	    } catch (AmqpException | JsonProcessingException e) {
	        throw new MicroServiceCommunicationException("Error while communicating with MS studies to send study card tags.", e);
	    }
	}
}

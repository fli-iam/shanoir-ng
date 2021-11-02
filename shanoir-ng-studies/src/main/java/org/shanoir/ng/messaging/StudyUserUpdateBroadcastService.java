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

import org.shanoir.ng.shared.configuration.RabbitMQConfiguration;
import org.shanoir.ng.shared.exception.MicroServiceCommunicationException;
import org.shanoir.ng.study.rights.command.StudyUserCommand;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

@Service
public class StudyUserUpdateBroadcastService {
	
	private static final Logger LOG = LoggerFactory.getLogger(StudyUserUpdateBroadcastService.class);

	@Autowired
	private RabbitTemplate rabbitTemplate;
	
	public void broadcast(Iterable<StudyUserCommand> commands) throws MicroServiceCommunicationException {
		try {
			String str = new ObjectMapper().writeValueAsString(commands);
			rabbitTemplate.convertAndSend(RabbitMQConfiguration.STUDY_USER_EXCHANGE, RabbitMQConfiguration.STUDY_USER_QUEUE, str);
			LOG.debug("Brodcasted study-user changes : {}", str);
		} catch (AmqpException | JsonProcessingException e) {
			throw new MicroServiceCommunicationException("Could not send data to study-user-exchange");
		}
	}

}

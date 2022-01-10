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
package org.shanoir.ng.studycard.service;

import org.shanoir.ng.shared.configuration.RabbitMQConfiguration;
import org.shanoir.ng.studycard.repository.StudyCardRepository;
import org.springframework.amqp.AmqpRejectAndDontRequeueException;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Service to manage rabbit MQ calls concerning study cards.
 * @author fli
 *
 */
@Service
public class RabbitMqStudyCardService {

	@Autowired
	private StudyCardRepository studyCardService;

	@Autowired
	ObjectMapper mapper;
	
	@RabbitListener(queues = RabbitMQConfiguration.FIND_STUDY_CARD_QUEUE)
	@RabbitHandler
	@Transactional
	public String findStudyCard(String message) {
		try {
			return mapper.writeValueAsString(studyCardService.findById(Long.valueOf(message)).orElse(null));
		} catch (Exception e) {
			throw new AmqpRejectAndDontRequeueException(e);
		}
	}
}

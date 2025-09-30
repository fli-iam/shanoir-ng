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

import java.util.List;
import java.util.Properties;

import org.shanoir.ng.shared.configuration.RabbitMQConfiguration;
import org.shanoir.ng.studycard.model.StudyCard;
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
	private ObjectMapper mapper;

	@RabbitListener(queues = RabbitMQConfiguration.FIND_STUDY_CARD_QUEUE, containerFactory = "multipleConsumersFactory")
	@RabbitHandler
	@Transactional
	public String findStudyCard(String message) {
		try {
			return mapper.writeValueAsString(studyCardService.findById(Long.valueOf(message)).orElse(null));
		} catch (Exception e) {
			throw new AmqpRejectAndDontRequeueException(e);
		}
	}

	@RabbitListener(queues = RabbitMQConfiguration.IMPORT_STUDY_CARD_QUEUE, containerFactory = "multipleConsumersFactory")
	@RabbitHandler
	@Transactional
	public Long getBestStudyCard(String message) {
		try {
			Properties properties = mapper.readValue(message, Properties.class);
			Long equipmentId = Long.valueOf(properties.getProperty("EQUIPMENT_ID_PROPERTY"));
			Long studyId = Long.valueOf(properties.getProperty("STUDY_ID_PROPERTY"));
			Long studyCardId = Long.valueOf(properties.getProperty("STUDYCARD_ID_PROPERTY"));
			List<StudyCard> studyCards = this.studyCardService.findByStudyId(studyId);
			for (StudyCard sc : studyCards) {
				if (sc.getAcquisitionEquipmentId().equals(equipmentId)) {
					return sc.getId();
				}
			}
			return studyCardId;
		} catch (Exception e) {
			throw new AmqpRejectAndDontRequeueException(e);
		}
	}

}

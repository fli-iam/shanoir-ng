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

import org.shanoir.ng.examination.model.Examination;
import org.shanoir.ng.examination.repository.ExaminationRepository;
import org.shanoir.ng.shared.configuration.RabbitMQConfiguration;
import org.springframework.amqp.AmqpRejectAndDontRequeueException;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

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
	ObjectMapper mapper;

	
	@RabbitListener(queues = RabbitMQConfiguration.EXAMINATION_CREATION_QUEUE)
	@RabbitHandler
	@Transactional
	public String findCenterIdFromAcquisitionEquipement(String message) {
		try {
			mapper.registerModule(new JavaTimeModule());

			Examination exam = mapper.readValue(message, Examination.class);
			exam = examRepo.save(exam);
			return mapper.writeValueAsString(exam);
		} catch (Exception e) {
			throw new AmqpRejectAndDontRequeueException(e);
		}
	}

	@RabbitListener(queues = RabbitMQConfiguration.DELETE_CENTER_QUEUE)
	@RabbitHandler
	@Transactional
	public boolean checkExaminationsFromCenter(String message) {
		try {
			Long centerId = mapper.readValue(message, Long.class);
			return CollectionUtils.isEmpty(examRepo.findByCenterId(centerId));
		} catch (Exception e) {
			throw new AmqpRejectAndDontRequeueException(e);
		}
	}
}

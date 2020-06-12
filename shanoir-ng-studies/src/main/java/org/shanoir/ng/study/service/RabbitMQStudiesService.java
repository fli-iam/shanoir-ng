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

package org.shanoir.ng.study.service;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import javax.transaction.Transactional;

import org.shanoir.ng.shared.configuration.RabbitMQConfiguration;
import org.shanoir.ng.shared.security.rights.StudyUserRight;
import org.shanoir.ng.study.model.Study;
import org.shanoir.ng.study.repository.StudyRepository;
import org.springframework.amqp.core.ExchangeTypes;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonProcessingException;

@Service
public class RabbitMQStudiesService {
	
	@Autowired
	private StudyRepository studyRepo;

	/**
	 * This methods allow to get the list of amdin users for a given study ID
	 * @param studyId the study ID
	 * @return a liost of ID of the users administrating the study
	 * @throws JsonProcessingException
	 */
	@RabbitListener(bindings = @QueueBinding(
	        value = @Queue(value = RabbitMQConfiguration.USER_ADMIN_STUDY_QUEUE, durable = "true"),
	        exchange = @Exchange(value = RabbitMQConfiguration.STUDY_ADMIN_EXCHANGE, ignoreDeclarationExceptions = "true",
	        	autoDelete = "false", durable = "true", type=ExchangeTypes.FANOUT)))
	@Transactional
	public List<Long> manageAdminsStudy(String studyId) throws JsonProcessingException {
		try {
		Study study = studyRepo.findOne(Long.valueOf(studyId));
		if (study == null) {
			return Collections.emptyList();
		}
		// Filter administrators and map to get only IDs
		List<Long> ids = study.getStudyUserList().stream()
				.filter(studyUser -> studyUser.getStudyUserRights().contains(StudyUserRight.CAN_ADMINISTRATE) && studyUser.isReceiveAnonymizationReport())
				.map(studyUser -> studyUser.getId())
				.collect(Collectors.toList());
		return ids;
		} catch (Exception e) {
			e.printStackTrace();
			return Collections.emptyList();
		}
	}
}

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

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import javax.transaction.Transactional;

import org.shanoir.ng.shared.configuration.RabbitMQConfiguration;
import org.shanoir.ng.shared.event.ShanoirEvent;
import org.shanoir.ng.shared.event.ShanoirEventType;
import org.shanoir.ng.shared.security.rights.StudyUserRight;
import org.shanoir.ng.study.dua.DataUserAgreementService;
import org.shanoir.ng.study.model.Study;
import org.shanoir.ng.study.model.StudyUser;
import org.shanoir.ng.study.repository.StudyRepository;
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
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

@Service
public class RabbitMQStudiesService {

	private static final Logger LOG = LoggerFactory.getLogger(RabbitMQStudiesService.class);

	@Autowired
	private StudyRepository studyRepo;

	@Autowired
	private StudyService studyService;
	
	@Autowired
	private DataUserAgreementService dataUserAgreementService;

	/**
	 * This methods allow to get the list of amdin users for a given study ID
	 * @param studyId the study ID
	 * @return a liost of ID of the users administrating the study
	 * @throws JsonProcessingException
	 */
	@RabbitListener(queues = RabbitMQConfiguration.USER_ADMIN_STUDY_QUEUE)
	@RabbitHandler
	@Transactional
	public List<Long> manageAdminsStudy(String studyId) {
		try {
			Study study = studyRepo.findOne(Long.valueOf(studyId));
			if (study == null) {
				return Collections.emptyList();
			}
			// Filter administrators and map to get only IDs
			return study.getStudyUserList().stream()
					.filter(studyUser -> studyUser.isReceiveNewImportReport())
					.map(studyUser -> studyUser.getUserId())
					.collect(Collectors.toList());
		} catch (Exception e) {
			LOG.error("Could not get study administrators.", e);
			return Collections.emptyList();
		}
	}

	/**
<<<<<<< HEAD
<<<<<<< HEAD
	 * Receives a shanoirEvent as a json object, concerning an examination creation
	 * @param commandArrStr the task as a json string.
	 */
	@RabbitListener(bindings = @QueueBinding(
			key = ShanoirEventType.CREATE_EXAMINATION_EVENT,
			value = @Queue(value = RabbitMQConfiguration.CREATE_DATASET_ACQUISITION_QUEUE, durable = "true"),
			exchange = @Exchange(value = RabbitMQConfiguration.EVENTS_EXCHANGE, ignoreDeclarationExceptions = "true",
			autoDelete = "false", durable = "true", type=ExchangeTypes.TOPIC))
			)
	@Transactional
	public void linkExamination(final String eventStr) {
		SecurityContextUtil.initAuthenticationContext("ADMIN_ROLE");
		ObjectMapper objectMapper = new ObjectMapper();
		try {
			ShanoirEvent event =  objectMapper.readValue(eventStr, ShanoirEvent.class);
			Long examinationId = Long.valueOf(event.getObjectId());
			Long studyId = Long.valueOf(event.getMessage());
			this.studyService.addExaminationToStudy(examinationId, studyId);

		} catch (Exception e) {
			LOG.error("Could not index examination on given study ", e);
			throw new AmqpRejectAndDontRequeueException("Something went wrong deserializing the event." + e.getMessage());
		}
	}

	/**
	 * Receives a shanoirEvent as a json object, concerning an examination creation
	 * @param commandArrStr the task as a json string.
	 */
	@RabbitListener(bindings = @QueueBinding(
			key = ShanoirEventType.DELETE_EXAMINATION_EVENT,
			value = @Queue(value = RabbitMQConfiguration.CREATE_DATASET_ACQUISITION_QUEUE, durable = "true"),
			exchange = @Exchange(value = RabbitMQConfiguration.EVENTS_EXCHANGE, ignoreDeclarationExceptions = "true",
			autoDelete = "false", durable = "true", type=ExchangeTypes.TOPIC))
			)
	@Transactional
	public void deleteExaminationStudy(final String eventStr) {
		SecurityContextUtil.initAuthenticationContext("ADMIN_ROLE");
		ObjectMapper objectMapper = new ObjectMapper();
		try {
			ShanoirEvent event =  objectMapper.readValue(eventStr, ShanoirEvent.class);
			Long examinationId = Long.valueOf(event.getObjectId());
			Long studyId = Long.valueOf(event.getMessage());
			this.studyService.deleteExamination(examinationId, studyId);

		} catch (Exception e) {
			LOG.error("Could not index examination on given study ", e);
			throw new AmqpRejectAndDontRequeueException("Something went wrong deserializing the event." + e.getMessage());
		}
	}

	/**
	 * Receives a shanoirEvent as a json object, concerning a challenge subscription
	 * @param commandArrStr the task as a json string.
	 */
	@RabbitListener(bindings = @QueueBinding(
			key = ShanoirEventType.CHALLENGE_SUBSCRIPTION_EVENT,
			value = @Queue(value = RabbitMQConfiguration.CHALLENGE_SUBSCRIPTION_QUEUE, durable = "true"),
			exchange = @Exchange(value = RabbitMQConfiguration.EVENTS_EXCHANGE, ignoreDeclarationExceptions = "true",
			autoDelete = "false", durable = "true", type=ExchangeTypes.TOPIC))
			)
	@Transactional
	public void challengeSubscription(final String studyStr) {
		SecurityContextUtil.initAuthenticationContext("ADMIN_ROLE");
		ObjectMapper objectMapper = new ObjectMapper();
		try {
			ShanoirEvent event =  objectMapper.readValue(studyStr, ShanoirEvent.class);
			Long userId = event.getUserId();
			Long studyId = Long.valueOf(event.getObjectId());
			// Get the study
			Study studyToUpdate = studyRepo.findOne(studyId);
			// Create a new StudyUser
			StudyUser subscription = new StudyUser();
			subscription.setStudy(studyToUpdate);
			subscription.setUserId(userId);
			subscription.setStudyUserRights(Arrays.asList(StudyUserRight.CAN_SEE_ALL, StudyUserRight.CAN_DOWNLOAD));
			subscription.setUserName(event.getMessage());
			if (studyToUpdate.getDataUserAgreementPaths() != null && !studyToUpdate.getDataUserAgreementPaths().isEmpty()) {
				subscription.setConfirmed(false);
				dataUserAgreementService.createDataUserAgreementForUserInStudy(studyToUpdate, subscription.getUserId());
			} else {
				subscription.setConfirmed(true);
			}
			studyService.addStudyUserToStudy(subscription, studyToUpdate);
		} catch (Exception e) {
			LOG.error("Could not directly subscribe a user to the challenge: ", e);
			throw new AmqpRejectAndDontRequeueException("Something went wrong deserializing the event." + e.getMessage(), e);
		}
	}
}

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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityManager;
import org.shanoir.ng.center.model.Center;
import org.shanoir.ng.center.repository.CenterRepository;
import org.shanoir.ng.manufacturermodel.service.ManufacturerModelServiceImpl;
import org.shanoir.ng.messaging.StudyUserUpdateBroadcastService;
import org.shanoir.ng.shared.configuration.RabbitMQConfiguration;
import org.shanoir.ng.shared.exception.EntityNotFoundException;
import org.shanoir.ng.shared.exception.MicroServiceCommunicationException;
import org.shanoir.ng.shared.security.rights.StudyUserRight;
import org.shanoir.ng.study.model.Study;
import org.shanoir.ng.study.model.StudyUser;
import org.shanoir.ng.study.repository.StudyRepository;
import org.shanoir.ng.study.repository.StudyUserRepository;
import org.shanoir.ng.study.security.StudySecurityService;
import org.shanoir.ng.studycenter.StudyCenter;
import org.shanoir.ng.tag.model.Tag;
import org.shanoir.ng.utils.KeycloakUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Implementation of study service.
 * 
 * @author msimon
 *
 */
@Component
public class RelatedDatasetServiceImpl implements RelatedDatasetService {

	@Autowired
	private StudyUserRepository studyUserRepository;
	@Autowired
	private StudyService studyService;
	@Autowired
	private CenterRepository centerRepository;
	@Autowired
	private StudyRepository studyRepository;
	@Autowired
	private RabbitTemplate rabbitTemplate;
	@Autowired
	private ObjectMapper objectMapper;
	private static final Logger LOG = LoggerFactory.getLogger(RelatedDatasetServiceImpl.class);

	public String addCenterAndCopyDatasetToStudy(String datasetIds, String id, String centerIds) {
		String result = "";
		result = addCenterToStudy(id, centerIds);

		try {
			copyDatasetToStudy(datasetIds, id);
		} catch (MicroServiceCommunicationException e) {
			throw new RuntimeException(e);
		}
		return result;
	}

	private String addCenterToStudy(String id, String centerIds) {
		Long userId = KeycloakUtil.getTokenUserId();
		Long studyId = Long.valueOf(id);

		Study study = studyService.findById(studyId);
		StudyUser studyUser = studyUserRepository.findByUserIdAndStudy_Id(userId, studyId);
		List<StudyUserRight> rights = studyUser.getStudyUserRights();

		if (rights.contains(StudyUserRight.CAN_ADMINISTRATE) || rights.contains(StudyUserRight.CAN_IMPORT)) {

			for (String centerId : centerIds.split(",")) {
				if (!centerRepository.findByStudy(studyId).contains(centerId)) {

					List<StudyCenter> studyCenterList = study.getStudyCenterList();
					Center center = centerRepository.findById(Long.valueOf(centerId)).orElse(null);
					List<Center> centersOfStudy = centerRepository.findByStudy(studyId);

					if (center != null && !centersOfStudy.contains(center)) {
						LOG.info("Add center " + center.getName() + " to study " + study.getName() + ".");
						System.out.println("add center " + center.getName() + " to study " + study.getName());
						StudyCenter centerToAdd = new StudyCenter();
						centerToAdd.setStudy(study);
						centerToAdd.setCenter(center);
						centerToAdd.setSubjectNamePrefix(null);
						studyCenterList.add(centerToAdd);
						study.setMonoCenter(false);
						study.setStudyCenterList(studyCenterList);
						studyRepository.save(study);
					} else {
						LOG.info("Center " + center.getName() + " already exists in study " + study.getName() + ".");
						// TODO if center already exist in study, return message to be displayed in modal
					}
				}
			}
			return "Center added to study";
		} else {
			LOG.error("Missing IMPORT or ADMIN rights on destination study " + study.getName());
			return "Missing IMPORT or ADMIN rights on destination study " + study.getName();
			// TODO if we don't have the rights on this study, return message to be displayed in modal
		}
	}

	private boolean copyDatasetToStudy(String datasetIds, String studyId) throws MicroServiceCommunicationException {
		System.out.println("copyDatasetToStudy datasetsIds : " + datasetIds + " / studyId : " + studyId);
		try {
			rabbitTemplate.convertAndSend(RabbitMQConfiguration.COPY_DATASETS_TO_STUDY, datasetIds + ";" + studyId);
			return true;
		} catch (AmqpException e) {
			throw new MicroServiceCommunicationException(
					"Error while communicating with datasets MS to copy to study.", e);
		}
	}
}

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

import java.util.ArrayList;
import java.util.List;

import org.shanoir.ng.shared.configuration.RabbitMQConfiguration;
import org.shanoir.ng.shared.exception.MicroServiceCommunicationException;
import org.shanoir.ng.shared.subjectstudy.SubjectStudyDTO;
import org.shanoir.ng.subjectstudy.model.SubjectStudy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

@Service
public class SubjectStudyUpdateBroadcastService {
	
	private static final Logger LOG = LoggerFactory.getLogger(SubjectStudyUpdateBroadcastService.class);

	@Autowired
	private RabbitTemplate rabbitTemplate;
	
	@Autowired
	private ObjectMapper objectMapper;
	
	public void send(List<SubjectStudy> subjectStudies) throws MicroServiceCommunicationException {
		try {
			String str = objectMapper.writeValueAsString(toDTO(subjectStudies));
			rabbitTemplate.convertAndSend(RabbitMQConfiguration.SUBJECT_STUDY_QUEUE, str);
			LOG.debug("sent subject-study creations : {}", str);
		} catch (AmqpException | JsonProcessingException e) {
			throw new MicroServiceCommunicationException("Could not send data to subject-study-exchange");
		}
	}

	private List<SubjectStudyDTO> toDTO(List<SubjectStudy> subjectStudies) {
		List<SubjectStudyDTO> dtos = new ArrayList<>();
		for (SubjectStudy subjectStudy : subjectStudies) {
			SubjectStudyDTO dto = new SubjectStudyDTO(
					subjectStudy.getId(),
					subjectStudy.getStudy().getId(), 
					subjectStudy.getSubject().getId(),
					(subjectStudy.getSubjectType() != null ? subjectStudy.getSubjectType().getId() : null));
			dtos.add(dto);
		}
		return dtos;
	}
}

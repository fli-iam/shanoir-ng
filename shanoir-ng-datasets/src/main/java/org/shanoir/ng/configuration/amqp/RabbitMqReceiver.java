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

import java.io.IOException;
import java.util.concurrent.CountDownLatch;

import org.shanoir.ng.shared.core.model.IdName;
import org.shanoir.ng.shared.model.Study;
import org.shanoir.ng.shared.model.Subject;
import org.shanoir.ng.shared.repository.StudyRepository;
import org.shanoir.ng.shared.repository.SubjectRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * @author yyao
 *
 */
@Configuration
public class RabbitMqReceiver {
	
	private static final String STUDY_NAME_UPDATE = "study_name_update";
	private static final String SUBJECT_NAME_UPDATE = "subject_name_update";
	private static final Logger LOG = LoggerFactory.getLogger(RabbitMqReceiver.class);
	
	@Autowired StudyRepository studyRepository;
	@Autowired SubjectRepository subjectRepository;
	
	private CountDownLatch latch = new CountDownLatch(1);
	
	public CountDownLatch getLatch() {
		return latch;
	}
	
	@RabbitListener(queues = STUDY_NAME_UPDATE)
	public void receiveStudyNameUpdate(final String studyStr) {
		
		ObjectMapper objectMapper = new ObjectMapper();
		IdName receivedStudy = new IdName();
		try {
			receivedStudy = objectMapper.readValue(studyStr, IdName.class);
			Study existingStudy = studyRepository.findOne(receivedStudy.getId());
			if (existingStudy != null) {
				// update existing study's name
				existingStudy.setName(receivedStudy.getName());
				studyRepository.save(existingStudy);
			} else {
				// create new study
				Study newStudy = new Study(receivedStudy.getId(), receivedStudy.getName());
				studyRepository.save(newStudy);
			}
		} catch (IOException e) {
			LOG.error("Could not read value transmit as Study class through RabbitMQ");
		}
		
		latch.countDown();
	}
	
	@RabbitListener(queues = SUBJECT_NAME_UPDATE)
	public void receiveSubjectNameUpdate(final String subjectStr) {
		
		ObjectMapper objectMapper = new ObjectMapper();
		IdName receivedSubject = new IdName();
		try {
			receivedSubject = objectMapper.readValue(subjectStr, IdName.class);
			Subject existingSubject = subjectRepository.findOne(receivedSubject.getId());
			if (existingSubject != null) {
				existingSubject.setName(receivedSubject.getName());
				subjectRepository.save(existingSubject);
			} else {
				Subject newSubject = new Subject(receivedSubject.getId(), receivedSubject.getName());
				subjectRepository.save(newSubject);
			}
		} catch (IOException e) {
			LOG.error("Could not read value transmit as Subject class through RabbitMQ");
		}
		
		latch.countDown();
	}
}

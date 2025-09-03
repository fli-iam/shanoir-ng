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

package org.shanoir.ng.shared.service;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.shanoir.ng.shared.configuration.RabbitMQConfiguration;
import org.shanoir.ng.shared.exception.EntityNotFoundException;
import org.shanoir.ng.shared.exception.MicroServiceCommunicationException;
import org.shanoir.ng.shared.model.Subject;
import org.shanoir.ng.shared.quality.SubjectQualityTagDTO;
import org.shanoir.ng.shared.repository.SubjectRepository;
import org.shanoir.ng.utils.Utils;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Subject service implementation.
 *
 * @author yyao
 *
 */
@Service
public class SubjectService {

	@Autowired
	private SubjectRepository subjectRepository;

	@Autowired
    private RabbitTemplate rabbitTemplate;

	@Autowired
	private ObjectMapper objectMapper;

	public List<Subject> update(final Iterable<Subject> subjectsNew) throws EntityNotFoundException, MicroServiceCommunicationException {
        if (subjectsNew == null) return null;
		Set<Long> ids = new HashSet<>();
	    for (Subject subjectNew : subjectsNew) {
	        ids.add(subjectNew.getId());
	    }
		final Iterable<Subject> subjectsOld = subjectRepository.findAllById(ids);
        for (Subject subjectNew : subjectsNew) {
            for (Subject subjectOld : subjectsOld) {
                if (subjectOld.getId().equals(subjectNew.getId())) {
                    updateSubjectValues(subjectOld, subjectNew);
                    break;
                }
            }
        }
		subjectRepository.saveAll(subjectsOld);
        List<SubjectQualityTagDTO> SubjectTagDTOs = getSubjectTagDTOs(Utils.toList(subjectsOld));
        this.send(SubjectTagDTOs, RabbitMQConfiguration.STUDIES_SUBJECT_STUDY_STUDY_CARD_TAG);
		return Utils.toList(subjectsOld);
	}
	
    private Subject updateSubjectValues(final Subject subjectOld, final Subject subjectNew) {
        subjectOld.setId(subjectNew.getId());
        subjectOld.setSubjectType(subjectNew.getSubjectType());
        subjectOld.setQualityTag(subjectNew.getQualityTag());
        return subjectOld;
    }

    private List<SubjectQualityTagDTO> getSubjectTagDTOs(List<Subject> updatedSubjects) {
        List<SubjectQualityTagDTO> dtos = new ArrayList<>();
        if (updatedSubjects != null) {
            for (Subject Subject : updatedSubjects) {
                SubjectQualityTagDTO dto = new SubjectQualityTagDTO();
                dto.setSubjectId(Subject.getId());
                dto.setTag(Subject.getQualityTag());
                dtos.add(dto);
            }            
        }
        return dtos;
    }

    public List<Subject> get(Long studyId) {
        return subjectRepository.findByStudy_Id(studyId);
    }

	private void send(Object obj, String queue) throws MicroServiceCommunicationException {
	    try {
	        rabbitTemplate.convertAndSend(queue, objectMapper.writeValueAsString(obj));
	    } catch (AmqpException | JsonProcessingException e) {
	        throw new MicroServiceCommunicationException("Error while communicating with MS studies to send study card tags.", e);
	    }
	}

}

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

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.shanoir.ng.shared.configuration.RabbitMQConfiguration;
import org.shanoir.ng.shared.exception.EntityNotFoundException;
import org.shanoir.ng.shared.exception.MicroServiceCommunicationException;
import org.shanoir.ng.shared.model.SubjectStudy;
import org.shanoir.ng.shared.quality.SubjectStudyQualityTagDTO;
import org.shanoir.ng.shared.repository.SubjectStudyRepository;
import org.shanoir.ng.utils.Utils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.amqp.AmqpException;
import com.fasterxml.jackson.core.JsonProcessingException;

/**
 * SubjectStudy service implementation.
 *
 * @author yyao
 *
 */
@Service
public class SubjectStudyServiceImpl implements SubjectStudyService {

	@Autowired
	private SubjectStudyRepository subjectStudyRepository;

	@Autowired
    private RabbitTemplate rabbitTemplate;

	@Autowired
	private ObjectMapper objectMapper;

	@Override
	public List<SubjectStudy> update(final Iterable<SubjectStudy> subjectStudies) throws EntityNotFoundException, MicroServiceCommunicationException {
        if (subjectStudies == null) return null;
		Set<Long> ids = new HashSet<>();
	    for (SubjectStudy subjectStudy : subjectStudies) {
	        ids.add(subjectStudy.getId());
	    }
		final Iterable<SubjectStudy> subjectStudiesDb = subjectStudyRepository.findAllById(ids);
        for (SubjectStudy subjectStudy : subjectStudies) {
            for (SubjectStudy subjectStudyDb : subjectStudiesDb) {
                if (subjectStudyDb.getId().equals(subjectStudy.getId())) {
                    updateSubjectStudyValues(subjectStudyDb, subjectStudy);
                    break;
                }
            }
        }
		subjectStudyRepository.saveAll(subjectStudiesDb);
        List<SubjectStudyQualityTagDTO> subjectStudyTagDTOs = getSubjectStudyTagDTOs(Utils.toList(subjectStudiesDb));
			    this.send(subjectStudyTagDTOs, RabbitMQConfiguration.STUDIES_SUBJECT_STUDY_STUDY_CARD_TAG);
		return Utils.toList(subjectStudiesDb);
	}
	
	/*
     * Update some values of subject study to save them in database.
     *
     * @param subjectStudyDb subjectStudy found in database.
     * @param subjectStudy subjectStudy with new values.
     * @return database subjectStudy with new values.
     */
    private SubjectStudy updateSubjectStudyValues(final SubjectStudy subjectStudyDb, final SubjectStudy subjectStudy) {
        subjectStudyDb.setId(subjectStudy.getId());
        subjectStudyDb.setSubjectType(subjectStudy.getSubjectType());
        subjectStudyDb.setQualityTag(subjectStudy.getQualityTag());
        return subjectStudyDb;
    }


    private List<SubjectStudyQualityTagDTO> getSubjectStudyTagDTOs(List<SubjectStudy> updatedSubjectStudies) {
        List<SubjectStudyQualityTagDTO> dtos = new ArrayList<>();
        if (updatedSubjectStudies != null) {
            for (SubjectStudy subjectStudy : updatedSubjectStudies) {
                SubjectStudyQualityTagDTO dto = new SubjectStudyQualityTagDTO();
                dto.setSubjectStudyId(subjectStudy.getId());
                dto.setTag(subjectStudy.getQualityTag());
                dtos.add(dto);
            }            
        }
        return dtos;
    }

    @Override
    public List<SubjectStudy> get(Long subjectId, Long studyId) {
        return subjectStudyRepository.findByStudy_IdAndSubjectId(studyId, subjectId);
    }

	private void send(Object obj, String queue) throws MicroServiceCommunicationException {
	    try {
	        rabbitTemplate.convertAndSend(queue, objectMapper.writeValueAsString(obj));
	    } catch (AmqpException | JsonProcessingException e) {
	        throw new MicroServiceCommunicationException("Error while communicating with MS studies to send study card tags.", e);
	    }
	}
}

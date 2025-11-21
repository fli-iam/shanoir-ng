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

package org.shanoir.ng.subject.controler;


import org.shanoir.ng.shared.configuration.RabbitMQConfiguration;

import io.swagger.v3.oas.annotations.Parameter;

import org.shanoir.ng.shared.core.model.IdName;
import org.shanoir.ng.shared.error.FieldErrorMap;
import org.shanoir.ng.shared.event.ShanoirEvent;
import org.shanoir.ng.shared.event.ShanoirEventService;
import org.shanoir.ng.shared.event.ShanoirEventType;
import org.shanoir.ng.shared.exception.*;
import org.shanoir.ng.study.model.Study;
import org.shanoir.ng.study.service.StudyService;
import org.shanoir.ng.subject.dto.SimpleSubjectDTO;
import org.shanoir.ng.subject.dto.SubjectDTO;
import org.shanoir.ng.subject.dto.mapper.SubjectMapper;
import org.shanoir.ng.subject.model.Subject;
import org.shanoir.ng.subject.service.SubjectService;
import org.shanoir.ng.subject.service.SubjectUniqueConstraintManager;
import org.shanoir.ng.utils.KeycloakUtil;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import jakarta.transaction.Transactional;

@Controller
public class SubjectApiController implements SubjectApi {

    @Autowired
    private SubjectMapper subjectMapper;

    @Autowired
    private SubjectService subjectService;

    @Autowired
    private SubjectUniqueConstraintManager uniqueConstraintManager;

    @Autowired
    private ShanoirEventService eventService;

    @Autowired
    private StudyService studyService;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Override
    public ResponseEntity<Void> deleteSubject(
            @Parameter(description = "id of the subject", required = true) @PathVariable("subjectId") Long subjectId) {
        try {
            // Delete all associated BIDS folders
            subjectService.deleteById(subjectId);
            eventService.publishEvent(new ShanoirEvent(ShanoirEventType.DELETE_SUBJECT_EVENT, subjectId.toString(), KeycloakUtil.getTokenUserId(), "", ShanoirEvent.SUCCESS));
            rabbitTemplate.convertAndSend(RabbitMQConfiguration.DELETE_SUBJECT_QUEUE, subjectId.toString());
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } catch (EntityNotFoundException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @Override
    public ResponseEntity<SubjectDTO> findSubjectById(
            @Parameter(description = "id of the subject", required = true) @PathVariable("subjectId") Long subjectId) {
        final Subject subject = subjectService.findById(subjectId);
        if (subject == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(subjectMapper.subjectToSubjectDTO(subject), HttpStatus.OK);
    }

    @Override
    @Transactional
    public ResponseEntity<List<SubjectDTO>> findSubjects(boolean preclinical, boolean clinical) {
        List<Subject> subjects = new ArrayList<>();
        if (preclinical && clinical) {
            subjects = subjectService.findAll();
        } else if (preclinical) {
            subjects = subjectService.findByPreclinical(true);
        } else if (clinical) {
            subjects = subjectService.findByPreclinical(false);
        }
        if (subjects.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        }
        return new ResponseEntity<>(subjectMapper.subjectsToSubjectDTOs(subjects), HttpStatus.OK);
    }

    @Override
    public ResponseEntity<List<IdName>> findAllSubjectsNames() {
        final List<IdName> subjectsNames = subjectService.findAllNames();
        if (subjectsNames.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        }
        return new ResponseEntity<>(subjectsNames, HttpStatus.OK);
    }

    @Override
    public ResponseEntity<List<IdName>> findSubjectsNames(List<Long> subjectIds) {
        final List<IdName> subjectsNames = subjectService.findNames(subjectIds);
        if (subjectsNames.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        }
        return new ResponseEntity<>(subjectsNames, HttpStatus.OK);
    }

    // Attention: this method is used by ShanoirUploader!!!
    @Override
    public ResponseEntity<SubjectDTO> saveNewSubject(
            @RequestBody Subject subject,
            @RequestParam(required = false) Long centerId,
            final BindingResult result) throws ShanoirException, RestServiceException {
        validate(subject, result);
        Subject createdSubject;
        if (centerId == null) {
            // #2475 Trim subject common name, only when not coming from SHUP
            if (subject.getName() != null) {
                subject.setName(subject.getName().trim());
            }
            createdSubject = subjectService.create(subject);
        } else {
            createdSubject = subjectService.createAutoIncrement(subject, centerId);
        }
        eventService.publishEvent(new ShanoirEvent(ShanoirEventType.CREATE_SUBJECT_EVENT, createdSubject.getId().toString(), KeycloakUtil.getTokenUserId(), "", ShanoirEvent.SUCCESS));
        final SubjectDTO subjectDTO = subjectMapper.subjectToSubjectDTO(createdSubject);
        return new ResponseEntity<SubjectDTO>(subjectDTO, HttpStatus.OK);
    }

    // Attention: this method is used by ShanoirUploader!!!
    @Override
    public ResponseEntity<Void> updateSubject(
            @Parameter(description = "id of the subject", required = true) @PathVariable("subjectId") Long subjectId,
            @Parameter(description = "subject to update", required = true) @RequestBody Subject subject,
            final BindingResult result) throws RestServiceException, MicroServiceCommunicationException {
        validate(subject, result);
        try {
            subjectService.update(subject);
            eventService.publishEvent(new ShanoirEvent(ShanoirEventType.UPDATE_SUBJECT_EVENT, subject.getId().toString(), KeycloakUtil.getTokenUserId(), "", ShanoirEvent.SUCCESS));
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } catch (EntityNotFoundException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        } catch (ShanoirException e) {
            throw new RestServiceException(e, new ErrorModel(e.getErrorCode(), e.getMessage()));
        }
    }

    // Attention: this method is used by ShanoirUploader!!!
    @Override
    public ResponseEntity<List<SimpleSubjectDTO>> findSubjectsByStudyId(
            @Parameter(description = "id of the study", required = true) @PathVariable("studyId") Long studyId,
            @Parameter(description = "preclinical", required = false) @RequestParam(value = "preclinical", required = false) String preclinical) {
        final List<SimpleSubjectDTO> simpleSubjectDTOList;
        if ("null".equals(preclinical)) {
            simpleSubjectDTOList = subjectService.findAllSubjectsOfStudyId(studyId);
        } else {
            simpleSubjectDTOList = subjectService.findAllSubjectsOfStudyAndPreclinical(studyId, Boolean.parseBoolean(preclinical));
        }
        if (simpleSubjectDTOList.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        }
        simpleSubjectDTOList.sort(new Comparator<SimpleSubjectDTO>() {
            @Override
            public int compare(SimpleSubjectDTO o1, SimpleSubjectDTO o2) {
                String aname = o1.getSubjectStudy().getSubjectStudyIdentifier() != null ? o1.getSubjectStudy().getSubjectStudyIdentifier() : o1.getName();
                String bname = o2.getSubjectStudy().getSubjectStudyIdentifier() != null ? o2.getSubjectStudy().getSubjectStudyIdentifier() : o2.getName();
                return aname.compareToIgnoreCase(bname);
            }
        });
        return new ResponseEntity<>(simpleSubjectDTOList, HttpStatus.OK);
    }

    // Attention: this method is used by ShanoirUploader!!!
    @Override
    @Transactional
    public ResponseEntity<SubjectDTO> findSubjectByIdentifier(
            @Parameter(description = "identifier of the subject", required = true) @PathVariable("subjectIdentifier") String subjectIdentifier) {
        // Get all allowed studies
        List<Study> studies = studyService.findAll();
        // As only studies are used to find a subject, in which the user has rights, no need for further rights checks
        final Subject subject = subjectService.findByIdentifierInStudiesWithRights(subjectIdentifier, studies);
        if (subject == null) {
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        }
        return new ResponseEntity<>(subjectMapper.subjectToSubjectDTO(subject), HttpStatus.OK);
    }

    private void validate(Subject subject, BindingResult result) throws RestServiceException {
        final FieldErrorMap errors = new FieldErrorMap().add(new FieldErrorMap(result))
                .add(uniqueConstraintManager.validate(subject));
        if (!errors.isEmpty()) {
            ErrorModel error = new ErrorModel(HttpStatus.UNPROCESSABLE_ENTITY.value(), "Bad arguments",
                    new ErrorDetails(errors));
            throw new RestServiceException(error);
        }
    }

    public ResponseEntity<Page<SubjectDTO>> findClinicalSubjectsPageByName(Pageable page, String name) {
        // Get all allowed studies
        List<Study> studies = this.studyService.findAll();
        Page<Subject> subjects = this.subjectService.getClinicalFilteredPageByStudies(page, name, studies);
        if (subjects.getContent().isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        }
        return new ResponseEntity<>(subjectMapper.subjectsToSubjectDTOs(subjects), HttpStatus.OK);
    }

}

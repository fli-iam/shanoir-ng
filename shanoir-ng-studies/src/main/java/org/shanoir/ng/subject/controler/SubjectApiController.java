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

import java.util.Comparator;
import java.util.List;

import org.shanoir.ng.bids.service.StudyBIDSService;
import org.shanoir.ng.shared.core.model.IdName;
import org.shanoir.ng.shared.error.FieldErrorMap;
import org.shanoir.ng.shared.event.ShanoirEvent;
import org.shanoir.ng.shared.event.ShanoirEventService;
import org.shanoir.ng.shared.event.ShanoirEventType;
import org.shanoir.ng.shared.exception.EntityNotFoundException;
import org.shanoir.ng.shared.exception.ErrorDetails;
import org.shanoir.ng.shared.exception.ErrorModel;
import org.shanoir.ng.shared.exception.MicroServiceCommunicationException;
import org.shanoir.ng.shared.exception.RestServiceException;
import org.shanoir.ng.shared.exception.ShanoirException;
import org.shanoir.ng.subject.dto.SimpleSubjectDTO;
import org.shanoir.ng.subject.dto.SubjectDTO;
import org.shanoir.ng.subject.dto.mapper.SubjectMapper;
import org.shanoir.ng.subject.model.Subject;
import org.shanoir.ng.subject.service.SubjectService;
import org.shanoir.ng.subject.service.SubjectUniqueConstraintManager;
import org.shanoir.ng.utils.KeycloakUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import io.swagger.annotations.ApiParam;

@Controller
public class SubjectApiController implements SubjectApi {

	@Autowired
	private SubjectMapper subjectMapper;

	@Autowired
	private SubjectService subjectService;

	@Autowired
	private SubjectUniqueConstraintManager uniqueConstraintManager;

	@Autowired
	private StudyBIDSService bidsService;

	@Autowired
	private ShanoirEventService eventService;

	@Override
	public ResponseEntity<Void> deleteSubject(
			@ApiParam(value = "id of the subject", required = true) @PathVariable("subjectId") Long subjectId) {
		try {
			bidsService.deleteSubjectBids(subjectId);
			// Delete all associated bids folders
			subjectService.deleteById(subjectId);
			eventService.publishEvent(new ShanoirEvent(ShanoirEventType.DELETE_SUBJECT_EVENT, subjectId.toString(), KeycloakUtil.getTokenUserId(), "", ShanoirEvent.SUCCESS));
			return new ResponseEntity<>(HttpStatus.NO_CONTENT);
		} catch (EntityNotFoundException e) {
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		}
	}

	@Override
	public ResponseEntity<SubjectDTO> findSubjectById(
			@ApiParam(value = "id of the subject", required = true) @PathVariable("subjectId") Long subjectId) {
		final Subject subject = subjectService.findById(subjectId);
		if (subject == null) {
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		}
		return new ResponseEntity<>(subjectMapper.subjectToSubjectDTO(subject), HttpStatus.OK);
	}

	@Override
	public ResponseEntity<List<SubjectDTO>> findSubjects() {
		final List<Subject> subjects = subjectService.findAll();
		if (subjects.isEmpty()) {
			return new ResponseEntity<>(HttpStatus.NO_CONTENT);
		}
		return new ResponseEntity<>(subjectMapper.subjectsToSubjectDTOs(subjects), HttpStatus.OK);
	}

	@Override
	public ResponseEntity<List<IdName>> findSubjectsNames() {
		final List<IdName> subjectsNames = subjectService.findNames();
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
			final BindingResult result) throws RestServiceException {
		validate(subject, result);
		Subject createdSubject;
		if (centerId == null) {
			createdSubject = subjectService.create(subject);
		} else {
			createdSubject = subjectService.createAutoIncrement(subject, centerId);
		}
		final SubjectDTO subjectDTO = subjectMapper.subjectToSubjectDTO(createdSubject);
		return new ResponseEntity<SubjectDTO>(subjectDTO, HttpStatus.OK);
	}

	// Attention: this method is used by ShanoirUploader!!!
	@Override
	public ResponseEntity<Void> updateSubject(
			@ApiParam(value = "id of the subject", required = true) @PathVariable("subjectId") Long subjectId,
			@ApiParam(value = "subject to update", required = true) @RequestBody Subject subject,
			final BindingResult result) throws RestServiceException, MicroServiceCommunicationException {
		validate(subject, result);
		try {
			subjectService.update(subject);

			eventService.publishEvent(new ShanoirEvent(ShanoirEventType.UPDATE_SUBJECT_EVENT, subject.getId().toString(), KeycloakUtil.getTokenUserId(), "", ShanoirEvent.SUCCESS));
			return new ResponseEntity<>(HttpStatus.NO_CONTENT);
		} catch (EntityNotFoundException e) {
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		} catch (ShanoirException e) {
			throw new RestServiceException(new ErrorModel(e.getErrorCode(), e.getMessage()));
		}
	}

	@Override
	public ResponseEntity<List<SimpleSubjectDTO>> findSubjectsByStudyId(
			@ApiParam(value = "id of the study", required = true) @PathVariable("studyId") Long studyId,
			@ApiParam(value="preclinical", required = false) @RequestParam(value="preclinical", required = false) String preclinical) {
		final List<SimpleSubjectDTO> simpleSubjectDTOList;
		if ("null".equals(preclinical)) {
			simpleSubjectDTOList = subjectService.findAllSubjectsOfStudy(studyId);
		} else {
			simpleSubjectDTOList = subjectService.findAllSubjectsOfStudyAndPreclinical(studyId, Boolean.parseBoolean(preclinical));
		}
		if (simpleSubjectDTOList.isEmpty()) {
			return new ResponseEntity<>(HttpStatus.NO_CONTENT);
		}
		simpleSubjectDTOList.sort(new Comparator<SimpleSubjectDTO>() {
			@Override
			public int compare(SimpleSubjectDTO o1, SimpleSubjectDTO o2) {
				return o1.getSubjectStudy().getSubjectStudyIdentifier().compareToIgnoreCase(o2.getSubjectStudy().getSubjectStudyIdentifier());
			}
		});
		return new ResponseEntity<>(simpleSubjectDTOList, HttpStatus.OK);
	}

	@Override
	public ResponseEntity<SubjectDTO> findSubjectByIdentifier(
			@ApiParam(value = "identifier of the subject", required = true) @PathVariable("subjectIdentifier") String subjectIdentifier) {
		final Subject subject = subjectService.findByIdentifier(subjectIdentifier);
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
}

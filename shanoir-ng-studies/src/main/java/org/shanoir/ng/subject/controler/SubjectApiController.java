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

import java.util.List;

import org.shanoir.ng.shared.core.model.IdName;
import org.shanoir.ng.shared.error.FieldErrorMap;
import org.shanoir.ng.shared.exception.EntityNotFoundException;
import org.shanoir.ng.shared.exception.ErrorDetails;
import org.shanoir.ng.shared.exception.ErrorModel;
import org.shanoir.ng.shared.exception.MicroServiceCommunicationException;
import org.shanoir.ng.shared.exception.RestServiceException;
import org.shanoir.ng.subject.dto.SimpleSubjectDTO;
import org.shanoir.ng.subject.dto.SubjectDTO;
import org.shanoir.ng.subject.dto.SubjectFromShupDTO;
import org.shanoir.ng.subject.dto.SubjectStudyCardIdDTO;
import org.shanoir.ng.subject.dto.mapper.SubjectMapper;
import org.shanoir.ng.subject.dto.mapper.SubjectMappingUtilsService;
import org.shanoir.ng.subject.model.Subject;
import org.shanoir.ng.subject.service.SubjectService;
import org.shanoir.ng.subject.service.SubjectUniqueConstraintManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;

import io.swagger.annotations.ApiParam;

@Controller
public class SubjectApiController implements SubjectApi {

	@Autowired
	private SubjectMapper subjectMapper;
	
	@Autowired
	private SubjectMappingUtilsService mappingUtils;

	@Autowired
	private SubjectService subjectService;
	
	@Autowired
	private SubjectUniqueConstraintManager uniqueConstraintManager;
	
	@Override
	public ResponseEntity<Void> deleteSubject(
			@ApiParam(value = "id of the subject", required = true) @PathVariable("subjectId") Long subjectId) {

		try {
			subjectService.deleteById(subjectId);
			return new ResponseEntity<Void>(HttpStatus.NO_CONTENT);
			
		} catch (EntityNotFoundException e) {
			return new ResponseEntity<Void>(HttpStatus.NOT_FOUND);
		}
	}

	@Override
	public ResponseEntity<SubjectDTO> findSubjectById(
			@ApiParam(value = "id of the subject", required = true) @PathVariable("subjectId") Long subjectId) {
		
		final Subject subject = subjectService.findById(subjectId);
		if (subject == null) {
			return new ResponseEntity<SubjectDTO>(HttpStatus.NOT_FOUND);
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

	@Override
	public ResponseEntity<SubjectDTO> saveNewSubject(
			@ApiParam(value = "subject to create", required = true) @RequestBody Subject subject,
			final BindingResult result) throws RestServiceException {
		
		validate(subject, result);

		Subject createdSubject;
		try {
			createdSubject = subjectService.create(subject);
		} catch (MicroServiceCommunicationException e) {
			throw new RestServiceException(
					new ErrorModel(HttpStatus.UNPROCESSABLE_ENTITY.value(), "Microservice communication error", null));
		}
		return new ResponseEntity<SubjectDTO>(subjectMapper.subjectToSubjectDTO(createdSubject), HttpStatus.OK);
	}

	@Override
	public ResponseEntity<Subject> saveNewOFSEPSubject(
			@ApiParam(value = "subject to create", required = true) @RequestBody SubjectStudyCardIdDTO subjectStudyCardIdDTO,
			final BindingResult result) throws RestServiceException {

		Long studyCardId = subjectStudyCardIdDTO.getStudyCardId();
		Subject subject = subjectStudyCardIdDTO.getSubject();
		
		validate(subject, result);
		
		String commonName;
		try {
			commonName = mappingUtils.getOfsepCommonName(studyCardId);
		} catch (MicroServiceCommunicationException e) {
			throw new RestServiceException(
					new ErrorModel(HttpStatus.INTERNAL_SERVER_ERROR.value(), "Communication error beetween microservices"));
		}
		if (commonName == null || commonName.equals(""))
			subject.setName("NoCommonName");
		else
			subject.setName(commonName);
		
		Subject createdSubject;
		try {
			createdSubject = subjectService.create(subject);
		} catch (MicroServiceCommunicationException e) {
			throw new RestServiceException(
					new ErrorModel(HttpStatus.UNPROCESSABLE_ENTITY.value(), "Microservice communication error", null));
		}
		return new ResponseEntity<Subject>(createdSubject, HttpStatus.OK);

	}

	@Override
	public ResponseEntity<Void> updateSubject(
			@ApiParam(value = "id of the subject", required = true) @PathVariable("subjectId") Long subjectId,
			@ApiParam(value = "subject to update", required = true) @RequestBody Subject subject,
			final BindingResult result) throws RestServiceException {

		validate(subject, result);
		try {
			subjectService.update(subject);
			return new ResponseEntity<Void>(HttpStatus.NO_CONTENT);
			
		} catch (EntityNotFoundException e) {
			return new ResponseEntity<Void>(HttpStatus.NOT_FOUND);
		} catch (MicroServiceCommunicationException e) {
			throw new RestServiceException(
					new ErrorModel(HttpStatus.UNPROCESSABLE_ENTITY.value(), "Microservice communication error", null));
		}

	}

	
	@Override
	public ResponseEntity<List<SimpleSubjectDTO>> findSubjectsByStudyId(
			@ApiParam(value = "id of the study", required = true) @PathVariable("studyId") Long studyId) {

		final List<SimpleSubjectDTO> simpleSubjectDTOList = subjectService.findAllSubjectsOfStudy(studyId);
		if (simpleSubjectDTOList.isEmpty()) {
			return new ResponseEntity<List<SimpleSubjectDTO>>(HttpStatus.NO_CONTENT);
		}
		return new ResponseEntity<List<SimpleSubjectDTO>>(simpleSubjectDTOList, HttpStatus.OK);

	}

	@Override
	public ResponseEntity<SubjectDTO> findSubjectByIdentifier(
			@ApiParam(value = "identifier of the subject", required = true) @PathVariable("subjectIdentifier") String subjectIdentifier) {

		final Subject subject = subjectService.findByIdentifier(subjectIdentifier);
		if (subject == null) {
			return new ResponseEntity<SubjectDTO>(HttpStatus.NO_CONTENT);
		}
		return new ResponseEntity<SubjectDTO>(subjectMapper.subjectToSubjectDTO(subject), HttpStatus.OK);

	}

	@Override
	public ResponseEntity<Long> saveNewOFSEPSubjectFromShup(@ApiParam(value = "subject to create and the id of the study card", required = true) @RequestBody SubjectFromShupDTO subjectFromShupDTO,
			final BindingResult result) throws RestServiceException {
		
		if (subjectService.findByIdentifier(subjectFromShupDTO.getIdentifier()) != null) {
			return new ResponseEntity<Long>(HttpStatus.FOUND);
		}
		
		Subject subject;
		try {
			subject = mappingUtils.toSubject(subjectFromShupDTO);
		} catch (MicroServiceCommunicationException e) {
			throw new RestServiceException(
					new ErrorModel(HttpStatus.INTERNAL_SERVER_ERROR.value(), "Communication error beetween microservices"));
		}
		
		Subject createdSubject;
		try {
			createdSubject = subjectService.create(subject);
		} catch (MicroServiceCommunicationException e) {
			throw new RestServiceException(
					new ErrorModel(HttpStatus.UNPROCESSABLE_ENTITY.value(), "Microservice communication error", null));
		}
		return new ResponseEntity<Long>(createdSubject.getId(), HttpStatus.OK);
	}

	@Override
	public ResponseEntity<Long> updateSubjectFromShup(
			@ApiParam(value = "id of the subject", required = true) @PathVariable("subjectId") Long subjectId,
			@ApiParam(value = "subject to update", required = true) @RequestBody SubjectFromShupDTO subjectFromShupDTO,
			final BindingResult result) throws RestServiceException {

		Subject subject = subjectService.findByIdWithSubjecStudies(subjectId);
		if (subject == null) {
			return new ResponseEntity<>(HttpStatus.NO_CONTENT);
		}
		
		try {
			subjectService.update(mappingUtils.updateSubjectValues(subject, subjectFromShupDTO));
			return new ResponseEntity<Long>(subject.getId(), HttpStatus.OK);
		} catch (EntityNotFoundException e) {
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		} catch (MicroServiceCommunicationException e) {
			throw new RestServiceException(
					new ErrorModel(HttpStatus.UNPROCESSABLE_ENTITY.value(), "Microservice communication error", null));
		}
	}
	
	
	private void validate(Subject subject, BindingResult result) throws RestServiceException {
		final FieldErrorMap errors = new FieldErrorMap()
				.add(new FieldErrorMap(result))
				.add(uniqueConstraintManager.validate(subject));
		if (!errors.isEmpty()) {
			ErrorModel error = new ErrorModel(HttpStatus.UNPROCESSABLE_ENTITY.value(), "Bad arguments", new ErrorDetails(errors));
			throw new RestServiceException(error);
		} 
	}
}

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

package org.shanoir.ng.subject;

import java.util.List;

import org.shanoir.ng.shared.dto.IdNameDTO;
import org.shanoir.ng.shared.error.FieldErrorMap;
import org.shanoir.ng.shared.exception.ErrorDetails;
import org.shanoir.ng.shared.exception.ErrorModel;
import org.shanoir.ng.shared.exception.RestServiceException;
import org.shanoir.ng.shared.exception.ShanoirStudiesException;
import org.shanoir.ng.shared.validation.EditableOnlyByValidator;
import org.shanoir.ng.shared.validation.UniqueValidator;
import org.shanoir.ng.subject.dto.SimpleSubjectDTO;
import org.shanoir.ng.subject.dto.SubjectDTO;
import org.shanoir.ng.subject.dto.SubjectFromShupDTO;
import org.shanoir.ng.subject.dto.SubjectStudyCardIdDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

	private static final Logger LOG = LoggerFactory.getLogger(SubjectApiController.class);

	@Autowired
	private SubjectMapper subjectMapper;
	
	@Autowired
	private SubjectService subjectService;
	
	@Override
	public ResponseEntity<Void> deleteSubject(
			@ApiParam(value = "id of the subject", required = true) @PathVariable("subjectId") Long subjectId) {
		if (subjectService.findById(subjectId) == null) {
			return new ResponseEntity<Void>(HttpStatus.NOT_FOUND);
		}
		try {
			subjectService.deleteById(subjectId);
		} catch (ShanoirStudiesException e) {
			return new ResponseEntity<Void>(HttpStatus.NOT_ACCEPTABLE);
		}
		return new ResponseEntity<Void>(HttpStatus.NO_CONTENT);
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
	public ResponseEntity<List<IdNameDTO>> findSubjectsNames() {
		final List<IdNameDTO> subjectsNames = subjectService.findIdsAndNames();
		if (subjectsNames.isEmpty()) {
			return new ResponseEntity<>(HttpStatus.NO_CONTENT);
		}
		return new ResponseEntity<>(subjectsNames, HttpStatus.OK);
	}

	@Override
	public ResponseEntity<SubjectDTO> saveNewSubject(
			@ApiParam(value = "subject to create", required = true) @RequestBody Subject subject,
			final BindingResult result) throws RestServiceException {
		
		/* Validation */
		// A basic template can only update certain fields, check that
		final FieldErrorMap accessErrors = this.getCreationRightsErrors(subject);
		// Check hibernate validation
		final FieldErrorMap hibernateErrors = new FieldErrorMap(result);
		// Check unique constrainte
		final FieldErrorMap uniqueErrors = this.getUniqueConstraintErrors(subject);
		/* Merge errors. */
		final FieldErrorMap errors = new FieldErrorMap(accessErrors, hibernateErrors, uniqueErrors);
		if (!errors.isEmpty()) {
			throw new RestServiceException(
					new ErrorModel(HttpStatus.UNPROCESSABLE_ENTITY.value(), "Bad arguments", new ErrorDetails(errors)));
		}

		try {
			final Subject createdSubject = subjectService.save(subject);
			subjectService.updateShanoirOld(createdSubject);
			return new ResponseEntity<SubjectDTO>(subjectMapper.subjectToSubjectDTO(createdSubject), HttpStatus.OK);
		} catch (ShanoirStudiesException e) {
			throw new RestServiceException(
					new ErrorModel(HttpStatus.UNPROCESSABLE_ENTITY.value(), "Bad arguments", null));
		}
	}

	@Override
	public ResponseEntity<Subject> saveNewOFSEPSubject(
			@ApiParam(value = "subject to create", required = true) @RequestBody SubjectStudyCardIdDTO subjectStudyCardIdDTO,
			final BindingResult result) throws RestServiceException {

		Long studyCardId = subjectStudyCardIdDTO.getStudyCardId();
		Subject subject = subjectStudyCardIdDTO.getSubject();
		/* Validation */
		// A basic template can only update certain fields, check that
		final FieldErrorMap accessErrors = this.getCreationRightsErrors(subject);
		// Check hibernate validation
		final FieldErrorMap hibernateErrors = new FieldErrorMap(result);
		// Check unique constrainte
		final FieldErrorMap uniqueErrors = this.getUniqueConstraintErrors(subject);
		/* Merge errors. */
		final FieldErrorMap errors = new FieldErrorMap(accessErrors, hibernateErrors, uniqueErrors);
		if (!errors.isEmpty()) {
			throw new RestServiceException(
					new ErrorModel(HttpStatus.UNPROCESSABLE_ENTITY.value(), "Bad arguments", new ErrorDetails(errors)));
		}

		try {
			final Subject createdSubject = subjectService.saveForOFSEP(subject, studyCardId);
			subjectService.updateShanoirOld(createdSubject);
			return new ResponseEntity<Subject>(createdSubject, HttpStatus.OK);
		} catch (ShanoirStudiesException e) {
			throw new RestServiceException(
					new ErrorModel(HttpStatus.UNPROCESSABLE_ENTITY.value(), "Bad arguments", null));
		}
	}

	@Override
	public ResponseEntity<Void> updateSubject(
			@ApiParam(value = "id of the subject", required = true) @PathVariable("subjectId") Long subjectId,
			@ApiParam(value = "subject to update", required = true) @RequestBody Subject subject,
			final BindingResult result) throws RestServiceException {

		// IMPORTANT : avoid any confusion that could lead to security breach
		subject.setId(subjectId);

		// A basic template can only update certain fields, check that
		final FieldErrorMap accessErrors = this.getUpdateRightsErrors(subject);
		// Check hibernate validation
		final FieldErrorMap hibernateErrors = new FieldErrorMap(result);
		// Check unique constrainte
		final FieldErrorMap uniqueErrors = this.getUniqueConstraintErrors(subject);
		/* Merge errors. */
		final FieldErrorMap errors = new FieldErrorMap(accessErrors, hibernateErrors, uniqueErrors);
		if (!errors.isEmpty()) {
			throw new RestServiceException(
					new ErrorModel(HttpStatus.UNPROCESSABLE_ENTITY.value(), "Bad arguments", new ErrorDetails(errors)));
		}

		/* Update template in db. */
		try {
			subjectService.update(subject);
		} catch (ShanoirStudiesException e) {
			LOG.error("Error while trying to update subject " + subjectId + " : ", e);
			throw new RestServiceException(
					new ErrorModel(HttpStatus.UNPROCESSABLE_ENTITY.value(), "Bad arguments", null));
		}

		return new ResponseEntity<Void>(HttpStatus.NO_CONTENT);
	}

	/*
	 * Get access rights errors.
	 *
	 * @param template template.
	 *
	 * @return an error map.
	 */
	private FieldErrorMap getUpdateRightsErrors(final Subject subject) {
		final Subject previousStateTemplate = subjectService.findById(subject.getId());
		final FieldErrorMap accessErrors = new EditableOnlyByValidator<Subject>().validate(previousStateTemplate,
				subject);
		return accessErrors;
	}

	/*
	 * Get access rights errors.
	 *
	 * @param template template.
	 *
	 * @return an error map.
	 */
	private FieldErrorMap getCreationRightsErrors(final Subject subject) {
		return new EditableOnlyByValidator<Subject>().validate(subject);
	}

	/*
	 * Get unique constraint errors
	 *
	 * @param template
	 *
	 * @return an error map
	 */
	private FieldErrorMap getUniqueConstraintErrors(final Subject subject) {
		final UniqueValidator<Subject> uniqueValidator = new UniqueValidator<Subject>(subjectService);
		final FieldErrorMap uniqueErrors = uniqueValidator.validate(subject);
		return uniqueErrors;
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
		Subject subject = subjectService.findByIdentifier(subjectFromShupDTO.getIdentifier());
		if (subject != null) {
			return new ResponseEntity<Long>(HttpStatus.FOUND);
		}
		
		try {
			final Subject createdSubject = subjectService.saveForOFSEP(subjectFromShupDTO);
			LOG.warn("Subject service completed");
			subjectService.updateShanoirOld(createdSubject);
			return new ResponseEntity<Long>(createdSubject.getId(), HttpStatus.OK);
		} catch (ShanoirStudiesException e) {
			throw new RestServiceException(
					new ErrorModel(HttpStatus.UNPROCESSABLE_ENTITY.value(), "Bad arguments", null));
		}
	}

	@Override
	public ResponseEntity<Long> updateSubjectFromShup(
			@ApiParam(value = "id of the subject", required = true) @PathVariable("subjectId") Long subjectId,
			@ApiParam(value = "subject to update", required = true) @RequestBody SubjectFromShupDTO subjectFromShupDTO,
			final BindingResult result) throws RestServiceException {

		// IMPORTANT : avoid any confusion that could lead to security breach
	
		Subject subject = subjectService.findByIdWithSubjecStudies(subjectId);
		if (subject == null) {
			return new ResponseEntity<Long>(HttpStatus.NO_CONTENT);
		}

		// A basic template can only update certain fields, check that
		final FieldErrorMap accessErrors = this.getUpdateRightsErrors(subject);
		// Check hibernate validation
		final FieldErrorMap hibernateErrors = new FieldErrorMap(result);
		// Check unique constrainte
		final FieldErrorMap uniqueErrors = this.getUniqueConstraintErrors(subject);
		/* Merge errors. */
		final FieldErrorMap errors = new FieldErrorMap(accessErrors, hibernateErrors, uniqueErrors);
		if (!errors.isEmpty()) {
			throw new RestServiceException(
					new ErrorModel(HttpStatus.UNPROCESSABLE_ENTITY.value(), "Bad arguments", new ErrorDetails(errors)));
		}

		/* Update template in db. */
		try {
			subjectService.update(subject,subjectFromShupDTO);
			return new ResponseEntity<Long>(subject.getId(), HttpStatus.OK);
		} catch (ShanoirStudiesException e) {
			LOG.error("Error while trying to update subject " + subjectId + " : ", e);
			throw new RestServiceException(
					new ErrorModel(HttpStatus.UNPROCESSABLE_ENTITY.value(), "Bad arguments", null));
		}


	}
}

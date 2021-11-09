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

package org.shanoir.ng.preclinical.subjects;

import java.util.List;

import javax.validation.Valid;

import org.shanoir.ng.preclinical.pathologies.subject_pathologies.SubjectPathologyService;
import org.shanoir.ng.preclinical.references.RefsService;
import org.shanoir.ng.preclinical.therapies.subject_therapies.SubjectTherapyService;
import org.shanoir.ng.shared.error.FieldErrorMap;
import org.shanoir.ng.shared.event.ShanoirEvent;
import org.shanoir.ng.shared.event.ShanoirEventService;
import org.shanoir.ng.shared.event.ShanoirEventType;
import org.shanoir.ng.shared.exception.ErrorDetails;
import org.shanoir.ng.shared.exception.ErrorModel;
import org.shanoir.ng.shared.exception.RestServiceException;
import org.shanoir.ng.shared.exception.ShanoirException;
import org.shanoir.ng.shared.validation.RefValueExistsValidator;
import org.shanoir.ng.utils.KeycloakUtil;
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
public class AnimalSubjectApiController implements AnimalSubjectApi {

	private static final String BAD_ARGUMENTS = "Bad arguments";

	/**
	 * Logger
	 */
	private static final Logger LOG = LoggerFactory.getLogger(AnimalSubjectApiController.class);

	@Autowired
	private AnimalSubjectService subjectService;
	@Autowired
	private RefsService refsService;
	@Autowired
	private SubjectPathologyService subjectPathologyService;
	@Autowired
	private SubjectTherapyService subjectTherapyService;
	@Autowired
	private ShanoirEventService eventService;

	@Autowired
	private AnimalSubjectUniqueValidator uniqueValidator;

	@Autowired
	private AnimalSubjectEditableByManager editableOnlyValidator;

	@Override
	public ResponseEntity<AnimalSubject> createAnimalSubject(
			@ApiParam(value = "AnimalSubject object to add", required = true) @RequestBody @Valid final AnimalSubject animalSubject,
			final BindingResult result) throws RestServiceException {

		/* Validation */
		// A basic user can only update certain fields, check that
		final FieldErrorMap accessErrors = this.getCreationRightsErrors(animalSubject);
		// Check hibernate validation
		final FieldErrorMap hibernateErrors = new FieldErrorMap(result);
		// Check unique constraint
		final FieldErrorMap uniqueErrors = this.getUniqueConstraintErrors(animalSubject);
		// Check if given reference values exist
		final FieldErrorMap refValuesExistsErrors = this.checkRefsValueExists(animalSubject);

		/* Merge errors. */
		final FieldErrorMap errors = new FieldErrorMap(accessErrors, hibernateErrors, uniqueErrors,
				refValuesExistsErrors);
		if (!errors.isEmpty()) {
			LOG.error("ERROR while creating AnimalSubject - error in fields :{}", errors.size());
			throw new RestServiceException(
					new ErrorModel(HttpStatus.UNPROCESSABLE_ENTITY.value(), BAD_ARGUMENTS, new ErrorDetails(errors)));
		}
		// Guarantees it is a creation, not an update
		animalSubject.setId(null);

		try {
			final AnimalSubject createdSubject = subjectService.save(animalSubject);
			eventService.publishEvent(new ShanoirEvent(ShanoirEventType.CREATE_PRECLINICAL_SUBJECT_EVENT, createdSubject.getId().toString(), KeycloakUtil.getTokenUserId(), "", ShanoirEvent.SUCCESS));
			return new ResponseEntity<>(createdSubject, HttpStatus.OK);
		} catch (ShanoirException e) {
			throw new RestServiceException(e,
					new ErrorModel(HttpStatus.UNPROCESSABLE_ENTITY.value(), BAD_ARGUMENTS, null));
		}
	}

	@Override
	public ResponseEntity<Void> deleteAnimalSubject(
			@ApiParam(value = "AnimalSubject id to delete", required = true) @PathVariable("id") Long id) {
		if (subjectService.findById(id) == null) {
			LOG.error("ERROR animalSubject not found while deleting {}", id);
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		}
		try {
			AnimalSubject animalSubject = subjectService.findById(id);
			if (animalSubject == null) {
				LOG.error("ERROR animalSubject not found while deleting {}", id);
				return new ResponseEntity<>(HttpStatus.NOT_FOUND);
			} else {
				subjectPathologyService.deleteByAnimalSubject(animalSubject);
				subjectTherapyService.deleteByAnimalSubject(animalSubject);
			}
			subjectService.deleteById(id);
			eventService.publishEvent(new ShanoirEvent(ShanoirEventType.DELETE_PRECLINICAL_SUBJECT_EVENT, id.toString(), KeycloakUtil.getTokenUserId(), "", ShanoirEvent.SUCCESS));
		} catch (ShanoirException e) {
			LOG.error("ERROR while deleting animal subject " + id, e);
			return new ResponseEntity<>(HttpStatus.NOT_ACCEPTABLE);
		}
		return new ResponseEntity<>(HttpStatus.OK);
	}

	@Override
	public ResponseEntity<AnimalSubject> getAnimalSubjectById(
			@ApiParam(value = "ID of animalSubject that needs to be fetched", required = true) @PathVariable("id") Long id) {
		final AnimalSubject subject = subjectService.findById(id);
		if (subject == null) {
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		}
		return new ResponseEntity<>(subject, HttpStatus.OK);
	}

	@Override
	public ResponseEntity<List<AnimalSubject>> getAnimalSubjects() {
		LOG.info("PRECLINICAL getAnimalSubjects");
		final List<AnimalSubject> subjects = subjectService.findAll();
		if (subjects.isEmpty()) {
			return new ResponseEntity<>(HttpStatus.NO_CONTENT);
		}
		return new ResponseEntity<>(subjects, HttpStatus.OK);
	}

	@Override
	public ResponseEntity<Void> updateAnimalSubject(
			@ApiParam(value = "ID of animalSubject that needs to be updated", required = true) @PathVariable("id") Long id,
			@ApiParam(value = "Subject object that needs to be updated", required = true) @RequestBody AnimalSubject animalSubject,
			final BindingResult result) throws RestServiceException {

		// IMPORTANT : avoid any confusion that could lead to security breach
		animalSubject.setId(id);

		// A basic template can only update certain fields, check that
		final FieldErrorMap accessErrors = this.getUpdateRightsErrors(animalSubject);
		// Check hibernate validation
		final FieldErrorMap hibernateErrors = new FieldErrorMap(result);
		// Check unique constrainte
		final FieldErrorMap uniqueErrors = this.getUniqueConstraintErrors(animalSubject);
		/* Merge errors. */
		final FieldErrorMap errors = new FieldErrorMap(accessErrors, hibernateErrors, uniqueErrors);
		if (!errors.isEmpty()) {
			throw new RestServiceException(
					new ErrorModel(HttpStatus.UNPROCESSABLE_ENTITY.value(), BAD_ARGUMENTS, new ErrorDetails(errors)));
		}

		/* Update template in db. */
		try {
			subjectService.update(animalSubject);
			eventService.publishEvent(new ShanoirEvent(ShanoirEventType.UPDATE_PRECLINICAL_SUBJECT_EVENT, id.toString(), KeycloakUtil.getTokenUserId(), "", ShanoirEvent.SUCCESS));
		} catch (ShanoirException e) {
			LOG.error("Error while trying to update subject " + id + " : ", e);
			throw new RestServiceException(e,
					new ErrorModel(HttpStatus.UNPROCESSABLE_ENTITY.value(), BAD_ARGUMENTS, null));
		}

		return new ResponseEntity<>(HttpStatus.OK);
	}

	@Override
	public ResponseEntity<AnimalSubject> getAnimalSubjectBySubjectId(
			@ApiParam(value = "ID of subject that needs to be fetched", required = true) @PathVariable("id") Long id) {
		final List<AnimalSubject> subjects = subjectService.findBySubjectId(id);
		if (subjects == null || subjects.isEmpty()) {
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		}
		return new ResponseEntity<>(subjects.get(0), HttpStatus.OK);
	}

	private FieldErrorMap getUpdateRightsErrors(final AnimalSubject subject) {
	    return editableOnlyValidator.validate(subject);
	}

	private FieldErrorMap getCreationRightsErrors(final AnimalSubject subject) {
	    return editableOnlyValidator.validate(subject);
	}

	private FieldErrorMap getUniqueConstraintErrors(final AnimalSubject subject) {
		return uniqueValidator.validate(subject);
	}

	private FieldErrorMap checkRefsValueExists(final AnimalSubject subject) {
		return new RefValueExistsValidator<AnimalSubject>(refsService).validate(subject);
	}
}

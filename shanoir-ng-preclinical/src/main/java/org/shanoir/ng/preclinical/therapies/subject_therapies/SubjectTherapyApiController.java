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

package org.shanoir.ng.preclinical.therapies.subject_therapies;

import java.util.List;

import org.shanoir.ng.preclinical.subjects.AnimalSubject;
import org.shanoir.ng.preclinical.subjects.AnimalSubjectService;
import org.shanoir.ng.preclinical.therapies.Therapy;
import org.shanoir.ng.preclinical.therapies.TherapyService;
import org.shanoir.ng.shared.error.FieldErrorMap;
import org.shanoir.ng.shared.exception.ErrorDetails;
import org.shanoir.ng.shared.exception.ErrorModel;
import org.shanoir.ng.shared.exception.RestServiceException;
import org.shanoir.ng.shared.exception.ShanoirException;
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
public class SubjectTherapyApiController implements SubjectTherapyApi {

	private static final String ANIMAL_SUBJECT_NOT_FOUND = "AnimalSubject not found";

	private static final String BAD_ARGUMENTS = "Bad arguments";

	private static final Logger LOG = LoggerFactory.getLogger(SubjectTherapyApiController.class);

	@Autowired
	private SubjectTherapyService subtherapiesService;
	@Autowired
	private AnimalSubjectService subjectService;
	@Autowired
	private TherapyService therapyService;
	@Autowired
	private SubjectTherapyUniqueValidator uniqueValidator;
	@Autowired
	private SubjectTherapyEditableByManager editableOnlyValidator;

	@Override
	public ResponseEntity<SubjectTherapy> addSubjectTherapy(
			@ApiParam(value = "subject id", required = true) @PathVariable("id") Long id,
			@ApiParam(value = "therapy to add to subject", required = true) @RequestBody SubjectTherapy subtherapy,
			BindingResult result) throws RestServiceException {

		// First check if given user exists
		AnimalSubject animalSubject = subjectService.findById(id);
		if (animalSubject == null) {
			throw new RestServiceException(
					new ErrorModel(HttpStatus.NOT_FOUND.value(), ANIMAL_SUBJECT_NOT_FOUND, new ErrorDetails()));
		} else {
			final FieldErrorMap accessErrors = this.getCreationRightsErrors(subtherapy);
			final FieldErrorMap hibernateErrors = new FieldErrorMap(result);
			final FieldErrorMap uniqueErrors = this.getUniqueConstraintErrors(subtherapy);
			/* Merge errors. */
			final FieldErrorMap errors = new FieldErrorMap(accessErrors, hibernateErrors, uniqueErrors);
			if (!errors.isEmpty()) {
				throw new RestServiceException(new ErrorModel(HttpStatus.UNPROCESSABLE_ENTITY.value(), BAD_ARGUMENTS,
						new ErrorDetails(errors)));
			}

			// Guarantees it is a creation, not an update
			subtherapy.setId(null);
			// Just in case
			try {
				subtherapy.setAnimalSubject(animalSubject);
			} catch (Exception e) {
				LOG.error("Error while parsing subject id for Long cast " + e.getMessage(), e);
			}

			/* Save subject therapy in db. */
			try {
				final SubjectTherapy createdSubTherapy = subtherapiesService.save(subtherapy);
				return new ResponseEntity<>(createdSubTherapy, HttpStatus.OK);
			} catch (ShanoirException e) {
				throw new RestServiceException(e,
						new ErrorModel(HttpStatus.UNPROCESSABLE_ENTITY.value(), BAD_ARGUMENTS, null));
			}
		}

	}

	@Override
	public ResponseEntity<Void> deleteSubjectTherapy(
			@ApiParam(value = "Subject id", required = true) @PathVariable("id") Long id,
			@ApiParam(value = "subject therapy id to delete", required = true) @PathVariable("tid") Long tid)
			throws RestServiceException {

		// First check if given user exists
		AnimalSubject animalSubject = subjectService.findById(id);
		if (animalSubject == null) {
			throw new RestServiceException(
					new ErrorModel(HttpStatus.NOT_FOUND.value(), ANIMAL_SUBJECT_NOT_FOUND, new ErrorDetails()));
		} else {
			SubjectTherapy toDelete = subtherapiesService.findById(tid);
			if (toDelete == null) {
				return new ResponseEntity<>(HttpStatus.NOT_FOUND);
			}
			try {
				subtherapiesService.deleteById(toDelete.getId());
			} catch (ShanoirException e) {
				return new ResponseEntity<>(HttpStatus.NOT_ACCEPTABLE);
			}
			return new ResponseEntity<>(HttpStatus.OK);
		}

	}

	@Override
	public ResponseEntity<Void> deleteSubjectTherapies(
			@ApiParam(value = "animal subject id", required = true) @PathVariable("id") Long id)
			throws RestServiceException {
		AnimalSubject animalSubject = subjectService.findById(id);
		if (animalSubject == null) {
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		} else {
			try {
				subtherapiesService.deleteByAnimalSubject(animalSubject);
			} catch (ShanoirException e) {
				LOG.error("ERROR while deleting therapies for subject " + animalSubject.getId(), e);
				return new ResponseEntity<>(HttpStatus.NOT_ACCEPTABLE);
			}
			return new ResponseEntity<>(HttpStatus.OK);
		}
	}

	@Override
	public ResponseEntity<SubjectTherapy> getSubjectTherapyById(
			@ApiParam(value = "subject id", required = true) @PathVariable("id") Long id,
			@ApiParam(value = "ID of subject therapy that needs to be fetched", required = true) @PathVariable("tid") Long tid)
			throws RestServiceException {
		AnimalSubject animalSubject = subjectService.findById(id);
		if (animalSubject == null) {
			throw new RestServiceException(
					new ErrorModel(HttpStatus.NOT_FOUND.value(), ANIMAL_SUBJECT_NOT_FOUND, new ErrorDetails()));
		} else {
			final SubjectTherapy subtherapy = subtherapiesService.findById(tid);
			if (subtherapy == null) {
				return new ResponseEntity<>(HttpStatus.NOT_FOUND);
			}
			return new ResponseEntity<>(subtherapy, HttpStatus.OK);
		}
	}

	@Override
	public ResponseEntity<List<SubjectTherapy>> getSubjectTherapies(
			@ApiParam(value = "subject id", required = true) @PathVariable("id") Long id) throws RestServiceException {
		AnimalSubject animalSubject = subjectService.findById(id);
		if (animalSubject == null) {
			throw new RestServiceException(
					new ErrorModel(HttpStatus.NOT_FOUND.value(), "Subject not found", new ErrorDetails()));
		} else {
			final List<SubjectTherapy> subtherapies = subtherapiesService.findAllByAnimalSubject(animalSubject);
			return new ResponseEntity<>(subtherapies, HttpStatus.OK);
		}
	}

	@Override
	public ResponseEntity<List<SubjectTherapy>> getSubjectTherapiesByTherapy(
			@ApiParam(value = "therapy id", required = true) @PathVariable("tid") Long tid)
			throws RestServiceException {
		Therapy therapy = therapyService.findById(tid);
		if (therapy == null) {
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		} else {
			final List<SubjectTherapy> subtherapies = subtherapiesService.findAllByTherapy(therapy);
			if (subtherapies.isEmpty()) {
				return new ResponseEntity<>(HttpStatus.NO_CONTENT);
			}
			return new ResponseEntity<>(subtherapies, HttpStatus.OK);
		}
	}

	@Override
	public ResponseEntity<Void> updateSubjectTherapy(
			@ApiParam(value = "subject id", required = true) @PathVariable("id") Long id,
			@ApiParam(value = "ID of subject therapy that needs to be updated", required = true) @PathVariable("tid") Long tid,
			@ApiParam(value = "Subject therapy object that needs to be updated", required = true) @RequestBody SubjectTherapy subtherapy,
			final BindingResult result) throws RestServiceException {

		subtherapy.setId(tid);
		final FieldErrorMap accessErrors = this.getUpdateRightsErrors(subtherapy);
		final FieldErrorMap hibernateErrors = new FieldErrorMap(result);
		final FieldErrorMap uniqueErrors = this.getUniqueConstraintErrors(subtherapy);
		/* Merge errors. */
		final FieldErrorMap errors = new FieldErrorMap(accessErrors, hibernateErrors, uniqueErrors);
		if (!errors.isEmpty()) {
			throw new RestServiceException(
					new ErrorModel(HttpStatus.UNPROCESSABLE_ENTITY.value(), BAD_ARGUMENTS, new ErrorDetails(errors)));
		}

		try {
			subtherapiesService.update(subtherapy);
		} catch (ShanoirException e) {
			LOG.error("Error while trying to update subject therapy " + tid + " : ", e);
			throw new RestServiceException(e,
					new ErrorModel(HttpStatus.UNPROCESSABLE_ENTITY.value(), BAD_ARGUMENTS, null));
		}
		return new ResponseEntity<>(HttpStatus.OK);
	}

	private FieldErrorMap getUpdateRightsErrors(final SubjectTherapy subtherapy) {
	    return editableOnlyValidator.validate(subtherapy);
	}

	private FieldErrorMap getCreationRightsErrors(final SubjectTherapy subtherapy) {
	    return editableOnlyValidator.validate(subtherapy);
	}

	private FieldErrorMap getUniqueConstraintErrors(final SubjectTherapy therapies) {
		return uniqueValidator.validate(therapies);
	}

}

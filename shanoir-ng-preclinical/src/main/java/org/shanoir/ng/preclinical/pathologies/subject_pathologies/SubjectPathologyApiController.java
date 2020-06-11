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

package org.shanoir.ng.preclinical.pathologies.subject_pathologies;

import java.util.List;

import org.shanoir.ng.preclinical.pathologies.Pathology;
import org.shanoir.ng.preclinical.pathologies.PathologyService;
import org.shanoir.ng.preclinical.pathologies.pathology_models.PathologyModel;
import org.shanoir.ng.preclinical.pathologies.pathology_models.PathologyModelService;
import org.shanoir.ng.preclinical.subjects.AnimalSubject;
import org.shanoir.ng.preclinical.subjects.AnimalSubjectService;
import org.shanoir.ng.shared.error.FieldErrorMap;
import org.shanoir.ng.shared.exception.ErrorDetails;
import org.shanoir.ng.shared.exception.ErrorModel;
import org.shanoir.ng.shared.exception.RestServiceException;
import org.shanoir.ng.shared.exception.ShanoirException;
import org.shanoir.ng.shared.validation.EditableOnlyByValidator;
import org.shanoir.ng.shared.validation.UniqueValidator;
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

@javax.annotation.Generated(value = "io.swagger.codegen.languages.SpringCodegen", date = "2017-03-23T14:51:04.625Z")

@Controller
public class SubjectPathologyApiController implements SubjectPathologyApi {

	private static final Logger LOG = LoggerFactory.getLogger(SubjectPathologyApiController.class);

	@Autowired
	private SubjectPathologyService pathosService;
	@Autowired
	private PathologyModelService pathosModelService;
	@Autowired
	private AnimalSubjectService subjectService;
	@Autowired
	private PathologyService pathologyService;

	@Override
	public ResponseEntity<SubjectPathology> addSubjectPathology(
			@ApiParam(value = "subject id", required = true) @PathVariable("id") Long id,
			@ApiParam(value = "pathology to add to subject", required = true) @RequestBody SubjectPathology pathos,
			BindingResult result) throws RestServiceException {

		// First check if given user exists
		AnimalSubject animalSubject = subjectService.findById(id);
		if (animalSubject == null) {
			throw new RestServiceException(
					new ErrorModel(HttpStatus.NOT_FOUND.value(), "animalSubject not found", new ErrorDetails()));
		} else {
			final FieldErrorMap accessErrors = this.getCreationRightsErrors(pathos);
			final FieldErrorMap hibernateErrors = new FieldErrorMap(result);
			final FieldErrorMap uniqueErrors = this.getUniqueConstraintErrors(pathos);
			/* Merge errors. */
			final FieldErrorMap errors = new FieldErrorMap(accessErrors, hibernateErrors, uniqueErrors);
			if (!errors.isEmpty()) {
				throw new RestServiceException(new ErrorModel(HttpStatus.UNPROCESSABLE_ENTITY.value(), "Bad arguments",
						new ErrorDetails(errors)));
			}

			// Guarantees it is a creation, not an update
			pathos.setId(null);

			// Just in case
			try {
				pathos.setAnimalSubject(animalSubject);
			} catch (Exception e) {
				LOG.error("Error while parsing subject id for Long cast " + e.getMessage(), e);
			}

			/* Save pathos in db. */
			try {
				final SubjectPathology createdPathos = pathosService.save(pathos);
				return new ResponseEntity<SubjectPathology>(createdPathos, HttpStatus.OK);
			} catch (ShanoirException e) {
				throw new RestServiceException(e,
						new ErrorModel(HttpStatus.UNPROCESSABLE_ENTITY.value(), "Bad arguments", null));
			}
		}

	}

	@Override
	public ResponseEntity<Void> deleteSubjectPathology(
			@ApiParam(value = "Animal Subject id", required = true) @PathVariable("id") Long id,
			@ApiParam(value = "pathology id to delete", required = true) @PathVariable("pid") Long pid)
			throws RestServiceException {

		// First check if given subject exists
		if (subjectService.findById(id) == null) {
			throw new RestServiceException(
					new ErrorModel(HttpStatus.NOT_FOUND.value(), "Subject not found", new ErrorDetails()));
		} else {
			if (pathosService.findById(pid) == null) {
				return new ResponseEntity<Void>(HttpStatus.NOT_FOUND);
			}
			try {
				pathosService.deleteById(pid);
			} catch (ShanoirException e) {
				return new ResponseEntity<Void>(HttpStatus.NOT_ACCEPTABLE);
			}
			return new ResponseEntity<Void>(HttpStatus.OK);
		}

	}

	@Override
	public ResponseEntity<Void> deleteSubjectPathologies(
			@ApiParam(value = "animal subject id", required = true) @PathVariable("id") Long id)
			throws RestServiceException {
		AnimalSubject animalSubject = subjectService.findById(id);
		if (animalSubject == null) {
			return new ResponseEntity<Void>(HttpStatus.NOT_FOUND);
		} else {
			try {
				pathosService.deleteByAnimalSubject(animalSubject);
			} catch (ShanoirException e) {
				LOG.error("ERROR while deleting pathologies for subject " + animalSubject.getId(), e);
				return new ResponseEntity<Void>(HttpStatus.NOT_ACCEPTABLE);
			}
			return new ResponseEntity<Void>(HttpStatus.OK);
		}
	}

	@Override
	public ResponseEntity<SubjectPathology> getSubjectPathologyById(
			@ApiParam(value = "subject id", required = true) @PathVariable("id") Long id,
			@ApiParam(value = "ID of subject pathology that needs to be fetched", required = true) @PathVariable("pid") Long pid)
			throws RestServiceException {
		if (subjectService.findById(id) == null) {
			throw new RestServiceException(
					new ErrorModel(HttpStatus.NOT_FOUND.value(), "Subject not found", new ErrorDetails()));
		} else {
			final SubjectPathology pathos = pathosService.findById(pid);
			if (pathos == null) {
				return new ResponseEntity<SubjectPathology>(HttpStatus.NOT_FOUND);
			}
			return new ResponseEntity<SubjectPathology>(pathos, HttpStatus.OK);
		}
	}

	@Override
	public ResponseEntity<List<SubjectPathology>> getSubjectPathologies(
			@ApiParam(value = "animalSubject id", required = true) @PathVariable("id") Long id)
			throws RestServiceException {
		AnimalSubject animalSubject = subjectService.findById(id);
		if (animalSubject == null) {
			return new ResponseEntity<List<SubjectPathology>>(HttpStatus.NOT_FOUND);
		} else {
			final List<SubjectPathology> pathos = pathosService.findByAnimalSubject(animalSubject);
			return new ResponseEntity<List<SubjectPathology>>(pathos, HttpStatus.OK);
		}
	}

	@Override
	public ResponseEntity<List<SubjectPathology>> getSubjectPathologiesByPathology(
			@ApiParam(value = "pathology id", required = true) @PathVariable("pid") Long pid)
			throws RestServiceException {
		Pathology pathology = pathologyService.findById(pid);
		if (pathology == null) {
			return new ResponseEntity<List<SubjectPathology>>(HttpStatus.NOT_FOUND);
		} else {
			final List<SubjectPathology> pathos = pathosService.findAllByPathology(pathology);
			if (pathos.isEmpty()) {
				return new ResponseEntity<List<SubjectPathology>>(HttpStatus.NO_CONTENT);
			}
			return new ResponseEntity<List<SubjectPathology>>(pathos, HttpStatus.OK);
		}
	}

	@Override
	public ResponseEntity<List<SubjectPathology>> getSubjectPathologiesByPathologyModel(
			@ApiParam(value = "pathology model id", required = true) @PathVariable("pathoModelId") Long pathoModelId) {
		PathologyModel patMod = pathosModelService.findById(pathoModelId);
		List<SubjectPathology> subPathology = pathosService.findBy("pathologyModel", patMod);
		if (subPathology == null || subPathology.isEmpty()) {
			return new ResponseEntity<List<SubjectPathology>>(HttpStatus.NO_CONTENT);
		} else {
			return new ResponseEntity<List<SubjectPathology>>(subPathology, HttpStatus.OK);
		}
	}

	@Override
	public ResponseEntity<Void> updateSubjectPathology(
			@ApiParam(value = "subject id", required = true) @PathVariable("id") Long id,
			@ApiParam(value = "ID of subject pathology that needs to be updated", required = true) @PathVariable("pid") Long pid,
			@ApiParam(value = "Subject pathology object that needs to be updated", required = true) @RequestBody SubjectPathology pathos,
			final BindingResult result) throws RestServiceException {

		AnimalSubject animalSubject = subjectService.findById(id);
		if (animalSubject == null) {
			throw new RestServiceException(
					new ErrorModel(HttpStatus.NOT_FOUND.value(), "animalSubject not found", new ErrorDetails()));
		} else {
			pathos.setId(pid);

			final FieldErrorMap accessErrors = this.getUpdateRightsErrors(pathos);
			final FieldErrorMap hibernateErrors = new FieldErrorMap(result);
			final FieldErrorMap uniqueErrors = this.getUniqueConstraintErrors(pathos);
			/* Merge errors. */
			final FieldErrorMap errors = new FieldErrorMap(accessErrors, hibernateErrors, uniqueErrors);
			if (!errors.isEmpty()) {
				throw new RestServiceException(new ErrorModel(HttpStatus.UNPROCESSABLE_ENTITY.value(), "Bad arguments",
						new ErrorDetails(errors)));
			}
			try {
				pathosService.update(pathos);
			} catch (ShanoirException e) {
				LOG.error("Error while trying to update subject pathology " + pid + " : ", e);
				throw new RestServiceException(e,
						new ErrorModel(HttpStatus.UNPROCESSABLE_ENTITY.value(), "Bad arguments", null));
			}
			return new ResponseEntity<Void>(HttpStatus.OK);
		}
	}

	private FieldErrorMap getUpdateRightsErrors(final SubjectPathology pathos) {
		final SubjectPathology previousStatePathos = pathosService.findById(pathos.getId());
		final FieldErrorMap accessErrors = new EditableOnlyByValidator<SubjectPathology>().validate(previousStatePathos,
				pathos);
		return accessErrors;
	}

	private FieldErrorMap getCreationRightsErrors(final SubjectPathology pathos) {
		return new EditableOnlyByValidator<SubjectPathology>().validate(pathos);
	}

	private FieldErrorMap getUniqueConstraintErrors(final SubjectPathology pathos) {
		final UniqueValidator<SubjectPathology> uniqueValidator = new UniqueValidator<SubjectPathology>(pathosService);
		final FieldErrorMap uniqueErrors = uniqueValidator.validate(pathos);
		return uniqueErrors;
	}

}

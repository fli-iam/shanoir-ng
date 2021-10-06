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

package org.shanoir.ng.preclinical.anesthetics.examination_anesthetics;

import java.util.List;

import org.shanoir.ng.preclinical.anesthetics.anesthetic.Anesthetic;
import org.shanoir.ng.preclinical.anesthetics.anesthetic.AnestheticService;
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
public class ExaminationAnestheticApiController implements ExaminationAnestheticApi {

	private static final String BAD_ARGUMENTS = "Bad arguments";

	private static final Logger LOG = LoggerFactory.getLogger(ExaminationAnestheticApiController.class);

	@Autowired
	private ExaminationAnestheticService examAnestheticsService;

	@Autowired
	private AnestheticService anestheticsService;
	@Autowired
	private ExaminationAnestheticUniqueValidator uniqueValidator;
	
	@Autowired
	private ExaminationAnestheticEditableByManager editableOnlyValidator;


	@Override
	public ResponseEntity<ExaminationAnesthetic> addExaminationAnesthetic(
			@ApiParam(value = "examination id", required = true) @PathVariable("id") Long id,
			@ApiParam(value = "anesthetic to add to examination", required = true) @RequestBody ExaminationAnesthetic examAnesthetic,
			BindingResult result) throws RestServiceException {

		final FieldErrorMap accessErrors = this.getCreationRightsErrors(examAnesthetic);
		final FieldErrorMap hibernateErrors = new FieldErrorMap(result);
		final FieldErrorMap uniqueErrors = this.getUniqueConstraintErrors(examAnesthetic);
		/* Merge errors. */
		final FieldErrorMap errors = new FieldErrorMap(accessErrors, hibernateErrors, uniqueErrors);
		if (!errors.isEmpty()) {
			throw new RestServiceException(
					new ErrorModel(HttpStatus.UNPROCESSABLE_ENTITY.value(), BAD_ARGUMENTS, new ErrorDetails(errors)));
		}

		// Guarantees it is a creation, not an update
		examAnesthetic.setId(null);
		// Set the examination id
		examAnesthetic.setExaminationId(id);

		/* Save examination anesthetic in db. */
		try {
			final ExaminationAnesthetic createdExamAnesthetic = examAnestheticsService.save(examAnesthetic);
			return new ResponseEntity<>(createdExamAnesthetic, HttpStatus.OK);
		} catch (ShanoirException e) {
			throw new RestServiceException(e,
					new ErrorModel(HttpStatus.UNPROCESSABLE_ENTITY.value(), BAD_ARGUMENTS, null));
		}
	}

	@Override
	public ResponseEntity<Void> deleteExaminationAnesthetic(
			@ApiParam(value = "Examination id", required = true) @PathVariable("id") Long id,
			@ApiParam(value = "Examination anesthetic id to delete", required = true) @PathVariable("eaid") Long eaid)
			throws RestServiceException {

		if (examAnestheticsService.findById(eaid) == null) {
			return new ResponseEntity<>(HttpStatus.NO_CONTENT);
		}
		try {
			examAnestheticsService.deleteById(eaid);
		} catch (ShanoirException e) {
			return new ResponseEntity<>(HttpStatus.NOT_ACCEPTABLE);
		}
		return new ResponseEntity<>(HttpStatus.OK);

	}

	@Override
	public ResponseEntity<ExaminationAnesthetic> getExaminationAnestheticById(
			@ApiParam(value = "examination id", required = true) @PathVariable("id") Long id,
			@ApiParam(value = "ID of examination id that needs to be fetched", required = true) @PathVariable("eaid") Long eaid)
			throws RestServiceException {
		final ExaminationAnesthetic examAnesthetic = examAnestheticsService.findById(eaid);
		if (examAnesthetic == null) {
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		}
		return new ResponseEntity<>(examAnesthetic, HttpStatus.OK);
	}

	@Override
	public ResponseEntity<List<ExaminationAnesthetic>> getExaminationAnesthetics(
			@ApiParam(value = "examination id", required = true) @PathVariable("id") Long id)
			throws RestServiceException {
		final List<ExaminationAnesthetic> examAnesthetics = examAnestheticsService.findByExaminationId(id);
		return new ResponseEntity<>(examAnesthetics, HttpStatus.OK);
	}

	@Override
	public ResponseEntity<Void> updateExaminationAnesthetic(
			@ApiParam(value = "examination id", required = true) @PathVariable("id") Long id,
			@ApiParam(value = "ID of examination anesthetic that needs to be updated", required = true) @PathVariable("eaid") Long eaid,
			@ApiParam(value = "Examination anesthetic object that needs to be updated", required = true) @RequestBody ExaminationAnesthetic examAnesthetic,
			final BindingResult result) throws RestServiceException {

		examAnesthetic.setId(eaid);
		final FieldErrorMap accessErrors = this.getUpdateRightsErrors(examAnesthetic);
		final FieldErrorMap hibernateErrors = new FieldErrorMap(result);
		final FieldErrorMap uniqueErrors = this.getUniqueConstraintErrors(examAnesthetic);
		/* Merge errors. */
		final FieldErrorMap errors = new FieldErrorMap(accessErrors, hibernateErrors, uniqueErrors);
		if (!errors.isEmpty()) {
			throw new RestServiceException(
					new ErrorModel(HttpStatus.UNPROCESSABLE_ENTITY.value(), BAD_ARGUMENTS, new ErrorDetails(errors)));
		}

		try {
			examAnestheticsService.update(examAnesthetic);
		} catch (ShanoirException e) {
			LOG.error("Error while trying to update examination anesthetic " + eaid + " : ", e);
			throw new RestServiceException(e,
					new ErrorModel(HttpStatus.UNPROCESSABLE_ENTITY.value(), BAD_ARGUMENTS, null));
		}
		return new ResponseEntity<>(HttpStatus.OK);
	}

	@Override
	public ResponseEntity<List<ExaminationAnesthetic>> getExaminationAnestheticsByAnesthetic(
			@ApiParam(value = "anesthetic id", required = true) @PathVariable("id") Long id)
			throws RestServiceException {
		Anesthetic anesthetic = anestheticsService.findById(id);
		if (anesthetic == null) {
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		} else {
			final List<ExaminationAnesthetic> subexaminations = examAnestheticsService.findByAnesthetic(anesthetic);
			if (subexaminations.isEmpty()) {
				return new ResponseEntity<>(HttpStatus.NO_CONTENT);
			}
			return new ResponseEntity<>(subexaminations, HttpStatus.OK);
		}
	}

	private FieldErrorMap getUpdateRightsErrors(final ExaminationAnesthetic examAnesthetic) {
	    return editableOnlyValidator.validate(examAnesthetic);
	}

	private FieldErrorMap getCreationRightsErrors(final ExaminationAnesthetic examAnesthetics) {
	    return editableOnlyValidator.validate(examAnesthetics);
	}

	private FieldErrorMap getUniqueConstraintErrors(final ExaminationAnesthetic examAnesthetic) {
		return uniqueValidator.validate(examAnesthetic);
	}

}

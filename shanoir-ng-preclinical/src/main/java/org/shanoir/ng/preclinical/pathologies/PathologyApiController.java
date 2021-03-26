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

package org.shanoir.ng.preclinical.pathologies;

import java.util.List;

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

@Controller
public class PathologyApiController implements PathologyApi {

	private static final String BAD_ARGUMENTS = "Bad arguments";

	private static final Logger LOG = LoggerFactory.getLogger(PathologyApiController.class);

	@Autowired
	private PathologyService pathologiesService;

	@Override
	public ResponseEntity<Pathology> createPathology(
			@ApiParam(value = "pathology to create", required = true) @RequestBody Pathology pathology,
			BindingResult result) throws RestServiceException {

		final FieldErrorMap accessErrors = this.getCreationRightsErrors(pathology);
		final FieldErrorMap hibernateErrors = new FieldErrorMap(result);
		final FieldErrorMap uniqueErrors = this.getUniqueConstraintErrors(pathology);
		/* Merge errors. */
		final FieldErrorMap errors = new FieldErrorMap(accessErrors, hibernateErrors, uniqueErrors);
		if (!errors.isEmpty()) {
			throw new RestServiceException(
					new ErrorModel(HttpStatus.UNPROCESSABLE_ENTITY.value(), BAD_ARGUMENTS, new ErrorDetails(errors)));
		}

		// Guarantees it is a creation, not an update
		pathology.setId(null);

		/* Save pathology in db. */
		try {
			final Pathology createdPathology = pathologiesService.save(pathology);
			return new ResponseEntity<>(createdPathology, HttpStatus.OK);
		} catch (ShanoirException e) {
			throw new RestServiceException(e,
					new ErrorModel(HttpStatus.UNPROCESSABLE_ENTITY.value(), BAD_ARGUMENTS, null));
		}

	}

	@Override
	public ResponseEntity<Void> deletePathology(
			@ApiParam(value = "Pathology id to delete", required = true) @PathVariable("id") Long id) {
		if (pathologiesService.findById(id) == null) {
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		}
		try {
			pathologiesService.deleteById(id);
		} catch (ShanoirException e) {
			return new ResponseEntity<>(HttpStatus.NOT_ACCEPTABLE);
		}
		return new ResponseEntity<>(HttpStatus.OK);
	}

	@Override
	public ResponseEntity<Pathology> getPathologyById(
			@ApiParam(value = "ID of subject that needs to be fetched", required = true) @PathVariable("id") Long id) {
		final Pathology pathology = pathologiesService.findById(id);
		if (pathology == null) {
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		}
		return new ResponseEntity<>(pathology, HttpStatus.OK);
	}

	@Override
	public ResponseEntity<List<Pathology>> getPathologies() {
		final List<Pathology> pathologies = pathologiesService.findAll();
		if (pathologies.isEmpty()) {
			return new ResponseEntity<>(HttpStatus.NO_CONTENT);
		}
		return new ResponseEntity<>(pathologies, HttpStatus.OK);
	}

	@Override
	public ResponseEntity<Void> updatePathology(
			@ApiParam(value = "ID of subject that needs to be updated", required = true) @PathVariable("id") Long id,
			@ApiParam(value = "Pathology object that needs to be updated", required = true) @RequestBody Pathology pathology,
			final BindingResult result) throws RestServiceException {

		pathology.setId(id);

		final FieldErrorMap accessErrors = this.getUpdateRightsErrors(pathology);
		final FieldErrorMap hibernateErrors = new FieldErrorMap(result);
		final FieldErrorMap uniqueErrors = this.getUniqueConstraintErrors(pathology);
		/* Merge errors. */
		final FieldErrorMap errors = new FieldErrorMap(accessErrors, hibernateErrors, uniqueErrors);
		if (!errors.isEmpty()) {
			throw new RestServiceException(
					new ErrorModel(HttpStatus.UNPROCESSABLE_ENTITY.value(), BAD_ARGUMENTS, new ErrorDetails(errors)));
		}
		try {
			pathologiesService.update(pathology);
		} catch (ShanoirException e) {
			LOG.error("Error while trying to update pathology " + id + " : ", e);
			throw new RestServiceException(e,
					new ErrorModel(HttpStatus.UNPROCESSABLE_ENTITY.value(), BAD_ARGUMENTS, null));
		}
		return new ResponseEntity<>(HttpStatus.OK);

	}

	private FieldErrorMap getUpdateRightsErrors(final Pathology pathology) {
		final Pathology previousStatePathology = pathologiesService.findById(pathology.getId());
		return new EditableOnlyByValidator<Pathology>().validate(previousStatePathology,
				pathology);
	}

	private FieldErrorMap getCreationRightsErrors(final Pathology pathology) {
		return new EditableOnlyByValidator<Pathology>().validate(pathology);
	}

	private FieldErrorMap getUniqueConstraintErrors(final Pathology pathology) {
		final UniqueValidator<Pathology> uniqueValidator = new UniqueValidator<>(pathologiesService);
		return uniqueValidator.validate(pathology);
	}

}

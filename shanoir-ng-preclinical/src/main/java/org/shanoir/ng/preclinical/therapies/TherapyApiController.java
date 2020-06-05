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

package org.shanoir.ng.preclinical.therapies;

import java.util.List;

import org.shanoir.ng.shared.error.FieldErrorMap;
import org.shanoir.ng.shared.event.ShanoirEvent;
import org.shanoir.ng.shared.event.ShanoirEventService;
import org.shanoir.ng.shared.event.ShanoirEventType;
import org.shanoir.ng.shared.exception.ErrorDetails;
import org.shanoir.ng.shared.exception.ErrorModel;
import org.shanoir.ng.shared.exception.RestServiceException;
import org.shanoir.ng.shared.exception.ShanoirException;
import org.shanoir.ng.shared.validation.EditableOnlyByValidator;
import org.shanoir.ng.shared.validation.UniqueValidator;
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
public class TherapyApiController implements TherapyApi {

	private static final String BAD_ARGUMENTS = "Bad arguments";

	private static final Logger LOG = LoggerFactory.getLogger(TherapyApiController.class);

	@Autowired
	private TherapyService therapiesService;

	@Autowired
	private ShanoirEventService eventService;
	
	@Override
	public ResponseEntity<Therapy> createTherapy(
			@ApiParam(value = "therapy to create", required = true) @RequestBody Therapy therapy, BindingResult result)
			throws RestServiceException {

		final FieldErrorMap accessErrors = this.getCreationRightsErrors(therapy);
		final FieldErrorMap hibernateErrors = new FieldErrorMap(result);
		final FieldErrorMap uniqueErrors = this.getUniqueConstraintErrors(therapy);
		/* Merge errors. */
		final FieldErrorMap errors = new FieldErrorMap(accessErrors, hibernateErrors, uniqueErrors);
		if (!errors.isEmpty()) {
			throw new RestServiceException(
					new ErrorModel(HttpStatus.UNPROCESSABLE_ENTITY.value(), BAD_ARGUMENTS, new ErrorDetails(errors)));
		}

		// Guarantees it is a creation, not an update
		therapy.setId(null);

		/* Save therapy in db. */
		try {
			final Therapy createdTherapy = therapiesService.save(therapy);
			eventService.publishEvent(new ShanoirEvent(ShanoirEventType.CREATE_THERAPY_EVENT, createdTherapy.getId().toString(), KeycloakUtil.getTokenUserId(), "", ShanoirEvent.SUCCESS));
			return new ResponseEntity<>(createdTherapy, HttpStatus.OK);
		} catch (ShanoirException e) {
			throw new RestServiceException(e,
					new ErrorModel(HttpStatus.UNPROCESSABLE_ENTITY.value(), BAD_ARGUMENTS, null));
		}

	}

	@Override
	public ResponseEntity<Void> deleteTherapy(
			@ApiParam(value = "Therapy id to delete", required = true) @PathVariable("id") Long id) {
		if (therapiesService.findById(id) == null) {
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		}
		try {
			therapiesService.deleteById(id);
			eventService.publishEvent(new ShanoirEvent(ShanoirEventType.DELETE_THERAPY_EVENT, id.toString(), KeycloakUtil.getTokenUserId(), "", ShanoirEvent.SUCCESS));
		} catch (ShanoirException e) {
			return new ResponseEntity<>(HttpStatus.NOT_ACCEPTABLE);
		}
		return new ResponseEntity<>(HttpStatus.OK);
	}

	@Override
	public ResponseEntity<Therapy> getTherapyById(
			@ApiParam(value = "ID of therapy that needs to be fetched", required = true) @PathVariable("id") Long id) {
		final Therapy therapy = therapiesService.findById(id);
		if (therapy == null) {
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		}
		return new ResponseEntity<>(therapy, HttpStatus.OK);
	}

	@Override
	public ResponseEntity<List<Therapy>> getTherapyByType(
			@ApiParam(value = "Type of therapies that needs to be fetched", required = true) @PathVariable("type") String type)
			throws RestServiceException {
		try {
			final List<Therapy> therapies = therapiesService.findByTherapyType(TherapyType.valueOf(type.toUpperCase()));
			if (therapies.isEmpty()) {
				return new ResponseEntity<>(HttpStatus.NO_CONTENT);
			}
			return new ResponseEntity<>(therapies, HttpStatus.OK);
		} catch (Exception e) {
			return new ResponseEntity<>(HttpStatus.NO_CONTENT);
		}
	}

	@Override
	public ResponseEntity<List<Therapy>> getTherapies() {
		final List<Therapy> therapies = therapiesService.findAll();
		if (therapies.isEmpty()) {
			return new ResponseEntity<>(HttpStatus.NO_CONTENT);
		}
		return new ResponseEntity<>(therapies, HttpStatus.OK);
	}

	@Override
	public ResponseEntity<Void> updateTherapy(
			@ApiParam(value = "ID of therapy that needs to be updated", required = true) @PathVariable("id") Long id,
			@ApiParam(value = "Therapy object that needs to be updated", required = true) @RequestBody Therapy therapy,
			final BindingResult result) throws RestServiceException {

		therapy.setId(id);

		final FieldErrorMap accessErrors = this.getUpdateRightsErrors(therapy);
		final FieldErrorMap hibernateErrors = new FieldErrorMap(result);
		final FieldErrorMap uniqueErrors = this.getUniqueConstraintErrors(therapy);
		/* Merge errors. */
		final FieldErrorMap errors = new FieldErrorMap(accessErrors, hibernateErrors, uniqueErrors);
		if (!errors.isEmpty()) {
			throw new RestServiceException(
					new ErrorModel(HttpStatus.UNPROCESSABLE_ENTITY.value(), BAD_ARGUMENTS, new ErrorDetails(errors)));
		}

		try {
			therapiesService.update(therapy);
			eventService.publishEvent(new ShanoirEvent(ShanoirEventType.UPDATE_THERAPY_EVENT, id.toString(), KeycloakUtil.getTokenUserId(), "", ShanoirEvent.SUCCESS));
		} catch (ShanoirException e) {
			LOG.error("Error while trying to update therapy " + id + " : ", e);
			throw new RestServiceException(e,
					new ErrorModel(HttpStatus.UNPROCESSABLE_ENTITY.value(), BAD_ARGUMENTS, null));
		}
		return new ResponseEntity<>(HttpStatus.OK);
	}

	private FieldErrorMap getUpdateRightsErrors(final Therapy therapy) {
		final Therapy previousStateTherapy = therapiesService.findById(therapy.getId());
		return new EditableOnlyByValidator<Therapy>().validate(previousStateTherapy,
				therapy);
	}

	private FieldErrorMap getCreationRightsErrors(final Therapy therapy) {
		return new EditableOnlyByValidator<Therapy>().validate(therapy);
	}

	private FieldErrorMap getUniqueConstraintErrors(final Therapy therapy) {
		final UniqueValidator<Therapy> uniqueValidator = new UniqueValidator<>(therapiesService);
		return uniqueValidator.validate(therapy);
	}

}

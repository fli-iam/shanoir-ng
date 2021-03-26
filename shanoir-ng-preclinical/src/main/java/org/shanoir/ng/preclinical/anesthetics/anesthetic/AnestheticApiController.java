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

package org.shanoir.ng.preclinical.anesthetics.anesthetic;

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
public class AnestheticApiController implements AnestheticApi {

	private static final String BAD_ARGUMENTS = "Bad arguments";

	private static final Logger LOG = LoggerFactory.getLogger(AnestheticApiController.class);

	@Autowired
	private AnestheticService anestheticsService;

	@Autowired
	private ShanoirEventService eventService;

	@Override
	public ResponseEntity<Anesthetic> createAnesthetic(
			@ApiParam(value = "Anesthetic to create", required = true) @RequestBody Anesthetic anesthetic,
			BindingResult result) throws RestServiceException {

		final FieldErrorMap accessErrors = this.getCreationRightsErrors(anesthetic);
		final FieldErrorMap hibernateErrors = new FieldErrorMap(result);
		final FieldErrorMap uniqueErrors = this.getUniqueConstraintErrors(anesthetic);
		/* Merge errors. */
		final FieldErrorMap errors = new FieldErrorMap(accessErrors, hibernateErrors, uniqueErrors);
		if (!errors.isEmpty()) {
			throw new RestServiceException(
					new ErrorModel(HttpStatus.UNPROCESSABLE_ENTITY.value(), BAD_ARGUMENTS, new ErrorDetails(errors)));
		}

		// Guarantees it is a creation, not an update
		anesthetic.setId(null);

		/* Save anesthetic in db. */
		try {
			final Anesthetic createdAnesthetic = anestheticsService.save(anesthetic);
			eventService.publishEvent(new ShanoirEvent(ShanoirEventType.CREATE_ANESTHETIC_EVENT, createdAnesthetic.getId().toString(), KeycloakUtil.getTokenUserId(), "", ShanoirEvent.SUCCESS));
			return new ResponseEntity<>(createdAnesthetic, HttpStatus.OK);
		} catch (ShanoirException e) {
			throw new RestServiceException(e,
					new ErrorModel(HttpStatus.UNPROCESSABLE_ENTITY.value(), BAD_ARGUMENTS, null));
		}

	}

	@Override
	public ResponseEntity<Void> deleteAnesthetic(
			@ApiParam(value = "Anesthetic id to delete", required = true) @PathVariable("id") Long id) {
		if (anestheticsService.findById(id) == null) {
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		}
		try {
			anestheticsService.deleteById(id);
			eventService.publishEvent(new ShanoirEvent(ShanoirEventType.DELETE_ANESTHETIC_EVENT, id.toString(), KeycloakUtil.getTokenUserId(), "", ShanoirEvent.SUCCESS));
		} catch (ShanoirException e) {
			return new ResponseEntity<>(HttpStatus.NOT_ACCEPTABLE);
		}
		return new ResponseEntity<>(HttpStatus.OK);
	}

	@Override
	public ResponseEntity<Anesthetic> getAnestheticById(
			@ApiParam(value = "ID of anesthetic that needs to be fetched", required = true) @PathVariable("id") Long id) {
		final Anesthetic anesthetic = anestheticsService.findById(id);
		if (anesthetic == null) {
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		}
		return new ResponseEntity<>(anesthetic, HttpStatus.OK);
	}

	@Override
	public ResponseEntity<List<Anesthetic>> getAnesthetics() {
		final List<Anesthetic> anesthetics = anestheticsService.findAll();
		if (anesthetics.isEmpty()) {
			return new ResponseEntity<>(HttpStatus.NO_CONTENT);
		}
		return new ResponseEntity<>(anesthetics, HttpStatus.OK);
	}

	@Override
	public ResponseEntity<List<Anesthetic>> getAnestheticsByType(
			@ApiParam(value = "Anesthetic type ", required = true) @PathVariable("type") String type) {
		try {
			final List<Anesthetic> anesthetics = anestheticsService
					.findAllByAnestheticType(AnestheticType.valueOf(type.toUpperCase()));

			if (anesthetics.isEmpty()) {
				return new ResponseEntity<>(HttpStatus.NO_CONTENT);
			}
			return new ResponseEntity<>(anesthetics, HttpStatus.OK);
		} catch (IllegalArgumentException e) {
			return new ResponseEntity<>(HttpStatus.NO_CONTENT);
		}
	}

	@Override
	public ResponseEntity<Void> updateAnesthetic(
			@ApiParam(value = "ID of anesthetic that needs to be updated", required = true) @PathVariable("id") Long id,
			@ApiParam(value = "Anesthetic object that needs to be updated", required = true) @RequestBody Anesthetic anesthetic,
			final BindingResult result) throws RestServiceException {

		anesthetic.setId(id);
		final FieldErrorMap accessErrors = this.getUpdateRightsErrors(anesthetic);
		final FieldErrorMap hibernateErrors = new FieldErrorMap(result);
		final FieldErrorMap uniqueErrors = this.getUniqueConstraintErrors(anesthetic);
		/* Merge errors. */
		final FieldErrorMap errors = new FieldErrorMap(accessErrors, hibernateErrors, uniqueErrors);
		if (!errors.isEmpty()) {
			throw new RestServiceException(
					new ErrorModel(HttpStatus.UNPROCESSABLE_ENTITY.value(), BAD_ARGUMENTS, new ErrorDetails(errors)));
		}

		try {
			anestheticsService.update(anesthetic);
			eventService.publishEvent(new ShanoirEvent(ShanoirEventType.UPDATE_ANESTHETIC_EVENT, id.toString(), KeycloakUtil.getTokenUserId(), "", ShanoirEvent.SUCCESS));
		} catch (ShanoirException e) {
			LOG.error("Error while trying to update anesthetic " + id + " : ", e);
			throw new RestServiceException(e,
					new ErrorModel(HttpStatus.UNPROCESSABLE_ENTITY.value(), BAD_ARGUMENTS, null));
		}
		return new ResponseEntity<>(HttpStatus.OK);
	}

	private FieldErrorMap getUpdateRightsErrors(final Anesthetic anesthetic) {
		final Anesthetic previousStateAnesthetic = anestheticsService.findById(anesthetic.getId());
		return new EditableOnlyByValidator<Anesthetic>().validate(previousStateAnesthetic,
				anesthetic);
	}

	private FieldErrorMap getCreationRightsErrors(final Anesthetic anesthetic) {
		return new EditableOnlyByValidator<Anesthetic>().validate(anesthetic);
	}

	private FieldErrorMap getUniqueConstraintErrors(final Anesthetic anesthetic) {
		final UniqueValidator<Anesthetic> uniqueValidator = new UniqueValidator<>(anestheticsService);
		return uniqueValidator.validate(anesthetic);
	}

}

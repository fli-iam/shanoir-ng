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

package org.shanoir.ng.coil.controler;

import java.util.List;

import javax.validation.Valid;

import org.shanoir.ng.coil.dto.CoilDTO;
import org.shanoir.ng.coil.dto.mapper.CoilMapper;
import org.shanoir.ng.coil.model.Coil;
import org.shanoir.ng.coil.service.CoilService;
import org.shanoir.ng.shared.error.FieldErrorMap;
import org.shanoir.ng.shared.event.ShanoirEvent;
import org.shanoir.ng.shared.event.ShanoirEventService;
import org.shanoir.ng.shared.event.ShanoirEventType;
import org.shanoir.ng.shared.exception.EntityNotFoundException;
import org.shanoir.ng.shared.exception.ErrorDetails;
import org.shanoir.ng.shared.exception.ErrorModel;
import org.shanoir.ng.shared.exception.RestServiceException;
import org.shanoir.ng.utils.KeycloakUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;

import io.swagger.annotations.ApiParam;

@Controller
public class CoilApiController implements CoilApi {

	@Autowired
	private CoilMapper coilMapper;

	@Autowired
	private CoilService coilService;

	@Autowired
	private ShanoirEventService eventService;

	@Override
	public ResponseEntity<Void> deleteCoil(
			@ApiParam(value = "id of the coil", required = true) @PathVariable("coilId") Long coilId)
			throws RestServiceException {

		try {
			coilService.deleteById(coilId);
			eventService.publishEvent(new ShanoirEvent(ShanoirEventType.DELETE_COIL_EVENT, coilId.toString(), KeycloakUtil.getTokenUserId(), "", ShanoirEvent.SUCCESS));
			return new ResponseEntity<>(HttpStatus.NO_CONTENT);
			
		} catch (EntityNotFoundException e) {
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		}
	}

	@Override
	public ResponseEntity<CoilDTO> findCoilById(
			@ApiParam(value = "id of the coil", required = true) @PathVariable("coilId") Long coilId) {
		
		final Coil coil = coilService.findById(coilId);
		if (coil == null) {
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		}
		return new ResponseEntity<>(coilMapper.coilToCoilDTO(coil), HttpStatus.OK);
	}

	@Override
	public ResponseEntity<List<CoilDTO>> findCoils() {
		final List<Coil> coils = coilService.findAll();
		if (coils.isEmpty()) {
			return new ResponseEntity<>(HttpStatus.NO_CONTENT);
		}
		return new ResponseEntity<>(coilMapper.coilsToCoilDTOs(coils), HttpStatus.OK);
	}

	@Override
	public ResponseEntity<CoilDTO> saveNewCoil(
			@ApiParam(value = "coil to create", required = true) @Valid @RequestBody Coil coil,
			final BindingResult result) throws RestServiceException {
		
		/* Validation */
		validate(result);

		/* Save coil in db. */
		final Coil createdCoil = coilService.create(coil);
		eventService.publishEvent(new ShanoirEvent(ShanoirEventType.CREATE_COIL_EVENT, createdCoil.getId().toString(), KeycloakUtil.getTokenUserId(), "", ShanoirEvent.SUCCESS));
		return new ResponseEntity<>(coilMapper.coilToCoilDTO(createdCoil), HttpStatus.OK);
	}

	@Override
	public ResponseEntity<Void> updateCoil(
			@ApiParam(value = "id of the coil", required = true) @PathVariable("coilId") Long coilId,
			@ApiParam(value = "coil to update", required = true) @Valid @RequestBody Coil coil,
			final BindingResult result) throws RestServiceException {

		validate(result);
		try {
			coilService.update(coil);
			eventService.publishEvent(new ShanoirEvent(ShanoirEventType.UPDATE_COIL_EVENT, coilId.toString(), KeycloakUtil.getTokenUserId(), "", ShanoirEvent.SUCCESS));
			return new ResponseEntity<>(HttpStatus.NO_CONTENT);
		
		} catch (EntityNotFoundException e) {
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		}
	}
	
	
	private void validate(BindingResult result) throws RestServiceException {
		final FieldErrorMap errors = new FieldErrorMap(result);
		if (!errors.isEmpty()) {
			ErrorModel error = new ErrorModel(HttpStatus.UNPROCESSABLE_ENTITY.value(), "Bad arguments", new ErrorDetails(errors));
			throw new RestServiceException(error);
		}
	}
}

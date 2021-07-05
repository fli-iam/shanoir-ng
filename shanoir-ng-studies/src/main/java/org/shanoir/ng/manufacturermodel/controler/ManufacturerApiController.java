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

package org.shanoir.ng.manufacturermodel.controler;

import java.util.List;
import java.util.Optional;

import javax.validation.Valid;

import org.shanoir.ng.manufacturermodel.model.Manufacturer;
import org.shanoir.ng.manufacturermodel.service.ManufacturerService;
import org.shanoir.ng.manufacturermodel.service.ManufacturerUniqueConstraintManager;
import org.shanoir.ng.shared.error.FieldErrorMap;
import org.shanoir.ng.shared.exception.EntityNotFoundException;
import org.shanoir.ng.shared.exception.ErrorDetails;
import org.shanoir.ng.shared.exception.ErrorModel;
import org.shanoir.ng.shared.exception.RestServiceException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;

@Controller
public class ManufacturerApiController implements ManufacturerApi {

	@Autowired
	private ManufacturerService manufacturerService;
	
	@Autowired
	private ManufacturerUniqueConstraintManager uniqueConstraintManager;

	@Override
	public ResponseEntity<Manufacturer> findManufacturerById(@PathVariable("manufacturerId") final Long manufacturerId) {
		final Optional<Manufacturer> manufacturerOpt = manufacturerService.findById(manufacturerId);
		if (manufacturerOpt.isEmpty()) {
			return new ResponseEntity<>(HttpStatus.NO_CONTENT);
		}
		return new ResponseEntity<>(manufacturerOpt.orElseThrow(), HttpStatus.OK);
	}

	@Override
	public ResponseEntity<List<Manufacturer>> findManufacturers() {
		final List<Manufacturer> manufacturers = manufacturerService.findAll();
		if (manufacturers.isEmpty()) {
			return new ResponseEntity<>(HttpStatus.NO_CONTENT);
		}
		return new ResponseEntity<>(manufacturers, HttpStatus.OK);
	}

	@Override
	public ResponseEntity<Manufacturer> saveNewManufacturer(@RequestBody final Manufacturer manufacturer,
			final BindingResult result) throws RestServiceException {
		
		validate(manufacturer, result);
		return new ResponseEntity<>(manufacturerService.create(manufacturer), HttpStatus.OK);
	}

	@Override
	public ResponseEntity<Void> updateManufacturer(@PathVariable("manufacturerId") final Long manufacturerId,
			@RequestBody @Valid final Manufacturer manufacturer, final BindingResult result) throws RestServiceException {
		
		validate(manufacturer, result);
		try {			
			/* Update user in db. */
			manufacturerService.update(manufacturer);
			return new ResponseEntity<>(HttpStatus.NO_CONTENT);
			
		} catch (EntityNotFoundException e) {
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		}
	}

	@Override
	public ResponseEntity<Void> deleteManufacturer(Long manufacturerId) throws RestServiceException {
		try {
			manufacturerService.deleteById(manufacturerId);
			return new ResponseEntity<>(HttpStatus.NO_CONTENT);
			
		} catch (EntityNotFoundException e) {
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		}
	}
	
	
	private void validate(Manufacturer manufacturer, BindingResult result) throws RestServiceException {
		final FieldErrorMap errors = new FieldErrorMap()
				.add(new FieldErrorMap(result))
				.add(uniqueConstraintManager.validate(manufacturer));
		if (!errors.isEmpty()) {
			ErrorModel error = new ErrorModel(HttpStatus.UNPROCESSABLE_ENTITY.value(), "Bad arguments", new ErrorDetails(errors));
			throw new RestServiceException(error);
		} 
	}
	
}

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

package org.shanoir.ng.manufacturermodel;

import java.util.List;

import javax.validation.Valid;

import org.shanoir.ng.shared.error.FieldErrorMap;
import org.shanoir.ng.shared.exception.ErrorDetails;
import org.shanoir.ng.shared.exception.ErrorModel;
import org.shanoir.ng.shared.exception.StudiesErrorModelCode;
import org.shanoir.ng.shared.exception.RestServiceException;
import org.shanoir.ng.shared.exception.ShanoirStudiesException;
import org.shanoir.ng.shared.validation.UniqueValidator;
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

	public ResponseEntity<Manufacturer> findManufacturerById(
			@PathVariable("manufacturerId") final Long manufacturerId) {
		final Manufacturer manufacturer = manufacturerService.findById(manufacturerId);
		if (manufacturer == null) {
			return new ResponseEntity<>(HttpStatus.NO_CONTENT);
		}
		return new ResponseEntity<>(manufacturer, HttpStatus.OK);
	}

	public ResponseEntity<List<Manufacturer>> findManufacturers() {
		final List<Manufacturer> manufacturers = manufacturerService.findAll();
		if (manufacturers.isEmpty()) {
			return new ResponseEntity<>(HttpStatus.NO_CONTENT);
		}
		return new ResponseEntity<>(manufacturers, HttpStatus.OK);
	}

	public ResponseEntity<Manufacturer> saveNewManufacturer(@RequestBody final Manufacturer manufacturer,
			final BindingResult result) throws RestServiceException {
		/* Validation */
		// Check hibernate validation
		final FieldErrorMap hibernateErrors = new FieldErrorMap(result);
		// Check unique constrainte
		final FieldErrorMap uniqueErrors = this.getUniqueConstraintErrors(manufacturer);
		/* Merge errors. */
		final FieldErrorMap errors = new FieldErrorMap(hibernateErrors, uniqueErrors);
		if (!errors.isEmpty()) {
			throw new RestServiceException(
					new ErrorModel(HttpStatus.UNPROCESSABLE_ENTITY.value(), "Bad arguments", new ErrorDetails(errors)));
		}

		// Guarantees it is a creation, not an update
		manufacturer.setId(null);

		/* Save center in db. */
		try {
			return new ResponseEntity<>(manufacturerService.save(manufacturer), HttpStatus.OK);
		} catch (ShanoirStudiesException e) {
			throw new RestServiceException(
					new ErrorModel(HttpStatus.UNPROCESSABLE_ENTITY.value(), "Bad arguments", null));
		}
	}

	public ResponseEntity<Void> updateManufacturer(@PathVariable("manufacturerId") final Long manufacturerId,
			@RequestBody @Valid final Manufacturer manufacturer, final BindingResult result) throws RestServiceException {
		manufacturer.setId(manufacturerId);

		// Check hibernate validation
		final FieldErrorMap hibernateErrors = new FieldErrorMap(result);
		// Check unique constrainte
		final FieldErrorMap uniqueErrors = this.getUniqueConstraintErrors(manufacturer);
		/* Merge errors. */
		final FieldErrorMap errors = new FieldErrorMap(hibernateErrors, uniqueErrors);
		if (!errors.isEmpty()) {
			throw new RestServiceException(
					new ErrorModel(HttpStatus.UNPROCESSABLE_ENTITY.value(), "Bad arguments", new ErrorDetails(errors)));
		}

		/* Update user in db. */
		try {
			manufacturerService.update(manufacturer);
		} catch (final ShanoirStudiesException e) {
			if (StudiesErrorModelCode.MANUFACTURER_NOT_FOUND.equals(e.getErrorCode())) {
				return new ResponseEntity<>(HttpStatus.NOT_FOUND);
			}
			throw new RestServiceException(
					new ErrorModel(HttpStatus.UNPROCESSABLE_ENTITY.value(), "Bad arguments", null));
		}

		return new ResponseEntity<>(HttpStatus.NO_CONTENT);
	}

	/*
	 * Get unique constraint errors.
	 *
	 * @param manufacturer manufacturer.
	 * 
	 * @return an error map.
	 */
	private FieldErrorMap getUniqueConstraintErrors(final Manufacturer manufacturer) {
		final UniqueValidator<Manufacturer> uniqueValidator = new UniqueValidator<Manufacturer>(manufacturerService);
		final FieldErrorMap uniqueErrors = uniqueValidator.validate(manufacturer);
		return uniqueErrors;
	}

}

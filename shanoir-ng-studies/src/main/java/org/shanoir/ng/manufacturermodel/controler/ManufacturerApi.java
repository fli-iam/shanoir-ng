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

import org.shanoir.ng.manufacturermodel.model.Manufacturer;
import org.shanoir.ng.shared.exception.ErrorModel;
import org.shanoir.ng.shared.exception.RestServiceException;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

@Api(value = "manufacturer")
@RequestMapping("/manufacturers")
public interface ManufacturerApi {

	@ApiOperation(value = "", notes = "If exists, returns the manufacturer corresponding to the given id", response = Manufacturer.class, tags = {})
	@ApiResponses(value = { @ApiResponse(code = 200, message = "found manufacturer", response = Manufacturer.class),
			@ApiResponse(code = 204, message = "no manufacturer found", response = Void.class),
			@ApiResponse(code = 401, message = "unauthorized", response = Void.class),
			@ApiResponse(code = 403, message = "forbidden", response = Void.class),
			@ApiResponse(code = 500, message = "unexpected error", response = ErrorModel.class) })
	@GetMapping(value = "/{manufacturerId}", produces = { "application/json" })
	@PreAuthorize("hasAnyRole('ADMIN', 'EXPERT', 'USER')")
	ResponseEntity<Manufacturer> findManufacturerById(
			@ApiParam(value = "id of the manufacturer", required = true) @PathVariable("manufacturerId") Long manufacturerId);

	@ApiOperation(value = "", notes = "Returns all the manufacturers", response = Manufacturer.class, responseContainer = "List", tags = {})
	@ApiResponses(value = { @ApiResponse(code = 200, message = "found manufacturers", response = Manufacturer.class),
			@ApiResponse(code = 204, message = "no manufacturer found", response = Void.class),
			@ApiResponse(code = 401, message = "unauthorized", response = Void.class),
			@ApiResponse(code = 403, message = "forbidden", response = Void.class),
			@ApiResponse(code = 500, message = "unexpected error", response = ErrorModel.class) })
	@GetMapping(value = "", produces = { "application/json" })
	@PreAuthorize("hasAnyRole('ADMIN', 'EXPERT', 'USER')")
	ResponseEntity<List<Manufacturer>> findManufacturers();

	@ApiOperation(value = "", notes = "Saves a new manufacturer", response = Manufacturer.class, tags = {})
	@ApiResponses(value = { @ApiResponse(code = 200, message = "created manufacturer", response = Manufacturer.class),
			@ApiResponse(code = 401, message = "unauthorized", response = Void.class),
			@ApiResponse(code = 403, message = "forbidden", response = Void.class),
			@ApiResponse(code = 422, message = "bad parameters", response = Void.class),
			@ApiResponse(code = 500, message = "unexpected error", response = ErrorModel.class) })
	@PostMapping(value = "", produces = { "application/json" }, consumes = {
			"application/json" })
	@PreAuthorize("hasAnyRole('ADMIN', 'EXPERT')")
	ResponseEntity<Manufacturer> saveNewManufacturer(
			@ApiParam(value = "manufacturer to create", required = true) @RequestBody Manufacturer manufacturer,
			final BindingResult result) throws RestServiceException;

	@ApiOperation(value = "", notes = "Updates a manufacturer", response = Void.class, tags = {})
	@ApiResponses(value = { @ApiResponse(code = 204, message = "manufacturer updated", response = Void.class),
			@ApiResponse(code = 401, message = "unauthorized", response = Void.class),
			@ApiResponse(code = 403, message = "forbidden", response = Void.class),
			@ApiResponse(code = 404, message = "manufacturer not found", response = Void.class),
			@ApiResponse(code = 422, message = "bad parameters", response = Void.class),
			@ApiResponse(code = 500, message = "unexpected error", response = ErrorModel.class) })
	@PutMapping(value = "/{manufacturerId}", produces = { "application/json" }, consumes = {
			"application/json" })
	@PreAuthorize("hasAnyRole('ADMIN', 'EXPERT') and @controlerSecurityService.idMatches(#manufacturerId, #manufacturer)")
	ResponseEntity<Void> updateManufacturer(
			@ApiParam(value = "id of the manufacturer", required = true) @PathVariable("manufacturerId") Long manufacturerId,
			@ApiParam(value = "manufacturer to update", required = true) @RequestBody Manufacturer manufacturer,
			BindingResult result) throws RestServiceException;
	
	@ApiOperation(value = "", notes = "Deletes a manufacturer", response = Void.class, tags = {})
	@ApiResponses(value = { @ApiResponse(code = 204, message = "manufacturer deleted", response = Void.class),
			@ApiResponse(code = 401, message = "unauthorized", response = Void.class),
			@ApiResponse(code = 403, message = "forbidden", response = Void.class),
			@ApiResponse(code = 404, message = "no center found", response = Void.class),
			@ApiResponse(code = 500, message = "unexpected error", response = ErrorModel.class) })
	@DeleteMapping(value = "/{manufacturerId}", produces = { "application/json" })
	@PreAuthorize("hasAnyRole('ADMIN', 'EXPERT')")
	ResponseEntity<Void> deleteManufacturer(
			@ApiParam(value = "id of the manufacturer", required = true) @PathVariable("manufacturerId") Long manufacturerId)
			throws RestServiceException;

}

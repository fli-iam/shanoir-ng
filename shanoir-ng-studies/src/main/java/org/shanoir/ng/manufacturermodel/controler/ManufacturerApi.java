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

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "manufacturer")
@RequestMapping("/manufacturers")
public interface ManufacturerApi {

	@Operation(summary = "", description = "If exists, returns the manufacturer corresponding to the given id")
	@ApiResponses(value = { @ApiResponse(responseCode = "200", description = "found manufacturer"),
			@ApiResponse(responseCode = "204", description = "no manufacturer found"),
			@ApiResponse(responseCode = "401", description = "unauthorized"),
			@ApiResponse(responseCode = "403", description = "forbidden"),
			@ApiResponse(responseCode = "500", description = "unexpected error") })
	@GetMapping(value = "/{manufacturerId}", produces = { "application/json" })
	@PreAuthorize("hasAnyRole('ADMIN', 'EXPERT', 'USER')")
	ResponseEntity<Manufacturer> findManufacturerById(
			@Parameter(description = "id of the manufacturer", required = true) @PathVariable("manufacturerId") Long manufacturerId);

	@Operation(summary = "", description = "Returns all the manufacturers")
	@ApiResponses(value = { @ApiResponse(responseCode = "200", description = "found manufacturers"),
			@ApiResponse(responseCode = "204", description = "no manufacturer found"),
			@ApiResponse(responseCode = "401", description = "unauthorized"),
			@ApiResponse(responseCode = "403", description = "forbidden"),
			@ApiResponse(responseCode = "500", description = "unexpected error") })
	@GetMapping(value = "", produces = { "application/json" })
	@PreAuthorize("hasAnyRole('ADMIN', 'EXPERT', 'USER')")
	ResponseEntity<List<Manufacturer>> findManufacturers();

	@Operation(summary = "", description = "Saves a new manufacturer")
	@ApiResponses(value = { @ApiResponse(responseCode = "200", description = "created manufacturer"),
			@ApiResponse(responseCode = "401", description = "unauthorized"),
			@ApiResponse(responseCode = "403", description = "forbidden"),
			@ApiResponse(responseCode = "422", description = "bad parameters"),
			@ApiResponse(responseCode = "500", description = "unexpected error") })
	@PostMapping(value = "", produces = { "application/json" }, consumes = {
			"application/json" })
	@PreAuthorize("hasAnyRole('ADMIN', 'EXPERT')")
	ResponseEntity<Manufacturer> saveNewManufacturer(
			@Parameter(description = "manufacturer to create", required = true) @RequestBody Manufacturer manufacturer,
			final BindingResult result) throws RestServiceException;

	@Operation(summary = "", description = "Updates a manufacturer")
	@ApiResponses(value = { @ApiResponse(responseCode = "204", description = "manufacturer updated"),
			@ApiResponse(responseCode = "401", description = "unauthorized"),
			@ApiResponse(responseCode = "403", description = "forbidden"),
			@ApiResponse(responseCode = "404", description = "manufacturer not found"),
			@ApiResponse(responseCode = "422", description = "bad parameters"),
			@ApiResponse(responseCode = "500", description = "unexpected error") })
	@PutMapping(value = "/{manufacturerId}", produces = { "application/json" }, consumes = {
			"application/json" })
	@PreAuthorize("hasAnyRole('ADMIN', 'EXPERT') and @controlerSecurityService.idMatches(#manufacturerId, #manufacturer)")
	ResponseEntity<Void> updateManufacturer(
			@Parameter(description = "id of the manufacturer", required = true) @PathVariable("manufacturerId") Long manufacturerId,
			@Parameter(description = "manufacturer to update", required = true) @RequestBody Manufacturer manufacturer,
			BindingResult result) throws RestServiceException;
	
	@Operation(summary = "", description = "Deletes a manufacturer")
	@ApiResponses(value = { @ApiResponse(responseCode = "204", description = "manufacturer deleted"),
			@ApiResponse(responseCode = "401", description = "unauthorized"),
			@ApiResponse(responseCode = "403", description = "forbidden"),
			@ApiResponse(responseCode = "404", description = "no center found"),
			@ApiResponse(responseCode = "500", description = "unexpected error") })
	@DeleteMapping(value = "/{manufacturerId}", produces = { "application/json" })
	@PreAuthorize("hasAnyRole('ADMIN', 'EXPERT')")
	ResponseEntity<Void> deleteManufacturer(
			@Parameter(description = "id of the manufacturer", required = true) @PathVariable("manufacturerId") Long manufacturerId)
			throws RestServiceException;

}

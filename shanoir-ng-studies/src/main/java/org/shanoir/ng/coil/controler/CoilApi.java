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

import org.shanoir.ng.coil.dto.CoilDTO;
import org.shanoir.ng.coil.model.Coil;
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
import jakarta.validation.Valid;

@Tag(name = "coil")
@RequestMapping("/coils")
public interface CoilApi {

	@Operation(summary = "", description = "Deletes a coil")
	@ApiResponses(value = { @ApiResponse(responseCode = "204", description = "coil deleted"),
			@ApiResponse(responseCode = "401", description = "unauthorized"),
			@ApiResponse(responseCode = "403", description = "forbidden"),
			@ApiResponse(responseCode = "404", description = "no coil found"),
			@ApiResponse(responseCode = "500", description = "unexpected error") })
	@DeleteMapping(value = "/{coilId}", produces = { "application/json" })
	@PreAuthorize("hasAnyRole('ADMIN', 'EXPERT')")
	ResponseEntity<Void> deleteCoil(
			@Parameter(description = "id of the coil", required = true) @PathVariable("coilId") Long coilId)
			throws RestServiceException;

	@Operation(summary = "", description = "If exists, returns the coil corresponding to the given id")
	@ApiResponses(value = { @ApiResponse(responseCode = "200", description = "found coil"),
			@ApiResponse(responseCode = "401", description = "unauthorized"),
			@ApiResponse(responseCode = "403", description = "forbidden"),
			@ApiResponse(responseCode = "404", description = "no coil found"),
			@ApiResponse(responseCode = "500", description = "unexpected error") })
	@GetMapping(value = "/{coilId}", produces = { "application/json" })
	@PreAuthorize("hasAnyRole('ADMIN', 'EXPERT', 'USER')")
	ResponseEntity<CoilDTO> findCoilById(
			@Parameter(description = "id of the coil", required = true) @PathVariable("coilId") Long coilId);

	@Operation(summary = "", description = "If exists, returns all the coil corresponding to the given center id")
	@ApiResponses(value = { @ApiResponse(responseCode = "200", description = "found coil"),
			@ApiResponse(responseCode = "401", description = "unauthorized"),
			@ApiResponse(responseCode = "403", description = "forbidden"),
			@ApiResponse(responseCode = "204", description = "no coil found"),
			@ApiResponse(responseCode = "500", description = "unexpected error") })
	@GetMapping(value = "/byCenter/{centerId}", produces = { "application/json" })
	@PreAuthorize("hasAnyRole('ADMIN', 'EXPERT', 'USER')")
	ResponseEntity<List<CoilDTO>> findCoilsByCenterId(
			@Parameter(name = "id of the center", required = true) @PathVariable("centerId") Long centerId);

	@Operation(summary = "", description = "Returns all the coils")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "found coils"),
			@ApiResponse(responseCode = "204", description = "no coil found"),
			@ApiResponse(responseCode = "401", description = "unauthorized"),
			@ApiResponse(responseCode = "403", description = "forbidden"),
			@ApiResponse(responseCode = "500", description = "unexpected error") })
	@GetMapping(value = "", produces = { "application/json" })
	@PreAuthorize("hasAnyRole('ADMIN', 'EXPERT', 'USER')")
	ResponseEntity<List<CoilDTO>> findCoils();

	@Operation(summary = "", description = "Saves a new coil")
	@ApiResponses(value = { @ApiResponse(responseCode = "200", description = "created coil"),
			@ApiResponse(responseCode = "401", description = "unauthorized"),
			@ApiResponse(responseCode = "403", description = "forbidden"),
			@ApiResponse(responseCode = "422", description = "bad parameters"),
			@ApiResponse(responseCode = "500", description = "unexpected error") })
	@PostMapping(value = "", produces = { "application/json" }, consumes = {
			"application/json" })
	@PreAuthorize("hasAnyRole('ADMIN', 'EXPERT')")
	ResponseEntity<CoilDTO> saveNewCoil(@Parameter(description = "coil to create", required = true) @Valid @RequestBody Coil coil,
			BindingResult result) throws RestServiceException;

	@Operation(summary = "", description = "Updates a coil")
	@ApiResponses(value = { @ApiResponse(responseCode = "204", description = "coil updated"),
			@ApiResponse(responseCode = "401", description = "unauthorized"),
			@ApiResponse(responseCode = "403", description = "forbidden"),
			@ApiResponse(responseCode = "422", description = "bad parameters"),
			@ApiResponse(responseCode = "500", description = "unexpected error") })
	@PutMapping(value = "/{coilId}", produces = { "application/json" }, consumes = {
			"application/json" })
	@PreAuthorize("hasAnyRole('ADMIN', 'EXPERT') and @controlerSecurityService.idMatches(#coilId, #coil)")
	ResponseEntity<Void> updateCoil(
			@Parameter(description = "id of the coil", required = true) @PathVariable("coilId") Long coilId,
			@Parameter(description = "coil to update", required = true) @Valid @RequestBody Coil coil, BindingResult result)
			throws RestServiceException;

}

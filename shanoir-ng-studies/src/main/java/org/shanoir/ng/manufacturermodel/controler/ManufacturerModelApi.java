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

import org.shanoir.ng.manufacturermodel.model.ManufacturerModel;
import org.shanoir.ng.shared.core.model.IdName;
import org.shanoir.ng.shared.exception.RestServiceException;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "manufacturermodel")
@RequestMapping("/manufacturermodels")
public interface ManufacturerModelApi {

	@Operation(summary = "", description = "If exists, returns the manufacturer model corresponding to the given id")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "found manufacturer model"),
			@ApiResponse(responseCode = "204", description = "no manufacturer model found"),
			@ApiResponse(responseCode = "401", description = "unauthorized"),
			@ApiResponse(responseCode = "403", description = "forbidden"),
			@ApiResponse(responseCode = "500", description = "unexpected error") })
	@GetMapping(value = "/{manufacturerModelId}", produces = { "application/json" })
	@PreAuthorize("hasAnyRole('ADMIN', 'EXPERT', 'USER')")
	ResponseEntity<ManufacturerModel> findManufacturerModelById(
			@Parameter(description = "id of the manufacturer model", required = true) @PathVariable("manufacturerModelId") Long manufacturerModelId);

	@Operation(summary = "", description = "Returns id and name of all the manufacturer models")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "found manufacturer models"),
			@ApiResponse(responseCode = "204", description = "no manufacturer model found"),
			@ApiResponse(responseCode = "401", description = "unauthorized"),
			@ApiResponse(responseCode = "403", description = "forbidden"),
			@ApiResponse(responseCode = "500", description = "unexpected error") })
	@GetMapping(value = "/names", produces = { "application/json" })
	@PreAuthorize("hasAnyRole('ADMIN', 'EXPERT', 'USER')")
	ResponseEntity<List<IdName>> findManufacturerModelsNames();

	@Operation(summary = "", description = "Returns id and name of all the manufacturer models")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "found manufacturer models"),
			@ApiResponse(responseCode = "204", description = "no manufacturer model found"),
			@ApiResponse(responseCode = "401", description = "unauthorized"),
			@ApiResponse(responseCode = "403", description = "forbidden"),
			@ApiResponse(responseCode = "500", description = "unexpected error") })
	@GetMapping(value = "/centerManuModelsNames/{centerId}", produces = { "application/json" })
	@PreAuthorize("hasAnyRole('ADMIN', 'EXPERT', 'USER')")
	ResponseEntity<List<IdName>> findCenterManufacturerModelsNames(@Parameter(description = "id of the center", required = true) @PathVariable("centerId") Long centerId);

	@Operation(summary = "", description = "Returns all the manufacturer models")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "found manufacturer models"),
			@ApiResponse(responseCode = "204", description = "no manufacturer model found"),
			@ApiResponse(responseCode = "401", description = "unauthorized"),
			@ApiResponse(responseCode = "403", description = "forbidden"),
			@ApiResponse(responseCode = "500", description = "unexpected error") })
	@GetMapping(value = "", produces = { "application/json" })
	@PreAuthorize("hasAnyRole('ADMIN', 'EXPERT', 'USER')")
	ResponseEntity<List<ManufacturerModel>> findManufacturerModels();

	@Operation(summary = "", description = "Saves a new manufacturer model")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "created manufacturer model"),
			@ApiResponse(responseCode = "401", description = "unauthorized"),
			@ApiResponse(responseCode = "403", description = "forbidden"),
			@ApiResponse(responseCode = "422", description = "bad parameters"),
			@ApiResponse(responseCode = "500", description = "unexpected error") })
	@PostMapping(value = "", produces = { "application/json" }, consumes = {
			"application/json" })
	@PreAuthorize("hasAnyRole('ADMIN', 'EXPERT')")
	ResponseEntity<ManufacturerModel> saveNewManufacturerModel(
			@Parameter(description = "manufacturer model to create", required = true) @RequestBody ManufacturerModel manufacturerModel,
			final BindingResult result) throws RestServiceException;

	@Operation(summary = "", description = "Updates a manufacturer model")
	@ApiResponses(value = { @ApiResponse(responseCode = "204", description = "manufacturer model updated"),
			@ApiResponse(responseCode = "401", description = "unauthorized"),
			@ApiResponse(responseCode = "403", description = "forbidden"),
			@ApiResponse(responseCode = "404", description = "manufacturer model not found"),
			@ApiResponse(responseCode = "422", description = "bad parameters"),
			@ApiResponse(responseCode = "500", description = "unexpected error") })
	@PutMapping(value = "/{manufacturerModelId}", produces = { "application/json" }, consumes = {
			"application/json" })
	@PreAuthorize("hasAnyRole('ADMIN', 'EXPERT') and @controllerSecurityService.idMatches(#manufacturerModelId, #manufacturerModel)")
	ResponseEntity<Void> updateManufacturerModel(
			@Parameter(description = "id of the manufacturer model", required = true) @PathVariable("manufacturerModelId") Long manufacturerModelId,
			@Parameter(description = "manufacturer model to update", required = true) @RequestBody ManufacturerModel manufacturerModel,
			final BindingResult result) throws RestServiceException;
}

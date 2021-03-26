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
import org.shanoir.ng.shared.exception.ErrorModel;
import org.shanoir.ng.shared.exception.RestServiceException;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.BindingResult;
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

@Api(value = "manufacturermodel")
@RequestMapping("/manufacturermodels")
public interface ManufacturerModelApi {

	@ApiOperation(value = "", notes = "If exists, returns the manufacturer model corresponding to the given id", response = ManufacturerModel.class, tags = {})
	@ApiResponses(value = {
			@ApiResponse(code = 200, message = "found manufacturer model", response = ManufacturerModel.class),
			@ApiResponse(code = 204, message = "no manufacturer model found", response = Void.class),
			@ApiResponse(code = 401, message = "unauthorized", response = Void.class),
			@ApiResponse(code = 403, message = "forbidden", response = Void.class),
			@ApiResponse(code = 500, message = "unexpected error", response = ErrorModel.class) })
	@GetMapping(value = "/{manufacturerModelId}", produces = { "application/json" })
	@PreAuthorize("hasAnyRole('ADMIN', 'EXPERT', 'USER')")
	ResponseEntity<ManufacturerModel> findManufacturerModelById(
			@ApiParam(value = "id of the manufacturer model", required = true) @PathVariable("manufacturerModelId") Long manufacturerModelId);

	@ApiOperation(value = "", notes = "Returns id and name of all the manufacturer models", response = IdName.class, responseContainer = "List", tags = {})
	@ApiResponses(value = {
			@ApiResponse(code = 200, message = "found manufacturer models", response = ManufacturerModel.class),
			@ApiResponse(code = 204, message = "no manufacturer model found", response = Void.class),
			@ApiResponse(code = 401, message = "unauthorized", response = Void.class),
			@ApiResponse(code = 403, message = "forbidden", response = Void.class),
			@ApiResponse(code = 500, message = "unexpected error", response = ErrorModel.class) })
	@GetMapping(value = "/names", produces = { "application/json" })
	@PreAuthorize("hasAnyRole('ADMIN', 'EXPERT', 'USER')")
	ResponseEntity<List<IdName>> findManufacturerModelsNames();
	
	
	@ApiOperation(value = "", notes = "Returns id and name of all the manufacturer models", response = IdName.class, responseContainer = "List", tags = {})
	@ApiResponses(value = {
			@ApiResponse(code = 200, message = "found manufacturer models", response = ManufacturerModel.class),
			@ApiResponse(code = 204, message = "no manufacturer model found", response = Void.class),
			@ApiResponse(code = 401, message = "unauthorized", response = Void.class),
			@ApiResponse(code = 403, message = "forbidden", response = Void.class),
			@ApiResponse(code = 500, message = "unexpected error", response = ErrorModel.class) })
	@GetMapping(value = "/centerManuModelsNames/{centerId}", produces = { "application/json" })
	@PreAuthorize("hasAnyRole('ADMIN', 'EXPERT', 'USER')")
	ResponseEntity<List<IdName>> findCenterManufacturerModelsNames(@ApiParam(value = "id of the center", required = true) @PathVariable("centerId") Long centerId);
	
	@ApiOperation(value = "", notes = "Returns all the manufacturer models", response = ManufacturerModel.class, responseContainer = "List", tags = {})
	@ApiResponses(value = {
			@ApiResponse(code = 200, message = "found manufacturer models", response = ManufacturerModel.class),
			@ApiResponse(code = 204, message = "no manufacturer model found", response = Void.class),
			@ApiResponse(code = 401, message = "unauthorized", response = Void.class),
			@ApiResponse(code = 403, message = "forbidden", response = Void.class),
			@ApiResponse(code = 500, message = "unexpected error", response = ErrorModel.class) })
	@GetMapping(value = "", produces = { "application/json" })
	@PreAuthorize("hasAnyRole('ADMIN', 'EXPERT', 'USER')")
	ResponseEntity<List<ManufacturerModel>> findManufacturerModels();

	@ApiOperation(value = "", notes = "Saves a new manufacturer model", response = ManufacturerModel.class, tags = {})
	@ApiResponses(value = {
			@ApiResponse(code = 200, message = "created manufacturer model", response = ManufacturerModel.class),
			@ApiResponse(code = 401, message = "unauthorized", response = Void.class),
			@ApiResponse(code = 403, message = "forbidden", response = Void.class),
			@ApiResponse(code = 422, message = "bad parameters", response = Void.class),
			@ApiResponse(code = 500, message = "unexpected error", response = ErrorModel.class) })
	@PostMapping(value = "", produces = { "application/json" }, consumes = {
			"application/json" })
	@PreAuthorize("hasAnyRole('ADMIN', 'EXPERT')")
	ResponseEntity<ManufacturerModel> saveNewManufacturerModel(
			@ApiParam(value = "manufacturer model to create", required = true) @RequestBody ManufacturerModel manufacturerModel,
			final BindingResult result) throws RestServiceException;

	@ApiOperation(value = "", notes = "Updates a manufacturer model", response = Void.class, tags = {})
	@ApiResponses(value = { @ApiResponse(code = 204, message = "manufacturer model updated", response = Void.class),
			@ApiResponse(code = 401, message = "unauthorized", response = Void.class),
			@ApiResponse(code = 403, message = "forbidden", response = Void.class),
			@ApiResponse(code = 404, message = "manufacturer model not found", response = Void.class),
			@ApiResponse(code = 422, message = "bad parameters", response = Void.class),
			@ApiResponse(code = 500, message = "unexpected error", response = ErrorModel.class) })
	@PutMapping(value = "/{manufacturerModelId}", produces = { "application/json" }, consumes = {
			"application/json" })
	@PreAuthorize("hasAnyRole('ADMIN', 'EXPERT') and @controlerSecurityService.idMatches(#manufacturerModelId, #manufacturerModel)")
	ResponseEntity<Void> updateManufacturerModel(
			@ApiParam(value = "id of the manufacturer model", required = true) @PathVariable("manufacturerModelId") Long manufacturerModelId,
			@ApiParam(value = "manufacturer model to update", required = true) @RequestBody ManufacturerModel manufacturerModel,
			final BindingResult result) throws RestServiceException;

}

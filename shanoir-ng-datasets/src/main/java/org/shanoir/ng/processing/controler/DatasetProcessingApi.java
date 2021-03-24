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

package org.shanoir.ng.processing.controler;

import java.util.List;

import javax.validation.Valid;

import org.shanoir.ng.dataset.dto.DatasetDTO;
import org.shanoir.ng.processing.dto.DatasetProcessingDTO;
import org.shanoir.ng.processing.model.DatasetProcessing;
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

@Api(value = "datasetProcessing")
@RequestMapping("/datasetProcessing")
public interface DatasetProcessingApi {

	@ApiOperation(value = "", notes = "Deletes a dataset processing", response = Void.class, tags = {})
	@ApiResponses(value = { @ApiResponse(code = 204, message = "dataset processing deleted", response = Void.class),
			@ApiResponse(code = 401, message = "unauthorized", response = Void.class),
			@ApiResponse(code = 403, message = "forbidden", response = Void.class),
			@ApiResponse(code = 404, message = "no dataset processing found", response = Void.class),
			@ApiResponse(code = 500, message = "unexpected error", response = ErrorModel.class) })
	@DeleteMapping(value = "/{datasetProcessingId}", produces = { "application/json" })
	@PreAuthorize("hasAnyRole('ADMIN', 'EXPERT')")
	ResponseEntity<Void> deleteDatasetProcessing(
			@ApiParam(value = "id of the dataset processing", required = true) @PathVariable("datasetProcessingId") Long datasetProcessingId)
			throws RestServiceException;

	@ApiOperation(value = "", notes = "If exists, returns the dataset processing corresponding to the given id", response = DatasetProcessingDTO.class, tags = {})
	@ApiResponses(value = { @ApiResponse(code = 200, message = "found dataset processing", response = DatasetProcessingDTO.class),
			@ApiResponse(code = 401, message = "unauthorized", response = Void.class),
			@ApiResponse(code = 403, message = "forbidden", response = Void.class),
			@ApiResponse(code = 404, message = "no dataset processing found", response = Void.class),
			@ApiResponse(code = 500, message = "unexpected error", response = ErrorModel.class) })
	@GetMapping(value = "/{datasetProcessingId}", produces = { "application/json" })
	@PreAuthorize("hasAnyRole('ADMIN', 'EXPERT', 'USER')")
	ResponseEntity<DatasetProcessingDTO> findDatasetProcessingById(
			@ApiParam(value = "id of the dataset processing", required = true) @PathVariable("datasetProcessingId") Long datasetProcessingId);

	@ApiOperation(value = "", notes = "Returns all the dataset processings", response = DatasetProcessingDTO.class, responseContainer = "List", tags = {})
	@ApiResponses(value = {
			@ApiResponse(code = 200, message = "found dataset processings", response = DatasetProcessingDTO.class, responseContainer = "List"),
			@ApiResponse(code = 204, message = "no dataset processing found", response = Void.class),
			@ApiResponse(code = 401, message = "unauthorized", response = Void.class),
			@ApiResponse(code = 403, message = "forbidden", response = Void.class),
			@ApiResponse(code = 500, message = "unexpected error", response = ErrorModel.class) })
	@GetMapping(value = "", produces = { "application/json" })
	@PreAuthorize("hasAnyRole('ADMIN', 'EXPERT', 'USER')")
	ResponseEntity<List<DatasetProcessingDTO>> findDatasetProcessings();

	@ApiOperation(value = "", notes = "Returns the input datasets of a processing", response = DatasetDTO.class, responseContainer = "List", tags = {})
	@ApiResponses(value = {
			@ApiResponse(code = 200, message = "found dataset processings", response = DatasetDTO.class, responseContainer = "List"),
			@ApiResponse(code = 204, message = "no dataset processing found", response = Void.class),
			@ApiResponse(code = 401, message = "unauthorized", response = Void.class),
			@ApiResponse(code = 403, message = "forbidden", response = Void.class),
			@ApiResponse(code = 500, message = "unexpected error", response = ErrorModel.class) })
	@GetMapping(value = "/{datasetProcessingId}/inputDatasets/", produces = { "application/json" })
	@PreAuthorize("hasAnyRole('ADMIN', 'EXPERT', 'USER')")
	ResponseEntity<List<DatasetDTO>> getInputDatasets(@ApiParam(value = "id of the dataset processing", required = true) @PathVariable("datasetProcessingId") Long datasetProcessingId);

	@ApiOperation(value = "", notes = "Returns the output datasets of a processing", response = DatasetDTO.class, responseContainer = "List", tags = {})
	@ApiResponses(value = {
			@ApiResponse(code = 200, message = "found dataset processings", response = DatasetDTO.class, responseContainer = "List"),
			@ApiResponse(code = 204, message = "no dataset processing found", response = Void.class),
			@ApiResponse(code = 401, message = "unauthorized", response = Void.class),
			@ApiResponse(code = 403, message = "forbidden", response = Void.class),
			@ApiResponse(code = 500, message = "unexpected error", response = ErrorModel.class) })
	@GetMapping(value = "/{datasetProcessingId}/outputDatasets/", produces = { "application/json" })
	@PreAuthorize("hasAnyRole('ADMIN', 'EXPERT', 'USER')")
	ResponseEntity<List<DatasetDTO>> getOutputDatasets(@ApiParam(value = "id of the dataset processing", required = true) @PathVariable("datasetProcessingId") Long datasetProcessingId);

	@ApiOperation(value = "", notes = "Saves a new dataset processing", response = DatasetProcessingDTO.class, tags = {})
	@ApiResponses(value = { @ApiResponse(code = 200, message = "created dataset processing", response = DatasetProcessingDTO.class),
			@ApiResponse(code = 401, message = "unauthorized", response = Void.class),
			@ApiResponse(code = 403, message = "forbidden", response = Void.class),
			@ApiResponse(code = 422, message = "bad parameters", response = ErrorModel.class),
			@ApiResponse(code = 500, message = "unexpected error", response = ErrorModel.class) })
	@PostMapping(value = "", produces = { "application/json" }, consumes = {
			"application/json" })
	@PreAuthorize("hasAnyRole('ADMIN', 'EXPERT')")
	ResponseEntity<DatasetProcessingDTO> saveNewDatasetProcessing(@ApiParam(value = "dataset processing to create", required = true) @Valid @RequestBody DatasetProcessing datasetProcessing,
			BindingResult result) throws RestServiceException;

	@ApiOperation(value = "", notes = "Updates a dataset processing", response = Void.class, tags = {})
	@ApiResponses(value = { @ApiResponse(code = 204, message = "dataset processing updated", response = Void.class),
			@ApiResponse(code = 401, message = "unauthorized", response = Void.class),
			@ApiResponse(code = 403, message = "forbidden", response = Void.class),
			@ApiResponse(code = 422, message = "bad parameters", response = ErrorModel.class),
			@ApiResponse(code = 500, message = "unexpected error", response = ErrorModel.class) })
	@PutMapping(value = "/{datasetProcessingId}", produces = { "application/json" }, consumes = {
			"application/json" })
	@PreAuthorize("hasAnyRole('ADMIN', 'EXPERT') and @controlerSecurityService.idMatches(#datasetProcessingId, #datasetProcessing)")
	ResponseEntity<Void> updateDatasetProcessing(
			@ApiParam(value = "id of the dataset processing", required = true) @PathVariable("datasetProcessingId") Long datasetProcessingId,
			@ApiParam(value = "dataset processing to update", required = true) @Valid @RequestBody DatasetProcessing datasetProcessing, BindingResult result)
			throws RestServiceException;

}

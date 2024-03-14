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

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.apache.solr.client.solrj.SolrServerException;
import org.shanoir.ng.dataset.dto.DatasetDTO;
import org.shanoir.ng.processing.dto.DatasetProcessingDTO;
import org.shanoir.ng.processing.model.DatasetProcessing;
import org.shanoir.ng.shared.exception.RestServiceException;
import org.shanoir.ng.shared.exception.ShanoirException;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;

@Tag(name = "datasetProcessing")
@RequestMapping("/datasetProcessing")
public interface DatasetProcessingApi {

	@Operation(summary = "", description = "Deletes a dataset processing")
	@ApiResponses(value = { @ApiResponse(responseCode = "204", description = "dataset processing deleted"),
			@ApiResponse(responseCode = "401", description = "unauthorized"),
			@ApiResponse(responseCode = "403", description = "forbidden"),
			@ApiResponse(responseCode = "404", description = "no dataset processing found"),
			@ApiResponse(responseCode = "500", description = "unexpected error") })
	@DeleteMapping(value = "/{datasetProcessingId}", produces = { "application/json" })
	@PreAuthorize("hasAnyRole('ADMIN', 'EXPERT')")
	ResponseEntity<Void> deleteDatasetProcessing(
			@Parameter(name = "id of the dataset processing", required = true) @PathVariable("datasetProcessingId") Long datasetProcessingId)
            throws RestServiceException, ShanoirException, SolrServerException, IOException;

	@Operation(summary = "", description = "If exists, returns the dataset processing corresponding to the given id")
	@ApiResponses(value = { @ApiResponse(responseCode = "200", description = "found dataset processing"),
			@ApiResponse(responseCode = "401", description = "unauthorized"),
			@ApiResponse(responseCode = "403", description = "forbidden"),
			@ApiResponse(responseCode = "404", description = "no dataset processing found"),
			@ApiResponse(responseCode = "500", description = "unexpected error") })
	@GetMapping(value = "/{datasetProcessingId}", produces = { "application/json" })
	@PreAuthorize("hasAnyRole('ADMIN', 'EXPERT', 'USER')")
	ResponseEntity<DatasetProcessingDTO> findDatasetProcessingById(
			@Parameter(name = "id of the dataset processing", required = true) @PathVariable("datasetProcessingId") Long datasetProcessingId);

	@Operation(summary = "", description = "Returns all the dataset processings")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "found dataset processings"),
			@ApiResponse(responseCode = "204", description = "no dataset processing found"),
			@ApiResponse(responseCode = "401", description = "unauthorized"),
			@ApiResponse(responseCode = "403", description = "forbidden"),
			@ApiResponse(responseCode = "500", description = "unexpected error") })
	@GetMapping(value = "/study/{studyId}/subject/{subjectId}", produces = { "application/json" })
	@PreAuthorize("hasRole('ADMIN') or (hasAnyRole('EXPERT', 'USER') and @datasetSecurityService.hasRightOnStudy(#studyId, 'CAN_SEE_ALL'))")
	ResponseEntity<List<DatasetProcessingDTO>> findDatasetProcessingsByStudyIdAndSubjectId(
			@Parameter(name = "id of the study", required = true) @PathVariable("studyId") Long studyId,
			@Parameter(name = "id of the subject", required = true) @PathVariable("subjectId") Long subjectId);

	@Operation(summary = "", description = "Returns the dataset processings with given study and subject")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "found dataset processings"),
			@ApiResponse(responseCode = "204", description = "no dataset processing found"),
			@ApiResponse(responseCode = "401", description = "unauthorized"),
			@ApiResponse(responseCode = "403", description = "forbidden"),
			@ApiResponse(responseCode = "500", description = "unexpected error") })
	@GetMapping(value = "", produces = { "application/json" })
	@PreAuthorize("hasAnyRole('ADMIN', 'EXPERT', 'USER')")
	ResponseEntity<List<DatasetProcessingDTO>> findDatasetProcessings();

	@Operation(summary = "", description = "Returns the input datasets of a processing")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "found dataset processings"),
			@ApiResponse(responseCode = "204", description = "no dataset processing found"),
			@ApiResponse(responseCode = "401", description = "unauthorized"),
			@ApiResponse(responseCode = "403", description = "forbidden"),
			@ApiResponse(responseCode = "500", description = "unexpected error") })
	@GetMapping(value = "/{datasetProcessingId}/inputDatasets/", produces = { "application/json" })
	@PreAuthorize("hasAnyRole('ADMIN', 'EXPERT', 'USER')")
	ResponseEntity<List<DatasetDTO>> getInputDatasets(@Parameter(name = "id of the dataset processing", required = true) @PathVariable("datasetProcessingId") Long datasetProcessingId);

	@Operation(summary = "", description = "Returns the output datasets of a processing")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "found dataset processings"),
			@ApiResponse(responseCode = "204", description = "no dataset processing found"),
			@ApiResponse(responseCode = "401", description = "unauthorized"),
			@ApiResponse(responseCode = "403", description = "forbidden"),
			@ApiResponse(responseCode = "500", description = "unexpected error") })
	@GetMapping(value = "/{datasetProcessingId}/outputDatasets/", produces = { "application/json" })
	@PreAuthorize("hasAnyRole('ADMIN', 'EXPERT', 'USER')")
	ResponseEntity<List<DatasetDTO>> getOutputDatasets(@Parameter(name = "id of the dataset processing", required = true) @PathVariable("datasetProcessingId") Long datasetProcessingId);

	@Operation(summary = "", description = "Saves a new dataset processing")
	@ApiResponses(value = { @ApiResponse(responseCode = "200", description = "created dataset processing"),
			@ApiResponse(responseCode = "401", description = "unauthorized"),
			@ApiResponse(responseCode = "403", description = "forbidden"),
			@ApiResponse(responseCode = "422", description = "bad parameters"),
			@ApiResponse(responseCode = "500", description = "unexpected error") })
	@PostMapping(value = "", produces = { "application/json" }, consumes = {
			"application/json" })
	@PreAuthorize("hasAnyRole('ADMIN', 'EXPERT', 'USER')")
	ResponseEntity<DatasetProcessingDTO> saveNewDatasetProcessing(@Parameter(name = "dataset processing to create", required = true) @Valid @RequestBody DatasetProcessing datasetProcessing,
			BindingResult result) throws RestServiceException;

	@Operation(summary = "", description = "Updates a dataset processing")
	@ApiResponses(value = { @ApiResponse(responseCode = "204", description = "dataset processing updated"),
			@ApiResponse(responseCode = "401", description = "unauthorized"),
			@ApiResponse(responseCode = "403", description = "forbidden"),
			@ApiResponse(responseCode = "422", description = "bad parameters"),
			@ApiResponse(responseCode = "500", description = "unexpected error") })
	@PutMapping(value = "/{datasetProcessingId}", produces = { "application/json" }, consumes = {
			"application/json" })
	@PreAuthorize("hasAnyRole('ADMIN', 'EXPERT', 'USER') and @controlerSecurityService.idMatches(#datasetProcessingId, #datasetProcessing)")
	ResponseEntity<Void> updateDatasetProcessing(
			@Parameter(name = "id of the dataset processing", required = true) @PathVariable("datasetProcessingId") Long datasetProcessingId,
			@Parameter(name = "dataset processing to update", required = true) @Valid @RequestBody DatasetProcessing datasetProcessing, BindingResult result)
			throws RestServiceException;

}

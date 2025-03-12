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

package org.shanoir.ng.tag.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.apache.solr.client.solrj.SolrServerException;
import org.shanoir.ng.shared.exception.EntityNotFoundException;
import org.shanoir.ng.shared.exception.RestServiceException;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;

@Tag(name = "studytag")
@RequestMapping("/studytag")
public interface StudyTagApi {

	@Operation(summary = "addStudyTagsToDataset", description = "Add study tags to a dataset")
	@ApiResponses(value = { @ApiResponse(responseCode = "200", description = "dataset associated to study tags"),
			@ApiResponse(responseCode = "401", description = "unauthorized"),
			@ApiResponse(responseCode = "403", description = "forbidden"),
			@ApiResponse(responseCode = "404", description = "dataset does not exists"),
			@ApiResponse(responseCode = "422", description = "bad parameters"),
			@ApiResponse(responseCode = "500", description = "unexpected error") })
	@PutMapping(value = "/addStudyTagsToDataset")
	@PreAuthorize("hasRole('ADMIN') or (hasRole('EXPERT') and @datasetSecurityService.hasRightOnDataset(#datasetId, 'CAN_IMPORT'))")
	ResponseEntity<Void> addStudyTagsToDataset(
			@Parameter(description = "id of the dataset", required = true) @RequestParam(value = "datasetId") Long datasetId,
			@Parameter(description = "study tag ids", required = true) @RequestParam(value = "studyTagIds") List<Long> studyTagIds)
			throws RestServiceException, EntityNotFoundException, SolrServerException, IOException;

	@Operation(summary = "removeStudyTagsFromDataset", description = "Add study tags to a dataset")
	@ApiResponses(value = { @ApiResponse(responseCode = "200", description = "dataset associated to study tags"),
			@ApiResponse(responseCode = "401", description = "unauthorized"),
			@ApiResponse(responseCode = "403", description = "forbidden"),
			@ApiResponse(responseCode = "404", description = "dataset does not exists"),
			@ApiResponse(responseCode = "422", description = "bad parameters"),
			@ApiResponse(responseCode = "500", description = "unexpected error") })
	@PutMapping(value = "/removeStudyTagsFromDataset")
	@PreAuthorize("hasRole('ADMIN') or (hasRole('EXPERT') and @datasetSecurityService.hasRightOnDataset(#datasetId, 'CAN_IMPORT'))")
	ResponseEntity<Void> removeStudyTagsFromDataset(
			@Parameter(description = "id of the dataset", required = true) @RequestParam(value = "datasetId") Long datasetId,
			@Parameter(description = "study tag ids", required = true) @RequestParam(value = "studyTagIds") List<Long> studyTagIds)
			throws RestServiceException, EntityNotFoundException, SolrServerException, IOException;
}

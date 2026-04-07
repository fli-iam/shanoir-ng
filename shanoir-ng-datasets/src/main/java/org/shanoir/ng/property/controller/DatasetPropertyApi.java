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

package org.shanoir.ng.property.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.shanoir.ng.property.model.DatasetPropertyDTO;
import org.shanoir.ng.shared.exception.EntityNotFoundException;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@Tag(name = "properties", description = "the dataset properties API")
@RequestMapping("/properties")
public interface DatasetPropertyApi {

    @Operation(summary = "", description = "Returns the dataset properties associated to the given dataset")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "found dataset properties"),
            @ApiResponse(responseCode = "204", description = "no dataset properties found"),
            @ApiResponse(responseCode = "401", description = "unauthorized"),
            @ApiResponse(responseCode = "403", description = "forbidden"),
            @ApiResponse(responseCode = "404", description = "dataset does not exists"),
            @ApiResponse(responseCode = "500", description = "unexpected error") })
    @GetMapping(value = "/dataset/{datasetId}", produces = { "application/json" })
    @PreAuthorize("hasAnyRole('ADMIN', 'EXPERT', 'USER') and @datasetSecurityService.hasRightOnDataset(#datasetId, 'CAN_SEE_ALL')")
    ResponseEntity<List<DatasetPropertyDTO>> getPropertiesByDatasetId(
            @Parameter(description = "id of the dataset", required = true)
            @PathVariable("datasetId")
            Long datasetId);

    @Operation(summary = "", description = "Returns the dataset properties associated to the given processing")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "found dataset properties"),
            @ApiResponse(responseCode = "204", description = "no dataset properties found"),
            @ApiResponse(responseCode = "401", description = "unauthorized"),
            @ApiResponse(responseCode = "403", description = "forbidden"),
            @ApiResponse(responseCode = "404", description = "processing does not exists"),
            @ApiResponse(responseCode = "500", description = "unexpected error") })
    @GetMapping(value = "/processing/{processingId}", produces = { "application/json" })
    @PreAuthorize("hasAnyRole('ADMIN', 'EXPERT', 'USER')")
    ResponseEntity<List<DatasetPropertyDTO>> getPropertiesByProcessingId(@Parameter(description = "id of the processing", required = true) @PathVariable("processingId") Long processingId) throws EntityNotFoundException;

}

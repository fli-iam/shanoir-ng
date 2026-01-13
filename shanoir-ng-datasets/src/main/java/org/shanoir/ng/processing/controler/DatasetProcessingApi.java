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

import java.io.IOException;
import java.util.List;

import com.fasterxml.jackson.databind.JsonNode;
import org.apache.solr.client.solrj.SolrServerException;
import org.shanoir.ng.dataset.dto.DatasetDTO;
import org.shanoir.ng.processing.dto.DatasetProcessingDTO;
import org.shanoir.ng.processing.model.DatasetProcessing;
import org.shanoir.ng.shared.exception.RestServiceException;
import org.shanoir.ng.shared.exception.ShanoirException;
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
import org.springframework.web.bind.annotation.RequestParam;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;

@Tag(name = "datasetProcessing")
@RequestMapping("/datasetProcessing")
public interface DatasetProcessingApi {

    @Operation(summary = "", description = "Deletes a dataset processing")
    @ApiResponses(value = {@ApiResponse(responseCode = "204", description = "dataset processing deleted"),
            @ApiResponse(responseCode = "401", description = "unauthorized"),
            @ApiResponse(responseCode = "403", description = "forbidden"),
            @ApiResponse(responseCode = "404", description = "no dataset processing found"),
            @ApiResponse(responseCode = "500", description = "unexpected error")})
    @DeleteMapping(value = "/{datasetProcessingId}", produces = {"application/json"})
    @PreAuthorize("hasAnyRole('ADMIN', 'EXPERT')")
    ResponseEntity<Void> deleteDatasetProcessing(
            @Parameter(description = "id of the dataset processing", required = true) @PathVariable("datasetProcessingId") Long datasetProcessingId)
            throws RestServiceException, ShanoirException, SolrServerException, IOException;

    @Operation(summary = "", description = "If exists, returns the dataset processing corresponding to the given id")
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "found dataset processing"),
            @ApiResponse(responseCode = "401", description = "unauthorized"),
            @ApiResponse(responseCode = "403", description = "forbidden"),
            @ApiResponse(responseCode = "404", description = "no dataset processing found"),
            @ApiResponse(responseCode = "500", description = "unexpected error")})
    @GetMapping(value = "/{datasetProcessingId}", produces = {"application/json"})
    @PreAuthorize("hasAnyRole('ADMIN', 'EXPERT', 'USER')")
    ResponseEntity<DatasetProcessingDTO> findDatasetProcessingById(
            @Parameter(description = "id of the dataset processing", required = true) @PathVariable("datasetProcessingId") Long datasetProcessingId);

    @Operation(summary = "", description = "Returns the dataset processings with given study and subject")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "found dataset processings"),
            @ApiResponse(responseCode = "204", description = "no dataset processing found"),
            @ApiResponse(responseCode = "401", description = "unauthorized"),
            @ApiResponse(responseCode = "403", description = "forbidden"),
            @ApiResponse(responseCode = "500", description = "unexpected error")})
    @GetMapping(value = "", produces = {"application/json"})
    @PreAuthorize("hasAnyRole('ADMIN', 'EXPERT', 'USER')")
    ResponseEntity<List<DatasetProcessingDTO>> findDatasetProcessings();

    @Operation(summary = "", description = "Returns the processings of an input dataset")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "found dataset processings"),
            @ApiResponse(responseCode = "204", description = "no dataset processing found"),
            @ApiResponse(responseCode = "401", description = "unauthorized"),
            @ApiResponse(responseCode = "403", description = "forbidden"),
            @ApiResponse(responseCode = "500", description = "unexpected error")})
    @GetMapping(value = "/inputDataset/{datasetId}", produces = {"application/json"})
    @PreAuthorize("hasAnyRole('ADMIN', 'EXPERT', 'USER')")
    ResponseEntity<List<DatasetProcessingDTO>> getProcessingsByInputDataset(@Parameter(description = "id of the input dataset", required = true) @PathVariable("datasetId") Long datasetId);

    @Operation(summary = "", description = "Returns the input datasets of a processing")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "found dataset processings"),
            @ApiResponse(responseCode = "204", description = "no dataset processing found"),
            @ApiResponse(responseCode = "401", description = "unauthorized"),
            @ApiResponse(responseCode = "403", description = "forbidden"),
            @ApiResponse(responseCode = "500", description = "unexpected error")})
    @GetMapping(value = "/{datasetProcessingId}/inputDatasets/", produces = {"application/json"})
    @PreAuthorize("hasAnyRole('ADMIN', 'EXPERT', 'USER')")
    ResponseEntity<List<DatasetDTO>> getInputDatasets(@Parameter(description = "id of the dataset processing", required = true) @PathVariable("datasetProcessingId") Long datasetProcessingId);

    @Operation(summary = "", description = "Returns the output datasets of a processing")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "found dataset processings"),
            @ApiResponse(responseCode = "204", description = "no dataset processing found"),
            @ApiResponse(responseCode = "401", description = "unauthorized"),
            @ApiResponse(responseCode = "403", description = "forbidden"),
            @ApiResponse(responseCode = "500", description = "unexpected error")})
    @GetMapping(value = "/{datasetProcessingId}/outputDatasets/", produces = {"application/json"})
    @PreAuthorize("hasAnyRole('ADMIN', 'EXPERT', 'USER')")
    ResponseEntity<List<DatasetDTO>> getOutputDatasets(@Parameter(description = "id of the dataset processing", required = true) @PathVariable("datasetProcessingId") Long datasetProcessingId);

    @Operation(summary = "", description = "Saves a new dataset processing")
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "created dataset processing"),
            @ApiResponse(responseCode = "401", description = "unauthorized"),
            @ApiResponse(responseCode = "403", description = "forbidden"),
            @ApiResponse(responseCode = "422", description = "bad parameters"),
            @ApiResponse(responseCode = "500", description = "unexpected error")})
    @PostMapping(value = "", produces = {"application/json"}, consumes = {
            "application/json"})
    @PreAuthorize("hasAnyRole('ADMIN', 'EXPERT', 'USER')")
    ResponseEntity<DatasetProcessingDTO> saveNewDatasetProcessing(@Parameter(description = "dataset processing to create", required = true) @Valid @RequestBody DatasetProcessing datasetProcessing,
                                                                  BindingResult result) throws RestServiceException;

    @Operation(summary = "", description = "Updates a dataset processing")
    @ApiResponses(value = {@ApiResponse(responseCode = "204", description = "dataset processing updated"),
            @ApiResponse(responseCode = "401", description = "unauthorized"),
            @ApiResponse(responseCode = "403", description = "forbidden"),
            @ApiResponse(responseCode = "422", description = "bad parameters"),
            @ApiResponse(responseCode = "500", description = "unexpected error")})
    @PutMapping(value = "/{datasetProcessingId}", produces = {"application/json"}, consumes = {
            "application/json"})
    @PreAuthorize("hasAnyRole('ADMIN', 'EXPERT', 'USER') and @controllerSecurityService.idMatches(#datasetProcessingId, #datasetProcessing)")
    ResponseEntity<Void> updateDatasetProcessing(
            @Parameter(description = "id of the dataset processing", required = true) @PathVariable("datasetProcessingId") Long datasetProcessingId,
            @Parameter(description = "dataset processing to update", required = true) @Valid @RequestBody DatasetProcessing datasetProcessing, BindingResult result)
            throws RestServiceException;

    @Operation(summary = "massiveDownloadByProcessingIds", description = "If exists, returns a zip file of the inputs/outputs per processing corresponding to the given processing IDs.  Datas are in the http response body, it must be written in a zip file. Datas are sorted with folders according to their respective examination and processing.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "zip file"),
            @ApiResponse(responseCode = "401", description = "unauthorized"),
            @ApiResponse(responseCode = "403", description = "forbidden"),
            @ApiResponse(responseCode = "404", description = "no dataset found"),
            @ApiResponse(responseCode = "500", description = "unexpected error")})
    @PostMapping(value = "/massiveDownloadByProcessingIds")
    @PreAuthorize("hasRole('ADMIN') or (hasAnyRole('EXPERT', 'USER') and @datasetSecurityService.hasRightOnEveryDatasetOfProcessings(#processingIds, 'CAN_DOWNLOAD'))")
    void massiveDownloadByProcessingIds(
            @Parameter(description = "id of the processing", required = true) @Valid
            @RequestBody List<Long> processingIds,
            @Parameter(description = "outputs to extract") @Valid
            @RequestParam(value = "resultOnly", defaultValue = "false") boolean resultOnly, HttpServletResponse response) throws RestServiceException;

    @Operation(summary = "massiveDownloadProcessingByExaminationIds", description = "If exists, returns a zip file of the inputs/outputs per processing corresponding to the given examination IDs. Datas are in the http response body, it must be written in a zip file. Datas are sorted with folders according to their respective examination and processing.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "zip file"),
            @ApiResponse(responseCode = "401", description = "unauthorized"),
            @ApiResponse(responseCode = "403", description = "forbidden"),
            @ApiResponse(responseCode = "404", description = "no dataset found"),
            @ApiResponse(responseCode = "500", description = "unexpected error")})
    @PostMapping(value = "/massiveDownloadProcessingByExaminationIds")
    @PreAuthorize("hasRole('ADMIN') or (hasAnyRole('EXPERT', 'USER') and @datasetSecurityService.hasRightOnExaminations(#examinationIds, 'CAN_DOWNLOAD'))")
    void massiveDownloadProcessingByExaminationIds(
            @Parameter(description = "id of the examination", required = true) @Valid
            @RequestBody List<Long> examinationIds,
            @Parameter(description = "comment of the desired processings") @Valid
            @RequestParam(value = "processingComment", required = false) String processingComment,
            @Parameter(description = "outputs to extract") @Valid
            @RequestParam(value = "resultOnly", defaultValue = "false") boolean resultOnly, HttpServletResponse response) throws RestServiceException;

    @Operation(summary = "complexMassiveDownload", description = "Returns a zip file of the inputs/outputs according to the json params file in the HTTP request body. Datas are in the http response body, it must be written in a zip file.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "OK"),
            @ApiResponse(responseCode = "401", description = "unauthorized"),
            @ApiResponse(responseCode = "403", description = "forbidden"),
            @ApiResponse(responseCode = "404", description = "no dataset found"),
            @ApiResponse(responseCode = "500", description = "unexpected error")})
    @PostMapping(value = "/complexMassiveDownload")
    ResponseEntity<String> complexMassiveDownload(
            @Parameter(description = "parameters for download", required = true)
            @Valid @RequestBody JsonNode request);
}

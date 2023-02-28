package org.shanoir.ng.processing.carmin.controller;

import java.util.List;

import jakarta.validation.Valid;

import org.shanoir.ng.processing.carmin.model.CarminDatasetProcessing;
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
import org.springframework.web.bind.annotation.RequestParam;

import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;

@Tag(name = "carminDatasetProcessing")
@RequestMapping("/carminDatasetProcessing")
public interface CarminDatasetProcessingApi {

        @Operation(summary = "", description = "Saves a new carmin dataset processing")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "created dataset processing"),
                        @ApiResponse(responseCode = "401", description = "unauthorized"),
                        @ApiResponse(responseCode = "403", description = "forbidden"),
                        @ApiResponse(responseCode = "422", description = "bad parameters"),
                        @ApiResponse(responseCode = "500", description = "unexpected error") })
        @PostMapping(value = "", produces = { "application/json" }, consumes = {
                        "application/json" })
        @PreAuthorize("hasAnyRole('ADMIN', 'EXPERT')")
        ResponseEntity<CarminDatasetProcessing> saveNewCarminDatasetProcessing(
                        @Parameter(name = "carmin dataset processing to create", required = true) @Valid @RequestBody CarminDatasetProcessing carminDatasetProcessing,
                        BindingResult result) throws RestServiceException;

        @Operation(summary = "", description = "If exists, returns the carmin dataset processing corresponding to the given id")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "found dataset processing"),
                        @ApiResponse(responseCode = "401", description = "unauthorized"),
                        @ApiResponse(responseCode = "403", description = "forbidden"),
                        @ApiResponse(responseCode = "404", description = "no dataset processing found"),
                        @ApiResponse(responseCode = "500", description = "unexpected error") })
        @GetMapping(value = "/{datasetProcessingId}", produces = { "application/json" })
        @PreAuthorize("hasAnyRole('ADMIN', 'EXPERT', 'USER')")
        ResponseEntity<CarminDatasetProcessing> findCarminDatasetProcessingById(
                        @Parameter(name = "id of the carmin dataset processing", required = true) @PathVariable("datasetProcessingId") Long datasetProcessingId);

        @Operation(summary = "", description = "If exists, returns the carmin dataset processing corresponding to the given execution identifier with carmin new fields")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "found dataset processing"),
                        @ApiResponse(responseCode = "401", description = "unauthorized"),
                        @ApiResponse(responseCode = "403", description = "forbidden"),
                        @ApiResponse(responseCode = "404", description = "no dataset processing found"),
                        @ApiResponse(responseCode = "500", description = "unexpected error") })
        @GetMapping(value = "", produces = { "application/json" })
        @PreAuthorize("hasAnyRole('ADMIN', 'EXPERT', 'USER')")
        ResponseEntity<CarminDatasetProcessing> findCarminDatasetProcessingByIdentifier(
                        @Parameter(name = "identifier of the execution", required = true) @RequestParam("identifier") String identifier);

        @Operation(summary = "", description = "If exists, returns the dataset processing corresponding to the given id with carmin new fields")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "found dataset processing"),
                        @ApiResponse(responseCode = "401", description = "unauthorized"),
                        @ApiResponse(responseCode = "403", description = "forbidden"),
                        @ApiResponse(responseCode = "404", description = "no dataset processing found"),
                        @ApiResponse(responseCode = "500", description = "unexpected error") })
        @GetMapping(value = "carminDatasetProcessings", produces = { "application/json" })
        @PreAuthorize("hasAnyRole('ADMIN', 'EXPERT', 'USER')")
        ResponseEntity<List<CarminDatasetProcessing>> findCarminDatasetProcessings();

        @Operation(summary = "", description = "Updates a dataset processing")
        @ApiResponses(value = { @ApiResponse(responseCode = "204", description = "dataset processing updated"),
                        @ApiResponse(responseCode = "401", description = "unauthorized"),
                        @ApiResponse(responseCode = "403", description = "forbidden"),
                        @ApiResponse(responseCode = "422", description = "bad parameters"),
                        @ApiResponse(responseCode = "500", description = "unexpected error") })
        @PutMapping(value = "/{datasetProcessingId}", produces = { "application/json" }, consumes = {
                        "application/json" })
        @PreAuthorize("hasAnyRole('ADMIN', 'EXPERT')")
        ResponseEntity<Void> updateCarminDatasetProcessing(
                        @Parameter(name = "id of the dataset processing", required = true) @PathVariable("datasetProcessingId") Long datasetProcessingId,
                        @Parameter(name = "carmin dataset processing to update", required = true) @Valid @RequestBody CarminDatasetProcessing carminDatasetProcessing,
                        BindingResult result)
                        throws RestServiceException;
}

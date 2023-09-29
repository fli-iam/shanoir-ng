package org.shanoir.ng.processing.carmin.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.shanoir.ng.processing.dto.ExecutionMonitoringDTO;
import org.shanoir.ng.shared.exception.EntityNotFoundException;
import org.shanoir.ng.shared.exception.RestServiceException;
import org.shanoir.ng.shared.exception.SecurityException;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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
        ResponseEntity<ExecutionMonitoringDTO> saveNewCarminDatasetProcessing(
                        @Parameter(name = "carmin dataset processing to create", required = true) @Valid @RequestBody ExecutionMonitoringDTO carminDatasetProcessing,
                        @Parameter(name = "start monitoring job once created") @RequestParam(value = "start", required = false, defaultValue="false") boolean start,
                        BindingResult result) throws RestServiceException, EntityNotFoundException, SecurityException;

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
                @Parameter(name = "carmin dataset processing to update", required = true) @Valid @RequestBody ExecutionMonitoringDTO carminDatasetProcessing,
                @Parameter(name = "start monitoring job once updated") @RequestParam(value = "start", required = false, defaultValue="false") boolean start,
                BindingResult result)
                throws RestServiceException, SecurityException;

        @Operation(summary = "", description = "If exists, returns the carmin dataset processing corresponding to the given id")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "found dataset processing"),
                        @ApiResponse(responseCode = "401", description = "unauthorized"),
                        @ApiResponse(responseCode = "403", description = "forbidden"),
                        @ApiResponse(responseCode = "404", description = "no dataset processing found"),
                        @ApiResponse(responseCode = "500", description = "unexpected error") })
        @GetMapping(value = "/{datasetProcessingId}", produces = { "application/json" })
        @PreAuthorize("hasAnyRole('ADMIN', 'EXPERT', 'USER')")
        ResponseEntity<ExecutionMonitoringDTO> findCarminDatasetProcessingById(
                        @Parameter(name = "id of the carmin dataset processing", required = true) @PathVariable("datasetProcessingId") Long datasetProcessingId);

        @Operation(summary = "", description = "If exists, returns the dataset processing corresponding to the given id with carmin new fields")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "found dataset processing"),
                        @ApiResponse(responseCode = "401", description = "unauthorized"),
                        @ApiResponse(responseCode = "403", description = "forbidden"),
                        @ApiResponse(responseCode = "404", description = "no dataset processing found"),
                        @ApiResponse(responseCode = "500", description = "unexpected error") })
        @GetMapping(value = "all", produces = { "application/json" })
        @PreAuthorize("hasAnyRole('ADMIN', 'EXPERT', 'USER')")
        ResponseEntity<List<ExecutionMonitoringDTO>> getAllCarminDatasetProcessings();

}

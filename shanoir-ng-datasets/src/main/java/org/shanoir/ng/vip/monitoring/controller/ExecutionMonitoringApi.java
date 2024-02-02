package org.shanoir.ng.vip.monitoring.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.shanoir.ng.vip.dto.ExecutionMonitoringDTO;
import org.shanoir.ng.shared.exception.EntityNotFoundException;
import org.shanoir.ng.shared.exception.RestServiceException;
import org.shanoir.ng.shared.exception.SecurityException;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "execution-monitoring")
@RequestMapping("/execution-monitoring")
public interface ExecutionMonitoringApi {

        @Operation(summary = "", description = "Saves a new execution monitoring")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "created execution monitoring"),
                        @ApiResponse(responseCode = "401", description = "unauthorized"),
                        @ApiResponse(responseCode = "403", description = "forbidden"),
                        @ApiResponse(responseCode = "422", description = "bad parameters"),
                        @ApiResponse(responseCode = "500", description = "unexpected error") })
        @PostMapping(value = "", produces = { "application/json" }, consumes = {
                        "application/json" })
        @PreAuthorize("hasAnyRole('ADMIN', 'EXPERT')")
        ResponseEntity<ExecutionMonitoringDTO> saveNewExecutionMonitoring(
                        @Parameter(name = "execution monitoring to create", required = true) @Valid @RequestBody ExecutionMonitoringDTO dto,
                        @Parameter(name = "start monitoring job once created") @RequestParam(value = "start", required = false, defaultValue="false") boolean start,
                        BindingResult result) throws RestServiceException, EntityNotFoundException, SecurityException;

        @Operation(summary = "", description = "Updates a execution monitoring")
        @ApiResponses(value = { @ApiResponse(responseCode = "204", description = "updated execution monitoring"),
                @ApiResponse(responseCode = "401", description = "unauthorized"),
                @ApiResponse(responseCode = "403", description = "forbidden"),
                @ApiResponse(responseCode = "422", description = "bad parameters"),
                @ApiResponse(responseCode = "500", description = "unexpected error") })
        @PutMapping(value = "/{executionMonitoringId}", produces = { "application/json" }, consumes = {
                "application/json" })
        @PreAuthorize("hasAnyRole('ADMIN', 'EXPERT')")
        ResponseEntity<Void> updateExecutionMonitoring(
                @Parameter(name = "execution monitoring to update", required = true) @Valid @RequestBody ExecutionMonitoringDTO dto,
                @Parameter(name = "start monitoring job once updated") @RequestParam(value = "start", required = false, defaultValue="false") boolean start,
                BindingResult result)
                throws RestServiceException, SecurityException;

        @Operation(summary = "", description = "If exists, returns the execution monitoring corresponding to the given id")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "found execution monitoring"),
                        @ApiResponse(responseCode = "401", description = "unauthorized"),
                        @ApiResponse(responseCode = "403", description = "forbidden"),
                        @ApiResponse(responseCode = "404", description = "no execution monitoring found"),
                        @ApiResponse(responseCode = "500", description = "unexpected error") })
        @GetMapping(value = "/{id}", produces = { "application/json" })
        @PreAuthorize("hasAnyRole('ADMIN', 'EXPERT', 'USER')")
        ResponseEntity<ExecutionMonitoringDTO> findExecutionMonitoringById(
                        @Parameter(name = "id of the execution monitoring", required = true) @PathVariable("id") Long id);

        @Operation(summary = "", description = "Return all execution monitorings")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "found execution monitorings"),
                        @ApiResponse(responseCode = "401", description = "unauthorized"),
                        @ApiResponse(responseCode = "403", description = "forbidden"),
                        @ApiResponse(responseCode = "404", description = "no execution monitoring found"),
                        @ApiResponse(responseCode = "500", description = "unexpected error") })
        @GetMapping(value = "all", produces = { "application/json" })
        @PreAuthorize("hasAnyRole('ADMIN', 'EXPERT', 'USER')")
        ResponseEntity<List<ExecutionMonitoringDTO>> getAllExecutionMonitoring();

}

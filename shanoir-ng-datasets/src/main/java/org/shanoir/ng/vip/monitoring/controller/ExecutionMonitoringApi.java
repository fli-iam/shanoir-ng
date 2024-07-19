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
                        @Parameter(description = "id of the execution monitoring", required = true) @PathVariable("id") Long id);

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

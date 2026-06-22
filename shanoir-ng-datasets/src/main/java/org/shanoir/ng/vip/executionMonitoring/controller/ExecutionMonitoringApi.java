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

package org.shanoir.ng.vip.executionMonitoring.controller;

import java.util.List;

import org.shanoir.ng.vip.executionMonitoring.dto.ExecutionMonitoringDTO;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

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
    @GetMapping(value = "/{executionMonitoringId}", produces = { "application/json" })
    @PreAuthorize("hasAnyRole('ADMIN') or @executionMonitoringSecurityService.hasRightOnExecutionMonitoringById(#executionMonitoringId, 'CAN_SEE_ALL')")
    ResponseEntity<ExecutionMonitoringDTO> findExecutionMonitoringById(
                    @Parameter(description = "id of the execution monitoring", required = true) @PathVariable("executionMonitoringId") Long executionMonitoringId);


    @Operation(summary = "", description = "Return all execution monitorings")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "found execution monitorings"),
        @ApiResponse(responseCode = "401", description = "unauthorized"),
        @ApiResponse(responseCode = "403", description = "forbidden"),
        @ApiResponse(responseCode = "404", description = "no execution monitoring found"),
        @ApiResponse(responseCode = "500", description = "unexpected error")})
    @GetMapping(value = "all", produces = {"application/json"})
    @PreAuthorize("hasAnyRole('ADMIN')")
    ResponseEntity<List<ExecutionMonitoringDTO>> getAllExecutionMonitoring();

}

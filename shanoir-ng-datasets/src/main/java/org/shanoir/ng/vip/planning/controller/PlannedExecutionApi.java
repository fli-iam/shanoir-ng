package org.shanoir.ng.vip.planning.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.shanoir.ng.shared.exception.EntityNotFoundException;
import org.shanoir.ng.shared.exception.RestServiceException;
import org.shanoir.ng.shared.exception.SecurityException;
import org.shanoir.ng.vip.planning.model.PlannedExecution;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;

/*
    Planned execution are VIP execution atuomatically applied after an import.
 */
@Tag(name = "plannedexecution", description = "the planned execution API")
@RequestMapping("/vip/execution/planned")
public interface PlannedExecutionApi {

    @Operation(summary = "Get list of existing planned executions for the given study_id", description = "Returns the list of existing planned executions for the given study id", tags={  })
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "successful response, returns the list of planned executions"),
            @ApiResponse(responseCode = "403", description = "forbidden"),
            @ApiResponse(responseCode = "500", description = "unexpected error"),
            @ApiResponse(responseCode = "503", description = "Internal error")})
    @GetMapping(value = "/byStudy/{studyId}",
            produces = { "application/json", "application/octet-stream" })
    ResponseEntity<List<PlannedExecution>> getPlannedExecutionsByStudyId(@Parameter(description = "The study Id", required=true) @PathVariable("studyId") Long studyId) throws IOException, RestServiceException, EntityNotFoundException, SecurityException;

    @Operation(summary = "Create a new PlannedExecution entity", description = "Creates a new planned execution", tags={  })
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "successful creation"),
            @ApiResponse(responseCode = "403", description = "forbidden"),
            @ApiResponse(responseCode = "500", description = "unexpected error"),
            @ApiResponse(responseCode = "503", description = "Internal error")})
    @PostMapping(value = "", consumes = "application/json", produces = "application/json")
    ResponseEntity<PlannedExecution> createPlannedExecution(@RequestBody PlannedExecution plannedExecution) throws IOException, RestServiceException, SecurityException;

    @Operation(summary = "Delete a PlannedExecution entity", description = "Deletes the planned execution by its ID", tags={  })
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "successful deletion"),
            @ApiResponse(responseCode = "403", description = "forbidden"),
            @ApiResponse(responseCode = "404", description = "not found"),
            @ApiResponse(responseCode = "500", description = "unexpected error"),
            @ApiResponse(responseCode = "503", description = "Internal error")})
    @DeleteMapping(value = "/delete/{executionId}")
    ResponseEntity<Void> deletePlannedExecution(@Parameter(description = "The PlannedExecution Id", required=true) @PathVariable("executionId") Long executionId) throws IOException, RestServiceException, EntityNotFoundException, SecurityException;

    @Operation(summary = "Get a PlannedExecution entity by ID", description = "Returns a planned execution by its ID", tags={  })
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "successful response, returns the planned execution"),
            @ApiResponse(responseCode = "403", description = "forbidden"),
            @ApiResponse(responseCode = "404", description = "not found"),
            @ApiResponse(responseCode = "500", description = "unexpected error"),
            @ApiResponse(responseCode = "503", description = "Internal error")})
    @GetMapping(value = "/{executionId}", produces = "application/json")
    ResponseEntity<PlannedExecution> getPlannedExecutionById(@Parameter(description = "The PlannedExecution Id", required=true) @PathVariable("executionId") Long executionId) throws IOException, RestServiceException, EntityNotFoundException, SecurityException;

    @Operation(summary = "Update a PlannedExecution entity", description = "Updates the existing planned execution by its ID", tags={  })
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "successful update, returns the updated planned execution"),
            @ApiResponse(responseCode = "403", description = "forbidden"),
            @ApiResponse(responseCode = "404", description = "not found"),
            @ApiResponse(responseCode = "500", description = "unexpected error"),
            @ApiResponse(responseCode = "503", description = "Internal error")})
    @PostMapping(value = "/{executionId}", consumes = "application/json", produces = "application/json")
    ResponseEntity<PlannedExecution> updatePlannedExecution(@RequestBody PlannedExecution plannedExecution) throws IOException, RestServiceException, EntityNotFoundException, SecurityException;

}

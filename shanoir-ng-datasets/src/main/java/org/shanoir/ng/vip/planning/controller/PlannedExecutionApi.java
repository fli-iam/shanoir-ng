package org.shanoir.ng.vip.planning.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.shanoir.ng.shared.exception.EntityNotFoundException;
import org.shanoir.ng.shared.exception.RestServiceException;
import org.shanoir.ng.shared.exception.SecurityException;
import org.shanoir.ng.vip.planning.dto.PlannedExecutionDTO;
import org.shanoir.ng.vip.planning.model.PlannedExecution;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PostAuthorize;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;

/*
    Planned execution are VIP execution atuomatically applied after an import.
 */
@Tag(name = "Planned execution", description = "the planned execution API")
@RequestMapping("/plannedexecution")
public interface PlannedExecutionApi {

    @Operation(summary = "Get list of existing planned executions for the given study_id", description = "Returns the list of existing planned executions for the given study id", tags={  })
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "successful response, returns the list of planned executions"),
            @ApiResponse(responseCode = "403", description = "forbidden"),
            @ApiResponse(responseCode = "500", description = "unexpected error"),
            @ApiResponse(responseCode = "503", description = "Internal error")})
    @GetMapping(value = "/byStudy/{studyId}",
            produces = { "application/json", "application/octet-stream" })
    @PreAuthorize("hasRole('ADMIN') or (hasRole('EXPERT') and @datasetSecurityService.hasRightOnStudy(#studyId, 'CAN_SEE_ALL'))")
    ResponseEntity<List<PlannedExecutionDTO>> getPlannedExecutionsByStudyId(@Parameter(description = "The study Id", required=true) @PathVariable("studyId") Long studyId) throws IOException, RestServiceException, EntityNotFoundException, SecurityException;

    @Operation(summary = "Create a new PlannedExecution entity", description = "Creates a new planned execution", tags={  })
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "successful creation"),
            @ApiResponse(responseCode = "403", description = "forbidden"),
            @ApiResponse(responseCode = "500", description = "unexpected error"),
            @ApiResponse(responseCode = "503", description = "Internal error")})
    @PostMapping(value = "", consumes = "application/json", produces = "application/json")
    @PreAuthorize("hasRole('ADMIN') or (hasRole('EXPERT') and @datasetSecurityService.hasRightOnStudy(#plannedExecution.getStudy(), 'CAN_ADMINISTRATE'))")
    ResponseEntity<PlannedExecutionDTO> saveNewPlannedExecution(@Parameter(description = "planned execution to create", required = true) @RequestBody PlannedExecution plannedExecution) throws IOException, RestServiceException, SecurityException;

    @Operation(summary = "Delete a PlannedExecution entity", description = "Deletes the planned execution by its ID", tags={  })
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "successful deletion"),
            @ApiResponse(responseCode = "403", description = "forbidden"),
            @ApiResponse(responseCode = "404", description = "not found"),
            @ApiResponse(responseCode = "500", description = "unexpected error"),
            @ApiResponse(responseCode = "503", description = "Internal error")})
    @DeleteMapping(value = "/{executionId}", produces = { "application/json" })
    ResponseEntity<Void> deletePlannedExecution(@Parameter(description = "The PlannedExecution Id", required=true) @PathVariable("executionId") Long executionId) throws IOException, RestServiceException, EntityNotFoundException, SecurityException;

    @Operation(summary = "Get a PlannedExecution entity by ID", description = "Returns a planned execution by its ID", tags={  })
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "successful response, returns the planned execution"),
            @ApiResponse(responseCode = "403", description = "forbidden"),
            @ApiResponse(responseCode = "404", description = "not found"),
            @ApiResponse(responseCode = "500", description = "unexpected error"),
            @ApiResponse(responseCode = "503", description = "Internal error")})
    @GetMapping(value = "/{executionId}", produces = "application/json")
    @PostAuthorize("hasRole('ADMIN') or (hasRole('EXPERT') and @datasetSecurityService.hasRightOnStudy(returnObject.getBody().getStudy(), 'CAN_SEE_ALL'))")
    ResponseEntity<PlannedExecutionDTO> getPlannedExecutionById(@Parameter(description = "The PlannedExecution Id", required=true) @PathVariable("executionId") Long executionId) throws IOException, RestServiceException, EntityNotFoundException, SecurityException;

    @Operation(summary = "Update a PlannedExecution entity", description = "Updates the existing planned execution by its ID", tags={  })
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "successful update, returns the updated planned execution"),
            @ApiResponse(responseCode = "403", description = "forbidden"),
            @ApiResponse(responseCode = "404", description = "not found"),
            @ApiResponse(responseCode = "500", description = "unexpected error"),
            @ApiResponse(responseCode = "503", description = "Internal error")})
    @PutMapping(value = "/{executionId}", consumes = "application/json", produces = "application/json")
    @PreAuthorize("hasRole('ADMIN') or (hasRole('EXPERT') and @datasetSecurityService.hasRightOnStudy(#plannedExecution.getStudy(), 'CAN_ADMINISTRATE'))")
    ResponseEntity<PlannedExecutionDTO> updatePlannedExecution(
            @Parameter(description = "id of the planned execution", required = true) @PathVariable("executionId") Long executionId,
            @Parameter(description = "center to update", required = true) @RequestBody PlannedExecutionDTO plannedExecution, BindingResult result)
    throws IOException, RestServiceException, EntityNotFoundException, SecurityException;

}

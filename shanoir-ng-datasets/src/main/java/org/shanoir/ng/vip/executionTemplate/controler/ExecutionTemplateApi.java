package org.shanoir.ng.vip.executionTemplate.controler;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

import org.shanoir.ng.vip.executionTemplate.dto.ExecutionTemplateDTO;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PostAuthorize;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/*
    Execution template are VIP execution automatically applied after an import, according to their configuration.
 */

@Tag(name = "Execution template", description = "the execution template API")
@RequestMapping("/execution-template")
public interface ExecutionTemplateApi {

    @Operation(summary = "Get a list of existing execution templates for the given study_id", description = "Returns the list of existing execution templates for the given study id", tags = {})
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "successful response, returns the list of execution templates"),
            @ApiResponse(responseCode = "403", description = "forbidden"),
            @ApiResponse(responseCode = "500", description = "unexpected error"),
            @ApiResponse(responseCode = "503", description = "Internal error")})
    @GetMapping(value = "/byStudy/{studyId}",
            produces = {"application/json", "application/octet-stream"})
    @PreAuthorize("hasRole('ADMIN') or (hasRole('EXPERT') and @datasetSecurityService.hasRightOnStudy(#studyId, 'CAN_SEE_ALL'))")
    ResponseEntity<List<ExecutionTemplateDTO>> getExecutionTemplatesByStudyId(@Parameter(description = "The study Id", required = true) @PathVariable("studyId") Long studyId);

    @Operation(summary = "Create a new ExecutionTemplate entity", description = "Creates a new execution template", tags = {})
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "successful creation"),
            @ApiResponse(responseCode = "403", description = "forbidden"),
            @ApiResponse(responseCode = "500", description = "unexpected error"),
            @ApiResponse(responseCode = "503", description = "Internal error")})
    @PostMapping(value = "", consumes = "application/json", produces = "application/json")
    @PreAuthorize("hasRole('ADMIN') or (hasRole('EXPERT') and @datasetSecurityService.hasRightOnStudy(#executionTemplateDTO.getStudyId(), 'CAN_ADMINISTRATE'))")
    ResponseEntity<ExecutionTemplateDTO> saveNewExecutionTemplate(@Parameter(description = "execution template dto to create", required = true) @RequestBody ExecutionTemplateDTO executionTemplateDTO);

    @Operation(summary = "Delete an ExecutionTemplate entity", description = "Deletes the execution template by its ID", tags = {})
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "successful deletion"),
            @ApiResponse(responseCode = "403", description = "forbidden"),
            @ApiResponse(responseCode = "404", description = "not found"),
            @ApiResponse(responseCode = "500", description = "unexpected error"),
            @ApiResponse(responseCode = "503", description = "Internal error")})
    @DeleteMapping(value = "/{executionId}", produces = {"application/json"})
    ResponseEntity<Void> deleteExecutionTemplate(@Parameter(description = "The ExecutionTemplate Id", required = true) @PathVariable("executionId") Long executionId);

    @Operation(summary = "Get an ExecutionTemplate entity by Id", description = "Returns a execution template by its ID", tags = {})
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "successful response, returns the execution template"),
            @ApiResponse(responseCode = "403", description = "forbidden"),
            @ApiResponse(responseCode = "404", description = "not found"),
            @ApiResponse(responseCode = "500", description = "unexpected error"),
            @ApiResponse(responseCode = "503", description = "Internal error")})
    @GetMapping(value = "/{executionId}", produces = "application/json")
    @PostAuthorize("hasRole('ADMIN') or (hasRole('EXPERT') and @datasetSecurityService.hasRightOnStudy(returnObject.getBody().getStudyId(), 'CAN_SEE_ALL'))")
    ResponseEntity<ExecutionTemplateDTO> getExecutionTemplateById(@Parameter(description = "The ExecutionTemplate Id", required = true) @PathVariable("executionId") Long executionId);

    @Operation(summary = "Update an ExecutionTemplate entity by Id", description = "Updates the existing execution template by its Id", tags={  })
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "successful update, returns the updated execution template"),
            @ApiResponse(responseCode = "403", description = "forbidden"),
            @ApiResponse(responseCode = "404", description = "not found"),
            @ApiResponse(responseCode = "500", description = "unexpected error"),
            @ApiResponse(responseCode = "503", description = "Internal error")})
    @PutMapping(value = "/{parameterId}", consumes = "application/json", produces = "application/json")
    @PreAuthorize("hasRole('ADMIN') or (hasRole('EXPERT') and @datasetSecurityService.hasRightOnStudy(#executionTemplateDTO.getStudyId(), 'CAN_ADMINISTRATE'))")
    ResponseEntity<ExecutionTemplateDTO> updateExecutionTemplate(
            @Parameter(description = "execution template updated", required = true) @RequestBody ExecutionTemplateDTO executionTemplateDTO,
            @Parameter(description = "id of the execution template", required = true) @PathVariable("parameterId") Long parameterId);
}
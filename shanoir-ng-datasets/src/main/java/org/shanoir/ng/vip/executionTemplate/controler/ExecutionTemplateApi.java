package org.shanoir.ng.vip.executionTemplate.controler;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.shanoir.ng.shared.exception.EntityNotFoundException;
import org.shanoir.ng.shared.exception.RestServiceException;
import org.shanoir.ng.shared.exception.SecurityException;
import org.shanoir.ng.vip.executionTemplate.dto.ExecutionTemplateDTO;
import org.shanoir.ng.vip.executionTemplate.model.ExecutionTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PostAuthorize;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import java.io.IOException;
import java.util.List;

/*
    Execution template are VIP execution automatically applied after an import, according to their configuration.
 */

@Tag(name = "Execution template", description = "the execution template API")
@RequestMapping("/executiontemplate")
public interface ExecutionTemplateApi {

    @Operation(summary = "Get list of existing execution templates for the given study_id", description = "Returns the list of existing execution templates for the given study id", tags = {})
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "successful response, returns the list of execution templates"),
            @ApiResponse(responseCode = "403", description = "forbidden"),
            @ApiResponse(responseCode = "500", description = "unexpected error"),
            @ApiResponse(responseCode = "503", description = "Internal error")})
    @GetMapping(value = "/byStudy/{studyId}",
            produces = {"application/json", "application/octet-stream"})
    @PreAuthorize("hasRole('ADMIN') or (hasRole('EXPERT') and @datasetSecurityService.hasRightOnStudy(#studyId, 'CAN_SEE_ALL'))")
    ResponseEntity<List<ExecutionTemplateDTO>> getExecutionTemplatesByStudyId(@Parameter(description = "The study Id", required = true) @PathVariable("studyId") Long studyId) throws IOException, RestServiceException, EntityNotFoundException, SecurityException;

    @Operation(summary = "Create a new ExecutionTemplate entity", description = "Creates a new execution template", tags = {})
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "successful creation"),
            @ApiResponse(responseCode = "403", description = "forbidden"),
            @ApiResponse(responseCode = "500", description = "unexpected error"),
            @ApiResponse(responseCode = "503", description = "Internal error")})
    @PostMapping(value = "", consumes = "application/json", produces = "application/json")
    @PreAuthorize("hasRole('ADMIN') or (hasRole('EXPERT') and @datasetSecurityService.hasRightOnStudy(#executionTemplate.getStudy(), 'CAN_ADMINISTRATE'))")
    ResponseEntity<ExecutionTemplateDTO> saveNewExecutionTemplate(@Parameter(description = "execution template to create", required = true) @RequestBody ExecutionTemplate executionTemplate) throws IOException, RestServiceException, SecurityException;

    @Operation(summary = "Delete a ExecutionTemplate entity", description = "Deletes the execution template by its ID", tags = {})
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "successful deletion"),
            @ApiResponse(responseCode = "403", description = "forbidden"),
            @ApiResponse(responseCode = "404", description = "not found"),
            @ApiResponse(responseCode = "500", description = "unexpected error"),
            @ApiResponse(responseCode = "503", description = "Internal error")})
    @DeleteMapping(value = "/{executionId}", produces = {"application/json"})
    ResponseEntity<Void> deleteExecutionTemplate(@Parameter(description = "The ExecutionTemplate Id", required = true) @PathVariable("executionId") Long executionId) throws IOException, RestServiceException, EntityNotFoundException, SecurityException;

    @Operation(summary = "Get a ExecutionTemplate entity by ID", description = "Returns a execution template by its ID", tags = {})
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "successful response, returns the execution template"),
            @ApiResponse(responseCode = "403", description = "forbidden"),
            @ApiResponse(responseCode = "404", description = "not found"),
            @ApiResponse(responseCode = "500", description = "unexpected error"),
            @ApiResponse(responseCode = "503", description = "Internal error")})
    @GetMapping(value = "/{executionId}", produces = "application/json")
    @PostAuthorize("hasRole('ADMIN') or (hasRole('EXPERT') and @datasetSecurityService.hasRightOnStudy(returnObject.getBody().getStudy(), 'CAN_SEE_ALL'))")
    ResponseEntity<ExecutionTemplateDTO> getExecutionTemplateById(@Parameter(description = "The ExecutionTemplate Id", required = true) @PathVariable("executionId") Long executionId) throws IOException, RestServiceException, EntityNotFoundException, SecurityException;

    @Operation(summary = "Update a ExecutionTemplate entity", description = "Updates the existing execution template by its ID", tags = {})
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "successful update, returns the updated execution template"),
            @ApiResponse(responseCode = "403", description = "forbidden"),
            @ApiResponse(responseCode = "404", description = "not found"),
            @ApiResponse(responseCode = "500", description = "unexpected error"),
            @ApiResponse(responseCode = "503", description = "Internal error")})
    @PutMapping(value = "/{executionId}", consumes = "application/json", produces = "application/json")
    @PreAuthorize("hasRole('ADMIN') or (hasRole('EXPERT') and @datasetSecurityService.hasRightOnStudy(#executionTemplate.getStudy(), 'CAN_ADMINISTRATE'))")
    ResponseEntity<ExecutionTemplateDTO> updateExecutionTemplate(
            @Parameter(description = "id of the execution template", required = true) @PathVariable("executionId") Long executionId,
            @Parameter(description = "center to update", required = true) @RequestBody ExecutionTemplateDTO executionTemplate, BindingResult result);
}
package org.shanoir.ng.vip.executionTemplate.controler;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.shanoir.ng.shared.exception.EntityNotFoundException;
import org.shanoir.ng.shared.exception.RestServiceException;
import org.shanoir.ng.shared.exception.SecurityException;
import org.shanoir.ng.vip.executionTemplate.dto.ExecutionTemplateParameterDTO;
import org.shanoir.ng.vip.executionTemplate.model.ExecutionTemplateParameter;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PostAuthorize;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;

/*
    Execution template parameter are parameters of VIP execution automatically applied after an import.
 */
@Tag(name = "Execution template parameter", description = "the execution template parameters API")
@RequestMapping("/vip/execution-template/execution-template-parameter")
public interface ExecutionTemplateParameterApi {

    @Operation(summary = "Get list of existing execution templates parameters for the given execution_template_id", description = "Returns the list of existing execution template parameters for the given execution template id", tags={  })
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "successful response, returns the list of execution template parameters"),
            @ApiResponse(responseCode = "403", description = "forbidden"),
            @ApiResponse(responseCode = "500", description = "unexpected error"),
            @ApiResponse(responseCode = "503", description = "Internal error")})
    @GetMapping(value = "/byExecutionTemplate/{executionTemplateId}",
            produces = { "application/json", "application/octet-stream" })
    @PreAuthorize("hasRole('ADMIN') or (hasRole('EXPERT') and @datasetSecurityService.hasRightOnStudy(#studyId, 'CAN_SEE_ALL'))")
    ResponseEntity<List<ExecutionTemplateParameterDTO>> getExecutionTemplateParametersByExecutionTemplateId(@Parameter(description = "The execution template Id", required=true) @PathVariable("executionTemplateId") Long executionTemplateId) throws IOException, RestServiceException, EntityNotFoundException, SecurityException;

    @Operation(summary = "Create a new ExecutionTemplateParameter entity", description = "Creates a new execution template parameter", tags={  })
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "successful creation"),
            @ApiResponse(responseCode = "403", description = "forbidden"),
            @ApiResponse(responseCode = "500", description = "unexpected error"),
            @ApiResponse(responseCode = "503", description = "Internal error")})
    @PostMapping(value = "", consumes = "application/json", produces = "application/json")
    @PreAuthorize("hasRole('ADMIN') or (hasRole('EXPERT') and @datasetSecurityService.hasRightOnStudy(#executionTemplate.getStudy(), 'CAN_ADMINISTRATE'))")
    ResponseEntity<ExecutionTemplateParameterDTO> saveNewExecutionTemplateParameter(@Parameter(description = "execution template parameter to create", required = true) @RequestBody ExecutionTemplateParameter executionTemplateParameter) throws IOException, RestServiceException, SecurityException;

    @Operation(summary = "Delete a ExecutionTemplateParameter entity", description = "Deletes the execution template parameter by its ID", tags={  })
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "successful deletion"),
            @ApiResponse(responseCode = "403", description = "forbidden"),
            @ApiResponse(responseCode = "404", description = "not found"),
            @ApiResponse(responseCode = "500", description = "unexpected error"),
            @ApiResponse(responseCode = "503", description = "Internal error")})
    @DeleteMapping(value = "/{executionTemplateParameterId}", produces = { "application/json" })
    ResponseEntity<Void> deleteExecutionTemplateParameter(@Parameter(description = "The ExecutionTemplateParameter Id", required=true) @PathVariable("executionTemplateParameterId") Long executionTemplateParameterId) throws IOException, RestServiceException, EntityNotFoundException, SecurityException;

    @Operation(summary = "Get a ExecutionTemplateParameter entity by ID", description = "Returns a execution template parameter by its ID", tags={  })
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "successful response, returns the execution template parameter"),
            @ApiResponse(responseCode = "403", description = "forbidden"),
            @ApiResponse(responseCode = "404", description = "not found"),
            @ApiResponse(responseCode = "500", description = "unexpected error"),
            @ApiResponse(responseCode = "503", description = "Internal error")})
    @GetMapping(value = "/{executionTemplateParameterId}", produces = "application/json")
    @PostAuthorize("hasRole('ADMIN') or (hasRole('EXPERT') and @datasetSecurityService.hasRightOnStudy(returnObject.getBody().getStudy(), 'CAN_SEE_ALL'))")
    ResponseEntity<ExecutionTemplateParameterDTO> getExecutionTemplateParameterById(@Parameter(description = "The ExecutionTemplateParameter Id", required=true) @PathVariable("executionTemplateParameterId") Long executionTemplateParameterId) throws IOException, RestServiceException, EntityNotFoundException, SecurityException;

    @Operation(summary = "Update a ExecutionTemplateParameter entity", description = "Updates the existing execution template parameter by its ID", tags={  })
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "successful update, returns the updated execution template parameter"),
            @ApiResponse(responseCode = "403", description = "forbidden"),
            @ApiResponse(responseCode = "404", description = "not found"),
            @ApiResponse(responseCode = "500", description = "unexpected error"),
            @ApiResponse(responseCode = "503", description = "Internal error")})
    @PutMapping(value = "/{executionTemplateParameterId}", consumes = "application/json", produces = "application/json")
    @PreAuthorize("hasRole('ADMIN') or (hasRole('EXPERT') and @datasetSecurityService.hasRightOnStudy(#executionTemplate.getStudy(), 'CAN_ADMINISTRATE'))")
    ResponseEntity<ExecutionTemplateParameterDTO> updateExecutionTemplateParameter(
            @Parameter(description = "id of the execution template parameter", required = true) @PathVariable("executionTemplateParameterId") Long executionTemplateParameterId,
            @Parameter(description = "center to update", required = true) @RequestBody ExecutionTemplateParameterDTO executionTemplateParameter, BindingResult result)
            throws IOException, RestServiceException, EntityNotFoundException, SecurityException;

}

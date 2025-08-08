package org.shanoir.ng.vip.executionTemplate.controler;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

import org.shanoir.ng.shared.exception.EntityNotFoundException;
import org.shanoir.ng.shared.exception.RestServiceException;
import org.shanoir.ng.shared.exception.SecurityException;
import org.shanoir.ng.vip.executionTemplate.dto.ExecutionTemplateFilterDTO;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PostAuthorize;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;

/*
    Execution template filter for VIP execution automatically used after an import.
 */
@Tag(name = "Execution template filter", description = "the execution template filters API")
@RequestMapping("/execution-template-filter")
public interface ExecutionTemplateFilterApi {

    @Operation(summary = "Get list of existing execution templates filters for the given execution_template_id", description = "Returns the list of existing execution template filters for the given execution template id", tags={  })
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "successful response, returns the list of execution template filters"),
            @ApiResponse(responseCode = "403", description = "forbidden"),
            @ApiResponse(responseCode = "500", description = "unexpected error"),
            @ApiResponse(responseCode = "503", description = "Internal error")})
    @GetMapping(value = "/byExecutionTemplate/{executionTemplateId}",
            produces = { "application/json", "application/octet-stream" })
    @PreAuthorize("hasRole('ADMIN') or (hasRole('EXPERT') and @datasetSecurityService.hasRightOnStudy(#studyId, 'CAN_SEE_ALL'))")
    ResponseEntity<List<ExecutionTemplateFilterDTO>> getExecutionTemplateFiltersByExecutionTemplateId(@Parameter(description = "The execution template Id", required=true) @PathVariable("executionTemplateId") Long executionTemplateId);

    @Operation(summary = "Create a new ExecutionTemplateFilter entity", description = "Creates a new execution template filter", tags={  })
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "successful creation"),
            @ApiResponse(responseCode = "403", description = "forbidden"),
            @ApiResponse(responseCode = "500", description = "unexpected error"),
            @ApiResponse(responseCode = "503", description = "Internal error")})
    @PostMapping(value = "", consumes = "application/json", produces = "application/json")
    @PreAuthorize("hasRole('ADMIN') or (hasRole('EXPERT') and @datasetSecurityService.hasRightOnStudy(#executionTemplate.getStudy(), 'CAN_ADMINISTRATE'))")
    ResponseEntity<ExecutionTemplateFilterDTO> saveNewExecutionTemplateFilter(@Parameter(description = "execution template filter to create", required = true) @RequestBody ExecutionTemplateFilterDTO executionTemplateFilterDTO);

    @Operation(summary = "Delete a ExecutionTemplateFilter entity", description = "Deletes the execution template filter by its ID", tags={  })
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "successful deletion"),
            @ApiResponse(responseCode = "403", description = "forbidden"),
            @ApiResponse(responseCode = "404", description = "not found"),
            @ApiResponse(responseCode = "500", description = "unexpected error"),
            @ApiResponse(responseCode = "503", description = "Internal error")})
    @DeleteMapping(value = "/{executionTemplateFilterId}", produces = { "application/json" })
    ResponseEntity<Void> deleteExecutionTemplateFilter(@Parameter(description = "The ExecutionTemplateFilter Id", required=true) @PathVariable("executionTemplateFilterId") Long executionTemplateFilterId);

    @Operation(summary = "Get a ExecutionTemplateFilter entity by ID", description = "Returns a execution template filter by its ID", tags={  })
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "successful response, returns the execution template filter"),
            @ApiResponse(responseCode = "403", description = "forbidden"),
            @ApiResponse(responseCode = "404", description = "not found"),
            @ApiResponse(responseCode = "500", description = "unexpected error"),
            @ApiResponse(responseCode = "503", description = "Internal error")})
    @GetMapping(value = "/{executionTemplateFilterId}", produces = "application/json")
    @PostAuthorize("hasRole('ADMIN') or (hasRole('EXPERT') and @datasetSecurityService.hasRightOnStudy(returnObject.getBody().getStudy(), 'CAN_SEE_ALL'))")
    ResponseEntity<ExecutionTemplateFilterDTO> getExecutionTemplateFilterById(@Parameter(description = "The ExecutionTemplateFilter Id", required=true) @PathVariable("executionTemplateFilterId") Long executionTemplateFilterId);

    @Operation(summary = "Update a ExecutionTemplateFilter entity", description = "Updates the existing execution template filter by its ID", tags={  })
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "successful update, returns the updated execution template filter"),
            @ApiResponse(responseCode = "403", description = "forbidden"),
            @ApiResponse(responseCode = "404", description = "not found"),
            @ApiResponse(responseCode = "500", description = "unexpected error"),
            @ApiResponse(responseCode = "503", description = "Internal error")})
    @PutMapping(value = "/{executionTemplateFilterId}", consumes = "application/json", produces = "application/json")
    @PreAuthorize("hasRole('ADMIN') or (hasRole('EXPERT') and @datasetSecurityService.hasRightOnStudy(#executionTemplate.getStudy(), 'CAN_ADMINISTRATE'))")
    ResponseEntity<ExecutionTemplateFilterDTO> updateExecutionTemplateFilter(
            @Parameter(description = "id of the execution template filter", required = true) @PathVariable("executionTemplateFilterId") Long executionTemplateFilterId,
            @Parameter(description = "execution template filter updated", required = true) @RequestBody ExecutionTemplateFilterDTO executionTemplateFilter)
            throws IOException, RestServiceException, EntityNotFoundException, SecurityException;
}

package org.shanoir.ng.vip.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.shanoir.ng.shared.exception.SecurityException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@Tag(name = "pipeline")
@RequestMapping("/vip/pipeline")
public interface PipelineApi {

    @Operation(summary = "Get execution DTO for the given identifier", description = "Returns the VIP execution that has the given identifier in parameter.", tags={  })
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "successful response, returns the status"),
            @ApiResponse(responseCode = "403", description = "forbidden"),
            @ApiResponse(responseCode = "500", description = "unexpected error") })
    @RequestMapping(value = "/{identifier}",
            produces = { "application/json", "application/octet-stream" },
            method = RequestMethod.GET)
    ResponseEntity<String> getPipeline(@Parameter(name = "The pipeline identifier", required=true) @PathVariable("identifier") String identifier) throws SecurityException;

    @Operation(summary = "Get execution DTO for the given identifier", description = "Returns the VIP execution that has the given identifier in parameter.", tags={  })
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "successful response, returns the status"),
            @ApiResponse(responseCode = "403", description = "forbidden"),
            @ApiResponse(responseCode = "500", description = "unexpected error") })
    @RequestMapping(value = "/all",
            produces = { "application/json", "application/octet-stream" },
            method = RequestMethod.GET)
    ResponseEntity<String> getPipelineAll() throws SecurityException;


}

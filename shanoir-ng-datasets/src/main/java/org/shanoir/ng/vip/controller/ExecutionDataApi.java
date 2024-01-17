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

package org.shanoir.ng.vip.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.shanoir.ng.shared.exception.EntityNotFoundException;
import org.shanoir.ng.shared.exception.RestServiceException;
import org.shanoir.ng.shared.exception.SecurityException;
import org.shanoir.ng.vip.monitoring.model.ExecutionDTO;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;

/**
 * @author Alae Es-saki
 */
@Tag(name = "carmin-data")
@RequestMapping("/carmin-data")
public interface ExecutionDataApi {

    @Operation(summary = "Get content or information for a given path", description = "Download a file (or a directory) or retun information about a specific path. The response format and content depends on the mandatory action query parameter (see the parameter description). Basically, the \"content\" action downloads the raw file, and the other actions return various information in a JSON record.", tags={  })
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "successful response. If the action is \"content\", the raw file (or a tarball) is returned, with the according mime type. Otherwise a json response a returned"),
            @ApiResponse(responseCode = "403", description = "forbidden"),
            @ApiResponse(responseCode = "404", description = "No dataset found"),
            @ApiResponse(responseCode = "500", description = "unexpected error") })
    @RequestMapping(value = "/path/{completePath}",
            produces = { "application/json", "application/octet-stream" },
            method = RequestMethod.GET)
    ResponseEntity<?> getPath(@Parameter(name = "the complete path on which to request information. It can contain non-encoded slashes. Except for the \"exists\" action, any request on a non-existing path should return an error", required=true) @PathVariable("completePath") String completePath, @NotNull @Parameter(name = "The \"content\" action downloads the raw file. If the path points to a directory, a tarball of this directory is returned. The \"exists\" action returns a BooleanResponse object (see definition) indicating if the path exists or not. The \"properties\" action returns a Path object (see definition) with the path properties. The \"list\" action returns a DirectoryList object (see definition) with the properties of all the files of the directory (if the path is not a directory an error must be returned). The \"md5\" action is optional and returns a PathMd5 object (see definition)." ,required=true
    ) @Valid @RequestParam(value = "action", required = true) String action, @Valid @RequestParam(value = "format", required = false, defaultValue = "dcm") final String format, HttpServletResponse response) throws IOException, RestServiceException, EntityNotFoundException;

    @Operation(summary = "Creates an execution inside VIP", description = "Creates the ressources and path control in shanoir before creating an execution in VIP", tags={  })
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Execution successfully created response."),
            @ApiResponse(responseCode = "403", description = "forbidden"),
            @ApiResponse(responseCode = "500", description = "unexpected error") })
    @RequestMapping(value = "/createExecution",
            produces = {"application/json"},
            consumes = {"application/json"},
            method = RequestMethod.POST)
    ResponseEntity<?> createExecution(
            @Parameter(name = "execution", required = true) @RequestBody final String execution) throws EntityNotFoundException, SecurityException;

    @Operation(summary = "Get status for the given execution identifier", description = "Returns the status of the VIP execution that has the given identifier in parameter.", tags={  })
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "successful response, returns the status"),
            @ApiResponse(responseCode = "403", description = "forbidden"),
            @ApiResponse(responseCode = "500", description = "unexpected error") })
    @RequestMapping(value = "/execution/{identifier}",
            produces = { "application/json", "application/octet-stream" },
            method = RequestMethod.GET)
    ResponseEntity<String> getexecutionStatus(@Parameter(name = "The execution identifier", required=true) @PathVariable("identifier") String identifier) throws IOException, RestServiceException, EntityNotFoundException, SecurityException;

}

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

package org.shanoir.ng.preclinical.contrast_agent;

import java.util.List;

import org.shanoir.ng.shared.exception.RestServiceException;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;

@Tag(name = "contrast_agent")
@RequestMapping("/protocol/{pid}/contrastagent")
public interface ContrastAgentApi {

    @Operation(summary = "Add a new contrast agent", description = "")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "success returns Contrast Agent"),
        @ApiResponse(responseCode = "400", description = "Invalid input / Bad Request"),
        @ApiResponse(responseCode = "409", description = "Already exists - conflict"),
        @ApiResponse(responseCode = "500", description = "Unexpected Error") })
    @PostMapping(value = "",
        produces = { "application/json" },
        consumes = { "application/json" })
    ResponseEntity<ContrastAgent> createContrastAgent(@Parameter(name = "protocol id", required = true ) @PathVariable("pid") Long pid,
            @Parameter(name = "Contrast Agent to create", required = true) @RequestBody ContrastAgent contrastagent,
    BindingResult result) throws RestServiceException;


    @Operation(summary = "Deletes a ContrastAgent", description = "")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successful operation"),
        @ApiResponse(responseCode = "400", description = "Invalid ContrastAgent id"),
        @ApiResponse(responseCode = "500", description = "Unexpected Error") })
    @DeleteMapping(value = "/{cid}",
        produces = { "application/json" })
    ResponseEntity<Void> deleteContrastAgent(@Parameter(name = "protocol id", required = true ) @PathVariable("pid") Long pid,
            @Parameter(name = "Contrast Agent id", required = true ) @PathVariable("cid") Long cid);



    @Operation(summary = "Get Contrast Agent by protocol id", description = "")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "A Contrast Agent"),
        @ApiResponse(responseCode = "404", description = "Contrast Agent not found"),
        @ApiResponse(responseCode = "500", description = "Unexpected error") })
    @GetMapping(value = "",
        produces = { "application/json" })
    ResponseEntity<ContrastAgent> getContrastAgentByProtocolId(@Parameter(name = "protocol id", required = true ) @PathVariable("pid") Long pid);

    @Operation(summary = "Get Contrast Agent by id", description = "")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "A Contrast Agent"),
        @ApiResponse(responseCode = "404", description = "Contrast Agent not found"),
        @ApiResponse(responseCode = "500", description = "Unexpected error") })
    @GetMapping(value = "/{cid}",
        produces = { "application/json" })
    ResponseEntity<ContrastAgent> getContrastAgentById(@Parameter(name = "protocol id", required = true ) @PathVariable("pid") Long pid,
            @Parameter(name = "Contrast Agent id", required = true ) @PathVariable("cid") Long cid);

    @Operation(summary = "Get Contrast Agent by name", description = "")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "A Contrast Agent"),
        @ApiResponse(responseCode = "404", description = "Contrast Agent not found"),
        @ApiResponse(responseCode = "500", description = "Unexpected error") })
    @GetMapping(value = "/name/{name}",
        produces = { "application/json" })
    ResponseEntity<ContrastAgent> getContrastAgentByName(@Parameter(name = "protocol id", required = true ) @PathVariable("pid") Long pid,
            @Parameter(name = "Contrast Agent id", required = true ) @PathVariable("name") String name);


    @Operation(summary = "List all contrast agents", description = "")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "An array of contrast agents"),
        @ApiResponse(responseCode = "500", description = "Unexpected error") })
    @GetMapping(value = "/all",
        produces = { "application/json" })
    ResponseEntity<List<ContrastAgent>> getContrastAgents(@Parameter(name = "protocol id", required = true ) @PathVariable("pid") Long pid);


    @Operation(summary = "Update an existing contrast agent", description = "")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successful operation"),
        @ApiResponse(responseCode = "400", description = "Invalid input / Bad Request"),
        @ApiResponse(responseCode = "404", description = "Contrast Agent not found"),
        @ApiResponse(responseCode = "500", description = "Unexpected Error") })
    @PutMapping(value = "/{cid}",
        produces = { "application/json" },
        consumes = { "application/json" })
    ResponseEntity<Void> updateContrastAgent(@Parameter(name = "protocol id", required = true ) @PathVariable("pid") Long pid,
            @Parameter(name = "ID of contrast agent that needs to be updated", required = true ) @PathVariable("cid") Long cid,
        @Parameter(name = "Contrast Agent object that needs to be updated", required = true ) @RequestBody ContrastAgent contrastagent,
        final BindingResult result) throws RestServiceException;

}

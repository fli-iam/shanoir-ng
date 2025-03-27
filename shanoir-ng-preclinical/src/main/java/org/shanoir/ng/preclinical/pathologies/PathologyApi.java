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

package org.shanoir.ng.preclinical.pathologies;

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

@Tag(name = "pathology")
@RequestMapping("/pathology")
public interface PathologyApi {

    @Operation(summary = "Add a new pathology", description = "")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "success returns Pathology"),
        @ApiResponse(responseCode = "400", description = "Invalid input / Bad Request"),
        @ApiResponse(responseCode = "409", description = "Already exists - conflict"),
        @ApiResponse(responseCode = "500", description = "Unexpected Error") })
    @PostMapping(value = "",
        produces = { "application/json" },
        consumes = { "application/json" })
    ResponseEntity<Pathology> createPathology(@Parameter(name = "pathology to create", required = true) @RequestBody Pathology pathology,
    BindingResult result) throws RestServiceException;


    @Operation(summary = "Deletes a pathology value", description = "")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successful operation"),
        @ApiResponse(responseCode = "400", description = "Invalid pathology id"),
        @ApiResponse(responseCode = "500", description = "Unexpected Error") })
    @DeleteMapping(value = "/{id}",
        produces = { "application/json" })
    ResponseEntity<Void> deletePathology(@Parameter(name = "pathology id",required = true ) @PathVariable("id") Long id);


    @Operation(summary = "Get Pathology", description = "")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "A pathology "),
        @ApiResponse(responseCode = "404", description = "Pathology not found"),
        @ApiResponse(responseCode = "500", description = "Unexpected error") })
    @GetMapping(value = "/{id}",
        produces = { "application/json" })
    ResponseEntity<Pathology> getPathologyById(@Parameter(name = "Pathology id",required = true ) @PathVariable("id") Long id);


    @Operation(summary = "List all pathologies", description = "")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "An array of pathologies"),
        @ApiResponse(responseCode = "500", description = "Unexpected error") })
    @GetMapping(value = "",
        produces = { "application/json" })
    ResponseEntity<List<Pathology>> getPathologies();


    @Operation(summary = "Update an existing pathology", description = "")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successful operation"),
        @ApiResponse(responseCode = "400", description = "Invalid input / Bad Request"),
        @ApiResponse(responseCode = "404", description = "Pathology not found"),
        @ApiResponse(responseCode = "500", description = "Unexpected Error") })
    @PutMapping(value = "/{id}",
        produces = { "application/json" },
        consumes = { "application/json" })
    ResponseEntity<Void> updatePathology(@Parameter(name = "ID of pathology that needs to be updated",required = true ) @PathVariable("id") Long id,
        @Parameter(name = "Pathology object that needs to be updated" ,required = true ) @RequestBody Pathology pathology,
        final BindingResult result) throws RestServiceException;

}

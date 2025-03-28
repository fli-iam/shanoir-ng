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

package org.shanoir.ng.preclinical.anesthetics.anesthetic;

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

@Tag(name = "anesthetic")
@RequestMapping("/anesthetic")
public interface AnestheticApi {

    @Operation(summary = "Add a new anesthetic", description = "")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "success returns Anesthetic"),
        @ApiResponse(responseCode = "400", description = "Invalid input / Bad Request"),
        @ApiResponse(responseCode = "409", description = "Already exists - conflict"),
        @ApiResponse(responseCode = "500", description = "Unexpected Error") })
    @PostMapping(value = "",
        produces = { "application/json" },
        consumes = { "application/json" })
    ResponseEntity<Anesthetic> createAnesthetic(@Parameter(name = "anesthetic to create", required = true) @RequestBody Anesthetic anesthetic,
    BindingResult result) throws RestServiceException;


    @Operation(summary = "Deletes an Anesthetic", description = "")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successful operation"),
        @ApiResponse(responseCode = "400", description = "Invalid Anesthetic id"),
        @ApiResponse(responseCode = "500", description = "Unexpected Error") })
    @DeleteMapping(value = "/{id}",
        produces = { "application/json" })
    ResponseEntity<Void> deleteAnesthetic(@Parameter(name = "Anesthetic id", required = true ) @PathVariable("id") Long id);


    @Operation(summary = "Get Anesthetic by id", description = "")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "An Anesthetic"),
        @ApiResponse(responseCode = "404", description = "Anesthetic not found"),
        @ApiResponse(responseCode = "500", description = "Unexpected error") })
    @GetMapping(value = "/{id}",
        produces = { "application/json" })
    ResponseEntity<Anesthetic> getAnestheticById(@Parameter(name = "Anesthetic id", required = true ) @PathVariable("id") Long id);


    @Operation(summary = "List all anesthetics", description = "")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "An array of anesthetics"),
        @ApiResponse(responseCode = "500", description = "Unexpected error") })
    @GetMapping(value = "",
        produces = { "application/json" })
    ResponseEntity<List<Anesthetic>> getAnesthetics();

    @Operation(summary = "List all anesthetics by type", description = "")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "An array of anesthetics"),
        @ApiResponse(responseCode = "500", description = "Unexpected error") })
    @GetMapping(value = "/type/{type}",
        produces = { "application/json" })
    ResponseEntity<List<Anesthetic>> getAnestheticsByType(@Parameter(name = "Anesthetic type ", required = true ) @PathVariable("type") String type);


    @Operation(summary = "Update an existing anesthetic", description = "")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successful operation"),
        @ApiResponse(responseCode = "400", description = "Invalid input / Bad Request"),
        @ApiResponse(responseCode = "404", description = "Anesthetic not found"),
        @ApiResponse(responseCode = "500", description = "Unexpected Error") })
    @PutMapping(value = "/{id}",
        produces = { "application/json" },
        consumes = { "application/json" })
    ResponseEntity<Void> updateAnesthetic(@Parameter(name = "ID of Anesthetic that needs to be updated", required = true ) @PathVariable("id") Long id,
        @Parameter(name = "Anesthetic object that needs to be updated", required = true ) @RequestBody Anesthetic anesthetic,
        final BindingResult result) throws RestServiceException;

}

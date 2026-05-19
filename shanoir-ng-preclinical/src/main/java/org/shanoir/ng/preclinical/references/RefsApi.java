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

package org.shanoir.ng.preclinical.references;

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

@Tag(name = "refs")
@RequestMapping("/refs")
public interface RefsApi {

    @Operation(summary = "Add a new Reference", description = "")
    @ApiResponses(value = { @ApiResponse(responseCode = "200", description = "success returns Reference"),
            @ApiResponse(responseCode = "400", description = "Invalid input / Bad Request"),
            @ApiResponse(responseCode = "500", description = "Unexpected Error") })
    @PostMapping(value = "", produces = { "application/json" }, consumes = {
            "application/json" })
    ResponseEntity<Reference> createReferenceValue(
            @Parameter(name = "Ref to add", required = true) @RequestBody Reference body) throws RestServiceException;

    @Operation(summary = "Deletes a reference", description = "")
    @ApiResponses(value = { @ApiResponse(responseCode = "200", description = "Successful operation"),
            @ApiResponse(responseCode = "400", description = "Not found"),
            @ApiResponse(responseCode = "406", description = "Not acceptable"),
            @ApiResponse(responseCode = "500", description = "Unexpected Error") })
    @DeleteMapping(value = "/{id}", produces = { "application/json" })
    ResponseEntity<Void> deleteReferenceValue(
            @Parameter(name = "id of reference to be deleted", required = true) @PathVariable("id") Long id);

    @Operation(summary = "Get existing references by category", description = "")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "An array of references values according to category parameter"),
            @ApiResponse(responseCode = "204", description = "No content"),
            @ApiResponse(responseCode = "500", description = "Unexpected error") })
    @GetMapping(value = "/category/{category}", produces = { "application/json" })
    ResponseEntity<List<Reference>> getReferencesByCategory(
            @Parameter(name = "Category of the references", required = true) @PathVariable("category") String category);

    @Operation(summary = "Get all references for given category and type", description = "")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "An array of references values according to category and type parameter"),
            @ApiResponse(responseCode = "204", description = "No content"),
            @ApiResponse(responseCode = "500", description = "Unexpected error") })
    @GetMapping(value = "/category/{category}/{type}", produces = {
            "application/json" })
    ResponseEntity<List<Reference>> getReferencesByCategoryAndType(
            @Parameter(name = "Category of the reference", required = true) @PathVariable("category") String category,
            @Parameter(name = "Type of the reference", required = true) @PathVariable("type") String type);

    @Operation(summary = "References categories", description = "")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "An array of ref elements names"),
            @ApiResponse(responseCode = "500", description = "Unexpected error") })
    @GetMapping(value = "/categories", produces = { "application/json" })
    ResponseEntity<List<String>> getReferenceCategories();

    @Operation(summary = "References types for given category", description = "")
    @ApiResponses(value = { @ApiResponse(responseCode = "200", description = "An array of ref types names"),
            @ApiResponse(responseCode = "500", description = "Unexpected error") })
    @GetMapping(value = "/category/{category}/types", produces = { "application/json" })
    ResponseEntity<List<String>> getReferenceTypesByCategory(
            @Parameter(name = "Category of the reference", required = true) @PathVariable("category") String category);

    @Operation(summary = "Get all references", description = "")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "An array of all references"),
            @ApiResponse(responseCode = "500", description = "Unexpected error") })
    @GetMapping(value = "", produces = { "application/json" })
    ResponseEntity<List<Reference>> getReferences();

    @Operation(summary = "Find reference by its id", description = "Returns a reference")
    @ApiResponses(value = { @ApiResponse(responseCode = "200", description = "Successful operation"),
            @ApiResponse(responseCode = "404", description = "Reference not found"),
            @ApiResponse(responseCode = "500", description = "Unexpected Error") })
    @GetMapping(value = "/{id}", produces = { "application/json" })
    ResponseEntity<Reference> getReferenceById(
            @Parameter(name = "Id of the reference", required = true) @PathVariable("id") Long id);

    @Operation(summary = "Find reference by category, type and value", description = "Returns a reference")
    @ApiResponses(value = { @ApiResponse(responseCode = "200", description = "Successful operation"),
            @ApiResponse(responseCode = "404", description = "Reference not found"),
            @ApiResponse(responseCode = "500", description = "Unexpected Error") })
    @GetMapping(value = "/category/{category}/{type}/{value}", produces = {
            "application/json" })
    ResponseEntity<Reference> getReferenceByCategoryTypeAndValue(
            @Parameter(name = "Category of the reference", required = true) @PathVariable("category") String category,
            @Parameter(name = "type of reference", required = true) @PathVariable("type") String type,
            @Parameter(name = "value of reference", required = true) @PathVariable("value") String value);

    @Operation(summary = "Update an existing Ref ", description = "")
    @ApiResponses(value = { @ApiResponse(responseCode = "200", description = "Successful operation"),
            @ApiResponse(responseCode = "400", description = "Invalid input / Bad Request"),
            @ApiResponse(responseCode = "404", description = "Reference value not found"),
            @ApiResponse(responseCode = "500", description = "Unexpected Error") })
    @PutMapping(value = "/{id}", produces = { "application/json" }, consumes = {
            "application/json" })
    ResponseEntity<Void> updateReferenceValue(
            @Parameter(name = "value of ref name to be updated", required = true) @PathVariable("id") Long id,
            @Parameter(name = "New value for the ref given as name", required = true) @RequestBody Reference body,
            BindingResult result) throws RestServiceException;

}

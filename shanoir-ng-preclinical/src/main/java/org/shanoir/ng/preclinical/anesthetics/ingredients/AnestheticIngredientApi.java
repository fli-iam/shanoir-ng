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

package org.shanoir.ng.preclinical.anesthetics.ingredients;

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

@Tag(name = "ingredients")
@RequestMapping("/anesthetic/{id}/ingredient")
public interface AnestheticIngredientApi {

    @Operation(summary = "Add a new ingredient", description = "")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "success returns Ingredient"),
        @ApiResponse(responseCode = "400", description = "Invalid input / Bad Request"),
        @ApiResponse(responseCode = "404", description = "Anesthetic not found"),
        @ApiResponse(responseCode = "409", description = "Already exists - conflict"),
        @ApiResponse(responseCode = "500", description = "Unexpected Error") })
    @PostMapping(value = "",
        produces = { "application/json" },
        consumes = { "application/json" })
    ResponseEntity<AnestheticIngredient> createAnestheticIngredient(@Parameter(name = "anesthetic id", required = true ) @PathVariable("id") Long id,
            @Parameter(name = "anesthetic ingredient to create", required = true) @RequestBody AnestheticIngredient ingredient,
    BindingResult result) throws RestServiceException;


    @Operation(summary = "Deletes an ingredient", description = "")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successful operation"),
        @ApiResponse(responseCode = "400", description = "Invalid Anesthetic Ingredient id"),
        @ApiResponse(responseCode = "404", description = "Anesthetic not found"),
        @ApiResponse(responseCode = "500", description = "Unexpected Error") })
    @DeleteMapping(value = "/{aiid}",
        produces = { "application/json" })
    ResponseEntity<Void> deleteAnestheticIngredient(@Parameter(name = "anesthetic id", required = true ) @PathVariable("id") Long id,
            @Parameter(name = "Anesthetic Ingredient id", required = true ) @PathVariable("aiid") Long aiid);


    @Operation(summary = "Get Anesthetic Ingredient by id", description = "")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "An Anesthetic Ingredient  "),
        @ApiResponse(responseCode = "404", description = "Anesthetic Ingredient not found"),
        @ApiResponse(responseCode = "500", description = "Unexpected error") })
    @GetMapping(value = "/{aiid}",
        produces = { "application/json" })
    ResponseEntity<AnestheticIngredient> getAnestheticIngredientById(@Parameter(name = "anesthetic id", required = true ) @PathVariable("id") Long id,
            @Parameter(name = "Anesthetic Ingredient id", required = true ) @PathVariable("id") Long aiid);


    @Operation(summary = "List all anesthetic ingredients for given anesthetic", description = "")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "An array of anesthetic ingredients"),
        @ApiResponse(responseCode = "200", description = "Unexpected error"),
        @ApiResponse(responseCode = "404", description = "Anesthetic not found"),
        @ApiResponse(responseCode = "500", description = "Unexpected error") })
    @GetMapping(value = "/all",
        produces = { "application/json" })
    ResponseEntity<List<AnestheticIngredient>> getAnestheticIngredients(@Parameter(name = "anesthetic id", required = true ) @PathVariable("id") Long id)  throws RestServiceException ;


    @Operation(summary = "Update an existing anesthetic ingredient", description = "")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successful operation"),
        @ApiResponse(responseCode = "400", description = "Invalid input / Bad Request"),
        @ApiResponse(responseCode = "404", description = "Anesthetic Ingredient not found"),
        @ApiResponse(responseCode = "500", description = "Unexpected Error") })
    @PutMapping(value = "/{aiid}",
        produces = { "application/json" },
        consumes = { "application/json" })
    ResponseEntity<Void> updateAnestheticIngredient(@Parameter(name = "anesthetic id", required = true ) @PathVariable("id") Long id,
            @Parameter(name = "ID of Anesthetic Ingredient that needs to be updated", required = true ) @PathVariable("aiid") Long aiid,
        @Parameter(name = "Anesthetic Ingredient object that needs to be updated", required = true ) @RequestBody AnestheticIngredient ingredient,
        final BindingResult result) throws RestServiceException;

}

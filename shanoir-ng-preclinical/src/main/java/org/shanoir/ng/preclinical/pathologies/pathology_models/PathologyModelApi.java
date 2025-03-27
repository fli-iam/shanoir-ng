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

package org.shanoir.ng.preclinical.pathologies.pathology_models;

import java.util.List;

import org.shanoir.ng.shared.exception.RestServiceException;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;

@Tag(name = "pathology_model")
public interface PathologyModelApi {

    @Operation(summary = "Add a new pathology model", description = "")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "success returns Pathology model"),
        @ApiResponse(responseCode = "400", description = "Invalid input / Bad Request"),
        @ApiResponse(responseCode = "409", description = "Already exists - conflict"),
        @ApiResponse(responseCode = "500", description = "Unexpected Error") })
    @PostMapping(value = "/pathology/model",
        produces = { "application/json" },
        consumes = { "application/json" })
    ResponseEntity<PathologyModel> createPathologyModel(@Parameter(name = "pathology model to create", required = true) @RequestBody PathologyModel model,
    BindingResult result) throws RestServiceException;


    @Operation(summary = "Deletes a pathology model", description = "")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successful operation"),
        @ApiResponse(responseCode = "400", description = "Invalid pathology model id"),
        @ApiResponse(responseCode = "500", description = "Unexpected Error") })
    @DeleteMapping(value = "/pathology/model/{id}",
        produces = { "application/json" })
    ResponseEntity<Void> deletePathologyModel(@Parameter(name = "pathology model id",required = true ) @PathVariable("id") Long id);


    @Operation(summary = "Get Pathology model", description = "")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "A pathology model"),
        @ApiResponse(responseCode = "404", description = "Pathology model not found"),
        @ApiResponse(responseCode = "500", description = "Unexpected error") })
    @GetMapping(value = "/pathology/model/{id}",
        produces = { "application/json" })
    ResponseEntity<PathologyModel> getPathologyModelById(@Parameter(name = "Pathology model id",required = true ) @PathVariable("id") Long id);


    @Operation(summary = "List all pathologies models", description = "")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "An array of pathology models"),
        @ApiResponse(responseCode = "500", description = "Unexpected error") })
    @GetMapping(value = "/pathology/model",
        produces = { "application/json" })
    ResponseEntity<List<PathologyModel>> getPathologyModels();

    @Operation(summary = "List all pathologies models for given pathology", description = "")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "An array of pathology models"),
        @ApiResponse(responseCode = "500", description = "Unexpected error") })
    @GetMapping(value = "/pathology/{id}/model/all",
        produces = { "application/json" })
    ResponseEntity<List<PathologyModel>> getPathologyModelsByPathology(@Parameter(name = "ID of pathology",required = true ) @PathVariable("id") Long id);


    @Operation(summary = "Update an existing pathology model", description = "")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successful operation"),
        @ApiResponse(responseCode = "400", description = "Invalid input / Bad Request"),
        @ApiResponse(responseCode = "404", description = "Pathology not found"),
        @ApiResponse(responseCode = "500", description = "Unexpected Error") })
    @PutMapping(value = "/pathology/model/{id}",
        produces = { "application/json" },
        consumes = { "application/json" })
    ResponseEntity<Void> updatePathologyModel(@Parameter(name = "ID of pathology model that needs to be updated",required = true ) @PathVariable("id") Long id,
        @Parameter(name = "Pathology model object that needs to be updated" ,required = true ) @RequestBody PathologyModel model,
        final BindingResult result) throws RestServiceException;

    @Operation(summary = "Upload model specifications", description = "")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "success returns model"),
        @ApiResponse(responseCode = "400", description = "Invalid input / Bad Request"),
        @ApiResponse(responseCode = "500", description = "Unexpected Error") })
    @PostMapping(value = "/pathology/model/upload/specs/{id}",
        produces = { "application/json" },
        consumes = { MediaType.MULTIPART_FORM_DATA_VALUE, "application/json" })
    ResponseEntity<PathologyModel> uploadModelSpecifications(@Parameter(name = "Pathology model id",required = true ) @PathVariable("id") Long id,
            @RequestParam("files") MultipartFile[] uploadfiles) throws RestServiceException;

    @Operation(summary = "Download model specifications file file", description = "")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successful operation"),
        @ApiResponse(responseCode = "400", description = "Invalid PathologyModel  id"),
        @ApiResponse(responseCode = "500", description = "Unexpected Error") })
    @GetMapping(value = "/pathology/model/download/specs/{id}",
        produces = { MediaType.APPLICATION_OCTET_STREAM_VALUE,"application/json" })
    ResponseEntity<Resource> downloadModelSpecifications(@Parameter(name = "pathology model id",required = true ) @PathVariable("id") Long id) throws RestServiceException;


}

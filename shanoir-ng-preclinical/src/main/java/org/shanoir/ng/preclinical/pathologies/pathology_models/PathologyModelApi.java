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
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

@Api(value = "pathology_model", description = "the refs API")
public interface PathologyModelApi {

    @ApiOperation(value = "Add a new pathology model", notes = "", response = Void.class, tags={ "PathologyModel", })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "success returns Pathology model", response = PathologyModel.class),
        @ApiResponse(code = 400, message = "Invalid input / Bad Request", response = PathologyModel.class),
        @ApiResponse(code = 409, message = "Already exists - conflict", response = PathologyModel.class),
        @ApiResponse(code = 500, message = "Unexpected Error", response = PathologyModel.class) })
    @RequestMapping(value = "/pathology/model",
        produces = { "application/json" }, 
        consumes = { "application/json" },
        method = RequestMethod.POST)
    ResponseEntity<PathologyModel> createPathologyModel(@ApiParam(value = "pathology model to create", required = true) @RequestBody PathologyModel model,
	BindingResult result) throws RestServiceException;


    @ApiOperation(value = "Deletes a pathology model", notes = "", response = Void.class, tags={ "PathologyModel", })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "Successful operation", response = Void.class),
        @ApiResponse(code = 400, message = "Invalid pathology model id", response = Void.class),
        @ApiResponse(code = 500, message = "Unexpected Error", response = Void.class) })
    @RequestMapping(value = "/pathology/model/{id}",
        produces = { "application/json" }, 
        method = RequestMethod.DELETE)
    ResponseEntity<Void> deletePathologyModel(@ApiParam(value = "pathology model id",required=true ) @PathVariable("id") Long id);


    @ApiOperation(value = "Get Pathology model", notes = "", response = PathologyModel.class, responseContainer = "List", tags={ "PathologyModel", })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "A pathology model", response = PathologyModel.class),
        @ApiResponse(code = 404, message = "Pathology model not found", response = Void.class),
        @ApiResponse(code = 500, message = "Unexpected error", response = PathologyModel.class) })
    @RequestMapping(value = "/pathology/model/{id}",
        produces = { "application/json" }, 
        method = RequestMethod.GET)
    ResponseEntity<PathologyModel> getPathologyModelById(@ApiParam(value = "Pathology model id",required=true ) @PathVariable("id") Long id);


    @ApiOperation(value = "List all pathologies models", notes = "", response = PathologyModel.class, responseContainer = "List", tags={ "PathologyModel", })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "An array of pathology models", response = PathologyModel.class),
        @ApiResponse(code = 500, message = "Unexpected error", response = PathologyModel.class) })
    @RequestMapping(value = "/pathology/model",
        produces = { "application/json" }, 
        method = RequestMethod.GET)
    ResponseEntity<List<PathologyModel>> getPathologyModels();
    
    @ApiOperation(value = "List all pathologies models for given pathology", notes = "", response = PathologyModel.class, responseContainer = "List", tags={ "PathologyModel", })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "An array of pathology models", response = PathologyModel.class),
        @ApiResponse(code = 500, message = "Unexpected error", response = PathologyModel.class) })
    @RequestMapping(value = "/pathology/{id}/model/all",
        produces = { "application/json" }, 
        method = RequestMethod.GET)
    ResponseEntity<List<PathologyModel>> getPathologyModelsByPathology(@ApiParam(value = "ID of pathology",required=true ) @PathVariable("id") Long id);


    @ApiOperation(value = "Update an existing pathology model", notes = "", response = Void.class, tags={ "PathologyModel", })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "Successful operation", response = Void.class),
        @ApiResponse(code = 400, message = "Invalid input / Bad Request", response = Void.class),
        @ApiResponse(code = 404, message = "Pathology not found", response = Void.class),
        @ApiResponse(code = 500, message = "Unexpected Error", response = Void.class) })
    @RequestMapping(value = "/pathology/model/{id}",
        produces = { "application/json" }, 
        consumes = { "application/json" },
        method = RequestMethod.PUT)
    ResponseEntity<Void> updatePathologyModel(@ApiParam(value = "ID of pathology model that needs to be updated",required=true ) @PathVariable("id") Long id,
        @ApiParam(value = "Pathology model object that needs to be updated" ,required=true ) @RequestBody PathologyModel model,
        final BindingResult result) throws RestServiceException;

    @ApiOperation(value = "Upload model specifications", notes = "", response = PathologyModel.class, tags={ "PathologyModel", })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "success returns model", response = PathologyModel.class),
        @ApiResponse(code = 400, message = "Invalid input / Bad Request", response = PathologyModel.class),
        @ApiResponse(code = 500, message = "Unexpected Error", response = PathologyModel.class) })
    @RequestMapping(value = "/pathology/model/upload/specs/{id}",
        produces = { "application/json" },
        consumes = { MediaType.MULTIPART_FORM_DATA_VALUE, "application/json" },
        method = RequestMethod.POST)
    ResponseEntity<PathologyModel> uploadModelSpecifications(@ApiParam(value = "Pathology model id",required=true ) @PathVariable("id") Long id,
    		@RequestParam("files") MultipartFile[] uploadfiles) throws RestServiceException;
	
    @ApiOperation(value = "Download model specifications file file", notes = "", response = Void.class, tags={ "PathologyModel", })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "Successful operation", response = Void.class),
        @ApiResponse(code = 400, message = "Invalid PathologyModel  id", response = Void.class),
        @ApiResponse(code = 500, message = "Unexpected Error", response = Void.class) })
    @RequestMapping(value = "/pathology/model/download/specs/{id}",
        produces = { MediaType.APPLICATION_OCTET_STREAM_VALUE,"application/json" }, 
        method = RequestMethod.GET)
    ResponseEntity<Resource> downloadModelSpecifications(@ApiParam(value = "pathology model id",required=true ) @PathVariable("id") Long id) throws RestServiceException;

    
}

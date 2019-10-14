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

package org.shanoir.ng.preclinical.therapies;

import java.util.List;

import org.shanoir.ng.shared.exception.RestServiceException;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

@Api(value = "therapy", description = "the therapies API")
@RequestMapping("/therapy")
public interface TherapyApi {

    @ApiOperation(value = "Add a new therapy", notes = "", response = Void.class, tags={ "Therapy", })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "success returns Therapy", response = Therapy.class),
        @ApiResponse(code = 400, message = "Invalid input / Bad Request", response = Therapy.class),
        @ApiResponse(code = 409, message = "Already exists - conflict", response = Therapy.class),
        @ApiResponse(code = 500, message = "Unexpected Error", response = Therapy.class) })
    @RequestMapping(value = "",
        produces = { "application/json" },
        consumes = { "application/json" },
        method = RequestMethod.POST)
    ResponseEntity<Therapy> createTherapy(@ApiParam(value = "therapy to create", required = true) @RequestBody Therapy therapy,
	BindingResult result) throws RestServiceException;


    @ApiOperation(value = "Deletes a therapy value", notes = "", response = Void.class, tags={ "Therapy", })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "Successful operation", response = Void.class),
        @ApiResponse(code = 400, message = "Invalid therapy id", response = Void.class),
        @ApiResponse(code = 500, message = "Unexpected Error", response = Void.class) })
    @RequestMapping(value = "/{id}",
        produces = { "application/json" }, 
        method = RequestMethod.DELETE)
    ResponseEntity<Void> deleteTherapy(@ApiParam(value = "therapy id",required=true ) @PathVariable("id") Long id);


    @ApiOperation(value = "Get Therapy by id", notes = "", response = Therapy.class, responseContainer = "List", tags={ "Therapy", })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "A therapy ", response = Therapy.class),
        @ApiResponse(code = 404, message = "Therapy not found", response = Void.class),
        @ApiResponse(code = 500, message = "Unexpected error", response = Therapy.class) })
    @RequestMapping(value = "/{id}",
        produces = { "application/json" }, 
        method = RequestMethod.GET)
    ResponseEntity<Therapy> getTherapyById(@ApiParam(value = "Therapy id",required=true ) @PathVariable("id") Long id);
    
    
    @ApiOperation(value = "Get Therapy by type", notes = "", response = Therapy.class, responseContainer = "List", tags={ "Therapy", })
    @ApiResponses(value = { 
    		@ApiResponse(code = 200, message = "An array of therapies", response = Therapy.class),
            @ApiResponse(code = 500, message = "Unexpected error", response = Therapy.class) })
    @RequestMapping(value = "/type/{type}",
        produces = { "application/json" }, 
        method = RequestMethod.GET)
    ResponseEntity<List<Therapy>> getTherapyByType(@ApiParam(value = "Therapy type",required=true ) @PathVariable("type") String type) throws RestServiceException ;


    @ApiOperation(value = "List all therapies", notes = "", response = Therapy.class, responseContainer = "List", tags={ "Therapy", })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "An array of therapies", response = Therapy.class),
        @ApiResponse(code = 500, message = "Unexpected error", response = Therapy.class) })
    @RequestMapping(value = "",
        produces = { "application/json" }, 
        method = RequestMethod.GET)
    ResponseEntity<List<Therapy>> getTherapies();


    @ApiOperation(value = "Update an existing therapy", notes = "", response = Void.class, tags={ "Therapy", })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "Successful operation", response = Void.class),
        @ApiResponse(code = 400, message = "Invalid input / Bad Request", response = Void.class),
        @ApiResponse(code = 404, message = "Therapy not found", response = Void.class),
        @ApiResponse(code = 500, message = "Unexpected Error", response = Void.class) })
    @RequestMapping(value = "/{id}",
        produces = { "application/json" }, 
        consumes = { "application/json" },
        method = RequestMethod.PUT)
    ResponseEntity<Void> updateTherapy(@ApiParam(value = "ID of therapy that needs to be updated",required=true ) @PathVariable("id") Long id,
        @ApiParam(value = "Therapy object that needs to be updated" ,required=true ) @RequestBody Therapy therapy,
        final BindingResult result) throws RestServiceException;

}

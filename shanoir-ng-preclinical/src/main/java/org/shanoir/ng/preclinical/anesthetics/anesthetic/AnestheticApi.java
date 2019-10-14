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
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

@Api(value = "anesthetic", description = "the anesthetic API")
@RequestMapping("/anesthetic")
public interface AnestheticApi {

    @ApiOperation(value = "Add a new anesthetic", notes = "", response = Void.class, tags={ "Anesthetic", })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "success returns Anesthetic", response = Anesthetic.class),
        @ApiResponse(code = 400, message = "Invalid input / Bad Request", response = Anesthetic.class),
        @ApiResponse(code = 409, message = "Already exists - conflict", response = Anesthetic.class),
        @ApiResponse(code = 500, message = "Unexpected Error", response = Anesthetic.class) })
    @RequestMapping(value = "",
        produces = { "application/json" },
        consumes = { "application/json" },
        method = RequestMethod.POST)
    ResponseEntity<Anesthetic> createAnesthetic(@ApiParam(value = "anesthetic to create", required = true) @RequestBody Anesthetic anesthetic,
	BindingResult result) throws RestServiceException;


    @ApiOperation(value = "Deletes an Anesthetic", notes = "", response = Void.class, tags={ "Anesthetic", })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "Successful operation", response = Void.class),
        @ApiResponse(code = 400, message = "Invalid Anesthetic id", response = Void.class),
        @ApiResponse(code = 500, message = "Unexpected Error", response = Void.class) })
    @RequestMapping(value = "/{id}",
        produces = { "application/json" }, 
        method = RequestMethod.DELETE)
    ResponseEntity<Void> deleteAnesthetic(@ApiParam(value = "Anesthetic id",required=true ) @PathVariable("id") Long id);


    @ApiOperation(value = "Get Anesthetic by id", notes = "", response = Anesthetic.class, responseContainer = "List", tags={ "Anesthetic", })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "An Anesthetic", response = Anesthetic.class),
        @ApiResponse(code = 404, message = "Anesthetic not found", response = Void.class),
        @ApiResponse(code = 500, message = "Unexpected error", response = Anesthetic.class) })
    @RequestMapping(value = "/{id}",
        produces = { "application/json" }, 
        method = RequestMethod.GET)
    ResponseEntity<Anesthetic> getAnestheticById(@ApiParam(value = "Anesthetic id",required=true ) @PathVariable("id") Long id);
    

    @ApiOperation(value = "List all anesthetics", notes = "", response = Anesthetic.class, responseContainer = "List", tags={ "Anesthetic", })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "An array of anesthetics", response = Anesthetic.class),
        @ApiResponse(code = 500, message = "Unexpected error", response = Anesthetic.class) })
    @RequestMapping(value = "",
        produces = { "application/json" }, 
        method = RequestMethod.GET)
    ResponseEntity<List<Anesthetic>> getAnesthetics();
    
    @ApiOperation(value = "List all anesthetics by type", notes = "", response = Anesthetic.class, responseContainer = "List", tags={ "Anesthetic", })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "An array of anesthetics", response = Anesthetic.class),
        @ApiResponse(code = 500, message = "Unexpected error", response = Anesthetic.class) })
    @RequestMapping(value = "/type/{type}",
        produces = { "application/json" }, 
        method = RequestMethod.GET)
    ResponseEntity<List<Anesthetic>> getAnestheticsByType(@ApiParam(value = "Anesthetic type ",required=true ) @PathVariable("type") String type);


    @ApiOperation(value = "Update an existing anesthetic", notes = "", response = Void.class, tags={ "Anesthetic", })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "Successful operation", response = Void.class),
        @ApiResponse(code = 400, message = "Invalid input / Bad Request", response = Void.class),
        @ApiResponse(code = 404, message = "Anesthetic not found", response = Void.class),
        @ApiResponse(code = 500, message = "Unexpected Error", response = Void.class) })
    @RequestMapping(value = "/{id}",
        produces = { "application/json" }, 
        consumes = { "application/json" },
        method = RequestMethod.PUT)
    ResponseEntity<Void> updateAnesthetic(@ApiParam(value = "ID of Anesthetic that needs to be updated",required=true ) @PathVariable("id") Long id,
        @ApiParam(value = "Anesthetic object that needs to be updated" ,required=true ) @RequestBody Anesthetic anesthetic,
        final BindingResult result) throws RestServiceException;

}

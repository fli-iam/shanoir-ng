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
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

@Api(value = "contrast_agent")
@RequestMapping("/protocol/{pid}/contrastagent")
public interface ContrastAgentApi {
	
	@ApiOperation(value = "Add a new contrast agent", notes = "", response = Void.class, tags={ "ContrastAgent", })
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "success returns Contrast Agent", response = ContrastAgent.class),
        @ApiResponse(code = 400, message = "Invalid input / Bad Request", response = ContrastAgent.class),
        @ApiResponse(code = 409, message = "Already exists - conflict", response = ContrastAgent.class),
        @ApiResponse(code = 500, message = "Unexpected Error", response = ContrastAgent.class) })
    @PostMapping(value = "",
        produces = { "application/json" },
        consumes = { "application/json" })
    ResponseEntity<ContrastAgent> createContrastAgent(@ApiParam(value = "protocol id",required=true ) @PathVariable("pid") Long pid,
    		@ApiParam(value = "Contrast Agent to create", required = true) @RequestBody ContrastAgent contrastagent,
	BindingResult result) throws RestServiceException;


    @ApiOperation(value = "Deletes a ContrastAgent", notes = "", response = Void.class, tags={ "ContrastAgent", })
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "Successful operation", response = Void.class),
        @ApiResponse(code = 400, message = "Invalid ContrastAgent id", response = Void.class),
        @ApiResponse(code = 500, message = "Unexpected Error", response = Void.class) })
    @DeleteMapping(value = "/{cid}",
        produces = { "application/json" })
    ResponseEntity<Void> deleteContrastAgent(@ApiParam(value = "protocol id",required=true ) @PathVariable("pid") Long pid,
    		@ApiParam(value = "Contrast Agent id",required=true ) @PathVariable("cid") Long cid);


    
    @ApiOperation(value = "Get Contrast Agent by protocol id", notes = "", response = ContrastAgent.class, responseContainer = "List", tags={ "ContrastAgent", })
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "A Contrast Agent", response = ContrastAgent.class),
        @ApiResponse(code = 404, message = "Contrast Agent not found", response = Void.class),
        @ApiResponse(code = 500, message = "Unexpected error", response = ContrastAgent.class) })
    @GetMapping(value = "",
        produces = { "application/json" })
    ResponseEntity<ContrastAgent> getContrastAgentByProtocolId(@ApiParam(value = "protocol id",required=true ) @PathVariable("pid") Long pid);
    
    @ApiOperation(value = "Get Contrast Agent by id", notes = "", response = ContrastAgent.class, responseContainer = "List", tags={ "ContrastAgent", })
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "A Contrast Agent", response = ContrastAgent.class),
        @ApiResponse(code = 404, message = "Contrast Agent not found", response = Void.class),
        @ApiResponse(code = 500, message = "Unexpected error", response = ContrastAgent.class) })
    @GetMapping(value = "/{cid}",
        produces = { "application/json" })
    ResponseEntity<ContrastAgent> getContrastAgentById(@ApiParam(value = "protocol id",required=true ) @PathVariable("pid") Long pid,
    		@ApiParam(value = "Contrast Agent id",required=true ) @PathVariable("cid") Long cid);
    
    @ApiOperation(value = "Get Contrast Agent by name", notes = "", response = ContrastAgent.class, responseContainer = "List", tags={ "ContrastAgent", })
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "A Contrast Agent", response = ContrastAgent.class),
        @ApiResponse(code = 404, message = "Contrast Agent not found", response = Void.class),
        @ApiResponse(code = 500, message = "Unexpected error", response = ContrastAgent.class) })
    @GetMapping(value = "/name/{name}",
        produces = { "application/json" })
    ResponseEntity<ContrastAgent> getContrastAgentByName(@ApiParam(value = "protocol id",required=true ) @PathVariable("pid") Long pid,
    		@ApiParam(value = "Contrast Agent id",required=true ) @PathVariable("name") String name);
    

    @ApiOperation(value = "List all contrast agents", notes = "", response = ContrastAgent.class, responseContainer = "List", tags={ "ContrastAgent", })
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "An array of contrast agents", response = ContrastAgent.class),
        @ApiResponse(code = 500, message = "Unexpected error", response = ContrastAgent.class) })
    @GetMapping(value = "/all",
        produces = { "application/json" })
    ResponseEntity<List<ContrastAgent>> getContrastAgents(@ApiParam(value = "protocol id",required=true ) @PathVariable("pid") Long pid);


    @ApiOperation(value = "Update an existing contrast agent", notes = "", response = Void.class, tags={ "ContrastAgent", })
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "Successful operation", response = Void.class),
        @ApiResponse(code = 400, message = "Invalid input / Bad Request", response = Void.class),
        @ApiResponse(code = 404, message = "Contrast Agent not found", response = Void.class),
        @ApiResponse(code = 500, message = "Unexpected Error", response = Void.class) })
	@PutMapping(value = "/{cid}",
        produces = { "application/json" },
        consumes = { "application/json" })
    ResponseEntity<Void> updateContrastAgent(@ApiParam(value = "protocol id",required=true ) @PathVariable("pid") Long pid,
    		@ApiParam(value = "ID of contrast agent that needs to be updated",required=true ) @PathVariable("cid") Long cid,
        @ApiParam(value = "Contrast Agent object that needs to be updated" ,required=true ) @RequestBody ContrastAgent contrastagent,
        final BindingResult result) throws RestServiceException;

}

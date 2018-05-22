package org.shanoir.ng.preclinical.pathologies;

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

@Api(value = "pathology", description = "the pathologies API")
@RequestMapping("/pathology")
public interface PathologyApi {

    @ApiOperation(value = "Add a new pathology", notes = "", response = Void.class, tags={ "Pathology", })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "success returns Pathology", response = Pathology.class),
        @ApiResponse(code = 400, message = "Invalid input / Bad Request", response = Pathology.class),
        @ApiResponse(code = 409, message = "Already exists - conflict", response = Pathology.class),
        @ApiResponse(code = 500, message = "Unexpected Error", response = Pathology.class) })
    @RequestMapping(value = "",
        produces = { "application/json" },
        consumes = { "application/json" },
        method = RequestMethod.POST)
    ResponseEntity<Pathology> createPathology(@ApiParam(value = "pathology to create", required = true) @RequestBody Pathology pathology,
	BindingResult result) throws RestServiceException;


    @ApiOperation(value = "Deletes a pathology value", notes = "", response = Void.class, tags={ "Pathology", })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "Successful operation", response = Void.class),
        @ApiResponse(code = 400, message = "Invalid pathology id", response = Void.class),
        @ApiResponse(code = 500, message = "Unexpected Error", response = Void.class) })
    @RequestMapping(value = "/{id}",
        produces = { "application/json" }, 
        method = RequestMethod.DELETE)
    ResponseEntity<Void> deletePathology(@ApiParam(value = "pathology id",required=true ) @PathVariable("id") Long id);


    @ApiOperation(value = "Get Pathology", notes = "", response = Pathology.class, responseContainer = "List", tags={ "Pathology", })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "A pathology ", response = Pathology.class),
        @ApiResponse(code = 404, message = "Pathology not found", response = Void.class),
        @ApiResponse(code = 500, message = "Unexpected error", response = Pathology.class) })
    @RequestMapping(value = "/{id}",
        produces = { "application/json" }, 
        method = RequestMethod.GET)
    ResponseEntity<Pathology> getPathologyById(@ApiParam(value = "Pathology id",required=true ) @PathVariable("id") Long id);


    @ApiOperation(value = "List all pathologies", notes = "", response = Pathology.class, responseContainer = "List", tags={ "Pathology", })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "An array of pathologies", response = Pathology.class),
        @ApiResponse(code = 500, message = "Unexpected error", response = Pathology.class) })
    @RequestMapping(value = "/all",
        produces = { "application/json" },
        method = RequestMethod.GET)
    ResponseEntity<List<Pathology>> getPathologies();


    @ApiOperation(value = "Update an existing pathology", notes = "", response = Void.class, tags={ "Pathology", })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "Successful operation", response = Void.class),
        @ApiResponse(code = 400, message = "Invalid input / Bad Request", response = Void.class),
        @ApiResponse(code = 404, message = "Pathology not found", response = Void.class),
        @ApiResponse(code = 500, message = "Unexpected Error", response = Void.class) })
    @RequestMapping(value = "/{id}",
        produces = { "application/json" }, 
        consumes = { "application/json" },
        method = RequestMethod.PUT)
    ResponseEntity<Void> updatePathology(@ApiParam(value = "ID of pathology that needs to be updated",required=true ) @PathVariable("id") Long id,
        @ApiParam(value = "Pathology object that needs to be updated" ,required=true ) @RequestBody Pathology pathology,
        final BindingResult result) throws RestServiceException;

}

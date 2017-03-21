package org.shanoir.ng.controller.rest;

import java.util.List;

import org.shanoir.ng.exception.RestServiceException;
import org.shanoir.ng.model.Center;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
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

@javax.annotation.Generated(value = "class io.swagger.codegen.languages.SpringCodegen", date = "2016-11-18T15:36:13.002Z")

@Api(value = "center", description = "the center API")
@RequestMapping("/center")
public interface CenterApi {

	@ApiOperation(value = "", notes = "Deletes a center", response = Void.class, tags = {})
	@ApiResponses(value = { @ApiResponse(code = 204, message = "center deleted", response = Void.class),
			@ApiResponse(code = 401, message = "unauthorized", response = Void.class),
			@ApiResponse(code = 403, message = "forbidden", response = Void.class),
			@ApiResponse(code = 404, message = "no center found", response = Void.class),
			@ApiResponse(code = 500, message = "unexpected error", response = Void.class) })
	@RequestMapping(value = "/{centerId}", produces = { "application/json" }, method = RequestMethod.DELETE)
	@PreAuthorize("hasAnyRole('ADMIN', 'EXPERT')")
	ResponseEntity<Void> deleteCenter(
			@ApiParam(value = "id of the center", required = true) @PathVariable("centerId") Long centerId);

	@ApiOperation(value = "", notes = "If exists, returns the center corresponding to the given id", response = Center.class, tags = {})
	@ApiResponses(value = { @ApiResponse(code = 200, message = "found center", response = Center.class),
			@ApiResponse(code = 401, message = "unauthorized", response = Center.class),
			@ApiResponse(code = 403, message = "forbidden", response = Center.class),
			@ApiResponse(code = 404, message = "no center found", response = Center.class),
			@ApiResponse(code = 500, message = "unexpected error", response = Center.class) })
	@RequestMapping(value = "/{centerId}", produces = { "application/json" }, method = RequestMethod.GET)
	@PreAuthorize("hasAnyRole('ADMIN', 'EXPERT', 'USER')")
	ResponseEntity<Center> findCenterById(
			@ApiParam(value = "id of the center", required = true) @PathVariable("centerId") Long centerId);

	@ApiOperation(value = "", notes = "Returns all the centers", response = Center.class, responseContainer = "List", tags = {})
	@ApiResponses(value = { @ApiResponse(code = 200, message = "found centers", response = Center.class),
			@ApiResponse(code = 204, message = "no center found", response = Center.class),
			@ApiResponse(code = 401, message = "unauthorized", response = Center.class),
			@ApiResponse(code = 403, message = "forbidden", response = Center.class),
			@ApiResponse(code = 500, message = "unexpected error", response = Center.class) })
	@RequestMapping(value = "/all", produces = { "application/json" }, method = RequestMethod.GET)
	@PreAuthorize("hasAnyRole('ADMIN', 'EXPERT', 'USER')")
	ResponseEntity<List<Center>> findCenters();

	@ApiOperation(value = "", notes = "Saves a new center", response = Center.class, tags = {})
	@ApiResponses(value = { @ApiResponse(code = 200, message = "created center", response = Center.class),
			@ApiResponse(code = 401, message = "unauthorized", response = Center.class),
			@ApiResponse(code = 403, message = "forbidden", response = Center.class),
			@ApiResponse(code = 422, message = "bad parameters", response = Center.class),
			@ApiResponse(code = 500, message = "unexpected error", response = Center.class) })
	@RequestMapping(value = "", produces = { "application/json" }, consumes = {
			"application/json" }, method = RequestMethod.POST)
	@PreAuthorize("hasAnyRole('ADMIN', 'EXPERT')")
	ResponseEntity<Center> saveNewCenter(@ApiParam(value = "center to create", required = true) @RequestBody Center center,
			BindingResult result) throws RestServiceException;

	@ApiOperation(value = "", notes = "Updates a center", response = Void.class, tags = {})
	@ApiResponses(value = { @ApiResponse(code = 204, message = "center updated", response = Void.class),
			@ApiResponse(code = 401, message = "unauthorized", response = Void.class),
			@ApiResponse(code = 403, message = "forbidden", response = Void.class),
			@ApiResponse(code = 422, message = "bad parameters", response = Void.class),
			@ApiResponse(code = 500, message = "unexpected error", response = Void.class) })
	@RequestMapping(value = "/{centerId}", produces = { "application/json" }, consumes = {
			"application/json" }, method = RequestMethod.PUT)
	@PreAuthorize("hasAnyRole('ADMIN', 'EXPERT')")
	ResponseEntity<Void> updateCenter(
			@ApiParam(value = "id of the center", required = true) @PathVariable("centerId") Long centerId,
			@ApiParam(value = "center to update", required = true) @RequestBody Center center, BindingResult result)
			throws RestServiceException;

}

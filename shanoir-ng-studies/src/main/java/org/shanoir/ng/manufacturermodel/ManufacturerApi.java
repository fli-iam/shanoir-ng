package org.shanoir.ng.manufacturermodel;

import java.util.List;

import org.shanoir.ng.shared.exception.ErrorModel;
import org.shanoir.ng.shared.exception.RestServiceException;
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

@Api(value = "manufacturer", description = "the manufacturer API")
@RequestMapping("/manufacturers")
public interface ManufacturerApi {

	@ApiOperation(value = "", notes = "If exists, returns the manufacturer corresponding to the given id", response = Manufacturer.class, tags = {})
	@ApiResponses(value = { @ApiResponse(code = 200, message = "found manufacturer", response = Manufacturer.class),
			@ApiResponse(code = 204, message = "no manufacturer found", response = Void.class),
			@ApiResponse(code = 401, message = "unauthorized", response = Void.class),
			@ApiResponse(code = 403, message = "forbidden", response = Void.class),
			@ApiResponse(code = 500, message = "unexpected error", response = ErrorModel.class) })
	@RequestMapping(value = "/{manufacturerId}", produces = { "application/json" }, method = RequestMethod.GET)
	ResponseEntity<Manufacturer> findManufacturerById(
			@ApiParam(value = "id of the manufacturer", required = true) @PathVariable("manufacturerId") Long manufacturerId);

	@ApiOperation(value = "", notes = "Returns all the manufacturers", response = Manufacturer.class, responseContainer = "List", tags = {})
	@ApiResponses(value = { @ApiResponse(code = 200, message = "found manufacturers", response = Manufacturer.class),
			@ApiResponse(code = 204, message = "no manufacturer found", response = Void.class),
			@ApiResponse(code = 401, message = "unauthorized", response = Void.class),
			@ApiResponse(code = 403, message = "forbidden", response = Void.class),
			@ApiResponse(code = 500, message = "unexpected error", response = ErrorModel.class) })
	@RequestMapping(value = "", produces = { "application/json" }, method = RequestMethod.GET)
	@PreAuthorize("hasAnyRole('ADMIN', 'EXPERT')")
	ResponseEntity<List<Manufacturer>> findManufacturers();

	@ApiOperation(value = "", notes = "Saves a new manufacturer", response = Manufacturer.class, tags = {})
	@ApiResponses(value = { @ApiResponse(code = 200, message = "created manufacturer", response = Manufacturer.class),
			@ApiResponse(code = 401, message = "unauthorized", response = Void.class),
			@ApiResponse(code = 403, message = "forbidden", response = Void.class),
			@ApiResponse(code = 422, message = "bad parameters", response = Void.class),
			@ApiResponse(code = 500, message = "unexpected error", response = ErrorModel.class) })
	@RequestMapping(value = "", produces = { "application/json" }, consumes = {
			"application/json" }, method = RequestMethod.POST)
	@PreAuthorize("hasAnyRole('ADMIN', 'EXPERT')")
	ResponseEntity<Manufacturer> saveNewManufacturer(
			@ApiParam(value = "manufacturer to create", required = true) @RequestBody Manufacturer manufacturer,
			final BindingResult result) throws RestServiceException;

	@ApiOperation(value = "", notes = "Updates a manufacturer", response = Void.class, tags = {})
	@ApiResponses(value = { @ApiResponse(code = 204, message = "manufacturer updated", response = Void.class),
			@ApiResponse(code = 401, message = "unauthorized", response = Void.class),
			@ApiResponse(code = 403, message = "forbidden", response = Void.class),
			@ApiResponse(code = 404, message = "manufacturer not found", response = Void.class),
			@ApiResponse(code = 422, message = "bad parameters", response = Void.class),
			@ApiResponse(code = 500, message = "unexpected error", response = ErrorModel.class) })
	@RequestMapping(value = "/{manufacturerId}", produces = { "application/json" }, consumes = {
			"application/json" }, method = RequestMethod.PUT)
	@PreAuthorize("hasAnyRole('ADMIN', 'EXPERT')")
	ResponseEntity<Void> updateManufacturer(
			@ApiParam(value = "id of the manufacturer", required = true) @PathVariable("manufacturerId") Long manufacturerId,
			@ApiParam(value = "manufacturer to update", required = true) @RequestBody Manufacturer manufacturer,
			BindingResult result) throws RestServiceException;

}

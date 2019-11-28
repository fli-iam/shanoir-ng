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
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

@Api(value = "refs", description = "the refs API")
@RequestMapping("/refs")
public interface RefsApi {

	@ApiOperation(value = "Add a new Reference", notes = "", response = Void.class, tags = { "Reference", })
	@ApiResponses(value = { @ApiResponse(code = 200, message = "success returns Reference", response = Void.class),
			@ApiResponse(code = 400, message = "Invalid input / Bad Request", response = Void.class),
			@ApiResponse(code = 500, message = "Unexpected Error", response = Void.class) })
	@RequestMapping(value = "", produces = { "application/json" }, consumes = {
			"application/json" }, method = RequestMethod.POST)
	ResponseEntity<Reference> createReferenceValue(
			@ApiParam(value = "Ref to add", required = true) @RequestBody Reference body) throws RestServiceException;

	@ApiOperation(value = "Deletes a reference", notes = "", response = Void.class, tags = { "Reference", })
	@ApiResponses(value = { @ApiResponse(code = 200, message = "Successful operation", response = Void.class),
			@ApiResponse(code = 400, message = "Not found", response = Void.class),
			@ApiResponse(code = 406, message = "Not acceptable", response = Void.class),
			@ApiResponse(code = 500, message = "Unexpected Error", response = Void.class) })
	@RequestMapping(value = "/{id}", produces = { "application/json" }, method = RequestMethod.DELETE)
	ResponseEntity<Void> deleteReferenceValue(
			@ApiParam(value = "id of reference to be deleted", required = true) @PathVariable("id") Long id);

	@ApiOperation(value = "Get existing references by category", notes = "", response = Reference.class, responseContainer = "List", tags = {
			"Reference", })
	@ApiResponses(value = {
			@ApiResponse(code = 200, message = "An array of references values according to category parameter", response = Reference.class),
			@ApiResponse(code = 204, message = "No content", response = Reference.class),
			@ApiResponse(code = 500, message = "Unexpected error", response = Reference.class) })
	@RequestMapping(value = "/category/{category}", produces = { "application/json" }, method = RequestMethod.GET)
	ResponseEntity<List<Reference>> getReferencesByCategory(
			@ApiParam(value = "Category of the references", required = true) @PathVariable("category") String category);

	@ApiOperation(value = "Get all references for given category and type", notes = "", response = Reference.class, responseContainer = "List", tags = {
			"Reference", })
	@ApiResponses(value = {
			@ApiResponse(code = 200, message = "An array of references values according to category and type parameter", response = Reference.class),
			@ApiResponse(code = 204, message = "No content", response = Reference.class),
			@ApiResponse(code = 500, message = "Unexpected error", response = Reference.class) })
	@RequestMapping(value = "/category/{category}/{type}", produces = {
			"application/json" }, method = RequestMethod.GET)
	ResponseEntity<List<Reference>> getReferencesByCategoryAndType(
			@ApiParam(value = "Category of the reference", required = true) @PathVariable("category") String category,
			@ApiParam(value = "Type of the reference", required = true) @PathVariable("type") String type);

	@ApiOperation(value = "References categories", notes = "", response = String.class, responseContainer = "List", tags = {
			"Reference", })
	@ApiResponses(value = {
			@ApiResponse(code = 200, message = "An array of ref elements names", response = String.class),
			@ApiResponse(code = 500, message = "Unexpected error", response = String.class) })
	@RequestMapping(value = "/categories", produces = { "application/json" }, method = RequestMethod.GET)
	ResponseEntity<List<String>> getReferenceCategories();

	@ApiOperation(value = "References types for given category", notes = "", response = String.class, responseContainer = "List", tags = {
			"Reference", })
	@ApiResponses(value = { @ApiResponse(code = 200, message = "An array of ref types names", response = String.class),
			@ApiResponse(code = 500, message = "Unexpected error", response = String.class) })
	@RequestMapping(value = "/category/{category}/types", produces = { "application/json" }, method = RequestMethod.GET)
	ResponseEntity<List<String>> getReferenceTypesByCategory(
			@ApiParam(value = "Category of the reference", required = true) @PathVariable("category") String category);

	@ApiOperation(value = "Get all references", notes = "", response = Reference.class, responseContainer = "List", tags = {
			"Reference", })
	@ApiResponses(value = {
			@ApiResponse(code = 200, message = "An array of all references", response = Reference.class),
			@ApiResponse(code = 500, message = "Unexpected error", response = Reference.class) })
	@RequestMapping(value = "", produces = { "application/json" }, method = RequestMethod.GET)
	ResponseEntity<List<Reference>> getReferences();

	@ApiOperation(value = "Find reference by its id", notes = "Returns a reference", response = Reference.class, tags = {
			"Reference", })
	@ApiResponses(value = { @ApiResponse(code = 200, message = "Successful operation", response = Reference.class),
			@ApiResponse(code = 404, message = "Reference not found", response = Reference.class),
			@ApiResponse(code = 500, message = "Unexpected Error", response = Reference.class) })
	@RequestMapping(value = "/{id}", produces = { "application/json" }, method = RequestMethod.GET)
	ResponseEntity<Reference> getReferenceById(
			@ApiParam(value = "Id of the reference", required = true) @PathVariable("id") Long id);

	@ApiOperation(value = "Find reference by category, type and value", notes = "Returns a reference", response = Reference.class, tags = {
			"Reference", })
	@ApiResponses(value = { @ApiResponse(code = 200, message = "Successful operation", response = Reference.class),
			@ApiResponse(code = 404, message = "Reference not found", response = Reference.class),
			@ApiResponse(code = 500, message = "Unexpected Error", response = Reference.class) })
	@RequestMapping(value = "/category/{category}/{type}/{value}", produces = {
			"application/json" }, method = RequestMethod.GET)
	ResponseEntity<Reference> getReferenceByCategoryTypeAndValue(
			@ApiParam(value = "Category of the reference", required = true) @PathVariable("category") String category,
			@ApiParam(value = "type of reference", required = true) @PathVariable("type") String type,
			@ApiParam(value = "value of reference", required = true) @PathVariable("value") String value);

	@ApiOperation(value = "Update an existing Ref ", notes = "", response = Void.class, tags = { "Reference", })
	@ApiResponses(value = { @ApiResponse(code = 200, message = "Successful operation", response = Void.class),
			@ApiResponse(code = 400, message = "Invalid input / Bad Request", response = Void.class),
			@ApiResponse(code = 404, message = "Reference value not found", response = Void.class),
			@ApiResponse(code = 500, message = "Unexpected Error", response = Void.class) })
	@RequestMapping(value = "/{id}", produces = { "application/json" }, consumes = {
			"application/json" }, method = RequestMethod.PUT)
	ResponseEntity<Void> updateReferenceValue(
			@ApiParam(value = "value of ref name to be updated", required = true) @PathVariable("id") Long id,
			@ApiParam(value = "New value for the ref given as name", required = true) @RequestBody Reference body,
			BindingResult result) throws RestServiceException;

}

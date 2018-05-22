package org.shanoir.ng.preclinical.subjects;

import java.util.List;

import javax.validation.Valid;

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

@Api(value = "subject", description = "the subjects API")
@RequestMapping("/subject")
public interface AnimalSubjectApi {

	@ApiOperation(value = "Add a new animalsubject", notes = "", response = AnimalSubject.class, tags = {
			"AnimalSubject", })
	@ApiResponses(value = {
			@ApiResponse(code = 200, message = "success returns AnimalSubject", response = AnimalSubject.class),
			@ApiResponse(code = 400, message = "Invalid input / Bad Request", response = AnimalSubject.class),
			@ApiResponse(code = 409, message = "Already exists - conflict", response = AnimalSubject.class),
			@ApiResponse(code = 500, message = "Unexpected Error", response = AnimalSubject.class) })
	@RequestMapping(value = "", produces = { "application/json" }, consumes = {
			"application/json" }, method = RequestMethod.POST)
	ResponseEntity<AnimalSubject> createAnimalSubject(
			@ApiParam(value = "AnimalSubject object to add", required = true) @RequestBody @Valid final AnimalSubject animalSubject,
			final BindingResult result) throws RestServiceException;

	@ApiOperation(value = "Deletes an animalSubject", notes = "", response = Void.class, tags = { "AnimalSubject", })
	@ApiResponses(value = { @ApiResponse(code = 200, message = "Successful operation", response = Void.class),
			@ApiResponse(code = 400, message = "Invalid subject value", response = Void.class),
			@ApiResponse(code = 500, message = "Unexpected Error", response = Void.class) })
	@RequestMapping(value = "/{id}", produces = { "application/json" }, method = RequestMethod.DELETE)
	ResponseEntity<Void> deleteAnimalSubject(
			@ApiParam(value = "AnimalSubject id to delete", required = true) @PathVariable("id") Long id);

	@ApiOperation(value = "Find animalSubject by ID", notes = "Returns a subject", response = AnimalSubject.class, tags = {
			"AnimalSubject", })
	@ApiResponses(value = { @ApiResponse(code = 200, message = "Successful operation", response = AnimalSubject.class),
			@ApiResponse(code = 400, message = "Invalid ID supplied", response = AnimalSubject.class),
			@ApiResponse(code = 404, message = "Subject not found", response = AnimalSubject.class),
			@ApiResponse(code = 500, message = "Unexpected Error", response = AnimalSubject.class) })
	@RequestMapping(value = "/{id}", produces = { "application/json" }, method = RequestMethod.GET)
	ResponseEntity<AnimalSubject> getAnimalSubjectById(
			@ApiParam(value = "ID of animalSubject that needs to be fetched", required = true) @PathVariable("id") Long id);

	@ApiOperation(value = "List all animalSubjects", notes = "", response = AnimalSubject.class, responseContainer = "List", tags = {
			"AnimalSubject", })
	@ApiResponses(value = {
			@ApiResponse(code = 200, message = "An array of Preclinical AnimalSubject", response = AnimalSubject.class),
			@ApiResponse(code = 500, message = "Unexpected error", response = AnimalSubject.class) })
	@RequestMapping(value = "/all", produces = { "application/json" }, method = RequestMethod.GET)
	ResponseEntity<List<AnimalSubject>> getAnimalSubjects();

	@ApiOperation(value = "Update an existing animalSubject", notes = "", response = Void.class, tags = {
			"AnimalSubject", })
	@ApiResponses(value = { @ApiResponse(code = 200, message = "Successful operation", response = Void.class),
			@ApiResponse(code = 400, message = "Invalid input / Bad Request", response = Void.class),
			@ApiResponse(code = 404, message = "Subject not found", response = Void.class),
			@ApiResponse(code = 500, message = "Unexpected Error", response = Void.class) })
	@RequestMapping(value = "/{id}", produces = { "application/json" }, consumes = {
			"application/json" }, method = RequestMethod.PUT)
	ResponseEntity<Void> updateAnimalSubject(
			@ApiParam(value = "ID of animalSubject that needs to be updated", required = true) @PathVariable("id") Long id,
			@ApiParam(value = "AnimalSubject object that needs to be updated", required = true) @RequestBody AnimalSubject animalSubject,
			final BindingResult result) throws RestServiceException;

}

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

package org.shanoir.ng.preclinical.subjects;

import java.util.List;

import jakarta.validation.Valid;

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
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;

@Tag(name = "subject")
@RequestMapping("/subject")
public interface AnimalSubjectApi {

	@ApiOperation(value = "Add a new animalsubject", notes = "", response = AnimalSubject.class, tags = {
			"AnimalSubject", })
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "success returns AnimalSubject"),
			@ApiResponse(responseCode = "400", description = "Invalid input / Bad Request"),
			@ApiResponse(responseCode = "409", description = "Already exists - conflict"),
			@ApiResponse(responseCode = "500", description = "Unexpected Error") })
	@PostMapping(value = "", produces = { "application/json" }, consumes = {
			"application/json" })
	ResponseEntity<AnimalSubject> createAnimalSubject(
			@Parameter(name = "AnimalSubject object to add", required = true) @RequestBody @Valid final AnimalSubject animalSubject,
			final BindingResult result) throws RestServiceException;

	@Operation(summary = "Deletes an animalSubject", description = "")
	@ApiResponses(value = { @ApiResponse(responseCode = "200", description = "Successful operation"),
			@ApiResponse(responseCode = "400", description = "Invalid subject value"),
			@ApiResponse(responseCode = "500", description = "Unexpected Error") })
	@DeleteMapping(value = "/{id}", produces = { "application/json" })
	ResponseEntity<Void> deleteAnimalSubject(
			@Parameter(name = "AnimalSubject id to delete", required = true) @PathVariable("id") Long id);

	@ApiOperation(value = "Find animalSubject by ID", notes = "Returns a subject", response = AnimalSubject.class, tags = {
			"AnimalSubject", })
	@ApiResponses(value = { @ApiResponse(responseCode = "200", description = "Successful operation"),
			@ApiResponse(responseCode = "400", description = "Invalid ID supplied"),
			@ApiResponse(responseCode = "404", description = "Subject not found"),
			@ApiResponse(responseCode = "500", description = "Unexpected Error") })
	@GetMapping(value = "/{id}", produces = { "application/json" })
	ResponseEntity<AnimalSubject> getAnimalSubjectById(
			@Parameter(name = "ID of animalSubject that needs to be fetched", required = true) @PathVariable("id") Long id);

	@ApiOperation(value = "Find animalSubject by SubjectID", notes = "Returns a subject", response = AnimalSubject.class, tags = {
			"AnimalSubject", })
	@ApiResponses(value = { @ApiResponse(responseCode = "200", description = "Successful operation"),
			@ApiResponse(responseCode = "400", description = "Invalid ID supplied"),
			@ApiResponse(responseCode = "404", description = "Subject not found"),
			@ApiResponse(responseCode = "500", description = "Unexpected Error") })
	@GetMapping(value = "/find/{id}", produces = { "application/json" })
	ResponseEntity<AnimalSubject> getAnimalSubjectBySubjectId(
			@Parameter(name = "ID of subject that needs to be fetched", required = true) @PathVariable("id") Long id);

	@ApiOperation(value = "List all animalSubjects", notes = "", response = AnimalSubject.class, responseContainer = "List", tags = {
			"AnimalSubject", })
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "An array of Preclinical AnimalSubject"),
			@ApiResponse(responseCode = "401", description = "unauthorized"),
			@ApiResponse(responseCode = "403", description = "forbidden"),
			@ApiResponse(responseCode = "500", description = "Unexpected error") })
	@GetMapping(value = "/all", produces = { "application/json" })
	ResponseEntity<List<AnimalSubject>> getAnimalSubjects();

	@ApiOperation(value = "Update an existing animalSubject", notes = "", response = Void.class, tags = {
			"AnimalSubject", })
	@ApiResponses(value = { @ApiResponse(responseCode = "200", description = "Successful operation"),
			@ApiResponse(responseCode = "400", description = "Invalid input / Bad Request"),
			@ApiResponse(responseCode = "404", description = "Subject not found"),
			@ApiResponse(responseCode = "500", description = "Unexpected Error") })
	@PutMapping(value = "/{id}", produces = { "application/json" }, consumes = {
			"application/json" })
	ResponseEntity<Void> updateAnimalSubject(
			@Parameter(name = "ID of animalSubject that needs to be updated", required = true) @PathVariable("id") Long id,
			@Parameter(name = "AnimalSubject object that needs to be updated", required = true) @RequestBody AnimalSubject animalSubject,
			final BindingResult result) throws RestServiceException;

}

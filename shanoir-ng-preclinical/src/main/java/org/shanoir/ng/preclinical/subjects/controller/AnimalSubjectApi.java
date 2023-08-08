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

package org.shanoir.ng.preclinical.subjects.controller;

import java.util.List;

import javax.validation.Valid;

import org.shanoir.ng.preclinical.subjects.dto.AnimalSubjectDto;
import org.shanoir.ng.preclinical.subjects.dto.PreclinicalSubjectDto;
import org.shanoir.ng.preclinical.subjects.model.AnimalSubject;
import org.shanoir.ng.shared.exception.RestServiceException;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

@Api(value = "subject")
@RequestMapping("/subject")
public interface AnimalSubjectApi {

	@ApiOperation(value = "Add a new animalsubject", notes = "", response = PreclinicalSubjectDto.class, tags = {
			"AnimalSubject", })
	@ApiResponses(value = {
			@ApiResponse(code = 200, message = "success returns AnimalSubject", response = PreclinicalSubjectDto.class),
			@ApiResponse(code = 400, message = "Invalid input / Bad Request", response = PreclinicalSubjectDto.class),
			@ApiResponse(code = 409, message = "Already exists - conflict", response = PreclinicalSubjectDto.class),
			@ApiResponse(code = 500, message = "Unexpected Error", response = PreclinicalSubjectDto.class) })
	@PostMapping(value = "", produces = { "application/json" }, consumes = {
			"application/json" })
	ResponseEntity<PreclinicalSubjectDto> createAnimalSubject(
			@ApiParam(value = "AnimalSubject object to add", required = true) @RequestBody @Valid final PreclinicalSubjectDto animalSubject,
			final BindingResult result) throws RestServiceException;

	@ApiOperation(value = "Deletes an animalSubject", notes = "", response = Void.class, tags = { "AnimalSubject", })
	@ApiResponses(value = { @ApiResponse(code = 200, message = "Successful operation", response = Void.class),
			@ApiResponse(code = 400, message = "Invalid subject value", response = Void.class),
			@ApiResponse(code = 500, message = "Unexpected Error", response = Void.class) })
	@DeleteMapping(value = "/{id}", produces = { "application/json" })
	ResponseEntity<Void> deleteAnimalSubject(
			@ApiParam(value = "AnimalSubject id to delete", required = true) @PathVariable("id") Long id);

	@ApiOperation(value = "Find animalSubject by subject Id", notes = "Returns a subject", response = AnimalSubject.class, tags = {
			"AnimalSubject", })
	@ApiResponses(value = { @ApiResponse(code = 200, message = "Successful operation", response = AnimalSubjectDto.class),
			@ApiResponse(code = 400, message = "Invalid ID supplied", response = AnimalSubjectDto.class),
			@ApiResponse(code = 404, message = "Subject not found", response = AnimalSubjectDto.class),
			@ApiResponse(code = 500, message = "Unexpected Error", response = AnimalSubjectDto.class) })
	@GetMapping(value = "/{id}", produces = { "application/json" })
	ResponseEntity<AnimalSubjectDto> getAnimalSubjectBySubjectId(
			@ApiParam(value = "ID of animalSubject that needs to be fetched", required = true) @PathVariable("id") Long id);

	@ApiOperation(value = "List all animalSubjects", notes = "", response = AnimalSubject.class, responseContainer = "List", tags = {
			"AnimalSubject", })
	@ApiResponses(value = {
			@ApiResponse(code = 200, message = "An array of Preclinical AnimalSubject", response = AnimalSubjectDto.class),
			@ApiResponse(code = 401, message = "unauthorized", response = AnimalSubjectDto.class),
			@ApiResponse(code = 403, message = "forbidden", response = AnimalSubjectDto.class),
			@ApiResponse(code = 500, message = "Unexpected error", response = AnimalSubjectDto.class) })
	@GetMapping(value = "/all", produces = { "application/json" })
	ResponseEntity<List<AnimalSubjectDto>> getAllAnimalSubjects();

	@ApiOperation(value = "Update an existing animalSubject", notes = "", response = Void.class, tags = {
			"AnimalSubject", })
	@ApiResponses(value = { @ApiResponse(code = 200, message = "Successful operation", response = Void.class),
			@ApiResponse(code = 400, message = "Invalid input / Bad Request", response = Void.class),
			@ApiResponse(code = 404, message = "Subject not found", response = Void.class),
			@ApiResponse(code = 500, message = "Unexpected Error", response = Void.class) })
	@PutMapping(value = "/{id}", produces = { "application/json" }, consumes = {
			"application/json" })
	ResponseEntity<Void> updateAnimalSubject(
			@ApiParam(value = "subject id of animalSubject that needs to be updated", required = true) @PathVariable("id") Long id,
			@ApiParam(value = "AnimalSubject object that needs to be updated", required = true) @RequestBody AnimalSubjectDto dto,
			final BindingResult result) throws RestServiceException;

}

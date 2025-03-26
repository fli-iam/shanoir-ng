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

import org.shanoir.ng.preclinical.subjects.dto.AnimalSubjectDto;
import org.shanoir.ng.preclinical.subjects.dto.PreclinicalSubjectDto;
import org.shanoir.ng.shared.exception.RestServiceException;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@Tag(name = "subject")
@RequestMapping("/subject")
public interface AnimalSubjectApi {

	@Operation(summary = "Add a new animalsubject", description = "")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "success returns AnimalSubject"),
			@ApiResponse(responseCode = "400", description = "Invalid input / Bad Request"),
			@ApiResponse(responseCode = "409", description = "Already exists - conflict"),
			@ApiResponse(responseCode = "500", description = "Unexpected Error") })
	@PostMapping(value = "", produces = { "application/json" }, consumes = {
			"application/json" })
	ResponseEntity<PreclinicalSubjectDto> createAnimalSubject(
			@Parameter(name = "AnimalSubject object to add", required = true) @RequestBody @Valid final PreclinicalSubjectDto animalSubject,
			final BindingResult result) throws RestServiceException;

	@Operation(summary  = "Find animalSubject by subject Id", description = "Returns a subject")
	@ApiResponses(value = { @ApiResponse(responseCode = "200", description = "Successful operation"),
		@ApiResponse(responseCode = "400", description = "Invalid ID supplied"),
		@ApiResponse(responseCode = "404", description = "Subject not found"),
		@ApiResponse(responseCode = "500", description = "Unexpected Error") })
	@GetMapping(value = "/{id}", produces = { "application/json" })
	ResponseEntity<AnimalSubjectDto> getAnimalSubjectBySubjectId(
			@Parameter(name = "ID of animalSubject that needs to be fetched", required = true) @PathVariable("id") Long id);

	@Operation(summary = "", description = "List animalSubjects linked to the given subject ids")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "An array of Preclinical AnimalSubject"),
			@ApiResponse(responseCode = "401", description = "unauthorized"),
			@ApiResponse(responseCode = "403", description = "forbidden"),
			@ApiResponse(responseCode = "500", description = "Unexpected error") })
	@PostMapping(value = "/find", produces = { "application/json" })
	ResponseEntity<List<AnimalSubjectDto>> findBySubjectIds(
			@Parameter(name = "List of subject ids", required = true) @RequestParam(value = "subjectIds") List<Long> subjectIds
	);

	@Operation(summary = "Update an existing animalSubject", description = "")
	@ApiResponses(value = { @ApiResponse(responseCode = "200", description = "Successful operation"),
			@ApiResponse(responseCode = "400", description = "Invalid input / Bad Request"),
			@ApiResponse(responseCode = "404", description = "Subject not found"),
			@ApiResponse(responseCode = "500", description = "Unexpected Error") })
	@PutMapping(value = "/{id}", produces = { "application/json" }, consumes = {
			"application/json" })
	ResponseEntity<Void> updateAnimalSubject(
			@Parameter(name = "subject id of animalSubject that needs to be updated", required = true) @PathVariable("id") Long id,
			@Parameter(name = "AnimalSubject object that needs to be updated", required = true) @RequestBody AnimalSubjectDto dto,
			final BindingResult result) throws RestServiceException;

}

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

package org.shanoir.ng.preclinical.therapies.subject_therapies;

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
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;

@Tag(name = "subject_therapy")
public interface SubjectTherapyApi {

	@Operation(summary = "Add a new subject therapy", description = "")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "success returns subject therapy"),
			@ApiResponse(responseCode = "400", description = "Invalid input / Bad Request"),
			@ApiResponse(responseCode = "409", description = "Already exists - conflict"),
			@ApiResponse(responseCode = "500", description = "Unexpected Error") })
	@PostMapping(value = "/subject/{id}/therapy", produces = { "application/json" }, consumes = {
			"application/json" })
	ResponseEntity<SubjectTherapy> addSubjectTherapy(
			@Parameter(name = "subject id", required = true) @PathVariable("id") Long id,
			@Parameter(name = "therapy to add to subject", required = true) @RequestBody SubjectTherapy therapy,
			BindingResult result) throws RestServiceException;

	@Operation(summary = "Deletes a therapy from subject", description = "")
	@ApiResponses(value = { @ApiResponse(responseCode = "200", description = "Successful operation"),
			@ApiResponse(responseCode = "400", description = "Invalid subject therapy id"),
			@ApiResponse(responseCode = "500", description = "Unexpected Error") })
	@DeleteMapping(value = "/subject/{id}/therapy/{tid}", produces = {
			"application/json" })
	ResponseEntity<Void> deleteSubjectTherapy(
			@Parameter(name = "subject id", required = true) @PathVariable("id") Long id,
			@Parameter(name = "subject therapy id", required = true) @PathVariable("tid") Long tid)
			throws RestServiceException;

	@Operation(summary = "Deletes all therapies linked to a given subject", description = "")
	@ApiResponses(value = { @ApiResponse(responseCode = "200", description = "Successful operation"),
			@ApiResponse(responseCode = "400", description = "Invalid subject id"),
			@ApiResponse(responseCode = "500", description = "Unexpected Error") })
	@DeleteMapping(value = "/subject/{id}/therapy/all", produces = {
			"application/json" })
	ResponseEntity<Void> deleteSubjectTherapies(
			@Parameter(name = "animal subject id", required = true) @PathVariable("id") Long id)
			throws RestServiceException;

	@Operation(summary = "Get subject therapy by id", description = "")
	@ApiResponses(value = { @ApiResponse(responseCode = "200", description = "A subject therapy"),
			@ApiResponse(responseCode = "404", description = "Subjet therapy not found"),
			@ApiResponse(responseCode = "500", description = "Unexpected error") })
	@GetMapping(value = "/subject/{id}/therapy/{tid}", produces = {
			"application/json" })
	ResponseEntity<SubjectTherapy> getSubjectTherapyById(
			@Parameter(name = "subject id", required = true) @PathVariable("id") Long id,
			@Parameter(name = "Subject therapy id", required = true) @PathVariable("tid") Long tid)
			throws RestServiceException;

	@Operation(summary = "List all subject therapies for subject", description = "")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "An array of subject therapies"),
			@ApiResponse(responseCode = "500", description = "Unexpected error") })
	@GetMapping(value = "/subject/{id}/therapy/all", produces = { "application/json" })
	ResponseEntity<List<SubjectTherapy>> getSubjectTherapies(
			@Parameter(name = "subject id", required = true) @PathVariable("id") Long id) throws RestServiceException;

	@Operation(summary = "List all subject therapies for given therapy", description = "")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "An array of subject therapies"),
			@ApiResponse(responseCode = "500", description = "Unexpected error") })
	@GetMapping(value = "/subject/all/therapy/{tid}", produces = { "application/json" })
	ResponseEntity<List<SubjectTherapy>> getSubjectTherapiesByTherapy(
			@Parameter(name = "therapy id", required = true) @PathVariable("tid") Long tid) throws RestServiceException;

	@Operation(summary = "Update an existing subject therapy", description = "")
	@ApiResponses(value = { @ApiResponse(responseCode = "200", description = "Successful operation"),
			@ApiResponse(responseCode = "400", description = "Invalid input / Bad Request"),
			@ApiResponse(responseCode = "404", description = "Subject Therapy not found"),
			@ApiResponse(responseCode = "500", description = "Unexpected Error") })
	@PutMapping(value = "/subject/{id}/therapy/{tid}", produces = { "application/json" }, consumes = {
			"application/json" })
	ResponseEntity<Void> updateSubjectTherapy(
			@Parameter(name = "ID of subject", required = true) @PathVariable("id") Long id,
			@Parameter(name = "ID of subject therapy that needs to be updated", required = true) @PathVariable("tid") Long tid,
			@Parameter(name = "Subject Therapy that will be be updated", required = true) @RequestBody SubjectTherapy therapy,
			final BindingResult result) throws RestServiceException;

}

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
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

@Api(value = "subject_therapy")
public interface SubjectTherapyApi {

	@ApiOperation(value = "Add a new subject therapy", notes = "", response = Void.class, tags = { "SubjectTherapy", })
	@ApiResponses(value = {
			@ApiResponse(code = 200, message = "success returns subject therapy", response = SubjectTherapy.class),
			@ApiResponse(code = 400, message = "Invalid input / Bad Request", response = SubjectTherapy.class),
			@ApiResponse(code = 409, message = "Already exists - conflict", response = SubjectTherapy.class),
			@ApiResponse(code = 500, message = "Unexpected Error", response = SubjectTherapy.class) })
	@PostMapping(value = "/subject/{id}/therapy", produces = { "application/json" }, consumes = {
			"application/json" })
	ResponseEntity<SubjectTherapy> addSubjectTherapy(
			@ApiParam(value = "subject id", required = true) @PathVariable("id") Long id,
			@ApiParam(value = "therapy to add to subject", required = true) @RequestBody SubjectTherapy therapy,
			BindingResult result) throws RestServiceException;

	@ApiOperation(value = "Deletes a therapy from subject", notes = "", response = Void.class, tags = {
			"SubjectTherapy", })
	@ApiResponses(value = { @ApiResponse(code = 200, message = "Successful operation", response = Void.class),
			@ApiResponse(code = 400, message = "Invalid subject therapy id", response = Void.class),
			@ApiResponse(code = 500, message = "Unexpected Error", response = Void.class) })
	@DeleteMapping(value = "/subject/{id}/therapy/{tid}", produces = {
			"application/json" })
	ResponseEntity<Void> deleteSubjectTherapy(
			@ApiParam(value = "subject id", required = true) @PathVariable("id") Long id,
			@ApiParam(value = "subject therapy id", required = true) @PathVariable("tid") Long tid)
			throws RestServiceException;

	@ApiOperation(value = "Deletes all therapies linked to a given subject", notes = "", response = Void.class, tags = {
			"SubjectTherapy", })
	@ApiResponses(value = { @ApiResponse(code = 200, message = "Successful operation", response = Void.class),
			@ApiResponse(code = 400, message = "Invalid subject id", response = Void.class),
			@ApiResponse(code = 500, message = "Unexpected Error", response = Void.class) })
	@DeleteMapping(value = "/subject/{id}/therapy/all", produces = {
			"application/json" })
	ResponseEntity<Void> deleteSubjectTherapies(
			@ApiParam(value = "animal subject id", required = true) @PathVariable("id") Long id)
			throws RestServiceException;

	@ApiOperation(value = "Get subject therapy by id", notes = "", response = SubjectTherapy.class, responseContainer = "List", tags = {
			"SubjectTherapy", })
	@ApiResponses(value = { @ApiResponse(code = 200, message = "A subject therapy", response = SubjectTherapy.class),
			@ApiResponse(code = 404, message = "Subjet therapy not found", response = Void.class),
			@ApiResponse(code = 500, message = "Unexpected error", response = SubjectTherapy.class) })
	@GetMapping(value = "/subject/{id}/therapy/{tid}", produces = {
			"application/json" })
	ResponseEntity<SubjectTherapy> getSubjectTherapyById(
			@ApiParam(value = "subject id", required = true) @PathVariable("id") Long id,
			@ApiParam(value = "Subject therapy id", required = true) @PathVariable("tid") Long tid)
			throws RestServiceException;

	@ApiOperation(value = "List all subject therapies for subject", notes = "", response = SubjectTherapy.class, responseContainer = "List", tags = {
			"SubjectTherapy", })
	@ApiResponses(value = {
			@ApiResponse(code = 200, message = "An array of subject therapies", response = SubjectTherapy.class),
			@ApiResponse(code = 500, message = "Unexpected error", response = SubjectTherapy.class) })
	@GetMapping(value = "/subject/{id}/therapy/all", produces = { "application/json" })
	ResponseEntity<List<SubjectTherapy>> getSubjectTherapies(
			@ApiParam(value = "subject id", required = true) @PathVariable("id") Long id) throws RestServiceException;

	@ApiOperation(value = "List all subject therapies for given therapy", notes = "", response = SubjectTherapy.class, responseContainer = "List", tags = {
			"SubjectTherapy", })
	@ApiResponses(value = {
			@ApiResponse(code = 200, message = "An array of subject therapies", response = SubjectTherapy.class),
			@ApiResponse(code = 500, message = "Unexpected error", response = SubjectTherapy.class) })
	@GetMapping(value = "/subject/all/therapy/{tid}", produces = { "application/json" })
	ResponseEntity<List<SubjectTherapy>> getSubjectTherapiesByTherapy(
			@ApiParam(value = "therapy id", required = true) @PathVariable("tid") Long tid) throws RestServiceException;

	@ApiOperation(value = "Update an existing subject therapy", notes = "", response = Void.class, tags = {
			"SubjectTherapy", })
	@ApiResponses(value = { @ApiResponse(code = 200, message = "Successful operation", response = Void.class),
			@ApiResponse(code = 400, message = "Invalid input / Bad Request", response = Void.class),
			@ApiResponse(code = 404, message = "Subject Therapy not found", response = Void.class),
			@ApiResponse(code = 500, message = "Unexpected Error", response = Void.class) })
	@PutMapping(value = "/subject/{id}/therapy/{tid}", produces = { "application/json" }, consumes = {
			"application/json" })
	ResponseEntity<Void> updateSubjectTherapy(
			@ApiParam(value = "ID of subject", required = true) @PathVariable("id") Long id,
			@ApiParam(value = "ID of subject therapy that needs to be updated", required = true) @PathVariable("tid") Long tid,
			@ApiParam(value = "Subject Therapy that will be be updated", required = true) @RequestBody SubjectTherapy therapy,
			final BindingResult result) throws RestServiceException;

}

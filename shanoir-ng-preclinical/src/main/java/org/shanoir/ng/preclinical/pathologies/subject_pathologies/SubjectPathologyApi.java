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

package org.shanoir.ng.preclinical.pathologies.subject_pathologies;

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

@javax.annotation.Generated(value = "io.swagger.codegen.languages.SpringCodegen", date = "2017-03-23T14:51:04.625Z")

@Api(value = "subject_pathology", description = "the subject pathologies API")
public interface SubjectPathologyApi {

	@ApiOperation(value = "Add a new subject pathology", notes = "", response = Void.class, tags = {
			"SubjectPathology", })
	@ApiResponses(value = {
			@ApiResponse(code = 200, message = "success returns subject pathology", response = SubjectPathology.class),
			@ApiResponse(code = 400, message = "Invalid input / Bad Request", response = SubjectPathology.class),
			@ApiResponse(code = 409, message = "Already exists - conflict", response = SubjectPathology.class),
			@ApiResponse(code = 500, message = "Unexpected Error", response = SubjectPathology.class) })
	@RequestMapping(value = "/subject/{id}/pathology", produces = { "application/json" }, consumes = {
			"application/json" }, method = RequestMethod.POST)
	ResponseEntity<SubjectPathology> addSubjectPathology(
			@ApiParam(value = "subject id", required = true) @PathVariable("id") Long id,
			@ApiParam(value = "pathology to add to subject", required = true) @RequestBody SubjectPathology pathos,
			BindingResult result) throws RestServiceException;

	@ApiOperation(value = "Deletes a pathology from subject", notes = "", response = Void.class, tags = {
			"SubjectPathology", })
	@ApiResponses(value = { @ApiResponse(code = 200, message = "Successful operation", response = Void.class),
			@ApiResponse(code = 400, message = "Invalid subject pathology id", response = Void.class),
			@ApiResponse(code = 500, message = "Unexpected Error", response = Void.class) })
	@RequestMapping(value = "/subject/{id}/pathology/{pid}", produces = {
			"application/json" }, method = RequestMethod.DELETE)
	ResponseEntity<Void> deleteSubjectPathology(
			@ApiParam(value = "animal subject id", required = true) @PathVariable("id") Long id,
			@ApiParam(value = "pathology id", required = true) @PathVariable("pid") Long pid)
			throws RestServiceException;

	@ApiOperation(value = "Deletes all pathology linked to a given subject", notes = "", response = Void.class, tags = {
			"SubjectPathology", })
	@ApiResponses(value = { @ApiResponse(code = 200, message = "Successful operation", response = Void.class),
			@ApiResponse(code = 400, message = "Invalid subject id", response = Void.class),
			@ApiResponse(code = 500, message = "Unexpected Error", response = Void.class) })
	@RequestMapping(value = "/subject/{id}/pathology/all", produces = {
			"application/json" }, method = RequestMethod.DELETE)
	ResponseEntity<Void> deleteSubjectPathologies(
			@ApiParam(value = "animal subject id", required = true) @PathVariable("id") Long id)
			throws RestServiceException;

	@ApiOperation(value = "Get subject pathology by id", notes = "", response = SubjectPathology.class, responseContainer = "List", tags = {
			"SubjectPathology", })
	@ApiResponses(value = {
			@ApiResponse(code = 200, message = "A subject pathology", response = SubjectPathology.class),
			@ApiResponse(code = 404, message = "Subjet pathology not found", response = Void.class),
			@ApiResponse(code = 500, message = "Unexpected error", response = SubjectPathology.class) })
	@RequestMapping(value = "/subject/{id}/pathology/{pid}", produces = {
			"application/json" }, method = RequestMethod.GET)
	ResponseEntity<SubjectPathology> getSubjectPathologyById(
			@ApiParam(value = "subject id", required = true) @PathVariable("id") Long id,
			@ApiParam(value = "Subject pathology id", required = true) @PathVariable("pid") Long pid)
			throws RestServiceException;

	@ApiOperation(value = "List all pathologies for subject", notes = "", response = SubjectPathology.class, responseContainer = "List", tags = {
			"SubjectPathology", })
	@ApiResponses(value = {
			@ApiResponse(code = 200, message = "An array of subject pathologies", response = SubjectPathology.class),
			@ApiResponse(code = 500, message = "Unexpected error", response = SubjectPathology.class) })
	@RequestMapping(value = "/subject/{id}/pathology/all", produces = {
			"application/json" }, method = RequestMethod.GET)
	ResponseEntity<List<SubjectPathology>> getSubjectPathologies(
			@ApiParam(value = "subject id", required = true) @PathVariable("id") Long id) throws RestServiceException;

	@ApiOperation(value = "List all subjects for pathology", notes = "", response = SubjectPathology.class, responseContainer = "List", tags = {
			"SubjectPathology", })
	@ApiResponses(value = {
			@ApiResponse(code = 200, message = "An array of subject pathologies", response = SubjectPathology.class),
			@ApiResponse(code = 500, message = "Unexpected error", response = SubjectPathology.class) })
	@RequestMapping(value = "/subject/all/pathology/{pid}", produces = {
			"application/json" }, method = RequestMethod.GET)
	ResponseEntity<List<SubjectPathology>> getSubjectPathologiesByPathology(
			@ApiParam(value = "pathology id", required = true) @PathVariable("pid") Long pid)
			throws RestServiceException;

	@ApiOperation(value = "Update an existing subject pathology", notes = "", response = Void.class, tags = {
			"SubjectPathology", })
	@ApiResponses(value = { @ApiResponse(code = 200, message = "Successful operation", response = Void.class),
			@ApiResponse(code = 400, message = "Invalid input / Bad Request", response = Void.class),
			@ApiResponse(code = 404, message = "Subject Pathology not found", response = Void.class),
			@ApiResponse(code = 500, message = "Unexpected Error", response = Void.class) })
	@RequestMapping(value = "/subject/{id}/pathology/{pid}", produces = { "application/json" }, consumes = {
			"application/json" }, method = RequestMethod.PUT)
	ResponseEntity<Void> updateSubjectPathology(
			@ApiParam(value = "ID of subject", required = true) @PathVariable("id") Long id,
			@ApiParam(value = "ID of subject pathology that needs to be updated", required = true) @PathVariable("pid") Long pid,
			@ApiParam(value = "Subject Pathology that will be be updated", required = true) @RequestBody SubjectPathology pathos,
			final BindingResult result) throws RestServiceException;

}

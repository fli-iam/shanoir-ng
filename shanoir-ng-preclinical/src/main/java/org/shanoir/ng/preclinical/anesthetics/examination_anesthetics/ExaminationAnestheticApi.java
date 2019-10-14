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

package org.shanoir.ng.preclinical.anesthetics.examination_anesthetics;

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

@Api(value = "examination_anesthetic")
public interface ExaminationAnestheticApi {

	@ApiOperation(value = "Add a new anesthetic to examination", notes = "", response = Void.class, tags = {
			"ExaminationAnesthetic", })
	@ApiResponses(value = {
			@ApiResponse(code = 200, message = "success returns anesthetic examination", response = ExaminationAnesthetic.class),
			@ApiResponse(code = 400, message = "Invalid input / Bad Request", response = ExaminationAnesthetic.class),
			@ApiResponse(code = 409, message = "Already exists - conflict", response = ExaminationAnesthetic.class),
			@ApiResponse(code = 500, message = "Unexpected Error", response = ExaminationAnesthetic.class) })
	@RequestMapping(value = "/examination/{id}/anesthetic", produces = { "application/json" }, consumes = {
			"application/json" }, method = RequestMethod.POST)
	ResponseEntity<ExaminationAnesthetic> addExaminationAnesthetic(
			@ApiParam(value = "examination id", required = true) @PathVariable("id") Long id,
			@ApiParam(value = "anesthetic to add to examination", required = true) @RequestBody ExaminationAnesthetic anesthetic,
			BindingResult result) throws RestServiceException;

	@ApiOperation(value = "Deletes an anesthetic from examination", notes = "", response = Void.class, tags = {
			"ExaminationAnesthetic", })
	@ApiResponses(value = { @ApiResponse(code = 200, message = "Successful operation", response = Void.class),
			@ApiResponse(code = 400, message = "Invalid examination anesthetic id", response = Void.class),
			@ApiResponse(code = 500, message = "Unexpected Error", response = Void.class) })
	@RequestMapping(value = "/examination/{id}/anesthetic/{eaid}", produces = {
			"application/json" }, method = RequestMethod.DELETE)
	ResponseEntity<Void> deleteExaminationAnesthetic(
			@ApiParam(value = "examination id", required = true) @PathVariable("id") Long id,
			@ApiParam(value = "examination anesthetic id", required = true) @PathVariable("eaid") Long eaid)
			throws RestServiceException;

	@ApiOperation(value = "Get examination anesthetic by id", notes = "", response = ExaminationAnesthetic.class, responseContainer = "List", tags = {
			"ExaminationAnesthetic", })
	@ApiResponses(value = {
			@ApiResponse(code = 200, message = "An examination anesthetic", response = ExaminationAnesthetic.class),
			@ApiResponse(code = 404, message = "Examination anesthetic not found", response = Void.class),
			@ApiResponse(code = 500, message = "Unexpected error", response = ExaminationAnesthetic.class) })
	@RequestMapping(value = "/examination/{id}/anesthetic/{eaid}", produces = {
			"application/json" }, method = RequestMethod.GET)
	ResponseEntity<ExaminationAnesthetic> getExaminationAnestheticById(
			@ApiParam(value = "examination id", required = true) @PathVariable("id") Long id,
			@ApiParam(value = "Examination anesthetic id", required = true) @PathVariable("eaid") Long eaid)
			throws RestServiceException;

	@ApiOperation(value = "List all anesthetics for examination", notes = "", response = ExaminationAnesthetic.class, responseContainer = "List", tags = {
			"ExaminationAnesthetic", })
	@ApiResponses(value = {
			@ApiResponse(code = 200, message = "An array of examination anesthetics", response = ExaminationAnesthetic.class),
			@ApiResponse(code = 500, message = "Unexpected error", response = ExaminationAnesthetic.class) })
	@RequestMapping(value = "/examination/{id}/anesthetic/all", produces = {
			"application/json" }, method = RequestMethod.GET)
	ResponseEntity<List<ExaminationAnesthetic>> getExaminationAnesthetics(
			@ApiParam(value = "examination id", required = true) @PathVariable("id") Long id)
			throws RestServiceException;

	@ApiOperation(value = "Update an existing examination anesthetic", notes = "", response = Void.class, tags = {
			"ExaminationAnesthetic", })
	@ApiResponses(value = { @ApiResponse(code = 200, message = "Successful operation", response = Void.class),
			@ApiResponse(code = 400, message = "Invalid input / Bad Request", response = Void.class),
			@ApiResponse(code = 404, message = "Examination anesthetic not found", response = Void.class),
			@ApiResponse(code = 500, message = "Unexpected Error", response = Void.class) })
	@RequestMapping(value = "/examination/{id}/anesthetic/{eaid}", produces = { "application/json" }, consumes = {
			"application/json" }, method = RequestMethod.PUT)
	ResponseEntity<Void> updateExaminationAnesthetic(
			@ApiParam(value = "ID of examination", required = true) @PathVariable("id") Long id,
			@ApiParam(value = "ID of examination anesthetic that needs to be updated", required = true) @PathVariable("eaid") Long eaid,
			@ApiParam(value = "Examination Anesthetic that will be be updated", required = true) @RequestBody ExaminationAnesthetic anesthetic,
			final BindingResult result) throws RestServiceException;

	@ApiOperation(value = "List all examinationsAnesthetics for a given anesthetic", notes = "", response = ExaminationAnesthetic.class, responseContainer = "List", tags = {
			"ExaminationAnesthetic", })
	@ApiResponses(value = {
			@ApiResponse(code = 200, message = "An array of examination anesthetics", response = ExaminationAnesthetic.class),
			@ApiResponse(code = 500, message = "Unexpected error", response = ExaminationAnesthetic.class) })
	@RequestMapping(value = "/examination/all/anesthetic/{id}", produces = {
			"application/json" }, method = RequestMethod.GET)
	ResponseEntity<List<ExaminationAnesthetic>> getExaminationAnestheticsByAnesthetic(
			@ApiParam(value = "anesthetic id", required = true) @PathVariable("id") Long id)
			throws RestServiceException;

}

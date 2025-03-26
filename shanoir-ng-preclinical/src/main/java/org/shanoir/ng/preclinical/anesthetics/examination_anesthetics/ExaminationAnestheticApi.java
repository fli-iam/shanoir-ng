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

@Tag(name = "examination_anesthetic")
public interface ExaminationAnestheticApi {

	@Operation(summary = "Add a new anesthetic to examination", description = "")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "success returns anesthetic examination"),
			@ApiResponse(responseCode = "400", description = "Invalid input / Bad Request"),
			@ApiResponse(responseCode = "409", description = "Already exists - conflict"),
			@ApiResponse(responseCode = "500", description = "Unexpected Error") })
	@PostMapping(value = "/examination/{id}/anesthetic", produces = { "application/json" }, consumes = {
			"application/json" })
	ResponseEntity<ExaminationAnesthetic> addExaminationAnesthetic(
			@Parameter(name = "examination id", required = true) @PathVariable("id") Long id,
			@Parameter(name = "anesthetic to add to examination", required = true) @RequestBody ExaminationAnesthetic anesthetic,
			BindingResult result) throws RestServiceException;

	@Operation(summary = "Deletes an anesthetic from examination", description = "")
	@ApiResponses(value = { @ApiResponse(responseCode = "200", description = "Successful operation"),
			@ApiResponse(responseCode = "400", description = "Invalid examination anesthetic id"),
			@ApiResponse(responseCode = "500", description = "Unexpected Error") })
	@DeleteMapping(value = "/examination/{id}/anesthetic/{eaid}", produces = {
			"application/json" })
	ResponseEntity<Void> deleteExaminationAnesthetic(
			@Parameter(name = "examination id", required = true) @PathVariable("id") Long id,
			@Parameter(name = "examination anesthetic id", required = true) @PathVariable("eaid") Long eaid)
			throws RestServiceException;

	@Operation(summary = "Get examination anesthetic by id", description = "")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "An examination anesthetic"),
			@ApiResponse(responseCode = "404", description = "Examination anesthetic not found"),
			@ApiResponse(responseCode = "500", description = "Unexpected error") })
	@GetMapping(value = "/examination/{id}/anesthetic/{eaid}", produces = {
			"application/json" })
	ResponseEntity<ExaminationAnesthetic> getExaminationAnestheticById(
			@Parameter(name = "examination id", required = true) @PathVariable("id") Long id,
			@Parameter(name = "Examination anesthetic id", required = true) @PathVariable("eaid") Long eaid)
			throws RestServiceException;

	@Operation(summary = "List all anesthetics for examination", description = "")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "An array of examination anesthetics"),
			@ApiResponse(responseCode = "500", description = "Unexpected error") })
	@GetMapping(value = "/examination/{id}/anesthetic/all", produces = {
			"application/json" })
	ResponseEntity<List<ExaminationAnesthetic>> getExaminationAnesthetics(
			@Parameter(name = "examination id", required = true) @PathVariable("id") Long id)
			throws RestServiceException;

	@Operation(summary = "Update an existing examination anesthetic", description = "")
	@ApiResponses(value = { @ApiResponse(responseCode = "200", description = "Successful operation"),
			@ApiResponse(responseCode = "400", description = "Invalid input / Bad Request"),
			@ApiResponse(responseCode = "404", description = "Examination anesthetic not found"),
			@ApiResponse(responseCode = "500", description = "Unexpected Error") })
	@PutMapping(value = "/examination/{id}/anesthetic/{eaid}", produces = { "application/json" }, consumes = {
			"application/json" })
	ResponseEntity<Void> updateExaminationAnesthetic(
			@Parameter(name = "ID of examination", required = true) @PathVariable("id") Long id,
			@Parameter(name = "ID of examination anesthetic that needs to be updated", required = true) @PathVariable("eaid") Long eaid,
			@Parameter(name = "Examination Anesthetic that will be be updated", required = true) @RequestBody ExaminationAnesthetic anesthetic,
			final BindingResult result) throws RestServiceException;

	@Operation(summary = "List all examinationsAnesthetics for a given anesthetic", description = "")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "An array of examination anesthetics"),
			@ApiResponse(responseCode = "500", description = "Unexpected error") })
	@GetMapping(value = "/examination/all/anesthetic/{id}", produces = {
			"application/json" })
	ResponseEntity<List<ExaminationAnesthetic>> getExaminationAnestheticsByAnesthetic(
			@Parameter(name = "anesthetic id", required = true) @PathVariable("id") Long id)
			throws RestServiceException;

}

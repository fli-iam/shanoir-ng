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

package org.shanoir.ng.dua.controller;

import org.shanoir.ng.dua.dto.DuaDraftCreationWrapperDTO;
import org.shanoir.ng.dua.dto.DuaDraftDTO;
import org.shanoir.ng.shared.exception.RestServiceException;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;


@Tag(name = "duaDraft", description = "the dua draft API")
@RequestMapping("/dua")
public interface DuaDraftAPI {

	@Operation(summary = "", description = "Saves a new dua draft")
	@ApiResponses(value = { @ApiResponse(responseCode = "200", description = "created dua draft"),
			@ApiResponse(responseCode = "401", description = "unauthorized"),
			@ApiResponse(responseCode = "403", description = "forbidden"),
			@ApiResponse(responseCode = "422", description = "bad parameters"),
			@ApiResponse(responseCode = "500", description = "unexpected error") })
	@RequestMapping(value = "", produces = { "application/json" }, consumes = {
			"application/json" }, method = RequestMethod.POST)
	@PreAuthorize("hasAnyRole('ADMIN', 'EXPERT') and @studySecurityService.hasRightOnStudy(#dua.duaDraft.studyId, 'CAN_ADMINISTRATE')")
	ResponseEntity<String> saveNew(
			@Parameter(description = "dua draft to create", required = true) @Valid @RequestBody DuaDraftCreationWrapperDTO dua, BindingResult result)
			throws RestServiceException;

	@Operation(summary = "", description = "Updates a dua draft")
	@ApiResponses(value = { @ApiResponse(responseCode = "204", description = "dua draft updated"),
			@ApiResponse(responseCode = "401", description = "unauthorized"),
			@ApiResponse(responseCode = "403", description = "forbidden"),
			@ApiResponse(responseCode = "422", description = "bad parameters"),
			@ApiResponse(responseCode = "500", description = "unexpected error") })
	@RequestMapping(value = "/{duaId}", produces = { "application/json" }, consumes = {
			"application/json" }, method = RequestMethod.PUT)
	ResponseEntity<Void> update(
			@Parameter(description = "id of the draft", required = true) @PathVariable("duaId") String duaId,
			@Parameter(description = "study to update", required = true) @Valid @RequestBody DuaDraftDTO dua, BindingResult result)
			throws RestServiceException;

	@Operation(summary = "", description = "If exists, returns the dua draft corresponding to the given id")
	@ApiResponses(value = { @ApiResponse(responseCode = "200", description = "found dua draft"),
			@ApiResponse(responseCode = "401", description = "unauthorized"),
			@ApiResponse(responseCode = "403", description = "forbidden"),
			@ApiResponse(responseCode = "404", description = "no study found"),
			@ApiResponse(responseCode = "500", description = "unexpected error") })
	@RequestMapping(value = "/{duaId}", produces = { "application/json" }, method = RequestMethod.GET)
	ResponseEntity<DuaDraftDTO> findById(
			@Parameter(description = "id of the dua draft", required = true) @PathVariable("duaId") String duaId);
}

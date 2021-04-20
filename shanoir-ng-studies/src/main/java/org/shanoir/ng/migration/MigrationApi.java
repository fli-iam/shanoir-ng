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

package org.shanoir.ng.migration;

import org.shanoir.ng.shared.exception.RestServiceException;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

@Api(value = "migration")
@RequestMapping("/migration")
public interface MigrationApi {

	@ApiOperation(value = "", notes = "Migrates a study", response = Void.class, tags = {})
	@ApiResponses(value = { @ApiResponse(code = 204, message = "study updated", response = Void.class),
			@ApiResponse(code = 401, message = "unauthorized", response = Void.class),
			@ApiResponse(code = 403, message = "forbidden", response = Void.class),
			@ApiResponse(code = 422, message = "bad parameters", response = Void.class),
			@ApiResponse(code = 500, message = "unexpected error", response = Void.class) })
	@PostMapping(value = "/{studyId}", produces = { "application/json" }, consumes = {
			"application/json" })
	@PreAuthorize("hasAnyRole('ADMIN', 'EXPERT') and @controlerSecurityService.idMatches(#studyId, #study) and @studySecurityService.hasRightOnStudy(#studyId, 'CAN_ADMINISTRATE')")
	ResponseEntity<Void> migrateStudy (
			@ApiParam(value = "id of the study", required = true) @PathVariable("studyId") Long studyId,
			@ApiParam(value = "url of distant shanoir", required = true) @RequestBody String shanoirUrl)
			throws RestServiceException;

	@ApiOperation(value = "", notes = "Connects to a distant shanoir", response = Void.class, tags = {})
	@ApiResponses(value = { @ApiResponse(code = 204, message = "connected", response = Void.class),
			@ApiResponse(code = 401, message = "unauthorized", response = Void.class),
			@ApiResponse(code = 403, message = "forbidden", response = Void.class),
			@ApiResponse(code = 422, message = "bad parameters", response = Void.class),
			@ApiResponse(code = 500, message = "unexpected error", response = Void.class) })
	@PostMapping(value = "/connect", produces = { "application/json" }, consumes = {
			"application/json" })
	ResponseEntity<Void> connect (
			@ApiParam(value = "Url of distant shanoir", required = true) @RequestParam("shanoirUrl") String shanoirUrl,
			@ApiParam(value = "Username of user", required = true) @RequestParam("username") String username,
			@ApiParam(value = "Password of user", required = true) @RequestParam("userPassword") String userPassword)
			throws RestServiceException;

}

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

import java.io.IOException;
import java.util.List;

import org.shanoir.ng.shared.core.model.IdName;
import org.shanoir.ng.shared.exception.RestServiceException;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "migration")
@RequestMapping("/migration")
public interface MigrationApi {

	@Operation(summary = "", description = "Connects to a distant shanoir", tags = {})
	@ApiResponses(value = { 
			@ApiResponse(responseCode = "204", description = "connected"),
			@ApiResponse(responseCode = "401", description = "unauthorized"),
			@ApiResponse(responseCode = "403", description = "forbidden"),
			@ApiResponse(responseCode = "422", description = "bad parameters"),
			@ApiResponse(responseCode = "500", description = "unexpected error") })
	@GetMapping(value = "/migrate", produces = { "application/json" })
	@PreAuthorize("hasRole('ADMIN')")
	ResponseEntity<String> migrateStudy (
			@Parameter(name = "Url of distant shanoir", required = true) @RequestParam("shanoirUrl") Integer shanoirUrl,
			@Parameter(name = "Username of user", required = true) @RequestParam("username") String username,
			@Parameter(name = "Password of user", required = true) @RequestParam("userPassword") String userPassword,
			@Parameter(name = "study ID", required = true) @RequestParam("studyId") Long studyId,
			@Parameter(name = "Distant user ID", required = true) @RequestParam("userId") Long userId)
			throws RestServiceException;

	@Operation(summary = "", description = "Get migration configuration", tags = {})
	@ApiResponses(value = { 
			@ApiResponse(responseCode = "204", description = "no value"),
			@ApiResponse(responseCode = "401", description = "unauthorized"),
			@ApiResponse(responseCode = "403", description = "forbidden"),
			@ApiResponse(responseCode = "404", description = "no config found"),
			@ApiResponse(responseCode = "500", description = "unexpected error") })
	@GetMapping(value = "urls", produces = {
			"application/json" })
	@PreAuthorize("hasRole('ADMIN')")
	ResponseEntity<List<IdName>> getMigrationConfig() throws IOException;

}

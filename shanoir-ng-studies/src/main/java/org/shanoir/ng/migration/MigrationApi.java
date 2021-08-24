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

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

@Api(value = "migration")
@RequestMapping("/migration")
public interface MigrationApi {

	@ApiOperation(value = "", notes = "Connects to a distant shanoir", response = Void.class, tags = {})
	@ApiResponses(value = { @ApiResponse(code = 204, message = "connected", response = Void.class),
			@ApiResponse(code = 401, message = "unauthorized", response = Void.class),
			@ApiResponse(code = 403, message = "forbidden", response = Void.class),
			@ApiResponse(code = 422, message = "bad parameters", response = Void.class),
			@ApiResponse(code = 500, message = "unexpected error", response = Void.class) })
	@GetMapping(value = "/migrate", produces = { "application/json" })
	@PreAuthorize("hasRole('ADMIN')")
	ResponseEntity<String> migrateStudy (
			@ApiParam(value = "Url of distant shanoir", required = true) @RequestParam("shanoirUrl") Integer shanoirUrl,
			@ApiParam(value = "Username of user", required = true) @RequestParam("username") String username,
			@ApiParam(value = "Password of user", required = true) @RequestParam("userPassword") String userPassword,
			@ApiParam(value = "study ID", required = true) @RequestParam("studyId") Long studyId,
			@ApiParam(value = "Distant user ID", required = true) @RequestParam("userId") Long userId)
			throws RestServiceException;

	@ApiOperation(value = "", notes = "Get migration configuration", response = String.class, tags = {})
	@ApiResponses(value = { @ApiResponse(code = 204, message = "no value", response = Void.class),
			@ApiResponse(code = 401, message = "unauthorized", response = Void.class),
			@ApiResponse(code = 403, message = "forbidden", response = Void.class),
			@ApiResponse(code = 404, message = "no config found", response = Void.class),
			@ApiResponse(code = 500, message = "unexpected error", response = Void.class) })
	@GetMapping(value = "urls", produces = {
			"application/json" })
	@PreAuthorize("hasRole('ADMIN')")
	ResponseEntity<List<IdName>> getMigrationConfig() throws IOException;

}

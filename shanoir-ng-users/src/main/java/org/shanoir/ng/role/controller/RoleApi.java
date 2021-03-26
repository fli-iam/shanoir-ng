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

package org.shanoir.ng.role.controller;

import java.util.List;

import org.shanoir.ng.role.model.Role;
import org.shanoir.ng.shared.exception.ErrorModel;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

@Api(value = "role")
@RequestMapping("/roles")
public interface RoleApi {

	@ApiOperation(value = "", notes = "Returns all the roles", response = Role.class, responseContainer = "List", tags = {})
	@ApiResponses(value = { @ApiResponse(code = 200, message = "found roles", response = Role.class),
			@ApiResponse(code = 204, message = "no role found", response = Role.class),
			@ApiResponse(code = 401, message = "unauthorized", response = Role.class),
			@ApiResponse(code = 403, message = "forbidden", response = Role.class),
			@ApiResponse(code = 500, message = "unexpected error", response = ErrorModel.class) })
	@GetMapping(value = "", produces = { "application/json" })
	ResponseEntity<List<Role>> findRoles();

}

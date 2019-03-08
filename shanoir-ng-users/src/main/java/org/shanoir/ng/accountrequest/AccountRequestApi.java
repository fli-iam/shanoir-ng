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

package org.shanoir.ng.accountrequest;

import org.shanoir.ng.shared.exception.RestServiceException;
import org.shanoir.ng.user.User;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

@javax.annotation.Generated(value = "io.swagger.codegen.languages.SpringCodegen", date = "2017-03-07T15:52:25.736Z")

@Api(value = "accountrequest", description = "the accountrequest API")
@RequestMapping("/accountrequest")
public interface AccountRequestApi {

	@ApiOperation(value = "", notes = "Saves a new user from account request", response = User.class, tags = {})
	@ApiResponses(value = {
			@ApiResponse(code = 200, message = "created user from account request", response = User.class),
			@ApiResponse(code = 401, message = "unauthorized", response = User.class),
			@ApiResponse(code = 403, message = "forbidden", response = User.class),
			@ApiResponse(code = 422, message = "bad parameters", response = User.class),
			@ApiResponse(code = 500, message = "unexpected error", response = User.class) })
	@RequestMapping(value = "", produces = { "application/json" }, consumes = {
			"application/json" }, method = RequestMethod.POST)
	ResponseEntity<Void> saveNewAccountRequest(
			@ApiParam(value = "user to create from account request", required = true) @RequestBody User user,
			BindingResult result) throws RestServiceException;

}

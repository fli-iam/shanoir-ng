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

package org.shanoir.ng.user.controller;

import java.util.List;

import org.shanoir.ng.shared.core.model.IdList;
import org.shanoir.ng.shared.core.model.IdName;
import org.shanoir.ng.shared.exception.ForbiddenException;
import org.shanoir.ng.shared.exception.RestServiceException;
import org.shanoir.ng.user.model.User;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PostAuthorize;
import org.springframework.security.access.prepost.PreAuthorize;
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

@javax.annotation.Generated(value = "io.swagger.codegen.languages.SpringCodegen", date = "2017-03-07T15:52:25.736Z")

@Api(value = "user", description = "the user API")
@RequestMapping("/users")
public interface UserApi {

	@ApiOperation(value = "", notes = "Confirms an account request", response = Void.class, tags = {})
	@ApiResponses(value = { @ApiResponse(code = 204, message = "user confirmed", response = Void.class),
			@ApiResponse(code = 401, message = "unauthorized", response = Void.class),
			@ApiResponse(code = 403, message = "forbidden", response = Void.class),
			@ApiResponse(code = 404, message = "no user found", response = Void.class),
			@ApiResponse(code = 500, message = "unexpected error", response = Void.class) })
	@RequestMapping(value = "/{userId}/confirmaccountrequest", produces = { "application/json" }, consumes = {
			"application/json" }, method = RequestMethod.PUT)
	@PreAuthorize("hasRole('ADMIN') and @controlerSecurityService.idMatches(#userId, #user)")
	ResponseEntity<Void> confirmAccountRequest(
			@ApiParam(value = "id of the user", required = true) @PathVariable("userId") Long userId,
			@ApiParam(value = "user to update", required = true) @RequestBody User user, BindingResult result)
			throws RestServiceException;

	@ApiOperation(value = "", notes = "Deletes a user", response = Void.class, tags = {})
	@ApiResponses(value = { @ApiResponse(code = 204, message = "user deleted", response = Void.class),
			@ApiResponse(code = 401, message = "unauthorized", response = Void.class),
			@ApiResponse(code = 403, message = "forbidden", response = Void.class),
			@ApiResponse(code = 404, message = "no user found", response = Void.class),
			@ApiResponse(code = 500, message = "unexpected error", response = Void.class) })
	@RequestMapping(value = "/{userId}", produces = { "application/json" }, method = RequestMethod.DELETE)
	@PreAuthorize("hasRole('ADMIN') and !@isMeSecurityService.isMe(#userId)")
	ResponseEntity<Void> deleteUser(
			@ApiParam(value = "id of the user", required = true) @PathVariable("userId") Long userId) throws ForbiddenException;

	@ApiOperation(value = "", notes = "Denies an account request", response = Void.class, tags = {})
	@ApiResponses(value = { @ApiResponse(code = 204, message = "user deleted", response = Void.class),
			@ApiResponse(code = 401, message = "unauthorized", response = Void.class),
			@ApiResponse(code = 403, message = "forbidden", response = Void.class),
			@ApiResponse(code = 404, message = "no user found", response = Void.class),
			@ApiResponse(code = 500, message = "unexpected error", response = Void.class) })
	@RequestMapping(value = "/{userId}/denyaccountrequest", produces = {
			"application/json" }, method = RequestMethod.DELETE)
	@PreAuthorize("hasRole('ADMIN')")
	ResponseEntity<Void> denyAccountRequest(
			@ApiParam(value = "id of the user", required = true) @PathVariable("userId") Long userId) throws RestServiceException;

	@ApiOperation(value = "", notes = "If exists, returns the user corresponding to the given id", response = User.class, tags = {})
	@ApiResponses(value = { @ApiResponse(code = 200, message = "found user", response = User.class),
			@ApiResponse(code = 401, message = "unauthorized", response = User.class),
			@ApiResponse(code = 403, message = "forbidden", response = User.class),
			@ApiResponse(code = 404, message = "no user found", response = User.class),
			@ApiResponse(code = 500, message = "unexpected error", response = User.class) })
	@RequestMapping(value = "/{userId}", produces = { "application/json" }, method = RequestMethod.GET)
	@PreAuthorize("hasRole('ADMIN') or (hasAnyRole('USER', 'EXPERT') and @isMeSecurityService.isMe(#userId))")
	ResponseEntity<User> findUserById(
			@ApiParam(value = "id of the user", required = true) @PathVariable("userId") Long userId);

	@ApiOperation(value = "", notes = "Returns all the users", response = User.class, responseContainer = "List", tags = {})
	@ApiResponses(value = { @ApiResponse(code = 200, message = "found users", response = User.class),
			@ApiResponse(code = 204, message = "no user found", response = User.class),
			@ApiResponse(code = 401, message = "unauthorized", response = User.class),
			@ApiResponse(code = 403, message = "forbidden", response = User.class),
			@ApiResponse(code = 500, message = "unexpected error", response = User.class) })
	@RequestMapping(value = "", produces = { "application/json" }, method = RequestMethod.GET)
	@PreAuthorize("hasAnyRole('ADMIN', 'EXPERT')")
	@PostAuthorize("hasRole('ADMIN') or @userPrivacySecurityService.filterPersonnalData(returnObject.getBody())")
	ResponseEntity<List<User>> findUsers();

	@ApiOperation(value = "", notes = "Saves a new user", response = User.class, tags = {})
	@ApiResponses(value = { @ApiResponse(code = 200, message = "created user", response = User.class),
			@ApiResponse(code = 401, message = "unauthorized", response = User.class),
			@ApiResponse(code = 403, message = "forbidden", response = User.class),
			@ApiResponse(code = 422, message = "bad parameters", response = User.class),
			@ApiResponse(code = 500, message = "unexpected error", response = User.class) })
	@RequestMapping(value = "", produces = { "application/json" }, consumes = {
			"application/json" }, method = RequestMethod.POST)
	@PreAuthorize("hasRole('ADMIN')")
	ResponseEntity<User> saveNewUser(@ApiParam(value = "user to create", required = true) @RequestBody User user,
			BindingResult result) throws RestServiceException;

	@ApiOperation(value = "", notes = "Requests users by id list", response = IdName.class, tags = {})
	@ApiResponses(value = { @ApiResponse(code = 200, message = "found users", response = IdName.class),
			@ApiResponse(code = 401, message = "unauthorized", response = IdName.class),
			@ApiResponse(code = 403, message = "forbidden", response = IdName.class),
			@ApiResponse(code = 404, message = "no userfound", response = IdName.class),
			@ApiResponse(code = 500, message = "unexpected error", response = IdName.class) })
	@RequestMapping(value = "/search", produces = { "application/json" }, consumes = {
			"application/json" }, method = RequestMethod.POST)
	@PreAuthorize("hasAnyRole('USER', 'ADMIN', 'EXPERT')")
	ResponseEntity<List<IdName>> searchUsers(
			@ApiParam(value = "user ids", required = true) @RequestBody IdList userIds);

	@ApiOperation(value = "", notes = "Updates a user", response = Void.class, tags = {})
	@ApiResponses(value = { @ApiResponse(code = 204, message = "user updated", response = Void.class),
			@ApiResponse(code = 401, message = "unauthorized", response = Void.class),
			@ApiResponse(code = 403, message = "forbidden", response = Void.class),
			@ApiResponse(code = 422, message = "bad parameters", response = Void.class),
			@ApiResponse(code = 500, message = "unexpected error", response = Void.class) })
	@RequestMapping(value = "/{userId}", produces = { "application/json" }, consumes = {
			"application/json" }, method = RequestMethod.PUT)
	@PreAuthorize("hasRole('ADMIN') or (hasAnyRole('USER', 'EXPERT') and @isMeSecurityService.isMe(#userId)) and @controlerSecurityService.idMatches(#userId, #user)")
	ResponseEntity<Void> updateUser(
			@ApiParam(value = "id of the user", required = true) @PathVariable("userId") Long userId,
			@ApiParam(value = "user to update", required = true) @RequestBody User user, BindingResult result)
			throws RestServiceException;

}

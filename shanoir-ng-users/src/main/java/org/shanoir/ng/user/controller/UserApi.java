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

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "user", description = "the user API")
@RequestMapping("/users")
public interface UserApi {

	@Operation(summary = "", description = "Confirms an account request")
	@ApiResponses(value = { @ApiResponse(responseCode = "204", description = "user confirmed"),
			@ApiResponse(responseCode = "401", description = "unauthorized"),
			@ApiResponse(responseCode = "403", description = "forbidden"),
			@ApiResponse(responseCode = "404", description = "no user found"),
			@ApiResponse(responseCode = "500", description = "unexpected error") })
	@RequestMapping(value = "/{userId}/confirmaccountrequest", produces = { "application/json" }, consumes = {
			"application/json" }, method = RequestMethod.PUT)
	@PreAuthorize("hasRole('ADMIN') and @controllerSecurityService.idMatches(#userId, #user)")
	ResponseEntity<Void> confirmAccountRequest(
			@Parameter(name = "id of the user", required = true) @PathVariable("userId") Long userId,
			@Parameter(name = "user to update", required = true) @RequestBody User user, BindingResult result)
			throws RestServiceException;

	@Operation(summary = "", description = "Deletes a user")
	@ApiResponses(value = { @ApiResponse(responseCode = "204", description = "user deleted"),
			@ApiResponse(responseCode = "401", description = "unauthorized"),
			@ApiResponse(responseCode = "403", description = "forbidden"),
			@ApiResponse(responseCode = "404", description = "no user found"),
			@ApiResponse(responseCode = "500", description = "unexpected error") })
	@RequestMapping(value = "/{userId}", produces = { "application/json" }, method = RequestMethod.DELETE)
	@PreAuthorize("hasRole('ADMIN') and !@isMeSecurityService.isMe(#userId)")
	ResponseEntity<Void> deleteUser(
			@Parameter(name = "id of the user", required = true) @PathVariable("userId") Long userId) throws ForbiddenException;

	@Operation(summary = "", description = "Denies an account request")
	@ApiResponses(value = { @ApiResponse(responseCode = "204", description = "user deleted"),
			@ApiResponse(responseCode = "401", description = "unauthorized"),
			@ApiResponse(responseCode = "403", description = "forbidden"),
			@ApiResponse(responseCode = "404", description = "no user found"),
			@ApiResponse(responseCode = "500", description = "unexpected error") })
	@RequestMapping(value = "/{userId}/denyaccountrequest", produces = {
			"application/json" }, method = RequestMethod.DELETE)
	@PreAuthorize("hasRole('ADMIN')")
	ResponseEntity<Void> denyAccountRequest(
			@Parameter(name = "id of the user", required = true) @PathVariable("userId") Long userId) throws RestServiceException;

	@Operation(summary = "", description = "If exists, returns the user corresponding to the given id")
	@ApiResponses(value = { @ApiResponse(responseCode = "200", description = "found user"),
			@ApiResponse(responseCode = "401", description = "unauthorized"),
			@ApiResponse(responseCode = "403", description = "forbidden"),
			@ApiResponse(responseCode = "404", description = "no user found"),
			@ApiResponse(responseCode = "500", description = "unexpected error") })
	@RequestMapping(value = "/{userId}", produces = { "application/json" }, method = RequestMethod.GET)
	@PreAuthorize("hasRole('ADMIN') or (hasAnyRole('USER', 'EXPERT') and @isMeSecurityService.isMe(#userId))")
	ResponseEntity<User> findUserById(
			@Parameter(name = "id of the user", required = true) @PathVariable("userId") Long userId);

	@Operation(summary = "", description = "Returns all the users")
	@ApiResponses(value = { @ApiResponse(responseCode = "200", description = "found users"),
			@ApiResponse(responseCode = "204", description = "no user found"),
			@ApiResponse(responseCode = "401", description = "unauthorized"),
			@ApiResponse(responseCode = "403", description = "forbidden"),
			@ApiResponse(responseCode = "500", description = "unexpected error") })
	@RequestMapping(value = "", produces = { "application/json" }, method = RequestMethod.GET)
	@PostAuthorize("hasRole('ADMIN') or @userPrivacySecurityService.filterPersonnalData(returnObject.getBody())")
	ResponseEntity<List<User>> findUsers();

	@Operation(summary = "", description = "Returns all the users on account request")
	@ApiResponses(value = { @ApiResponse(responseCode = "200", description = "found users"),
			@ApiResponse(responseCode = "204", description = "no user found"),
			@ApiResponse(responseCode = "401", description = "unauthorized"),
			@ApiResponse(responseCode = "403", description = "forbidden"),
			@ApiResponse(responseCode = "500", description = "unexpected error") })
	@RequestMapping(value = "/accountRequests", produces = { "application/json" }, method = RequestMethod.GET)
	@PreAuthorize("hasAnyRole('ADMIN', 'EXPERT')")
	@PostAuthorize("hasRole('ADMIN') or @userPrivacySecurityService.filterPersonnalData(returnObject.getBody())")
	ResponseEntity<List<User>> findAccountRequests();
	
	@Operation(summary = "", description = "Saves a new user")
	@ApiResponses(value = { @ApiResponse(responseCode = "200", description = "created user"),
			@ApiResponse(responseCode = "401", description = "unauthorized"),
			@ApiResponse(responseCode = "403", description = "forbidden"),
			@ApiResponse(responseCode = "422", description = "bad parameters"),
			@ApiResponse(responseCode = "500", description = "unexpected error") })
	@RequestMapping(value = "", produces = { "application/json" }, consumes = {
			"application/json" }, method = RequestMethod.POST)
	@PreAuthorize("hasRole('ADMIN')")
	ResponseEntity<User> saveNewUser(@Parameter(name = "user to create", required = true) @RequestBody User user,
			BindingResult result) throws RestServiceException;

	@Operation(summary = "", description = "Requests users by id list")
	@ApiResponses(value = { @ApiResponse(responseCode = "200", description = "found users"),
			@ApiResponse(responseCode = "401", description = "unauthorized"),
			@ApiResponse(responseCode = "403", description = "forbidden"),
			@ApiResponse(responseCode = "404", description = "no userfound"),
			@ApiResponse(responseCode = "500", description = "unexpected error") })
	@RequestMapping(value = "/search", produces = { "application/json" }, consumes = {
			"application/json" }, method = RequestMethod.POST)
	@PreAuthorize("hasAnyRole('USER', 'ADMIN', 'EXPERT')")
	ResponseEntity<List<IdName>> searchUsers(
			@Parameter(name = "user ids", required = true) @RequestBody IdList userIds);

	@Operation(summary = "", description = "Updates a user")
	@ApiResponses(value = { @ApiResponse(responseCode = "204", description = "user updated"),
			@ApiResponse(responseCode = "401", description = "unauthorized"),
			@ApiResponse(responseCode = "403", description = "forbidden"),
			@ApiResponse(responseCode = "422", description = "bad parameters"),
			@ApiResponse(responseCode = "500", description = "unexpected error") })
	@RequestMapping(value = "/{userId}", produces = { "application/json" }, consumes = {
			"application/json" }, method = RequestMethod.PUT)
	@PreAuthorize("hasRole('ADMIN') or (hasAnyRole('USER', 'EXPERT') and @isMeSecurityService.isMe(#userId)) and @controllerSecurityService.idMatches(#userId, #user)")
	ResponseEntity<Void> updateUser(
			@Parameter(name = "id of the user", required = true) @PathVariable("userId") Long userId,
			@Parameter(name = "user to update", required = true) @RequestBody User user, BindingResult result)
			throws RestServiceException;

}

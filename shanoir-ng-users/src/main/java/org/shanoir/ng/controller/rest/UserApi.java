package org.shanoir.ng.controller.rest;

import java.util.List;

import org.keycloak.KeycloakPrincipal;
import org.keycloak.adapters.RefreshableKeycloakSecurityContext;
import org.shanoir.ng.exception.RestServiceException;
import org.shanoir.ng.model.User;
import org.springframework.http.ResponseEntity;
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

@javax.annotation.Generated(value = "class io.swagger.codegen.languages.SpringCodegen", date = "2016-11-18T15:36:13.002Z")

@Api(value = "user", description = "the user API")
@RequestMapping("/user")
public interface UserApi {

	@ApiOperation(value = "", notes = "Confirms an account request", response = Void.class, tags = {})
	@ApiResponses(value = { @ApiResponse(code = 204, message = "user confirmed", response = Void.class),
			@ApiResponse(code = 401, message = "unauthorized", response = Void.class),
			@ApiResponse(code = 403, message = "forbidden", response = Void.class),
			@ApiResponse(code = 404, message = "no user found", response = Void.class),
			@ApiResponse(code = 500, message = "unexpected error", response = Void.class) })
	@RequestMapping(value = "/{userId}/confirmaccountrequest", produces = { "application/json" }, consumes = {
			"application/json" }, method = RequestMethod.PUT)
	@PreAuthorize("hasAuthority('adminRole')")
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
	@PreAuthorize("hasAuthority('adminRole')")
	ResponseEntity<Void> deleteUser(
			@ApiParam(value = "id of the user", required = true) @PathVariable("userId") Long userId);

	@ApiOperation(value = "", notes = "Denies an account request", response = Void.class, tags = {})
	@ApiResponses(value = { @ApiResponse(code = 204, message = "user deleted", response = Void.class),
			@ApiResponse(code = 401, message = "unauthorized", response = Void.class),
			@ApiResponse(code = 403, message = "forbidden", response = Void.class),
			@ApiResponse(code = 404, message = "no user found", response = Void.class),
			@ApiResponse(code = 500, message = "unexpected error", response = Void.class) })
	@RequestMapping(value = "/{userId}/denyaccountrequest", produces = {
			"application/json" }, method = RequestMethod.DELETE)
	@PreAuthorize("hasAuthority('adminRole')")
	ResponseEntity<Void> denyAccountRequest(
			@ApiParam(value = "id of the user", required = true) @PathVariable("userId") Long userId)
			throws RestServiceException;

	@ApiOperation(value = "", notes = "If exists, returns the user corresponding to the given id", response = User.class, tags = {})
	@ApiResponses(value = { @ApiResponse(code = 200, message = "found user", response = User.class),
			@ApiResponse(code = 401, message = "unauthorized", response = User.class),
			@ApiResponse(code = 403, message = "forbidden", response = User.class),
			@ApiResponse(code = 404, message = "no user found", response = User.class),
			@ApiResponse(code = 500, message = "unexpected error", response = User.class) })
	@RequestMapping(value = "/{userId}", produces = { "application/json" }, method = RequestMethod.GET)
	@PreAuthorize("@currentUserServiceImpl.canAccessUser(#userId)")
	ResponseEntity<User> findUserById(
			@ApiParam(value = "id of the user", required = true) @PathVariable("userId") Long userId);

	@ApiOperation(value = "", notes = "Returns all the users", response = User.class, responseContainer = "List", tags = {})
	@ApiResponses(value = { @ApiResponse(code = 200, message = "found users", response = User.class),
			@ApiResponse(code = 204, message = "no user found", response = User.class),
			@ApiResponse(code = 401, message = "unauthorized", response = User.class),
			@ApiResponse(code = 403, message = "forbidden", response = User.class),
			@ApiResponse(code = 500, message = "unexpected error", response = User.class) })
	@RequestMapping(value = "/all", produces = { "application/json" }, method = RequestMethod.GET)
//	@PreAuthorize("hasAuthority('adminRole')")
	ResponseEntity<List<User>> findUsers(KeycloakPrincipal<RefreshableKeycloakSecurityContext> principal);

	@ApiOperation(value = "", notes = "Saves a new user", response = User.class, tags = {})
	@ApiResponses(value = { @ApiResponse(code = 200, message = "created user", response = User.class),
			@ApiResponse(code = 401, message = "unauthorized", response = User.class),
			@ApiResponse(code = 403, message = "forbidden", response = User.class),
			@ApiResponse(code = 422, message = "bad parameters", response = User.class),
			@ApiResponse(code = 500, message = "unexpected error", response = User.class) })
	@RequestMapping(value = "", produces = { "application/json" }, consumes = {
			"application/json" }, method = RequestMethod.POST)
	ResponseEntity<User> saveNewUser(@ApiParam(value = "user to create", required = true) @RequestBody User user,
			BindingResult result) throws RestServiceException;

	@ApiOperation(value = "", notes = "Updates a user", response = Void.class, tags = {})
	@ApiResponses(value = { @ApiResponse(code = 204, message = "user updated", response = Void.class),
			@ApiResponse(code = 401, message = "unauthorized", response = Void.class),
			@ApiResponse(code = 403, message = "forbidden", response = Void.class),
			@ApiResponse(code = 422, message = "bad parameters", response = Void.class),
			@ApiResponse(code = 500, message = "unexpected error", response = Void.class) })
	@RequestMapping(value = "/{userId}", produces = { "application/json" }, consumes = {
			"application/json" }, method = RequestMethod.PUT)
	@PreAuthorize("@currentUserServiceImpl.canAccessUser(#userId)")
	ResponseEntity<Void> updateUser(
			@ApiParam(value = "id of the user", required = true) @PathVariable("userId") Long userId,
			@ApiParam(value = "user to update", required = true) @RequestBody User user, BindingResult result)
			throws RestServiceException;

}

package org.shanoir.ng.controller.rest;


import java.util.List;

import org.shanoir.ng.model.ErrorModel;
import org.shanoir.ng.model.User;
import org.springframework.http.ResponseEntity;
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
public interface UserApi {

	@ApiOperation(value = "", notes = "Deletes a user", response = Void.class, tags = {})
	@ApiResponses(value = {
			@ApiResponse(code = 204, message = "user deleted", response = Void.class),
			@ApiResponse(code = 404, message = "no user founded", response = Void.class),
			@ApiResponse(code = 500, message = "unexpected error", response = Void.class) })
	@RequestMapping(value = "/user/{userId}", produces = { "application/json" }, method = RequestMethod.DELETE)
	ResponseEntity<Void> deleteUser(
			@ApiParam(value = "id of the user", required = true) @PathVariable("userId") Long userId);

	@ApiOperation(value = "", notes = "If exists, returns the user corresponding to the given id", response = User.class, tags = {})
	@ApiResponses(value = {
			@ApiResponse(code = 200, message = "founded user", response = User.class),
			@ApiResponse(code = 404, message = "no user founded", response = Void.class),
			@ApiResponse(code = 500, message = "unexpected error", response = ErrorModel.class) })
	@RequestMapping(value = "/user/{userId}", produces = { "application/json" }, method = RequestMethod.GET)
	ResponseEntity<User> findUserById(
			@ApiParam(value = "id of the user", required = true) @PathVariable("userId") Long userId);

	@ApiOperation(value = "", notes = "Returns all the users", response = User.class, tags = {})
	@ApiResponses(value = {
			@ApiResponse(code = 200, message = "founded users", response = User.class, responseContainer = "List"),
			@ApiResponse(code = 204, message = "no user founded", response = Void.class),
			@ApiResponse(code = 500, message = "unexpected error", response = ErrorModel.class) })
	@RequestMapping(value = "/user/all", produces = { "application/json" }, method = RequestMethod.GET)
	ResponseEntity<List<User>> findUsers();

	@ApiOperation(value = "", notes = "Saves a new user", response = User.class, tags = {})
	@ApiResponses(value = {
			@ApiResponse(code = 200, message = "created user", response = User.class),
			@ApiResponse(code = 422, message = "bad parameters", response = ErrorModel.class),
			@ApiResponse(code = 500, message = "unexpected error", response = ErrorModel.class) })
	@RequestMapping(value = "/user", produces = { "application/json" }, consumes = {
			"application/json" }, method = RequestMethod.POST)
	ResponseEntity<User> saveNewUser(@ApiParam(value = "the user to create", required = true) @RequestBody User user);

	@ApiOperation(value = "", notes = "Updates a user", response = Void.class, tags = {})
	@ApiResponses(value = {
			@ApiResponse(code = 204, message = "user updated", response = Void.class),
			@ApiResponse(code = 422, message = "bad parameters", response = ErrorModel.class),
			@ApiResponse(code = 500, message = "unexpected error", response = ErrorModel.class) })
	@RequestMapping(value = "/user/{userId}", produces = { "application/json" }, consumes = {
			"application/json" }, method = RequestMethod.PUT)
	ResponseEntity<Void> updateUser(
			@ApiParam(value = "id of the user", required = true) @PathVariable("userId") Long userId,
			@ApiParam(value = "the user to update", required = true) @RequestBody User user);

}

package org.shanoir.ng.accountrequest.controller;

import org.shanoir.ng.shared.exception.RestServiceException;
import org.shanoir.ng.user.model.User;
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

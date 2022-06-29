package org.shanoir.ng.accessrequest.controller;

import org.shanoir.ng.accessrequest.model.AccessRequest;
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

/**
 * Api for access request, to make a demand on 
 * @author jcome
 *
 */
@Api(value = "accountrequest")
@RequestMapping("/accountrequest")
public interface AccessRequestApi {

	@ApiOperation(value = "", notes = "Saves a new access request", response = User.class, tags = {})
	@ApiResponses(value = {
			@ApiResponse(code = 200, message = "created access request", response = AccessRequest.class),
			@ApiResponse(code = 401, message = "unauthorized", response = User.class),
			@ApiResponse(code = 403, message = "forbidden", response = User.class),
			@ApiResponse(code = 422, message = "bad parameters", response = User.class),
			@ApiResponse(code = 500, message = "unexpected error", response = User.class) })
	@RequestMapping(value = "", produces = { "application/json" }, consumes = {
			"application/json" }, method = RequestMethod.POST)
	ResponseEntity<Void> saveNewAccessRequest(
			@ApiParam(value = "uaccess request to create", required = true) @RequestBody AccessRequest request,
			BindingResult result) throws RestServiceException;
}

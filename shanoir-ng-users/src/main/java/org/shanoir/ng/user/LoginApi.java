package org.shanoir.ng.user;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.Context;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

@javax.annotation.Generated(value = "io.swagger.codegen.languages.SpringCodegen", date = "2017-03-16T08:28:10.257Z")

@Api(value = "login", description = "the login API")
@RequestMapping("/login")
public interface LoginApi {

	@ApiOperation(value = "", notes = "Updates login date for an user", response = Void.class, tags = {})
	@ApiResponses(value = { @ApiResponse(code = 204, message = "login date updated", response = Void.class),
			@ApiResponse(code = 401, message = "unauthorized", response = Void.class),
			@ApiResponse(code = 403, message = "forbidden", response = Void.class),
			@ApiResponse(code = 500, message = "unexpected error", response = Void.class) })
	@RequestMapping(value = "", produces = { "application/json" }, consumes = {
			"application/json" }, method = RequestMethod.POST)
	ResponseEntity<Void> login(
			@ApiParam(value = "username of user for login date update", required = true) @RequestBody String username,
		    @Context HttpServletRequest httpRequest);

}
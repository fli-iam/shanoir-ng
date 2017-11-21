package org.shanoir.ng.role;

import java.util.List;

import org.shanoir.ng.shared.exception.ErrorModel;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

@Api(value = "role", description = "the role API")
@RequestMapping("/roles")
public interface RoleApi {

	@ApiOperation(value = "", notes = "Returns all the roles", response = Role.class, responseContainer = "List", tags = {})
	@ApiResponses(value = { @ApiResponse(code = 200, message = "found roles", response = Role.class),
			@ApiResponse(code = 204, message = "no role found", response = Role.class),
			@ApiResponse(code = 401, message = "unauthorized", response = Role.class),
			@ApiResponse(code = 403, message = "forbidden", response = Role.class),
			@ApiResponse(code = 500, message = "unexpected error", response = ErrorModel.class) })
	@RequestMapping(value = "", produces = { "application/json" }, method = RequestMethod.GET)
	@PreAuthorize("hasRole('ADMIN')")
	ResponseEntity<List<Role>> findRoles();

}

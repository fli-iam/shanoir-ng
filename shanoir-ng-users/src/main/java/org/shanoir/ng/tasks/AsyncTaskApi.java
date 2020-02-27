package org.shanoir.ng.tasks;

import java.util.List;

import org.shanoir.ng.events.ShanoirEvent;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

/**
 * API to manage asynchronous tasks:
 * - Retrieve a list of tasks for a user
 * @author fli
 *
 */
@Api(value = "tasks")
@RequestMapping("/tasks")
public interface AsyncTaskApi {
	@ApiOperation(value = "", notes = "If exists, returns the tasks that the user is allowed to see", response = ShanoirEvent.class, tags = {})
	@ApiResponses(value = { @ApiResponse(code = 200, message = "found tasks", response = ShanoirEvent.class),
			@ApiResponse(code = 401, message = "unauthorized", response = Void.class),
			@ApiResponse(code = 403, message = "forbidden", response = Void.class),
			@ApiResponse(code = 404, message = "no task found", response = Void.class),
			@ApiResponse(code = 500, message = "unexpected error", response = Void.class) })
	@GetMapping(value = "", produces = { "application/json" })
	@PreAuthorize("hasAnyRole('ADMIN', 'EXPERT', 'USER')")
	ResponseEntity<List<ShanoirEvent>> findTasks();
}

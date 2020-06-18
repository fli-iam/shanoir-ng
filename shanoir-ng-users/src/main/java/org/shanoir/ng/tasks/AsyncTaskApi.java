package org.shanoir.ng.tasks;

import java.io.IOException;
import java.util.List;

import javax.validation.Valid;

import org.shanoir.ng.events.ShanoirEvent;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
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
@CrossOrigin(exposedHeaders = "Access-Control-Allow-Origin")
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

	@ApiOperation(value = "", notes = "If exists, returns the tasks that the user is allowed to see for the given tasks types", response = ShanoirEvent.class, tags = {})
	@ApiResponses(value = { @ApiResponse(code = 200, message = "found tasks", response = ShanoirEvent.class),
			@ApiResponse(code = 401, message = "unauthorized", response = Void.class),
			@ApiResponse(code = 403, message = "forbidden", response = Void.class),
			@ApiResponse(code = 404, message = "no task found", response = Void.class),
			@ApiResponse(code = 500, message = "unexpected error", response = Void.class) })
	@GetMapping(value = "/types", produces = { "application/json" })
	@PreAuthorize("hasAnyRole('ADMIN', 'EXPERT', 'USER')")
	ResponseEntity<List<ShanoirEvent>> findTasksByType(
    		@ApiParam(value = "types of events to retrieve", required=true) @Valid
    		@RequestParam(value = "types", required = true) String[] types);

	@ApiOperation(value = "", notes = "Pushes a new event emitter to front", response = SseEmitter.class, tags = {})
	@ApiResponses(value = { @ApiResponse(code = 200, message = "found tasks", response = SseEmitter.class),
			@ApiResponse(code = 401, message = "unauthorized", response = Void.class),
			@ApiResponse(code = 403, message = "forbidden", response = Void.class),
			@ApiResponse(code = 404, message = "no task found", response = Void.class),
			@ApiResponse(code = 500, message = "unexpected error", response = Void.class) })
	@GetMapping(value = "/updateTasks")
	ResponseEntity<SseEmitter> updateTasks() throws IOException;
}

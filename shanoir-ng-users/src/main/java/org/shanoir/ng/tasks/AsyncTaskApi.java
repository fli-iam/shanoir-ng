package org.shanoir.ng.tasks;

import java.io.IOException;
import java.util.List;

import org.shanoir.ng.events.ShanoirEvent;
import org.shanoir.ng.events.ShanoirEventLight;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;

/**
 * API to manage asynchronous tasks:
 * - Retrieve a list of tasks for a user
 * @author fli
 *
 */
@Tag(name = "tasks")
@RequestMapping("/tasks")
@CrossOrigin(exposedHeaders = "Access-Control-Allow-Origin")
public interface AsyncTaskApi {

	@Operation(summary = "", description = "If exists, returns the tasks that the user is allowed to see")
	@ApiResponses(value = { @ApiResponse(responseCode = "200", description = "found tasks"),
			@ApiResponse(responseCode = "401", description = "unauthorized"),
			@ApiResponse(responseCode = "403", description = "forbidden"),
			@ApiResponse(responseCode = "404", description = "no task found"),
			@ApiResponse(responseCode = "500", description = "unexpected error") })
	@GetMapping(value = "", produces = { "application/json" })
	@PreAuthorize("hasAnyRole('ADMIN', 'EXPERT', 'USER')")
	ResponseEntity<List<ShanoirEventLight>> findTasks();

	@Operation(summary = "", description = "If exists, returns the requested task")
	@ApiResponses(value = { @ApiResponse(responseCode = "200", description = "found tasks"),
			@ApiResponse(responseCode = "401", description = "unauthorized"),
			@ApiResponse(responseCode = "403", description = "forbidden"),
			@ApiResponse(responseCode = "404", description = "no task found"),
			@ApiResponse(responseCode = "500", description = "unexpected error") })
	@GetMapping(value = "/{taskId}", produces = { "application/json" })
	@PreAuthorize("hasAnyRole('ADMIN', 'EXPERT', 'USER')")
	ResponseEntity<ShanoirEvent> getTaskDetails(@Parameter(name = "id of the task", required = true) @PathVariable("taskId") Long taskId);

	@Operation(summary = "", description = "Pushes a new event emitter to front")
	@ApiResponses(value = { @ApiResponse(responseCode = "200", description = "found tasks"),
			@ApiResponse(responseCode = "401", description = "unauthorized"),
			@ApiResponse(responseCode = "403", description = "forbidden"),
			@ApiResponse(responseCode = "404", description = "no task found"),
			@ApiResponse(responseCode = "500", description = "unexpected error") })
	@GetMapping(value = "/updateTasks")
	ResponseEntity<SseEmitter> updateTasks() throws IOException;

}

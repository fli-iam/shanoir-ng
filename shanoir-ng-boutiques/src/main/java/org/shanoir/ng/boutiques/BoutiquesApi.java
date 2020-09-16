package org.shanoir.ng.boutiques;
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

import java.util.ArrayList;

import javax.validation.Valid;

import org.shanoir.ng.boutiques.model.BoutiquesTool;
import org.springframework.core.io.ByteArrayResource;

/**
 * https://github.com/swagger-api/swagger-codegen
 * Do not edit the class manually.
 */

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.fasterxml.jackson.databind.node.ObjectNode;

//import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

//@Api(value = "tools")
//@CrossOrigin(origins = "http://localhost:4200")
@CrossOrigin(origins = "https://shanoir-ng-nginx")
@RestController
public interface BoutiquesApi {


	@ApiOperation(value = "", notes = "Search for tools", response = Void.class, tags = {})
	@ApiResponses(value = { @ApiResponse(code = 200, message = "tool found", response = Void.class),
							@ApiResponse(code = 404, message = "no tool found", response = Void.class) })
	@PreAuthorize("hasRole('EXPERT') or hasRole('ADMIN')")
	@GetMapping("/tool/search")
    public ArrayList<BoutiquesTool> searchTool(@ApiParam(value = "query", defaultValue = "") @Valid @RequestParam(value = "query", required = false, defaultValue = "") String query) throws ResponseStatusException;

	@ApiOperation(value = "", notes = "Get all tools", response = Void.class, tags = {})
	@ApiResponses(value = { @ApiResponse(code = 200, message = "tools found", response = Void.class),
							@ApiResponse(code = 404, message = "no tool found", response = Void.class) })
	@PreAuthorize("hasRole('EXPERT') or hasRole('ADMIN')")
    @GetMapping("/tool/all")
    public ArrayList<BoutiquesTool> getAllTools() throws ResponseStatusException;

	@ApiOperation(value = "", notes = "Get a tool descriptor", response = Void.class, tags = {})
	@ApiResponses(value = { @ApiResponse(code = 200, message = "descriptor found", response = Void.class),
							@ApiResponse(code = 404, message = "no tool descriptor found", response = Void.class) })
	@PreAuthorize("hasRole('EXPERT') or hasRole('ADMIN')")
    @GetMapping("/tool/{id}/descriptor/")
    public ObjectNode getDescriptor(@ApiParam(value = "id of the tool", required = true) @PathVariable("id") String id) throws ResponseStatusException;


	@ApiOperation(value = "", notes = "Get a tool invocation example", response = Void.class, tags = {})
	@ApiResponses(value = { @ApiResponse(code = 200, message = "invocation generated", response = Void.class),
							@ApiResponse(code = 500, message = "error while generating example invocation", response = Void.class) })
    @PreAuthorize("hasRole('EXPERT') or hasRole('ADMIN')")
    @GetMapping("/tool/{id}/invocation")
    public String getInvocation(@ApiParam(value = "id of the tool", required = true) @PathVariable("id") String id, 
    								@RequestParam(value="complete", defaultValue="false") String completeString) throws ResponseStatusException;

	@ApiOperation(value = "", notes = "Generate the tool command", response = Void.class, tags = {})
	@ApiResponses(value = { @ApiResponse(code = 200, message = "command generated", response = Void.class),
							@ApiResponse(code = 500, message = "error while generating tool command", response = Void.class) })
    @PreAuthorize("hasRole('EXPERT') or hasRole('ADMIN')")
    @PostMapping("/tool/{id}/generate-command/")
    public String generateCommand(	@ApiParam(value = "invocation", required = true) @RequestBody ObjectNode invocation, 
    									@ApiParam(value = "id of the tool", required = true) @PathVariable("id") String id) throws ResponseStatusException;

	@ApiOperation(value = "", notes = "execute a tool", response = Void.class, tags = {})
	@ApiResponses(value = { @ApiResponse(code = 200, message = "tool execution started", response = Void.class),
							@ApiResponse(code = 417, message = "invalid output path", response = Void.class),
							@ApiResponse(code = 500, message = "error while executing tool", response = Void.class) })
    @PreAuthorize("hasRole('EXPERT') or hasRole('ADMIN')")
    @PostMapping("/tool/{toolId}/execute/{sessionId}")
    public String execute(	@ApiParam(value = "invocation", required = true) @RequestBody ObjectNode invocation, 
    							@ApiParam(value = "id of the tool", required = true) @PathVariable("toolId") String toolId, 
    							@ApiParam(value = "id of the session", required = true) @PathVariable("sessionId") String sessionId) throws ResponseStatusException;

	@ApiOperation(value = "", notes = "stop tool execution", response = Void.class, tags = {})
	@ApiResponses(value = { @ApiResponse(code = 200, message = "tool execution stopped", response = Void.class) })
    @PreAuthorize("hasRole('EXPERT') or hasRole('ADMIN')")
	@GetMapping("/tool/{toolId}/cancel-execution/{sessionId}")
    public String cancelExecution(@ApiParam(value = "id of the tool", required = true) @PathVariable("toolId") String toolId, 
    								@ApiParam(value = "id of the session", required = true) @PathVariable("sessionId") String sessionId) throws ResponseStatusException;

	@ApiOperation(value = "", notes = "get execution output", response = Void.class, tags = {})
	@ApiResponses(value = { @ApiResponse(code = 200, message = "output returned", response = Void.class) })
    @PreAuthorize("hasRole('EXPERT') or hasRole('ADMIN')")
    @GetMapping("/tool/{toolId}/output/{sessionId}")
    public ObjectNode getExecutionOutput(	@ApiParam(value = "id of the tool", required = true) @PathVariable("toolId") String toolId, 
												@ApiParam(value = "id of the session", required = true) @PathVariable("sessionId") String sessionId) throws ResponseStatusException;

	@ApiOperation(value = "", notes = "download output", response = Void.class, tags = {})
	@ApiResponses(value = { @ApiResponse(code = 200, message = "output returned", response = Void.class), 
							@ApiResponse(code = 500, message = "error while creating zip file", response = Void.class) })
    @PreAuthorize("hasRole('EXPERT') or hasRole('ADMIN')")
    @GetMapping("/tool/{toolId}/download-output/{sessionId}")
    public ResponseEntity<ByteArrayResource> downloadOutput(	
    		@ApiParam(value = "id of the tool", required = true) @PathVariable("toolId") String toolId, 
			@ApiParam(value = "id of the session", required = true) @PathVariable("sessionId") String sessionId) throws ResponseStatusException;

	@ApiOperation(value = "", notes = "update tool database", response = Void.class, tags = {})
	@ApiResponses(value = { @ApiResponse(code = 200, message = "tool database updated", response = Void.class), 
							@ApiResponse(code = 500, message = "error while updating database", response = Void.class) })
	@PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/tool/update-database/")
    public String updateDatabase() throws ResponseStatusException;
}

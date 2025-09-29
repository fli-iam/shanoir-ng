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

package org.shanoir.ng.importer.vip.controler;

import org.shanoir.ng.importer.vip.model.Path;
import org.shanoir.ng.shared.exception.RestServiceException;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.fasterxml.jackson.core.JsonProcessingException;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@Tag(name = "carmin-data", description = "carmin-data API")
@RequestMapping("/carmin-data")
public interface ExecutionResultApi {

	@Operation(summary = "Delete a path", description = "Delete a path and transitively delete all its content if it is a directory.")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "the deletion is successful and finished."),
			@ApiResponse(responseCode = "500", description = "A functional or internal error occured processing the request") })
	@RequestMapping(value = "/**", produces = { "application/json" }, method = RequestMethod.DELETE)
	@PreAuthorize("hasRole('ADMIN') or (hasAnyRole('EXPERT', 'USER'))")
	ResponseEntity<Void> deletePath();

	@Operation(summary = "Upload data to a path", description = "A request without content creates a directory (an error should be returned if the path already exists). "
			+ "A request with a specific content type (\"application/carmin+json\") allows to upload data encoded in base64. "
			+ "The base64 content (part of a json payload) can either be an encoded file, or an encoded zip archive that will create a directory. "
			+ "All other content (with any content type) will be considered as a raw file and will override the existing path content. "
			+ "If the parent directory of the file/directory to create does not exist, an error must be returned.")
	@ApiResponses(value = { @ApiResponse(responseCode = "200", description = "The upload is successful and finished."),
			@ApiResponse(responseCode = "500", description = "A functional or internal error occured processing the request") })
	@RequestMapping(value = "/**", produces = { "application/json" }, consumes = { "application/carmin+json",
			"application/octet-stream" }, method = RequestMethod.PUT)
	@PreAuthorize("hasRole('ADMIN') or (hasAnyRole('EXPERT', 'USER'))")
	ResponseEntity<Path> uploadPath(@Valid @RequestBody String body) throws RestServiceException, JsonProcessingException;

}
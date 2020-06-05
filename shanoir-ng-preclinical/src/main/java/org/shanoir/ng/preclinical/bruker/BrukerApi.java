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

package org.shanoir.ng.preclinical.bruker;

import org.shanoir.ng.shared.exception.RestServiceException;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

@Api(value = "bruker")
public interface BrukerApi {

	@ApiOperation(value = "Upload bruker zip archive file", notes = "", response = String.class, tags = {
			"BrukerModel", })
	@ApiResponses(value = { @ApiResponse(code = 200, message = "success returns ", response = String.class),
			@ApiResponse(code = 400, message = "Invalid input / Bad Request", response = Void.class),
			@ApiResponse(code = 406, message = "Not valid bruker file", response = Void.class),
			@ApiResponse(code = 500, message = "Unexpected Error", response = Void.class) })
	@PostMapping(value = "/bruker/upload", produces = { "application/json" }, consumes = {
			MediaType.MULTIPART_FORM_DATA_VALUE, "application/json" })
	ResponseEntity<String> uploadBrukerFile(@RequestParam("files") MultipartFile[] uploadfiles)
			throws RestServiceException;

}

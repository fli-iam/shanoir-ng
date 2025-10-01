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

import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;

@Tag(name = "bruker")
public interface BrukerApi {

    @Operation(summary = "Upload bruker zip archive file", description = "")
    @ApiResponses(value = { @ApiResponse(responseCode = "200", description = "success returns "),
            @ApiResponse(responseCode = "400", description = "Invalid input / Bad Request"),
            @ApiResponse(responseCode = "406", description = "Not valid bruker file"),
            @ApiResponse(responseCode = "500", description = "Unexpected Error") })
    @PostMapping(value = "/bruker/upload", produces = { "application/json" }, consumes = {
            MediaType.MULTIPART_FORM_DATA_VALUE, "application/json" })
    ResponseEntity<String> uploadBrukerFile(@RequestParam("files") MultipartFile[] uploadfiles)
            throws RestServiceException;

}

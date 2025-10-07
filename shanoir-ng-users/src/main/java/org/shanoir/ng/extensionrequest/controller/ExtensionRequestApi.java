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

package org.shanoir.ng.extensionrequest.controller;

import org.shanoir.ng.extensionrequest.model.ExtensionRequestInfo;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "extensionrequest", description = "the extensionrequest API")
@RequestMapping("/extensionrequest")
public interface ExtensionRequestApi {

    @Operation(summary = "", description = "Requests a date extension for current user")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "request ok"),
            @ApiResponse(responseCode = "400", description = "user not found"),
            @ApiResponse(responseCode = "406", description = "user enabled or already under extension"),
            @ApiResponse(responseCode = "401", description = "unauthorized"),
            @ApiResponse(responseCode = "403", description = "forbidden"),
            @ApiResponse(responseCode = "500", description = "unexpected error") })
    @PostMapping(value = "", produces = { "application/json" }, consumes = {
            "application/json" })
    ResponseEntity<Void> requestExtension(
            @Parameter(name = "request motivation", required = true) @RequestBody ExtensionRequestInfo requestInfo);

}

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

package org.shanoir.ng.key.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "keys", description = "the key value API")
@RequestMapping("/keys")
public interface KeyValueApi {

    @Operation(
            summary = "Get value by key",
            description = "Returns the value for the given key, if present"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Value found",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(type = "string", example = "hash")
            )
            ),
        @ApiResponse(responseCode = "204", description = "No value found for the key"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "403", description = "Forbidden"),
        @ApiResponse(responseCode = "500", description = "Unexpected error")
    })
    @GetMapping(value = "/{key}", produces = "application/json")
    @PreAuthorize("hasAnyRole('ADMIN','EXPERT','USER')")
    ResponseEntity<String> findValue(@PathVariable String key);

}

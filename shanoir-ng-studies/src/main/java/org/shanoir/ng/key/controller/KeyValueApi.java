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
                schema = @Schema(type = "string", example = "abc123hash")
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

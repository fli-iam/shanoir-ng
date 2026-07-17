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

package org.shanoir.ng.massemail.controller;

import org.shanoir.ng.massemail.model.MassEmailRequest;
import org.shanoir.ng.massemail.model.RecipientGroup;
import org.shanoir.ng.shared.exception.RestServiceException;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import jakarta.validation.Valid;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

/**
 * Api for administrator mass emails.
 *
 * @author afragkiadakis
 */
@Tag(name = "massemail")
@RequestMapping("/massemail")
public interface MassEmailApi {

    @Operation(summary = "count", description = "Count the users of the given mass email recipient group")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "number of recipients"),
            @ApiResponse(responseCode = "401", description = "unauthorized"),
            @ApiResponse(responseCode = "403", description = "forbidden"),
            @ApiResponse(responseCode = "500", description = "unexpected error") })
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping(value = "count", produces = { "application/json" })
    ResponseEntity<Integer> countRecipients(
            @Parameter(name = "recipient group to count", required = true) @RequestParam("group") RecipientGroup group)
            throws RestServiceException;

    @Operation(summary = "send", description = "Send an email to every user of the given recipient group")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "202", description = "sending started, the body is the number of recipients"),
            @ApiResponse(responseCode = "401", description = "unauthorized"),
            @ApiResponse(responseCode = "403", description = "forbidden"),
            @ApiResponse(responseCode = "422", description = "bad parameters"),
            @ApiResponse(responseCode = "500", description = "unexpected error") })
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping(value = "", produces = { "application/json" }, consumes = { "application/json" })
    ResponseEntity<Integer> sendMassEmail(
            @Parameter(name = "mass email to send", required = true) @RequestBody @Valid MassEmailRequest request,
            BindingResult result) throws RestServiceException;

}

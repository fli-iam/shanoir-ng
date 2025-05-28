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

package org.shanoir.ng.vip.output.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.shanoir.ng.shared.core.model.IdName;
import org.shanoir.ng.shared.exception.EntityNotFoundException;
import org.shanoir.ng.shared.exception.RestServiceException;
import org.shanoir.ng.shared.exception.SecurityException;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Post processing", description="API for post processing")
@RequestMapping("/vip/postProcessing")
public interface PostProcessingApi {

    @Operation(summary = "Launch every post processingq according to processing name and comment", description = "Launch every post processings according to processing name and comment.", tags={  })
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Post processing command successfully initiated"),
            @ApiResponse(responseCode = "403", description = "forbidden"),
            @ApiResponse(responseCode = "500", description = "unexpected error")})
    @GetMapping(value = { "/"}, produces = { "application/json" })
    @PreAuthorize("hasRole('ADMIN') or (hasAnyRole('EXPERT', 'USER'))")
    ResponseEntity<IdName> launchPostProcessing(
            @Parameter(description = "processing name", required = true) @RequestParam final String name,
            @Parameter(description = "processing comment", required = true) @RequestParam final String comment);
}

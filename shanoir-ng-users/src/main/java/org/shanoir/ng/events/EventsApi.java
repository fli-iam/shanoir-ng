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

package org.shanoir.ng.events;

import org.shanoir.ng.shared.exception.RestServiceException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;


@Tag(name = "events", description = "the events API")
@RequestMapping("/events")
public interface EventsApi {

    @Operation(summary = "", description = "If exists, returns the events corresponding to the given study id")
    @ApiResponses(value = { @ApiResponse(responseCode = "200", description = "found user"),
            @ApiResponse(responseCode = "401", description = "unauthorized"),
            @ApiResponse(responseCode = "403", description = "forbidden"),
            @ApiResponse(responseCode = "404", description = "no user found"),
            @ApiResponse(responseCode = "500", description = "unexpected error") })
    @RequestMapping(value = "/{studyId}", produces = { "application/json" }, method = RequestMethod.GET)
    @PreAuthorize("hasRole('ADMIN') or (hasAnyRole('USER', 'EXPERT') and @shanoirUsersManagement.hasRightOnStudy(#studyId, 'CAN_ADMINISTRATE'))")
    ResponseEntity<Page<ShanoirEvent>> findEventsByStudyId(Pageable pageable, @Parameter(name = "id of the study", required = true) @PathVariable("studyId") Long studyId,  String searchStr, String searchField) throws RestServiceException;

    @Operation(summary = "", description = "If exists, returns the total number of events happened in the last x days for statistic purpose")
    @ApiResponses(value = { @ApiResponse(responseCode = "200", description = "found number of events"),
            @ApiResponse(responseCode = "401", description = "unauthorized"),
            @ApiResponse(responseCode = "403", description = "forbidden"),
            @ApiResponse(responseCode = "404", description = "no user found"),
            @ApiResponse(responseCode = "500", description = "unexpected error") })
    @RequestMapping(value = "/count", produces = { "application/json" }, method = RequestMethod.GET)
    ResponseEntity<Long> countPassedEvents(@Parameter(name = "number of days", required = true) @RequestParam("days") Integer days) throws RestServiceException;
}

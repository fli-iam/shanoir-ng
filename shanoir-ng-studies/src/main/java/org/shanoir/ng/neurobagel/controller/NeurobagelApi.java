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

package org.shanoir.ng.neurobagel.controller;

import java.util.List;

import org.shanoir.ng.neurobagel.dto.NeurobagelStudyDTO;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

/**
 * Export endpoints for the Neurobagel node. Only the 'neurobagel-export' Keycloak client
 * (ROLE_NEUROBAGEL) may call them, see NeurobagelAccess and SecurityConfiguration.
 */
@Tag(name = "neurobagel")
@RequestMapping("/neurobagel")
public interface NeurobagelApi {

    @Operation(summary = "findExportedStudies", description = "Lists the studies exposed to the Neurobagel federation")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "exported studies, possibly none"),
            @ApiResponse(responseCode = "401", description = "unauthorized"),
            @ApiResponse(responseCode = "403", description = "forbidden") })
    @GetMapping(value = "/studies", produces = { "application/json" })
    @PreAuthorize("hasRole('NEUROBAGEL')")
    ResponseEntity<List<NeurobagelStudyDTO>> findExportedStudies();

    @Operation(summary = "findExportedStudy", description = "Returns a study exposed to the Neurobagel federation")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "the exported study"),
            @ApiResponse(responseCode = "401", description = "unauthorized"),
            @ApiResponse(responseCode = "403", description = "forbidden"),
            @ApiResponse(responseCode = "404", description = "no such study, or not exported") })
    @GetMapping(value = "/study/{studyId}", produces = { "application/json" })
    @PreAuthorize("hasRole('NEUROBAGEL')")
    ResponseEntity<NeurobagelStudyDTO> findExportedStudy(
            @Parameter(description = "id of the study", required = true) @PathVariable("studyId") Long studyId);

}

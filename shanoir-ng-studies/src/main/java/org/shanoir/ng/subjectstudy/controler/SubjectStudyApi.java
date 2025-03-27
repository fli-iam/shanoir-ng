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

/**
 *
 */
package org.shanoir.ng.subjectstudy.controler;

import org.shanoir.ng.shared.exception.RestServiceException;
import org.shanoir.ng.subjectstudy.model.SubjectStudy;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

/**
 * @author yyao
 *
 */

@Tag(name = "subjectStudy")
@RequestMapping("/subjectStudy")
public interface SubjectStudyApi {
    
    @Operation(summary = "", description = "Updates subject study")
    @ApiResponses(value = { @ApiResponse(responseCode = "204", description = "subject study updated"),
            @ApiResponse(responseCode = "401", description = "unauthorized"),
            @ApiResponse(responseCode = "403", description = "forbidden"),
            @ApiResponse(responseCode = "422", description = "bad parameters"),
            @ApiResponse(responseCode = "500", description = "unexpected error") })
    @PutMapping(value = "/{subjectStudyId}", produces = { "application/json" }, consumes = {
            "application/json" })
    @PreAuthorize("(hasRole('ADMIN') or (hasAnyRole('EXPERT', 'USER')"
            + "  and (@studySecurityService.hasRightOnStudy(#subjectStudy.getStudy(), 'CAN_IMPORT')"
            + " or @studySecurityService.hasRightOnStudy(#subjectStudy.getStudy(), 'CAN_ADMINISTRATE') )"
            + "  )) and @controlerSecurityService.idMatches(#subjectStudyId, #subjectStudy)")
    ResponseEntity<Void> updateSubjectStudy(
            @Parameter(description = "id of the subject study", required = true) @PathVariable("subjectStudyId") Long subjectStudyId,
            @Parameter(description = "subject study to update", required = true) @RequestBody SubjectStudy subjectStudy,
            final BindingResult result) throws RestServiceException;
}

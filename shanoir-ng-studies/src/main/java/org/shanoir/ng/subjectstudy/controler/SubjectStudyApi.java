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
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

/**
 * @author yyao
 *
 */

@Api(value = "subjectStudy")
@RequestMapping("/subjectStudy")
public interface SubjectStudyApi {
	
	@ApiOperation(value = "", notes = "Updates subject study", response = Void.class, tags = {})
	@ApiResponses(value = { @ApiResponse(code = 204, message = "subject study updated", response = Void.class),
			@ApiResponse(code = 401, message = "unauthorized", response = Void.class),
			@ApiResponse(code = 403, message = "forbidden", response = Void.class),
			@ApiResponse(code = 422, message = "bad parameters", response = Void.class),
			@ApiResponse(code = 500, message = "unexpected error", response = Void.class) })
	@PutMapping(value = "/{subjectStudyId}", produces = { "application/json" }, consumes = {
			"application/json" })
	@PreAuthorize("hasRole('ADMIN') or (hasAnyRole('EXPERT', 'USER') and (@studySecurityService.hasRightOnStudy(#subjectStudy.getStudy(), 'CAN_IMPORT') or @studySecurityService.hasRightOnStudy(#subjectStudy.getStudy(), 'CAN_ADMINISTRATE')) and @controlerSecurityService.idMatches(#subjectStudyId, #subjectStudy))")
	ResponseEntity<Void> updateSubjectStudy(
			@ApiParam(value = "id of the subject study", required = true) @PathVariable("subjectStudyId") Long subjectStudyId,
			@ApiParam(value = "subject study to update", required = true) @RequestBody SubjectStudy subjectStudy,
			final BindingResult result) throws RestServiceException;
}

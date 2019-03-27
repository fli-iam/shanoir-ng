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
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

/**
 * @author yyao
 *
 */

@Api(value = "subjectStudy", description = "the subject study API")
@RequestMapping("/subjectStudy")
public interface SubjectStudyApi {
	
	@ApiOperation(value = "", notes = "Updates subject study", response = Void.class, tags = {})
	@ApiResponses(value = { @ApiResponse(code = 204, message = "subject study updated", response = Void.class),
			@ApiResponse(code = 401, message = "unauthorized", response = Void.class),
			@ApiResponse(code = 403, message = "forbidden", response = Void.class),
			@ApiResponse(code = 422, message = "bad parameters", response = Void.class),
			@ApiResponse(code = 500, message = "unexpected error", response = Void.class) })
	@RequestMapping(value = "/{subjectStudyId}", produces = { "application/json" }, consumes = {
			"application/json" }, method = RequestMethod.PUT)
	@PreAuthorize("hasAnyRole('ADMIN', 'EXPERT') "
			+ "and (hasPermission(#subjectStudy.getStudy(), 'CAN_IMPORT') || hasPermission(#subjectStudy.getStudy(), 'CAN_ADMINISTRATE'))"
			+ "and @controlerSecurityService.idMatches(#subjectStudyId, #subjectStudy)")
	ResponseEntity<Void> updateSubjectStudy(
			@ApiParam(value = "id of the subject study", required = true) @PathVariable("subjectStudyId") Long subjectStudyId,
			@ApiParam(value = "subject study to update", required = true) @RequestBody SubjectStudy subjectStudy,
			final BindingResult result) throws RestServiceException;
}

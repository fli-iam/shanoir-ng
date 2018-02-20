/**
 * 
 */
package org.shanoir.ng.subjectstudy;

import org.shanoir.ng.shared.exception.RestServiceException;
import org.springframework.http.ResponseEntity;
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
	ResponseEntity<Void> updateSubjectStudy(
			@ApiParam(value = "id of the subject study", required = true) @PathVariable("subjectStudyId") Long subjectStudyId,
			@ApiParam(value = "subject study to update", required = true) @RequestBody SubjectStudy subjectStudy,
			final BindingResult result) throws RestServiceException;
	
	@ApiOperation(value = "", notes = "Saves a new subject study", response = SubjectStudy.class, tags = {})
	@ApiResponses(value = { @ApiResponse(code = 200, message = "created subject study", response = SubjectStudy.class),
			@ApiResponse(code = 401, message = "unauthorized", response = SubjectStudy.class),
			@ApiResponse(code = 403, message = "forbidden", response = SubjectStudy.class),
			@ApiResponse(code = 422, message = "bad parameters", response = SubjectStudy.class),
			@ApiResponse(code = 500, message = "unexpected error", response = SubjectStudy.class) })
	@RequestMapping(value = "", produces = { "application/json" }, consumes = {
			"application/json" }, method = RequestMethod.POST)
	ResponseEntity<SubjectStudy> saveNewSubjectStudy(
			@ApiParam(value = "subject study to create", required = true) @RequestBody SubjectStudy subjectStudy,
			final BindingResult result) throws RestServiceException;

}

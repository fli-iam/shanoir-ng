package org.shanoir.ng.tag.controler;

import java.util.List;

import org.shanoir.ng.shared.exception.RestServiceException;
import org.shanoir.ng.tag.model.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

@Api(value = "tag")
@RequestMapping("/tag")
public interface TagApi {

	@ApiOperation(value = "", notes = "Find tags for a given subject", response = Void.class, tags = {})
	@ApiResponses(value = { @ApiResponse(code = 200, message = "OK", response = Void.class),
			@ApiResponse(code = 401, message = "unauthorized", response = Void.class),
			@ApiResponse(code = 403, message = "forbidden", response = Void.class),
			@ApiResponse(code = 422, message = "bad parameters", response = Void.class),
			@ApiResponse(code = 500, message = "unexpected error", response = Void.class) })
	@GetMapping(value = "subject/{subjectId}", produces = { "application/json" })
	@PreAuthorize("hasRole('ADMIN') or @studySecurityService.hasRightOnSubjectForOneStudy(subjectId, 'CAN_SEE_ALL')")
	ResponseEntity<List<Tag>> findTags(
			@ApiParam(value = "id of the subject", required = true) @PathVariable("subjectId") Long subjectId) throws RestServiceException;

	@ApiOperation(value = "", notes = "Find tags for a given study", response = Void.class, tags = {})
	@ApiResponses(value = { @ApiResponse(code = 200, message = "OK", response = Void.class),
			@ApiResponse(code = 401, message = "unauthorized", response = Void.class),
			@ApiResponse(code = 403, message = "forbidden", response = Void.class),
			@ApiResponse(code = 422, message = "bad parameters", response = Void.class),
			@ApiResponse(code = 500, message = "unexpected error", response = Void.class) })
	@GetMapping(value = "study/{studyId}", produces = { "application/json" })
	@PreAuthorize("hasRole('ADMIN') or @studySecurityService.hasRightOnStudy(studyId, 'CAN_SEE_ALL')")
	ResponseEntity<List<Tag>> findTagsOfStudy(
			@ApiParam(value = "id of the study", required = true) @PathVariable("studyId") Long studyId) throws RestServiceException;

	@ApiOperation(value = "", notes = "Find tags for a given subject study", response = Void.class, tags = {})
	@ApiResponses(value = { @ApiResponse(code = 200, message = "OK", response = Void.class),
			@ApiResponse(code = 401, message = "unauthorized", response = Void.class),
			@ApiResponse(code = 403, message = "forbidden", response = Void.class),
			@ApiResponse(code = 422, message = "bad parameters", response = Void.class),
			@ApiResponse(code = 500, message = "unexpected error", response = Void.class) })
	@GetMapping(value = "subject_study/{subjectStudyId}", produces = { "application/json" })
	ResponseEntity<List<Tag>> findTagsOfSubjectStudy(
			@ApiParam(value = "id of the subject study", required = true) @PathVariable("subjectStudyId") Long subjectStudyId) throws RestServiceException;
}

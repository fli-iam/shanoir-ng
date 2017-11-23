package org.shanoir.ng.shared.api;

import org.shanoir.ng.study.dto.StudySubjectCenterNamesDTO;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

@Api(value = "studyMS", description = "the study MS shared API")
@RequestMapping("/studyMS")
public interface StudyServiceSharedApi {


	@ApiOperation(value = "", notes = "If exists, returns the study name, subject name, center name corresponding to the given ids", response = StudySubjectCenterNamesDTO.class, tags = {})
	@ApiResponses(value = { @ApiResponse(code = 200, message = "found elements", response = StudySubjectCenterNamesDTO.class),
			@ApiResponse(code = 401, message = "unauthorized", response = StudySubjectCenterNamesDTO.class),
			@ApiResponse(code = 403, message = "forbidden", response = StudySubjectCenterNamesDTO.class),
			@ApiResponse(code = 404, message = "no element found", response = StudySubjectCenterNamesDTO.class),
			@ApiResponse(code = 500, message = "unexpected error", response = StudySubjectCenterNamesDTO.class) })
	@RequestMapping(value = "/{studyId}/{subjectId}/{centerId}", produces = { "application/json" }, method = RequestMethod.GET)
	ResponseEntity<StudySubjectCenterNamesDTO> findStudySubjectCenterNamesByIds(
			@ApiParam(value = "id of the study", required = true) @PathVariable("studyId") Long studyId,
			@ApiParam(value = "id of the subject", required = true) @PathVariable("subjectId") Long subjectId,
			@ApiParam(value = "id of the center", required = true) @PathVariable("centerId") Long centerId)
	;

	

}

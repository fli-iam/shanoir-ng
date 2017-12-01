package org.shanoir.ng.shared.common;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

@RequestMapping("/common")
public interface CommonApi {

	@ApiOperation(value = "", notes = "If exists, returns the study name, subject name, center name corresponding to the given ids", response = CommonIdNamesDTO.class, tags = {})
	@ApiResponses(value = {
			@ApiResponse(code = 200, message = "found elements", response = CommonIdNamesDTO.class),
			@ApiResponse(code = 401, message = "unauthorized", response = CommonIdNamesDTO.class),
			@ApiResponse(code = 403, message = "forbidden", response = CommonIdNamesDTO.class),
			@ApiResponse(code = 404, message = "no element found", response = CommonIdNamesDTO.class),
			@ApiResponse(code = 500, message = "unexpected error", response = CommonIdNamesDTO.class) })
	@RequestMapping(value = "", produces = { "application/json" }, consumes = {
			"application/json" }, method = RequestMethod.POST)
	ResponseEntity<CommonIdNamesDTO> findStudySubjectCenterNamesByIds(
			@ApiParam(value = "study to update", required = true) @RequestBody CommonIdsDTO commonIdDTO);

}

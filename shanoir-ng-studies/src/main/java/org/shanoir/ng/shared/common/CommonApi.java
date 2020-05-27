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

package org.shanoir.ng.shared.common;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
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
	@PostMapping(value = "", produces = { "application/json" }, consumes = {
			"application/json" })
	ResponseEntity<CommonIdNamesDTO> findStudySubjectCenterNamesByIds(
			@ApiParam(value = "study to update", required = true) @RequestBody CommonIdsDTO commonIdDTO);

}

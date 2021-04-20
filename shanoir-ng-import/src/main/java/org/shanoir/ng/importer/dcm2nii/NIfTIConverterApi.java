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
package org.shanoir.ng.importer.dcm2nii;

import java.util.List;

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

/**
 * @author yyao
 *
 */
@Api(value = "niftiConverters")
@RequestMapping("/niftiConverters")
public interface NIfTIConverterApi {
	@ApiOperation(value = "", notes = "If exists, returns the niftiConverter corresponding to the given id", response = NIfTIConverter.class, tags = {})
	@ApiResponses(value = { @ApiResponse(code = 200, message = "found nifti converter", response = NIfTIConverter.class),
			@ApiResponse(code = 401, message = "unauthorized", response = NIfTIConverter.class),
			@ApiResponse(code = 403, message = "forbidden", response = NIfTIConverter.class),
			@ApiResponse(code = 404, message = "no nifti converter found", response = NIfTIConverter.class),
			@ApiResponse(code = 500, message = "unexpected error", response = NIfTIConverter.class) })
	@GetMapping(value = "/{niftiConverterId}", produces = { "application/json" })
	@PreAuthorize("hasAnyRole('ADMIN', 'EXPERT', 'USER')")
	ResponseEntity<NIfTIConverter> findNiftiConverterById(
			@ApiParam(value = "id of the niftiConverter", required = true) @PathVariable("niftiConverterId") Long niftiConverterId);
	
	@ApiOperation(value = "", notes = "If exists, returns all the niftiConverters", response = NIfTIConverter.class, tags = {})
	@ApiResponses(value = { @ApiResponse(code = 200, message = "found nifti converters", response = NIfTIConverter.class),
			@ApiResponse(code = 401, message = "unauthorized", response = NIfTIConverter.class),
			@ApiResponse(code = 403, message = "forbidden", response = NIfTIConverter.class),
			@ApiResponse(code = 404, message = "no nifti converter found", response = NIfTIConverter.class),
			@ApiResponse(code = 500, message = "unexpected error", response = NIfTIConverter.class) })
	@GetMapping(value = "", produces = { "application/json" })
	@PreAuthorize("hasAnyRole('ADMIN', 'EXPERT', 'USER')")
	ResponseEntity<List<NIfTIConverter>> findNiftiConverters();

}

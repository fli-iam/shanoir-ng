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

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

/**
 * @author yyao
 *
 */
@Tag(name = "niftiConverters")
@RequestMapping("/niftiConverters")
public interface NIfTIConverterApi {
	@Operation(summary = "", description = "If exists, returns the niftiConverter corresponding to the given id")
	@ApiResponses(value = { @ApiResponse(responseCode = "200", description = "found nifti converter"),
			@ApiResponse(responseCode = "401", description = "unauthorized"),
			@ApiResponse(responseCode = "403", description = "forbidden"),
			@ApiResponse(responseCode = "404", description = "no nifti converter found"),
			@ApiResponse(responseCode = "500", description = "unexpected error") })
	@GetMapping(value = "/{niftiConverterId}", produces = { "application/json" })
	@PreAuthorize("hasAnyRole('ADMIN', 'EXPERT', 'USER')")
	ResponseEntity<NIfTIConverter> findNiftiConverterById(
			@Parameter(name = "id of the niftiConverter", required = true) @PathVariable("niftiConverterId") Long niftiConverterId);
	
	@Operation(summary = "", description = "If exists, returns all the niftiConverters")
	@ApiResponses(value = { @ApiResponse(responseCode = "200", description = "found nifti converters"),
			@ApiResponse(responseCode = "401", description = "unauthorized"),
			@ApiResponse(responseCode = "403", description = "forbidden"),
			@ApiResponse(responseCode = "404", description = "no nifti converter found"),
			@ApiResponse(responseCode = "500", description = "unexpected error") })
	@GetMapping(value = "", produces = { "application/json" })
	@PreAuthorize("hasAnyRole('ADMIN', 'EXPERT', 'USER')")
	ResponseEntity<List<NIfTIConverter>> findNiftiConverters();

}

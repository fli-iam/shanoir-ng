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

package org.shanoir.ng.dicom.web;

import org.shanoir.ng.dicom.web.dto.StudiesDTO;
import org.shanoir.ng.dicom.web.dto.StudyDTO;
import org.shanoir.ng.shared.exception.ErrorModel;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PostAuthorize;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

/**
 * This API interface returns a DICOMJson as described in
 * https://v3-docs.ohif.org/configuration/datasources/dicom-json/
 * for version 3 of the OHIF-Viewer.
 * 
 * The below implementation is only a draft and NOT FINISHED!!!
 * It remains as an interface for a potential future integration
 * with e.g. OHIF-viewer version 3 and as this format is considered,
 * as close to the DICOMDir, as a much better format than the regular
 * DICOM Json used by DICOMWeb - in terms of human readability especially.
 * 
 * @author mkain
 *
 */
@Api(value = "dicomjson")
@RequestMapping("/dicomjson")
public interface DICOMJsonApi {

	@ApiOperation(value = "", notes = "Returns all studies", response = StudyDTO.class, responseContainer = "List", tags = {})
	@ApiResponses(value = {
			@ApiResponse(code = 200, message = "found studies", response = StudyDTO.class, responseContainer = "List"),
			@ApiResponse(code = 204, message = "no examination found", response = Void.class),
			@ApiResponse(code = 401, message = "unauthorized", response = Void.class),
			@ApiResponse(code = 403, message = "forbidden", response = Void.class),
			@ApiResponse(code = 500, message = "unexpected error", response = ErrorModel.class) })
	@GetMapping(value = "/studies", produces = { "application/dicom+json" })
	@PreAuthorize("hasAnyRole('ADMIN', 'EXPERT', 'USER')")
	@PostAuthorize("hasRole('ADMIN') or @datasetSecurityService.filterExaminationDTOPage(returnObject.getBody(), 'CAN_SEE_ALL')")
	ResponseEntity<StudiesDTO> findStudies();

}

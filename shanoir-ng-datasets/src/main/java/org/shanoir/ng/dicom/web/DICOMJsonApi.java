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

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.shanoir.ng.dicom.web.dto.StudiesDTO;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PostAuthorize;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

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
@Tag(name = "dicomjson")
@RequestMapping("/dicomjson")
public interface DICOMJsonApi {

	@Operation(summary = "", description = "Returns all studies")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "found studies"),
			@ApiResponse(responseCode = "204", description = "no examination found"),
			@ApiResponse(responseCode = "401", description = "unauthorized"),
			@ApiResponse(responseCode = "403", description = "forbidden"),
			@ApiResponse(responseCode = "500", description = "unexpected error") })
	@GetMapping(value = "/studies", produces = { "application/dicom+json" })
	@PreAuthorize("hasAnyRole('ADMIN', 'EXPERT', 'USER')")
	@PostAuthorize("hasRole('ADMIN') or @datasetSecurityService.filterExaminationDTOPage(returnObject.getBody(), 'CAN_SEE_ALL')")
	ResponseEntity<StudiesDTO> findStudies();

}

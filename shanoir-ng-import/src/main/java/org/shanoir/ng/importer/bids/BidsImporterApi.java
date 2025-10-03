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
package org.shanoir.ng.importer.bids;

import java.io.IOException;

import org.shanoir.ng.importer.model.ImportJob;
import org.shanoir.ng.shared.exception.RestServiceException;
import org.shanoir.ng.shared.exception.ShanoirException;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.multipart.MultipartFile;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

/**
 *
 * @author fli
 *
 */
@Tag(name = "bidsImporter", description = "BIDS Importer API")
@RequestMapping("/bidsImporter")
public interface BidsImporterApi {

    @Operation(summary = "Import datasets from a BIDS folder", description = "Import from bids")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "import from bids"),
        @ApiResponse(responseCode = "400", description = "Invalid input / Bad Request"),
        @ApiResponse(responseCode = "500", description = "unexpected error")})
    @PostMapping(value = "/{studyId}/{studyName}/{centerId}",
            produces = {"application/json"},
            consumes = {"multipart/form-data"})
    @PreAuthorize("hasAnyRole('ADMIN', 'EXPERT', 'USER')")
    ResponseEntity<ImportJob> importAsBids(@Parameter(name = "file detail") @RequestPart("file") MultipartFile bidsZipFile,
            @Parameter(name = "id of the study", required = true) @PathVariable("studyId") Long studyId,
            @Parameter(name = "name of the study", required = true) @PathVariable("studyName") String studyName,
            @Parameter(name = "id of the center", required = true) @PathVariable("centerId") Long centerId) throws RestServiceException, ShanoirException, IOException;

}

/**
< * Shanoir NG - Import, manage and share neuroimaging data
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
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.multipart.MultipartFile;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

/**
 * 
 * @author fli
 *
 */
@Api(value = "bidsImporter", description = "BIDS Importer API")
@RequestMapping("/bidsImporter")
public interface BidsImporterApi {

    @ApiOperation(value = "Import datasets from a BIDS folder", notes = "Import from bids", response = ImportJob.class, tags={ "BIDS", "Import" })
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "import from bids", response = Void.class),
        @ApiResponse(code = 400, message = "Invalid input / Bad Request", response = Void.class),
        @ApiResponse(code = 500, message = "unexpected error", response = Error.class) })
    @PostMapping(value = "",
        produces = { "application/json" },
        consumes = { "multipart/form-data" })
    @PreAuthorize("hasAnyRole('ADMIN', 'EXPERT', 'USER')")
    ResponseEntity<ImportJob> importAsBids(@ApiParam(value = "file detail") @RequestPart("file") MultipartFile bidsZipFile) throws RestServiceException, ShanoirException, IOException;

}

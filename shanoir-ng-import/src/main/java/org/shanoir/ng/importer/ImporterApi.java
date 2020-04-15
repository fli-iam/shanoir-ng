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

package org.shanoir.ng.importer;

import java.io.IOException;

import org.shanoir.ng.importer.dicom.query.DicomQuery;
import org.shanoir.ng.importer.model.ImportJob;
import org.shanoir.ng.shared.exception.RestServiceException;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.multipart.MultipartFile;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

@Api(value = "importer", description = "Importer API")
@RequestMapping("/importer")
public interface ImporterApi {

    @ApiOperation(value = "Create a temp directory (random long), as sub-dir of a user specific dir, for one import and return the name == tempDirId",
    		notes = "Create a temp directory (random long), as sub-dir of a user specific dir, for one import and return the name == tempDirId", response = String.class)
	@ApiResponses(value = {
			@ApiResponse(code = 200, message = "temp dir created", response = String.class),
			@ApiResponse(code = 401, message = "unauthorized", response = String.class),
			@ApiResponse(code = 403, message = "forbidden", response = String.class),
			@ApiResponse(code = 500, message = "unexpected error", response = String.class) })
	@RequestMapping(value = "", produces = { "application/json" }, method = RequestMethod.GET)
    @PreAuthorize("hasRole('ADMIN') or (hasAnyRole('EXPERT', 'USER') and @importSecurityService.hasRightOnOneStudy('CAN_IMPORT'))")
    ResponseEntity<String> createTempDir() throws RestServiceException;
    
    @ApiOperation(value = "Upload a file into a specific temp dir", notes = "Upload a file into a specific temp dir", response = Void.class)
	@ApiResponses(value = {
			@ApiResponse(code = 200, message = "file uploaded", response = Void.class),
			@ApiResponse(code = 401, message = "unauthorized", response = Void.class),
			@ApiResponse(code = 403, message = "forbidden", response = Void.class),
			@ApiResponse(code = 500, message = "unexpected error", response = Void.class) })    
	@RequestMapping(value = "{tempDirId}", consumes = { "multipart/form-data" }, method = RequestMethod.POST)
    @PreAuthorize("hasRole('ADMIN') or (hasAnyRole('EXPERT', 'USER') and @importSecurityService.hasRightOnOneStudy('CAN_IMPORT'))")
    ResponseEntity<Void> uploadFile(
    		@ApiParam(value = "tempDirId", required = true) @PathVariable("tempDirId") String tempDirId,
    		@ApiParam(value = "file") @RequestPart("file") MultipartFile file) throws RestServiceException, IOException;
	
    @ApiOperation(value = "Upload one DICOM .zip file", notes = "Upload DICOM .zip file", response = Void.class, tags={ "Upload one DICOM .zip file", })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "success returns file path", response = Void.class),
        @ApiResponse(code = 400, message = "Invalid input / Bad Request", response = Void.class),
        @ApiResponse(code = 409, message = "Already exists - conflict", response = Void.class),
        @ApiResponse(code = 200, message = "Unexpected Error", response = Error.class) })
    @RequestMapping(value = "/upload_dicom/",
        produces = { "application/json" }, 
        consumes = { "multipart/form-data" },
        method = RequestMethod.POST)
    @PreAuthorize("hasRole('ADMIN') or (hasAnyRole('EXPERT', 'USER') and @importSecurityService.hasRightOnOneStudy('CAN_IMPORT'))")
    ResponseEntity<ImportJob> uploadDicomZipFile(@ApiParam(value = "file detail") @RequestPart("file") MultipartFile dicomZipFile) throws RestServiceException;
    
    @ApiOperation(value = "Start import job", notes = "Start import job", response = Void.class, tags={ "Start import job", })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "import job started", response = Void.class),
        @ApiResponse(code = 400, message = "Invalid input / Bad Request", response = Void.class),
        @ApiResponse(code = 500, message = "unexpected error", response = Error.class) })
    @RequestMapping(value = "/start_import_job/",
        produces = { "application/json" }, 
        consumes = { "application/json" },
        method = RequestMethod.POST)
    @PreAuthorize("hasRole('ADMIN') or (hasAnyRole('EXPERT', 'USER') and @importSecurityService.hasRightOnStudy(#importJob.getFrontStudyId(), 'CAN_IMPORT'))")
    ResponseEntity<Void> startImportJob(@ApiParam(value = "ImportJob", required=true) @RequestBody ImportJob importJob) throws RestServiceException;
    
    @ApiOperation(value = "ImportFromPACS: Query PACS", notes = "ImportFromPACS: Query PACS", response = Void.class, tags={ "ImportFromPACS: Query PACS", })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "query the PACS started", response = Void.class),
        @ApiResponse(code = 400, message = "Invalid input / Bad Request", response = Void.class),
        @ApiResponse(code = 500, message = "unexpected error", response = Error.class) })
    @RequestMapping(value = "/query_pacs/",
        produces = { "application/json" }, 
        consumes = { "application/json" },
        method = RequestMethod.POST)
    @PreAuthorize("hasRole('ADMIN') or (hasAnyRole('EXPERT', 'USER') and @importSecurityService.hasRightOnOneStudy('CAN_IMPORT') and @importSecurityService.canImportFromPACS())")
    ResponseEntity<ImportJob> queryPACS(@ApiParam(value = "DicomQuery", required=true) @RequestBody DicomQuery dicomQuery) throws RestServiceException;

}

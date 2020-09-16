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

import javax.websocket.Decoder.BinaryStream;

import java.io.FileNotFoundException;

import org.shanoir.ng.exchange.model.Exchange;
import org.shanoir.ng.importer.dicom.query.DicomQuery;
import org.shanoir.ng.importer.model.EegImportJob;
import org.shanoir.ng.importer.model.ImportJob;
import org.shanoir.ng.shared.exception.RestServiceException;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
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
			@ApiResponse(code = 500, message = "unexpected error", response = Error.class) })
	@GetMapping(value = "", produces = { "application/json" })
    @PreAuthorize("hasRole('ADMIN') or (hasAnyRole('EXPERT', 'USER') and @importSecurityService.hasRightOnOneStudy('CAN_IMPORT'))")
    ResponseEntity<String> createTempDir() throws RestServiceException;
    
    @ApiOperation(value = "Upload a file into a specific temp dir", notes = "Upload a file into a specific temp dir", response = Void.class)
	@ApiResponses(value = {
			@ApiResponse(code = 200, message = "file uploaded", response = Void.class),
			@ApiResponse(code = 401, message = "unauthorized", response = Void.class),
			@ApiResponse(code = 403, message = "forbidden", response = Void.class),
			@ApiResponse(code = 500, message = "unexpected error", response = Error.class) })
    @PostMapping(value = "{tempDirId}", consumes = { "multipart/form-data" })
    @PreAuthorize("hasRole('ADMIN') or (hasAnyRole('EXPERT', 'USER') and @importSecurityService.hasRightOnOneStudy('CAN_IMPORT'))")
    ResponseEntity<Void> uploadFile(
    		@ApiParam(value = "tempDirId", required = true) @PathVariable("tempDirId") String tempDirId,
    		@ApiParam(value = "file") @RequestParam("file") MultipartFile file) throws RestServiceException, IOException;
    
    @ApiOperation(value = "Start exchange", notes = "Start exchange", response = Void.class)
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "exchange started", response = Void.class),
        @ApiResponse(code = 400, message = "Invalid input / Bad Request", response = Void.class),
        @ApiResponse(code = 500, message = "unexpected error", response = Error.class) })
    @PostMapping(value = "/start_import/", consumes = { "application/json" })

    @PreAuthorize("hasRole('ADMIN') or (hasAnyRole('EXPERT', 'USER') and @importSecurityService.hasRightOnOneStudy('CAN_IMPORT'))")
    ResponseEntity<Void> startImport(@ApiParam(value = "Exchange", required=true) @RequestBody Exchange exchange) throws RestServiceException, FileNotFoundException, IOException;
    
    @ApiOperation(value = "Upload one DICOM .zip file", notes = "Upload DICOM .zip file", response = Void.class, tags={ "Upload one DICOM .zip file", })
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "success returns file path", response = Void.class),
        @ApiResponse(code = 400, message = "Invalid input / Bad Request", response = Void.class),
        @ApiResponse(code = 409, message = "Already exists - conflict", response = Void.class),
        @ApiResponse(code = 200, message = "Unexpected Error", response = Error.class) })
    @PostMapping(value = "/upload_dicom/",
        produces = { "application/json" },
        consumes = { "multipart/form-data" })
    @PreAuthorize("hasRole('ADMIN') or (hasAnyRole('EXPERT', 'USER') and @importSecurityService.hasRightOnOneStudy('CAN_IMPORT'))")
    ResponseEntity<ImportJob> uploadDicomZipFile(@ApiParam(value = "file detail") @RequestPart("file") MultipartFile dicomZipFile) throws RestServiceException;
    
    @ApiOperation(value = "Upload one EEG file", notes = "Upload channel and metadata from EEG file", response = Void.class, tags = {"Import one EEG file", })
    @ApiResponses(value = {
    	@ApiResponse(code = 200, message = "success returns file path", response = Void.class),
		@ApiResponse(code = 400, message = "Invalid input / Bad Request", response = Void.class),
		@ApiResponse(code = 409, message = "Already exists - conflict", response = Void.class),
		@ApiResponse(code = 200, message = "Unexpected Error", response = Error.class) })
    @PostMapping(value = "/upload_eeg/",
	    produces = { "application/json" },
	    consumes = { "multipart/form-data" })
    @PreAuthorize("hasRole('ADMIN') or (hasAnyRole('EXPERT', 'USER') and @importSecurityService.hasRightOnOneStudy('CAN_IMPORT'))")
    ResponseEntity<EegImportJob> uploadEEGZipFile(@ApiParam(value = "file detail") @RequestPart("file") MultipartFile eegZipFile) throws RestServiceException;

    @ApiOperation(value = "Import one DICOM .zip file", notes = "Import DICOM .zip file already uploaded", response = Void.class, tags = {
			"Import one DICOM .zip file", })
	@ApiResponses(value = { @ApiResponse(code = 200, message = "success returns file path", response = Void.class),
			@ApiResponse(code = 400, message = "Invalid input / Bad Request", response = Void.class),
			@ApiResponse(code = 409, message = "Already exists - conflict", response = Void.class),
			@ApiResponse(code = 200, message = "Unexpected Error", response = Error.class) })
	@PostMapping(value = "/import_dicom/", produces = { "application/json" }, consumes = {
			"application/json" })
	ResponseEntity<ImportJob> importDicomZipFile(@ApiParam(value = "file path") @RequestBody String dicomZipFilename)
			throws RestServiceException;
    
    @ApiOperation(value = "Start import job", notes = "Start import job", response = Void.class, tags={ "Start import job", })
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "import job started", response = Void.class),
        @ApiResponse(code = 400, message = "Invalid input / Bad Request", response = Void.class),
        @ApiResponse(code = 500, message = "unexpected error", response = Error.class) })
    @PostMapping(value = "/start_import_job/",
        produces = { "application/json" },
        consumes = { "application/json" })
    @PreAuthorize("hasRole('ADMIN') or (hasAnyRole('EXPERT', 'USER') and @importSecurityService.hasRightOnStudy(#importJob.getStudyId(), 'CAN_IMPORT'))")
    ResponseEntity<Void> startImportJob(@ApiParam(value = "ImportJob", required=true) @RequestBody ImportJob importJob) throws RestServiceException;

    @ApiOperation(value = "Start import EEG job", notes = "Start import eeg job", response = Void.class, tags={ "Start import eeg job", })
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "import eeg job started", response = Void.class),
        @ApiResponse(code = 400, message = "Invalid input / Bad Request", response = Void.class),
        @ApiResponse(code = 500, message = "unexpected error", response = Error.class) })
    @PostMapping(value = "/start_import_eeg_job/",
        produces = { "application/json" },
        consumes = { "application/json" })
    @PreAuthorize("hasRole('ADMIN') or (hasAnyRole('EXPERT', 'USER') and @importSecurityService.hasRightOnStudy(#importJob.getStudyId(), 'CAN_IMPORT'))")
    ResponseEntity<Void> startImportEEGJob(@ApiParam(value = "EegImportJob", required=true) @RequestBody EegImportJob importJob) throws RestServiceException;

    @ApiOperation(value = "ImportFromPACS: Query PACS", notes = "ImportFromPACS: Query PACS", response = Void.class, tags={ "ImportFromPACS: Query PACS", })
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "query the PACS started", response = Void.class),
        @ApiResponse(code = 400, message = "Invalid input / Bad Request", response = Void.class),
        @ApiResponse(code = 500, message = "unexpected error", response = Error.class) })
    @PostMapping(value = "/query_pacs/",
        produces = { "application/json" },
        consumes = { "application/json" })
    @PreAuthorize("hasRole('ADMIN') or (hasAnyRole('EXPERT', 'USER') and @importSecurityService.hasRightOnOneStudy('CAN_IMPORT') and @importSecurityService.canImportFromPACS())")
    ResponseEntity<ImportJob> queryPACS(@ApiParam(value = "DicomQuery", required=true) @RequestBody DicomQuery dicomQuery) throws RestServiceException;

    
    @ApiOperation(value = "Get dicom image", notes = "Get dicom image", response = Void.class, tags={ "", })
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "get dicom image", response = Void.class),
        @ApiResponse(code = 400, message = "Invalid input / Bad Request", response = Void.class),
        @ApiResponse(code = 500, message = "unexpected error", response = Error.class) })
    @PostMapping(value = "/importAsBids/",
        produces = { "application/json" },
        consumes = { "multipart/form-data" })
    @PreAuthorize("hasAnyRole('ADMIN', 'EXPERT', 'USER')")
    ResponseEntity<ImportJob> importAsBids(@ApiParam(value = "file detail") @RequestPart("file") MultipartFile bidsZipFile) throws RestServiceException, ShanoirException, IOException;

    @ApiOperation(value = "Get dicom image", notes = "Get dicom image", response = Void.class, tags={ "", })
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "get dicom image", response = Void.class),
        @ApiResponse(code = 400, message = "Invalid input / Bad Request", response = Void.class),
        @ApiResponse(code = 500, message = "unexpected error", response = Error.class) })
    @GetMapping(value = "/get_dicom/", produces = { "application/dicom" })
        @PreAuthorize("hasRole('ADMIN') or (hasAnyRole('EXPERT', 'USER') and @importSecurityService.hasRightOnOneStudy('CAN_IMPORT'))")
    ResponseEntity<ByteArrayResource> getDicomImage(@ApiParam(value = "path", required=true) @RequestParam(value = "path", required = true) String path) throws RestServiceException, IOException;

}

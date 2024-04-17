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

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "importer", description = "Importer API")
@RequestMapping("/importer")
public interface ImporterApi {

    // used by ShanoirUploader!!! 1. step: create a tempDir for the import
    @Operation(summary = "Create a temp directory (random long), as sub-dir of a user specific dir, for one import and return the name == tempDirId",
    		description = "Create a temp directory (random long), as sub-dir of a user specific dir, for one import and return the name == tempDirId")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "temp dir created"),
			@ApiResponse(responseCode = "401", description = "unauthorized"),
			@ApiResponse(responseCode = "403", description = "forbidden"),
			@ApiResponse(responseCode = "500", description = "unexpected error") })
	@GetMapping(value = {"", "/"}, produces = { "application/json" })
    @PreAuthorize("hasRole('ADMIN') or (hasAnyRole('EXPERT', 'USER') and @importSecurityService.hasRightOnOneStudy('CAN_IMPORT'))")
    ResponseEntity<String> createTempDir() throws RestServiceException;
    
    // used by ShanoirUploader!!! 2. step: called for each DICOM file
    @Operation(summary = "Upload a file into a specific temp dir", description = "Upload a file into a specific temp dir")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "file uploaded"),
			@ApiResponse(responseCode = "401", description = "unauthorized"),
			@ApiResponse(responseCode = "403", description = "forbidden"),
			@ApiResponse(responseCode = "500", description = "unexpected error") })
    @PostMapping(value = "/{tempDirId}", consumes = { "multipart/form-data" })
    @PreAuthorize("hasRole('ADMIN') or (hasAnyRole('EXPERT', 'USER') and @importSecurityService.hasRightOnOneStudy('CAN_IMPORT'))")
    ResponseEntity<Void> uploadFile(
    		@Parameter(name = "tempDirId", required = true) @PathVariable("tempDirId") String tempDirId,
    		@Parameter(name = "file") @RequestParam("file") MultipartFile file) throws RestServiceException, IOException;
    
    @Operation(summary = "Upload one DICOM .zip file", description = "Upload DICOM .zip file")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "success returns file path"),
        @ApiResponse(responseCode = "400", description = "Invalid input / Bad Request"),
        @ApiResponse(responseCode = "409", description = "Already exists - conflict"),
        @ApiResponse(responseCode = "200", description = "Unexpected Error") })
    @PostMapping(value = "/upload_dicom/",
        produces = { "application/json" },
        consumes = { "multipart/form-data" })
    @PreAuthorize("hasRole('ADMIN') or (hasAnyRole('EXPERT', 'USER') and @importSecurityService.hasRightOnOneStudy('CAN_IMPORT'))")
    ResponseEntity<ImportJob> uploadDicomZipFile(@Parameter(name = "file detail") @RequestPart("file") MultipartFile dicomZipFile) throws RestServiceException;
    
    @Operation(summary = "Upload multiple examinations DICOM .zip file", description = "Upload DICOM .zip file")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "success returns file path"),
        @ApiResponse(responseCode = "400", description = "Invalid input / Bad Request"),
        @ApiResponse(responseCode = "409", description = "Already exists - conflict"),
        @ApiResponse(responseCode = "200", description = "Unexpected Error") })
    @PostMapping(value = "/upload_multiple_dicom/study/{studyId}/studyName/{studyName}/studyCard/{studyCardId}/center/{centerId}/equipment/{equipmentId}/",
        produces = { "application/json" },
        consumes = { "multipart/form-data" })
    @PreAuthorize("hasRole('ADMIN') or (hasAnyRole('EXPERT', 'USER') and @importSecurityService.hasRightOnOneStudy('CAN_IMPORT'))")
    ResponseEntity<ImportJob> uploadMultipleDicom(@Parameter(name = "file detail") @RequestPart("file") MultipartFile dicomZipFile,
    		@Parameter(name = "studyId", required = true) @PathVariable("studyId") Long studyId,
    		@Parameter(name = "studyName", required = true) @PathVariable("studyName") String studyName,
    		@Parameter(name = "studyCardId", required = true) @PathVariable("studyCardId") Long studyCardId,
    		@Parameter(name = "centerId", required = true) @PathVariable("centerId") Long centerId,
    		@Parameter(name = "equipmentId", required = true) @PathVariable("equipmentId") Long equipmentId) throws RestServiceException;

    @Operation(summary = "Upload one EEG file", description = "Upload channel and metadata from EEG file")
    @ApiResponses(value = {
    	@ApiResponse(responseCode = "200", description = "success returns file path"),
		@ApiResponse(responseCode = "400", description = "Invalid input / Bad Request"),
		@ApiResponse(responseCode = "409", description = "Already exists - conflict"),
		@ApiResponse(responseCode = "200", description = "Unexpected Error") })
    @PostMapping(value = "/upload_eeg/",
	    consumes = { "multipart/form-data" })
    @PreAuthorize("hasRole('ADMIN') or (hasAnyRole('EXPERT', 'USER') and @importSecurityService.hasRightOnOneStudy('CAN_IMPORT'))")
    ResponseEntity<EegImportJob> uploadEEGZipFile(@Parameter(name = "file detail") @RequestPart("file") MultipartFile eegZipFile) throws RestServiceException;

    @Operation(summary = "Upload a NIfTI file or an Analyze data item", description = "Upload .nii, .nii.gz or .hdr/.img files")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "success returns file path"),
        @ApiResponse(responseCode = "400", description = "Invalid input / Bad Request"),
        @ApiResponse(responseCode = "409", description = "Already exists - conflict"),
        @ApiResponse(responseCode = "200", description = "Unexpected Error") })
    @PostMapping(value = "/upload_processed_dataset/",
        consumes = { "multipart/form-data" })
    @PreAuthorize("hasRole('ADMIN') or (hasAnyRole('EXPERT', 'USER') and @importSecurityService.hasRightOnOneStudy('CAN_IMPORT'))")
    ResponseEntity<String> uploadProcessedDataset(
        @Parameter(name = "image detail") @RequestPart("image") MultipartFile imageFile, 
        @Parameter(name = "header detail", required = false) @RequestPart(value = "header", required = false) MultipartFile headerFile) 
        throws RestServiceException;
    
    @Operation(summary = "Import one DICOM .zip file", description = "Import DICOM .zip file already uploaded", tags = {
			"Import one DICOM .zip file", })
	@ApiResponses(value = { @ApiResponse(responseCode = "200", description = "success returns file path"),
			@ApiResponse(responseCode = "400", description = "Invalid input / Bad Request"),
			@ApiResponse(responseCode = "409", description = "Already exists - conflict"),
			@ApiResponse(responseCode = "200", description = "Unexpected Error") })
	@PostMapping(value = "/import_dicom/", produces = { "application/json" }, consumes = { "application/json" })
	ResponseEntity<ImportJob> importDicomZipFile(@Parameter(name = "file path") @RequestBody String dicomZipFilename)
			throws RestServiceException;
    
    // used by ShanoirUploader!!! 3. step
    @Operation(summary = "Start import job", description = "Start import job")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "import job started"),
        @ApiResponse(responseCode = "400", description = "Invalid input / Bad Request"),
        @ApiResponse(responseCode = "500", description = "unexpected error") })
    @PostMapping(value = "/start_import_job/", consumes = { "application/json" }, produces = { "application/json" })
    @PreAuthorize("hasRole('ADMIN') or (hasAnyRole('EXPERT', 'USER') and @importSecurityService.hasRightOnStudy(#importJob.getStudyId(), 'CAN_IMPORT'))")
    ResponseEntity<Void> startImportJob(@Parameter(name = "ImportJob", required=true) @RequestBody ImportJob importJob) throws RestServiceException;

    @Operation(summary = "Start analysis of EEG job", description = "Start analysis eeg job")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "import eeg job analysed"),
        @ApiResponse(responseCode = "400", description = "Invalid input / Bad Request"),
        @ApiResponse(responseCode = "500", description = "unexpected error") })
    @PostMapping(value = "/start_analysis_eeg_job/",
        produces = { "application/json" },
        consumes = { "application/json" })
    @PreAuthorize("hasRole('ADMIN') or (hasAnyRole('EXPERT', 'USER') and @importSecurityService.hasRightOnOneStudy('CAN_IMPORT'))")
    ResponseEntity<EegImportJob> analyzeEegZipFile(@Parameter(name = "EegImportJob", required=true) @RequestBody EegImportJob importJob) throws RestServiceException;

    @Operation(summary = "Start import EEG job", description = "Start import eeg job")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "import eeg job started"),
        @ApiResponse(responseCode = "400", description = "Invalid input / Bad Request"),
        @ApiResponse(responseCode = "500", description = "unexpected error") })
    @PostMapping(value = "/start_import_eeg_job/",
        produces = { "application/json" },
        consumes = { "application/json" })
    @PreAuthorize("hasRole('ADMIN') or (hasAnyRole('EXPERT', 'USER') and @importSecurityService.hasRightOnStudy(#importJob.getStudyId(), 'CAN_IMPORT'))")
    ResponseEntity<Void> startImportEEGJob(@Parameter(name = "EegImportJob", required=true) @RequestBody EegImportJob importJob) throws RestServiceException;

    @Operation(summary = "ImportFromPACS: Query PACS", description = "ImportFromPACS: Query PACS")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "query the PACS started"),
        @ApiResponse(responseCode = "400", description = "Invalid input / Bad Request"),
        @ApiResponse(responseCode = "500", description = "unexpected error") })
    @PostMapping(value = "/query_pacs/",
        produces = { "application/json" },
        consumes = { "application/json" })
    @PreAuthorize("hasRole('ADMIN') or (hasAnyRole('EXPERT', 'USER') and @importSecurityService.hasRightOnOneStudy('CAN_IMPORT') and @importSecurityService.canImportFromPACS())")
    ResponseEntity<ImportJob> queryPACS(@Parameter(name = "DicomQuery", required=true) @RequestBody DicomQuery dicomQuery) throws RestServiceException;
    
    @Operation(summary = "Get dicom image", description = "Get dicom image")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "get dicom image"),
        @ApiResponse(responseCode = "400", description = "Invalid input / Bad Request"),
        @ApiResponse(responseCode = "500", description = "unexpected error") })
    @GetMapping(value = "/get_dicom/", produces = { "application/dicom" })
    @PreAuthorize("hasRole('ADMIN') or (hasAnyRole('EXPERT', 'USER') and @importSecurityService.hasRightOnOneStudy('CAN_IMPORT'))")
    ResponseEntity<ByteArrayResource> getDicomImage(@Parameter(name = "path", required=true) @RequestParam(value = "path", required = true) String path) throws RestServiceException, IOException;

}

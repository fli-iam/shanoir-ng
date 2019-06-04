package org.shanoir.ng.importer;

import org.shanoir.ng.importer.dicom.query.DicomQuery;
import org.shanoir.ng.importer.model.ImportJob;
import org.shanoir.ng.shared.exception.RestServiceException;
import org.shanoir.ng.shared.exception.ShanoirException;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
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

@javax.annotation.Generated(value = "io.swagger.codegen.languages.SpringCodegen", date = "2017-08-10T08:45:26.334Z")

@Api(value = "importer", description = "the importer API")
@RequestMapping("/importer")
public interface ImporterApi {

    @ApiOperation(value = "Upload one DICOM .zip file from Shanoir uploader with importJob json file", notes = "Upload DICOM .zip file", response = Void.class, tags={ "Upload one DICOM .zip file from shup", })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "success returns file path", response = Void.class),
        @ApiResponse(code = 400, message = "Invalid input / Bad Request", response = Void.class),
        @ApiResponse(code = 409, message = "Already exists - conflict", response = Void.class),
        @ApiResponse(code = 200, message = "Unexpected Error", response = Error.class) })
    @RequestMapping(value = "/upload_dicom_shup/",
        produces = { "application/json" }, 
        consumes = { "multipart/form-data" },
        method = RequestMethod.POST)
    @PreAuthorize("hasRole('ADMIN') or (hasAnyRole('EXPERT', 'USER') and @importSecurityService.hasRightOnOneStudy('CAN_IMPORT'))")
    ResponseEntity<Void> uploadDicomZipFileFromShup(@ApiParam(value = "file detail") @RequestPart("file") MultipartFile dicomZipFile) throws RestServiceException, ShanoirException;
    
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
    @PreAuthorize("hasRole('ADMIN') or (hasAnyRole('EXPERT', 'USER') and @importSecurityService.hasRightOnOneStudy('CAN_IMPORT'))")
    ResponseEntity<ImportJob> queryPACS(@ApiParam(value = "DicomQuery", required=true) @RequestBody DicomQuery dicomQuery) throws RestServiceException;

}

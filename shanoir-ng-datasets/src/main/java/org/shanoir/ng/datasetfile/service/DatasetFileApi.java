package org.shanoir.ng.datasetfile.service;

import jakarta.validation.Valid;

import org.shanoir.ng.datasetfile.DatasetFile;
import org.shanoir.ng.shared.exception.RestServiceException;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.multipart.MultipartFile;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "datasetfile")
@RequestMapping("/datasetfiles")
public interface DatasetFileApi {

	@Operation(summary = "", description = "Saves a new dataset file", tags = {})
	@ApiResponses(value = { 
			@ApiResponse(responseCode = "200", description = "created file"),
			@ApiResponse(responseCode = "401", description = "unauthorized"),
			@ApiResponse(responseCode = "403", description = "forbidden"),
			@ApiResponse(responseCode = "422", description = "bad parameters"),
			@ApiResponse(responseCode = "500", description = "unexpected error") })
	@PostMapping(value = "",
		produces = { "application/json" },
		consumes ={ "application/json" })
	@PreAuthorize("hasAnyRole('ADMIN', 'EXPERT')")
	ResponseEntity<DatasetFile> saveNewDatasetFile(
			@Parameter(name = "datasetfile to create", required = true) @RequestBody DatasetFile datasetFile,
			BindingResult result)
			throws RestServiceException;

	@Operation(summary = "", description = "Add file on datasetFile", tags = {})
	@ApiResponses(value = { 
			@ApiResponse(responseCode = "200", description = "created file"),
			@ApiResponse(responseCode = "401", description = "unauthorized"),
			@ApiResponse(responseCode = "403", description = "forbidden"),
			@ApiResponse(responseCode = "422", description = "bad parameters"),
			@ApiResponse(responseCode = "500", description = "unexpected error") })
	@PostMapping(value = "file-upload/{datasetFileId}",
		produces = { "application/json" },
		consumes ={ "multipart/form-data" })
	@PreAuthorize("hasAnyRole('ADMIN', 'EXPERT')")
	ResponseEntity<Void> addFile(
			@Parameter(name = "id of the dataset file", required = true) @PathVariable("datasetFileId") Long datasetFileId,
			@Parameter(name = "file to upload", required = true) @Valid @RequestBody MultipartFile file)
			throws RestServiceException;

	@Operation(summary = "", description = "Add all files to the PACS", tags = {})
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "created file"),
			@ApiResponse(responseCode = "401", description = "unauthorized"),
			@ApiResponse(responseCode = "403", description = "forbidden"),
			@ApiResponse(responseCode = "422", description = "bad parameters"),
			@ApiResponse(responseCode = "500", description = "unexpected error") })
	@GetMapping(value = "file-upload-to-pacs/{datasetFileId}",
		produces = { "application/json" })
	@PreAuthorize("hasAnyRole('ADMIN', 'EXPERT')")
	ResponseEntity<Void> addFilesToPacs(
			@Parameter(name = "id of the dataset file", required = true) @PathVariable("datasetFileId") Long datasetFileId)
			throws RestServiceException;
}

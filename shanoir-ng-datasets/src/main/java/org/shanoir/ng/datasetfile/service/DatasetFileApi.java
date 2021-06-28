package org.shanoir.ng.datasetfile.service;

import javax.validation.Valid;

import org.shanoir.ng.datasetfile.DatasetFile;
import org.shanoir.ng.shared.exception.ErrorModel;
import org.shanoir.ng.shared.exception.RestServiceException;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.multipart.MultipartFile;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

@Api(value = "datasetfile", description = "the datasetfile API")
@RequestMapping("/datasetfiles")
public interface DatasetFileApi {

	@ApiOperation(value = "", notes = "Saves a new dataset file", response = DatasetFile.class, tags = {})
	@ApiResponses(value = { @ApiResponse(code = 200, message = "created file", response = DatasetFile.class),
			@ApiResponse(code = 401, message = "unauthorized", response = Void.class),
			@ApiResponse(code = 403, message = "forbidden", response = Void.class),
			@ApiResponse(code = 422, message = "bad parameters", response = Void.class),
			@ApiResponse(code = 500, message = "unexpected error", response = ErrorModel.class) })
	@PostMapping(value = "",
		produces = { "application/json" },
		consumes ={ "application/json" })
	@PreAuthorize("hasAnyRole('ADMIN', 'EXPERT')")
	ResponseEntity<DatasetFile> saveNewDatasetFile(
			@ApiParam(value = "datasetfile to create", required = true) @RequestBody DatasetFile datasetFile,
			BindingResult result)
			throws RestServiceException;

	@ApiOperation(value = "", notes = "Add file on datasetFile", response = Void.class, tags = {})
	@ApiResponses(value = { @ApiResponse(code = 200, message = "created file", response = Void.class),
			@ApiResponse(code = 401, message = "unauthorized", response = Void.class),
			@ApiResponse(code = 403, message = "forbidden", response = Void.class),
			@ApiResponse(code = 422, message = "bad parameters", response = Void.class),
			@ApiResponse(code = 500, message = "unexpected error", response = ErrorModel.class) })
	@PostMapping(value = "file-upload/{datasetFileId}",
		produces = { "application/json" },
		consumes ={ "multipart/form-data" })
	@PreAuthorize("hasAnyRole('ADMIN', 'EXPERT')")
	ResponseEntity<Void> addFile(
			@ApiParam(value = "id of the dataset file", required = true) @PathVariable("datasetFileId") Long datasetFileId,
			@ApiParam(value = "file to upload", required = true) @Valid @RequestBody MultipartFile file,
			BindingResult result)
			throws RestServiceException;
}

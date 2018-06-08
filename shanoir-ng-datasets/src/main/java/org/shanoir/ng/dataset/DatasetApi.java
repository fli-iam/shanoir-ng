/**
 * https://github.com/swagger-api/swagger-codegen
 * Do not edit the class manually.
 */
package org.shanoir.ng.dataset;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.List;

import javax.annotation.Resource;
import javax.validation.Valid;

import org.shanoir.ng.shared.exception.ErrorModel;
import org.shanoir.ng.shared.exception.RestServiceException;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

@Api(value = "dataset", description = "the dataset API")
@RequestMapping("/datasets")
public interface DatasetApi {

	@ApiOperation(value = "", notes = "Deletes a dataset", response = Void.class, tags = {})
	@ApiResponses(value = { @ApiResponse(code = 204, message = "dataset deleted", response = Void.class),
			@ApiResponse(code = 401, message = "unauthorized", response = Void.class),
			@ApiResponse(code = 403, message = "forbidden", response = Void.class),
			@ApiResponse(code = 404, message = "no dataset found", response = Void.class),
			@ApiResponse(code = 500, message = "unexpected error", response = ErrorModel.class) })
	@RequestMapping(value = "/{datasetId}", produces = { "application/json" }, method = RequestMethod.DELETE)
	ResponseEntity<Void> deleteDataset(
			@ApiParam(value = "id of the dataset", required = true) @PathVariable("datasetId") Long datasetId)
			throws RestServiceException;

	@ApiOperation(value = "", notes = "If exists, returns the dataset corresponding to the given id", response = Dataset.class, tags = {})
	@ApiResponses(value = { @ApiResponse(code = 200, message = "found dataset", response = Dataset.class),
			@ApiResponse(code = 401, message = "unauthorized", response = Void.class),
			@ApiResponse(code = 403, message = "forbidden", response = Void.class),
			@ApiResponse(code = 404, message = "no study found", response = Void.class),
			@ApiResponse(code = 500, message = "unexpected error", response = ErrorModel.class) })
	@RequestMapping(value = "/{datasetId}", produces = { "application/json" }, method = RequestMethod.GET)
	ResponseEntity<DatasetDTO> findDatasetById(
			@ApiParam(value = "id of the dataset", required = true) @PathVariable("datasetId") Long datasetId);

	@ApiOperation(value = "", notes = "Updates a dataset", response = Void.class, tags = {})
	@ApiResponses(value = { @ApiResponse(code = 204, message = "dataset updated", response = Void.class),
			@ApiResponse(code = 401, message = "unauthorized", response = Void.class),
			@ApiResponse(code = 403, message = "forbidden", response = Void.class),
			@ApiResponse(code = 422, message = "bad parameters", response = ErrorModel.class),
			@ApiResponse(code = 500, message = "unexpected error", response = ErrorModel.class) })
	@RequestMapping(value = "/{datasetId}", produces = { "application/json" }, consumes = {
			"application/json" }, method = RequestMethod.PUT)
	ResponseEntity<Void> updateDataset(
			@ApiParam(value = "id of the dataset", required = true) @PathVariable("datasetId") Long datasetId,
			@ApiParam(value = "study to update", required = true) @Valid @RequestBody Dataset dataset,
			BindingResult result) throws RestServiceException;
	
	@ApiOperation(value = "", notes = "Returns all the datasets", response = Dataset.class, responseContainer = "List", tags = {})
	@ApiResponses(value = { @ApiResponse(code = 200, message = "found datasets", response = Dataset.class),
			@ApiResponse(code = 204, message = "no user found", response = ErrorModel.class),
			@ApiResponse(code = 401, message = "unauthorized", response = ErrorModel.class),
			@ApiResponse(code = 403, message = "forbidden", response = ErrorModel.class),
			@ApiResponse(code = 500, message = "unexpected error", response = ErrorModel.class) })
	@RequestMapping(value = "", produces = { "application/json" }, method = RequestMethod.GET)
	@PreAuthorize("hasRole('ADMIN')")
	ResponseEntity<List<DatasetDTO>> findDatasets() throws RestServiceException;

    @ApiOperation(value = "", nickname = "downloadDatasetById", notes = "If exists, returns a zip file of the dataset corresponding to the given id", response = Resource.class, tags={  })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "zip file", response = Resource.class),
        @ApiResponse(code = 401, message = "unauthorized"),
        @ApiResponse(code = 403, message = "forbidden"),
        @ApiResponse(code = 404, message = "no dataset found"),
        @ApiResponse(code = 500, message = "unexpected error", response = ErrorModel.class) })
    @RequestMapping(value = "/download/{datasetId}",
        produces = { "application/zip" }, 
        method = RequestMethod.GET)
    ResponseEntity<ByteArrayResource> downloadDatasetById(
    		@ApiParam(value = "id of the dataset", required=true) @PathVariable("datasetId") Long datasetId,
    		@ApiParam(value = "Decide if you want to download dicom (dcm) or nifti (nii) files.", 
    			allowableValues = "dcm, nii", defaultValue = "dcm") @Valid 
    		@RequestParam(value = "format", required = false, defaultValue="dcm") String format) throws RestServiceException, MalformedURLException, IOException;
	
}

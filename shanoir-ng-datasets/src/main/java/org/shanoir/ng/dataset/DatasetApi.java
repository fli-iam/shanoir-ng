/**
 * https://github.com/swagger-api/swagger-codegen
 * Do not edit the class manually.
 */
package org.shanoir.ng.dataset;

import javax.validation.Valid;

import org.shanoir.ng.shared.exception.ErrorModel;
import org.shanoir.ng.shared.exception.RestServiceException;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

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
	ResponseEntity<Dataset> findDatasetById(
			@ApiParam(value = "id of the dataset", required = true) @PathVariable("datasetId") Long datasetId);

	@ApiOperation(value = "", notes = "Saves a new dataset", response = Dataset.class, tags = {})
	@ApiResponses(value = { @ApiResponse(code = 200, message = "created study", response = Dataset.class),
			@ApiResponse(code = 401, message = "unauthorized", response = Void.class),
			@ApiResponse(code = 403, message = "forbidden", response = Void.class),
			@ApiResponse(code = 422, message = "bad parameters", response = ErrorModel.class),
			@ApiResponse(code = 500, message = "unexpected error", response = ErrorModel.class) })
	@RequestMapping(value = "", produces = { "application/json" }, consumes = {
			"application/json" }, method = RequestMethod.POST)
	ResponseEntity<Dataset> saveNewDataset(
			@ApiParam(value = "dataset to create", required = true) @Valid @RequestBody Dataset dataset,
			BindingResult result) throws RestServiceException;

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
			@ApiParam(value = "study to update", required = true) @Valid @RequestBody Dataset study,
			BindingResult result) throws RestServiceException;

}

package org.shanoir.ng.dataset.controler;

import org.shanoir.ng.dataset.model.DatasetExpression;
import org.shanoir.ng.shared.exception.ErrorModel;
import org.shanoir.ng.shared.exception.RestServiceException;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

@Api(value = "datasetexpression", description = "the datasetexpression API")
@RequestMapping("/datasetexpressions")
public interface DatasetExpressionApi {

	@ApiOperation(value = "", notes = "Saves a new dataset expression", response = DatasetExpression.class, tags = {})
	@ApiResponses(value = { @ApiResponse(code = 200, message = "created expression", response = DatasetExpression.class),
			@ApiResponse(code = 401, message = "unauthorized", response = Void.class),
			@ApiResponse(code = 403, message = "forbidden", response = Void.class),
			@ApiResponse(code = 422, message = "bad parameters", response = Void.class),
			@ApiResponse(code = 500, message = "unexpected error", response = ErrorModel.class) })
	@PostMapping(value = "", produces = { "application/json" }, consumes = {
			"application/json" })
	@PreAuthorize("hasAnyRole('ADMIN', 'EXPERT')")
	ResponseEntity<DatasetExpression> saveNewDatasetExpression(
			@ApiParam(value = "datasetExpression to create", required = true) @RequestBody DatasetExpression expression, BindingResult result)
			throws RestServiceException;
}

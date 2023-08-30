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

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "datasetexpression")
@RequestMapping("/datasetexpressions")
public interface DatasetExpressionApi {

	@Operation(summary = "", description = "Saves a new dataset expression", tags = {})
	@ApiResponses(value = { 
			@ApiResponse(responseCode = "200", description = "created expression"),
			@ApiResponse(responseCode = "401", description = "unauthorized"),
			@ApiResponse(responseCode = "403", description = "forbidden"),
			@ApiResponse(responseCode = "422", description = "bad parameters"),
			@ApiResponse(responseCode = "500", description = "unexpected error") })
	@PostMapping(value = "", produces = { "application/json" }, consumes = {
			"application/json" })
	@PreAuthorize("hasAnyRole('ADMIN', 'EXPERT')")
	ResponseEntity<DatasetExpression> saveNewDatasetExpression(
			@Parameter(name = "datasetExpression to create", required = true) @RequestBody DatasetExpression expression, BindingResult result)
			throws RestServiceException;
}

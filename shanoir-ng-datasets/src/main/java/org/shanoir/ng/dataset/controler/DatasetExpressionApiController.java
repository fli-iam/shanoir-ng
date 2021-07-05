package org.shanoir.ng.dataset.controler;

import org.shanoir.ng.dataset.model.DatasetExpression;
import org.shanoir.ng.dataset.service.DatasetExpressionService;
import org.shanoir.ng.shared.exception.RestServiceException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.RequestBody;

import io.swagger.annotations.ApiParam;

@Controller
public class DatasetExpressionApiController implements DatasetExpressionApi {

	@Autowired
	DatasetExpressionService datasetExpressionService;
	
	@Override
	public ResponseEntity<DatasetExpression> saveNewDatasetExpression(
			@ApiParam(value = "datasetExpression to create", required = true) @RequestBody DatasetExpression expression, BindingResult result)
			throws RestServiceException {
		return new ResponseEntity<>(datasetExpressionService.create(expression), HttpStatus.OK);
	}
}

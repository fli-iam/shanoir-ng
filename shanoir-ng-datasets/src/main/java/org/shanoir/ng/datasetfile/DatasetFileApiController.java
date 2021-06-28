package org.shanoir.ng.datasetfile;

import org.shanoir.ng.datasetfile.service.DatasetFileApi;
import org.shanoir.ng.datasetfile.service.DatasetFileService;
import org.shanoir.ng.shared.exception.RestServiceException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.RequestBody;

import io.swagger.annotations.ApiParam;

@Controller
public class DatasetFileApiController implements DatasetFileApi {

	@Autowired
	DatasetFileService datasetFileService;
	
	@Override
	public ResponseEntity<DatasetFile> saveNewDatasetFile(
			@ApiParam(value = "datasetfile to create", required = true) @RequestBody DatasetFile file, BindingResult result)
			throws RestServiceException {
				return new ResponseEntity<>(datasetFileService.create(file), HttpStatus.OK);
	}
}

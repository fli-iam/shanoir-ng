package org.shanoir.ng.importer;

import javax.validation.Valid;

import org.shanoir.ng.importer.dto.ImportJob;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;

import io.swagger.annotations.ApiParam;

@javax.annotation.Generated(value = "io.swagger.codegen.languages.SpringCodegen", date = "2018-01-09T09:20:01.478Z")

@Controller
public class DatasetAcquisitionApiController implements DatasetAcquisitionApi {

	@Autowired
	private ImporterService importerService;

	public ResponseEntity<Void> createNewDatasetAcquisition(
			@ApiParam(value = "DatasetAcquisition to create", required = true) @Valid @RequestBody ImportJob importJob) {
		importerService.setImportJob(importJob);
		importerService.createAllDatasetAcquisition();
		return new ResponseEntity<Void>(HttpStatus.OK);
	}

}

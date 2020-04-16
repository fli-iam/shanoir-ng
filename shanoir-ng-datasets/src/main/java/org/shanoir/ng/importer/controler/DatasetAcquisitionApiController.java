/**
 * Shanoir NG - Import, manage and share neuroimaging data
 * Copyright (C) 2009-2019 Inria - https://www.inria.fr/
 * Contact us on https://project.inria.fr/shanoir/
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see https://www.gnu.org/licenses/gpl-3.0.html
 */

package org.shanoir.ng.importer.controler;

import javax.validation.Valid;

import org.shanoir.ng.importer.dto.EegImportJob;
import org.shanoir.ng.importer.dto.ImportJob;
import org.shanoir.ng.importer.service.ImporterService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;

import io.swagger.annotations.ApiParam;

@javax.annotation.Generated(value = "io.swagger.codegen.languages.SpringCodegen", date = "2018-01-09T09:20:01.478Z")

@Controller
public class DatasetAcquisitionApiController implements DatasetAcquisitionApi {

	private static final Logger LOG = LoggerFactory.getLogger(DatasetAcquisitionApiController.class);
	
	@Autowired
	private ImporterService importerService;

	@Override
	public ResponseEntity<Void> createNewDatasetAcquisition(
			@ApiParam(value = "DatasetAcquisition to create", required = true) @Valid @RequestBody ImportJob importJob) {
		try {
			long startTime = System.currentTimeMillis();
			importerService.createAllDatasetAcquisition(importJob);
		    long endTime = System.currentTimeMillis();
		    long duration = endTime - startTime;
		    LOG.info("Creation of dataset acquisition required " + duration + " millis.");
		} catch (Exception e) {
			return new ResponseEntity<Void>(HttpStatus.INTERNAL_SERVER_ERROR);
		} finally {
			importerService.cleanTempFiles(importJob.getWorkFolder());
		}
		return new ResponseEntity<Void>(HttpStatus.OK);
	}

	@Override
	public ResponseEntity<Void> createNewEegDatasetAcquisition(@ApiParam(value = "DatasetAcquisition to create" ,required=true )  @Valid @RequestBody EegImportJob importJob) {
		importerService.createEegDataset(importJob);
		importerService.cleanTempFiles(importJob.getWorkFolder());
		return new ResponseEntity<Void>(HttpStatus.OK);
	}

}

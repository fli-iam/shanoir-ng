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

package org.shanoir.ng.datasetacquisition.controler;

import java.util.List;

import javax.validation.Valid;

import org.shanoir.ng.datasetacquisition.model.DatasetAcquisition;
import org.shanoir.ng.datasetacquisition.service.DatasetAcquisitionService;
import org.shanoir.ng.importer.dto.ImportJob;
import org.shanoir.ng.importer.service.ImporterService;
import org.shanoir.ng.shared.error.FieldErrorMap;
import org.shanoir.ng.shared.exception.EntityNotFoundException;
import org.shanoir.ng.shared.exception.ErrorDetails;
import org.shanoir.ng.shared.exception.ErrorModel;
import org.shanoir.ng.shared.exception.RestServiceException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;

import io.swagger.annotations.ApiParam;

@Controller
public class DatasetAcquisitionApiController implements DatasetAcquisitionApi {

	
	@Autowired
	private DatasetAcquisitionService datasetAcquisitionService;
	
	@Autowired
	private ImporterService importerService;

	
	@Override
	public ResponseEntity<Void> createNewDatasetAcquisition(
			@ApiParam(value = "DatasetAcquisition to create", required = true) @Valid @RequestBody ImportJob importJob) {
		importerService.createAllDatasetAcquisition(importJob);
		importerService.cleanTempFiles(importJob.getWorkFolder());
		return new ResponseEntity<Void>(HttpStatus.OK);
	}
	
	@Override
	public ResponseEntity<List<DatasetAcquisition>> findByStudyCard(
			@ApiParam(value = "id of the study card", required = true) @PathVariable("studyCardId") Long studyCardId) {
		
		List<DatasetAcquisition> daList = datasetAcquisitionService.findByStudyCard(studyCardId);
		if (daList.isEmpty()) return new ResponseEntity<>(HttpStatus.NO_CONTENT);
		else return new ResponseEntity<>(daList, HttpStatus.OK);
	}
	
	@Override
	public ResponseEntity<Void> deleteDatasetAcquisition(
			@ApiParam(value = "id of the datasetAcquisition", required = true) @PathVariable("datasetAcquisitionId") Long datasetAcquisitionId)
			throws RestServiceException {

		try {
			datasetAcquisitionService.deleteById(datasetAcquisitionId);
			return new ResponseEntity<>(HttpStatus.NO_CONTENT);
			
		} catch (EntityNotFoundException e) {
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		}
	}

	@Override
	public ResponseEntity<DatasetAcquisition> findDatasetAcquisitionById(
			@ApiParam(value = "id of the datasetAcquisition", required = true) @PathVariable("datasetAcquisitionId") Long datasetAcquisitionId) {
		
		final DatasetAcquisition datasetAcquisition = datasetAcquisitionService.findById(datasetAcquisitionId);
		if (datasetAcquisition == null) {
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		}
		return new ResponseEntity<>(datasetAcquisition, HttpStatus.OK);
	}

	@Override
	public ResponseEntity<List<DatasetAcquisition>> findDatasetAcquisitions() {
		final List<DatasetAcquisition> datasetAcquisitions = datasetAcquisitionService.findAll();
		if (datasetAcquisitions.isEmpty()) {
			return new ResponseEntity<>(HttpStatus.NO_CONTENT);
		}
		return new ResponseEntity<>(datasetAcquisitions, HttpStatus.OK);
	}

	@Override
	public ResponseEntity<Void> updateDatasetAcquisition(
			@ApiParam(value = "id of the datasetAcquisition", required = true) @PathVariable("datasetAcquisitionId") Long datasetAcquisitionId,
			@ApiParam(value = "datasetAcquisition to update", required = true) @Valid @RequestBody DatasetAcquisition datasetAcquisition,
			final BindingResult result) throws RestServiceException {

		validate(result);
		try {
			datasetAcquisitionService.update(datasetAcquisition);
			return new ResponseEntity<>(HttpStatus.NO_CONTENT);
		
		} catch (EntityNotFoundException e) {
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		}
	}
	
	
	private void validate(BindingResult result) throws RestServiceException {
		final FieldErrorMap errors = new FieldErrorMap(result);
		if (!errors.isEmpty()) {
			ErrorModel error = new ErrorModel(HttpStatus.UNPROCESSABLE_ENTITY.value(), "Bad arguments", new ErrorDetails(errors));
			throw new RestServiceException(error);
		} 
	}
}

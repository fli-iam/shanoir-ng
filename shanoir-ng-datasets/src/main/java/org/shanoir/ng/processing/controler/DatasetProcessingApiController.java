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

package org.shanoir.ng.processing.controler;

import java.util.List;

import javax.validation.Valid;

import org.shanoir.ng.processing.dto.DatasetProcessingDTO;
import org.shanoir.ng.processing.dto.mapper.DatasetProcessingMapper;
import org.shanoir.ng.processing.model.DatasetProcessing;
import org.shanoir.ng.processing.service.DatasetProcessingService;
import org.shanoir.ng.shared.error.FieldErrorMap;
import org.shanoir.ng.shared.event.ShanoirEventService;
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
public class DatasetProcessingApiController implements DatasetProcessingApi {

	@Autowired
	private DatasetProcessingMapper datasetProcessingMapper;

	@Autowired
	private DatasetProcessingService datasetProcessingService;

	@Override
	public ResponseEntity<Void> deleteDatasetProcessing(
			@ApiParam(value = "id of the dataset processing", required = true) @PathVariable("datasetProcessingId") Long datasetProcessingId)
			throws RestServiceException {

		try {
			datasetProcessingService.deleteById(datasetProcessingId);
			return new ResponseEntity<>(HttpStatus.NO_CONTENT);
			
		} catch (EntityNotFoundException e) {
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		}
	}

	@Override
	public ResponseEntity<DatasetProcessingDTO> findDatasetProcessingById(
			@ApiParam(value = "id of the dataset processing", required = true) @PathVariable("datasetProcessingId") Long datasetProcessingId) {
		
		final DatasetProcessing datasetProcessing = datasetProcessingService.findById(datasetProcessingId);
		if (datasetProcessing == null) {
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		}
		return new ResponseEntity<>(datasetProcessingMapper.datasetProcessingToDatasetProcessingDTO(datasetProcessing), HttpStatus.OK);
	}

	@Override
	public ResponseEntity<List<DatasetProcessingDTO>> findDatasetProcessings() {
		final List<DatasetProcessing> datasetProcessings = datasetProcessingService.findAll();
		if (datasetProcessings.isEmpty()) {
			return new ResponseEntity<>(HttpStatus.NO_CONTENT);
		}
		return new ResponseEntity<>(datasetProcessingMapper.datasetProcessingsToDatasetProcessingDTOs(datasetProcessings), HttpStatus.OK);
	}

	@Override
	public ResponseEntity<DatasetProcessingDTO> saveNewDatasetProcessing(
			@ApiParam(value = "dataset processing to create", required = true) @Valid @RequestBody DatasetProcessing datasetProcessing,
			final BindingResult result) throws RestServiceException {
		
		/* Validation */
		validate(result);

		/* Save dataset processing in db. */
		final DatasetProcessing createdDatasetProcessing = datasetProcessingService.create(datasetProcessing);
		return new ResponseEntity<>(datasetProcessingMapper.datasetProcessingToDatasetProcessingDTO(createdDatasetProcessing), HttpStatus.OK);
	}

	@Override
	public ResponseEntity<Void> updateDatasetProcessing(
			@ApiParam(value = "id of the dataset processing", required = true) @PathVariable("datasetProcessingId") Long datasetProcessingId,
			@ApiParam(value = "dataset processing to update", required = true) @Valid @RequestBody DatasetProcessing datasetProcessing,
			final BindingResult result) throws RestServiceException {

		validate(result);
		try {
			datasetProcessingService.update(datasetProcessing);
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

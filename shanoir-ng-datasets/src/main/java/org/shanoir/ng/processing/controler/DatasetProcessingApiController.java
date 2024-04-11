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

import io.swagger.v3.oas.annotations.Parameter;
import jakarta.validation.Valid;
import org.apache.solr.client.solrj.SolrServerException;
import org.shanoir.ng.dataset.dto.DatasetDTO;
import org.shanoir.ng.dataset.dto.mapper.DatasetMapper;
import org.shanoir.ng.dataset.model.Dataset;
import org.shanoir.ng.processing.dto.DatasetProcessingDTO;
import org.shanoir.ng.processing.dto.mapper.DatasetProcessingMapper;
import org.shanoir.ng.processing.model.DatasetProcessing;
import org.shanoir.ng.processing.service.DatasetProcessingService;
import org.shanoir.ng.shared.error.FieldErrorMap;
import org.shanoir.ng.shared.exception.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.util.CollectionUtils;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Controller
public class DatasetProcessingApiController implements DatasetProcessingApi {

	private static final Logger LOG = LoggerFactory.getLogger(DatasetProcessingApiController.class);


	@Autowired
	private DatasetMapper datasetMapper;

	@Autowired
	private DatasetProcessingMapper datasetProcessingMapper;

	@Autowired
	private DatasetProcessingService datasetProcessingService;

	@Override
	public ResponseEntity<Void> deleteDatasetProcessing(
			@Parameter(name = "id of the dataset processing", required = true) @PathVariable("datasetProcessingId") Long datasetProcessingId)
			throws RestServiceException {

		try {
			datasetProcessingService.deleteById(datasetProcessingId);
			return new ResponseEntity<>(HttpStatus.NO_CONTENT);

		} catch (IOException | SolrServerException | ShanoirException e) {
			LOG.error("Error while deleting datasets: ", e);
			return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	@Override
	public ResponseEntity<DatasetProcessingDTO> findDatasetProcessingById(
			@Parameter(name = "id of the dataset processing", required = true) @PathVariable("datasetProcessingId") Long datasetProcessingId) {
		
		final Optional<DatasetProcessing> datasetProcessing = datasetProcessingService.findById(datasetProcessingId);
		if (!datasetProcessing.isPresent()) {
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		}
		return new ResponseEntity<>(datasetProcessingMapper.datasetProcessingToDatasetProcessingDTO(datasetProcessing.get()), HttpStatus.OK);
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
	public ResponseEntity<List<DatasetProcessingDTO>> findDatasetProcessingsByStudyIdAndSubjectId(
			@Parameter(name = "id of the study", required = true) @PathVariable("studyId") Long studyId,
			@Parameter(name = "id of the subject", required = true) @PathVariable("subjectId") Long subjectId) {
		List<DatasetProcessing> datasetProcessings = datasetProcessingService.findAll();
		if (datasetProcessings.isEmpty()) {
			return new ResponseEntity<>(HttpStatus.NO_CONTENT);
		}
		datasetProcessings = datasetProcessings.stream().filter(processing -> {
			return !CollectionUtils.isEmpty(processing.getInputDatasets())
					&& processing.getInputDatasets().get(0).getStudyId().equals(studyId)
					&& processing.getInputDatasets().get(0).getSubjectId().equals(subjectId);
		}).collect(Collectors.toList());
		if (datasetProcessings.isEmpty()) {
			return new ResponseEntity<>(HttpStatus.NO_CONTENT);
		}
		return new ResponseEntity<>(datasetProcessingMapper.datasetProcessingsToDatasetProcessingDTOs(datasetProcessings), HttpStatus.OK);
	}

	@Override
	public ResponseEntity<List<DatasetDTO>> getInputDatasets(
			@Parameter(name = "id of the dataset processing", required = true) @PathVariable("datasetProcessingId") Long datasetProcessingId) {
		final Optional<DatasetProcessing> datasetProcessing = datasetProcessingService.findById(datasetProcessingId);
		List<Dataset> inputDatasets = datasetProcessing.get().getInputDatasets();
		return new ResponseEntity<>(datasetMapper.datasetToDatasetDTO(inputDatasets), HttpStatus.OK);
	}

	@Override
	public ResponseEntity<List<DatasetDTO>> getOutputDatasets(
			@Parameter(name = "id of the dataset processing", required = true) @PathVariable("datasetProcessingId") Long datasetProcessingId) {
		final Optional<DatasetProcessing> datasetProcessing = datasetProcessingService.findById(datasetProcessingId);
		List<Dataset> outputDatasets = datasetProcessing.get().getOutputDatasets();
		return new ResponseEntity<>(datasetMapper.datasetToDatasetDTO(outputDatasets), HttpStatus.OK);
	}

	@Override
	public ResponseEntity<DatasetProcessingDTO> saveNewDatasetProcessing(
			@Parameter(name = "dataset processing to create", required = true) @Valid @RequestBody DatasetProcessing datasetProcessing,
			final BindingResult result) throws RestServiceException {
		
		/* Validation */
		validate(result);

		/* Save dataset processing in db. */
		final DatasetProcessing createdDatasetProcessing = datasetProcessingService.create(datasetProcessing);
		return new ResponseEntity<>(datasetProcessingMapper.datasetProcessingToDatasetProcessingDTO(createdDatasetProcessing), HttpStatus.OK);
	}

	@Override
	public ResponseEntity<Void> updateDatasetProcessing(
			@Parameter(name = "id of the dataset processing", required = true) @PathVariable("datasetProcessingId") Long datasetProcessingId,
			@Parameter(name = "dataset processing to update", required = true) @Valid @RequestBody DatasetProcessing datasetProcessing,
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

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
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.apache.poi.ss.extractor.ExcelExtractor;
import org.apache.solr.client.solrj.SolrServerException;
import org.shanoir.ng.dataset.dto.DatasetDTO;
import org.shanoir.ng.dataset.dto.mapper.DatasetMapper;
import org.shanoir.ng.dataset.model.Dataset;
import org.shanoir.ng.dataset.model.DatasetExpressionFormat;
import org.shanoir.ng.dataset.service.DatasetService;
import org.shanoir.ng.examination.model.Examination;
import org.shanoir.ng.examination.service.ExaminationService;
import org.shanoir.ng.processing.dto.DatasetProcessingDTO;
import org.shanoir.ng.processing.dto.mapper.DatasetProcessingMapper;
import org.shanoir.ng.processing.model.DatasetProcessing;
import org.shanoir.ng.processing.model.DatasetProcessingType;
import org.shanoir.ng.processing.service.DatasetProcessingService;
import org.shanoir.ng.processing.service.ProcessingDownloaderServiceImpl;
import org.shanoir.ng.shared.error.FieldErrorMap;
import org.shanoir.ng.shared.exception.*;
import org.shanoir.ng.utils.KeycloakUtil;
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
import org.springframework.web.bind.annotation.RequestParam;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
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

	@Autowired
	private ProcessingDownloaderServiceImpl processingDownloaderService;

	@Autowired
	private ExaminationService examinationService;

	public DatasetProcessingApiController() {

	}

	@Override
	public ResponseEntity<Void> deleteDatasetProcessing(
			@Parameter(description = "id of the dataset processing", required = true) @PathVariable("datasetProcessingId") Long datasetProcessingId)
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
			@Parameter(description = "id of the dataset processing", required = true) @PathVariable("datasetProcessingId") Long datasetProcessingId) {
		
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
	public ResponseEntity<List<DatasetDTO>> getInputDatasets(
			@Parameter(description = "id of the dataset processing", required = true) @PathVariable("datasetProcessingId") Long datasetProcessingId) {
		final Optional<DatasetProcessing> datasetProcessing = datasetProcessingService.findById(datasetProcessingId);
		List<Dataset> inputDatasets = datasetProcessing.get().getInputDatasets();
		return new ResponseEntity<>(datasetMapper.datasetToDatasetDTO(inputDatasets), HttpStatus.OK);
	}

	@Override
	public ResponseEntity<List<DatasetDTO>> getOutputDatasets(
			@Parameter(description = "id of the dataset processing", required = true) @PathVariable("datasetProcessingId") Long datasetProcessingId) {
		final Optional<DatasetProcessing> datasetProcessing = datasetProcessingService.findById(datasetProcessingId);
		List<Dataset> outputDatasets = datasetProcessing.get().getOutputDatasets();
		return new ResponseEntity<>(datasetMapper.datasetToDatasetDTO(outputDatasets), HttpStatus.OK);
	}

	@Override
	public ResponseEntity<DatasetProcessingDTO> saveNewDatasetProcessing(
			@Parameter(description = "dataset processing to create", required = true) @Valid @RequestBody DatasetProcessing datasetProcessing,
			final BindingResult result) throws RestServiceException {

		/* set authenticated username */
		datasetProcessing.setUsername(KeycloakUtil.getTokenUserName());
		
		/* Validation */
		validate(result);
		datasetProcessingService.validateDatasetProcessing(datasetProcessing);

		/* Save dataset processing in db. */
		final DatasetProcessing createdDatasetProcessing = datasetProcessingService.create(datasetProcessing);
		return new ResponseEntity<>(datasetProcessingMapper.datasetProcessingToDatasetProcessingDTO(createdDatasetProcessing), HttpStatus.OK);
	}

	@Override
	public ResponseEntity<Void> updateDatasetProcessing(
			@Parameter(description = "id of the dataset processing", required = true) @PathVariable("datasetProcessingId") Long datasetProcessingId,
			@Parameter(description = "dataset processing to update", required = true) @Valid @RequestBody DatasetProcessing datasetProcessing,
			final BindingResult result) throws RestServiceException {

		validate(result);
		datasetProcessingService.validateDatasetProcessing(datasetProcessing);

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

	@Override
	public void massiveDownloadByProcessingIds(
			@Parameter(description = "ids of processing", required = true) @Valid
			@RequestBody List<Long> processingIds,
			@Parameter(description = "outputs to extract") @Valid
			@RequestParam(value = "resultOnly") boolean resultOnly,
			HttpServletResponse response) throws RestServiceException {

		List<DatasetProcessing> processingList = new ArrayList<>();
		for (Long processingId : processingIds) {
			DatasetProcessing processing = null;
			try {
				if (processingId == null) {
					throw new Exception();
				}
				processing = datasetProcessingService.findById(processingId).get();
				processingList.add(processing);
			}catch (Exception e) {
				throw new RestServiceException(
						new ErrorModel(HttpStatus.FORBIDDEN.value(), processingId + " is not a valid processing id."));
			}
		}
		processingDownloaderService.massiveDownload(processingList, resultOnly, "dcm" , response, false, null);
	}

	@Override
	public void massiveDownloadProcessingByExaminationIds(
			@Parameter(description = "ids of examination", required = true) @Valid
			@RequestBody List<Long> examinationIds,
			@Parameter(description = "comment of the desired processings") @Valid
			@RequestParam(value = "processingComment", required = false) String processingComment,
			@Parameter(description = "outputs to extract") @Valid
			@RequestParam(value = "resultOnly") boolean resultOnly,
			HttpServletResponse response) throws RestServiceException {

		List<Examination> examinationList = new ArrayList<>();
		for (Long examinationId : examinationIds) {
			Examination examination = null;
			try {
				if (examinationId == null) {
					throw new Exception();
				}
				examination = examinationService.findById(examinationId);

				if (Objects.isNull(examination)) {
					throw new Exception();
				}
				examinationList.add(examination);
			}catch (Exception e) {
				throw new RestServiceException(
						new ErrorModel(HttpStatus.FORBIDDEN.value(), examinationId + " is not a valid examination id."));
			}
		}
		processingDownloaderService.massiveDownloadByExaminations(examinationList, processingComment, resultOnly, "dcm" , response, false, null);
	}
}

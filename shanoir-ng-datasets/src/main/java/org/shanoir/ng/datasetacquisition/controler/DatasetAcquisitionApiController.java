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

import java.io.IOException;
import java.util.Comparator;
import java.util.List;

import javax.validation.Valid;

import org.shanoir.ng.datasetacquisition.dto.DatasetAcquisitionDTO;
import org.shanoir.ng.datasetacquisition.dto.ExaminationDatasetAcquisitionDTO;
import org.shanoir.ng.datasetacquisition.dto.mapper.DatasetAcquisitionMapper;
import org.shanoir.ng.datasetacquisition.dto.mapper.ExaminationDatasetAcquisitionMapper;
import org.shanoir.ng.datasetacquisition.model.DatasetAcquisition;
import org.shanoir.ng.datasetacquisition.service.DatasetAcquisitionService;
import org.shanoir.ng.importer.dto.EegImportJob;
import org.shanoir.ng.importer.dto.ImportJob;
import org.shanoir.ng.importer.service.ImporterService;
import org.shanoir.ng.shared.configuration.RabbitMQConfiguration;
import org.shanoir.ng.shared.error.FieldErrorMap;
import org.shanoir.ng.shared.exception.EntityNotFoundException;
import org.shanoir.ng.shared.exception.ErrorDetails;
import org.shanoir.ng.shared.exception.ErrorModel;
import org.shanoir.ng.shared.exception.RestServiceException;
import org.shanoir.ng.shared.exception.ShanoirException;
import org.shanoir.ng.utils.KeycloakUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.AmqpRejectAndDontRequeueException;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.swagger.annotations.ApiParam;

@Controller
public class DatasetAcquisitionApiController implements DatasetAcquisitionApi {

	private static final Logger LOG = LoggerFactory.getLogger(DatasetAcquisitionApiController.class);

	@Autowired
	private DatasetAcquisitionService datasetAcquisitionService;

	@Autowired
	private ImporterService importerService;

	@Autowired
	private ObjectMapper objectMapper;

	@Autowired
	private DatasetAcquisitionMapper dsAcqMapper;

	@Autowired
	private ExaminationDatasetAcquisitionMapper examDsAcqMapper;

	@Override
	public ResponseEntity<Void> createNewDatasetAcquisition(
			@ApiParam(value = "DatasetAcquisition to create", required = true) @Valid @RequestBody ImportJob importJob) throws RestServiceException {
		try {
			importerService.createAllDatasetAcquisition(importJob, KeycloakUtil.getTokenUserId());
		} catch (ShanoirException e) {
			ErrorModel error = new ErrorModel(HttpStatus.INTERNAL_SERVER_ERROR.value(), "Error", e);
			throw new RestServiceException(error);
		}
		importerService.cleanTempFiles(importJob.getWorkFolder());
		return new ResponseEntity<>(HttpStatus.OK);
	}

	@Override
	public ResponseEntity<Void> createNewEegDatasetAcquisition(@ApiParam(value = "DatasetAcquisition to create" ,required=true )  @Valid @RequestBody EegImportJob importJob) {
		importerService.createEegDataset(importJob);
		importerService.cleanTempFiles(importJob.getWorkFolder());
		return new ResponseEntity<>(HttpStatus.OK);
	}

	@RabbitListener(queues = RabbitMQConfiguration.IMPORTER_QUEUE_DATASET)
	@RabbitHandler
	@Transactional
	public void createNewDatasetAcquisition(Message importJobStr) throws JsonParseException, JsonMappingException, IOException, AmqpRejectAndDontRequeueException {
		Long userId = Long.valueOf("" + importJobStr.getMessageProperties().getHeaders().get("x-user-id"));

		ImportJob importJob = objectMapper.readValue(importJobStr.getBody(), ImportJob.class);
		try {
			createAllDatasetAcquisitions(importJob, userId);
		} catch (Exception e) {
			LOG.error(e.getMessage(), e);
			throw new AmqpRejectAndDontRequeueException(e);
		} finally {
			// if the json could not be parsed, no way to know workFolder
			// so better to throw the exception, as no possibility to clean
			importerService.cleanTempFiles(importJob.getWorkFolder());
		}
	}

	private void createAllDatasetAcquisitions(ImportJob importJob, Long userId) throws Exception {
		long startTime = System.currentTimeMillis();
		importerService.createAllDatasetAcquisition(importJob, userId);
		long endTime = System.currentTimeMillis();
		long duration = endTime - startTime;
		LOG.info("Creation of dataset acquisition required " + duration + " millis.");
	}

	@Override
	public ResponseEntity<List<DatasetAcquisitionDTO>> findByStudyCard(
			@ApiParam(value = "id of the study card", required = true) @PathVariable("studyCardId") Long studyCardId) {

		List<DatasetAcquisition> daList = datasetAcquisitionService.findByStudyCard(studyCardId);
		if (daList.isEmpty()) {
			return new ResponseEntity<>(HttpStatus.NO_CONTENT);
		} else {
			return new ResponseEntity<>(dsAcqMapper.datasetAcquisitionsToDatasetAcquisitionDTOs(daList), HttpStatus.OK);
		}
	}

	@Override
	public ResponseEntity<List<ExaminationDatasetAcquisitionDTO>> findDatasetAcquisitionByExaminationId(
			@ApiParam(value = "id of the examination", required = true) @PathVariable("examinationId") Long examinationId) {

		List<DatasetAcquisition> daList = datasetAcquisitionService.findByExamination(examinationId);
		daList.sort(new Comparator<DatasetAcquisition>() {

			@Override
			public int compare(DatasetAcquisition o1, DatasetAcquisition o2) {
				return (o1.getSortingIndex() != null ? o1.getSortingIndex() : 0)
						- (o2.getSortingIndex() != null ? o2.getSortingIndex() : 0);
			}
		});
		if (daList.isEmpty()) {
			return new ResponseEntity<>(HttpStatus.NO_CONTENT);
		} else {
			return new ResponseEntity<>(examDsAcqMapper.datasetAcquisitionsToExaminationDatasetAcquisitionDTOs(daList), HttpStatus.OK);
		}
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
	public ResponseEntity<DatasetAcquisitionDTO> findDatasetAcquisitionById(
			@ApiParam(value = "id of the datasetAcquisition", required = true) @PathVariable("datasetAcquisitionId") Long datasetAcquisitionId) {

		final DatasetAcquisition datasetAcquisition = datasetAcquisitionService.findById(datasetAcquisitionId);
		if (datasetAcquisition == null) {
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		}
		return new ResponseEntity<>(dsAcqMapper.datasetAcquisitionToDatasetAcquisitionDTO(datasetAcquisition), HttpStatus.OK);
	}

	@Override
	public ResponseEntity<Page<DatasetAcquisitionDTO>> findDatasetAcquisitions(final Pageable pageable) throws RestServiceException {
		Page<DatasetAcquisition> datasetAcquisitions = datasetAcquisitionService.findPage(pageable);
		if (datasetAcquisitions.getContent().isEmpty()) {
			return new ResponseEntity<>(HttpStatus.NO_CONTENT);
		}
		return new ResponseEntity<>(dsAcqMapper.datasetAcquisitionsToDatasetAcquisitionDTOs(datasetAcquisitions), HttpStatus.OK);
	}



	@Override
	public ResponseEntity<Void> updateDatasetAcquisition(
			@ApiParam(value = "id of the datasetAcquisition", required = true) @PathVariable("datasetAcquisitionId") Long datasetAcquisitionId,
			@ApiParam(value = "datasetAcquisition to update", required = true) @Valid @RequestBody DatasetAcquisitionDTO datasetAcquisition,
			final BindingResult result) throws RestServiceException {

		validate(result);
		try {
			datasetAcquisitionService.update(dsAcqMapper.datasetAcquisitionDTOToDatasetAcquisition(datasetAcquisition));
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
	public ResponseEntity<DatasetAcquisition> createNewDatasetAcquisition(
			@ApiParam(value = "DatasetAcquisition to create", required = true) @RequestBody DatasetAcquisition acquisition,
			final BindingResult result) throws RestServiceException {
		try {
			validate(result);
			DatasetAcquisition acq = datasetAcquisitionService.create(acquisition);
			return new ResponseEntity<>(acq, HttpStatus.OK);
		} catch (Exception e) {
			return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}
}

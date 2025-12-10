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

import org.apache.solr.client.solrj.SolrServerException;
import org.shanoir.ng.datasetacquisition.dto.DatasetAcquisitionDTO;
import org.shanoir.ng.datasetacquisition.dto.DatasetAcquisitionDatasetsDTO;
import org.shanoir.ng.datasetacquisition.dto.ExaminationDatasetAcquisitionDTO;
import org.shanoir.ng.datasetacquisition.dto.mapper.DatasetAcquisitionDatasetsMapper;
import org.shanoir.ng.datasetacquisition.dto.mapper.DatasetAcquisitionMapper;
import org.shanoir.ng.datasetacquisition.dto.mapper.ExaminationDatasetAcquisitionMapper;
import org.shanoir.ng.datasetacquisition.model.DatasetAcquisition;
import org.shanoir.ng.datasetacquisition.service.DatasetAcquisitionService;
import org.shanoir.ng.importer.dto.EegImportJob;
import org.shanoir.ng.importer.dto.ImportJob;
import org.shanoir.ng.importer.service.EegImporterService;
import org.shanoir.ng.importer.service.ImporterService;
import org.shanoir.ng.shared.configuration.RabbitMQConfiguration;
import org.shanoir.ng.shared.error.FieldErrorMap;
import org.shanoir.ng.shared.event.ShanoirEventService;
import org.shanoir.ng.shared.exception.EntityNotFoundException;
import org.shanoir.ng.shared.exception.ErrorDetails;
import org.shanoir.ng.shared.exception.ErrorModel;
import org.shanoir.ng.shared.exception.RestServiceException;
import org.shanoir.ng.shared.exception.ShanoirException;
import org.shanoir.ng.utils.KeycloakUtil;
import org.shanoir.ng.utils.SecurityContextUtil;
import org.shanoir.ng.utils.usermock.WithMockKeycloakUser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.AmqpRejectAndDontRequeueException;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.RequestBody;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.swagger.v3.oas.annotations.Parameter;
import jakarta.validation.Valid;

@Controller
public class DatasetAcquisitionApiController implements DatasetAcquisitionApi {

    private static final Logger LOG = LoggerFactory.getLogger(DatasetAcquisitionApiController.class);

    @Autowired
    private DatasetAcquisitionService datasetAcquisitionService;

    @Autowired
    private ImporterService importerService;

    @Autowired
    private EegImporterService eegImporterService;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private DatasetAcquisitionMapper dsAcqMapper;

    @Autowired
    private DatasetAcquisitionDatasetsMapper dsAcqDsMapper;

    @Autowired
    private ExaminationDatasetAcquisitionMapper examDsAcqMapper;

    @Autowired
    private ShanoirEventService eventService;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Override
    public ResponseEntity<Void> createNewDatasetAcquisition(
            @Parameter(description = "DatasetAcquisition to create", required = true) @Valid @RequestBody ImportJob importJob) throws RestServiceException {
        try {
            importerService.createAllDatasetAcquisition(importJob, KeycloakUtil.getTokenUserId());
        } catch (ShanoirException e) {
            ErrorModel error = new ErrorModel(HttpStatus.INTERNAL_SERVER_ERROR.value(), "Error", e);
            throw new RestServiceException(error);
        }
        importerService.cleanTempFiles(importJob.getWorkFolder());
        return new ResponseEntity<Void>(HttpStatus.OK);
    }

    @RabbitListener(queues = RabbitMQConfiguration.IMPORT_EEG_QUEUE, containerFactory = "multipleConsumersFactory")
    @RabbitHandler
    @Transactional
    public int createNewEegDatasetAcquisition(Message importJobAsString) throws IOException {
        SecurityContextUtil.initAuthenticationContext("ROLE_ADMIN");
        EegImportJob importJob = objectMapper.readValue(importJobAsString.getBody(), EegImportJob.class);
        eegImporterService.createEegDataset(importJob);
        importerService.cleanTempFiles(importJob.getWorkFolder());
        return HttpStatus.OK.value();
    }

    @RabbitListener(queues = RabbitMQConfiguration.IMPORTER_QUEUE_DATASET, containerFactory = "multipleConsumersFactory")
    @RabbitHandler
    @Transactional
    @WithMockKeycloakUser(authorities = { "ROLE_ADMIN" })
    public void createNewDatasetAcquisition(Message importJobStr) throws IOException, AmqpRejectAndDontRequeueException {
        ImportJob importJob = objectMapper.readValue(importJobStr.getBody(), ImportJob.class);
        try {
            createAllDatasetAcquisitions(importJob, importJob.getUserId());
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
            throw new AmqpRejectAndDontRequeueException(e);
            // finally is called even if we throw an exception here
        } finally {
            // if the json could not be parsed, no way to know workFolder
            // so better to throw the exception, as no possibility to clean
            importerService.cleanTempFiles(importJob.getWorkFolder());
        }
    }

    private void createAllDatasetAcquisitions(ImportJob importJob, Long userId) throws Exception {
        LOG.info("Start dataset acquisition creation of importJob: {} for user {}", importJob.getWorkFolder(), userId);
        long startTime = System.currentTimeMillis();
        importerService.createAllDatasetAcquisition(importJob, userId);
        long endTime = System.currentTimeMillis();
        long duration = endTime - startTime;
        LOG.info("Creation of dataset acquisition required " + duration + " millis.");
    }

    @Override
    public ResponseEntity<List<DatasetAcquisitionDatasetsDTO>> findByStudyCard(
              Long studyCardId) {
        List<DatasetAcquisition> daList = datasetAcquisitionService.findByStudyCard(studyCardId);
        if (daList == null || daList.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } else {
            return new ResponseEntity<>(dsAcqDsMapper.datasetAcquisitionsToDatasetAcquisitionDatasetsDTOs(daList), HttpStatus.OK);
        }
    }

    @Override
    public ResponseEntity<List<ExaminationDatasetAcquisitionDTO>> findDatasetAcquisitionByExaminationId(Long examinationId) {
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
    public ResponseEntity<List<DatasetAcquisitionDatasetsDTO>> findDatasetAcquisitionByDatasetIds(
            @Parameter(description = "ids of the datasets", required = true) @RequestBody Long[] datasetIds) {

        List<DatasetAcquisition> daList = datasetAcquisitionService.findByDatasetId(datasetIds);

        daList.sort(new Comparator<DatasetAcquisition>() {
            @Override
            public int compare(DatasetAcquisition o1, DatasetAcquisition o2) {
                return o1.getExamination() != null && o2.getExamination() != null
                        ? Long.compare(o1.getExamination().getStudyId(), o2.getExamination().getStudyId())
                        : 0;
            }
        });
        if (daList.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } else {
            return new ResponseEntity<>(dsAcqDsMapper.datasetAcquisitionsToDatasetAcquisitionDatasetsDTOs(daList), HttpStatus.OK);
        }
    }

    @Override
    public ResponseEntity<Void> deleteDatasetAcquisition(
              Long datasetAcquisitionId)
            throws RestServiceException {
        try {
            Long studyId = datasetAcquisitionService.findById(datasetAcquisitionId).getExamination().getStudyId();

            datasetAcquisitionService.deleteById(datasetAcquisitionId, null);

            rabbitTemplate.convertAndSend(RabbitMQConfiguration.RELOAD_BIDS, objectMapper.writeValueAsString(studyId));

            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } catch (EntityNotFoundException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        } catch (IOException | SolrServerException | ShanoirException e) {
            LOG.error("Error while deleting dataset acquisition: ", e);
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    public ResponseEntity<DatasetAcquisitionDTO> findDatasetAcquisitionById(
              Long datasetAcquisitionId) {

        final DatasetAcquisition datasetAcquisition = datasetAcquisitionService.findById(datasetAcquisitionId);
        if (datasetAcquisition == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(dsAcqMapper.datasetAcquisitionToDatasetAcquisitionDTO(datasetAcquisition), HttpStatus.OK);
    }

    @Override
    public ResponseEntity<Page<DatasetAcquisitionDTO>> findDatasetAcquisitions(final Pageable pageable) throws RestServiceException {
        Page<DatasetAcquisition> datasetAcquisitions = datasetAcquisitionService.findPage(pageable);
        if (datasetAcquisitions == null || datasetAcquisitions.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        }
        return new ResponseEntity<>(dsAcqMapper.datasetAcquisitionsToDatasetAcquisitionDTOs(datasetAcquisitions), HttpStatus.OK);
    }



    @Override
    public ResponseEntity<Void> updateDatasetAcquisition(
              Long datasetAcquisitionId,
            @Parameter(description = "datasetAcquisition to update", required = true) @Valid @RequestBody DatasetAcquisitionDTO datasetAcquisition,
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
}

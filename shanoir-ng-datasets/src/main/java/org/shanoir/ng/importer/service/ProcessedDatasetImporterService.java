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

package org.shanoir.ng.importer.service;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;

import org.awaitility.reflect.exception.FieldNotFoundException;
import org.dcm4che3.data.Attributes;
import org.dcm4che3.io.DicomInputStream;
import org.shanoir.ng.dataset.model.Dataset;
import org.shanoir.ng.dataset.model.DatasetExpression;
import org.shanoir.ng.dataset.model.DatasetExpressionFormat;
import org.shanoir.ng.dataset.service.DatasetService;
import org.shanoir.ng.dataset.service.DatasetUtils;
import org.shanoir.ng.datasetfile.DatasetFile;
import org.shanoir.ng.dicom.web.service.DICOMWebService;
import org.shanoir.ng.importer.dto.ProcessedDatasetImportJob;
import org.shanoir.ng.importer.strategies.datasetexpression.DicomDatasetExpressionStrategy;
import org.shanoir.ng.processing.model.DatasetProcessing;
import org.shanoir.ng.shared.event.ShanoirEvent;
import org.shanoir.ng.shared.event.ShanoirEventService;
import org.shanoir.ng.shared.event.ShanoirEventType;
import org.shanoir.ng.solr.service.SolrService;
import org.shanoir.ng.storage.StorageException;
import org.shanoir.ng.storage.StorageService;
import org.shanoir.ng.utils.KeycloakUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

@Service
@Scope("prototype")
public class ProcessedDatasetImporterService {

    private static final Logger LOG = LoggerFactory.getLogger(ProcessedDatasetImporterService.class);

    @Autowired
    private DatasetService datasetService;

    @Autowired
    private DICOMWebService dicomWebService;

    @Autowired
    private DicomDatasetExpressionStrategy dicomDatasetExpressionStrategy;

    @Autowired
    private ShanoirEventService eventService;

    @Autowired
    private SolrService solrService;

    @Autowired
    private StorageService storageService;

    private DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS");

    /**
     * Create a processed dataset dataset associated with a dataset processing.
     *
     * @param importJob
     */
    public Dataset createProcessedDataset(final ProcessedDatasetImportJob importJob) throws Exception {
        ShanoirEvent event = new ShanoirEvent(ShanoirEventType.IMPORT_DATASET_EVENT, importJob.getProcessedDatasetFilePath(), KeycloakUtil.getTokenUserId(), "Starting import...", ShanoirEvent.IN_PROGRESS, 0f, importJob.getStudyId());
        eventService.publishEvent(event);

        DatasetProcessing datasetProcessing = importJob.getDatasetProcessing();
        if (!checkProcessedDatasetImportJob(importJob, event)) {
            return null;
        }

        try {
            Dataset dataset = createDataset(importJob);

            File processedDatasetFile = new File(importJob.getProcessedDatasetFilePath());
            Attributes attributes = checkIfDICOM(processedDatasetFile);

            DatasetExpression expression = new DatasetExpression();
            expression.setDataset(dataset);
            expression.setDatasetProcessingType(datasetProcessing.getDatasetProcessingType());
            expression.setSize(Files.size(processedDatasetFile.toPath()));
            dataset.setDatasetExpressions(Collections.singletonList(expression));

            DatasetFile datasetFile = new DatasetFile();
            datasetFile.setDatasetExpression(expression);
            // Processed dataset: DICOM
            if (attributes != null) {
                expression.setDatasetExpressionFormat(DatasetExpressionFormat.DICOM);
                datasetFile.setPacs(true);
                datasetFile = dicomDatasetExpressionStrategy.setPath(attributes, datasetFile);
                dicomWebService.sendDicomFileToPacs(processedDatasetFile);
            // Processed dataset: other, e.g. NIfTI
            } else {
                expression.setDatasetExpressionFormat(DatasetExpressionFormat.NIFTI_SINGLE_FILE);
                datasetFile.setPacs(false);
                String path = saveProcessedDatasetFile(importJob, processedDatasetFile);
                datasetFile.setPath(path);
            }
            expression.setDatasetFiles(Collections.singletonList(datasetFile));

            dataset = datasetService.create(dataset);
            solrService.indexDataset(dataset.getId());

            event.setStatus(ShanoirEvent.SUCCESS);
            event.setMessage("[" + importJob.getStudyName() + " (n°" + importJob.getStudyId() + ")] "
                    + "Successfully created processed dataset [" + dataset.getId() + "] "
                    + "for subject [" + importJob.getSubjectName() + "]");
            event.setProgress(1f);
            eventService.publishEvent(event);
            return dataset;
        } catch (Exception e) {
            LOG.error("Error while importing processed dataset: ", e);
            event.setStatus(ShanoirEvent.ERROR);
            event.setMessage("Unexpected error during the import: " + e.getClass() + " : " + e.getMessage() + ", please contact an administrator.");
            event.setProgress(-1f);
            eventService.publishEvent(event);
            throw e;
        }
    }

    private Attributes checkIfDICOM(File processedDatasetFile) throws FieldNotFoundException {
        if (processedDatasetFile.exists()) {
            // We pass here by using DicomInputStream,
            // in case processed dataset file does not end with .dcm
            try (DicomInputStream dIS = new DicomInputStream(processedDatasetFile)) {
                return dIS.readDataset(); // we close InputStream and use Attributes
            } catch (IOException e) {
                // We ignore the exception here: if not DICOM, we assume other format
            }
        } else {
            LOG.error("Processed dataset file not existing: {}", processedDatasetFile.getAbsolutePath());
            throw new FieldNotFoundException("Processed dataset file not existing.");
        }
        return null;
    }

    /**
     * Check ProcessedDatasetImportJob.
     *
     * @param job
     * @param event
     * @return
     */
    private boolean checkProcessedDatasetImportJob(ProcessedDatasetImportJob job, ShanoirEvent event) {
        if (job.getDatasetProcessing() == null) {
            event.setStatus(ShanoirEvent.ERROR);
            event.setMessage("Dataset processing missing.");
            event.setProgress(-1f);
            eventService.publishEvent(event);
            return false;
        }
        if (job.getDatasetProcessing().getInputDatasets() == null
                || job.getDatasetProcessing().getInputDatasets().isEmpty()) {
            event.setStatus(ShanoirEvent.ERROR);
            event.setMessage("Processing input dataset(s) missing.");
            event.setProgress(-1f);
            eventService.publishEvent(event);
            return false;
        }
        if (job.getStudyId() == null) {
            event.setStatus(ShanoirEvent.ERROR);
            event.setMessage("Study missing.");
            event.setProgress(-1f);
            eventService.publishEvent(event);
            return false;
        }
        for (Dataset input : job.getDatasetProcessing().getInputDatasets()) {
            Long studyId = datasetService.getStudyId(input);
            if (studyId != null && !studyId.equals(job.getStudyId())) {
                event.setStatus(ShanoirEvent.ERROR);
                event.setMessage("Study from input dataset [" + input.getId() + "] not the same as [" + studyId + "]");
                event.setProgress(-1f);
                eventService.publishEvent(event);
                return false;
            }
        }
        return true;
    }

    private Dataset createDataset(ProcessedDatasetImportJob job) {
        Dataset dataset = DatasetUtils.buildDatasetFromType(job.getDatasetType());
        dataset.getOriginMetadata().setProcessedDatasetType(job.getProcessedDatasetType());
        dataset.getOriginMetadata().setName(job.getProcessedDatasetName());
        dataset.setStudyId(job.getStudyId());
        dataset.setSubjectId(job.getSubjectId());
        dataset.setCreationDate(LocalDate.now());
        dataset.setUpdatedMetadata(dataset.getOriginMetadata());
        dataset.setDatasetProcessing(job.getDatasetProcessing());
        job.getDatasetProcessing().addOutputDataset(dataset);
        return dataset;
    }

    private String saveProcessedDatasetFile(ProcessedDatasetImportJob job, File processedDatasetFile)
            throws IOException {
        final String timestamp = formatter.format(LocalDateTime.now());
        final String fileName = processedDatasetFile.getName();
        final String contentType = Files.probeContentType(processedDatasetFile.toPath());
        final long size = processedDatasetFile.length();
        try (InputStream inputStream = new FileInputStream(processedDatasetFile)) {
            return storageService.storeProcessedData(
                    job.getSubjectId(),
                    timestamp,
                    fileName,
                    inputStream,
                    contentType,
                    size);
        } catch (StorageException e) {
            LOG.error("StorageException storing Processed Dataset file for subject {}", job.getSubjectName(), e);
            throw new IOException("Failed to store processed dataset file", e);
        } catch (IOException e) {
            LOG.error("IOException reading Processed Dataset file for subject {}", job.getSubjectName(), e);
            throw e;
        }
    }

}

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

package org.shanoir.ng.dataset.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.commons.io.FileUtils;
import org.joda.time.DateTime;
import org.shanoir.ng.dataset.model.Dataset;
import org.shanoir.ng.dataset.model.DatasetExpressionFormat;
import org.shanoir.ng.download.DatasetDownloadError;
import org.shanoir.ng.download.WADODownloaderService;
import org.shanoir.ng.examination.model.Examination;
import org.shanoir.ng.shared.configuration.RabbitMQConfiguration;
import org.shanoir.ng.shared.event.ShanoirEvent;
import org.shanoir.ng.shared.event.ShanoirEventService;
import org.shanoir.ng.shared.event.ShanoirEventType;
import org.shanoir.ng.shared.exception.ErrorModel;
import org.shanoir.ng.shared.exception.RestServiceException;
import org.shanoir.ng.shared.model.Study;
import org.shanoir.ng.shared.model.Subject;
import org.shanoir.ng.shared.repository.StudyRepository;
import org.shanoir.ng.shared.repository.SubjectRepository;
import org.shanoir.ng.utils.DatasetFileUtils;
import org.shanoir.ng.utils.KeycloakUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@Service
public class DatasetDownloaderServiceImpl {

    protected static final String FAILURES_TXT = "failures.txt";

    protected static final String NII = "nii";

    protected static final String DCM = "dcm";

    protected static final String ZIP = ".zip";

    protected static final Logger LOG = LoggerFactory.getLogger(DatasetDownloaderServiceImpl.class);

    protected static final String JSON_RESULT_FILENAME = "ERRORS.json";

    protected static final Long DEFAULT_NIFTI_CONVERTER_ID = 6L;

    protected static final String GZIP_EXTENSION = ".gz";

    protected static final String NII_GZ = ".nii.gz";

    protected static final String CONVERSION_FAILED_ERROR_MSG = "Nifti conversion failed, you may try to select another one.";

    @Autowired
    protected DatasetService datasetService;

    @Autowired
    protected WADODownloaderService downloader;

    @Autowired
    protected SubjectRepository subjectRepository;

    @Autowired
    protected StudyRepository studyRepository;

    @Autowired
    protected RabbitTemplate rabbitTemplate;

    @Autowired
    protected ShanoirEventService eventService;

    @Autowired
    protected ObjectMapper objectMapper;

    @PostConstruct
    protected void initialize() {
        // Set timeout to 5mn (consider nifti reconversion can take some time)
        this.rabbitTemplate.setReplyTimeout(300000);
    }

    public void massiveDownload(String outputFormat, List<Dataset> datasets, HttpServletResponse response, boolean withManifest, Long converterId) throws RestServiceException {
        massiveDownload(outputFormat, datasets, response, withManifest, converterId, false);
    }

    public void massiveDownload(String outputFormat, List<Dataset> datasets, HttpServletResponse response, boolean withManifest, Long converterId, Boolean withShanoirId) throws RestServiceException {
        Map<Long, List<String>> filesByAcquisitionId = new HashMap<>();
        Map<Long, DatasetDownloadError> downloadResults = new HashMap<>();
        Map<Long, String> datasetDownloadName = getDatasetDownloadName(datasets);

        // Prepare the HTTP response for a zip download
        response.setContentType("application/zip");
        response.setHeader("Content-Disposition", "attachment;filename=" + getFileName(datasets));

        try (ZipOutputStream zipOutputStream = new ZipOutputStream(response.getOutputStream())) {
            for (Dataset dataset : datasets) {
                // Prepare folder structure
                String subjectName = getSubjectName(dataset).replace(File.separator, "_");
                String studyName = studyRepository.findById(dataset.getStudyId())
                        .map(Study::getName)
                        .orElse("Unknown_study");

                // Determine dataset file path if multiple datasets are downloaded
                String datasetFilePath = datasets.size() != 1
                        ? getDatasetFilepath(dataset, studyName, subjectName, withShanoirId)
                        : null;

                // Download the dataset into the zip
                manageDatasetDownload(
                        dataset,
                        downloadResults,
                        zipOutputStream,
                        subjectName,
                        datasetFilePath,
                        outputFormat,
                        withManifest,
                        filesByAcquisitionId,
                        converterId,
                        datasetDownloadName.get(dataset.getId())
                );
            }

            // Write manifest if any files exist
            if (!filesByAcquisitionId.isEmpty())
                DatasetFileUtils.writeManifestForExport(zipOutputStream, filesByAcquisitionId);

            // Write download errors into a JSON file in the zip
            if (!downloadResults.isEmpty()) {
                ZipEntry zipEntry = new ZipEntry(JSON_RESULT_FILENAME);
                zipEntry.setTime(System.currentTimeMillis());
                zipOutputStream.putNextEntry(zipEntry);

                String errorsJson = objectMapper.writeValueAsString(downloadResults);
                zipOutputStream.write(errorsJson.getBytes());
                zipOutputStream.closeEntry();
            }

            // Publish download event
            String ids = String.join(",", datasets.stream()
                    .map(dataset -> dataset.getId().toString())
                    .collect(Collectors.toList()));

            ShanoirEvent event = new ShanoirEvent(
                    ShanoirEventType.DOWNLOAD_DATASET_EVENT,
                    ids,
                    KeycloakUtil.getTokenUserId(),
                    ids + "." + outputFormat,
                    ShanoirEvent.IN_PROGRESS
            );
            event.setStatus(ShanoirEvent.SUCCESS);
            eventService.publishEvent(event);
        } catch (Exception e) {
            throw new RestServiceException(e, new ErrorModel(
                                HttpStatus.UNPROCESSABLE_ENTITY.value(),
                                "Unexpected error while downloading dataset files: " + e.getMessage()
                        ));  // Pass the original exception as the cause
        }
    }

    protected Map<Long, String> getDatasetDownloadName(List<Dataset> datasets) {
        HashMap<Long, String> datasetDownloadName = new HashMap<>();
        int count = 0;
        for (Dataset dataset : datasets) {
            String datasetName = dataset.getName();
            if (datasetDownloadName.containsValue(datasetName)) {
                if (datasetName.contains(".")) {
                    datasetDownloadName.put(dataset.getId(), datasetName.replaceFirst("\\.", "_" + count + "."));
                } else {
                    datasetDownloadName.put(dataset.getId(), datasetName + "_" + count);
                }
                count++;
            } else {
                datasetDownloadName.put(dataset.getId(), dataset.getName());
            }
        }
        return datasetDownloadName;
    }

    protected void manageDatasetDownload(Dataset dataset, Map<Long, DatasetDownloadError> downloadResults, ZipOutputStream zipOutputStream, String subjectName, String datasetFilePath, String outputFormat, boolean withManifest, Map<Long, List<String>> filesByAcquisitionId, Long converterId, String datasetDownloadName) throws IOException, RestServiceException {
        if (!dataset.isDownloadable()) {
            downloadResults.put(dataset.getId(), new DatasetDownloadError("Dataset not downloadable", DatasetDownloadError.ERROR));
            return;
        }

        DatasetDownloadError downloadResult = new DatasetDownloadError();
        downloadResults.put(dataset.getId(), downloadResult);
        List<URL> pathURLs = new ArrayList<>();
        DatasetExpressionFormat format = dataset.getDatasetExpressions().get(0).getDatasetExpressionFormat();

        if (format == DatasetExpressionFormat.DICOM && Objects.equals(DCM, outputFormat)) { // Download DICOM dataset
            DatasetFileUtils.getDatasetFilePathURLs(dataset, pathURLs, format, downloadResult);
            List<String> files = downloader.downloadDicomFilesForURLsAsZip(pathURLs, zipOutputStream, subjectName, dataset, datasetFilePath, downloadResult);
            if (withManifest) {
                filesByAcquisitionId.putIfAbsent(dataset.getDatasetAcquisition().getId(), new ArrayList<>());
                filesByAcquisitionId.get(dataset.getDatasetAcquisition().getId()).addAll(files);
            }
        } else if (format == DatasetExpressionFormat.DICOM && Objects.equals(NII, outputFormat)) { // Convert dataset from DICOM to NIfTI and download it
            File tempDir = null;
            try {
                Long converterToUse = (converterId != null) ? converterId : DEFAULT_NIFTI_CONVERTER_ID;
                tempDir = convertToNifti(dataset, pathURLs, converterToUse, downloadResult, subjectName);
                DatasetFileUtils.copyFilesForDownload(pathURLs, zipOutputStream, dataset, subjectName, true, datasetFilePath, datasetDownloadName);
            } finally {
                LOG.info("Deleting temporary conversion folder [{}]", tempDir.getAbsolutePath());
                FileUtils.deleteQuietly(tempDir);
            }
        } else { // Download the other types
            DatasetFileUtils.getDatasetFilePathURLs(dataset, pathURLs, format, downloadResult);
            DatasetFileUtils.copyFilesForDownload(pathURLs, zipOutputStream, dataset, subjectName, true, datasetFilePath, datasetDownloadName);
        }
        if (downloadResult.getStatus() == null)
            downloadResults.remove(dataset.getId());
    }

    protected File convertToNifti(Dataset dataset, List<URL> pathURLs, Long converterId, DatasetDownloadError downloadResult, String subjectName) throws RestServiceException, IOException {
        File userDir = DatasetFileUtils.getUserImportDir("/tmp");
        String tmpFilePath = userDir + File.separator + dataset.getId() + "_nii";
        File sourceFolder = new File(tmpFilePath + "-" + UUID.randomUUID());

        // 1. Get DICOM URLs
        List<URL> dicomUrls = new ArrayList<>();
        DatasetFileUtils.getDatasetFilePathURLs(dataset, dicomUrls, DatasetExpressionFormat.DICOM, downloadResult);

        // 2. Prepare working folder and download
        sourceFolder.mkdirs();
        downloader.downloadDicomFilesForURLs(dicomUrls, sourceFolder, subjectName, dataset, downloadResult);

        // 3. Convert via RabbitMQ
        boolean result = Boolean.TRUE.equals(rabbitTemplate.convertSendAndReceive(
                RabbitMQConfiguration.NIFTI_CONVERSION_QUEUE,
                converterId + ";" + sourceFolder.getAbsolutePath()
        ));

        if (!result)
            downloadResult.update("Conversion to NIfTI failed.", DatasetDownloadError.ERROR);

        // 4. Collect converted files
        File resultFolder = new File(sourceFolder, "result");
        File[] files = resultFolder.listFiles();
        if (files == null || files.length == 0)
            downloadResult.update("No NIfTI files found after conversion.", DatasetDownloadError.ERROR);

        for (File file : files)
            if (!file.isDirectory())
                pathURLs.add(file.toURI().toURL());

        return sourceFolder;
    }

    protected String getSubjectName(Dataset dataset) {
        String subjectName = "unknownSubject";
        if (dataset.getSubjectId() != null) {
            Optional<Subject> subjectOpt = subjectRepository.findById(dataset.getSubjectId());
            if (subjectOpt.isPresent()) {
                subjectName = subjectOpt.get().getName();
            }
        }
        return subjectName;
    }

    protected String getFileName(List<Dataset> datasets) {
        SimpleDateFormat fileDateformatter = new SimpleDateFormat("yyyyMMddHHmmss");
        if (datasets != null && datasets.size() == 1) {
            String datasetName = getDatasetFileName(datasets.get(0));
            return "Dataset_" + datasetName + "_" + fileDateformatter.format(new DateTime().toDate()) + ZIP;
        } else {
            return "Datasets_" + fileDateformatter.format(new DateTime().toDate()) + ZIP;
        }
    }

    protected String getDatasetFileName(Dataset dataset) {
        // Only one dataset -> the logic for one dataset is used
        String subjectName = getSubjectName(dataset);

        String datasetName = subjectName + "_" + dataset.getId() + "_" + dataset.getName();
        if (dataset.getUpdatedMetadata() != null && dataset.getUpdatedMetadata().getComment() != null) {
            datasetName += "_" + dataset.getUpdatedMetadata().getComment();
        }
        // Replace all forbidden characters.
        datasetName = datasetName.replaceAll("[^a-zA-Z0-9\\.\\-]", "_");
        return datasetName;
    }

    protected String getDatasetFilepath(Dataset dataset, String studyName, String subjectName, Boolean withShanoirId) {
        Examination exam = datasetService.getExamination(dataset);

        String datasetFilePath = studyName + "_" + subjectName + "_Exam-" + exam.getId() + (withShanoirId ? "_shanoirId-" + dataset.getId() : "");
        if (exam.getComment() != null) {
            datasetFilePath += "-" + exam.getComment();
        }
        datasetFilePath = datasetFilePath.replaceAll("[^a-zA-Z0-9_\\-]", "_");
        if (datasetFilePath.length() > 255) {
            datasetFilePath = datasetFilePath.substring(0, 254);
        }
        return datasetFilePath;
    }
}

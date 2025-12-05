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

package org.shanoir.ng.processing.service;

import com.fasterxml.jackson.databind.JsonNode;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.apache.commons.collections4.ListUtils;
import org.apache.solr.common.util.Pair;
import org.assertj.core.util.Lists;
import org.shanoir.ng.dataset.model.Dataset;
import org.shanoir.ng.dataset.service.DatasetDownloaderServiceImpl;
import org.shanoir.ng.datasetacquisition.model.DatasetAcquisition;
import org.shanoir.ng.download.DatasetDownloadError;
import org.shanoir.ng.examination.model.Examination;
import org.shanoir.ng.processing.model.DatasetProcessing;
import org.shanoir.ng.processing.repository.DatasetProcessingRepository;
import org.shanoir.ng.shared.event.ShanoirEvent;
import org.shanoir.ng.shared.event.ShanoirEventType;
import org.shanoir.ng.shared.exception.ErrorModel;
import org.shanoir.ng.shared.exception.RestServiceException;
import org.shanoir.ng.utils.DatasetFileUtils;
import org.shanoir.ng.utils.KeycloakUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@Service
public class ProcessingDownloaderServiceImpl extends DatasetDownloaderServiceImpl implements ProcessingDownloaderService {    /** Number of downloadable datasets. */

    @Autowired
    private DatasetProcessingRepository datasetProcessingRepository;

    @Autowired
    private DatasetProcessingServiceImpl datasetProcessingService;

    @PersistenceContext
    private EntityManager em;

    public void massiveDownload(List<DatasetProcessing> processingList, boolean resultOnly, String format, HttpServletResponse response, boolean withManifest, Long converterId) throws RestServiceException {
        manageResultOnly(processingList, resultOnly);

        response.setContentType("application/zip");
        response.setHeader("Content-Disposition", "attachment;filename=Processings_" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")));
        Map<Long, DatasetDownloadError> downloadResults = new HashMap<Long, DatasetDownloadError>();
        Map<Long, List<String>> filesByAcquisitionId = new HashMap<>();

        try (ZipOutputStream zipOutputStream = new ZipOutputStream(response.getOutputStream())) {
            manageProcessingsDownload(processingList, downloadResults, zipOutputStream, format, withManifest, filesByAcquisitionId, converterId);

            String ids = Stream.concat(
                            processingList.stream().flatMap(p -> p.getInputDatasets().stream()),
                            processingList.stream().flatMap(p -> p.getOutputDatasets().stream())
                    )
                    .map(dataset -> dataset.getId().toString())
                    .collect(Collectors.joining(","));
            ShanoirEvent event = new ShanoirEvent(
                    ShanoirEventType.DOWNLOAD_DATASET_EVENT,
                    ids,
                    KeycloakUtil.getTokenUserId(),
                    ids + "." + format,
                    ShanoirEvent.IN_PROGRESS
            );
            event.setStatus(ShanoirEvent.SUCCESS);
            eventService.publishEvent(event);
        } catch (Exception e) {
            response.setContentType(null);
            LOG.error("Unexpected error while downloading dataset files.", e);
            throw new RestServiceException(
                    new ErrorModel(HttpStatus.UNPROCESSABLE_ENTITY.value(),
                            "Unexpected error while downloading dataset files"));
        }
    }

    public void complexMassiveDownload(@Valid JsonNode jsonRequest) throws Exception {
        if (!jsonRequest.has("data_to_extract")) {
            throw new Exception("There are no extraction defined.");
        }
        JsonNode extractions = jsonRequest.get("data_to_extract");
        List<Long> extractedIds = new ArrayList<>();

        for (JsonNode extraction : extractions) {
            extractedIds.addAll(getDatasetIdsFromJsonFilters(extraction));
        }

        int count = 1;
        List<List<Long>> partitions = ListUtils.partition(extractedIds, 3000);
        int maxCount = partitions.size();

        for (List<Long> partition : partitions) {
            ShanoirEvent newEvent = new ShanoirEvent(
                    ShanoirEventType.MASSIVE_OUTPUTS_DOWNLOAD,
                    null,
                    KeycloakUtil.getTokenUserId(),
                    "Output extraction, sorted by " + StreamSupport.stream(jsonRequest.get("sorting").spliterator(), false)
                            .map(JsonNode::asText).collect(Collectors.joining("/")) + ", part " + count  + " / " + maxCount + " : " + partition.stream().map(String::valueOf).collect(Collectors.joining(",")),
                    ShanoirEvent.SUCCESS,
                    1f,
                    null);
            eventService.publishEvent(newEvent);
            count++;
        }
    }

    public void massiveDownloadByExaminations(List<Examination> examinationList, String processingComment, boolean resultOnly, String format, HttpServletResponse response, boolean withManifest, Long converterId) throws RestServiceException {
        List<Long> processingIdsList = datasetProcessingRepository.findAllIdsByExaminationIds(examinationList.stream().map(Examination::getId).toList());
        List<DatasetProcessing> processingList = datasetProcessingService.findAllById(processingIdsList);
        if (!Objects.isNull(processingComment)) {
            processingList = processingList.stream().filter(it -> Objects.equals(it.getComment(), processingComment)).toList();
        }
        massiveDownload(processingList, resultOnly, format, response, withManifest, converterId);
    }

    protected void manageProcessingsDownload(List<DatasetProcessing> processingList, Map<Long, DatasetDownloadError> downloadResults, ZipOutputStream zipOutputStream, String format, boolean withManifest, Map<Long, List<String>> filesByAcquisitionId, Long converterId) throws RestServiceException, IOException {
        for (DatasetProcessing processing : processingList) {
            String processingFilePath = getExecFilepath(processing.getId(), getExaminationDatas(processing.getInputDatasets()));
            String subjectName = getProcessingSubject(processing);
            List<Dataset> inputs = processing.getInputDatasets();
            List<Dataset> outputs = processing.getOutputDatasets();

            for (Dataset dataset : inputs) {
                Map<String, List<String>> datasetDownloadNameListPerPath = new HashMap<>();

                manageDatasetDownload(dataset, downloadResults, zipOutputStream, subjectName, processingFilePath + "/" + shapeForPath(dataset.getName()), format, withManifest, filesByAcquisitionId, converterId, datasetDownloadNameListPerPath);
            }

            for (Dataset dataset : outputs) {
                Map<String, List<String>> datasetDownloadNameListPerPath = new HashMap<>();

                manageDatasetDownload(dataset, downloadResults, zipOutputStream, subjectName, processingFilePath + "/output", format, withManifest, filesByAcquisitionId, converterId, datasetDownloadNameListPerPath);
            }
        }
        if (!filesByAcquisitionId.isEmpty())
            DatasetFileUtils.writeManifestForExport(zipOutputStream, filesByAcquisitionId);

        // Write errors to the file
        if (!downloadResults.isEmpty()) {
            ZipEntry zipEntry = new ZipEntry(JSON_RESULT_FILENAME);
            zipEntry.setTime(System.currentTimeMillis());
            zipOutputStream.putNextEntry(zipEntry);
            zipOutputStream.write(objectMapper.writeValueAsString(downloadResults).getBytes());
            zipOutputStream.closeEntry();
        }
    }

    protected void manageResultOnly(List<DatasetProcessing> processingList, boolean resultOnly) {
        if (resultOnly) {
            processingList.forEach(it -> {
                it.setInputDatasets(new ArrayList<>());
            });
        }
    }

    protected String getProcessingSubject(DatasetProcessing processing) {
        Examination exam = null;
        for (Dataset dataset : processing.getInputDatasets()) {
            exam = Optional.ofNullable(dataset)
                    .map(Dataset::getDatasetAcquisition)
                    .map(DatasetAcquisition::getExamination)
                    .orElse(null);
            if (!Objects.isNull(exam)) {
                return exam.getSubject().getName();
            }
        }
        return "noSubject";
    }

    protected Pair<Long, String> getExaminationDatas(List<Dataset> inputs) {
        Examination exam = null;
        for (Dataset dataset : inputs) {
            exam = Optional.ofNullable(dataset)
                    .map(Dataset::getDatasetAcquisition)
                    .map(DatasetAcquisition::getExamination)
                    .orElse(null);
            if (!Objects.isNull(exam)) {
                return new Pair<>(exam.getId(), exam.getComment());
            }
        }

        return new Pair<>(0L, "");
    }

    private String getExecFilepath(Long processingId, Pair<Long, String> examDatas) {

        String execFilePath = "processing_" + processingId + "_exam_" + examDatas.first();
        if (!Objects.equals(examDatas.second(), "")) {
            execFilePath += "_" + examDatas.second();
        }
        return shapeForPath(execFilePath);
    }

    private String shapeForPath(String path) {
        path = path.replaceAll("[^a-zA-Z0-9_\\-]", "_");
        if (path.length() > 255) {
            path = path.substring(0, 254);
        }
        return path;
    }

    @SuppressWarnings("unchecked")
    protected List<Long> getDatasetIdsFromJsonFilters(JsonNode extraction) throws Exception {
        if (!extraction.has("filter") || !extraction.get("filter").isArray()) {
            throw new Exception("The extraction " + extraction.get("extraction_identifier") + "has no filters array.");
        } else if (!extraction.get("filter").isArray() || extraction.get("filter").isEmpty()) {
            throw new Exception("There is no extraction filter defined for the extraction " + extraction.get("extraction_identifier") + ".");
        } else if (StreamSupport.stream(extraction.get("filter").spliterator(), false).map(it -> it.get("type").asText()).noneMatch(filterType -> filterType.contains("processing"))) {
            throw new Exception("There is no specific processing filter defined for the extraction " + extraction.get("extraction_identifier") + ".");
        }

        Map<String, List<String>> queryFilters = new HashMap<>();
        String query = "SELECT DISTINCT dataset.id FROM dataset dataset "
                + "JOIN dataset_metadata AS metadata ON metadata.id = dataset.updated_metadata_id "
                + "JOIN dataset_processing AS processing ON dataset.dataset_processing_id = processing.id "
                + "LEFT JOIN execution_monitoring AS monitoring ON monitoring.id = processing.parent_id "
                + "JOIN input_of_dataset_processing AS input_link ON processing.id = input_link.processing_id "
                + "JOIN dataset AS input_dataset ON input_link.dataset_id = input_dataset.id "
                + "JOIN dataset_acquisition AS acquisition ON input_dataset.dataset_acquisition_id = acquisition.id "
                + "JOIN examination AS examination ON acquisition.examination_id = examination.id "
                + "JOIN subject ON dataset.subject_id = subject.id "
                + "JOIN study ON examination.study_id = study.id "
                + "WHERE ";

        for (JsonNode jsonFilter : extraction.get("filter")) {
            String queryFilter = correctFilterType(jsonFilter.get("type").asText());
            String filter = "";
            JsonNode value = jsonFilter.get("value");

            if (value.isBoolean()) {
                filter += " IS " + value.asBoolean();
            } else {
                String valueStr = value.asText();
                if (queryFilter.endsWith("date")) {
                    filter += " " + valueStr + " ";
                } else if (valueStr.replaceAll(",", "").trim().matches("\\d+")) {
                    filter += " IN (" + valueStr + ")";
                } else {
                    filter += " REGEXP '" + valueStr.replaceAll(",", "|") + "'";
                }
            }
            if (!queryFilters.containsKey(queryFilter)) {
                queryFilters.put(queryFilter, new ArrayList<>());
            }
            queryFilters.get(queryFilter).add(queryFilter + filter);
        }

        query += queryFilters.values().stream()
                .map(list -> "(" + String.join(" OR ", list) + ")")
                .collect(Collectors.joining(" AND "))
                + ";";

        List<Long> idList;
        try {
            idList =  em.createNativeQuery(query).getResultList();
            if (!extraction.has("input") || !extraction.get("input").asBoolean()) {
                return idList;
            }
        } catch (Exception e) {
            LOG.error("There is an issue with the filters of the extraction " + extraction.get("extraction_identifier") + ". Please check that they are correct, otherwise take contact with the dev team.");
            throw e;
        }

        String inputQuery = "SELECT DISTINCT input_link.dataset_id FROM dataset as dataset "
                + "JOIN dataset_processing AS processing ON dataset.dataset_processing_id = processing.id "
                + "JOIN input_of_dataset_processing AS input_link ON processing.id = input_link.processing_id "
                + "WHERE dataset.id IN ("
                + idList.stream().map(Object::toString).collect(Collectors.joining(","))
                + ");";

        try {
            idList.addAll(em.createNativeQuery(inputQuery).getResultList());
            return idList.stream().distinct().collect(Collectors.toList());
        } catch (Exception e) {
            LOG.error("There is an issue with the filters of the extraction " + extraction.get("extraction_identifier") + ". Please check that they are correct, otherwise take contact with the dev team.");
            throw e;
        }
    }

    //Some types need a tiny change. For example, dataset.name does not exist, metadata.name is required here.
    protected String correctFilterType(String type) {
        String table = type.split("\\.")[0];
        String field = type.split("\\.")[1];
        return switch (table) {
            case "dataset" -> Lists.list("name").contains(field) ? "metadata." + field : type;
            case "processing" -> Lists.list("name", "status", "pipeline_identifier").contains(field) ? "monitoring." + field : type;
            default -> type;
        };
    }
}

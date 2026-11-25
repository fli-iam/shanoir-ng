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

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import org.apache.commons.collections4.ListUtils;
import org.apache.solr.common.util.Pair;
import org.shanoir.ng.dataset.model.Dataset;
import org.shanoir.ng.dataset.repository.DatasetRepository;
import org.shanoir.ng.dataset.security.DatasetSecurityService;
import org.shanoir.ng.dataset.service.DatasetDownloaderServiceImpl;
import org.shanoir.ng.datasetacquisition.model.DatasetAcquisition;
import org.shanoir.ng.download.ArchiveWriter;
import org.shanoir.ng.download.DatasetDownloadError;
import org.shanoir.ng.download.ZipWriter;
import org.shanoir.ng.examination.model.Examination;
import org.shanoir.ng.processing.model.DatasetProcessing;
import org.shanoir.ng.processing.repository.DatasetProcessingRepository;
import org.shanoir.ng.shared.event.ShanoirEvent;
import org.shanoir.ng.shared.event.ShanoirEventType;
import org.shanoir.ng.shared.exception.EntityNotFoundException;
import org.shanoir.ng.shared.exception.ErrorModel;
import org.shanoir.ng.shared.exception.RestServiceException;
import org.shanoir.ng.shared.security.rights.StudyUserRight;
import org.shanoir.ng.utils.DatasetFileUtils;
import org.shanoir.ng.utils.KeycloakUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import tools.jackson.databind.JsonNode;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;

@Service
public class ProcessingDownloaderServiceImpl extends DatasetDownloaderServiceImpl implements ProcessingDownloaderService {    /** Number of downloadable datasets. */

    /** The tables of the complex download query a filter can use. */
    private static final Set<String> FILTER_TABLES = Set.of("dataset", "metadata", "processing", "monitoring",
            "input_link", "input_dataset", "acquisition", "examination", "subject", "study");

    /** A column name: no quote, space, parenthesis or operator. */
    private static final Pattern FILTER_COLUMN = Pattern.compile("[A-Za-z_][A-Za-z0-9_]*");

    private static final String FILTER_DATE = "'?(\\d{4}-\\d{2}-\\d{2}(?:[ T]\\d{2}:\\d{2}(?::\\d{2})?)?)'?";

    private static final Pattern DATE_COMPARISON = Pattern.compile("\\s*(<=|>=|<>|!=|=|<|>)\\s*" + FILTER_DATE + "\\s*");

    private static final Pattern DATE_BETWEEN = Pattern.compile(
            "\\s*BETWEEN\\s+" + FILTER_DATE + "\\s+AND\\s+" + FILTER_DATE + "\\s*", Pattern.CASE_INSENSITIVE);

    private static final Pattern ID_LIST = Pattern.compile("\\s*\\d+(\\s*,\\s*\\d+)*\\s*");

    @Autowired
    private DatasetProcessingRepository datasetProcessingRepository;

    @Autowired
    private DatasetRepository datasetRepository;

    @Autowired
    private DatasetSecurityService datasetSecurityService;

    @PersistenceContext
    private EntityManager em;

    public void massiveDownloadByProcessingIds(List<Long> processingIds, boolean resultOnly, String format, HttpServletResponse response, boolean withManifest, Long converterId) throws RestServiceException {
        massiveDownload(datasetProcessingRepository.findByIdsWithInputsAndOutputsAndDatasetFiles(processingIds), resultOnly, format, response, withManifest, converterId);
    }

    public void massiveDownload(List<DatasetProcessing> processingList, boolean resultOnly, String format, HttpServletResponse response, boolean withManifest, Long converterId) throws RestServiceException {
        response.setContentType("application/zip");
        response.setHeader("Content-Disposition", "attachment;filename=\"Processings_" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")) + "\"");
        Map<Long, DatasetDownloadError> downloadResults = new HashMap<Long, DatasetDownloadError>();
        Map<Long, List<String>> filesByAcquisitionId = new HashMap<>();

        try (ArchiveWriter archiveWriter = new ZipWriter(response.getOutputStream())) {
            manageProcessingsDownload(processingList, downloadResults, archiveWriter, format, withManifest, filesByAcquisitionId, converterId, resultOnly);

            String ids = Stream.concat(
                            resultOnly ? Stream.empty() : processingList.stream().flatMap(p -> p.getInputDatasets().stream()),
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
        }  catch (IOException e) {
            throw new RuntimeException("Impossible to open a TAR writer, aborting download", e);
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
            throw new IllegalArgumentException("There are no extraction defined.");
        }
        JsonNode extractions = jsonRequest.get("data_to_extract");
        List<Long> extractedIds = new ArrayList<>();

        for (JsonNode extraction : extractions) {
            extractedIds.addAll(getDatasetIdsFromJsonFilters(extraction));
        }
        // the filters can match any dataset of the instance: only the ones the user can download are listed
        extractedIds = datasetSecurityService.filterDatasetIds(
                extractedIds.stream().distinct().collect(Collectors.toList()), StudyUserRight.CAN_DOWNLOAD.name());

        int count = 1;
        List<List<Long>> partitions = ListUtils.partition(extractedIds, 3000);
        int maxCount = partitions.size();

        for (List<Long> partition : partitions) {
            ShanoirEvent newEvent = new ShanoirEvent(
                    ShanoirEventType.MASSIVE_OUTPUTS_DOWNLOAD,
                    null,
                    KeycloakUtil.getTokenUserId(),
                    "Output extraction" + Optional.ofNullable(jsonRequest.get("sorting"))
                            .filter(node -> !node.isNull() && !node.isEmpty())
                            .map(sorting -> ", sorted by " + StreamSupport.stream(sorting.spliterator(), false)
                                    .map(JsonNode::asString).collect(Collectors.joining("/")))
                            .orElse("") + ", part " + count  + " / " + maxCount + " : " + partition.stream().map(String::valueOf).collect(Collectors.joining(",")),
                    ShanoirEvent.SUCCESS,
                    1f,
                    null);
            eventService.publishEvent(newEvent);
            count++;
        }
    }

    public void massiveDownloadByExaminations(List<Examination> examinationList, String processingComment, boolean resultOnly, String format, HttpServletResponse response, boolean withManifest, Long converterId) throws RestServiceException {
        List<Long> processingIdsList = datasetProcessingRepository.findAllIdsByExaminationIds(examinationList.stream().map(Examination::getId).toList());
        List<DatasetProcessing> processingList = datasetProcessingRepository.findByIdsWithInputsAndOutputsAndDatasetFiles(processingIdsList);
        if (!Objects.isNull(processingComment)) {
            processingList = processingList.stream().filter(it -> Objects.equals(it.getComment(), processingComment)).toList();
        }
        massiveDownload(processingList, resultOnly, format, response, withManifest, converterId);
    }

    protected void manageProcessingsDownload(List<DatasetProcessing> processingList, Map<Long, DatasetDownloadError> downloadResults, ArchiveWriter archive, String format, boolean withManifest, Map<Long, List<String>> filesByAcquisitionId, Long converterId, boolean resultOnly) throws Exception {
        for (DatasetProcessing processing : processingList) {
            String processingFilePath = getExecFilepath(processing.getId(), getExaminationDatas(processing.getInputDatasets()));
            String subjectName = getProcessingSubject(processing);
            List<Dataset> inputs = resultOnly ? Collections.emptyList() : processing.getInputDatasets();
            List<Dataset> outputs = processing.getOutputDatasets();

            for (Dataset dataset : inputs) {
                Map<String, List<String>> datasetDownloadNameListPerPath = new HashMap<>();

                manageDatasetDownload(dataset, downloadResults, archive, subjectName, processingFilePath + "/" + shapeForPath(dataset.getName()), format, withManifest, filesByAcquisitionId, converterId, datasetDownloadNameListPerPath);
            }

            for (Dataset dataset : outputs) {
                Map<String, List<String>> datasetDownloadNameListPerPath = new HashMap<>();

                manageDatasetDownload(dataset, downloadResults, archive, subjectName, processingFilePath + "/output", format, withManifest, filesByAcquisitionId, converterId, datasetDownloadNameListPerPath);
            }
        }
        if (!filesByAcquisitionId.isEmpty())
            DatasetFileUtils.writeManifestForExport(archive, filesByAcquisitionId);

        // Write errors to the file
        if (!downloadResults.isEmpty()) {
            archive.addEntry(JSON_RESULT_FILENAME, objectMapper.writeValueAsBytes(downloadResults));
        }
    }

    protected String getProcessingSubject(DatasetProcessing processing) throws EntityNotFoundException {
        Examination exam = null;
        for (Dataset dataset : processing.getInputDatasets()) {
            Dataset loadedDataset = datasetRepository.findByIdWithProcessingAncestorsAndExaminationAndMetadata(dataset.getId()).orElseThrow(() -> new EntityNotFoundException(DatasetProcessing.class, processing.getId()));
            exam = Optional.ofNullable(loadedDataset)
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

    /**
     * Resolves the filters of one extraction into dataset ids. The filters come from the client: their types can only
     * be columns of the tables of the query, and their values are bound as parameters, never written in the SQL.
     *
     * @param extraction the extraction, with its "filter" array of {type: "table.column", value}
     * @return the ids of the matching datasets, and of their processing inputs if "input" is true
     * @throws IllegalArgumentException if a filter is invalid
     */
    protected List<Long> getDatasetIdsFromJsonFilters(JsonNode extraction) {
        JsonNode filters = extraction.get("filter");
        if (filters == null || !filters.isArray()) {
            throw new IllegalArgumentException("The extraction " + extraction.get("extraction_identifier") + " has no filters array.");
        } else if (filters.isEmpty()) {
            throw new IllegalArgumentException("There is no extraction filter defined for the extraction " + extraction.get("extraction_identifier") + ".");
        }
        Map<String, List<String>> queryFilters = new LinkedHashMap<>();
        Map<String, Object> parameters = new HashMap<>();
        boolean processingOrDatasetFilter = false;
        for (JsonNode jsonFilter : filters) {
            String type = jsonFilter.hasNonNull("type") ? jsonFilter.get("type").asText() : null;
            String column = filterColumn(type);
            processingOrDatasetFilter |= type.contains("processing") || type.contains("dataset");
            queryFilters.computeIfAbsent(column, c -> new ArrayList<>())
                    .add(filterCondition(column, jsonFilter.get("value"), parameters));
        }
        if (!processingOrDatasetFilter) {
            throw new IllegalArgumentException("There is no specific processing filter defined for the extraction " + extraction.get("extraction_identifier") + ".");
        }

        String query = "SELECT DISTINCT dataset.id FROM dataset dataset "
                + "LEFT JOIN dataset_metadata AS metadata ON metadata.id = dataset.updated_metadata_id "
                + "LEFT JOIN dataset_processing AS processing ON dataset.dataset_processing_id = processing.id "
                + "LEFT JOIN execution_monitoring AS monitoring ON monitoring.id = processing.parent_id "
                + "LEFT JOIN input_of_dataset_processing AS input_link ON processing.id = input_link.processing_id "
                + "LEFT JOIN dataset AS input_dataset ON input_link.dataset_id = input_dataset.id "
                + "LEFT JOIN dataset_acquisition AS acquisition ON input_dataset.dataset_acquisition_id = acquisition.id "
                + "LEFT JOIN examination AS examination ON acquisition.examination_id = examination.id "
                + "LEFT JOIN subject ON dataset.subject_id = subject.id "
                + "LEFT JOIN study ON examination.study_id = study.id "
                + "WHERE "
                + queryFilters.values().stream()
                        .map(list -> "(" + String.join(" OR ", list) + ")")
                        .collect(Collectors.joining(" AND "));

        List<Long> idList;
        try {
            Query nativeQuery = em.createNativeQuery(query);
            parameters.forEach(nativeQuery::setParameter);
            idList = ids(nativeQuery.getResultList());
            if (!extraction.has("input") || !extraction.get("input").asBoolean() || idList.isEmpty()) {
                return idList;
            }
        } catch (RuntimeException e) {
            LOG.error("There is an issue with the filters of the extraction " + extraction.get("extraction_identifier") + ". Please check that they are correct, otherwise take contact with the dev team.");
            throw e;
        }

        String inputQuery = "SELECT DISTINCT input_link.dataset_id FROM dataset as dataset "
                + "JOIN dataset_processing AS processing ON dataset.dataset_processing_id = processing.id "
                + "JOIN input_of_dataset_processing AS input_link ON processing.id = input_link.processing_id "
                + "WHERE dataset.id IN (:ids)";

        try {
            Query nativeInputQuery = em.createNativeQuery(inputQuery);
            nativeInputQuery.setParameter("ids", List.copyOf(idList));
            idList.addAll(ids(nativeInputQuery.getResultList()));
            return idList.stream().distinct().collect(Collectors.toList());
        } catch (RuntimeException e) {
            LOG.error("There is an issue with the filters of the extraction " + extraction.get("extraction_identifier") + ". Please check that they are correct, otherwise take contact with the dev team.");
            throw e;
        }
    }

    private static List<Long> ids(List<?> results) {
        return results.stream().map(id -> ((Number) id).longValue()).collect(Collectors.toCollection(ArrayList::new));
    }

    /**
     * @param type the filter type sent by the client, "table.column"
     * @return the column of the query to filter on
     * @throws IllegalArgumentException if the table is not one of the query or the column is not a plain name
     */
    protected String filterColumn(String type) {
        String[] parts = type == null ? new String[0] : type.split("\\.", -1);
        if (parts.length != 2 || !FILTER_TABLES.contains(parts[0]) || !FILTER_COLUMN.matcher(parts[1]).matches()) {
            throw new IllegalArgumentException("Invalid filter type: " + type + ". A filter type is table.column, the table being one of "
                    + FILTER_TABLES.stream().sorted().collect(Collectors.joining(", ")) + ".");
        }
        return correctFilterType(parts[0], parts[1]);
    }

    /**
     * @param column the column, checked by filterColumn
     * @param value the filter value sent by the client
     * @param parameters the query parameters, where the value is added
     * @return the SQL condition, where the value is a named parameter
     * @throws IllegalArgumentException if the value is missing, or is not a valid date filter for a date column
     */
    protected String filterCondition(String column, JsonNode value, Map<String, Object> parameters) {
        if (value == null || value.isNull()) {
            throw new IllegalArgumentException("The filter " + column + " has no value.");
        }
        if (value.isBoolean()) {
            return column + (value.asBoolean() ? " IS TRUE" : " IS FALSE");
        }
        String text = value.asText();
        if (column.endsWith("date")) {
            Matcher between = DATE_BETWEEN.matcher(text);
            if (between.matches()) {
                return column + " BETWEEN :" + parameter(parameters, between.group(1)) + " AND :" + parameter(parameters, between.group(2));
            }
            Matcher comparison = DATE_COMPARISON.matcher(text);
            if (comparison.matches()) {
                // the operator is one of the pattern, never the client's text
                return column + " " + comparison.group(1) + " :" + parameter(parameters, comparison.group(2));
            }
            throw new IllegalArgumentException("Invalid date filter for " + column + ": " + text
                    + ". Expected an operator (=, <, >, <=, >=, <>, !=) and a date (yyyy-MM-dd, optionally with HH:mm[:ss]),"
                    + " or BETWEEN date AND date.");
        }
        if (ID_LIST.matcher(text).matches()) {
            List<Long> ids = Stream.of(text.split(",")).map(String::trim).map(Long::valueOf).toList();
            return column + " IN (:" + parameter(parameters, ids) + ")";
        }
        return column + " REGEXP :" + parameter(parameters, text.replaceAll(",", "|"));
    }

    private static String parameter(Map<String, Object> parameters, Object value) {
        String name = "filter" + parameters.size();
        parameters.put(name, value);
        return name;
    }

    //Some types need a tiny change. For example, dataset.name does not exist, metadata.name is required here.
    protected String correctFilterType(String table, String field) {
        return switch (table) {
            case "dataset" -> "name".equals(field) ? "metadata." + field : table + "." + field;
            case "processing" -> List.of("name", "status", "pipeline_identifier").contains(field) ? "monitoring." + field : table + "." + field;
            default -> table + "." + field;
        };
    }
}

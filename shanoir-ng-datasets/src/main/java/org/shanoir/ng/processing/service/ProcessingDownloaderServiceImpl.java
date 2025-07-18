package org.shanoir.ng.processing.service;

import com.fasterxml.jackson.databind.JsonNode;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.apache.solr.common.util.Pair;
import org.assertj.core.util.Lists;
import org.shanoir.ng.dataset.model.Dataset;
import org.shanoir.ng.dataset.model.DatasetExpressionFormat;
import org.shanoir.ng.dataset.repository.DatasetRepository;
import org.shanoir.ng.dataset.service.DatasetDownloaderServiceImpl;
import org.shanoir.ng.datasetacquisition.model.DatasetAcquisition;
import org.shanoir.ng.download.DatasetDownloadError;
import org.shanoir.ng.download.NiftiConverter;
import org.shanoir.ng.examination.model.Examination;
import org.shanoir.ng.processing.model.DatasetProcessing;
import org.shanoir.ng.processing.repository.DatasetProcessingRepository;
import org.shanoir.ng.shared.event.ShanoirEvent;
import org.shanoir.ng.shared.event.ShanoirEventType;
import org.shanoir.ng.shared.exception.ErrorModel;
import org.shanoir.ng.shared.exception.RestServiceException;
import org.shanoir.ng.utils.DatasetFileUtils;
import org.shanoir.ng.utils.KeycloakUtil;
import org.shanoir.ng.vip.executionMonitoring.model.ExecutionMonitoring;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URL;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@Service
public class ProcessingDownloaderServiceImpl extends DatasetDownloaderServiceImpl implements ProcessingDownloaderService {

    /** Number of downloadable datasets. */
    private static final int DATASET_LIMIT = 500;

    @Autowired
    private DatasetProcessingRepository datasetProcessingRepository;

    @Autowired
    private DatasetProcessingServiceImpl datasetProcessingService;

    @Autowired
    private DatasetRepository datasetRepository;

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

            String ids = String.join(",", Stream.concat(processingList.stream().map(DatasetProcessing::getInputDatasets), processingList.stream().map(DatasetProcessing::getOutputDatasets)).map(dataset -> ((Dataset) dataset).getId().toString()).collect(Collectors.toList()));
            ShanoirEvent event = new ShanoirEvent(ShanoirEventType.DOWNLOAD_DATASET_EVENT, ids,
                    KeycloakUtil.getTokenUserId(), ids + "." + format, ShanoirEvent.IN_PROGRESS);
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

    public void complexMassiveDownload(@Valid JsonNode jsonRequest, HttpServletResponse response) throws Exception{
        Map<Integer, List<Long>> datasetIdsPerExtraction = new HashMap<>();
        Map<Integer, Boolean> inputPerExtraction = new HashMap<>();
        List<Integer> extractionIdList = new ArrayList<>();

        if(!jsonRequest.has("data_to_extract")){
            throw new Exception("There are no extraction defined.");
        }
        JsonNode extractions = jsonRequest.get("data_to_extract");

        for (JsonNode extraction : extractions) {
            if(!extraction.has("extraction_identifier")){
                throw new Exception("An extraction definition is missing an extraction identifier.");
            }

            Integer extractionId = extraction.get("extraction_identifier").asInt();
            extractionIdList.add(extractionId);
            inputPerExtraction.put(extractionId, extraction.get("input").asBoolean(false));
            datasetIdsPerExtraction.put(extractionId, getDatasetIdsFromJsonFilters(extraction));
            LOG.info("Results for extraction {} query are :{}", extractionId, datasetIdsPerExtraction.get(extractionId));
        }

        response.setContentType("application/zip");
        response.setHeader("Content-Disposition", "attachment;filename=Processings_" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")));
        try (ZipOutputStream zipOutputStream = new ZipOutputStream(response.getOutputStream())) {
            downloadDatasetsWithJsonSorting(datasetIdsPerExtraction, inputPerExtraction, extractionIdList, jsonRequest, zipOutputStream);
        } catch (Exception e) {
            LOG.error("An error occured while generating the zip.", e);
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
            Map<Long, String> inputsDownloadName = getDatasetDownloadName(inputs);
            Map<Long, String> outputsDownloadName = getDatasetDownloadName(outputs);

            for (Dataset dataset : inputs) {
                manageDatasetDownload(dataset, downloadResults, zipOutputStream, subjectName, processingFilePath  + "/" + shapeForPath(dataset.getName()), format, withManifest, filesByAcquisitionId, converterId, inputsDownloadName.get(dataset.getId()));
            }
            for (Dataset dataset : outputs) {
                manageDatasetDownload(dataset, downloadResults, zipOutputStream, subjectName, processingFilePath  + "/output", format, withManifest, filesByAcquisitionId, converterId, outputsDownloadName.get(dataset.getId()));
            }
        }
        if (!filesByAcquisitionId.isEmpty()) {
            DatasetFileUtils.writeManifestForExport(zipOutputStream, filesByAcquisitionId);
        }

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
                it.setOutputDatasets(it.getOutputDatasets().stream().filter(file -> Objects.equals(file.getName(), "result.yaml")).toList());
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

    protected String getExecFilepath(Long processingId, Pair<Long, String> examDatas) {
        String execFilePath = "processing_" + processingId + "_exam_" + examDatas.first();
        if (!Objects.equals(examDatas.second(), "")) {
            execFilePath += "_" + examDatas.second();
        }
        return shapeForPath(execFilePath);
    }

    protected String shapeForPath(String path) {
        path = path.replaceAll("[^a-zA-Z0-9_\\-]", "_");
        if (path.length() > 255) {
            path = path.substring(0, 254);
        }
        return path;
    }

    protected void downloadDatasetsWithJsonSorting(Map<Integer, List<Long>> datasetIdsPerExtraction, Map<Integer, Boolean> inputPerExtraction, List<Integer> extractionIdList, @Valid JsonNode jsonRequest, ZipOutputStream zipOutputStream) {
        List<String> sortingType = StreamSupport.stream(jsonRequest.get("sorting").spliterator(), false).map(JsonNode::asText).toList();
        List<Long> processingWithInputsAlreadyDownloaded = new ArrayList<>();
        Map<Long, DatasetDownloadError> downloadErrors = new HashMap<Long, DatasetDownloadError>();

        int converterId = 0;
        if(jsonRequest.has("converter")){
            converterId = NiftiConverter.getId(jsonRequest.get("converter").asText());
        }

        for(Integer extractionId : extractionIdList){
            List<Long> datasetIds = datasetIdsPerExtraction.get(extractionId);

            for (Long datasetId : datasetIds){
                prepareZipBuilding(datasetId, inputPerExtraction.get(extractionId), zipOutputStream, sortingType, converterId, processingWithInputsAlreadyDownloaded, downloadErrors);
            }
        }

        if (!downloadErrors.isEmpty()) {
            try{
                ZipEntry zipEntry = new ZipEntry(JSON_RESULT_FILENAME);
                zipEntry.setTime(System.currentTimeMillis());
                zipOutputStream.putNextEntry(zipEntry);
                zipOutputStream.write(objectMapper.writeValueAsString(downloadErrors).getBytes());
                zipOutputStream.closeEntry();
            } catch (Exception ignored){
                LOG.error("Errors happened while computing complex download, but they could not be noted into the zip archive.");
            }
        }
    }

    protected void prepareZipBuilding(Long datasetId, Boolean withInput, ZipOutputStream zipOutputStream, List<String> sortingType, Integer convertedId, List<Long> processingWithInputsAlreadyDownloaded, Map<Long, DatasetDownloadError> downloadErrors) {
        Dataset dataset = datasetRepository.findById(datasetId).get();
        DatasetProcessing processing = datasetProcessingRepository.findById(dataset.getDatasetProcessing().getId()).get();
        String datasetPath = getDatasetFilepath(dataset, sortingType, ((ExecutionMonitoring) processing.getParent()).getName());

        zipDataset(dataset, false, zipOutputStream, convertedId, datasetPath, downloadErrors);
        if(withInput && !processingWithInputsAlreadyDownloaded.contains(processing.getId())) {
            for(Dataset input : processing.getInputDatasets()){
                zipDataset(input, true, zipOutputStream, convertedId, datasetPath, downloadErrors);
            }
            processingWithInputsAlreadyDownloaded.add(processing.getId());
        }
    }

    protected void zipDataset(Dataset dataset, Boolean isInput, ZipOutputStream zipOutputStream, Integer converterId, String filePath, Map<Long, DatasetDownloadError> downloadErrors) {
        DatasetDownloadError downloadError = new DatasetDownloadError();
        downloadErrors.put(dataset.getId(), downloadError);
        List<URL> pathURLs = new ArrayList<>();

        try{
            if (!isInput) {
                DatasetFileUtils.getDatasetFilePathURLs(dataset, pathURLs, DatasetExpressionFormat.NIFTI_SINGLE_FILE, downloadError);
                DatasetFileUtils.copyNiftiFilesForURLs(pathURLs, zipOutputStream, dataset, "", true, filePath, null);
            } else if (Objects.equals(converterId, 0)) {
                DatasetFileUtils.getDatasetFilePathURLs(dataset, pathURLs, DatasetExpressionFormat.DICOM, downloadError);
                downloader.downloadDicomFilesForURLsAsZip(pathURLs, zipOutputStream, "", dataset, filePath, downloadError);
            } else {
                reconvertToNifti("", Long.valueOf(converterId), dataset, pathURLs, downloadError, "", zipOutputStream);
            }
            if (downloadError.getStatus() == null) {
                downloadErrors.remove(dataset.getId());
            }
        } catch (RestServiceException | IOException e) {
            LOG.error("An error occured while zipping dataset {} for complex processing download.", dataset.getId(), e);        }
    }

    protected String getDatasetFilepath(Dataset dataset, List<String> sortingType, String processingName) {
        Examination examination = dataset.getDatasetProcessing().getInputDatasets().getFirst().getDatasetAcquisition().getExamination();

        String filePath = "";
        if(sortingType.contains("study")) {
            filePath = filePath.concat(examination.getStudy().getName() + "/");
        }
        if(sortingType.contains("subject")) {
            filePath = filePath.concat(examination.getSubject().getName() + "/");
        }
        if(sortingType.contains("examination")) {
            filePath = filePath.concat(examination.getComment() + "/");
        }

        filePath = filePath.concat(processingName + "/");

        filePath = filePath.replaceAll("[^a-zA-Z0-9_\\-]", "_");
        if (filePath.length() > 255) {
            filePath = filePath.substring(0, 254);
        }
        return filePath;
    }

    @SuppressWarnings("unchecked")
    protected List<Long> getDatasetIdsFromJsonFilters(JsonNode extraction) throws Exception {
        if (!extraction.has("filter") || !extraction.get("filter").isArray()) {
            throw new Exception("The extraction " + extraction.get("extraction_identifier") + "has no filters array.");
        } else if(extraction.get("filter").isEmpty()) {
            throw new Exception("There is no extraction filter defined for the extraction " + extraction.get("extraction_identifier") + "." );
        } else if (StreamSupport.stream(extraction.get("filter").spliterator(), false).map(it -> it.get("type").asText()).noneMatch(filterType -> filterType.contains("processing"))){
            throw new Exception("There is no processing filter defined for the extraction " + extraction.get("extraction_identifier") + "." );
        }

        List<String> queryFilters = new ArrayList<>();
        String query = "SELECT dataset.id FROM dataset dataset " +
                "JOIN dataset_metadata AS metadata ON metadata.id = dataset.updated_metadata_id " +
                "JOIN dataset_processing AS processing ON dataset.dataset_processing_id = processing.id " +
                "JOIN execution_monitoring AS monitoring ON monitoring.id = processing.parent_id " +
                "JOIN input_of_dataset_processing AS input_link ON processing.id = input_link.processing_id " +
                "JOIN dataset AS input_dataset ON input_link.dataset_id = input_dataset.id " +
                "JOIN dataset_acquisition AS acquisition ON input_dataset.dataset_acquisition_id = acquisition.id " +
                "JOIN examination AS examination ON acquisition.examination_id = examination.id " +
                "JOIN subject ON dataset.subject_id = subject.id " +
                "JOIN study ON examination.study_id = study.id " +
                "WHERE ";

        for(JsonNode filter : extraction.get("filter")) {
            String queryFilter = correctFilterType(filter.get("type").asText());
            JsonNode value = filter.get("value");

            if(value.isBoolean()){
                queryFilter += " IS " + value.asBoolean();
            } else {
                String valueStr = value.asText();
                if(queryFilter.endsWith("date")) {
                    queryFilter += "'" + valueStr + "'";
                } else if(valueStr.replaceAll(",","").trim().matches("\\d+")){
                    queryFilter += " IN (" + valueStr + ")";
                } else {
                    queryFilter += " REGEXP '" + valueStr.replaceAll(",", "|") + "'";
                }
            }
            queryFilters.add(queryFilter);
        }
        query += String.join(" AND ", queryFilters) + ";";

        try{
            return em.createNativeQuery(query).getResultList();
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
            case "dataset" -> Lists.list("name", "pipeline_identifier").contains(field) ? "metadata." + field : type;
            case "processing" -> Lists.list("name", "comment").contains(field) ? "monitoring." + field : type;
            default -> type;
        };
    }
}
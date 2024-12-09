package org.shanoir.ng.processing.service;

import jakarta.servlet.http.HttpServletResponse;
import org.apache.solr.common.util.Pair;
import org.shanoir.ng.dataset.modality.BidsDataset;
import org.shanoir.ng.dataset.modality.EegDataset;
import org.shanoir.ng.dataset.model.Dataset;
import org.shanoir.ng.dataset.model.DatasetExpressionFormat;
import org.shanoir.ng.dataset.service.DatasetDownloaderServiceImpl;
import org.shanoir.ng.datasetacquisition.model.DatasetAcquisition;
import org.shanoir.ng.download.DatasetDownloadError;
import org.shanoir.ng.download.WADODownloaderService;
import org.shanoir.ng.examination.model.Examination;
import org.shanoir.ng.processing.model.DatasetProcessing;
import org.shanoir.ng.processing.model.DatasetProcessingType;
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
import java.net.URL;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@Service
public class ProcessingDownloaderServiceImpl extends DatasetDownloaderServiceImpl{

    /** Number of downloadable datasets. */
    private static final int DATASET_LIMIT = 500;

    @Autowired
    private WADODownloaderService downloader;
    @Autowired
    private DatasetProcessingRepository datasetProcessingRepository;
    @Autowired
    private DatasetProcessingServiceImpl datasetProcessingService;

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

    private void manageProcessingsDownload(List<DatasetProcessing> processingList, Map<Long, DatasetDownloadError> downloadResults, ZipOutputStream zipOutputStream, String format, boolean withManifest, Map<Long, List<String>> filesByAcquisitionId, Long converterId) throws RestServiceException, IOException {
        for (DatasetProcessing processing : processingList) {
            String processingFilePath = getExecFilepath(processing, getExaminationDatas(processing.getInputDatasets()));
            String subjectName = getProcessingSubject(processing);
            for (Dataset dataset : processing.getInputDatasets()) {
                manageDatasetDownload(dataset, downloadResults, zipOutputStream, subjectName, processingFilePath  + "/" + shapeForPath(dataset.getName()), format, withManifest, filesByAcquisitionId, converterId);
            }
            for (Dataset dataset : processing.getOutputDatasets()) {
                manageDatasetDownload(dataset, downloadResults, zipOutputStream, subjectName, processingFilePath  + "/output", format, withManifest, filesByAcquisitionId, converterId);
            }
        }
        if(!filesByAcquisitionId.isEmpty()){
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

    public void massiveDownloadByExaminations(List<Examination> examinationList, String processingComment, boolean resultOnly, String format, HttpServletResponse response, boolean withManifest, Long converterId) throws RestServiceException {
        List<Long> processingIdsList = datasetProcessingRepository.findAllIdsByExaminationIds(examinationList.stream().map(Examination::getId).toList());
        List<DatasetProcessing> processingList = datasetProcessingService.findAllById(processingIdsList);
        if(!Objects.isNull(processingComment)){
            processingList = processingList.stream().filter(it -> Objects.equals(it.getComment(), processingComment)).toList();
        };
        massiveDownload(processingList, resultOnly, format, response, withManifest, converterId);
    }

    private void manageResultOnly(List<DatasetProcessing> processingList, boolean resultOnly) {
        if(resultOnly){
            processingList.forEach(it -> {it.setOutputDatasets(it.getOutputDatasets().stream().filter(file -> Objects.equals(file.getName(), "result.yaml")).toList()); it.setInputDatasets(new ArrayList<>());});
        }
    }

    private String getProcessingSubject(DatasetProcessing processing) {
        Examination exam = null;
        for (Dataset dataset : processing.getInputDatasets()){
            exam = Optional.ofNullable(dataset)
                    .map(Dataset::getDatasetAcquisition)
                    .map(DatasetAcquisition::getExamination)
                    .orElse(null);
            if (!Objects.isNull(exam)){
                return exam.getSubject().getName();
            }
        }
        return "noSubject";
    }

    private Pair<Long, String> getExaminationDatas(List<Dataset> inputs) {
        Examination exam = null;
        for (Dataset dataset : inputs){
            exam = Optional.ofNullable(dataset)
                    .map(Dataset::getDatasetAcquisition)
                    .map(DatasetAcquisition::getExamination)
                    .orElse(null);
            if (!Objects.isNull(exam)){
                return new Pair<>(exam.getId(), exam.getComment());
            }
        }

        return new Pair<>(0L, "");
    }

    private String getExecFilepath(DatasetProcessing processing, Pair<Long, String> examDatas) {
        String execFilePath = "";
        if(Objects.equals(processing.getComment(), "comete_moelle/0.1")){
            if(!processing.getOutputDatasets().stream().filter(it -> Objects.equals(it.getName(), "results.yaml")).toList().isEmpty()) {
                execFilePath = "result/";
            } else if(!processing.getOutputDatasets().stream().filter(it -> Objects.equals(it.getName(), "error.yaml")).toList().isEmpty()) {
                execFilePath = "error/";
            } else {
                execFilePath = "unknown";
            }

        }
        execFilePath += "processing_" + processing.getId() +  "_exam_" + examDatas.first();
        if (Objects.nonNull(examDatas.second()) && !Objects.equals(examDatas.second(), "")) {
            execFilePath += "_" + examDatas.second();
        }
        return shapeForPath(execFilePath);
    }

    private String shapeForPath(String path){
        path = path.replaceAll("[^a-zA-Z0-9_]", "_").replaceAll("_+$", "").replaceAll("_+", "_");
        if (path.length() > 255) {
            path = path.substring(0, 254);
        }
        return path;
    }
}

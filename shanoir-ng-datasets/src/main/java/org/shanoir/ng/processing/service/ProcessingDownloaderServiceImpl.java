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
            String processingFilePath = getExecFilepath(processing.getId(), getExaminationDatas(processing.getInputDatasets()));
            String subjectName = getProcessingSubject(processing);
            for (Dataset dataset : Stream.concat(processing.getInputDatasets().stream(), processing.getOutputDatasets().stream()).toList()) {
                manageDatasetDownload(dataset, downloadResults, zipOutputStream, subjectName, processingFilePath, format, withManifest, filesByAcquisitionId, converterId);

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

    private void manageDatasetDownload(Dataset dataset, Map<Long, DatasetDownloadError> downloadResults, ZipOutputStream zipOutputStream, String subjectName, String processingFilePath, String format, boolean withManifest, Map<Long, List<String>> filesByAcquisitionId, Long converterId) throws IOException, RestServiceException {
        if (!dataset.isDownloadable()) {
            downloadResults.put(dataset.getId(), new DatasetDownloadError("Dataset not downloadable", DatasetDownloadError.ERROR));
            return;
        }
        DatasetDownloadError downloadResult = new DatasetDownloadError();
        downloadResults.put(dataset.getId(), downloadResult);

        List<URL> pathURLs = new ArrayList<>();

        if (dataset.getDatasetProcessing() != null) {
            // DOWNLOAD PROCESSED DATASET
            DatasetFileUtils.getDatasetFilePathURLs(dataset, pathURLs, DatasetExpressionFormat.NIFTI_SINGLE_FILE, downloadResult);
            DatasetFileUtils.copyNiftiFilesForURLs(pathURLs, zipOutputStream, dataset, subjectName, true, processingFilePath);
        } else if (dataset instanceof EegDataset) {
            // DOWNLOAD EEG
            DatasetFileUtils.getDatasetFilePathURLs(dataset, pathURLs, DatasetExpressionFormat.EEG, downloadResult);
            DatasetFileUtils.copyNiftiFilesForURLs(pathURLs, zipOutputStream, dataset, subjectName, false, processingFilePath);
        } else if (dataset instanceof BidsDataset) {
            // DOWNLOAD BIDS
            DatasetFileUtils.getDatasetFilePathURLs(dataset, pathURLs, DatasetExpressionFormat.BIDS, downloadResult);
            DatasetFileUtils.copyNiftiFilesForURLs(pathURLs, zipOutputStream, dataset, subjectName, true, processingFilePath);
            // Manage errors here
        } else if (Objects.equals("dcm", format)) {
            // DOWNLOAD DICOM
            DatasetFileUtils.getDatasetFilePathURLs(dataset, pathURLs, DatasetExpressionFormat.DICOM, downloadResult);
            List<String> files = downloader.downloadDicomFilesForURLsAsZip(pathURLs, zipOutputStream, subjectName, dataset, processingFilePath  + "/" + shapeForPath(dataset.getName()), downloadResult);
            if (withManifest) {
                filesByAcquisitionId.putIfAbsent(dataset.getDatasetAcquisition().getId(), new ArrayList<>());
                filesByAcquisitionId.get(dataset.getDatasetAcquisition().getId()).addAll(files);
            }
        } else if (Objects.equals("nii", format)) {
            // Check if we have a specific converter -> nifti reconversion
            if (converterId != null) {
                reconvertToNifti(format, converterId, dataset, pathURLs, downloadResult, subjectName, zipOutputStream);
            } else {
                // Check that we have existing nifti, otherwise reconvert using dcm2niix by default.
                DatasetFileUtils.getDatasetFilePathURLs(dataset, pathURLs, DatasetExpressionFormat.NIFTI_SINGLE_FILE, downloadResult);
                if (!pathURLs.isEmpty()) {
                    List<String> files = DatasetFileUtils.copyNiftiFilesForURLs(pathURLs, zipOutputStream, dataset, subjectName, false, processingFilePath + "/" + shapeForPath(dataset.getName()));
                } else {
                    // Reconvert using dcm2niix by default.
                    reconvertToNifti(format, DEFAULT_NIFTI_CONVERTER_ID, dataset, pathURLs, downloadResult, subjectName, zipOutputStream);
                }
            }
        } else {
            downloadResult.update("Dataset format was not adapted to dataset download choosen", DatasetDownloadError.ERROR);
        }

        if (downloadResult.getStatus() == null) {
            downloadResults.remove(dataset.getId());
        }
    }

    public void massiveDownloadByExamination(List<Examination> examinationList, boolean resultOnly, String format, HttpServletResponse response, boolean withManifest, Long converterId) throws RestServiceException {
        List<Long> processingIdsList = datasetProcessingRepository.findAllIdsByExaminationIds(examinationList.stream().map(Examination::getId).toList());
        List<DatasetProcessing> processingList = datasetProcessingService.findAllById(processingIdsList).stream().filter(it -> Objects.equals(it.getDatasetProcessingType(), DatasetProcessingType.SEGMENTATION)).toList();
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

    private String getExecFilepath(Long processingId, Pair<Long, String> examDatas) {

        String execFilePath = "processing_" + processingId +  "_exam_" + examDatas.first();
        if (!Objects.equals(examDatas.second(), "")) {
            execFilePath += "_" + examDatas.second();
        }
        return shapeForPath(execFilePath);
    }

    private String shapeForPath(String path){
        path = path.replaceAll("[^a-zA-Z0-9_\\-]", "_");
        if (path.length() > 255) {
            path = path.substring(0, 254);
        }
        return path;
    }
}

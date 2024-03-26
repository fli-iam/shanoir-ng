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

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.commons.collections4.CollectionUtils;
import org.joda.time.DateTime;
import org.shanoir.ng.dataset.modality.BidsDataset;
import org.shanoir.ng.dataset.modality.EegDataset;
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
import org.shanoir.ng.shared.model.Subject;
import org.shanoir.ng.shared.repository.StudyRepository;
import org.shanoir.ng.shared.repository.SubjectRepository;
import org.shanoir.ng.utils.DatasetFileUtils;
import org.shanoir.ng.utils.KeycloakUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.util.StreamUtils;
import com.fasterxml.jackson.databind.ObjectMapper;

@Service
public class DatasetDownloaderServiceImpl {

	private static final String FAILURES_TXT = "failures.txt";

	private static final String NII = "nii";

	private static final String DCM = "dcm";

	private static final String ZIP = ".zip";

	private static final Logger LOG = LoggerFactory.getLogger(DatasetDownloaderServiceImpl.class);

	private static final String JSON_RESULT_FILENAME = "ERRORS.json";

	@Autowired
	DatasetService datasetService;

	@Autowired
	private WADODownloaderService downloader;

	@Autowired
	private SubjectRepository subjectRepository;

	@Autowired
	private StudyRepository studyRepository;

	@Autowired
	private RabbitTemplate rabbitTemplate;

	@Autowired
	ShanoirEventService eventService;

	SimpleDateFormat fileDateformatter = new SimpleDateFormat("yyyyMMddHHmmss");

	@Autowired
	private ObjectMapper objectMapper;

	@PostConstruct
	private void initialize() {
		// Set timeout to 5mn (consider nifti reconversion can take some time)
		this.rabbitTemplate.setReplyTimeout(300000);
	}

	public void massiveDownload(String format, List<Dataset> datasets, HttpServletResponse response, boolean withManifest, Long converterId) throws RestServiceException {
		Map<Long, List<String>> filesByAcquisitionId = new HashMap<>();

		response.setContentType("application/zip");
		response.setHeader("Content-Disposition",
				"attachment;filename=" + getFileName(datasets));
		SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmssSSS");
		Map<Long, DatasetDownloadError> downloadResults = new HashMap<Long, DatasetDownloadError>();

		try (ZipOutputStream zipOutputStream = new ZipOutputStream(response.getOutputStream())) {
			for (Dataset dataset : datasets) {
				if (!dataset.isDownloadable()) {
					downloadResults.put(dataset.getId(), new DatasetDownloadError("Dataset not downloadable", DatasetDownloadError.ERROR));
					continue;
				}
				DatasetDownloadError downloadResult = new DatasetDownloadError();
				downloadResults.put(dataset.getId(), downloadResult);

				// Create a new folder organized by subject / examination
				String subjectName = getSubjectName(dataset);
				if (subjectName.contains(File.separator)) {
					subjectName = subjectName.replaceAll(File.separator, "_");
				}
				String studyName = studyRepository.findById(dataset.getStudyId()).orElse(null).getName();

				String datasetFilePath = null;
				if (datasets.size() != 1) {
					datasetFilePath = getDatasetFilepath(dataset, studyName, subjectName);
				}

				List<URL> pathURLs = new ArrayList<>();

				if (dataset instanceof EegDataset) {
					// DOWNLOAD EEG
					DatasetFileUtils.getDatasetFilePathURLs(dataset, pathURLs, DatasetExpressionFormat.EEG, downloadResult);
					DatasetFileUtils.copyNiftiFilesForURLs(pathURLs, zipOutputStream, dataset, subjectName, false, datasetFilePath);
				} else if (dataset instanceof BidsDataset) {
					// DOWNLOAD BIDS
					DatasetFileUtils.getDatasetFilePathURLs(dataset, pathURLs, DatasetExpressionFormat.BIDS, downloadResult);
					DatasetFileUtils.copyNiftiFilesForURLs(pathURLs, zipOutputStream, dataset, subjectName, true, datasetFilePath);
					// Manage errors here
				} else if (dataset.getDatasetProcessing() != null) {
					// DOWNLOAD PROCESSED DATASET
					DatasetFileUtils.getDatasetFilePathURLs(dataset, pathURLs, DatasetExpressionFormat.NIFTI_SINGLE_FILE, downloadResult);
					DatasetFileUtils.copyNiftiFilesForURLs(pathURLs, zipOutputStream, dataset, subjectName, true, datasetFilePath);
				} else if (DCM.equals(format)) {
					// DOWNLOAD DICOM
					DatasetFileUtils.getDatasetFilePathURLs(dataset, pathURLs, DatasetExpressionFormat.DICOM, downloadResult);
					List<String> files = downloader.downloadDicomFilesForURLsAsZip(pathURLs, zipOutputStream, subjectName, dataset, datasetFilePath, downloadResult);
					if (withManifest) {
						filesByAcquisitionId.putIfAbsent(dataset.getDatasetAcquisition().getId(), new ArrayList<>());
						filesByAcquisitionId.get(dataset.getDatasetAcquisition().getId()).addAll(files);
					}
				} else if (NII.equals(format)) {
					// Check if we want a specific converter -> nifti reconversion
					if (converterId != null) {
						File userDir = DatasetFileUtils.getUserImportDir("/tmp");
						String tmpFilePath = userDir + File.separator + dataset.getId() + "_" + format;
						File workFolder = new File(tmpFilePath + "-" + formatter.format(new DateTime().toDate()));

						DatasetFileUtils.getDatasetFilePathURLs(dataset, pathURLs, DatasetExpressionFormat.DICOM, downloadResult);

						// Create temporary workfolder with dicom files, to be able to convert them
						workFolder.mkdirs();

						downloader.downloadDicomFilesForURLs(pathURLs, workFolder, subjectName, dataset, downloadResult);

						// Convert them, sending to import microservice
						boolean result = (boolean) this.rabbitTemplate.convertSendAndReceive(RabbitMQConfiguration.NIFTI_CONVERSION_QUEUE, converterId + ";" + workFolder.getAbsolutePath());
						if (!result) {
							response.setContentType(null);
							throw new RestServiceException(
									new ErrorModel(HttpStatus.UNPROCESSABLE_ENTITY.value(), "Nifti conversion failed", null));
						}
						workFolder = new File(workFolder.getAbsolutePath() + File.separator + "result");
						List<String> files = new ArrayList<>();
						for (File res : workFolder.listFiles()) {

							if (!res.isDirectory()) {
								// Then send workFolder to zipOutputFile
								FileSystemResource fileSystemResource = new FileSystemResource(res.getAbsolutePath());
								ZipEntry zipEntry = new ZipEntry(res.getName());
								zipEntry.setSize(fileSystemResource.contentLength());
								zipEntry.setTime(System.currentTimeMillis());
								zipOutputStream.putNextEntry(zipEntry);
								StreamUtils.copy(fileSystemResource.getInputStream(), zipOutputStream);
								zipOutputStream.closeEntry();
								files.add(res.getName());
							}
						}
					} else {
						DatasetFileUtils.getDatasetFilePathURLs(dataset, pathURLs, DatasetExpressionFormat.NIFTI_SINGLE_FILE, downloadResult);
						List<String> files = DatasetFileUtils.copyNiftiFilesForURLs(pathURLs, zipOutputStream, dataset, subjectName, false, datasetFilePath);
					}
				} else {
					downloadResult.update("Dataset format was not adapted to dataset download choosen", DatasetDownloadError.ERROR);
				}

				if (downloadResult.getStatus() == null) {
					downloadResults.remove(dataset.getId());
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

			String ids = String.join(",", datasets.stream().map(dataset -> dataset.getId().toString()).collect(Collectors.toList()));
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

	private String getSubjectName(Dataset dataset) {
		String subjectName = "unknownSubject";
		if(dataset.getSubjectId() != null){
			Optional<Subject> subjectOpt = subjectRepository.findById(dataset.getSubjectId());
			if (subjectOpt.isPresent()) {
				subjectName = subjectOpt.get().getName();
			}
		}
		return subjectName;
	}

	private String getFileName(List<Dataset> datasets) {
		if (datasets != null && datasets.size() == 1) {
			Dataset dataset = datasets.get(0);
			// Only one dataset -> the logic for one dataset is used
			String subjectName = getSubjectName(dataset);

			String datasetName = subjectName + "_" + dataset.getId() + "_" + dataset.getName();
			if (dataset.getUpdatedMetadata() != null && dataset.getUpdatedMetadata().getComment() != null) {
				datasetName += "_" + dataset.getUpdatedMetadata().getComment();
			}
			// Replace all forbidden characters.
			datasetName = datasetName.replaceAll("[^a-zA-Z0-9\\.\\-]", "_");

			return "Dataset_" +  datasetName + "_" + fileDateformatter.format(new DateTime().toDate()) + ZIP;
		} else {
			return "Datasets_" + fileDateformatter.format(new DateTime().toDate()) + ZIP;
		}
	}

	private String getDatasetFilepath(Dataset dataset, String studyName, String subjectName) {
		Examination exam;
		if (dataset.getDatasetAcquisition() == null && dataset.getDatasetProcessing() != null) {
			exam = dataset.getDatasetProcessing().getInputDatasets().get(0).getDatasetAcquisition()
					.getExamination();
		} else {
			exam = dataset.getDatasetAcquisition().getExamination();
		}

		String datasetFilePath = studyName + "_" + subjectName + "_Exam-" + exam.getId();
		if (exam.getComment() != null) {
			datasetFilePath += "-" + exam.getComment();
		}
		datasetFilePath = datasetFilePath.replaceAll("[^a-zA-Z0-9_\\-]", "_");
		if (datasetFilePath.length() > 255) {
			datasetFilePath = datasetFilePath.substring(0, 254);
		}
		return datasetFilePath;
	}

	private void reconvertNifti(Dataset dataset, String format, List<URL> pathURLs, DatasetDownloadError downloadResult, String subjectName, Long converterId, ZipOutputStream zipOutputStream) throws RestServiceException, IOException {
		// DOWNLOAD NIFTI AFTER RECONVERSION
		File userDir = DatasetFileUtils.getUserImportDir("/tmp");
		String tmpFilePath = userDir + File.separator + dataset.getId() + "_" + format;
		File workFolder = new File(tmpFilePath + "-" + fileDateformatter.format(new DateTime().toDate()));

		DatasetFileUtils.getDatasetFilePathURLs(dataset, pathURLs, DatasetExpressionFormat.DICOM, downloadResult);
		// Create temporary workfolder with dicom files, to be able to convert them
		workFolder.mkdirs();

		downloader.downloadDicomFilesForURLs(pathURLs, workFolder, subjectName, dataset, downloadResult);

		// Convert them, sending to import microservice
		boolean result = (boolean) this.rabbitTemplate.convertSendAndReceive(
				RabbitMQConfiguration.NIFTI_CONVERSION_QUEUE,
				converterId + ";" + workFolder.getAbsolutePath());
		if (!result) {
			throw new RestServiceException(
					new ErrorModel(HttpStatus.UNPROCESSABLE_ENTITY.value(), "Bad arguments", null));
		}
		workFolder = new File(workFolder.getAbsolutePath() + File.separator + "result");

		if (workFolder.listFiles() == null) {
			LOG.error("Could not convert nifti dataset");
			throw new RestServiceException(
					new ErrorModel(HttpStatus.UNPROCESSABLE_ENTITY.value(), "Bad arguments", null));
		}

		for (File res : workFolder.listFiles()) {
			if (!res.isDirectory()) {
				// Then send workFolder to zipOutputFile
				FileSystemResource fileSystemResource = new FileSystemResource(res.getAbsolutePath());
				ZipEntry zipEntry = new ZipEntry(res.getName());
				zipEntry.setSize(fileSystemResource.contentLength());
				zipEntry.setTime(System.currentTimeMillis());
				zipOutputStream.putNextEntry(zipEntry);
				StreamUtils.copy(fileSystemResource.getInputStream(), zipOutputStream);
				zipOutputStream.closeEntry();
			}
		}
	}

}

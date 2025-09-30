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
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.ArrayUtils;
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
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.util.StreamUtils;

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
	DatasetService datasetService;

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

	public void massiveDownload(String format, List<Dataset> datasets, HttpServletResponse response, boolean withManifest, Long converterId) throws RestServiceException {
		massiveDownload(format,  datasets, response, withManifest, converterId, false);

	}

	public void massiveDownload(String format, List<Dataset> datasets, HttpServletResponse response, boolean withManifest, Long converterId, Boolean withShanoirId) throws RestServiceException {
		Map<Long, List<String>> filesByAcquisitionId = new HashMap<>();

		response.setContentType("application/zip");
		response.setHeader("Content-Disposition",
				"attachment;filename = " + getFileName(datasets));
		Map<Long, DatasetDownloadError> downloadResults = new HashMap<Long, DatasetDownloadError>();
		Map<Long, String> datasetDownloadName = getDatasetDownloadName(datasets);

		try (ZipOutputStream zipOutputStream = new ZipOutputStream(response.getOutputStream())) {
			for (Dataset dataset : datasets) {
				// Create a new folder organized by subject / examination
				String subjectName = getSubjectName(dataset);
				if (subjectName.contains(File.separator)) {
					subjectName = subjectName.replaceAll(File.separator, "_");
				}

				String studyName = studyRepository.findById(dataset.getStudyId()).map(Study::getName).orElse("Unknown_study");


				String datasetFilePath = null;
				if (datasets.size() != 1) {
					datasetFilePath = getDatasetFilepath(dataset, studyName, subjectName, withShanoirId);
				}

				manageDatasetDownload(dataset, downloadResults, zipOutputStream, subjectName, datasetFilePath, format, withManifest, filesByAcquisitionId, converterId, datasetDownloadName.get(dataset.getId()));

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

	protected void manageDatasetDownload(Dataset dataset, Map<Long, DatasetDownloadError> downloadResults, ZipOutputStream zipOutputStream, String subjectName, String datasetFilePath, String format, boolean withManifest, Map<Long, List<String>> filesByAcquisitionId, Long converterId, String datasetDownloadName) throws IOException, RestServiceException {
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
			DatasetFileUtils.copyNiftiFilesForURLs(pathURLs, zipOutputStream, dataset, subjectName, true, datasetFilePath, datasetDownloadName);
		} else if (dataset instanceof EegDataset) {
			// DOWNLOAD EEG
			DatasetFileUtils.getDatasetFilePathURLs(dataset, pathURLs, DatasetExpressionFormat.EEG, downloadResult);
			DatasetFileUtils.copyNiftiFilesForURLs(pathURLs, zipOutputStream, dataset, subjectName, false, datasetFilePath, null);
		} else if (dataset instanceof BidsDataset) {
			// DOWNLOAD BIDS
			DatasetFileUtils.getDatasetFilePathURLs(dataset, pathURLs, DatasetExpressionFormat.BIDS, downloadResult);
			DatasetFileUtils.copyNiftiFilesForURLs(pathURLs, zipOutputStream, dataset, subjectName, true, datasetFilePath, null);
			// Manage errors here
		} else if (Objects.equals("dcm", format)) {
			// DOWNLOAD DICOM
			DatasetFileUtils.getDatasetFilePathURLs(dataset, pathURLs, DatasetExpressionFormat.DICOM, downloadResult);
			List<String> files = downloader.downloadDicomFilesForURLsAsZip(pathURLs, zipOutputStream, subjectName, dataset, datasetFilePath, downloadResult);
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
					List<String> files = DatasetFileUtils.copyNiftiFilesForURLs(pathURLs, zipOutputStream, dataset, subjectName, false,  datasetFilePath, null);
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

	protected void reconvertToNifti(String format, Long converterId, Dataset dataset, List<URL> pathURLs, DatasetDownloadError downloadResult, String subjectName, ZipOutputStream zipOutputStream) throws RestServiceException, IOException {
		File userDir = DatasetFileUtils.getUserImportDir("/tmp");
		String tmpFilePath = userDir + File.separator + dataset.getId() + "_" + format;

		File sourceFolder = new File(tmpFilePath + "-" + UUID.randomUUID());

		DatasetFileUtils.getDatasetFilePathURLs(dataset, pathURLs, DatasetExpressionFormat.DICOM, downloadResult);

		// Create temporary workfolder with dicom files, to be able to convert them
		sourceFolder.mkdirs();

		try {
			downloader.downloadDicomFilesForURLs(pathURLs, sourceFolder, subjectName, dataset, downloadResult);

			// Convert them, sending to import microservice
			boolean result = (boolean) this.rabbitTemplate.convertSendAndReceive(RabbitMQConfiguration.NIFTI_CONVERSION_QUEUE, converterId + ";" + sourceFolder.getAbsolutePath());
			if (!result) {
				downloadResult.update(CONVERSION_FAILED_ERROR_MSG, DatasetDownloadError.ERROR);
				return;
			}
			File workFolder = new File(sourceFolder.getAbsolutePath() + File.separator + "result");
			File[] files = workFolder.listFiles();

			if (ArrayUtils.isEmpty(files)) {
				downloadResult.update(CONVERSION_FAILED_ERROR_MSG, DatasetDownloadError.ERROR);
				return;
			}

			for (File res : files) {
				String datasetFilePath = res.getAbsolutePath();
				String fileName = res.getName();
				String extension = fileName.endsWith(NII_GZ) ? NII_GZ : "." + FilenameUtils.getExtension(fileName);
				String fileNameToSet = getDatasetFileName(dataset) + extension;

				// Gzip file if necessary in order to always return a .nii.gz file
				if (".nii".equals(extension)) {
					datasetFilePath = datasetFilePath + GZIP_EXTENSION;
					fileNameToSet = fileNameToSet + GZIP_EXTENSION;
					File file = new File(datasetFilePath);
					file.getParentFile().mkdirs();
					file.createNewFile();
					DatasetFileUtils.compressGzipFile(res.getAbsolutePath(), datasetFilePath);
				}

				if (!res.isDirectory()) {
					// Then send workFolder to zipOutputFile
					FileSystemResource fileSystemResource = new FileSystemResource(datasetFilePath);
					ZipEntry zipEntry = new ZipEntry(fileNameToSet);
					zipEntry.setSize(fileSystemResource.contentLength());
					zipEntry.setTime(System.currentTimeMillis());
					zipOutputStream.putNextEntry(zipEntry);
					StreamUtils.copy(fileSystemResource.getInputStream(), zipOutputStream);
					zipOutputStream.closeEntry();
				}
			}
		} finally {
			LOG.info("Deleting directory [{}]", sourceFolder.getAbsolutePath());
			FileUtils.deleteQuietly(sourceFolder);
		}
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
			return "Dataset_" +  datasetName + "_" + fileDateformatter.format(new DateTime().toDate()) + ZIP;
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

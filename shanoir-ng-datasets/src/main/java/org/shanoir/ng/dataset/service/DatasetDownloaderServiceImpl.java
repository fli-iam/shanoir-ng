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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import jakarta.servlet.http.HttpServletResponse;
import org.apache.commons.io.FileUtils;
import org.apache.solr.common.util.Hash;
import org.joda.time.DateTime;
import org.shanoir.ng.dataset.modality.BidsDataset;
import org.shanoir.ng.dataset.modality.EegDataset;
import org.shanoir.ng.dataset.model.Dataset;
import org.shanoir.ng.dataset.model.DatasetExpressionFormat;
import org.shanoir.ng.download.DatasetError;
import org.shanoir.ng.download.SerieError;
import org.shanoir.ng.download.WADODownloaderService;
import org.shanoir.ng.examination.model.Examination;
import org.shanoir.ng.shared.configuration.RabbitMQConfiguration;
import org.shanoir.ng.shared.event.ShanoirEvent;
import org.shanoir.ng.shared.event.ShanoirEventService;
import org.shanoir.ng.shared.event.ShanoirEventType;
import org.shanoir.ng.shared.exception.EntityNotFoundException;
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

	private static final String EEG = "eeg";

	private static final String NII = "nii";

	private static final String BIDS = "BIDS";

	private static final String DCM = "dcm";

	private static final String ZIP = ".zip";

	private static final String JAVA_IO_TMPDIR = "java.io.tmpdir";

	private static final Logger LOG = LoggerFactory.getLogger(DatasetDownloaderServiceImpl.class);

	private static final String JSON_ERROR_FILENAME = "ERRORS.json";

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

	@Autowired
	private ObjectMapper objectMapper;

	public void downloadDatasetById(Long datasetId, Long converterId, String format, HttpServletResponse response, boolean withManifest)
			throws RestServiceException, IOException {

		final Dataset dataset = datasetService.findById(datasetId);
		if (dataset == null) {
			throw new RestServiceException(
					new ErrorModel(HttpStatus.NOT_FOUND.value(), "Dataset with id not found.", null));
		}
		
		if (!dataset.isDownloadable()) {
			throw new RestServiceException(
					new ErrorModel(HttpStatus.UNAUTHORIZED.value(), "Dataset cannot be downloaded for security reasons.", null));
		}

		Map<Long, List<String>> filesByAcquisitionId = new HashMap<>();

		String subjectName = getSubjectName(dataset);

		String datasetName = subjectName + "_" + dataset.getId() + "_" + dataset.getName();
		if (dataset.getUpdatedMetadata() != null && dataset.getUpdatedMetadata().getComment() != null) {
			datasetName += "_" + dataset.getUpdatedMetadata().getComment();
		}
		// Replace all forbidden characters.
		datasetName = datasetName.replaceAll("[^a-zA-Z0-9\\.\\-]", "_");

		String tmpDir = System.getProperty(JAVA_IO_TMPDIR);
		File userDir = DatasetFileUtils.getUserImportDir(tmpDir);

		String tmpFilePath = userDir + File.separator + datasetName + "_" + format;

		SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmss");
		File workFolder = new File(tmpFilePath + "-" + formatter.format(new DateTime().toDate()));

		String zipFileName = datasetName + "_" + format + ZIP;

		response.setContentType("application/zip");
		response.setHeader("Content-Disposition", "attachment;filename=" + zipFileName);

		List<SerieError> serieErrors = new ArrayList<>();
		ZipOutputStream zipOutputStream = new ZipOutputStream(response.getOutputStream());
		try {
			List<URL> pathURLs = new ArrayList<>();
			switch (format) {
				case DCM:
					DatasetFileUtils.getDatasetFilePathURLs(dataset, pathURLs, DatasetExpressionFormat.DICOM, serieErrors);
					List<String> files = downloader.downloadDicomFilesForURLsAsZip(pathURLs, zipOutputStream, subjectName, dataset, null, serieErrors);
					if(withManifest){
						filesByAcquisitionId.putIfAbsent(dataset.getDatasetAcquisition().getId(), new ArrayList<>());
						filesByAcquisitionId.get(dataset.getDatasetAcquisition().getId()).addAll(files);
					}
					break;
				case NII:
					// Check if we want a specific converter -> nifti reconversion
					if (converterId != null) {
						DatasetFileUtils.getDatasetFilePathURLs(dataset, pathURLs, DatasetExpressionFormat.DICOM, serieErrors);
						// Create temporary workfolder with dicom files, to be able to convert them
						workFolder.mkdirs();

						downloader.downloadDicomFilesForURLs(pathURLs, workFolder, subjectName, dataset, serieErrors);

						// Convert them, sending to import microservice
						boolean result = (boolean) this.rabbitTemplate.convertSendAndReceive(
								RabbitMQConfiguration.NIFTI_CONVERSION_QUEUE,
								converterId + ";" + workFolder.getAbsolutePath());
						if (!result) {
							throw new RestServiceException(
									new ErrorModel(HttpStatus.UNPROCESSABLE_ENTITY.value(), "Bad arguments", null));
						}
						workFolder = new File(workFolder.getAbsolutePath() + File.separator + "result");

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
					} else {
						DatasetFileUtils.getDatasetFilePathURLs(dataset, pathURLs,
								DatasetExpressionFormat.NIFTI_SINGLE_FILE, serieErrors);
						DatasetFileUtils.copyNiftiFilesForURLs(pathURLs, zipOutputStream, dataset, subjectName, false,
								null);
					}
					break;
				case EEG:
					DatasetFileUtils.getDatasetFilePathURLs(dataset, pathURLs, DatasetExpressionFormat.EEG, serieErrors);
					DatasetFileUtils.copyNiftiFilesForURLs(pathURLs, zipOutputStream, dataset, subjectName, false,
							null);
					break;
				case BIDS:
					DatasetFileUtils.getDatasetFilePathURLs(dataset, pathURLs, DatasetExpressionFormat.BIDS, serieErrors);
					DatasetFileUtils.copyNiftiFilesForURLs(pathURLs, zipOutputStream, dataset, subjectName, true, null);
					break;
				default:
					throw new RestServiceException(
							new ErrorModel(HttpStatus.UNPROCESSABLE_ENTITY.value(), "Bad arguments", null));
			}

			// Check folder emptiness
			if (pathURLs.isEmpty()) {
				// Folder is empty => return an error
				LOG.error("No files could be found for the dataset(s).");
				throw new RestServiceException(
						new ErrorModel(HttpStatus.UNPROCESSABLE_ENTITY.value(), "No files could be found for this dataset(s)."));
			}

			if(!filesByAcquisitionId.isEmpty()){
				DatasetFileUtils.writeManifestForExport(zipOutputStream, filesByAcquisitionId);
			}

			ShanoirEvent event = new ShanoirEvent(ShanoirEventType.DOWNLOAD_DATASET_EVENT, dataset.getId().toString(), KeycloakUtil.getTokenUserId(), dataset.getId().toString() + "." + format, ShanoirEvent.SUCCESS);
			eventService.publishEvent(event);
			if (!serieErrors.isEmpty()) {
				DatasetError error = new DatasetError(datasetId, null);
				error.setSerieErrors(serieErrors);
				writeErrorFileInZip(error, zipOutputStream);
			}
		} catch (Exception e) {
			LOG.error("Error while retrieveing dataset data.", e);
			DatasetError error = new DatasetError(datasetId, e.getMessage());
			error.setSerieErrors(serieErrors);
			writeErrorFileInZip(error, zipOutputStream);
			throw new RestServiceException(e,
					new ErrorModel(HttpStatus.UNPROCESSABLE_ENTITY.value(),
							"No files could be found for this dataset(s)."));
		} finally {
			zipOutputStream.close();
			FileUtils.deleteQuietly(workFolder);
		}
	}

	private void writeErrorFileInZip(DatasetError error, ZipOutputStream zipOutputStream) throws IOException {
		ZipEntry zipEntry = new ZipEntry(JSON_ERROR_FILENAME);
		zipEntry.setTime(System.currentTimeMillis());
		zipOutputStream.putNextEntry(zipEntry);
		zipOutputStream.write(objectMapper.writeValueAsString(error).getBytes());
		zipOutputStream.closeEntry();
	}

	public void massiveDownload(String format, List<Dataset> datasets, HttpServletResponse response, boolean withManifest, Long converterId) throws EntityNotFoundException, RestServiceException, IOException {
		// STEP 3: Get the data
		// Check rights on at least one of the datasets and filter the datasetIds list

		boolean isEmpty = true;
		// Get the data
		List<Dataset> failingDatasets = new ArrayList<Dataset>();

		Map<Long, List<String>> filesByAcquisitionId = new HashMap<>();

		response.setContentType("application/zip");
		// Add timestamp to get a difference
		SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmss");
		response.setHeader("Content-Disposition",
				"attachment;filename=" + "Datasets" + formatter.format(new DateTime().toDate()));

		try(ZipOutputStream zipOutputStream = new ZipOutputStream(response.getOutputStream())) {
			
			for (Dataset dataset : datasets) {
				if (!dataset.isDownloadable()) {
					continue;
				}
				try {
					List<String> datasetFiles = new ArrayList<>();

					// Ignore non adapted datasets
					if (EEG.equals(format) && !(dataset instanceof EegDataset)) {
						continue;
					}
					if (!EEG.equals(format) && (dataset instanceof EegDataset)) {
						continue;
					}
					// Create a new folder organized by subject / examination
					String subjectName = getSubjectName(dataset);
					if (subjectName.contains(File.separator)) {
						subjectName = subjectName.replaceAll(File.separator, "_");
					}
					String studyName = studyRepository.findById(dataset.getStudyId()).orElse(null).getName();

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

					List<URL> pathURLs = new ArrayList<>();

					if (dataset instanceof EegDataset) {
						DatasetFileUtils.getDatasetFilePathURLs(dataset, pathURLs, DatasetExpressionFormat.EEG, null);
						List<String> files = DatasetFileUtils.copyNiftiFilesForURLs(pathURLs, zipOutputStream, dataset,
								subjectName, false, datasetFilePath);
						datasetFiles.addAll(files);
					}  else if (dataset instanceof BidsDataset) {
						DatasetFileUtils.getDatasetFilePathURLs(dataset, pathURLs, DatasetExpressionFormat.BIDS, null);
						List<String> files = DatasetFileUtils.copyNiftiFilesForURLs(pathURLs, zipOutputStream, dataset,
								subjectName, true, datasetFilePath);
						datasetFiles.addAll(files);
					} else if (DCM.equals(format)) {
						if (dataset.getDatasetProcessing() != null) {
							// Do not load dicom for processed dataset
							continue;
						}
						DatasetFileUtils.getDatasetFilePathURLs(dataset, pathURLs, DatasetExpressionFormat.DICOM, null);
						List<String> files = downloader.downloadDicomFilesForURLsAsZip(pathURLs, zipOutputStream,
								subjectName, dataset, datasetFilePath, null);
						datasetFiles.addAll(files);

						if(withManifest){
							filesByAcquisitionId.putIfAbsent(dataset.getDatasetAcquisition().getId(), new ArrayList<>());
							filesByAcquisitionId.get(dataset.getDatasetAcquisition().getId()).addAll(datasetFiles);
						}

					} else if (NII.equals(format)) {
						LOG.error("hey: " +converterId);
						// Check if we want a specific converter -> nifti reconversion
						if (converterId != null) {
							File userDir = DatasetFileUtils.getUserImportDir("/tmp");
							String tmpFilePath = userDir + File.separator + dataset.getId() + "_" + format;
							File workFolder = new File(tmpFilePath + "-" + formatter.format(new DateTime().toDate()));

							DatasetFileUtils.getDatasetFilePathURLs(dataset, pathURLs, DatasetExpressionFormat.DICOM, null);

							LOG.error("hey: " + pathURLs);

							// Create temporary workfolder with dicom files, to be able to convert them
							workFolder.mkdirs();

							downloader.downloadDicomFilesForURLs(pathURLs, workFolder, subjectName, dataset, null);

							// Convert them, sending to import microservice
							boolean result = (boolean) this.rabbitTemplate.convertSendAndReceive(RabbitMQConfiguration.NIFTI_CONVERSION_QUEUE, converterId + ";" + workFolder.getAbsolutePath());
							if (!result) {
								throw new RestServiceException(
										new ErrorModel(HttpStatus.UNPROCESSABLE_ENTITY.value(), "Bad arguments", null));
							} 
							workFolder = new File(workFolder.getAbsolutePath() + File.separator + "result");
							LOG.error(workFolder.getAbsolutePath());
							List<String> files = new ArrayList<>();
							for (File res : workFolder.listFiles()) {
								LOG.error(res.getAbsolutePath());

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
							datasetFiles.addAll(files);
						} else  {
							DatasetFileUtils.getDatasetFilePathURLs(dataset, pathURLs, DatasetExpressionFormat.NIFTI_SINGLE_FILE);
							List<String> files = DatasetFileUtils.copyNiftiFilesForURLs(pathURLs, zipOutputStream, dataset, subjectName, false, datasetFilePath);
							datasetFiles.addAll(files);
						}
						
						DatasetFileUtils.getDatasetFilePathURLs(dataset, pathURLs, DatasetExpressionFormat.NIFTI_SINGLE_FILE);
						List<String> files = DatasetFileUtils.copyNiftiFilesForURLs(pathURLs, zipOutputStream, dataset, subjectName, false, datasetFilePath);
						datasetFiles.addAll(files);
					} else {
						throw new RestServiceException(
								new ErrorModel(HttpStatus.UNPROCESSABLE_ENTITY.value(),
										"Please choose either nifti, dicom or eeg file type.", null));
					}
					isEmpty = isEmpty && pathURLs.isEmpty();
					if (pathURLs.isEmpty()) {
						failingDatasets.add(dataset);
					}
				} catch (OutOfMemoryError error) {
					LOG.error("Out of memory error while copying files: ", error);
					throw new RestServiceException(
							new ErrorModel(HttpStatus.UNPROCESSABLE_ENTITY.value(),
									"The size of data you tried to download is too Important. Please split your download.",
									error));
				} catch (Exception e) {
					// Here we just keep in memory the list of failing files
					LOG.error("Error while copying files: ", e);
					failingDatasets.add(dataset);
				}
			}

			// Check emptiness => no data at all
			if (isEmpty) {
				// Folder is empty => return an error
				LOG.error("No files could be found for the dataset(s).");
				throw new RestServiceException(
						new ErrorModel(HttpStatus.UNPROCESSABLE_ENTITY.value(),
								"No files could be found for the dataset(s)."));
			}

			// Check for errors
			if (!failingDatasets.isEmpty()) {
				StringBuilder listOfDatasets = new StringBuilder();
				for (Dataset dataset : failingDatasets) {
					listOfDatasets.append("(ID = ").append(dataset.getId())
							.append(") ")
							.append(dataset.getName())
							.append("\n");
				}

				ZipEntry zipEntry = new ZipEntry(FAILURES_TXT);
				zipEntry.setTime(System.currentTimeMillis());
				zipOutputStream.putNextEntry(zipEntry);
				zipOutputStream.write(listOfDatasets.toString().getBytes());
				zipOutputStream.closeEntry();
			}

			if(!filesByAcquisitionId.isEmpty()){
				DatasetFileUtils.writeManifestForExport(zipOutputStream, filesByAcquisitionId);
			}

			String ids = String.join(",",
					datasets.stream().map(dataset -> dataset.getId().toString()).collect(Collectors.toList()));
			ShanoirEvent event = new ShanoirEvent(ShanoirEventType.DOWNLOAD_DATASET_EVENT, ids,
					KeycloakUtil.getTokenUserId(), ids + "." + format, ShanoirEvent.IN_PROGRESS);
			event.setStatus(ShanoirEvent.SUCCESS);
			eventService.publishEvent(event);
		} catch (Exception e) {
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

}

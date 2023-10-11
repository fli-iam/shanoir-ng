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
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.apache.commons.io.FileUtils;
import org.joda.time.DateTime;
import org.shanoir.ng.dataset.modality.EegDataset;
import org.shanoir.ng.dataset.model.Dataset;
import org.shanoir.ng.dataset.model.DatasetExpressionFormat;
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

import jakarta.servlet.http.HttpServletResponse;

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

	public void massiveDownload(String format, List<Dataset> datasets, HttpServletResponse response, boolean withInputFile, Long converterId) throws EntityNotFoundException, RestServiceException, IOException {
		// STEP 3: Get the data
		// Check rights on at least one of the datasets and filter the datasetIds list

		boolean isEmpty = true;
		// Get the data
		List<Dataset> failingDatasets = new ArrayList<Dataset>();

		Map<Long, List<String>> filesByAcquisitionId = new HashMap<>();

		response.setContentType("application/zip");
		// Add timestamp to get a difference
		SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmss");
		response.setHeader("Content-Disposition", "attachment;filename=" + "Datasets" + formatter.format(new DateTime().toDate()));

		try(ZipOutputStream zipOutputStream = new ZipOutputStream(response.getOutputStream())) {
			
			String tmpDir = System.getProperty(JAVA_IO_TMPDIR);
			File userDir = DatasetFileUtils.getUserImportDir(tmpDir);

			for (Dataset dataset : datasets) {
				if (!dataset.isDownloadable()) {
					continue;
				}
				try {
					List<String> datasetFiles = new ArrayList<>();

					// Ignore non adapted datasets
					if (EEG.equals(format) && ! (dataset instanceof EegDataset)) {
						continue;
					}
					if (!EEG.equals(format) &&  (dataset instanceof EegDataset)) {
						continue;
					}
					// Create a new folder organized by subject / examination
					String subjectName = subjectRepository.findById(dataset.getSubjectId()).orElse(null).getName();
					if (subjectName.contains(File.separator)) {
						subjectName = subjectName.replaceAll(File.separator, "_");
					}
					String studyName = studyRepository.findById(dataset.getStudyId()).orElse(null).getName();

					Examination exam;
					if (dataset.getDatasetAcquisition() == null && dataset.getDatasetProcessing() != null) {
						exam = dataset.getDatasetProcessing().getInputDatasets().get(0).getDatasetAcquisition().getExamination();
					} else {
						exam = dataset.getDatasetAcquisition().getExamination();
					}

					String datasetFilePath = studyName + "_" + subjectName + "_Exam-" + exam.getId();
					if (exam.getComment() != null) {
						datasetFilePath += "-" + exam.getComment();
					}
					datasetFilePath = datasetFilePath. replaceAll("[^a-zA-Z0-9_\\-]", "_");
					if(datasetFilePath.length() > 255 ){
						datasetFilePath = datasetFilePath.substring(0, 254);
					}

					List<URL> pathURLs = new ArrayList<>();

					if (dataset instanceof EegDataset) {
						DatasetFileUtils.getDatasetFilePathURLs(dataset, pathURLs, DatasetExpressionFormat.EEG);
						List<String> files = DatasetFileUtils.copyNiftiFilesForURLs(pathURLs, zipOutputStream, dataset, subjectName, false, datasetFilePath);
						datasetFiles.addAll(files);
					} else if (DCM.equals(format)) {
						if (dataset.getDatasetProcessing() != null) {
							// Do not load dicom for processed dataset
							continue;
						}
						DatasetFileUtils.getDatasetFilePathURLs(dataset, pathURLs, DatasetExpressionFormat.DICOM);
						List<String> files = downloader.downloadDicomFilesForURLsAsZip(pathURLs, zipOutputStream, subjectName, dataset, datasetFilePath);
						datasetFiles.addAll(files);

						if(withInputFile){
							filesByAcquisitionId.putIfAbsent(dataset.getDatasetAcquisition().getId(), new ArrayList<>());
							filesByAcquisitionId.get(dataset.getDatasetAcquisition().getId()).addAll(datasetFiles);
						}

					} else if (NII.equals(format)) {
						
						// Check if we want a specific converter -> nifti reconversion
						if (converterId != null) {
							String tmpFilePath = userDir + File.separator + dataset.getId() + "_" + format;
							File workFolder = new File(tmpFilePath + "-" + formatter.format(new DateTime().toDate()));

							DatasetFileUtils.getDatasetFilePathURLs(dataset, pathURLs, DatasetExpressionFormat.DICOM);

							// Create temporary workfolder with dicom files, to be able to convert them
							workFolder.mkdirs();

							downloader.downloadDicomFilesForURLs(pathURLs, workFolder, subjectName, dataset);

							// Convert them, sending to import microservice
							boolean result = (boolean) this.rabbitTemplate.convertSendAndReceive(RabbitMQConfiguration.NIFTI_CONVERSION_QUEUE, converterId + ";" + workFolder.getAbsolutePath());
							if (!result) {
								throw new RestServiceException(
										new ErrorModel(HttpStatus.UNPROCESSABLE_ENTITY.value(), "Bad arguments", null));
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
							datasetFiles.addAll(files);
						} else  {
							DatasetFileUtils.getDatasetFilePathURLs(dataset, pathURLs, DatasetExpressionFormat.NIFTI_SINGLE_FILE);
							List<String> files = DatasetFileUtils.copyNiftiFilesForURLs(pathURLs, zipOutputStream, dataset, subjectName, false, datasetFilePath);
							datasetFiles.addAll(files);
						}
						
						DatasetFileUtils.getDatasetFilePathURLs(dataset, pathURLs, DatasetExpressionFormat.NIFTI_SINGLE_FILE);
						List<String> files = DatasetFileUtils.copyNiftiFilesForURLs(pathURLs, zipOutputStream, dataset, subjectName, false, datasetFilePath);
						datasetFiles.addAll(files);
					} else if (BIDS.equals(format)) {
						DatasetFileUtils.getDatasetFilePathURLs(dataset, pathURLs, DatasetExpressionFormat.BIDS);
						List<String> files = DatasetFileUtils.copyNiftiFilesForURLs(pathURLs, zipOutputStream, dataset, subjectName, true, datasetFilePath);
						datasetFiles.addAll(files);
					} else {
						throw new RestServiceException(
								new ErrorModel(HttpStatus.UNPROCESSABLE_ENTITY.value(), "Please choose either nifti, dicom or eeg file type.", null));
					}
					isEmpty = isEmpty && pathURLs.isEmpty();
					if (pathURLs.isEmpty()) {
						failingDatasets.add(dataset);
					}
				} catch(OutOfMemoryError error) {
					LOG.error("Out of memory error while copying files: ", error);
					throw new RestServiceException(
							new ErrorModel(HttpStatus.UNPROCESSABLE_ENTITY.value(), "The size of data you tried to download is too Important. Please split your download.", error));
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
						new ErrorModel(HttpStatus.UNPROCESSABLE_ENTITY.value(), "No files could be found for the dataset(s)."));
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

			String ids = String.join(",", datasets.stream().map(dataset -> dataset.getId().toString()).collect(Collectors.toList()));
			ShanoirEvent event = new ShanoirEvent(ShanoirEventType.DOWNLOAD_DATASET_EVENT, ids, KeycloakUtil.getTokenUserId(), ids + "." + format, ShanoirEvent.IN_PROGRESS);
			event.setStatus(ShanoirEvent.SUCCESS);
			eventService.publishEvent(event);
		} catch (Exception e) {
			LOG.error("Unexpected error while downloading dataset files.", e);
			throw new RestServiceException(
					new ErrorModel(HttpStatus.UNPROCESSABLE_ENTITY.value(), "Unexpected error while downloading dataset files"));
		}
	}

}

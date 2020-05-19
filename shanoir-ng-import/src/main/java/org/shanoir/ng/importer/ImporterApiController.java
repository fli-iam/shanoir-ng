/**
< * Shanoir NG - Import, manage and share neuroimaging data
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

package org.shanoir.ng.importer;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.nio.file.Files;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipOutputStream;

import javax.validation.Valid;

import org.apache.commons.io.FilenameUtils;
import org.shanoir.ng.importer.dicom.DicomDirCreator;
import org.shanoir.ng.exchange.imports.dicom.DicomDirGeneratorService;
import org.shanoir.ng.exchange.model.ExExamination;
import org.shanoir.ng.exchange.model.ExStudy;
import org.shanoir.ng.exchange.model.ExStudyCard;
import org.shanoir.ng.exchange.model.ExSubject;
import org.shanoir.ng.exchange.model.Exchange;
import org.shanoir.ng.importer.dicom.DicomDirToModelService;
import org.shanoir.ng.importer.dicom.ImagesCreatorAndDicomFileAnalyzerService;
import org.shanoir.ng.importer.dicom.ImportJobConstructorService;
import org.shanoir.ng.importer.dicom.query.DicomQuery;
import org.shanoir.ng.importer.dicom.query.QueryPACSService;
import org.shanoir.ng.importer.dto.CommonIdNamesDTO;
import org.shanoir.ng.importer.dto.CommonIdsDTO;
import org.shanoir.ng.importer.eeg.brainvision.BrainVisionReader;
import org.shanoir.ng.importer.eeg.edf.EDFAnnotation;
import org.shanoir.ng.importer.eeg.edf.EDFParser;
import org.shanoir.ng.importer.eeg.edf.EDFParserResult;
import org.shanoir.ng.importer.model.Channel;
import org.shanoir.ng.importer.model.EegDataset;
import org.shanoir.ng.importer.model.EegImportJob;
import org.shanoir.ng.importer.model.Event;
import org.shanoir.ng.importer.dto.ExaminationDTO;
import org.shanoir.ng.importer.model.ImportJob;
import org.shanoir.ng.importer.model.Patient;
import org.shanoir.ng.importer.model.Serie;
import org.shanoir.ng.importer.model.Study;
import org.shanoir.ng.importer.model.Subject;
import org.shanoir.ng.shared.configuration.RabbitMQConfiguration;
import org.shanoir.ng.shared.core.model.IdName;
import org.shanoir.ng.shared.exception.ErrorModel;
import org.shanoir.ng.shared.exception.RestServiceException;
import org.shanoir.ng.shared.exception.ShanoirException;
import org.shanoir.ng.shared.exception.ShanoirImportException;
import org.shanoir.ng.study.rights.StudyUser;
import org.shanoir.ng.study.rights.StudyUserInterface;
import org.shanoir.ng.utils.ImportUtils;
import org.shanoir.ng.utils.KeycloakUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import io.swagger.annotations.ApiParam;

/**
 * This is the main component of the import of Shanoir-NG. The front-end in
 * Angular only communicates with this service. The import ms itself is calling
 * the ms datasets service.
 * 
 * The Import MS returns only a random ID to the outside world for one import.
 * Internally each user has its own folder in the importDirectory. So, when the
 * workFolder in the ImportJob is set to be returned, there is only the random
 * ID. When the requests arrive MS Import is adding the userId and the real path
 * value.
 * 
 * @author mkain
 *
 */
@Controller
public class ImporterApiController implements ImporterApi {

	private static final String WRONG_CONTENT_FILE_UPLOAD = "Wrong content type of file upload, .zip required.";

	private static final String ERROR_WHILE_SAVING_UPLOADED_FILE = "Error while saving uploaded file.";

	private static final String NO_FILE_UPLOADED = "No file uploaded.";

	private static final Logger LOG = LoggerFactory.getLogger(ImporterApiController.class);

	private static final SecureRandom RANDOM = new SecureRandom();

	private static final String FILE_POINT = ".";

	private static final String DICOMDIR = "DICOMDIR";

	private static final String IMPORTJOB = "importJob.json";

	private static final String APPLICATION_OCTET_STREAM = "application/octet-stream";

	private static final String APPLICATION_ZIP = "application/zip";

	private static final String UPLOAD_FILE_SUFFIX = ".upload";

	private static final String ZIP_FILE_SUFFIX = ".zip";

	@Value("${ms.url.shanoir-ng-datasets-eeg}")
	private String datasetsMsUrl;

	@Value("${ms.url.shanoir-ng-studies-commons}")
	private String studiesCommonMsUrl;

	@Value("${ms.url.shanoir-ng-studies-subjects-names}")
	private String studiesSubjectsNamesMsUrl;

	@Value("${ms.url.shanoir-ng-create-examination}")
	private String createExaminationMsUrl;

	@Value("${shanoir.import.directory}")
	private String importDir;

	@Autowired
	private RestTemplate restTemplate;

	@Autowired
	private DicomDirGeneratorService dicomDirGeneratorService;

	@Autowired
	private DicomDirToModelService dicomDirToModel;

	@Autowired
	private ImportJobConstructorService importJobConstructorService;

	@Autowired
	private ImagesCreatorAndDicomFileAnalyzerService imagesCreatorAndDicomFileAnalyzer;

	@Autowired
	private ImporterManagerService importerManagerService;

	@Autowired
	private QueryPACSService queryPACSService;

	@Autowired
	private RabbitTemplate rabbitTemplate;

	public ResponseEntity<Void> uploadFiles(
			@ApiParam(value = "file detail") @RequestPart("files") final MultipartFile[] files)
			throws RestServiceException {
		if (files.length == 0) {
			throw new RestServiceException(
					new ErrorModel(HttpStatus.UNPROCESSABLE_ENTITY.value(), NO_FILE_UPLOADED, null));
		}
		try {
			// not used currently
			for (int i = 0; i < files.length; i++) {
				saveTempFile(new File(importDir), files[i]);
			}
			return new ResponseEntity<>(HttpStatus.OK);
		} catch (IOException e) {
			throw new RestServiceException(
					new ErrorModel(HttpStatus.UNPROCESSABLE_ENTITY.value(), ERROR_WHILE_SAVING_UPLOADED_FILE, null));
		}
	}

	@Override
	public ResponseEntity<ImportJob> uploadDicomZipFile(
			@ApiParam(value = "file detail") @RequestPart("file") final MultipartFile dicomZipFile)
			throws RestServiceException {
		if (dicomZipFile == null || !isZipFile(dicomZipFile)) {
			throw new RestServiceException(
					new ErrorModel(HttpStatus.UNPROCESSABLE_ENTITY.value(), WRONG_CONTENT_FILE_UPLOAD, null));
		}
		if (!isZipFile(dicomZipFile)) {
			throw new RestServiceException(new ErrorModel(HttpStatus.UNPROCESSABLE_ENTITY.value(),
					"Wrong content type of file upload, .zip required.", null));
		}
		try {
			/**
			 * 1. STEP: Handle file management. Always create a userId specific folder in
			 * the import work folder (the root of everything): split imports to clearly
			 * separate them into separate folders for each user
			 */
			File userImportDir = getUserImportDir();
			File importJobDir = saveTempFileCreateFolderAndUnzip(userImportDir, dicomZipFile, true);

			/**
			 * 2. STEP: prepare patients list to be put into ImportJob: read DICOMDIR and
			 * complete with meta-data from files
			 */
			List<Patient> patients = preparePatientsForImportJob(importJobDir);

			/**
			 * 3. STEP: create ImportJob
			 */
			ImportJob importJob = new ImportJob();
			importJob.setFromDicomZip(true);
			// Work folder is always relative to general import directory
			importJob.setWorkFolder(importJobDir.getName());
			importJob.setPatients(patients);
			return new ResponseEntity<ImportJob>(importJob, HttpStatus.OK);
		} catch (IOException e) {
			LOG.error(e.getMessage(), e);
			throw new RestServiceException(
					new ErrorModel(HttpStatus.UNPROCESSABLE_ENTITY.value(), ERROR_WHILE_SAVING_UPLOADED_FILE, null));
		}
	}

	/**
	 * 1. STEP: read DICOMDIR and create Shanoir model from it (== Dicom model):
	 * Patient - Study - Serie - Instance 2. STEP: split instances into non-images
	 * and images and get additional meta-data from first dicom file of each serie,
	 * meta-data missing in dicomdir.
	 * 
	 * @param dirWithDicomDir
	 * @return
	 * @throws IOException
	 * @throws FileNotFoundException
	 */
	private List<Patient> preparePatientsForImportJob(File dirWithDicomDir) throws IOException, FileNotFoundException {
		List<Patient> patients = null;
		File dicomDirFile = new File(dirWithDicomDir.getAbsolutePath() + File.separator + DICOMDIR);
		if (dicomDirFile.exists()) {
			patients = dicomDirToModel.readDicomDirToPatients(dicomDirFile);
		}
		return patients;
	}

	@Override
	public ResponseEntity<Void> startImportJob(
			@ApiParam(value = "ImportJob", required = true) @Valid @RequestBody final ImportJob importJob)
			throws RestServiceException {
		File userImportDir = getUserImportDir();
		final Long userId = KeycloakUtil.getTokenUserId();
		String tempDirId = importJob.getWorkFolder();
		final File importJobDir = new File(userImportDir, tempDirId);
		if (importJobDir.exists()) {
			importJob.setWorkFolder(importJobDir.getAbsolutePath());
			if (!importJob.isFromShanoirUploader()) {
				importJob.setAnonymisationProfileToUse("Profile Neurinfo");
			}
			removeUnselectedSeries(importJob);
			importerManagerService.manageImportJob(userId, KeycloakUtil.getKeycloakHeader(), importJob);
			return new ResponseEntity<Void>(HttpStatus.OK);
		} else {
			LOG.error("Missing importJobDir.");
			throw new RestServiceException(
					new ErrorModel(HttpStatus.UNPROCESSABLE_ENTITY.value(), "Missing importJobDir.", null));
		}
	}

	private void removeUnselectedSeries(final ImportJob importJob) {
		for (Iterator<Patient> patientIt = importJob.getPatients().iterator(); patientIt.hasNext();) {
			Patient patient = patientIt.next();
			List<Study> studies = patient.getStudies();
			for (Iterator<Study> studyIt = studies.iterator(); studyIt.hasNext();) {
				Study study = studyIt.next();
				List<Serie> series = study.getSeries();
				for (Iterator<Serie> serieIt = series.iterator(); serieIt.hasNext();) {
					Serie serie = serieIt.next();
					if (!serie.getSelected()) {
						serieIt.remove();
					}
				}
			}
		}
	}

	@Override
	public ResponseEntity<ImportJob> queryPACS(
			@ApiParam(value = "DicomQuery", required = true) @Valid @RequestBody final DicomQuery dicomQuery)
			throws RestServiceException {
		ImportJob importJob;
		try {
			importJob = queryPACSService.queryCFIND(dicomQuery);
			// the pacs workfolder is empty here, as multiple queries could be asked before
			// string an import
			importJob.setWorkFolder("");
			importJob.setFromPacs(true);
		} catch (ShanoirException e) {
			throw new RestServiceException(
					new ErrorModel(HttpStatus.UNPROCESSABLE_ENTITY.value(), e.getMessage(), null));
		}
		if (importJob.getPatients() == null || importJob.getPatients().isEmpty()) {
			return new ResponseEntity<>(HttpStatus.NO_CONTENT);
		}
		return new ResponseEntity<>(importJob, HttpStatus.OK);
	}

	/**
	 * This method takes a multipart file and stores it in a configured upload
	 * directory in relation with the userId with a random name and the suffix
	 * .upload
	 *
	 * @param file
	 * @throws IOException
	 */
	private File saveTempFile(final File userImportDir, final MultipartFile file) throws IOException {
		long n = createRandomLong();
		File uploadFile = new File(userImportDir.getAbsolutePath(), Long.toString(n) + UPLOAD_FILE_SUFFIX);
		file.transferTo(uploadFile);
		return uploadFile;
	}

	/**
	 * This method creates a random long number.
	 * 
	 * @return long: random number
	 */
	private long createRandomLong() {
		long n = RANDOM.nextLong();
		if (n == Long.MIN_VALUE) {
			n = 0; // corner case
		} else {
			n = Math.abs(n);
		}
		return n;
	}

	/**
	 * This method stores an uploaded zip file in a temporary file, creates a new
	 * folder with the same name and unzips the content into this folder, and gives
	 * back the folder with the content.
	 * 
	 * @param userImportDir
	 * @param dicomZipFile
	 * @return
	 * @throws IOException
	 * @throws RestServiceException
	 */
	private File saveTempFileCreateFolderAndUnzip(final File userImportDir, final MultipartFile dicomZipFile,
			final boolean fromDicom) throws IOException, RestServiceException {
		File tempFile = saveTempFile(userImportDir, dicomZipFile);
		if (fromDicom && !ImportUtils.checkZipContainsFile(DICOMDIR, tempFile)) {
			throw new RestServiceException(
					new ErrorModel(HttpStatus.UNPROCESSABLE_ENTITY.value(), "DICOMDIR is missing in .zip file.", null));
		}
		String fileName = tempFile.getName();
		int pos = fileName.lastIndexOf(FILE_POINT);
		if (pos > 0) {
			fileName = fileName.substring(0, pos);
		}
		File unzipFolderFile = new File(tempFile.getParentFile().getAbsolutePath() + File.separator + fileName);
		if (!unzipFolderFile.exists()) {
			unzipFolderFile.mkdirs();
		} else {
			throw new RestServiceException(new ErrorModel(HttpStatus.UNPROCESSABLE_ENTITY.value(),
					"Error while unzipping file: folder already exists.", null));
		}
		ImportUtils.unzip(tempFile.getAbsolutePath(), unzipFolderFile.getAbsolutePath());
		tempFile.delete();
		return unzipFolderFile;
	}

	/**
	 * Check if sent file is of type .zip.
	 *
	 * @param file
	 */
	private boolean isZipFile(final MultipartFile file) {
		return file.getOriginalFilename().endsWith(ZIP_FILE_SUFFIX) || file.getContentType().equals(APPLICATION_ZIP)
				|| file.getContentType().equals(APPLICATION_OCTET_STREAM);
	}

	@Override
	public ResponseEntity<ImportJob> importDicomZipFile(
			@ApiParam(value = "file detail") @RequestBody final String dicomZipFilename) throws RestServiceException {
		if (dicomZipFilename == null) {
			throw new RestServiceException(
					new ErrorModel(HttpStatus.UNPROCESSABLE_ENTITY.value(), NO_FILE_UPLOADED, null));
		}
		File tempFile = new File(dicomZipFilename);

		// Import dicomfile
		ResponseEntity<ImportJob> dicomFile = importDicomZipFile(tempFile);

		// Delete temp file which is useless now
		tempFile.delete();

		return dicomFile;
	}

	private ResponseEntity<ImportJob> importDicomZipFile(final File dicomZipFile) throws RestServiceException {
		if (!isZipFileFromFile(dicomZipFile)) {
			throw new RestServiceException(
					new ErrorModel(HttpStatus.UNPROCESSABLE_ENTITY.value(), WRONG_CONTENT_FILE_UPLOAD, null));
		}
		try {
			LOG.info("importDicomZipFile step1 unzip file ");
			/**
			 * 1. STEP: Handle file management. Always create a userId specific folder in
			 * the import work folder (the root of everything): split imports to clearly
			 * separate them into separate folders for each user
			 */
			final Long userId = KeycloakUtil.getTokenUserId();
			final String userImportDirFilePath = importDir + File.separator + Long.toString(userId);
			final File userImportDir = new File(userImportDirFilePath);
			if (!userImportDir.exists()) {
				userImportDir.mkdirs(); // create if not yet existing
			}
			File importJobDir = saveTempFileCreateFolderAndUnzipFromFile(userImportDir, dicomZipFile);
			LOG.info("...unzipped into  {}", userImportDir);

			/**
			 * 2. STEP: read DICOMDIR and create Shanoir model from it (== Dicom model):
			 * Patient - Study - Serie - Instance
			 */
			LOG.info("importDicomZipFile step2 read DICOMDIR ");
			List<Patient> patients = null;
			File dicomDirFile = new File(importJobDir.getAbsolutePath() + File.separator + DICOMDIR);
			if (dicomDirFile.exists()) {
				patients = dicomDirToModel.readDicomDirToPatients(dicomDirFile);
			}
			/**
			 * 3. STEP: split instances into non-images and images and get additional
			 * meta-data from first dicom file of each serie, meta-data missing in dicomdir.
			 */
			LOG.info("importDicomZipFile step3 split instances into non-images and images ");
			imagesCreatorAndDicomFileAnalyzer.createImagesAndAnalyzeDicomFiles(patients, importJobDir.getAbsolutePath(),
					false);

			/**
			 * 4. STEP: create ImportJob
			 */
			LOG.info("importDicomZipFile step3 importJob {}", importJobDir.getAbsolutePath());
			ImportJob importJob = new ImportJob();
			importJob.setFromDicomZip(true);
			// Work folder is always relative to general import directory and userId (not
			// shown to outside world)
			importJob.setWorkFolder(importJobDir.getName());
			importJob.setPatients(patients);
			return new ResponseEntity<>(importJob, HttpStatus.OK);
		} catch (IOException e) {
			LOG.error(e.getMessage(), e);
			throw new RestServiceException(
					new ErrorModel(HttpStatus.UNPROCESSABLE_ENTITY.value(), ERROR_WHILE_SAVING_UPLOADED_FILE, null));
		}
	}

	private boolean isZipFileFromFile(final File file) {
		return file != null && file.getName().endsWith(ZIP_FILE_SUFFIX);
	}

	/**
	 * This method stores an uploaded zip file in a temporary file, creates a new
	 * folder with the same name and unzips the content into this folder, and gives
	 * back the folder with the content.
	 * 
	 * @param userImportDir
	 * @param dicomZipFile
	 * @return
	 * @throws IOException
	 * @throws RestServiceException
	 */
	private File saveTempFileCreateFolderAndUnzipFromFile(final File userImportDir, final File dicomZipFile)
			throws IOException, RestServiceException {
		File tempFile = saveTempFileFromFile(userImportDir, dicomZipFile);
		String fileName = tempFile.getName();
		int pos = fileName.lastIndexOf(FILE_POINT);
		if (pos > 0) {
			fileName = fileName.substring(0, pos);
		}
		File unzipFolderFile = new File(tempFile.getParentFile().getAbsolutePath() + File.separator + fileName);
		if (!unzipFolderFile.exists()) {
			unzipFolderFile.mkdirs();
		} else {
			throw new RestServiceException(new ErrorModel(HttpStatus.UNPROCESSABLE_ENTITY.value(),
					"Error while unzipping file: folder already exists.", null));
		}
		ImportUtils.unzip(tempFile.getAbsolutePath(), unzipFolderFile.getAbsolutePath());
		tempFile.delete();
		return unzipFolderFile;
	}

	/**
	 * This method takes a multipart file and stores it in a configured upload
	 * directory in relation with the userId with a random name and the suffix
	 * .upload
	 *
	 * @param file
	 * @throws IOException
	 */
	private File saveTempFileFromFile(final File userImportDir, final File file) throws IOException {
		long n = createRandomLong();
		File uploadFile = new File(userImportDir.getAbsolutePath(), Long.toString(n) + UPLOAD_FILE_SUFFIX);
		Files.move(file.toPath(), uploadFile.toPath());
		return uploadFile;
	}

	@Override
	/**
	 * This method load an EEG file, unzip it and load an import job with the
	 * informations collected
	 */
	public ResponseEntity<EegImportJob> uploadEEGZipFile(
			@ApiParam(value = "file detail") @RequestPart("file") final MultipartFile eegFile)
			throws RestServiceException {
		try {
			// Do some checks about the file, must be != null and must be a .zip file
			if (eegFile == null) {
				throw new RestServiceException(
						new ErrorModel(HttpStatus.UNPROCESSABLE_ENTITY.value(), NO_FILE_UPLOADED, null));
			}
			if (!isZipFile(eegFile)) {
				throw new RestServiceException(
						new ErrorModel(HttpStatus.UNPROCESSABLE_ENTITY.value(), WRONG_CONTENT_FILE_UPLOAD, null));
			}
			/**
			 * 1. STEP: Handle file management. Always create a userId specific folder in
			 * the import work folder (the root of everything): split imports to clearly
			 * separate them into separate folders for each user
			 */
			final Long userId = KeycloakUtil.getTokenUserId();
			final String userImportDirFilePath = importDir + File.separator + Long.toString(userId);
			final File userImportDir = new File(userImportDirFilePath);
			if (!userImportDir.exists()) {
				userImportDir.mkdirs(); // create if not yet existing
			}

			// Unzip the file and get the elements
			File importJobDir = saveTempFileCreateFolderAndUnzip(userImportDir, eegFile, false);

			EegImportJob importJob = new EegImportJob();
			importJob.setWorkFolder(importJobDir.getName());

			List<EegDataset> datasets = new ArrayList<>();

			File dataFileDir = new File(importJobDir.getAbsolutePath() + File.separator
					+ eegFile.getOriginalFilename().replace(".zip", ""));

			// Get .VHDR file
			File[] bvMatchingFiles = dataFileDir.listFiles(new FilenameFilter() {
				@Override
				public boolean accept(final File dir, final String name) {
					return name.endsWith("vhdr");
				}
			});

			// Get .edf file
			File[] edfMatchingFiles = dataFileDir.listFiles(new FilenameFilter() {
				@Override
				public boolean accept(final File dir, final String name) {
					return name.endsWith("edf");
				}
			});

			if (bvMatchingFiles != null && bvMatchingFiles.length > 0) {
				// Manage multiple vhdr files
				// read .vhdr files
				readBrainvisionFiles(bvMatchingFiles, dataFileDir, datasets);
			} else if (edfMatchingFiles != null && edfMatchingFiles.length > 0) {
				// read .edf files
				readEdfFiles(edfMatchingFiles, dataFileDir, datasets);
			} else {
				throw new RestServiceException(new ErrorModel(HttpStatus.UNPROCESSABLE_ENTITY.value(),
						"File does not contains a .vhdr or .edf file."));
			}

			importJob.setDatasets(datasets);

			return new ResponseEntity<>(importJob, HttpStatus.OK);
		} catch (IOException ioe) {
			throw new RestServiceException(ioe, new ErrorModel(HttpStatus.BAD_REQUEST.value(), "Invalid file"));
		} catch (ShanoirImportException e) {
			throw new RestServiceException(e, new ErrorModel(HttpStatus.BAD_REQUEST.value(), e.getMessage()));
		}
	}

	/**
	 * Reads a list of .edf files to generate a bunch of datasets.
	 * 
	 * @param datasets         the list of datasets to import
	 * @param dataFileDir      the file directory where we are working
	 * @param edfMatchingFiles the list of .edf files
	 * @throws ShanoirImportException when parsing fails
	 */
	private void readEdfFiles(final File[] edfMatchingFiles, final File dataFileDir, final List<EegDataset> datasets)
			throws ShanoirImportException {
		for (File edfFile : edfMatchingFiles) {

			// Parse the file
			try (FileInputStream edfStream = new FileInputStream(edfFile)) {
				EDFParserResult result = EDFParser.parseEDF(edfStream);

				// Create channels
				List<Channel> channels = new ArrayList<>();
				for (int i = 0; i < result.getHeader().getNumberOfChannels(); i++) {
					Channel chan = new Channel();
					Pattern p = Pattern.compile("HP:(\\d+)k?Hz\\sLP:(\\d+)k?Hz(\\sN:(\\d+)k?Hz)?");
					Matcher m = p.matcher(result.getHeader().getPrefilterings()[i].trim());
					if (m.matches()) {
						chan.setHighCutoff(Integer.parseInt(m.group(1)));
						chan.setLowCutoff(Integer.parseInt(m.group(2)));
						if (m.groupCount() > 2) {
							chan.setNotch(Integer.parseInt(m.group(4)));
						}
					}
					chan.setName(result.getHeader().getChannelLabels()[i].trim());
					chan.setReferenceUnits(result.getHeader().getDimensions()[i].trim());

					channels.add(chan);
				}

				double samplingfrequency = result.getHeader().getNumberOfRecords()
						/ result.getHeader().getDurationOfRecords();

				// Create events
				List<Event> events = new ArrayList<>();
				for (EDFAnnotation annotation : result.getAnnotations()) {
					Event event = new Event();

					// This is done by default
					event.setChannelNumber(0);
					event.setPosition(String.valueOf((float) (samplingfrequency / annotation.getOnSet())));
					event.setPoints((int) annotation.getDuration());
					events.add(event);
				}

				EegDataset dataset = new EegDataset();
				dataset.setEvents(events);
				dataset.setChannels(channels);
				dataset.setChannelCount(result.getHeader().getNumberOfChannels());

				// Get dataset name from EDF file name
				String fileNameWithOutExt = FilenameUtils.removeExtension(edfFile.getName());
				dataset.setName(fileNameWithOutExt);

				dataset.setSamplingFrequency((int) samplingfrequency);

				// Get the list of file to save from reader
				List<String> files = new ArrayList<>();

				File[] filesToSave = dataFileDir.listFiles(new FilenameFilter() {
					@Override
					public boolean accept(final File dir, final String name) {
						return name.startsWith(fileNameWithOutExt);
					}
				});
				for (File fi : filesToSave) {
					files.add(fi.getCanonicalPath());
				}
				dataset.setFiles(files);
				datasets.add(dataset);
			} catch (IOException e) {
				throw new ShanoirImportException("Error while parsing file. Please contact an amdinistrator", e);
			}
		}
	}

	/**
	 * Reads a list of .vhdr files to generate a bunch of datasets.
	 * 
	 * @param dataFileDir     the file directory where we are working
	 * @param bvMatchingFiles the list of vhdr files
	 * @param datasets        the list of datasets to import
	 * @return a list of datasets generated from the informations of the .vhdr files
	 * @throws ShanoirImportException when parsing fails
	 */
	private void readBrainvisionFiles(final File[] bvMatchingFiles, final File dataFileDir,
			final List<EegDataset> datasets) throws ShanoirImportException {
		for (File vhdrFile : bvMatchingFiles) {

			// Parse the file
			BrainVisionReader bvr = new BrainVisionReader(vhdrFile);

			EegDataset dataset = new EegDataset();
			dataset.setEvents(bvr.getEvents());
			dataset.setChannels(bvr.getChannels());
			dataset.setChannelCount(bvr.getNbchan());
			// Get dataset name from VHDR file name
			String fileNameWithOutExt = FilenameUtils.removeExtension(vhdrFile.getName());
			dataset.setName(fileNameWithOutExt);

			// Manage when we have a sampling interval but no sampling frequency
			int samplingFrequency = bvr.getSamplingFrequency();
			if (samplingFrequency == 0 && bvr.getSamplingIntervall() != 0) {
				samplingFrequency = Math.round(1000 / bvr.getSamplingIntervall());
			}

			dataset.setSamplingFrequency(samplingFrequency);
			dataset.setCoordinatesSystem(bvr.getHasPosition() ? "true" : null);

			try {
				bvr.close();
			} catch (IOException e) {
				throw new ShanoirImportException("Error while parsing file. Please contact an administrator.", e);
			}

			// Get the list of file to save from reader
			List<String> files = new ArrayList<>();

			File[] filesToSave = dataFileDir.listFiles(new FilenameFilter() {
				@Override
				public boolean accept(final File dir, final String name) {
					return name.startsWith(fileNameWithOutExt);
				}
			});
			try {
				for (File fi : filesToSave) {
					files.add(fi.getCanonicalPath());

				}
			} catch (IOException e) {
				throw new ShanoirImportException("Error while parsing file. Please contact an administrator.", e);
			}
			dataset.setFiles(files);
			datasets.add(dataset);
		}
	}

	/**
	 * Here we had all the informations we needed (metadata, examination, study,
	 * subject, ect...) so we make a call to dataset API to create it.
	 */
	@Override
	public ResponseEntity<Void> startImportEEGJob(
			@ApiParam(value = "EegImportJob", required = true) @Valid @RequestBody final EegImportJob importJob) {
		// Comment: Anonymisation is not necessary for pure brainvision EEGs data
		// For .EDF, anonymisation could be done here.
		// Comment: BIDS translation will be done during export and not during import.

		// HttpEntity represents the request
		final HttpEntity<EegImportJob> requestBody = new HttpEntity<>(importJob, KeycloakUtil.getKeycloakHeader());
		// Post to dataset MS to finish import and create associated datasets
		ResponseEntity<String> response = restTemplate.exchange(datasetsMsUrl, HttpMethod.POST, requestBody,
				String.class);
		return new ResponseEntity<>(response.getStatusCode());
	}

	@Override
	public ResponseEntity<String> createTempDir() throws RestServiceException {
		final File userImportDir = getUserImportDir();
		long n = createRandomLong();
		File tempDirForImport = new File(userImportDir, Long.toString(n));
		if (!tempDirForImport.exists()) {
			tempDirForImport.mkdirs();
		} else {
			throw new RestServiceException(new ErrorModel(HttpStatus.UNPROCESSABLE_ENTITY.value(),
					"Error while creating temp dir: random number generated twice?", null));
		}
		return new ResponseEntity<String>(tempDirForImport.getName(), HttpStatus.OK);
	}

	private File getUserImportDir() {
		final Long userId = KeycloakUtil.getTokenUserId();
		final String userImportDirFilePath = importDir + File.separator + Long.toString(userId);
		final File userImportDir = new File(userImportDirFilePath);
		if (!userImportDir.exists()) {
			userImportDir.mkdirs(); // create if not yet existing
		} // else is wanted case, user has already its import directory
		return userImportDir;
	}

	@Override
	public ResponseEntity<Void> uploadFile(@PathVariable("tempDirId") String tempDirId,
			@RequestParam("file") MultipartFile file) throws RestServiceException, IOException {
		final File userImportDir = getUserImportDir();
		final File importJobDir = new File(userImportDir, tempDirId);
		// only continue in case of existing temp dir id
		if (importJobDir.exists()) {
			File fileToWrite = new File(importJobDir, file.getOriginalFilename());
			if (fileToWrite.exists()) {
				throw new RestServiceException(new ErrorModel(HttpStatus.UNPROCESSABLE_ENTITY.value(),
						"Duplicate file name in tempDir, could not create file as file exists already.", null));
			} else {
				byte[] bytes = file.getBytes();
				Files.write(fileToWrite.toPath(), bytes);
			}
		} else {
			throw new RestServiceException(new ErrorModel(HttpStatus.UNPROCESSABLE_ENTITY.value(),
					"Upload file called with not existing tempDirId.", null));
		}
		return null;
	}

	@Override
	public ResponseEntity<Void> startImport(@RequestBody Exchange exchange)
			throws RestServiceException, FileNotFoundException, IOException {
		// 1. Check if uploaded data are complete (to be done a little later)
		final File userImportDir = getUserImportDir();
		final File tempDir = new File(userImportDir, exchange.getTempDirId());

		final File dicomDir = new File(tempDir, DICOMDIR);
		if (!dicomDir.exists()) {
			dicomDirGeneratorService.generateDicomDirFromDirectory(dicomDir, tempDir);
			LOG.info("DICOMDIR generated at path: " + dicomDir.getAbsolutePath());
		}

		/**
		 * 2. STEP: prepare patients list to be put into ImportJob: read DICOMDIR and
		 * complete with meta-data from files
		 */
		ImportJob importJob = new ImportJob();
		List<Patient> patients = preparePatientsForImportJob(tempDir);
		importJob.setPatients(patients);
		importJob.setFromDicomZip(true);
		importJob.setAnonymisationProfileToUse(exchange.getAnonymisationProfileToUse());
		// Work folder is always relative to general import directory and userId (not
		// shown to outside world)
		importJob.setWorkFolder(tempDir.getAbsolutePath());
		/**
		 * Handle Study and StudyCard settings:
		 */
		ExStudy exStudy = exchange.getExStudy();
		if (exStudy != null && exStudy.getStudyId() != null) {
			importJob.setFrontStudyId(exStudy.getStudyId());
			ExStudyCard exStudyCard = exStudy.getExStudyCards().get(0);
			importJob.setStudyCardName(exStudyCard.getName());
			int i = 0;
			List<ExSubject> exSubjects = exStudy.getExSubjects();
			for (Iterator<ExSubject> iterator = exSubjects.iterator(); iterator.hasNext();) {
				ExSubject exSubject = iterator.next();
				Subject subject = new Subject();
				subject.setId(exSubject.getSubjectId());
				subject.setName(exSubject.getSubjectName());
				patients.get(i).setSubject(subject);
				if (exSubject != null && exSubject.getSubjectName() != null) {
					List<ExExamination> exExaminations = exSubject.getExExaminations();
					for (Iterator<ExExamination> iterator2 = exExaminations.iterator(); iterator2.hasNext();) {
						ExExamination exExamination = iterator2.next();
						// @TODO: adapt ImportJob later for multiple-exams
						importJob.setExaminationId(exExamination.getId());
					}
				} else {
					// handle creation of subject and exams later here
				}
				i++;
			}
		} else {
			// handle creation of study and study cards later here
		}
		final Long userId = KeycloakUtil.getTokenUserId();
		importerManagerService.manageImportJob(userId, KeycloakUtil.getKeycloakHeader(), importJob);
		return null;
	}

	/**
	 * This methods import a bunch of datasets from a Shanoir Exchange Format (based
	 * on BIDS format)
	 * 
	 * @param bidsFile
	 *            the file
	 * @throws ShanoirException
	 *             when something gets wrong during the import
	 * @throws IOException
	 *             when IO fails
	 * @throws RestServiceException
	 */
	@Override
	public ResponseEntity<ImportJob> importAsBids(
			@ApiParam(value = "file detail") @RequestPart("file") final MultipartFile bidsFile)
			throws RestServiceException, ShanoirException, IOException {
		// Check that the file is not null and well zpiped
		try {
		if (bidsFile == null) {
			throw new RestServiceException(
					new ErrorModel(HttpStatus.UNPROCESSABLE_ENTITY.value(), NO_FILE_UPLOADED, null));
		}
		if (!isZipFile(bidsFile)) {
			// .SEF ?
			throw new RestServiceException(
					new ErrorModel(HttpStatus.UNPROCESSABLE_ENTITY.value(), WRONG_CONTENT_FILE_UPLOAD, null));
		}
		// Todo: what if we are coming from SHUP ?

		// Create tmp folder and unzip archive
		final File userImportDir = getUserImportDir();
		File importJobDir = saveTempFileCreateFolderAndUnzip(userImportDir, bidsFile, false);
		// Deserialize participants.tsv => Do a call to studies API to create
		// corresponding subjects
		File participantsFile = new File(importJobDir.getAbsolutePath() + "/participants.tsv");
		if (!participantsFile.exists()) {
			throw new ShanoirException("participants.tsv file is mandatory");
		}

		ObjectMapper mapper = new ObjectMapper();

		SimpleModule module = new SimpleModule();
		module.addAbstractTypeMapping(StudyUserInterface.class, StudyUser.class);
		mapper.registerModule(module);
		// Here we wait for the response => to be sure that the subjects are created
		String participantString = (String) rabbitTemplate.convertSendAndReceive(RabbitMQConfiguration.SUBJECTS_EXCHANGE, "", participantsFile.getAbsolutePath());
		List<IdName> participants = Arrays.asList(mapper.readValue(participantString, IdName[].class));
		// If we receive a unique subject with no ID => It's an error
		if (participants.size() == 1 && participants.get(0).getId() == null) {
			throw new ShanoirException(participants.get(0).getName());
		}

		File studyDescriptionFile = new File(importJobDir.getAbsolutePath() + "/dataset_description.json");
		if (!studyDescriptionFile.exists()) {
			throw new ShanoirException("studyDescriptionFile file is mandatory");
		}

		// Then import data
		File sourceData = new File(importJobDir.getAbsolutePath() + "/sourcedata");
		if (!sourceData.exists()) {
			throw new ShanoirException("sourcedata folder is mandatory");
		}

		// 2) Import Datasets
		File[] subjectFiles = sourceData.listFiles(new FilenameFilter() {
			@Override
			public boolean accept(File dir, String name) {
				return name.startsWith("sub-");
			}
		});
		ImportJob job = null;

		for (File subjFile : subjectFiles) {
			// Get subjectName
			String subjectName = subjFile.getName().substring("sub-".length());

			// Read shanoirImportFile
			File shanoirImportFile = new File(subjFile.getAbsolutePath() + "/shanoir-import.json");

			if (!shanoirImportFile.exists()) {
				throw new ShanoirException("shanoir-import.json file is mandatory in subject folder");
			}

			ObjectMapper objectMapper = new ObjectMapper();
			ImportJob sid = objectMapper.readValue(shanoirImportFile, ImportJob.class);
			CommonIdsDTO idsDTO = new CommonIdsDTO(null, sid.getFrontStudyId(), null,
					sid.getFrontAcquisitionEquipmentId());
			final HttpEntity<CommonIdsDTO> requestBody = new HttpEntity<>(idsDTO, KeycloakUtil.getKeycloakHeader());
			// Post to dataset MS to finish import and create associated datasets
			ResponseEntity<CommonIdNamesDTO> response = restTemplate.exchange(studiesCommonMsUrl, HttpMethod.POST,
					requestBody, CommonIdNamesDTO.class);
			// Check that equipement exists
			// Check that study exists
			// All in one with studies MS CommonsApi
			// This is not necessary if we further use the studyCard
			if (response.getBody().getEquipement() == null) {
				throw new ShanoirException(
						"Equipement with ID " + sid.getFrontAcquisitionEquipmentId() + " does not exists.");
			}
			if (response.getBody().getStudy() == null) {
				throw new ShanoirException("Study with ID " + sid.getFrontStudyId() + " does not exists.");
			}
			// Subject based on folder name
			Long subjectId = getSubjectIdByName(subjectName, participants);
			if (subjectId == null) {
				throw new ShanoirException(
						"Subject " + subjectName + " could not be created. Please check participants.tsv file.");
			}

			// If there is no DICOMDIR: create it
			File dicomDir = new File(subjFile.getAbsolutePath() + "/DICOM/DICOMDIR");
			if (!dicomDir.exists()) {
				DicomDirCreator creator = new DicomDirCreator(subjFile.getAbsolutePath() + "/DICOMDIR",
						subjFile.getAbsolutePath() + "/DICOM");
				creator.start();
			}
			// Zip data folders to be able to call ImporterAPIController.uploadDicomZipFile
			FileOutputStream fos = new FileOutputStream(subjFile.getAbsolutePath() + ".zip");
			ZipOutputStream zipOut = new ZipOutputStream(fos);

			ImportUtils.zipFile(subjFile, subjFile.getName(), zipOut, true);

			zipOut.close();
			fos.close();
			MockMultipartFile multiPartFile = new MockMultipartFile(subjFile.getName(), subjFile.getName() + ".zip",
					APPLICATION_ZIP, new FileInputStream(subjFile.getAbsolutePath() + ".zip"));

			// Send data folder to import API and get import job
			ResponseEntity<ImportJob> entity = this.uploadDicomZipFile(multiPartFile);

			// Complete ImportJob to use startImportJob
			job = entity.getBody();

			// Construire l'arborescence
			job.setFrontAcquisitionEquipmentId(sid.getFrontAcquisitionEquipmentId());
			job.setFrontStudyId(sid.getFrontStudyId());

			job.setFromPacs(false);
			job.setFromShanoirUploader(false);
			job.setFromDicomZip(true);
			for (Patient pat : job.getPatients()) {
				pat.setPatientName(subjectName);
				Subject subject = new Subject();
				subject.setId(subjectId);
				subject.setName(subjectName);
				pat.setSubject(subject);

				// Select all series to be imported
				for (Study study : pat.getStudies()) {
					for (Serie serie : study.getSeries()) {
						serie.setSelected(Boolean.TRUE);
					}
				}
			}

			// Create a new examination if not existing
			if (sid.getExaminationId() == null || sid.getExaminationId().equals(Long.valueOf(0l))) {
				// Create examination => We actually need its ID so do a direct API call
				ExaminationDTO examDTO = new ExaminationDTO();
				// Construct DTO
				examDTO.setCenter(new IdName(Long.valueOf(1), null));
				examDTO.setPreclinical(false); // Pour le moment on fait que du DICOM
				examDTO.setStudy(new IdName(sid.getFrontStudyId(), response.getBody().getStudy().getName()));
				examDTO.setSubject(new IdName(subjectId, subjectName));
				examDTO.setExaminationDate(job.getPatients().get(0).getStudies().get(0).getStudyDate());
				examDTO.setComment(job.getPatients().get(0).getStudies().get(0).getStudyDescription());

				final HttpEntity<ExaminationDTO> requestBodyExam = new HttpEntity<>(examDTO,
						KeycloakUtil.getKeycloakHeader());
				ResponseEntity<ExaminationDTO> examResponse = restTemplate.exchange(createExaminationMsUrl,
						HttpMethod.POST, requestBodyExam, ExaminationDTO.class);
				job.setExaminationId(examResponse.getBody().getId());
			}
			// Next API call => StartImportJob
			ResponseEntity<Void> result = this.startImportJob(job);
			if (!result.getStatusCode().equals(HttpStatus.OK)) {
				throw new ShanoirException("Error while importing subject: " + subjectName);
			}
		}
		// TODO ONE DAY: Copy "other" files to the bids folder
		// Copy non datasets elements
		// Don't copy "data" folder
		// Don't copy examination_description.json
		// copy /sourceData??, /code and / files (readme, changes, participants.tsv,
		// participants.json, etc..)
		return new ResponseEntity<>(job, HttpStatus.OK);

		} catch (Exception e) {
			System.err.println("Coucou" + e + e.getMessage() + e.getStackTrace());
			throw e;
		}
	}

	/**
	 * Get the ID of a subject from its name and a list of subject
	 * 
	 * @param name
	 *            the name of the subject to find
	 * @param subjects
	 *            the list of subjects to supply
	 * @return the ID of the subject corresponding to the name, null otherwise
	 */
	public Long getSubjectIdByName(String name, List<IdName> subjects) {
		for (IdName sub : subjects) {
			if (sub.getName().equals(name)) {
				return sub.getId();
			}
		}
		return null;
	}

}

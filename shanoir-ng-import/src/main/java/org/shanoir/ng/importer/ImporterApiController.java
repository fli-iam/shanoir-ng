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
import java.io.FilenameFilter;
import java.io.IOException;
import java.nio.file.Files;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.validation.Valid;

import org.apache.commons.io.FilenameUtils;
import org.shanoir.ng.importer.dicom.DicomDirToModelService;
import org.shanoir.ng.importer.dicom.ImagesCreatorAndDicomFileAnalyzerService;
import org.shanoir.ng.importer.dicom.ImportJobConstructorService;
import org.shanoir.ng.importer.dicom.query.DicomQuery;
import org.shanoir.ng.importer.dicom.query.QueryPACSService;
import org.shanoir.ng.importer.eeg.brainvision.BrainVisionReader;
import org.shanoir.ng.importer.eeg.edf.EDFAnnotation;
import org.shanoir.ng.importer.eeg.edf.EDFParser;
import org.shanoir.ng.importer.eeg.edf.EDFParserResult;
import org.shanoir.ng.importer.model.Channel;
import org.shanoir.ng.importer.model.EegDataset;
import org.shanoir.ng.importer.model.EegImportJob;
import org.shanoir.ng.importer.model.Event;
import org.shanoir.ng.importer.model.ImportJob;
import org.shanoir.ng.importer.model.Patient;
import org.shanoir.ng.shared.exception.ErrorModel;
import org.shanoir.ng.shared.exception.ImportErrorModelCode;
import org.shanoir.ng.shared.exception.RestServiceException;
import org.shanoir.ng.shared.exception.ShanoirException;
import org.shanoir.ng.utils.ImportUtils;
import org.shanoir.ng.utils.KeycloakUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.swagger.annotations.ApiParam;

/**
 * This is the main component of the import of Shanoir-NG.
 * The front-end in Angular only communicates with this service.
 * The import ms itself is calling the ms datasets service.
 * 
 * @author mkain
 *
 */
@Controller
public class ImporterApiController implements ImporterApi {

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
	
	@Value("${shanoir.import.directory}")
	private String importDir;
	
	@Autowired
	private RestTemplate restTemplate;
	
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
	
	public ResponseEntity<Void> uploadFiles(
			@ApiParam(value = "file detail") @RequestPart("files") final MultipartFile[] files) throws RestServiceException {
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
		if (dicomZipFile == null) {
			throw new RestServiceException(
					new ErrorModel(HttpStatus.UNPROCESSABLE_ENTITY.value(), NO_FILE_UPLOADED, null));
		}
		if (!isZipFile(dicomZipFile)) {
			throw new RestServiceException(new ErrorModel(HttpStatus.UNPROCESSABLE_ENTITY.value(),
					"Wrong content type of file upload, .zip required.", null));
		}
		try {
			/**
			 * 1. STEP: Handle file management.
			 * Always create a userId specific folder in the import work folder (the root of everything):
			 * split imports to clearly separate them into separate folders for each user
			 */
			final Long userId = KeycloakUtil.getTokenUserId();
			final String userImportDirFilePath = importDir + File.separator + Long.toString(userId);
			final File userImportDir = new File(userImportDirFilePath);
			if (!userImportDir.exists()) {
				userImportDir.mkdirs(); // create if not yet existing
			}
			File importJobDir = saveTempFileCreateFolderAndUnzip(userImportDir, dicomZipFile, true);
	
			/**
			 * 2. STEP: read DICOMDIR and create Shanoir model from it (== Dicom model):
			 * Patient - Study - Serie - Instance
			 */
			List<Patient> patients = null;
			File dicomDirFile = new File(importJobDir.getAbsolutePath() + File.separator + DICOMDIR);
			if (dicomDirFile.exists()) {
				patients = dicomDirToModel.readDicomDirToPatients(dicomDirFile);
			}
			/**
			 * 3. STEP: split instances into non-images and images and get additional meta-data
			 * from first dicom file of each serie, meta-data missing in dicomdir.
			 */
			imagesCreatorAndDicomFileAnalyzer.createImagesAndAnalyzeDicomFiles(patients, importJobDir.getAbsolutePath(), false);
	
			/**
			 * 4. STEP: create ImportJob
			 */
			ImportJob importJob = new ImportJob();
			importJob.setFromDicomZip(true);
			// Work folder is always relative to general import directory and userId (not shown to outside world)
			importJob.setWorkFolder(File.separator + importJobDir.getAbsolutePath());
			importJob.setPatients(patients);
			return new ResponseEntity<>(importJob, HttpStatus.OK);
		} catch (IOException e) {
			LOG.error(e.getMessage(), e);
			throw new RestServiceException(
					new ErrorModel(HttpStatus.UNPROCESSABLE_ENTITY.value(), ERROR_WHILE_SAVING_UPLOADED_FILE, null));
		}
	}

	@Override
	public ResponseEntity<Void> uploadDicomZipFileFromShup(@ApiParam(value = "file detail") @RequestPart("file") final MultipartFile dicomZipFile)
			throws RestServiceException, ShanoirException {
		if (dicomZipFile == null) {
			throw new RestServiceException(
					new ErrorModel(HttpStatus.UNPROCESSABLE_ENTITY.value(), NO_FILE_UPLOADED, null));
		}
		try {
			final Long userId = KeycloakUtil.getTokenUserId();
			final String userImportDirFilePath = importDir + File.separator + Long.toString(userId);
			final File userImportDir = new File(userImportDirFilePath);
			if (!userImportDir.exists()) {
				userImportDir.mkdirs(); // create if not yet existing
			}
			File importJobDir = saveTempFileCreateFolderAndUnzip(userImportDir, dicomZipFile, true);
			File importJobFile = new File(importJobDir.getAbsolutePath() + File.separator + IMPORTJOB);
			ImportJob importJob = null;
			if (importJobFile.exists()) {
				ObjectMapper objectMapper = new ObjectMapper();
				try {
					importJob = objectMapper.readValue(importJobFile, ImportJob.class);
					importJob = importJobConstructorService.reconstructImportJob(importJob, importJobDir);
					LOG.warn(objectMapper.writeValueAsString(importJob));
				} catch (IOException ioe) {
					LOG.error(ioe.getMessage(), ioe);
					throw new RestServiceException(new ErrorModel(HttpStatus.UNPROCESSABLE_ENTITY.value(),
							"Error while mapping importJob.json file to object.", null));
				}
			} else {
				throw new RestServiceException(new ErrorModel(HttpStatus.UNPROCESSABLE_ENTITY.value(),
						"Error missing importJob.json in upload from ShUp.", null));
			}
			importJob.setFromShanoirUploader(true);
			importJob.setWorkFolder(File.separator + importJobDir.getName());
			importerManagerService.manageImportJob(userId, KeycloakUtil.getKeycloakHeader(), importJob);
		} catch (IOException e) {
			LOG.error(e.getMessage());
			throw new RestServiceException(
					new ErrorModel(HttpStatus.UNPROCESSABLE_ENTITY.value(), ERROR_WHILE_SAVING_UPLOADED_FILE, null));
		} catch (RestClientException e) {
			LOG.error("Error on dataset microservice request", e);
			throw new ShanoirException("Error while sending import job", ImportErrorModelCode.SC_MS_COMM_FAILURE);
		}
		return new ResponseEntity<>(HttpStatus.OK);
	}

	@Override
	public ResponseEntity<Void> startImportJob( @ApiParam(value = "ImportJob", required = true) @Valid @RequestBody final ImportJob importJob)
			throws RestServiceException {
		try {
			final Long userId = KeycloakUtil.getTokenUserId();
			importerManagerService.manageImportJob(userId, KeycloakUtil.getKeycloakHeader(), importJob);
			return new ResponseEntity<>(HttpStatus.OK);
		} catch (Exception e) {
			throw new RestServiceException(new ErrorModel(HttpStatus.UNPROCESSABLE_ENTITY.value(),
					e.getMessage(), null));
		}
	}
	
	@Override
	public ResponseEntity<ImportJob> queryPACS( @ApiParam(value = "DicomQuery", required = true) @Valid @RequestBody final DicomQuery dicomQuery)
			throws RestServiceException {
		ImportJob importJob;
		try {
			importJob = queryPACSService.queryCFIND(dicomQuery);
			// the pacs workfolder is empty here, as multiple queries could be asked before string an import
			importJob.setWorkFolder("");
			importJob.setFromPacs(true);
		} catch (ShanoirException e) {
			throw new RestServiceException(
					new ErrorModel(HttpStatus.UNPROCESSABLE_ENTITY.value(),
							e.getMessage(), null));
		}
		if (importJob.getPatients() == null || importJob.getPatients().isEmpty()) {
			return new ResponseEntity<>(HttpStatus.NO_CONTENT);
		}
		return new ResponseEntity<>(importJob, HttpStatus.OK);
	}

	/**
	 * This method takes a multipart file and stores it in a configured upload
	 * directory in relation with the userId with a random name and the suffix .upload
	 *
	 * @param file
	 * @throws IOException
	 */
	private File saveTempFile(final File userImportDir, final MultipartFile file) throws IOException {
		long n = createRandomLong();
		File uploadFile = new File(userImportDir.getAbsolutePath(), Long.toString(n) + UPLOAD_FILE_SUFFIX);
		byte[] bytes = file.getBytes();
		Files.write(uploadFile.toPath(), bytes);
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
	 * This method stores an uploaded zip file in a temporary file, creates a new folder with the same
	 * name and unzips the content into this folder, and gives back the folder with the content.
	 * 
	 * @param userImportDir
	 * @param dicomZipFile
	 * @return
	 * @throws IOException
	 * @throws RestServiceException
	 */
	private File saveTempFileCreateFolderAndUnzip(final File userImportDir, final MultipartFile dicomZipFile, final boolean fromDicom) throws IOException, RestServiceException {
		File tempFile = saveTempFile(userImportDir, dicomZipFile);
		if (fromDicom && !ImportUtils.checkZipContainsFile(DICOMDIR, tempFile)) {
			throw new RestServiceException(new ErrorModel(HttpStatus.UNPROCESSABLE_ENTITY.value(),
					"DICOMDIR is missing in .zip file.", null));
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
		return unzipFolderFile;
	}

	/**
	 * Check if sent file is of type .zip.
	 *
	 * @param file
	 */
	private boolean isZipFile(final MultipartFile file) {
		return file.getContentType().equals(APPLICATION_ZIP) || file.getContentType().equals(APPLICATION_OCTET_STREAM)
				|| file.getOriginalFilename().endsWith(ZIP_FILE_SUFFIX);
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
			throw new RestServiceException(new ErrorModel(HttpStatus.UNPROCESSABLE_ENTITY.value(),
					"Wrong content type of file upload, .zip required.", null));
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
			importJob.setWorkFolder(File.separator + importJobDir.getAbsolutePath());
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
	 * This method stores an uploaded zip file in a temporary file, creates a new folder with the same
	 * name and unzips the content into this folder, and gives back the folder with the content.
	 * 
	 * @param userImportDir
	 * @param dicomZipFile
	 * @return
	 * @throws IOException
	 * @throws RestServiceException
	 */
	private File saveTempFileCreateFolderAndUnzipFromFile(final File userImportDir, final File dicomZipFile) throws IOException, RestServiceException {
		File tempFile = saveTempFileFromFile(userImportDir, dicomZipFile);
		if (!ImportUtils.checkZipContainsFile(DICOMDIR, tempFile)) {
			throw new RestServiceException(new ErrorModel(HttpStatus.UNPROCESSABLE_ENTITY.value(),
					"DICOMDIR is missing in .zip file.", null));
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
		return unzipFolderFile;
	}
	
	/**
	 * This method takes a multipart file and stores it in a configured upload
	 * directory in relation with the userId with a random name and the suffix .upload
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
	 * This method load an EEG file, unzip it and load an import job with the informations collected
	 */
	public ResponseEntity<EegImportJob> uploadEEGZipFile(@ApiParam(value = "file detail") @RequestPart("file") final MultipartFile eegFile)
			throws RestServiceException {
		try {
			// Do some checks about the file, must be != null and must be a .zip file
			if (eegFile == null) {
				throw new RestServiceException(new ErrorModel(HttpStatus.UNPROCESSABLE_ENTITY.value(), NO_FILE_UPLOADED, null));
			}
			if (!isZipFile(eegFile)) {
				throw new RestServiceException(new ErrorModel(HttpStatus.UNPROCESSABLE_ENTITY.value(),"Wrong content type of file upload, .zip or .edf required.", null));
			}
			/**
			 * 1. STEP: Handle file management.
			 * Always create a userId specific folder in the import work folder (the root of everything):
			 * split imports to clearly separate them into separate folders for each user
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
			importJob.setWorkFolder(importJobDir.getAbsolutePath());

			List<EegDataset> datasets = new ArrayList<>();

			File dataFileDir = new File(importJobDir.getAbsolutePath() + File.separator + eegFile.getOriginalFilename().replace(".zip", ""));

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
				throw new RestServiceException(new ErrorModel(HttpStatus.UNPROCESSABLE_ENTITY.value(), "File does not contains a .vhdr or .edf file.", null));
			}

			importJob.setDatasets(datasets);
			
			return new ResponseEntity<>(importJob, HttpStatus.OK);
		} catch (IOException ioe) {
			throw new RestServiceException(ioe, new ErrorModel(HttpStatus.BAD_REQUEST.value(), "Invalid file"));
		}
	}

	/**
	 * Reads a list of .edf files to generate a bunch of datasets.
	 * @param datasets the list of datasets to import
	 * @param dataFileDir the file directory where we are working
	 * @param edfMatchingFiles the list of .edf files
	 * @throws IOException when parsing fails
	 */
	private void readEdfFiles(final File[] edfMatchingFiles, final File dataFileDir, final List<EegDataset> datasets) throws IOException {
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
				
				double samplingfrequency = result.getHeader().getNumberOfRecords() / result.getHeader().getDurationOfRecords();

				// Create events
				List<Event> events = new ArrayList<>();
				for (EDFAnnotation annotation : result.getAnnotations()) {
					Event event = new Event();
					
					// This is done by default
					event.setChannelNumber(0);
					event.setPosition(String.valueOf((float)(samplingfrequency / annotation.getOnSet())));
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
				
				dataset.setSamplingFrequency((int)samplingfrequency);
				
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
			}
		}
	}

	/**
	 * Reads a list of .vhdr files to generate a bunch of datasets.
	 * @param dataFileDir the file directory where we are working
	 * @param bvMatchingFiles  the list of vhdr files
	 * @param datasets the list of datasets to import
	 * @return a list of datasets generated from the informations of the .vhdr files
	 * @throws IOException when parsing fails
	 */
	private void readBrainvisionFiles(final File[] bvMatchingFiles, final File dataFileDir, final List<EegDataset> datasets) throws IOException {
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
				samplingFrequency = Math.round(1000/bvr.getSamplingIntervall());
			}

			dataset.setSamplingFrequency(samplingFrequency);
			dataset.setCoordinatesSystem(bvr.getHasPosition()? "true" : null);

			bvr.close();
			
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
		}
	}

	/**
	 * Here we had all the informations we needed (metadata, examination, study, subject, ect...) so we make a call to dataset API to create it.
	 */
	@Override
	public ResponseEntity<Void> startImportEEGJob( @ApiParam(value = "EegImportJob", required = true) @Valid @RequestBody final EegImportJob importJob) {
		// Comment: Anonymisation is not necessary for pure brainvision EEGs data
		// For .EDF, anonymisation could be done here.
		// Comment: BIDS translation will be done during export and not during import.

		// HttpEntity represents the request
		final HttpEntity<EegImportJob> requestBody = new HttpEntity<>(importJob, KeycloakUtil.getKeycloakHeader());
		// Post to dataset MS to finish import and create associated datasets
		ResponseEntity<String> response = restTemplate.exchange(datasetsMsUrl, HttpMethod.POST, requestBody, String.class);
		return new ResponseEntity<>(response.getStatusCode());
	}

}

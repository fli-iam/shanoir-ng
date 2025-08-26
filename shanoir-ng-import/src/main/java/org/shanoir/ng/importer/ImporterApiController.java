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

import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.v3.oas.annotations.Parameter;
import jakarta.validation.Valid;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.shanoir.ng.importer.dicom.DicomDirGeneratorService;
import org.shanoir.ng.importer.dicom.DicomDirToModelService;
import org.shanoir.ng.importer.dicom.ImagesCreatorAndDicomFileAnalyzerService;
import org.shanoir.ng.importer.dicom.query.DicomQuery;
import org.shanoir.ng.importer.dicom.query.QueryPACSService;
import org.shanoir.ng.importer.dto.ExaminationDTO;
import org.shanoir.ng.importer.eeg.brainvision.BrainVisionReader;
import org.shanoir.ng.importer.eeg.edf.EDFAnnotation;
import org.shanoir.ng.importer.eeg.edf.EDFParser;
import org.shanoir.ng.importer.eeg.edf.EDFParserResult;
import org.shanoir.ng.importer.model.*;
import org.shanoir.ng.shared.configuration.RabbitMQConfiguration;
import org.shanoir.ng.shared.core.model.IdName;
import org.shanoir.ng.shared.event.ShanoirEvent;
import org.shanoir.ng.shared.event.ShanoirEventService;
import org.shanoir.ng.shared.event.ShanoirEventType;
import org.shanoir.ng.shared.exception.ErrorModel;
import org.shanoir.ng.shared.exception.RestServiceException;
import org.shanoir.ng.shared.exception.ShanoirException;
import org.shanoir.ng.shared.exception.ShanoirImportException;
import org.shanoir.ng.utils.ImportUtils;
import org.shanoir.ng.utils.KeycloakUtil;
import org.shanoir.ng.utils.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.Files;
import java.time.LocalDate;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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

	private static final String DICOMDIR = "DICOMDIR";

	private static final String APPLICATION_ZIP = "application/zip";

	/** The Constant KB. */
	private static final int KB = 1024;

	/** The Constant BUFFER_SIZE. */
	private static final int BUFFER_SIZE = 10 * KB;

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
	private DicomDirGeneratorService dicomDirGeneratorService;

	@Autowired
	private DicomDirToModelService dicomDirToModel;

	@Autowired
	private ImagesCreatorAndDicomFileAnalyzerService imagesCreatorAndDicomFileAnalyzer;

	@Autowired
	private ImporterManagerService importerManagerService;

	@Autowired
	private QueryPACSService queryPACSService;

	@Autowired
	private RabbitTemplate rabbitTemplate;

	@Autowired
	private ObjectMapper objectMapper;

	@Autowired
	private ShanoirEventService eventService;

	@Override
	public ResponseEntity<ImportJob> uploadDicomZipFile(
			@Parameter(name = "file detail") @RequestPart("file") final MultipartFile dicomZipFile)
					throws RestServiceException {
		if (dicomZipFile == null || !ImportUtils.isZipFile(dicomZipFile)) {
			throw new RestServiceException(
					new ErrorModel(HttpStatus.UNPROCESSABLE_ENTITY.value(), WRONG_CONTENT_FILE_UPLOAD, null));
		}
		if (!ImportUtils.isZipFile(dicomZipFile)) {
			throw new RestServiceException(new ErrorModel(HttpStatus.UNPROCESSABLE_ENTITY.value(),
					"Wrong content type of file upload, .zip required.", null));
		}
		File tempFile = null;
		try {
			/**
			 * 1. STEP: Handle file management. Always create a userId specific folder in
			 * the import work folder (the root of everything): split imports to clearly
			 * separate them into separate folders for each user
			 */
			File userImportDir = ImportUtils.getUserImportDir(importDir);
			boolean createDicomDir = false;
			tempFile = ImportUtils.saveTempFile(userImportDir, dicomZipFile);
			if (!ImportUtils.checkZipContainsFile(DICOMDIR, tempFile)) {
				createDicomDir = true;
			}

			File importJobDir = ImportUtils.saveTempFileCreateFolderAndUnzip(tempFile, dicomZipFile, true);

			if (createDicomDir) {
				LOG.info("DICOMDIR missing from zip file, generating one.");
				final File dicomDir = new File(importJobDir, DICOMDIR);
				if (!dicomDir.exists()) {
					dicomDirGeneratorService.generateDicomDirFromDirectory(dicomDir, importJobDir);
					LOG.info("DICOMDIR generated at path: {}", dicomDir.getAbsolutePath());
				}
			}

			/**
			 * 2. STEP: prepare patients list to be put into ImportJob: read DICOMDIR and
			 * complete with meta-data from files
			 */
			List<Patient> patients = preparePatientsForImportJob(importJobDir);

			/**
			 * 3. STEP: split instances into non-images and images and get additional meta-data
			 * from first dicom file of each serie, meta-data missing in dicomdir.
			 */
			imagesCreatorAndDicomFileAnalyzer.createImagesAndAnalyzeDicomFiles(patients, importJobDir.getAbsolutePath(), false, null, false);

			/**
			 * . STEP: create ImportJob
			 */
			ImportJob importJob = new ImportJob();
			importJob.setFromDicomZip(true);
			importJob.setUserId(KeycloakUtil.getTokenUserId());
			// Work folder is always relative to general import directory
			importJob.setWorkFolder(importJobDir.getName());
			importJob.setPatients(patients);
			return new ResponseEntity<>(importJob, HttpStatus.OK);
		} catch (Exception e) {
			// If there is an exception, we should delete the temporary folder
			if (tempFile != null) {
				FileUtils.deleteQuietly(tempFile);
			}
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
			@Parameter(name = "ImportJob", required = true) @Valid @RequestBody final ImportJob importJob)
					throws RestServiceException {
		File userImportDir = ImportUtils.getUserImportDir(importDir);
		final Long userId = KeycloakUtil.getTokenUserId();
		importJob.setUserId(userId);
		String tempDirId = importJob.getWorkFolder();
		final File importJobDir = new File(userImportDir, tempDirId);
		if (importJobDir.exists()) {
			importJob.setWorkFolder(importJobDir.getAbsolutePath());
			LOG.info("Starting import job for user {} (userId: {}) with import job folder: {}", KeycloakUtil.getTokenUserName(), userId, importJob.getWorkFolder());
			importerManagerService.manageImportJob(importJob);
			return new ResponseEntity<>(HttpStatus.OK);
		} else {
			LOG.error("Missing importJobDir.");
			throw new RestServiceException(
					new ErrorModel(HttpStatus.UNPROCESSABLE_ENTITY.value(), "Missing importJobDir.", null));
		}
	}

	@Override
	public ResponseEntity<ImportJob> queryPACS(
			@Parameter(name = "DicomQuery", required = true) @Valid @RequestBody final DicomQuery dicomQuery)
					throws RestServiceException {
		ImportJob importJob;
		try {
			importJob = queryPACSService.queryCFIND(dicomQuery);
			// the pacs workfolder is empty here, as multiple queries could be asked before
			// string an import
			importJob.setWorkFolder("");
			importJob.setFromPacs(true);
			importJob.setUserId(KeycloakUtil.getTokenUserId());
		} catch (Exception e) {
			throw new RestServiceException(
					new ErrorModel(HttpStatus.UNPROCESSABLE_ENTITY.value(), e.getMessage(), null));
		}
		if (importJob.getPatients() == null || importJob.getPatients().isEmpty()) {
			return new ResponseEntity<>(HttpStatus.NO_CONTENT);
		}
		return new ResponseEntity<>(importJob, HttpStatus.OK);
	}

	@Override
	public ResponseEntity<ImportJob> importDicomZipFile(
			@Parameter(name = "file detail") @RequestBody final String dicomZipFilename) throws RestServiceException {
		// We use this when coming from BRUKER upload
		if (dicomZipFilename == null) {
			throw new RestServiceException(
					new ErrorModel(HttpStatus.UNPROCESSABLE_ENTITY.value(), NO_FILE_UPLOADED, null));
		}
		File tempFile = new File(dicomZipFilename);
		MockMultipartFile multiPartFile;
		try {
			multiPartFile = new MockMultipartFile(tempFile.getName(), tempFile.getName(), APPLICATION_ZIP, new FileInputStream(tempFile.getAbsolutePath()));
			// Import dicomfile
			return uploadDicomZipFile(multiPartFile);
		} catch (IOException e) {
			LOG.error("ERROR while loading zip file, please contact an administrator");
			LOG.error(e.getMessage(), e);
			return new ResponseEntity<>(HttpStatus.NOT_ACCEPTABLE);
		} finally {
			// Delete temp file which is useless now
			tempFile.delete();
		}
	}

	@Override
	/**
	 * This method load an EEG file, unzip it and load an import job with the
	 * informations collected
	 */
	public ResponseEntity<EegImportJob> uploadEEGZipFile(
			@Parameter(name = "file detail") @RequestPart("file") final MultipartFile eegFile)
					throws RestServiceException {
		try {
			// Do some checks about the file, must be != null and must be a .zip file
			if (eegFile == null) {
				throw new RestServiceException(
						new ErrorModel(HttpStatus.UNPROCESSABLE_ENTITY.value(), NO_FILE_UPLOADED, null));
			}
			if (!ImportUtils.isZipFile(eegFile)) {
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
			File tempFile = ImportUtils.saveTempFile(userImportDir, eegFile);
			File importJobDir = ImportUtils.saveTempFileCreateFolderAndUnzip(tempFile, eegFile, false);
			EegImportJob importJob = new EegImportJob();
			importJob.setUserId(userId);
			importJob.setArchive(eegFile.getOriginalFilename());
			importJob.setWorkFolder(importJobDir.getAbsolutePath());
			importJob.setUsername(KeycloakUtil.getTokenUserName());
			return new ResponseEntity<>(importJob, HttpStatus.OK);
		} catch (IOException ioe) {
			throw new RestServiceException(ioe, new ErrorModel(HttpStatus.BAD_REQUEST.value(), "Invalid file"));
		}
	}

	@Override
	public ResponseEntity<EegImportJob> analyzeEegZipFile(@Parameter(name = "EegImportJob", required=true) @RequestBody EegImportJob importJob) throws RestServiceException {
		try {
			List<EegDataset> datasets = new ArrayList<>();

			File dataFileDir = new File(importJob.getWorkFolder() + File.separator
					+ importJob.getArchive().replace(".zip", ""));

			LOG.error(dataFileDir.getAbsolutePath());


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
			importJob.setUsername(KeycloakUtil.getTokenUserName());

			return new ResponseEntity<>(importJob, HttpStatus.OK);
		} catch (ShanoirImportException e) {
			throw new RestServiceException(e, new ErrorModel(HttpStatus.BAD_REQUEST.value(), e.getMessage()));
		}
	}

	private File convertAnalyzeToNifti(File imageFile, File headerFile) throws ShanoirException {
		String imageName = imageFile.getAbsolutePath();
		String newImageName = imageName.replace(".img", ".nii.gz");
		File parentFolder = imageFile.getParentFile().getAbsoluteFile();

		// Send to nifti conversion micro service
		boolean result = (boolean) rabbitTemplate.convertSendAndReceive(RabbitMQConfiguration.ANIMA_CONVERSION_QUEUE, imageName);

		if (!result) {
			throw new ShanoirException("Could not convert from anima to nifti, please contact an administrator.");
		}

		return new File(parentFolder, newImageName);
	}

	@Override
	/**
	 * This method imports dataset file, and converts them to nifti if necessary (in case of a Analyze file format from .hdr/.img files)
	 */
	public ResponseEntity<String> uploadProcessedDataset(
			@Parameter(name = "image detail") @RequestPart("image") MultipartFile imageFile, 
			@Parameter(name = "header detail", required = false) @RequestPart(value = "header", required = false) MultipartFile headerFile) 
					throws RestServiceException {

		String imageFileName = imageFile == null ? "" : imageFile.getOriginalFilename();
		String headerFileName = headerFile == null ? "" : headerFile.getOriginalFilename();
		Boolean isNifti = imageFileName.endsWith(".nii") || imageFileName.endsWith(".nii.gz");
		Boolean isAnalyze = imageFileName.endsWith(".img") && headerFileName.endsWith(".hdr");

		if (!isNifti && !isAnalyze) {
			throw new RestServiceException(new ErrorModel(HttpStatus.UNPROCESSABLE_ENTITY.value(),
					"Wrong content type of file upload.", null));
		}

		try {

			// 1. Save files to user directory

			//    - Create user directory

			long n = ImportUtils.createRandomLong();
			final Long userId = KeycloakUtil.getTokenUserId();
			final String userImportDirFilePath = importDir + File.separator + Long.toString(userId) + File.separator + Long.toString(n);
			final File userImportDir = new File(userImportDirFilePath);
			if (!userImportDir.exists()) {
				userImportDir.mkdirs();
			}

			//    - Save files
			File destinationImageFile = new File(userImportDir.getAbsolutePath(), imageFileName);
			imageFile.transferTo(destinationImageFile);
			if(headerFile != null) {
				File destinationHeaderFile = new File(userImportDir.getAbsolutePath(), headerFileName);
				headerFile.transferTo(destinationHeaderFile);

				// Convert Analyze format to nifti format
				destinationImageFile = convertAnalyzeToNifti(destinationImageFile, destinationHeaderFile);
			}

			return new ResponseEntity<String>(destinationImageFile.getAbsolutePath(), HttpStatus.OK);
		} catch (IOException | ShanoirException e) {
			LOG.error(e.getMessage(), e);
			throw new RestServiceException(
					new ErrorModel(HttpStatus.UNPROCESSABLE_ENTITY.value(), ERROR_WHILE_SAVING_UPLOADED_FILE, null));
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
			@Parameter(name = "EegImportJob", required = true) @Valid @RequestBody final EegImportJob importJob) {
		// Comment: Anonymisation is not necessary for pure brainvision EEGs data
		try {
			importJob.setUsername(KeycloakUtil.getTokenUserName());
			ShanoirEvent event = new ShanoirEvent(ShanoirEventType.IMPORT_DATASET_EVENT, importJob.getExaminationId().toString(), KeycloakUtil.getTokenUserId(), "Starting import...", ShanoirEvent.IN_PROGRESS, 0f, importJob.getStudyId());
			importJob.setShanoirEvent(event);
			Integer integg = (Integer) rabbitTemplate.convertSendAndReceive(RabbitMQConfiguration.IMPORT_EEG_QUEUE,  objectMapper.writeValueAsString(importJob));
			return new ResponseEntity<Void>(HttpStatusCode.valueOf(integg.intValue()));			
		} catch (Exception e) {
			LOG.error("Error during EEG import", e);
			return new ResponseEntity<Void>(HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	@Override
	public ResponseEntity<String> createTempDir() throws RestServiceException {
		final File userImportDir = ImportUtils.getUserImportDir(importDir);
		long n = ImportUtils.createRandomLong();
		File tempDirForImport = new File(userImportDir, Long.toString(n));
		if (!tempDirForImport.exists()) {
			tempDirForImport.mkdirs();
		} else {
			throw new RestServiceException(new ErrorModel(HttpStatus.UNPROCESSABLE_ENTITY.value(),
					"Error while creating temp dir: random number generated twice?", null));
		}
		return new ResponseEntity<String>(tempDirForImport.getName(), HttpStatus.OK);
	}

	@Override
	public ResponseEntity<Void> uploadFile(@PathVariable("tempDirId") String tempDirId,
										   @RequestParam("file") MultipartFile file) throws RestServiceException, IOException {
		final File userImportDir = ImportUtils.getUserImportDir(importDir);
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

	/**
	 * This methods returns a dicom file
	 *
	 * @param path
	 *            the dicom file path
	 * @throws ShanoirException
	 *             when something gets wrong during the import
	 * @throws IOException
	 *             when IO fails
	 * @throws RestServiceException
	 */
	@Override
	public ResponseEntity<ByteArrayResource> getDicomImage(@Parameter(name = "path", required=true)  @RequestParam(value = "path", required = true) String path)
			throws RestServiceException, IOException {

		final File userImportDir = ImportUtils.getUserImportDir(importDir);
		String pathInfo = userImportDir.getAbsolutePath() + File.separator + path;
		URL url = new URL("file:///" + pathInfo);
		final URLConnection uCon = url.openConnection();
		final InputStream is = uCon.getInputStream();

		ByteArrayOutputStream buffer = new ByteArrayOutputStream();
		int nRead;
		byte[] data = new byte[BUFFER_SIZE];
		while ((nRead = is.read(data, 0, data.length)) != -1) {
			buffer.write(data, 0, nRead);
		}

		buffer.flush();
		byte[] byteArray = buffer.toByteArray();

		ByteArrayResource resource = new ByteArrayResource(byteArray);

		return ResponseEntity.ok()
				.contentType(MediaType.parseMediaType("application/dicom"))
				.contentLength(uCon.getContentLength())
				.body(resource);
	}

	public ResponseEntity<ImportJob> uploadMultipleDicom(@Parameter(name = "file detail") @RequestPart("file") MultipartFile dicomZipFile,
			@Parameter(name = "studyId", required = true) @PathVariable("studyId") Long studyId,
			@Parameter(name = "studyName", required = true) @PathVariable("studyName") String studyName,
			@Parameter(name = "studyCardId") @PathVariable("studyCardId") Long studyCardId,
			@Parameter(name = "centerId", required = true) @PathVariable("centerId") Long centerId,
			@Parameter(name = "equipmentId", required = true) @PathVariable("equipmentId") Long equipmentId) throws RestServiceException {
		LOG.warn("Multiple examination import.");
		// STEP 1: Unzip file
		if (dicomZipFile == null || !ImportUtils.isZipFile(dicomZipFile)) {
			throw new RestServiceException(
					new ErrorModel(HttpStatus.UNPROCESSABLE_ENTITY.value(), WRONG_CONTENT_FILE_UPLOAD, null));
		}

		ImportJob job = null;

		try {
			File userImportDir = ImportUtils.getUserImportDir(importDir);
			File tempFile = ImportUtils.saveTempFile(userImportDir, dicomZipFile);
			File importJobDir = ImportUtils.saveTempFileCreateFolderAndUnzip(tempFile, dicomZipFile, true);

			// STEP 2: Analyze file structure and verify for mismatch
			File[] subjectFolders = importJobDir.listFiles();
			if (subjectFolders == null || subjectFolders.length != 1) {
				throw new RestServiceException(new ErrorModel(HttpStatus.UNPROCESSABLE_ENTITY.value(),
						"The zip must contain a single folder named with the desired subject name.", null));
			}
			File subjectFolder = subjectFolders[0];
			File[] examinationsFolders = subjectFolder.listFiles();

			String subjectName = studyName + "_" + subjectFolder.getName();
			Subject subject = null;

			// Sort examination folders by alphabetical order
			List<File> sortedExamFolders = Arrays.asList(examinationsFolders);
			sortedExamFolders.sort(new Comparator<File>() {
				@Override
				public int compare(File f1, File f2) {
					return f1.getName().compareTo(f2.getName());
				}
			});

			// STEP 4: Iterate over examination folders
			for (File examFolder : sortedExamFolders) {
				job = null;
				// Check of it's a folder
				if (!examFolder.isDirectory()) {
					throw new RestServiceException(new ErrorModel(HttpStatus.UNPROCESSABLE_ENTITY.value(),
							"The main subject folder should only contain sub-folders and not single/data files.", null));
				}
				File zippedExamFolder = new File (examFolder.getAbsolutePath() + ".zip");
				zippedExamFolder.createNewFile();
				Utils.zip(examFolder.getAbsolutePath(), zippedExamFolder.getAbsolutePath());
				MockMultipartFile mockedFile = new MockMultipartFile(examFolder.getName(), zippedExamFolder.getName(), APPLICATION_ZIP, new FileInputStream(zippedExamFolder));

				// STEP 4.0 "Fake" upload file to get create the dicomdir and temporary folder
				job = this.uploadDicomZipFile(mockedFile).getBody();
				Patient patient = job.getPatients().get(0);

				// Create subject only once.
				if (subject == null) {

					// Create subject
					// Update birth date to 1st of january of the year
					LocalDate updateBirthdate = patient.getPatientBirthDate().withDayOfYear(1);
					subject = ImportUtils.createSubject(subjectName, updateBirthdate, patient.getPatientSex(), 1, Collections.singletonList(new SubjectStudy(new IdName(null, subjectName), new IdName(studyId, studyName))));

					LOG.debug("We found a subject " + subjectName);

					// Create subject
					Long subjectId = (Long) rabbitTemplate.convertSendAndReceive(RabbitMQConfiguration.SUBJECTS_QUEUE, objectMapper.writeValueAsString(subject));
					if (subjectId == null) {
						throw new RestServiceException(new ErrorModel(HttpStatus.UNPROCESSABLE_ENTITY.value(), "Subject could not be created, please check data", null));
					}
					subject.setId(subjectId);
				}

				// STEP 4.1 Get informations about center / study card
				// Get equipment id
				Long equipmentIdFromDicom = null;
				if (job.getPatients().get(0).getStudies().get(0).getSeries().get(0).getEquipment().getDeviceSerialNumber() != null) {
					equipmentIdFromDicom = (Long) this.rabbitTemplate.convertSendAndReceive(RabbitMQConfiguration.EQUIPMENT_FROM_CODE_QUEUE, job.getPatients().get(0).getStudies().get(0).getSeries().get(0).getEquipment().getDeviceSerialNumber());
					if (equipmentIdFromDicom != null) {
						if (studyCardId != null) {
							Properties props = new Properties();
							props.setProperty("EQUIPMENT_ID_PROPERTY", "" + equipmentIdFromDicom);
							props.setProperty("STUDY_ID_PROPERTY", "" + studyId);
							props.setProperty("STUDYCARD_ID_PROPERTY", "" + studyCardId);

							Long newStudyCardId = (Long) this.rabbitTemplate.convertSendAndReceive(RabbitMQConfiguration.IMPORT_STUDY_CARD_QUEUE, props);
							if (newStudyCardId != null) {
								studyCardId = newStudyCardId;
							}
						}
						job.setAcquisitionEquipmentId(equipmentIdFromDicom);
					} else {
						job.setAcquisitionEquipmentId(equipmentId);
					}
				}

				if (studyCardId != null)
					job.setStudyCardId(studyCardId);

				// STEP 4.2 Create examination
				ExaminationDTO examination = ImportUtils.createExam(studyId, centerId, subject.getId(), examFolder.getName(), job.getPatients().get(0).getStudies().get(0).getStudyDate(), subject.getName());

				// Create multiple examinations for every session folder
				Long examId = (Long) rabbitTemplate.convertSendAndReceive(RabbitMQConfiguration.EXAMINATION_CREATION_QUEUE, objectMapper.writeValueAsString(examination));

				if (examId == null) {
					throw new RestServiceException(new ErrorModel(HttpStatus.UNPROCESSABLE_ENTITY.value(), "Error while creating examination", null));
				}
				eventService.publishEvent(new ShanoirEvent(ShanoirEventType.CREATE_EXAMINATION_EVENT, examId.toString(), KeycloakUtil.getTokenUserId(), "centerId:" + centerId + ";subjectId:" + examination.getSubject().getId(), ShanoirEvent.SUCCESS, examination.getStudyId()));

				// STEP 4.3 Complete importJob with subject / study /examination
				
				String anonymizationProfile = (String) this.rabbitTemplate.convertSendAndReceive(RabbitMQConfiguration.STUDY_ANONYMISATION_PROFILE_QUEUE, studyId);
				
				job.setSubjectName(subjectName);
				job.setExaminationId(examId);
				job.setFromDicomZip(true);
				job.setFromPacs(false);
				job.setStudyId(studyId);
				job.setCenterId(centerId);
				job.setStudyName(studyName);
				job.setAcquisitionEquipmentId(equipmentId);
				job.setAnonymisationProfileToUse(anonymizationProfile);
				for (Patient pat : job.getPatients()) {
					pat.setSubject(subject);
				}

				// STEP 4.4 Select all series
				for(Study study : job.getPatients().get(0).getStudies()) {
					for (Serie serie : study.getSeries()) {
						serie.setSelected(true);
					}
				}

				// STEP 4.4 Send to dataset for logical import
				this.startImportJob(job);
			}

			// STEP 5 delete temporary file
			FileUtils.deleteQuietly(importJobDir);
		} catch (IOException e) {
			throw new RestServiceException(new ErrorModel(HttpStatus.INTERNAL_SERVER_ERROR.value(),
					"The file could not be correctly unziped on the server. Please check consistency.", e));
		}
		return new ResponseEntity<ImportJob>(job, HttpStatus.OK);

	}
}

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

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.validation.Valid;

import org.apache.commons.io.FilenameUtils;
import org.shanoir.ng.exchange.imports.dicom.DicomDirGeneratorService;
import org.shanoir.ng.exchange.model.ExExamination;
import org.shanoir.ng.exchange.model.ExStudy;
import org.shanoir.ng.exchange.model.ExStudyCard;
import org.shanoir.ng.exchange.model.ExSubject;
import org.shanoir.ng.exchange.model.Exchange;
import org.shanoir.ng.importer.dicom.DicomDirToModelService;
import org.shanoir.ng.importer.dicom.ImagesCreatorAndDicomFileAnalyzerService;
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
import org.shanoir.ng.importer.model.Serie;
import org.shanoir.ng.importer.model.Study;
import org.shanoir.ng.importer.model.Subject;
import org.shanoir.ng.shared.exception.ErrorModel;
import org.shanoir.ng.shared.exception.RestServiceException;
import org.shanoir.ng.shared.exception.ShanoirException;
import org.shanoir.ng.shared.exception.ShanoirImportException;
import org.shanoir.ng.utils.ImportUtils;
import org.shanoir.ng.utils.KeycloakUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

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
	private RestTemplate restTemplate;

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
				ImportUtils.saveTempFile(new File(importDir), files[i]);
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
		if (dicomZipFile == null || !ImportUtils.isZipFile(dicomZipFile)) {
			throw new RestServiceException(
					new ErrorModel(HttpStatus.UNPROCESSABLE_ENTITY.value(), WRONG_CONTENT_FILE_UPLOAD, null));
		}
		if (!ImportUtils.isZipFile(dicomZipFile)) {
			throw new RestServiceException(new ErrorModel(HttpStatus.UNPROCESSABLE_ENTITY.value(),
					"Wrong content type of file upload, .zip required.", null));
		}
		try {
			/**
			 * 1. STEP: Handle file management. Always create a userId specific folder in
			 * the import work folder (the root of everything): split imports to clearly
			 * separate them into separate folders for each user
			 */
			File userImportDir = ImportUtils.getUserImportDir(importDir);
			
			boolean createDicomDir = false;

			File tempFile = ImportUtils.saveTempFile(userImportDir, dicomZipFile);

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
			imagesCreatorAndDicomFileAnalyzer.createImagesAndAnalyzeDicomFiles(patients, importJobDir.getAbsolutePath(), false);
	
			/**
			 * . STEP: create ImportJob
			 */
			ImportJob importJob = new ImportJob();
			importJob.setFromDicomZip(true);
			// Work folder is always relative to general import directory
			importJob.setWorkFolder(importJobDir.getName());
			importJob.setPatients(patients);
			return new ResponseEntity<>(importJob, HttpStatus.OK);
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
		File userImportDir = ImportUtils.getUserImportDir(importDir);
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

	@Override
	public ResponseEntity<ImportJob> importDicomZipFile(
			@ApiParam(value = "file detail") @RequestBody final String dicomZipFilename) throws RestServiceException {
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
			LOG.error("ERROR while loading zip fiole, please contact an administrator");
			e.printStackTrace();
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
			@ApiParam(value = "file detail") @RequestPart("file") final MultipartFile eegFile)
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

	@Override
	public ResponseEntity<Void> startImport(@RequestBody Exchange exchange)
			throws RestServiceException, FileNotFoundException, IOException {
		// 1. Check if uploaded data are complete (to be done a little later)
		final File userImportDir = ImportUtils.getUserImportDir(importDir);
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
			importJob.setStudyId(exStudy.getStudyId());
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
	public ResponseEntity<ByteArrayResource> getDicomImage(@ApiParam(value = "path", required=true)  @RequestParam(value = "path", required = true) String path)
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
}

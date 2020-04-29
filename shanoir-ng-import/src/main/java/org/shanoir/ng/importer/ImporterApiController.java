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

package org.shanoir.ng.importer;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.nio.file.Files;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.zip.ZipOutputStream;

import javax.validation.Valid;

import org.shanoir.ng.exchange.imports.dicom.DicomDirGeneratorService;
import org.shanoir.ng.exchange.model.ExExamination;
import org.shanoir.ng.exchange.model.ExStudy;
import org.shanoir.ng.exchange.model.ExStudyCard;
import org.shanoir.ng.exchange.model.ExSubject;
import org.shanoir.ng.exchange.model.Exchange;
import org.shanoir.ng.importer.dicom.DicomDirCreator;
import org.shanoir.ng.importer.dicom.DicomDirToModelService;
import org.shanoir.ng.importer.dicom.query.DicomQuery;
import org.shanoir.ng.importer.dicom.query.QueryPACSService;
import org.shanoir.ng.importer.dto.CommonIdNamesDTO;
import org.shanoir.ng.importer.dto.CommonIdsDTO;
import org.shanoir.ng.importer.dto.ExaminationDTO;
import org.shanoir.ng.importer.model.ImportJob;
import org.shanoir.ng.importer.model.Patient;
import org.shanoir.ng.importer.model.Serie;
import org.shanoir.ng.importer.model.Study;
import org.shanoir.ng.importer.model.Subject;
import org.shanoir.ng.shared.core.model.IdName;
import org.shanoir.ng.shared.exception.ErrorModel;
import org.shanoir.ng.shared.exception.RestServiceException;
import org.shanoir.ng.shared.exception.ShanoirException;
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

	private static final String NO_FILE_UPLOADED = "No file uploaded.";

	private static final Logger LOG = LoggerFactory.getLogger(ImporterApiController.class);

	private static final SecureRandom RANDOM = new SecureRandom();

	private static final String FILE_POINT = ".";

	private static final String DICOMDIR = "DICOMDIR";

	private static final String APPLICATION_OCTET_STREAM = "application/octet-stream";

	private static final String APPLICATION_ZIP = "application/zip";

	private static final String UPLOAD_FILE_SUFFIX = ".upload";

	private static final String ZIP_FILE_SUFFIX = ".zip";

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
	private RabbitTemplate rabbitTemplate;

	@Autowired
	private DicomDirGeneratorService dicomDirGeneratorService;

	@Autowired
	private DicomDirToModelService dicomDirToModel;

	@Autowired
	private ImporterManagerService importerManagerService;

	@Autowired
	private QueryPACSService queryPACSService;

	@Override
	public ResponseEntity<ImportJob> uploadDicomZipFile(
			@ApiParam(value = "file detail") @RequestPart("file") MultipartFile dicomZipFile)
			throws RestServiceException {
		if (dicomZipFile == null) {
			throw new RestServiceException(
					new ErrorModel(HttpStatus.UNPROCESSABLE_ENTITY.value(), "No file uploaded.", null));
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
					new ErrorModel(HttpStatus.UNPROCESSABLE_ENTITY.value(), "Error while saving uploaded file.", null));
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
			Patient patient = (Patient) patientIt.next();
			List<Study> studies = patient.getStudies();
			for (Iterator<Study> studyIt = studies.iterator(); studyIt.hasNext();) {
				Study study = (Study) studyIt.next();
				List<Serie> series = study.getSeries();
				for (Iterator<Serie> serieIt = series.iterator(); serieIt.hasNext();) {
					Serie serie = (Serie) serieIt.next();
					if (!serie.getSelected()) {
						series.remove(serie);
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
			LOG.error(e.getMessage(), e);
			throw new RestServiceException(
					new ErrorModel(HttpStatus.UNPROCESSABLE_ENTITY.value(), e.getMessage(), null));
		}
		if (importJob.getPatients() == null || importJob.getPatients().isEmpty()) {
			return new ResponseEntity<>(HttpStatus.NO_CONTENT);
		}
		return new ResponseEntity<ImportJob>(importJob, HttpStatus.OK);
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
	private boolean isZipFile(MultipartFile file) {
		if (file.getContentType().equals(APPLICATION_ZIP) || file.getContentType().equals(APPLICATION_OCTET_STREAM)
				|| file.getOriginalFilename().endsWith(ZIP_FILE_SUFFIX)) {
			return true;
		}
		return false;
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
				ExSubject exSubject = (ExSubject) iterator.next();
				Subject subject = new Subject();
				subject.setId(exSubject.getSubjectId());
				subject.setName(exSubject.getSubjectName());
				patients.get(i).setSubject(subject);
				if (exSubject != null && exSubject.getSubjectName() != null) {
					List<ExExamination> exExaminations = exSubject.getExExaminations();
					for (Iterator<ExExamination> iterator2 = exExaminations.iterator(); iterator2.hasNext();) {
						ExExamination exExamination = (ExExamination) iterator2.next();
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
		String participantString = (String) rabbitTemplate.convertSendAndReceive("subject_exchange", "",
				participantsFile.getAbsolutePath());

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
						"Subject " + subjectName + " could not be created. Please check participants.tsv file. ");
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

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
package org.shanoir.ng.importer.bids;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.zip.ZipOutputStream;

import org.shanoir.ng.importer.ImporterApiController;
import org.shanoir.ng.importer.dicom.DicomDirCreator;
import org.shanoir.ng.importer.dto.ExaminationDTO;
import org.shanoir.ng.importer.dto.StudyCardDTO;
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
import org.shanoir.ng.study.rights.StudyUser;
import org.shanoir.ng.study.rights.StudyUserInterface;
import org.shanoir.ng.utils.ImportUtils;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.multipart.MultipartFile;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import io.swagger.annotations.ApiParam;

@Controller
public class BidsImporterApiController implements BidsImporterApi {

	@Value("${shanoir.import.directory}")
	private String importDir;

	@Autowired
	private RabbitTemplate rabbitTemplate;

	@Autowired ObjectMapper mapper;

	private static final String WRONG_CONTENT_FILE_UPLOAD = "Wrong content type of file upload, .zip required.";

	private static final String NO_FILE_UPLOADED = "No file uploaded.";

	private static final String APPLICATION_ZIP = "application/zip";

	@Autowired
	ImporterApiController importer;

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
		// Check that the file is not null and well zipped
		if (bidsFile == null) {
			throw new RestServiceException(
					new ErrorModel(HttpStatus.UNPROCESSABLE_ENTITY.value(), NO_FILE_UPLOADED, null));
		}
		if (!ImportUtils.isZipFile(bidsFile)) {
			// .SEF ?
			throw new RestServiceException(
					new ErrorModel(HttpStatus.UNPROCESSABLE_ENTITY.value(), WRONG_CONTENT_FILE_UPLOAD, null));
		}

		// Create tmp folder and unzip archive
		final File userImportDir = ImportUtils.getUserImportDir(importDir);

		File tempFile = ImportUtils.saveTempFile(userImportDir, bidsFile);

		File importJobDir = ImportUtils.saveTempFileCreateFolderAndUnzip(tempFile, bidsFile, false);
		// Deserialize participants.tsv => Do a call to studies API to create
		// corresponding subjects
		File participantsFile = new File(importJobDir.getAbsolutePath() + "/participants.tsv");
		if (!participantsFile.exists()) {
			throw new ShanoirException("participants.tsv file is mandatory");
		}

		SimpleModule module = new SimpleModule();
		module.addAbstractTypeMapping(StudyUserInterface.class, StudyUser.class);
		mapper.registerModule(module);

		// Here we wait for the response => to be sure that the subjects are created
		String participantString = (String) rabbitTemplate.convertSendAndReceive(RabbitMQConfiguration.SUBJECTS_QUEUE, participantsFile.getAbsolutePath());
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
		File dicomSourceData = new File(sourceData.getAbsolutePath() + "/DICOM");
		File rawData = new File(importJobDir.getAbsolutePath() + "/rawData");
		boolean isSourceDataDicom = sourceData.exists() && dicomSourceData.exists();
		boolean isRawData = rawData.exists();

		// 2) List subject files
		File[] subjectFiles = (isSourceDataDicom? dicomSourceData : rawData).listFiles(new FilenameFilter() {
			@Override
			public boolean accept(File dir, String name) {
				return name.startsWith("sub-");
			}
		});

		for (File subjFile : subjectFiles) {
			// Get subjectName
			String subjectName = subjFile.getName().substring("sub-".length());

			List<File> workDirs = new ArrayList<>();

			File[] sessionFiles = subjFile.listFiles(new FilenameFilter() {
				@Override
				public boolean accept(File dir, String name) {
					return name.startsWith("ses-");
				}
			});

			// If we have sessions folders, iterate over them, otherwise, only keep subject folder
			if (sessionFiles == null || sessionFiles.length == 0) {
				workDirs.add(subjFile);
			} else {
				workDirs = Arrays.asList(sessionFiles);
			}

			for (File workDir : workDirs) {
				if (isSourceDataDicom) {
					// We have sourceData, we import data from it. (DICOM for MR)
					importFromSourceDataDicom(workDir, participants, subjectName);
				} else if (isRawData) {
					// We only have rawData, we import into BidsDatasets
					importFromRawData(workDir, participants, subjectName);
				} else {
					// At least rawData or sourceData folder should be present
					throw new ShanoirException("At least /sourcedata(/DICOM) or /rawData folder should be present.");
				}
			}
		}
		return null;
	}

	/**
	 * This method allows to import data from rawData folder in BIDS strucutre
	 * @param rawData the file where shanoir-import.json file is
	 * @param participants the list of participants
	 * @param subjectName the subject name
	 * @return the import job created
	 */
	private ResponseEntity<ImportJob> importFromRawData(File workDir, List<IdName> participants, String subjectName) throws ShanoirException, IOException, RestServiceException {

		// Here should be defined the data type.
		// + bruker ?
		// anat / func / dwi / eeg [...]
		// Then import following the modality (specific code for eeg)
		
		// List directories
		File[] typeFile  = workDir.listFiles(new FilenameFilter() {
			@Override
			public boolean accept(File dir, String name) {
				return dir.isDirectory();
			}
		});

		// What if multiple types ?
		// => Treat them one by one ?
		for (File type : typeFile) {
			if (type.getName().equals("eeg")) {
				// import as EEG
				importEegFromBids();
			}
			else if (type.getName().equals("func|anat|dwi")) {
				// Import direct nifti
			}
			else {
				// Not treated yet => Display a warning message ?
				// For this, use a BIDSImportJob ?
				break;
			}
		}

		// Read shanoirImportFile => Add configuration for examination ?
		File shanoirImportFile = new File(workDir.getAbsolutePath() + "/shanoir-import.json");

		if (!shanoirImportFile.exists()) {
			throw new ShanoirException("shanoir-import.json file is mandatory in subject / session folder");
		}

		ObjectMapper objectMapper = new ObjectMapper();
		ImportJob sid = objectMapper.readValue(shanoirImportFile, ImportJob.class);

		ImportJob job = new ImportJob();

		// Create a new examination if not existing
		if (sid.getExaminationId() == null || sid.getExaminationId().equals(Long.valueOf(0l))) {
			// Create examination => We actually need its ID so do a direct API call

			// Get center ID

			Long subjectId = getSubjectIdByName(subjectName, participants);

			ExaminationDTO examDTO = new ExaminationDTO();
			// Construct DTO
			// get center from study card => equipment => center
			// examDTO.setCenter(new IdName(center.getId(), center.getName()));
			// TODO, import bruker too
			examDTO.setPreclinical(false);
			examDTO.setStudy(new IdName(sid.getStudyId(), sid.getStudyName()));
			examDTO.setSubject(new IdName(subjectId, subjectName));

			mapper.registerModule(new JavaTimeModule());
			String examAsString = (String) rabbitTemplate.convertSendAndReceive(RabbitMQConfiguration.EXAMINATION_CREATION_QUEUE, mapper.writeValueAsString(examDTO));

			examDTO = mapper.readValue(examAsString, ExaminationDTO.class);
			job.setExaminationId(examDTO.getId());
		}

		// Next API call => StartImportJob
		ResponseEntity<Void> result = importer.startImportJob(job);
		if (!result.getStatusCode().equals(HttpStatus.OK)) {
			throw new ShanoirException("Error while importing subject: " + subjectName);
		}

		// TODO ONE DAY: Copy "other" files to the bids folder
		// Copy non datasets elements
		// Don't copy "data" folder
		// Don't copy examination_description.json
		// copy /sourceData??, /code and / files (readme, changes, participants.tsv,
		// participants.json, etc..)
		return new ResponseEntity<>(job, HttpStatus.OK);
	}

	private void importEegFromBids() {
		// TODO Auto-generated method stub
		
	}

	/**
	 * This method allows to import data from sourceData folder in BIDS strucutre
	 * @param participants the list of participants of the study
	 * @param sourceData the sourceData folder we are importing from
	 * @param subjectName the subject name
	 * @return a response entity
	 * @throws ShanoirException
	 * @throws IOException
	 * @throws RestServiceException
	 */
	private ResponseEntity<ImportJob> importFromSourceDataDicom(File workDir, List<IdName> participants, String subjectName) throws ShanoirException, IOException, RestServiceException {

		File shanoirImportFile = new File(workDir.getAbsolutePath() + "/shanoir-import.json");

		if (!shanoirImportFile.exists()) {
			throw new ShanoirException("shanoir-import.json file is mandatory in subject / session folder");
		}

		ObjectMapper objectMapper = new ObjectMapper();
		ImportJob sid = objectMapper.readValue(shanoirImportFile, ImportJob.class);

		// StudyCard only for DICOM
		String studyCardAsString = (String) rabbitTemplate.convertSendAndReceive(RabbitMQConfiguration.FIND_STUDY_CARD_QUEUE, sid.getStudyCardId());

		if (studyCardAsString == null) {
			throw new ShanoirException(
					"StudyCard with ID " + sid.getStudyCardId() + " does not exists.");
		}

		StudyCardDTO studyCard = objectMapper.readValue(studyCardAsString, StudyCardDTO.class);

		if (!studyCard.getStudyId().equals(sid.getStudyId())) {
			throw new ShanoirException("Study with ID " + sid.getStudyId() + " does not exists.");
		}
		if (studyCard.isDisabled()) {
			throw new ShanoirException("StudyCard with ID " + sid.getStudyCardId() + " is currently disabled, please select another one.");
		}

		// Subject based on folder name
		Long subjectId = getSubjectIdByName(subjectName, participants);
		if (subjectId == null) {
			throw new ShanoirException(
					"Subject " + subjectName + " could not be created. Please check participants.tsv file.");
		}

		// Create subjectStudy
		IdName participantsInfo = new IdName(subjectId, studyCard.getStudyId().toString());
		String studyName = (String) rabbitTemplate.convertSendAndReceive(RabbitMQConfiguration.DATASET_SUBJECT_STUDY_QUEUE, mapper.writeValueAsString(participantsInfo));

		if (studyName == null) {
			throw new ShanoirException("An error occured while linking subject to study. Please contact an administrator");
		}

		// If there is no DICOMDIR: create it
		File dicomDir = new File(workDir.getAbsolutePath() + "/DICOM/DICOMDIR");
		if (!dicomDir.exists()) {
			DicomDirCreator creator = new DicomDirCreator(workDir.getAbsolutePath() + "/DICOMDIR",
					workDir.getAbsolutePath() + "/DICOM");
			creator.start();
		}

		// Zip data folders to be able to call ImporterAPIController.uploadDicomZipFile
		FileOutputStream fos = new FileOutputStream(workDir.getAbsolutePath() + ".zip");
		ZipOutputStream zipOut = new ZipOutputStream(fos);

		ImportUtils.zipFile(workDir, workDir.getName(), zipOut, true);

		zipOut.close();
		fos.close();
		MockMultipartFile multiPartFile = new MockMultipartFile(workDir.getName(), workDir.getName() + ".zip",
				APPLICATION_ZIP, new FileInputStream(workDir.getAbsolutePath() + ".zip"));

		// Send data folder to import API and get import job
		ResponseEntity<ImportJob> entity = importer.uploadDicomZipFile(multiPartFile);

		// Complete ImportJob to use startImportJob
		ImportJob job = entity.getBody();

		// Construire l'arborescence
		job.setAcquisitionEquipmentId(studyCard.getAcquisitionEquipmentId());
		job.setStudyId(sid.getStudyId());
		job.setStudyCardId(sid.getStudyCardId());
		job.setSubjectName(subjectName);
		job.setStudyName(studyName);

		job.setFromPacs(false);
		job.setFromShanoirUploader(false);
		job.setFromDicomZip(true);
		job.setConverterId(studyCard.getNiftiConverterId());

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

			// Get center ID
			String centerAsString = (String) rabbitTemplate.convertSendAndReceive(RabbitMQConfiguration.ACQUISITION_EQUIPEMENT_CENTER_QUEUE, studyCard.getAcquisitionEquipmentId());
			IdName center = objectMapper.readValue(centerAsString, IdName.class);

			ExaminationDTO examDTO = new ExaminationDTO();
			// Construct DTO
			// get center from study card => equipment => center
			examDTO.setCenter(new IdName(center.getId(), center.getName()));
			examDTO.setPreclinical(false); // Pour le moment on fait que du DICOM
			examDTO.setStudy(new IdName(sid.getStudyId(), sid.getStudyName()));
			examDTO.setSubject(new IdName(subjectId, subjectName));
			examDTO.setExaminationDate(job.getPatients().get(0).getStudies().get(0).getStudyDate());
			examDTO.setComment(job.getPatients().get(0).getStudies().get(0).getStudyDescription());

			mapper.registerModule(new JavaTimeModule());
			String examAsString = (String) rabbitTemplate.convertSendAndReceive(RabbitMQConfiguration.EXAMINATION_CREATION_QUEUE, mapper.writeValueAsString(examDTO));

			examDTO = mapper.readValue(examAsString, ExaminationDTO.class);
			job.setExaminationId(examDTO.getId());
		}

		// Next API call => StartImportJob
		ResponseEntity<Void> result = importer.startImportJob(job);
		if (!result.getStatusCode().equals(HttpStatus.OK)) {
			throw new ShanoirException("Error while importing subject: " + subjectName);
		}

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
	public static Long getSubjectIdByName(String name, List<IdName> subjects) {
		for (IdName sub : subjects) {
			if (sub.getName().equals(name)) {
				return sub.getId();
			}
		}
		return null;
	}
}

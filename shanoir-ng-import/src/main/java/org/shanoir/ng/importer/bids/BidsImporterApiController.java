/**
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
import java.io.FilenameFilter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.attribute.FileTime;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAccessor;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.shanoir.ng.importer.ImporterApiController;
import org.shanoir.ng.importer.dto.ExaminationDTO;
import org.shanoir.ng.importer.model.ImportJob;
import org.shanoir.ng.importer.model.Subject;
import org.shanoir.ng.importer.model.SubjectStudy;
import org.shanoir.ng.shared.configuration.RabbitMQConfiguration;
import org.shanoir.ng.shared.core.model.IdName;
import org.shanoir.ng.shared.event.ShanoirEvent;
import org.shanoir.ng.shared.event.ShanoirEventService;
import org.shanoir.ng.shared.event.ShanoirEventType;
import org.shanoir.ng.shared.exception.ErrorModel;
import org.shanoir.ng.shared.exception.RestServiceException;
import org.shanoir.ng.shared.exception.ShanoirException;
import org.shanoir.ng.utils.ImportUtils;
import org.shanoir.ng.utils.KeycloakUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.multipart.MultipartFile;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.MappingIterator;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import com.fasterxml.jackson.dataformat.csv.CsvParser;

import io.swagger.v3.oas.annotations.Parameter;

@Controller
public class BidsImporterApiController implements BidsImporterApi {

	@Value("${shanoir.import.directory}")
	private String importDir;

	private static final String WRONG_CONTENT_FILE_UPLOAD = "Wrong content type of file upload, .zip required.";

	private static final String NO_FILE_UPLOADED = "No file uploaded.";

	private static final String NOT_SUBJECT_BASED_SUBJECT = "The zip has to contain an unique 'sub-XXX' subject folder with data following the BIDS specification.";

	private static final String SUBJECT_CREATION_ERROR = "An error occured during the subject creation, please check your rights.";

	private static final String EXAMINATION_CREATION_ERROR = "An error occured during the examination creation, please check your rights.";

	private static final String CSV_SEPARATOR = "\t";

	@Autowired
	ImporterApiController importer;

	@Autowired
	private RabbitTemplate rabbitTemplate;

	@Autowired
	private ObjectMapper objectMapper;

	@Autowired
	private ShanoirEventService eventService;

	private static final Logger LOG = LoggerFactory.getLogger(BidsImporterApiController.class);


	/**
	 * This method import a BIDS subject folder.
	 */
	@Override
	public ResponseEntity<ImportJob> importAsBids(
			@Parameter(name = "file detail") @RequestPart("file") final MultipartFile bidsFile,
			@Parameter(name = "id of the study", required = true) @PathVariable("studyId") Long studyId,
			@Parameter(name = "name of the study", required = true) @PathVariable("studyName") String studyName,
			@Parameter(name = "id of the center", required = true) @PathVariable("centerId") Long centerId)
					throws RestServiceException, ShanoirException, IOException {

		// STEP 1: Analyze folder and unzip it.
		if (bidsFile == null) {
			throw new RestServiceException(new ErrorModel(HttpStatus.UNPROCESSABLE_ENTITY.value(), NO_FILE_UPLOADED, null));
		}
		if (!ImportUtils.isZipFile(bidsFile)) {
			throw new RestServiceException(new ErrorModel(HttpStatus.UNPROCESSABLE_ENTITY.value(), WRONG_CONTENT_FILE_UPLOAD, null));
		}
		ImportJob importJob = new ImportJob();
		importJob.setStudyId(studyId);
		importJob.setStudyName(studyName);
		importJob.setUserId(KeycloakUtil.getTokenUserId());
		importJob.setUsername(KeycloakUtil.getTokenUserName());

		// Create tmp folder and unzip archive
		final File userImportDir = ImportUtils.getUserImportDir(importDir);
		File tempFile = ImportUtils.saveTempFile(userImportDir, bidsFile);
		File importJobDir = ImportUtils.saveTempFileCreateFolderAndUnzip(tempFile, bidsFile, false);

		// Get equipment from file if existing, otherwise, set the "UNKNOWN EQUIPMENT"
		importJob.setAcquisitionEquipmentId(0L);
		importJob.setWorkFolder(importJobDir.getAbsolutePath());

		// STEP 2: Subject level, analyze and create the new subject if necessary
		Long subjectId = null;
		// Exclude MACOS automatically added metadata files and directories (AppleDouble and Finder)
		for (File subjectFile : importJobDir.listFiles(new FilenameFilter() {
			@Override
			public boolean accept(File arg0, String name) {
				return !name.startsWith(".DS_Store") && !name.startsWith("__MAC") && !name.startsWith("._") && !name.startsWith(".AppleDouble") ;
			}})) {
			String fileName = subjectFile.getName();
			String subjectName = null;
			if (fileName.startsWith("sub-")) {
				// We found a subject
				subjectName = studyName + "_" + subjectFile.getName().split("sub-")[1];
				Subject subject = new Subject();
				subject.setName(subjectName);
				subject.setSubjectStudyList(Collections.singletonList(new SubjectStudy(new IdName(subject.getId(), subject.getName()), new IdName(studyId, studyName))));
				importJob.setSubjectName(subjectName);

				LOG.debug("We found a subject " + subjectName);

				// Create subject
				subjectId = (Long) rabbitTemplate.convertSendAndReceive(RabbitMQConfiguration.SUBJECTS_QUEUE, objectMapper.writeValueAsString(subject));
				if (subjectId == null) {
					throw new RestServiceException(new ErrorModel(HttpStatus.UNPROCESSABLE_ENTITY.value(), SUBJECT_CREATION_ERROR, null));
				}
				importJob.setSubjectName(subjectName);

			} else {
				throw new RestServiceException(new ErrorModel(HttpStatus.UNPROCESSABLE_ENTITY.value(), NOT_SUBJECT_BASED_SUBJECT, null));
			}


			// STEP 3: Examination level, check if there are session, otherwise create examinations
			Map<String, LocalDate> examDates = checkDatesFromSessionFile(subjectFile);
			// Filter out scans.tsv and sessions.tsv files
			File[] examFiles = subjectFile.listFiles(new FilenameFilter() {
				@Override
				public boolean accept(File arg0, String name) {
					return !name.endsWith("_scans.tsv") && !name.endsWith("_sessions.tsv") && !name.startsWith(".DS_Store") && !name.startsWith("__MAC") && !name.startsWith("._") && !name.startsWith(".AppleDouble");
				}
			});

			// Iterate over session files
			boolean examCreated = false;
			for (File sessionFile : examFiles) {
				FileTime creationTime = (FileTime) Files.getAttribute(Paths.get(sessionFile.getAbsolutePath()), "creationTime");
				ExaminationDTO examination;
				Long examId;

				// STEP 3.1 There is a session level
				if (sessionFile.getName().startsWith("ses-")) {
					String sessionLabel = sessionFile.getName().substring(sessionFile.getName().indexOf("ses-") + "ses-".length());
					LocalDate examDate = examDates.get(sessionLabel);

					// Check for scans.tsv if session does not exist
					if (examDate == null) {
						examDate = checkDateFromScansFile(sessionFile);
					}

					// Set file creation as default
					if (examDate == null) {
						examDate = LocalDate.ofInstant(creationTime.toInstant(), ZoneId.systemDefault());
					}
					examination = ImportUtils.createExam(studyId, centerId, subjectId, sessionLabel, examDate, subjectName);
					examCreated = true;

					// Create multiple examinations for every session folder
					examId = (Long) rabbitTemplate.convertSendAndReceive(RabbitMQConfiguration.EXAMINATION_CREATION_QUEUE, objectMapper.writeValueAsString(examination));

					if (examId == null) {
						throw new RestServiceException(new ErrorModel(HttpStatus.UNPROCESSABLE_ENTITY.value(), EXAMINATION_CREATION_ERROR, null));
					}
					eventService.publishEvent(new ShanoirEvent(ShanoirEventType.CREATE_EXAMINATION_EVENT, examId.toString(), KeycloakUtil.getTokenUserId(), "centerId:" + centerId + ";subjectId:" + examination.getSubject().getId(), ShanoirEvent.SUCCESS, examination.getStudyId()));

					importJob.setExaminationId(examId);

					// STEP 4: Finish import from every bids data folder
					for (File dataTypeFile : sessionFile.listFiles(
							new FilenameFilter() {
								@Override
								public boolean accept(File arg0, String name) {
									return !name.startsWith(".DS_Store") && !name.startsWith("__MAC") && !name.startsWith("._") && !name.startsWith(".AppleDouble") ;
								}}
					)) {
						importSession(dataTypeFile, importJob);
					}
				} else {
					// STEP 3.2 No session level
					if (!examCreated) {
						// Try to find acqusition_time in _scans.tsv file
						LocalDate examDate = checkDateFromScansFile(sessionFile);
						if (examDate == null) {
							// Set exam date by default using file creation date
							examDate = LocalDate.ofInstant(creationTime.toInstant(), ZoneOffset.UTC);
						}
						examination = ImportUtils.createExam(studyId, centerId, subjectId, "", examDate, subjectName);
						examId = (Long) rabbitTemplate.convertSendAndReceive(RabbitMQConfiguration.EXAMINATION_CREATION_QUEUE, objectMapper.writeValueAsString(examination));

						if (examId == null) {
							throw new RestServiceException(new ErrorModel(HttpStatus.UNPROCESSABLE_ENTITY.value(), EXAMINATION_CREATION_ERROR, null));
						}
						eventService.publishEvent(new ShanoirEvent(ShanoirEventType.CREATE_EXAMINATION_EVENT, examId.toString(), KeycloakUtil.getTokenUserId(), "" + examination.getStudyId(), ShanoirEvent.SUCCESS, examination.getStudyId()));

						importJob.setExaminationId(examId);
						examCreated = true;
					}
					// STEP 4: Finish impor from bids data folder
					importSession(sessionFile, importJob);
				}
			}
		}

		return new ResponseEntity<>(null, HttpStatus.OK);
	}

	/**
	 * This methods check if a _scans.tsv exists in the folder, and load the date of the first reference
	 * @param parentFile the parent folder where the scancs.tsv file can be found
	 * @return the first found date in the tsv file, null otherwise
	 * @throws IOException when the file reading fails.
	 */
	private LocalDate checkDateFromScansFile(File parentFile) throws IOException {
		File[] scansFiles = parentFile.listFiles(new FilenameFilter() {
			@Override
			public boolean accept(File dir, String name) {
				return name.endsWith("_scans.tsv");
			}
		});

		// We found _scans.tsv file
		if (scansFiles.length != 1) {
			return null;
		}

		File scanFile = scansFiles[0];

		CsvMapper mapper = new CsvMapper();
		mapper.enable(CsvParser.Feature.WRAP_AS_ARRAY);
		MappingIterator<String[]> it = mapper.readerFor(String[].class).readValues(scanFile);

		// Check that the list of column is known
		List<String> columns = Arrays.asList(it.next()[0].split(CSV_SEPARATOR));

		int dateIndex = columns.indexOf("acq_time");

		// If there is no date, just give up
		if (dateIndex == -1) {
			return null;
		}
		// Legal format in BIDS (are we up to date ? I don't think so)
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss[.SSSSSS][X]");
		String[] row = it.next()[0].split(CSV_SEPARATOR);
		String dateAsString = row[dateIndex];
		try {
			TemporalAccessor date = formatter.parseBest(dateAsString, LocalDate::from, LocalDateTime::from);
			return LocalDate.from(date);
		} catch (Exception e) {
			LOG.error("Could not parse date [{}] for csv.", dateAsString);
			return null;
		}
	}

	/**
	 * This methods check if a _sessions.tsv file exists in the folder, and load the dates for every referenced session
	 * @param parentFile the parent folder where the sessions.tsv file can be found
	 * @return a Map of sessionID -> Date, an empty Hashset otherwise.
	 * @throws IOException when the file reading fails.
	 */
	private Map<String, LocalDate> checkDatesFromSessionFile(File parentFile) throws IOException {
		// Try to find sub-<label>_sessions.tsv file
		Map<String, LocalDate> examDates = new HashMap<String, LocalDate>();
		File[] sessionFiles = parentFile.listFiles(new FilenameFilter() {
			@Override
			public boolean accept(File dir, String name) {
				return name.endsWith("_sessions.tsv");
			}
		});

		// We found session_tsv file
		if (sessionFiles.length != 1) {
			return examDates;
		}
		File sessionFile = sessionFiles[0];


		// session_id;acq_time;pathology
		// analyze date for every session
		CsvMapper mapper = new CsvMapper();
		mapper.enable(CsvParser.Feature.WRAP_AS_ARRAY);
		MappingIterator<String[]> it = mapper.readerFor(String[].class).readValues(sessionFile);

		// Check File is not empty
		if (sessionFile.length() > 0) {
			LOG.error("We found a non empty session.tsv file ");
			// Check that the list of column is known
			List<String> columns = Arrays.asList(it.next()[0].split(CSV_SEPARATOR));
			int sessionIdIndex = columns.indexOf("session_id");
			int dateIndex = columns.indexOf("acq_time");

			// If there is no date, just give up
			if (dateIndex == -1) {
				return examDates;
			}

			// Legal format in BIDS (are we up to date ? I don't think so)
			DateTimeFormatter formatter = DateTimeFormatter.ofPattern("YYYY-MM-DDThh:mm:ss[.000000][Z]");

			while (it.hasNext()) {
				String[] row = it.next()[0].split(CSV_SEPARATOR);
				String sessionLabel = row[sessionIdIndex];
				String dateAsString = row[dateIndex];
				TemporalAccessor date = formatter.parseBest(dateAsString, LocalDate::from);
				examDates.put(sessionLabel, LocalDate.from(date));
			}
		} else {
			LOG.error("We found an empty session.tsv file ");
			return examDates;
		}

		return examDates;
	}

	/**
	 * Import a session from a data type file.
	 * @param dataTypeFile
	 * @param importJob
	 */
	private void importSession(File dataTypeFile, ImportJob importJob) throws AmqpException, JsonProcessingException {
		if (dataTypeFile.isDirectory()) {
			importJob.setWorkFolder(dataTypeFile.getAbsolutePath());
			LOG.debug("We found a data folder " + dataTypeFile.getName());
			rabbitTemplate.convertAndSend(RabbitMQConfiguration.IMPORTER_BIDS_DATASET_QUEUE, objectMapper.writeValueAsString(importJob));
		} else {
			LOG.debug("We found an examination extra-data " + dataTypeFile.getAbsolutePath());
			IdName extraData = new IdName(importJob.getExaminationId(), dataTypeFile.getAbsolutePath());
			this.rabbitTemplate.convertAndSend(RabbitMQConfiguration.EXAMINATION_EXTRA_DATA_QUEUE, objectMapper.writeValueAsString(extraData));
		}
	}

}

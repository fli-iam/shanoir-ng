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
import java.io.FilenameFilter;
import java.io.IOException;
import java.time.LocalDate;

import org.shanoir.ng.importer.ImporterApiController;
import org.shanoir.ng.importer.dto.ExaminationDTO;
import org.shanoir.ng.importer.model.ImportJob;
import org.shanoir.ng.importer.model.Subject;
import org.shanoir.ng.shared.configuration.RabbitMQConfiguration;
import org.shanoir.ng.shared.core.model.IdName;
import org.shanoir.ng.shared.exception.ErrorModel;
import org.shanoir.ng.shared.exception.RestServiceException;
import org.shanoir.ng.shared.exception.ShanoirException;
import org.shanoir.ng.utils.ImportUtils;
import org.shanoir.ng.utils.KeycloakUtil;
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
import com.fasterxml.jackson.databind.ObjectMapper;

import io.swagger.annotations.ApiParam;

@Controller
public class BidsImporterApiController implements BidsImporterApi {

	@Value("${shanoir.import.directory}")
	private String importDir;

	private static final String WRONG_CONTENT_FILE_UPLOAD = "Wrong content type of file upload, .zip required.";

	private static final String NO_FILE_UPLOADED = "No file uploaded.";

	@Autowired
	ImporterApiController importer;

	@Autowired
	private RabbitTemplate rabbitTemplate;

	@Autowired
	private ObjectMapper objectMapper;

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
			@ApiParam(value = "file detail") @RequestPart("file") final MultipartFile bidsFile,
    		@ApiParam(value = "id of the study", required = true) @PathVariable("studyId") Long studyId,
    		@ApiParam(value = "id of the center", required = true) @PathVariable("centerId") Long centerId)
			throws RestServiceException, ShanoirException, IOException {
		
		// Analyze folder
		if (bidsFile == null) {
			throw new RestServiceException(
					new ErrorModel(HttpStatus.UNPROCESSABLE_ENTITY.value(), NO_FILE_UPLOADED, null));
		}
		if (!ImportUtils.isZipFile(bidsFile)) {
			throw new RestServiceException(
					new ErrorModel(HttpStatus.UNPROCESSABLE_ENTITY.value(), WRONG_CONTENT_FILE_UPLOAD, null));
		}
		
        rabbitTemplate.setBeforePublishPostProcessors(message -> {
            message.getMessageProperties().setHeader("x-user-id",
            		KeycloakUtil.getTokenUserId());
            return message;
        });

		ImportJob importJob = new ImportJob();
		importJob.setStudyId(studyId);

		// Create tmp folder and unzip archive
		final File userImportDir = ImportUtils.getUserImportDir(importDir);
		File tempFile = ImportUtils.saveTempFile(userImportDir, bidsFile);
		File importJobDir = ImportUtils.saveTempFileCreateFolderAndUnzip(tempFile, bidsFile, false);
		
		importJob.setWorkFolder(importJobDir.getAbsolutePath());

		Long subjectId = null;
		for (File subjectFile : importJobDir.listFiles()) {
			String fileName = subjectFile.getName();
			if (fileName.startsWith("sub-")) {
				// We found a subject
				String subjectName = subjectFile.getName().split("sub-")[1];
				Subject subject = new Subject();
				subject.setName(subjectName);
				// Be carefull here, ID field is used to carry study id information
				subject.setId(studyId);
				importJob.setSubjectName(subjectName);
				
				System.err.println("We found a subject " + subjectName);

				// Create subject
				subjectId = (Long) rabbitTemplate.convertSendAndReceive(RabbitMQConfiguration.SUBJECTS_QUEUE, objectMapper.writeValueAsString(subject));
				
				importJob.setSubjectName(subjectName);

			} else {
				System.err.println("This is not a subject based, do something");
				throw new ShanoirException("The folder should start with sub-");
			}
			
			// Try to find sub-<label>_sessions.tsv file ?
			File[] sessionFiles = subjectFile.listFiles(new FilenameFilter() {
				@Override
				public boolean accept(File dir, String name) {
					return name.equals(fileName + "_sessions.tsv");
				}
			});
			
			// We found something interesting
			if (sessionFiles.length == 1) {
				File sessionFile = sessionFiles[0];
				// analyze date for every session
				
				// Set exam date
			}
			
			// Try to find acqusition_time in scans.tsv ?
			
			for (File sessionFile : subjectFile.listFiles()) {
				boolean examCreated = false;
				ExaminationDTO examination = null;
				Long examId = null;
				if (sessionFile.getName().startsWith("ses-")) {
					examination = createExam(studyId, centerId, subjectId, LocalDate.now());
					examCreated = true;

					// Create multiple examination for every session folder
					examId = (Long) rabbitTemplate.convertSendAndReceive(RabbitMQConfiguration.EXAMINATION_CREATION_QUEUE, objectMapper.writeValueAsString(examination));
					System.err.println("We found a session " + sessionFile.getName());
					importJob.setExaminationId(examId);

					for (File dataTypeFile : sessionFile.listFiles()) {
						importSession(dataTypeFile, importJob);
					}
				} else {
					// What if we find a extra-data file first ?
					if (!examCreated) {
						examination = createExam(studyId, centerId, subjectId, LocalDate.now());
						examId = (Long) rabbitTemplate.convertSendAndReceive(RabbitMQConfiguration.EXAMINATION_CREATION_QUEUE, objectMapper.writeValueAsString(examination));
						importJob.setExaminationId(examId);
						examCreated = true;
					}
					importSession(sessionFile, importJob);
				}
			}
		}
		return new ResponseEntity<>(null, HttpStatus.OK);
	}

	private ExaminationDTO createExam(Long studyId, Long centerId, Long subjectId, LocalDate examDate) {
		// Create one examination
		ExaminationDTO examination = new ExaminationDTO();
		IdName study = new IdName();
		study.setId(studyId);
		examination.setStudy(study);

		IdName subj = new IdName();
		subj.setId(subjectId);
		examination.setSubject(subj);
		
		IdName center = new IdName();
		center.setId(centerId);
		examination.setCenter(center);

		// TODO: change
		// FileTime creationTime = (FileTime) Files.getAttribute(path, "creationTime");
		examination.setExaminationDate(examDate);

		return examination;
	}

	private void importSession(File dataTypeFile, ImportJob importJob) throws AmqpException, JsonProcessingException {
		if (dataTypeFile.isDirectory()) {
			importJob.setWorkFolder(dataTypeFile.getAbsolutePath());
			System.err.println("We found a data folder " + dataTypeFile.getName());
			rabbitTemplate.convertAndSend(RabbitMQConfiguration.IMPORTER_BIDS_DATASET_QUEUE, objectMapper.writeValueAsString(importJob));
		} else {
			System.err.println("We found an examination extra-data " + dataTypeFile.getAbsolutePath());
			IdName extraData = new IdName(importJob.getExaminationId(), dataTypeFile.getAbsolutePath());
			this.rabbitTemplate.convertAndSend(RabbitMQConfiguration.EXAMINATION_EXTRA_DATA_QUEUE, objectMapper.writeValueAsString(extraData));			
		}
	}

}

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
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
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

	private static final String WRONG_CONTENT_FILE_UPLOAD = "Wrong content type of file upload, .zip required.";

	private static final String NO_FILE_UPLOADED = "No file uploaded.";

	@Autowired
	ImporterApiController importer;

	@Autowired
	private RabbitTemplate rabbitTemplate;

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
    		@ApiParam(value = "id of the study", required = true) @PathVariable("studyId") Long studyId)
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
		ImportJob importJob = new ImportJob();

		// Create tmp folder and unzip archive
		final File userImportDir = ImportUtils.getUserImportDir(importDir);

		File tempFile = ImportUtils.saveTempFile(userImportDir, bidsFile);

		File importJobDir = ImportUtils.saveTempFileCreateFolderAndUnzip(tempFile, bidsFile, false);
		
		importJob.setWorkFolder(importJobDir.getAbsolutePath());

		for (File subjectFile : importJobDir.listFiles()) {
			String fileName = subjectFile.getName();
			if (fileName.startsWith("sub-")) {
				// We found a subject
				String subjectName = subjectFile.getName().split("sub-")[1];
				Subject subject = new Subject();
				subject.setName(subjectName);
				
				System.err.println("We found a subject " + subjectName);

				// Create subject
				Long subjectId = (Long) rabbitTemplate.convertSendAndReceive(RabbitMQConfiguration.SUBJECTS_QUEUE, subject);
				
				importJob.setSubjectName(subjectName);

			} else {
				System.err.println("This is not a subject based, do something");
				throw new ShanoirException("The folder should start with sub-");
			}
			
			for (File sessionFile : subjectFile.listFiles()) {
				if (sessionFile.getName().startsWith("ses-")) {
					ExaminationDTO examination = new ExaminationDTO();
					examination.setStudy(null);
					examination.setSubject(null);
					examination.setExaminationDate(null);
					// Create multiple examination for every session folder
					Long examId = (Long) rabbitTemplate.convertSendAndReceive(RabbitMQConfiguration.EXAMINATION_CREATION_QUEUE, examination );
					System.err.println("We found a session " + sessionFile.getName());

					for (File dataTypeFile : sessionFile.listFiles()) {
						importSession(dataTypeFile, examId);
					}
				} else {
					// Create one examination
					ExaminationDTO examination = new ExaminationDTO();
					examination.setStudy(null);
					examination.setSubject(null);
					examination.setExaminationDate(null);

					Long examId = (Long) rabbitTemplate.convertSendAndReceive(RabbitMQConfiguration.EXAMINATION_CREATION_QUEUE, examination );
					importSession(sessionFile, examId);
				}
			}
		}
		return new ResponseEntity<>(null, HttpStatus.OK);
	}

	private void importSession(File dataTypeFile, Long examId) {
		if (dataTypeFile.isDirectory()) {
			System.err.println("We found a data folder " + dataTypeFile.getName());
			for (File datasetFile : dataTypeFile.listFiles()) {
				System.err.println("We found a dataset file " + datasetFile.getName());
				// Create dataset
				rabbitTemplate.convertSendAndReceive(RabbitMQConfiguration.CREATE_DATASET_ACQUISITION_QUEUE, datasetFile.getAbsolutePath());
			}
		} else {
			// Create examination extra-data
			System.err.println("We found an examination extra-data " + dataTypeFile.getAbsolutePath());
			this.rabbitTemplate.convertAndSend(RabbitMQConfiguration.EXAMINATION_EXTRA_DATA_QUEUE, dataTypeFile.getAbsolutePath());
		}
	}

}

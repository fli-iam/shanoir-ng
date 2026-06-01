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

package org.shanoir.ng.importer.bids;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.attribute.FileTime;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Map;

import org.shanoir.ng.importer.ImporterApiController;
import org.shanoir.ng.importer.dto.ExaminationDTO;
import org.shanoir.ng.importer.model.ImportJob;
import org.shanoir.ng.importer.model.Subject;
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
import com.fasterxml.jackson.databind.ObjectMapper;

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

    @Autowired
    private ImporterApiController importer;

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
                return !name.startsWith(".DS_Store") && !name.startsWith("__MAC") && !name.startsWith("._") && !name.startsWith(".AppleDouble");
            } })) {
            String fileName = subjectFile.getName();
            String subjectName = null;
            if (fileName.startsWith("sub-")) {
                // We found a subject
                subjectName = subjectFile.getName().split("sub-")[1];
                Subject subject = new Subject();
                subject.setName(subjectName);
                subject.setStudy(new IdName(studyId, studyName));
                importJob.setSubjectName(subjectName);

                // Create subject
                subjectId = (Long) rabbitTemplate.convertSendAndReceive(RabbitMQConfiguration.SUBJECTS_QUEUE_WITH_DATASETS, objectMapper.writeValueAsString(subject));
                if (subjectId == null) {
                    throw new RestServiceException(new ErrorModel(HttpStatus.UNPROCESSABLE_ENTITY.value(), SUBJECT_CREATION_ERROR, null));
                }
            } else {
                throw new RestServiceException(new ErrorModel(HttpStatus.UNPROCESSABLE_ENTITY.value(), NOT_SUBJECT_BASED_SUBJECT, null));
            }


            // STEP 3: Examination level, check if there are session, otherwise create examinations
            Map<String, LocalDate> examDates = BidsTsvDateParser.readDatesFromSessionsFile(subjectFile);
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
                    BidsExaminationDateResolution dateResolution = BidsExaminationDateResolution.resolve(
                            sessionFile, sessionLabel, examDates, creationTime, ZoneId.systemDefault());
                    if (dateResolution.isFallback()) {
                        LOG.warn("BIDS import subject [{}] session [{}]: {}", subjectName, sessionLabel,
                                dateResolution.getSourceDescription());
                    } else {
                        LOG.info("BIDS import subject [{}] session [{}]: examination date {} from {}",
                                subjectName, sessionLabel, dateResolution.getDate(), dateResolution.getSourceDescription());
                    }
                    examination = ImportUtils.createExam(studyId, centerId, subjectId, sessionLabel,
                            dateResolution.getDate(), subjectName);
                    examCreated = true;

                    // Create multiple examinations for every session folder
                    examId = (Long) rabbitTemplate.convertSendAndReceive(RabbitMQConfiguration.EXAMINATION_CREATION_QUEUE, objectMapper.writeValueAsString(examination));

                    if (examId == null) {
                        throw new RestServiceException(new ErrorModel(HttpStatus.UNPROCESSABLE_ENTITY.value(), EXAMINATION_CREATION_ERROR, null));
                    }
                    publishExaminationCreatedEvent(examId, examination, centerId, dateResolution);

                    importJob.setExaminationId(examId);

                    // STEP 4: Finish import from every bids data folder
                    for (File dataTypeFile : sessionFile.listFiles(
                            new FilenameFilter() {
                                @Override
                                public boolean accept(File arg0, String name) {
                                    return !name.startsWith(".DS_Store") && !name.startsWith("__MAC") && !name.startsWith("._") && !name.startsWith(".AppleDouble");
                                }
                            }
                    )) {
                        importSession(dataTypeFile, importJob);
                    }
                } else {
                    // STEP 3.2 No session level
                    if (!examCreated) {
                        BidsExaminationDateResolution dateResolution = BidsExaminationDateResolution
                                .resolveWithoutSessionFolder(sessionFile, creationTime);
                        if (dateResolution.isFallback()) {
                            LOG.warn("BIDS import subject [{}]: {}", subjectName, dateResolution.getSourceDescription());
                        }
                        examination = ImportUtils.createExam(studyId, centerId, subjectId, "",
                                dateResolution.getDate(), subjectName);
                        examId = (Long) rabbitTemplate.convertSendAndReceive(RabbitMQConfiguration.EXAMINATION_CREATION_QUEUE, objectMapper.writeValueAsString(examination));

                        if (examId == null) {
                            throw new RestServiceException(new ErrorModel(HttpStatus.UNPROCESSABLE_ENTITY.value(), EXAMINATION_CREATION_ERROR, null));
                        }
                        publishExaminationCreatedEvent(examId, examination, centerId, dateResolution);

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

    private void publishExaminationCreatedEvent(Long examId, ExaminationDTO examination, Long centerId,
            BidsExaminationDateResolution dateResolution) {
        String message = dateResolution.formatEventMessage() + ";centerId:" + centerId + ";subjectId:"
                + examination.getSubject().getId();
        eventService.publishEvent(new ShanoirEvent(ShanoirEventType.CREATE_EXAMINATION_EVENT, examId.toString(),
                KeycloakUtil.getTokenUserId(), message, ShanoirEvent.SUCCESS, examination.getStudyId()));
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

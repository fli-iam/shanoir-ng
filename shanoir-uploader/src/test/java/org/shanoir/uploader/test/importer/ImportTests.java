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

package org.shanoir.uploader.test.importer;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.nio.file.Files;
import java.security.NoSuchAlgorithmException;
import java.text.ParseException;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import javax.swing.JProgressBar;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.shanoir.ng.dicom.web.StudyInstanceUIDAndSubjectNameHandler;
import org.shanoir.ng.importer.dicom.ImagesCreatorAndDicomFileAnalyzerService;
import org.shanoir.ng.importer.model.ImportJob;
import org.shanoir.ng.importer.model.Patient;
import org.shanoir.ng.importer.model.Serie;
import org.shanoir.ng.importer.model.Study;
import org.shanoir.ng.importer.model.Subject;
import org.shanoir.uploader.ShUpConfig;
import org.shanoir.uploader.check.DicomInstanceConsistencyChecker;
import org.shanoir.uploader.dicom.anonymize.Anonymizer;
import org.shanoir.uploader.dicom.retrieve.DcmRcvManager;
import org.shanoir.uploader.exception.PseudonymusException;
import org.shanoir.uploader.model.rest.Examination;
import org.shanoir.uploader.model.rest.HemisphericDominance;
import org.shanoir.uploader.model.rest.ImagedObjectCategory;
import org.shanoir.uploader.model.rest.StudyCard;
import org.shanoir.uploader.model.rest.SubjectType;
import org.shanoir.uploader.test.AbstractTest;
import org.shanoir.uploader.utils.ImportUtils;
import org.shanoir.uploader.utils.Util;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonProcessingException;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class ImportTests extends AbstractTest {

    private static final Logger logger = LoggerFactory.getLogger(ImportTests.class);

    private static final String ACR_PHANTOM_T1_ZIP = "acr_phantom_t1.zip";

    private static final String ACR_PHANTOM_T1_DIR = "acr_phantom_t1/";

    // The server-side import is asynchronous: give it time to appear before
    // declaring the consistency check failed.
    private static final long CONSISTENCY_CHECK_TIMEOUT_MILLIS = 2 * 60 * 1000; // 2 min

    private static final long CONSISTENCY_CHECK_POLL_INTERVAL_MILLIS = 5 * 1000; // 5 sec

    private static org.shanoir.uploader.model.rest.Study study;

    @Test
    @Order(1)
    public void testImportFromDicomZip() {
        logger.info("START testImportFromDicomZip...................");
        try {
            study = createStudyAndCenterAndStudyCardAndAddMembers();
            ImportJob importJob = uploadDicomZip(ACR_PHANTOM_T1_ZIP);
            if (!importJob.getPatients().isEmpty()) {
                selectAllSeriesForImport(importJob);
                org.shanoir.uploader.model.rest.Subject subject = createSubject(importJob, study);
                Long examinationId = createExamination(study, importJob, subject);
                startImportJobFromZip(importJob, subject, examinationId, study);
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            Assertions.fail(e.getMessage());
        }
    }

    @Test
    @Order(2)
    public void testImportFromShanoirUploader() throws Exception {
        logger.info("START testImportFromShanoirUploader...................");
        URL resource = getClass().getClassLoader().getResource(ACR_PHANTOM_T1_DIR);
        Assertions.assertNotNull(resource, "Test resource folder " + ACR_PHANTOM_T1_DIR + " not found.");
        File dicomSourceDir = new File(resource.toURI());

        List<Patient> patients = ImportUtils.getPatientsFromDir(dicomSourceDir, true);
        Assertions.assertNotNull(patients);
        Assertions.assertFalse(patients.isEmpty(), "No patients found in test DICOM folder.");
        Patient patient = patients.get(0);
        Assertions.assertFalse(patient.getStudies().isEmpty(), "No studies found for parsed patient.");
        Study dicomStudy = patient.getStudies().get(0);
        selectAllSeriesForImport(patient);

        List<Serie> selectedSeries = new ArrayList<>();
        for (Serie serie : dicomStudy.getSeries()) {
            if (serie.getSelected()) {
                selectedSeries.add(serie);
            }
        }
        Assertions.assertFalse(selectedSeries.isEmpty(), "No series selected for import.");

        ImportJob importJob = ImportUtils.createNewImportJob(patient, dicomStudy);
        importJob.setSelectedSeries(selectedSeries);

        org.shanoir.uploader.model.rest.Subject subjectREST = createSubjectFromLocalPatient(patient, study);
        importJob.setSubject(patient.getSubject());
        Examination examination = createExaminationFromDicomStudy(study, dicomStudy, subjectREST);

        StudyCard studyCard = study.getStudyCards().get(0);
        importJob = ImportUtils.prepareImportJob(importJob, subjectREST.getName(), subjectREST.getId(),
                examination.getId(), examination.getStudyInstanceUID(), study, studyCard,
                studyCard.getAcquisitionEquipment());
        importJob.setFromDicomZip(false);
        importJob.setFromPacs(false);
        importJob.setFromShanoirUploader(true);

        File uploadFolder = Files.createTempDirectory("shanoir-uploader-test-upload-").toFile();
        ImagesCreatorAndDicomFileAnalyzerService dicomFileAnalyzer = new ImagesCreatorAndDicomFileAnalyzerService();
        JProgressBar progressBar = new JProgressBar();
        StringBuilder copyReport = new StringBuilder();
        List<String> copiedFileNames = ImportUtils.copyFilesToUploadFolder(
                progressBar, copyReport, dicomFileAnalyzer, selectedSeries, uploadFolder,
                dicomSourceDir.getAbsolutePath());
        Assertions.assertNotNull(copiedFileNames);
        Assertions.assertFalse(copiedFileNames.isEmpty(), "No DICOM files copied to local upload folder.");
        logger.info(copyReport.toString());

        Anonymizer anonymizer = new Anonymizer();
        String anonymizationProfile = ShUpConfig.profileProperties.getProperty(ShUpConfig.ANONYMIZATION_PROFILE);
        boolean anonymizationSuccess = anonymizer.pseudonymize(
                uploadFolder, anonymizationProfile, subjectREST.getName(), examination.getStudyInstanceUID());
        Assertions.assertTrue(anonymizationSuccess, "Local anonymization of DICOM files failed.");

        File importJobJsonFile = new File(uploadFolder, ShUpConfig.IMPORT_JOB_JSON);
        importJobJsonFile.createNewFile();
        Util.mapper.writeValue(importJobJsonFile, importJob);

        startImportJobFromShanoirUploader(importJob, uploadFolder);

        // Local files here were pseudonymized before upload, exactly like the
        // server-side copy, so a full tag comparison is meaningful.
        waitAndCheckServerConsistency(uploadFolder, examination.getId(), true);
    }

    /**
     * Polls the server until the local DICOM files match their remote,
     * persisted counterparts for the given examination, or a timeout is
     * reached. Wraps {@link DicomInstanceConsistencyChecker}, since the import
     * triggered by {@code startImportJob} is processed asynchronously on
     * the server and may not be immediately queryable.
     *
     * @param localDicomFolder folder containing the local DICOM files to
     *                         compare (flat, referencedFileID-named)
     * @param examinationId    the examination created for this import
     * @param compareTags      passed through to
     *                         {@link DicomInstanceConsistencyChecker#checkImportJob}
     */
    private void waitAndCheckServerConsistency(File localDicomFolder, Long examinationId, boolean compareTags)
            throws Exception {
        String examinationUID = StudyInstanceUIDAndSubjectNameHandler.PREFIX + examinationId;
        DicomInstanceConsistencyChecker checker = new DicomInstanceConsistencyChecker(userClient);

        // Parse the local DICOM folder once; do not delete the generated DICOMDIR
        // between retries, since the on-disk content never changes while polling.
        List<Patient> localPatients = checker.parseLocalFolder(localDicomFolder, false);

        long deadline = System.currentTimeMillis() + CONSISTENCY_CHECK_TIMEOUT_MILLIS;
        Exception lastError = null;
        while (System.currentTimeMillis() < deadline) {
            try {
                int numberChecked = checker.checkImportJob(localPatients, localDicomFolder, examinationUID,
                        compareTags, false);
                logger.info("Server-side consistency check OK: {} DICOM instance(s) matched for examination {}.",
                        numberChecked, examinationId);
                return;
            } catch (Exception e) {
                lastError = e;
                logger.debug("Import not yet available/consistent on server for examination {}, retrying: {}",
                        examinationId, e.getMessage());
                Thread.sleep(CONSISTENCY_CHECK_POLL_INTERVAL_MILLIS);
            }
        }
        Assertions.fail("DICOM instances were not consistent with the server within timeout for examination "
                + examinationId + (lastError != null ? ": " + lastError.getMessage() : ""));
    }

    private Long createExamination(org.shanoir.uploader.model.rest.Study study, ImportJob importJob,
            org.shanoir.uploader.model.rest.Subject subject) {
        Study dicomStudy = importJob.getPatients().get(0).getStudies().get(0);
        LocalDate studyDate = dicomStudy.getStudyDate();
        Instant studyDateInstant = studyDate.atStartOfDay(ZoneId.systemDefault()).toInstant();
        Date studyDateDate = Date.from(studyDateInstant);
        String examinationComment = dicomStudy.getStudyDescription();
        Examination examination = ImportUtils.createExamination(study, subject, studyDateDate,
                examinationComment, study.getStudyCards().get(0).getCenterId(), false);
        return examination.getId();
    }

    private Examination createExaminationFromDicomStudy(org.shanoir.uploader.model.rest.Study study, Study dicomStudy,
            org.shanoir.uploader.model.rest.Subject subjectREST) {
        LocalDate studyDate = dicomStudy.getStudyDate();
        Instant studyDateInstant = studyDate.atStartOfDay(ZoneId.systemDefault()).toInstant();
        Date studyDateDate = Date.from(studyDateInstant);
        String examinationComment = dicomStudy.getStudyDescription();
        Examination examination = ImportUtils.createExamination(study, subjectREST, studyDateDate,
                examinationComment, study.getStudyCards().get(0).getCenterId(), false);
        Assertions.assertNotNull(examination, "Examination could not be created.");
        return examination;
    }

    private org.shanoir.uploader.model.rest.Subject createSubjectFromLocalPatient(Patient patient,
            org.shanoir.uploader.model.rest.Study study)
            throws UnsupportedEncodingException, NoSuchAlgorithmException, PseudonymusException, ParseException {
        final String randomPatientName = "Subject-" + UUID.randomUUID().toString();
        Subject subject = ImportUtils.createSubjectFromPatient(patient, pseudonymizer, identifierCalculator);
        org.shanoir.uploader.model.rest.Subject subjectREST = ImportUtils.manageSubject(
                null, subject, randomPatientName, ImagedObjectCategory.LIVING_HUMAN_BEING,
                HemisphericDominance.Left.toString(), HemisphericDominance.Left.toString(),
                SubjectType.PATIENT, false, false, randomPatientName, study,
                study.getStudyCards().get(0).getAcquisitionEquipment());
        Assertions.assertNotNull(subjectREST, "Subject could not be created from local patient.");
        subject.setImagedObjectCategory(null); // to fix server issue with incompatible mapping value
        patient.setSubject(subject);
        return subjectREST;
    }

    private void startImportJobFromShanoirUploader(ImportJob importJob, File uploadFolder) throws Exception {
        File[] dicomFiles = uploadFolder.listFiles(
                (dir, name) -> name.endsWith(DcmRcvManager.DICOM_FILE_SUFFIX));
        Assertions.assertNotNull(dicomFiles);
        Assertions.assertTrue(dicomFiles.length > 0, "No anonymized DICOM files found in upload folder.");

        String tempDirId = userClient.createTempDir();
        Assertions.assertNotNull(tempDirId);
        logger.info("Upload: tempDirId for import: " + tempDirId);

        int i = 0;
        for (File file : dicomFiles) {
            i++;
            userClient.uploadFile(tempDirId, file);
            logger.debug("Uploaded file {}/{}: {}", i, dicomFiles.length, file.getName());
        }
        logger.info("Upload: " + dicomFiles.length + " uploaded files to tempDirId: " + tempDirId);

        importJob.setWorkFolder(tempDirId);
        String importJobJson = Util.objectWriter.writeValueAsString(importJob);
        userClient.startImportJob(importJobJson);
    }

    private void startImportJobFromZip(ImportJob importJob, org.shanoir.uploader.model.rest.Subject subjectREST,
            Long examinationId, org.shanoir.uploader.model.rest.Study study)
            throws JsonProcessingException, Exception {
        importJob.setStudyId(study.getId());
        importJob.setStudyName(study.getName());
        StudyCard studyCard = study.getStudyCards().get(0);
        importJob.setStudyCardId(studyCard.getId());
        importJob.setStudyCardName(studyCard.getName());
        importJob.setAcquisitionEquipmentId(studyCard.getAcquisitionEquipment().getId());
        importJob.setExaminationId(examinationId);
        if (ShUpConfig.isModeSubjectNameManual()) {
            importJob.setAnonymisationProfileToUse("Profile Neurinfo");
        } else {
            importJob.setAnonymisationProfileToUse("Profile OFSEP");
        }
        String importJobJson = Util.objectWriter.writeValueAsString(importJob);
        userClient.startImportJob(importJobJson);
    }

    private org.shanoir.uploader.model.rest.Subject createSubject(ImportJob importJob,
            org.shanoir.uploader.model.rest.Study study)
            throws UnsupportedEncodingException, NoSuchAlgorithmException, PseudonymusException, ParseException {
        Patient patient = importJob.getPatients().get(0);
        final String randomPatientName = "Subject-" + UUID.randomUUID().toString();
        Subject subject = ImportUtils.createSubjectFromPatient(patient, pseudonymizer, identifierCalculator);
        org.shanoir.uploader.model.rest.Subject subjectREST = ImportUtils.manageSubject(
                null, subject, randomPatientName, ImagedObjectCategory.LIVING_HUMAN_BEING,
                HemisphericDominance.Left.toString(), HemisphericDominance.Left.toString(),
                SubjectType.PATIENT, false, false, randomPatientName, study,
                study.getStudyCards().get(0).getAcquisitionEquipment());
        subject.setImagedObjectCategory(null); // to fix server issue with incompatible mapping value
        org.shanoir.ng.importer.model.Subject subjectForImportJob = new org.shanoir.ng.importer.model.Subject();
        subjectForImportJob.setId(subjectREST.getId());
        subjectForImportJob.setName(subjectREST.getName());
        patient.setSubject(subjectForImportJob);
        importJob.setSubjectName(subjectREST.getName());
        return subjectREST;
    }

    private void selectAllSeriesForImport(ImportJob importJob) {
        List<Patient> patients = importJob.getPatients();
        for (Patient patient : patients) {
            List<Study> studies = patient.getStudies();
            for (Study study : studies) {
                List<Serie> series = study.getSeries();
                for (Serie serie : series) {
                    serie.setSelected(true);
                }
            }
        }
    }

    private void selectAllSeriesForImport(Patient patient) {
        for (Study study : patient.getStudies()) {
            for (Serie serie : study.getSeries()) {
                serie.setSelected(true);
            }
        }
    }

    private ImportJob uploadDicomZip(final String fileName) {
        try {
            URL resource = getClass().getClassLoader().getResource(fileName);
            if (resource != null) {
                File file = new File(resource.toURI());
                return userClient.uploadDicom(file);
            }
        } catch (Exception e) {
            logger.error("Error while reading file: ", e);
        }
        return null;
    }

}
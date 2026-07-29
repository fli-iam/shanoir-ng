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
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.security.NoSuchAlgorithmException;
import java.text.ParseException;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.UUID;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import javax.swing.JProgressBar;

import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.core5.http.HttpEntity;
import org.dcm4che3.data.Attributes;
import org.dcm4che3.data.Tag;
import org.dcm4che3.io.DicomInputStream;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.shanoir.ng.dicom.web.StudyInstanceUIDAndSubjectNameHandler;
import org.shanoir.ng.importer.ImportJobStatusService;
import org.shanoir.ng.importer.dicom.ImagesCreatorAndDicomFileAnalyzerService;
import org.shanoir.ng.importer.model.EegImportJob;
import org.shanoir.ng.importer.model.ImportJobBase;
import org.shanoir.ng.importer.model.ImportJobStatus;
import org.shanoir.ng.importer.model.Patient;
import org.shanoir.ng.importer.model.Serie;
import org.shanoir.ng.importer.model.Subject;
import org.shanoir.uploader.ShUpConfig;
import org.shanoir.uploader.check.DicomInstanceConsistencyChecker;
import org.shanoir.uploader.dicom.DicomServerClient;
import org.shanoir.uploader.dicom.anonymize.Anonymizer;
import org.shanoir.uploader.dicom.retrieve.DcmRcvManager;
import org.shanoir.uploader.exception.PseudonymusException;
import org.shanoir.uploader.model.rest.AcquisitionEquipment;
import org.shanoir.uploader.model.rest.DatasetLight;
import org.shanoir.uploader.model.rest.DatasetsImportStatus;
import org.shanoir.uploader.model.rest.Examination;
import org.shanoir.uploader.model.rest.HemisphericDominance;
import org.shanoir.uploader.model.rest.ImagedObjectCategory;
import org.shanoir.uploader.model.rest.Study;
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

    private static final String TEST_MULTIPLE_EXAM_ZIP = "TEST_MET_0001.zip";

    private static final String TEST_EEG_ZIP = "testEDF.zip";

    private static final String TEST_BIDS_ZIP = "testBIDS.zip";
    
    private static final String TEST_PACS_AET = "TEST_PACS";

    private static final int TEST_PACS_PORT = 11121;

    // The server-side import is asynchronous: give it time to appear before
    // declaring the consistency check failed.
    private static final long CONSISTENCY_CHECK_TIMEOUT_MILLIS = 2 * 60 * 1000; // 2 min

    private static final long CONSISTENCY_CHECK_POLL_INTERVAL_MILLIS = 5 * 1000; // 5 sec
    
    private static final long DATASET_STATUS_TIMEOUT_MILLIS = 2 * 60 * 1000;

    private static final long DATASET_STATUS_POLL_INTERVAL_MILLIS = 5 * 1000;

    // Local folder where a copy of every import-job JSON sent is dumped
    private static final File IMPORT_JOB_DUMP_DIR = new File("target/import-job-dumps");

    private static final DateTimeFormatter IMPORT_JOB_DUMP_TIMESTAMP_FORMATTER =
            DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss-SSS");

    private static Study studyWithStudyCards;

    private static Study studyNoStudyCards;

    private static AcquisitionEquipment equipment;

    @Test
    @Order(1)
    public void testImportFromDicomZip() {
        logger.info("......................................................");
        logger.info("START testImportFromDicomZip..........................");
        logger.info("......................................................");
        try {
            ImportJobBase importJob = uploadDicomZip(ACR_PHANTOM_T1_ZIP);
            logger.info("TempDirId: {}", importJob.getWorkFolder());
            studyWithStudyCards = createStudyAndCenterAndStudyCardAndAddMembers();
            if (!importJob.getSeries().isEmpty()) {
                selectAllSeriesForImport(importJob);
                org.shanoir.uploader.model.rest.Subject subject = createSubject(importJob, studyWithStudyCards);
                org.shanoir.ng.importer.model.Study dicomStudy = importJob.getStudy();
                Examination examination = createExaminationFromDicomStudy(studyWithStudyCards, dicomStudy, subject);
                startImportJobFromDicomZip(importJob, subject, examination, studyWithStudyCards);
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            Assertions.fail(e.getMessage());
        }
    }

    @Test
    @Order(2)
    public void testImportFromShanoirUploader() throws Exception {
        logger.info("......................................................");
        logger.info("START testImportFromShanoirUploader...................");
        logger.info("......................................................");
        URL resource = getClass().getClassLoader().getResource(ACR_PHANTOM_T1_DIR);
        Assertions.assertNotNull(resource, "Test resource folder " + ACR_PHANTOM_T1_DIR + " not found.");
        File dicomSourceDir = new File(resource.toURI());

        List<Patient> patients = ImportUtils.getPatientsFromDir(dicomSourceDir, true);
        Assertions.assertNotNull(patients);
        Assertions.assertFalse(patients.isEmpty(), "No patients found in test DICOM folder.");
        Patient patient = patients.get(0);
        Assertions.assertFalse(patient.getStudies().isEmpty(), "No studies found for parsed patient.");
        org.shanoir.ng.importer.model.Study dicomStudy = patient.getStudies().get(0);

        List<Serie> selectedSeries = new ArrayList<>();
        for (Serie serie : dicomStudy.getSeries()) {
            serie.setSelected(true);
            selectedSeries.add(serie);
        }
        Assertions.assertFalse(selectedSeries.isEmpty(), "No series selected for import.");

        ImportJobBase importJob = ImportUtils.createNewImportJob(patient, dicomStudy);
        importJob.setSeries(selectedSeries);

        org.shanoir.uploader.model.rest.Subject subject = createSubjectFromLocalPatient(patient, studyWithStudyCards);
        importJob.setSubject(patient.getSubject());
        Examination examination = createExaminationFromDicomStudy(studyWithStudyCards, dicomStudy, subject);

        StudyCard studyCard = studyWithStudyCards.getStudyCards().get(0);
        importJob = ImportUtils.prepareImportJob(importJob, subject.getName(), subject.getId(),
                examination.getId(), examination.getStudyInstanceUID(), studyWithStudyCards, studyCard,
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
                uploadFolder, anonymizationProfile, subject.getName(), examination.getStudyInstanceUID());
        Assertions.assertTrue(anonymizationSuccess, "Local anonymization of DICOM files failed.");

        File importJobJsonFile = new File(uploadFolder, ShUpConfig.IMPORT_JOB_JSON);
        importJobJsonFile.createNewFile();
        Util.mapper.writeValue(importJobJsonFile, importJob);

        startImportJobFromShanoirUploader(importJob, uploadFolder, "testImportFromShanoirUploader");
        waitForServerImportJobStatus(importJob.getWorkFolder(), "testImportFromShanoirUploader-before-ds");

        // Local files here were pseudonymized before upload, exactly like the
        // server-side copy, so a full tag comparison is meaningful.
        waitAndCheckServerConsistency(uploadFolder, examination.getId(), true);
        downloadAndCompareDatasetsZip(examination.getId(), uploadFolder, "testImportFromShanoirUploader");
    }

    @Test
    @Order(3)
    public void testImportFromDicomZipNoStudyCard() {
        logger.info("......................................................");
        logger.info("START testImportFromDicomZipNoStudyCard...............");
        logger.info("......................................................");
        try {
            ImportJobBase importJob = uploadDicomZip(ACR_PHANTOM_T1_ZIP);
            logger.info("TempDirId: {}", importJob.getWorkFolder());
            studyNoStudyCards = createStudyAndCenterWithoutStudyCard();
            equipment = createEquipment(
                    studyNoStudyCards.getStudyCenterList().get(0).getCenter());
            Assertions.assertNotNull(equipment);
            if (!importJob.getSeries().isEmpty()) {
                selectAllSeriesForImport(importJob);
                org.shanoir.uploader.model.rest.Subject subject = createSubjectNoStudyCard(importJob, studyNoStudyCards);
                org.shanoir.ng.importer.model.Study dicomStudy = importJob.getStudy();
                Examination examination = createExaminationNoStudyCard(studyNoStudyCards, dicomStudy, subject);
                startImportJobFromDicomZipNoStudyCard(importJob, subject, examination, studyNoStudyCards, equipment.getId());
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            Assertions.fail(e.getMessage());
        }
    }

    @Test
    @Order(4)
    public void testImportFromShanoirUploaderNoStudyCard() throws Exception {
        logger.info("......................................................");
        logger.info("START testImportFromShanoirUploaderNoStudyCard........");
        logger.info("......................................................");
        URL resource = getClass().getClassLoader().getResource(ACR_PHANTOM_T1_DIR);
        Assertions.assertNotNull(resource, "Test resource folder " + ACR_PHANTOM_T1_DIR + " not found.");
        File dicomSourceDir = new File(resource.toURI());

        List<Patient> patients = ImportUtils.getPatientsFromDir(dicomSourceDir, true);
        Assertions.assertNotNull(patients);
        Assertions.assertFalse(patients.isEmpty(), "No patients found in test DICOM folder.");
        Patient patient = patients.get(0);
        Assertions.assertFalse(patient.getStudies().isEmpty(), "No studies found for parsed patient.");
        org.shanoir.ng.importer.model.Study dicomStudy = patient.getStudies().get(0);

        List<Serie> selectedSeries = new ArrayList<>();
        for (Serie serie : dicomStudy.getSeries()) {
            serie.setSelected(true);
            selectedSeries.add(serie);
        }
        Assertions.assertFalse(selectedSeries.isEmpty(), "No series selected for import.");

        ImportJobBase importJob = ImportUtils.createNewImportJob(patient, dicomStudy);
        importJob.setSeries(selectedSeries);

        org.shanoir.uploader.model.rest.Subject subject = createSubjectFromLocalPatientNoStudyCard(patient,
                studyNoStudyCards);
        importJob.setSubject(patient.getSubject());
        Examination examination = createExaminationNoStudyCard(studyNoStudyCards, dicomStudy, subject);

        // No study card for this study (SC_DISABLED policy): pass null instead
        // of a StudyCard/AcquisitionEquipment, mirroring the client-side "no
        // study card" import path.
        importJob = ImportUtils.prepareImportJob(importJob, subject.getName(), subject.getId(),
                examination.getId(), examination.getStudyInstanceUID(), studyNoStudyCards, null, equipment);
        importJob.setFromDicomZip(false);
        importJob.setFromPacs(false);
        importJob.setFromShanoirUploader(true);

        File uploadFolder = Files.createTempDirectory("shanoir-uploader-test-upload-nostudycard-").toFile();
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
                uploadFolder, anonymizationProfile, subject.getName(), examination.getStudyInstanceUID());
        Assertions.assertTrue(anonymizationSuccess, "Local anonymization of DICOM files failed.");

        File importJobJsonFile = new File(uploadFolder, ShUpConfig.IMPORT_JOB_JSON);
        importJobJsonFile.createNewFile();
        Util.mapper.writeValue(importJobJsonFile, importJob);

        startImportJobFromShanoirUploader(importJob, uploadFolder, "testImportFromShanoirUploaderNoStudyCard");
        waitForServerImportJobStatus(importJob.getWorkFolder(), "testImportFromShanoirUploaderNoStudyCard-before-ds");
        waitAndCheckServerConsistency(uploadFolder, examination.getId(), true);
        downloadAndCompareDatasetsZip(examination.getId(), uploadFolder, "testImportFromShanoirUploaderNoStudyCard");
    }

    @Test
    @Order(5)
    public void testImportMultipleDicomZipWithStudyCard() throws Exception {
        logger.info("......................................................");
        logger.info("START testImportMultipleDicomZipWithStudyCard.........");
        logger.info("......................................................");
        URL resource = getClass().getClassLoader().getResource(TEST_MULTIPLE_EXAM_ZIP);
        Assertions.assertNotNull(resource, "Test resource " + TEST_MULTIPLE_EXAM_ZIP + " not found.");
        File file = new File(resource.toURI());

        StudyCard studyCard = studyWithStudyCards.getStudyCards().get(0);
        ImportJobBase importJob = userClient.uploadMultipleDicom(file,
                studyWithStudyCards.getId(),
                studyWithStudyCards.getName(),
                studyCard.getId(),
                studyCard.getCenterId(),
                studyCard.getAcquisitionEquipment().getId());

        Assertions.assertNotNull(importJob, "Multiple-examination import returned no import job.");
        Assertions.assertNotNull(importJob.getExaminationId(),
                "Multiple-examination import did not create/return an examination id.");
        logger.info("Multiple-examination import (with study card) created examination: {}",
                importJob.getExaminationId());

        if (importJob.getWorkFolder() != null && !importJob.getWorkFolder().isEmpty()) {
            final String tempDirId = ImportJobStatusService.keyOf(importJob.getWorkFolder());
            waitForServerImportJobStatus(tempDirId, "testImportMultipleDicomZipWithStudyCard-before-ds");
        }
        File multiExamExtractDir = Files.createTempDirectory("shanoir-multi-exam-source-").toFile();
        unzip(file, multiExamExtractDir);
        downloadAndCompareDatasetsZip(importJob.getExaminationId(), multiExamExtractDir, "testImportMultipleDicomZipWithStudyCard");
    }

    @Test
    @Order(6)
    public void testImportMultipleDicomZipNoStudyCard() throws Exception {
        logger.info("......................................................");
        logger.info("START testImportMultipleDicomZipNoStudyCard...........");
        logger.info("......................................................");
        URL resource = getClass().getClassLoader().getResource(TEST_MULTIPLE_EXAM_ZIP);
        Assertions.assertNotNull(resource, "Test resource " + TEST_MULTIPLE_EXAM_ZIP + " not found.");
        File file = new File(resource.toURI());

        Long centerId = studyNoStudyCards.getStudyCenterList().get(0).getCenter().getId();
        // studyCardId 0L: mirrors the SC_DISABLED / "no study card" policy of this
        // study.
        ImportJobBase importJob = userClient.uploadMultipleDicom(file,
                studyNoStudyCards.getId(),
                studyNoStudyCards.getName(),
                0L,
                centerId,
                equipment.getId());

        Assertions.assertNotNull(importJob, "Multiple-examination import returned no import job.");
        Assertions.assertNotNull(importJob.getExaminationId(),
                "Multiple-examination import did not create/return an examination id.");
        logger.info("Multiple-examination import (no study card) created examination: {}",
                importJob.getExaminationId());

        if (importJob.getWorkFolder() != null && !importJob.getWorkFolder().isEmpty()) {
            final String tempDirId = ImportJobStatusService.keyOf(importJob.getWorkFolder());
            waitForServerImportJobStatus(tempDirId, "testImportMultipleDicomZipNoStudyCard-before-ds");
        }
        File multiExamExtractDir = Files.createTempDirectory("shanoir-multi-exam-source-").toFile();
        unzip(file, multiExamExtractDir);
        downloadAndCompareDatasetsZip(importJob.getExaminationId(), multiExamExtractDir, "testImportMultipleDicomZipNoStudyCard");
    }

    @Test
    @Order(7)
    public void testImportEEG() throws Exception {
        logger.info("......................................................");
        logger.info("START testImportEEG...................................");
        logger.info("......................................................");
        URL resource = getClass().getClassLoader().getResource(TEST_EEG_ZIP);
        Assertions.assertNotNull(resource, "Test resource " + TEST_EEG_ZIP + " not found.");
        File file = new File(resource.toURI());

        // 1. Upload the EEG zip: server unzips it and returns a first, near-empty job.
        EegImportJob uploadedJob = userClient.uploadEEGZip(file);
        Assertions.assertNotNull(uploadedJob, "EEG upload did not return an import job.");
        Assertions.assertNotNull(uploadedJob.getWorkFolder(), "EEG upload did not return a work folder.");
        logger.info("EEG file uploaded, workFolder: {}", uploadedJob.getWorkFolder());

        // 2. Analyze the uploaded file(s) to build channels/events/datasets.
        EegImportJob analyzedJob = userClient.analyzeEegZipFile(uploadedJob);
        Assertions.assertNotNull(analyzedJob, "EEG analysis did not return an import job.");
        Assertions.assertNotNull(analyzedJob.getDatasets(), "EEG analysis did not return any datasets.");
        Assertions.assertFalse(analyzedJob.getDatasets().isEmpty(), "EEG analysis returned an empty dataset list.");
        logger.info("EEG file analyzed, {} dataset(s) found.", analyzedJob.getDatasets().size());

        // 3. Create the subject/examination that will receive the dataset(s), then
        // start the import.
        org.shanoir.uploader.model.rest.Subject subject = createSubject(studyNoStudyCards);
        Assertions.assertNotNull(subject, "Subject could not be created for EEG import.");
        Examination examination = createExamination(studyWithStudyCards.getId(), subject.getId(),
                studyWithStudyCards.getStudyCards().get(0).getCenterId());
        Assertions.assertNotNull(examination, "Examination could not be created for EEG import.");

        analyzedJob.setStudyId(studyNoStudyCards.getId());
        analyzedJob.setStudyName(studyNoStudyCards.getName());
        analyzedJob.setSubjectName(subject.getName());
        analyzedJob.setExaminationId(examination.getId());
        analyzedJob.setAcquisitionEquipmentId(equipment.getId());

        // MS Import is only relaying the job to MS Datasets
        userClient.startImportEEGJob(analyzedJob);
        if (analyzedJob.getWorkFolder() != null && !analyzedJob.getWorkFolder().isEmpty()) {
            final String tempDirId = ImportJobStatusService.keyOf(analyzedJob.getWorkFolder());
            waitForServerImportJobStatus(tempDirId, "testImportEEG-before-ds");
        }
    }

    @Test
    @Order(8)
    public void testImportBIDS() throws Exception {
        logger.info("......................................................");
        logger.info("START testImportBIDS...................................");
        logger.info("......................................................");
        URL resource = getClass().getClassLoader().getResource(TEST_BIDS_ZIP);
        Assertions.assertNotNull(resource, "Test resource " + TEST_BIDS_ZIP + " not found.");
        File file = new File(resource.toURI());

        Long centerId = studyWithStudyCards.getStudyCards().get(0).getCenterId();

        // Unlike the DICOM/EEG import paths, the BIDS import endpoint (datasets
        // microservice) responds with an empty 200 OK and processes the subject/
        // examination/dataset creation asynchronously over RabbitMQ. There is no
        // workFolder/tempDirId returned, so we cannot poll ImporterStatus here as
        // the other tests do - this only verifies the server accepted the upload.
        userClient.uploadBIDSDataset(file, studyWithStudyCards.getId(), studyWithStudyCards.getName(), centerId);
        logger.info("BIDS dataset upload accepted by server for study: {}", studyWithStudyCards.getId());
    }
    
    @Test
    @Order(9)
    public void testImportFromPACS() throws Exception {
        logger.info("......................................................");
        logger.info("START testImportFromPACS..............................");
        logger.info("......................................................");

        URL resource = getClass().getClassLoader().getResource(ACR_PHANTOM_T1_DIR);
        Assertions.assertNotNull(resource, "Test resource folder " + ACR_PHANTOM_T1_DIR + " not found.");
        File dicomSourceDir = new File(resource.toURI());
        Set<String[]> sopClassesAndTransferSyntaxes =
                TestDicomServerSeeder.scanSopClassesAndTransferSyntaxes(dicomSourceDir);

        File pacsStorageRoot = Files.createTempDirectory("shanoir-uploader-test-pacs-storage-").toFile();
        TestDicomServer pacs = new TestDicomServer(TEST_PACS_AET, TEST_PACS_PORT, pacsStorageRoot,
                sopClassesAndTransferSyntaxes);
        pacs.addRemoteConnection("SHANOIR-UPLOADER", "127.0.0.1", 44105);
        pacs.start();
        try {
            // Seed the fake PACS exactly once, via a real C-STORE - mirrors data
            // that would already sit on a real PACS before ShUp queries it.
            TestDicomServerSeeder.seed(dicomSourceDir, sopClassesAndTransferSyntaxes,
                    "SEEDER", TEST_PACS_AET, "127.0.0.1", TEST_PACS_PORT);

            Properties dicomServerProperties = new Properties();
            dicomServerProperties.setProperty("dicom.server.host", "127.0.0.1");
            dicomServerProperties.setProperty("dicom.server.port", String.valueOf(TEST_PACS_PORT));
            dicomServerProperties.setProperty("dicom.server.aet.called", TEST_PACS_AET);
            dicomServerProperties.setProperty("local.dicom.server.aet.calling", "SHANOIR-UPLOADER");
            dicomServerProperties.setProperty("local.dicom.server.host", "127.0.0.1");
            dicomServerProperties.setProperty("local.dicom.server.port", "44105");

            File workFolder = Files.createTempDirectory("shanoir-uploader-test-pacs-work-").toFile();
            DicomServerClient dicomServerClient = new DicomServerClient(dicomServerProperties, workFolder);

            List<Patient> patients = dicomServerClient.queryDicomServer(
                    true, "MR", "*", "", "", "", "");
            Assertions.assertNotNull(patients);
            Assertions.assertFalse(patients.isEmpty(), "PACS C-FIND returned no patients.");

            Patient patient = patients.get(0);
            org.shanoir.ng.importer.model.Study dicomStudy = patient.getStudies().get(0);
            List<Serie> selectedSeries = new ArrayList<>();
            for (Serie serie : dicomStudy.getSeries()) {
                serie.setSelected(true);
                selectedSeries.add(serie);
            }
            Assertions.assertFalse(selectedSeries.isEmpty(), "No series returned by PACS C-FIND.");

            File uploadFolder = ImportUtils.createUploadFolder(workFolder, "pacs-test-subject");
            List<String> retrievedFiles = dicomServerClient.retrieveDicomFiles(
                    new JProgressBar(), new StringBuilder(), dicomStudy.getStudyInstanceUID(),
                    selectedSeries, uploadFolder);
            Assertions.assertNotNull(retrievedFiles);
            Assertions.assertFalse(retrievedFiles.isEmpty(), "No DICOM files retrieved via C-MOVE.");
            
            ImportJobBase importJob = ImportUtils.createNewImportJob(patient, dicomStudy);
            importJob.setSeries(selectedSeries);
            ImagesCreatorAndDicomFileAnalyzerService dicomFileAnalyzer = new ImagesCreatorAndDicomFileAnalyzerService();
            for (Serie serie : selectedSeries) {
                dicomFileAnalyzer.getAdditionalMetaDataFromFirstInstanceOfSerie(uploadFolder.getAbsolutePath(), importJob.getPatient(),
                        importJob.getStudy(), serie, true);
            }

            org.shanoir.uploader.model.rest.Subject subject =
                    createSubjectFromLocalPatient(patient, studyWithStudyCards);
            importJob.setSubject(patient.getSubject());
            Examination examination = createExaminationFromDicomStudy(studyWithStudyCards, dicomStudy, subject);

            StudyCard studyCard = studyWithStudyCards.getStudyCards().get(0);
            importJob = ImportUtils.prepareImportJob(importJob, subject.getName(), subject.getId(),
                    examination.getId(), examination.getStudyInstanceUID(), studyWithStudyCards, studyCard,
                    studyCard.getAcquisitionEquipment());
            importJob.setFromShanoirUploader(true);
            importJob.setFromDicomZip(false);
            importJob.setFromPacs(false);

            Anonymizer anonymizer = new Anonymizer();
            String anonymizationProfile = ShUpConfig.profileProperties.getProperty(ShUpConfig.ANONYMIZATION_PROFILE);
            boolean anonymizationSuccess = anonymizer.pseudonymize(
                    uploadFolder, anonymizationProfile, subject.getName(), examination.getStudyInstanceUID());
            Assertions.assertTrue(anonymizationSuccess, "Local anonymization of PACS-retrieved DICOM files failed.");

            File importJobJsonFile = new File(uploadFolder, ShUpConfig.IMPORT_JOB_JSON);
            importJobJsonFile.createNewFile();
            Util.mapper.writeValue(importJobJsonFile, importJob);

            startImportJobFromShanoirUploader(importJob, uploadFolder, "testImportFromPACS");
            waitForServerImportJobStatus(importJob.getWorkFolder(), "testImportFromPACS-before-ds");
            waitAndCheckServerConsistency(uploadFolder, examination.getId(), true);
            downloadAndCompareDatasetsZip(examination.getId(), uploadFolder, "testImportFromPACS");
        } finally {
            pacs.stop();
        }
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

    private ImportJobStatus waitForServerImportJobStatus(String tempDirId, String label) throws Exception {
        long deadline = System.currentTimeMillis() + CONSISTENCY_CHECK_TIMEOUT_MILLIS;
        while (System.currentTimeMillis() < deadline) {
            ImportJobStatus status = userClient.getImportJobStatus(tempDirId);
            if (status != null) {
                if (status.getState() == ImportJobStatus.State.FINISHED) {
                    logger.info("Server reported FINISHED for tempDirId {}.", tempDirId);
                    ImportJobBase importJob = status.getImportJob();
                    String importJobJson = Util.objectWriter.writeValueAsString(importJob);
                    dumpImportJobJson(importJobJson, tempDirId, label);
                    return status;
                } else if (status.getState() == ImportJobStatus.State.ERROR) {
                    Assertions.fail("Import failed on server for tempDirId " + tempDirId + ": " + status.getMessage());
                }
            }
            Thread.sleep(CONSISTENCY_CHECK_POLL_INTERVAL_MILLIS);
        }
        Assertions.fail("Import did not reach FINISHED on server within timeout for tempDirId " + tempDirId);
        return null;
    }

    private Examination createExaminationFromDicomStudy(Study study,
            org.shanoir.ng.importer.model.Study dicomStudy,
            org.shanoir.uploader.model.rest.Subject subject) {
        LocalDate studyDate = dicomStudy.getStudyDate();
        Instant studyDateInstant = studyDate.atStartOfDay(ZoneId.systemDefault()).toInstant();
        Date studyDateDate = Date.from(studyDateInstant);
        String examinationComment = dicomStudy.getStudyDescription();
        Examination examination = ImportUtils.createExamination(study, subject, studyDateDate,
                examinationComment, study.getStudyCards().get(0).getCenterId(), false);
        Assertions.assertNotNull(examination, "Examination could not be created.");
        return examination;
    }

    /**
     * Same as {@link #createExaminationFromDicomStudy} but for a study without
     * a study card: the center is taken directly from the study's (single)
     * center, as set up by
     * {@link AbstractTest#createStudyAndCenterWithoutStudyCard()},
     * instead of from a (non-existent) study card.
     */
    private Examination createExaminationNoStudyCard(Study study,
            org.shanoir.ng.importer.model.Study dicomStudy,
            org.shanoir.uploader.model.rest.Subject subject) {
        LocalDate studyDate = dicomStudy.getStudyDate();
        Instant studyDateInstant = studyDate.atStartOfDay(ZoneId.systemDefault()).toInstant();
        Date studyDateDate = Date.from(studyDateInstant);
        String examinationComment = dicomStudy.getStudyDescription();
        Long centerId = study.getStudyCenterList().get(0).getCenter().getId();
        Examination examination = ImportUtils.createExamination(study, subject, studyDateDate,
                examinationComment, centerId, false);
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

    /**
     * Same as {@link #createSubjectFromLocalPatient} but for a study without a
     * study card: {@code null} is passed instead of an
     * {@link org.shanoir.uploader.model.rest.AcquisitionEquipment}, matching
     * the "no study card" import path.
     */
    private org.shanoir.uploader.model.rest.Subject createSubjectFromLocalPatientNoStudyCard(Patient patient,
            org.shanoir.uploader.model.rest.Study study)
            throws UnsupportedEncodingException, NoSuchAlgorithmException, PseudonymusException, ParseException {
        final String randomPatientName = "Subject-" + UUID.randomUUID().toString();
        Subject subject = ImportUtils.createSubjectFromPatient(patient, pseudonymizer, identifierCalculator);
        org.shanoir.uploader.model.rest.Subject subjectREST = ImportUtils.manageSubject(
                null, subject, randomPatientName, ImagedObjectCategory.LIVING_HUMAN_BEING,
                HemisphericDominance.Left.toString(), HemisphericDominance.Left.toString(),
                SubjectType.PATIENT, false, false, randomPatientName, study, equipment);
        Assertions.assertNotNull(subjectREST, "Subject could not be created from local patient.");
        subject.setImagedObjectCategory(null); // to fix server issue with incompatible mapping value
        patient.setSubject(subject);
        return subjectREST;
    }

    private void startImportJobFromShanoirUploader(ImportJobBase importJob, File uploadFolder, String label)
            throws Exception {
        Collection<File> dicomFiles = Util.listFiles(
                uploadFolder,
                (dir, name) -> name.endsWith(DcmRcvManager.DICOM_FILE_SUFFIX),
                true
        );
        Assertions.assertNotNull(dicomFiles);
        Assertions.assertTrue(dicomFiles.size() > 0, "No anonymized DICOM files found in upload folder.");
        String tempDirId = userClient.createTempDir();
        Assertions.assertNotNull(tempDirId);
        logger.info("Upload: tempDirId for import: " + tempDirId);
        int i = 0;
        for (File file : dicomFiles) {
            i++;
            userClient.uploadFile(tempDirId, file);
            logger.debug("Uploaded file {}/{}: {}", i, dicomFiles.size(), file.getName());
        }
        logger.info("Upload: " + dicomFiles.size() + " uploaded files to tempDirId: " + tempDirId);
        importJob.setWorkFolder(tempDirId);
        logger.info("TempDirId: {}", importJob.getWorkFolder());
        String importJobJson = Util.objectWriter.writeValueAsString(importJob);
        dumpImportJobJson(importJobJson, tempDirId, label);
        userClient.startImportJob(tempDirId, importJobJson);
    }

    private void startImportJobFromDicomZip(ImportJobBase importJob, org.shanoir.uploader.model.rest.Subject subjectREST,
            Examination examination, org.shanoir.uploader.model.rest.Study study)
            throws JsonProcessingException, Exception {
        importJob.setStudyId(study.getId());
        importJob.setStudyName(study.getName());
        StudyCard studyCard = study.getStudyCards().get(0);
        importJob.setStudyCardId(studyCard.getId());
        importJob.setStudyCardName(studyCard.getName());
        importJob.setAcquisitionEquipmentId(studyCard.getAcquisitionEquipment().getId());
        importJob.setExaminationId(examination.getId());
        importJob.setStudyInstanceUID(examination.getStudyInstanceUID());
        if (ShUpConfig.isModeSubjectNameManual()) {
            importJob.setAnonymisationProfileToUse("Profile Neurinfo");
        } else {
            importJob.setAnonymisationProfileToUse("Profile OFSEP");
        }
        importJob.setPatient(null);
        importJob.setStudy(null);
        String importJobJson = Util.objectWriter.writeValueAsString(importJob);
        dumpImportJobJson(importJobJson, importJob.getWorkFolder(), "testImportFromDicomZip");
        userClient.startImportJob(importJob.getWorkFolder(), importJobJson);
        waitForServerImportJobStatus(importJob.getWorkFolder(), "testImportFromDicomZip-before-ds");
    }

    /**
     * Same as {@link #startImportJobFromZip} but for a study without a study
     * card: the study-card / acquisition-equipment fields on the import job
     * are left {@code null} instead of being populated from a (non-existent)
     * study card.
     */
    private void startImportJobFromDicomZipNoStudyCard(ImportJobBase importJob,
            org.shanoir.uploader.model.rest.Subject subjectREST,
            Examination examination, org.shanoir.uploader.model.rest.Study study, Long acquisitionEquipmentId)
            throws JsonProcessingException, Exception {
        importJob.setStudyId(study.getId());
        importJob.setStudyName(study.getName());
        importJob.setStudyCardId(null);
        importJob.setStudyCardName(null);
        importJob.setAcquisitionEquipmentId(acquisitionEquipmentId);
        importJob.setExaminationId(examination.getId());
        importJob.setStudyInstanceUID(examination.getStudyInstanceUID());
        if (ShUpConfig.isModeSubjectNameManual()) {
            importJob.setAnonymisationProfileToUse("Profile Neurinfo");
        } else {
            importJob.setAnonymisationProfileToUse("Profile OFSEP");
        }
        importJob.setPatient(null);
        importJob.setStudy(null);
        String importJobJson = Util.objectWriter.writeValueAsString(importJob);
        dumpImportJobJson(importJobJson, importJob.getWorkFolder(), "testImportFromDicomZipNoStudyCard");
        userClient.startImportJob(importJob.getWorkFolder(), importJobJson);
        waitForServerImportJobStatus(importJob.getWorkFolder(), "testImportFromDicomZipNoStudyCard-before-ds");
    }

    private org.shanoir.uploader.model.rest.Subject createSubject(ImportJobBase importJob,
            org.shanoir.uploader.model.rest.Study study)
            throws UnsupportedEncodingException, NoSuchAlgorithmException, PseudonymusException, ParseException {
        Patient patient = importJob.getPatient();
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
        importJob.setSubject(subjectForImportJob);
        importJob.setSubjectName(subjectREST.getName());
        return subjectREST;
    }

    /**
     * Same as {@link #createSubject} but for a study without a study card:
     * {@code null} is passed instead of an
     * {@link org.shanoir.uploader.model.rest.AcquisitionEquipment}, matching
     * the "no study card" import path.
     */
    private org.shanoir.uploader.model.rest.Subject createSubjectNoStudyCard(ImportJobBase importJob,
            org.shanoir.uploader.model.rest.Study study)
            throws UnsupportedEncodingException, NoSuchAlgorithmException, PseudonymusException, ParseException {
        Patient patient = importJob.getPatient();
        final String randomPatientName = "Subject-" + UUID.randomUUID().toString();
        Subject subject = ImportUtils.createSubjectFromPatient(patient, pseudonymizer, identifierCalculator);
        org.shanoir.uploader.model.rest.Subject subjectREST = ImportUtils.manageSubject(
                null, subject, randomPatientName, ImagedObjectCategory.LIVING_HUMAN_BEING,
                HemisphericDominance.Left.toString(), HemisphericDominance.Left.toString(),
                SubjectType.PATIENT, false, false, randomPatientName, study, equipment);
        subject.setImagedObjectCategory(null); // to fix server issue with incompatible mapping value
        org.shanoir.ng.importer.model.Subject subjectForImportJob = new org.shanoir.ng.importer.model.Subject();
        subjectForImportJob.setId(subjectREST.getId());
        subjectForImportJob.setName(subjectREST.getName());
        importJob.setSubject(subjectForImportJob);
        importJob.setSubjectName(subjectREST.getName());
        return subjectREST;
    }

    private void selectAllSeriesForImport(ImportJobBase importJob) {
        List<Serie> series = importJob.getSeries();
        for (Serie serie : series) {
            serie.setSelected(true);
        }
    }

    private ImportJobBase uploadDicomZip(final String fileName) {
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

    private void dumpImportJobJson(String importJobJson, String workFolder, String label) {
        try {
            Files.createDirectories(IMPORT_JOB_DUMP_DIR.toPath());
            String timestamp = LocalDateTime.now().format(IMPORT_JOB_DUMP_TIMESTAMP_FORMATTER);
            String workFolderPart = (workFolder != null && !workFolder.isEmpty()) ? workFolder : "no-workfolder";
            File dumpFile = new File(IMPORT_JOB_DUMP_DIR, timestamp + "_" + workFolderPart + "_" + label + ".json");
            Files.writeString(dumpFile.toPath(), importJobJson, StandardCharsets.UTF_8);
            logger.info("Dumped import-job to: {}", dumpFile.getAbsolutePath());
        } catch (Exception e) {
            // This is a debugging convenience only - never fail the test because of it.
            logger.warn("Could not dump import-job for manual inspection: {}", e.getMessage());
        }
    }

    /**
     * Polls MS Datasets' new import-status endpoint until dataset creation
     * for this examination is FINISHED (or fails/times out). This is
     * necessary because dataset/acquisition creation happens asynchronously
     * over RabbitMQ, and ExaminationApi (another microservice) has no
     * visibility into whether that step has completed.
     */
    private void waitForDatasetImportFinished(Long examinationId, String label) throws Exception {
        long deadline = System.currentTimeMillis() + DATASET_STATUS_TIMEOUT_MILLIS;
        while (System.currentTimeMillis() < deadline) {
            DatasetsImportStatus status = userClient.findImportStatusByExaminationId(examinationId);
            if (status != null) {
                if (status.getState() == DatasetsImportStatus.State.FINISHED) {
                    logger.info("[{}] Dataset creation FINISHED for examination {}", label, examinationId);
                    return;
                } else if (status.getState() == DatasetsImportStatus.State.ERROR) {
                    Assertions.fail("[" + label + "] Dataset creation failed for examination " + examinationId
                            + ": " + status.getMessage());
                }
            }
            Thread.sleep(DATASET_STATUS_POLL_INTERVAL_MILLIS);
        }
        Assertions.fail("[" + label + "] Dataset creation did not reach FINISHED within timeout for examination "
                + examinationId);
    }

    /**
     * Downloads the zip produced by POST /datasets/datasets/massiveDownload
     * for all datasets of an examination, extracts it, and compares the
     * DICOM files it contains (by SOPInstanceUID) against the local DICOM
     * files used as the source of the import.
     *
     * @param examinationId   examination whose datasets should be checked
     * @param localDicomFolder folder containing the original/local DICOM
     *                         files used for this import (searched recursively)
     * @param label           label used for logging / dumped artifact naming
     */
    private void downloadAndCompareDatasetsZip(Long examinationId, File localDicomFolder, String label) throws Exception {
        waitForDatasetImportFinished(examinationId, label);

        List<DatasetLight> datasets = userClient.findDatasetsByExaminationId(examinationId);
        Assertions.assertNotNull(datasets, "[" + label + "] findDatasetsByExaminationId returned null.");
        Assertions.assertFalse(datasets.isEmpty(),
                "[" + label + "] No datasets found for examination " + examinationId + ".");
        List<Long> datasetIds = datasets.stream().map(DatasetLight::getId).toList();

        File downloadedZip = File.createTempFile("shanoir-massive-download-" + examinationId + "-", ".zip");
        try (CloseableHttpResponse response = userClient.downloadDatasetsByIds(datasetIds, "dcm")) {
            Assertions.assertNotNull(response, "[" + label + "] massiveDownload returned no response.");
            HttpEntity entity = response.getEntity();
            Assertions.assertNotNull(entity, "[" + label + "] massiveDownload returned no entity.");
            try (var out = new java.io.FileOutputStream(downloadedZip)) {
                entity.writeTo(out);
            }
        }
        Assertions.assertTrue(downloadedZip.length() > 0,
                "[" + label + "] Downloaded massiveDownload zip is empty.");

        File extractDir = Files.createTempDirectory("shanoir-massive-download-extract-").toFile();
        unzip(downloadedZip, extractDir);

        Set<String> remoteSopInstanceUIDs = collectSopInstanceUIDs(extractDir);
        Set<String> localSopInstanceUIDs = collectSopInstanceUIDs(localDicomFolder);

        Assertions.assertFalse(remoteSopInstanceUIDs.isEmpty(),
                "[" + label + "] No DICOM instances found in downloaded zip for examination " + examinationId + ".");
        Assertions.assertFalse(localSopInstanceUIDs.isEmpty(),
                "[" + label + "] No local DICOM instances found to compare against for examination " + examinationId + ".");

        Assertions.assertEquals(localSopInstanceUIDs, remoteSopInstanceUIDs,
                "[" + label + "] SOPInstanceUIDs of downloaded datasets (" + remoteSopInstanceUIDs.size()
                        + ") do not match local DICOM files (" + localSopInstanceUIDs.size()
                        + ") for examination " + examinationId + ".");

        logger.info("[{}] massiveDownload check OK: {} DICOM instance(s) matched for examination {}.",
                label, remoteSopInstanceUIDs.size(), examinationId);
    }

    private void unzip(File zipFile, File targetDir) throws IOException {
        try (ZipInputStream zis = new ZipInputStream(new java.io.FileInputStream(zipFile))) {
            ZipEntry entry;
            while ((entry = zis.getNextEntry()) != null) {
                File outFile = new File(targetDir, entry.getName());
                if (entry.isDirectory()) {
                    outFile.mkdirs();
                    continue;
                }
                outFile.getParentFile().mkdirs();
                try (var out = new java.io.FileOutputStream(outFile)) {
                    zis.transferTo(out);
                }
            }
        }
    }

    /**
     * Recursively walks a folder, tries to read every regular file as DICOM,
     * and collects the SOPInstanceUID of each one that parses successfully.
     * Non-DICOM files (manifests, json reports, etc.) are silently skipped.
     */
    private Set<String> collectSopInstanceUIDs(File folder) throws IOException {
        Set<String> uids = new HashSet<>();
        try (var stream = Files.walk(folder.toPath())) {
            for (java.nio.file.Path path : (Iterable<java.nio.file.Path>) stream.filter(Files::isRegularFile)::iterator) {
                try (DicomInputStream dis = new DicomInputStream(path.toFile())) {
                    Attributes attrs = dis.readDataset();
                    String sopInstanceUID = attrs.getString(Tag.SOPInstanceUID);
                    if (sopInstanceUID != null) {
                        uids.add(sopInstanceUID);
                    }
                } catch (Exception notDicomOrUnreadable) {
                    // expected for non-DICOM files in the zip/folder (manifest, json, etc.)
                }
            }
        }
        return uids;
    }

}
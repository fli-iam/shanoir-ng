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
import java.net.URL;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.shanoir.anonymization.anonymization.AnonymizationResult;
import org.shanoir.ng.importer.model.EegImportJob;
import org.shanoir.ng.importer.model.ImportJobBase;
import org.shanoir.ng.importer.model.Patient;
import org.shanoir.ng.importer.model.Serie;
import org.shanoir.ng.shared.quality.QualityTag;
import org.shanoir.ng.studycard.dto.QualityCardResult;
import org.shanoir.ng.studycard.model.QualityCard;
import org.shanoir.uploader.ShUpConfig;
import org.shanoir.uploader.dicom.DicomServerClient;
import org.shanoir.uploader.dicom.anonymize.Anonymizer;
import org.shanoir.uploader.model.rest.Examination;
import org.shanoir.uploader.model.rest.Study;
import org.shanoir.uploader.model.rest.StudyCard;
import org.shanoir.uploader.utils.ImportUtils;
import org.shanoir.uploader.utils.Util;

/**
 * Import tests for studies whose {@code StudyCardPolicy} is
 * {@code MANDATORY}: every import must resolve subject/examination/dataset
 * creation through a {@link StudyCard}.
 *
 * Counterpart to {@link ImportWithoutStudyCardTests}. Covers DICOM zip,
 * multi-exam zip, EEG, BIDS and PACS import entry points for
 * {@code SC_MANDATORY} studies; the local "ShUp upload" pipeline is only
 * covered indirectly here via {@link #testImportShUpFromPACS()} (see the note
 * above {@link #testImportMultipleDicomZip()}), since
 * {@link ImportWithoutStudyCardTests} already exercises it directly for
 * {@code SC_DISABLED} studies.
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class ImportWithStudyCardTests extends AbstractImportTest {

    private static Study study;

    /**
     * Examination created by {@link #testImportFromDicomZip()}, kept around so
     * {@link #testQualityCard()} can apply/test the quality card against a
     * concrete, already-imported examination rather than an arbitrary one.
     */
    private static Long referenceExaminationId;

    /**
     * Name of the subject created by {@link #testImportFromDicomZip()} -
     * randomly generated (UUID-based) per test run, so matching on it in a
     * {@link org.shanoir.ng.studycard.dto.QualityCardResult} reliably proves
     * the quality card was evaluated against this specific import.
     */
    private static String referenceSubjectName;

    @Test
    @Order(1)
    public void testImportFromDicomZip() {
        logger.info("......................................................");
        logger.info("START testImportFromDicomZip (with study card)........");
        logger.info("......................................................");
        try {
            ImportJobBase importJob = uploadDicomZip(ACR_PHANTOM_T1_ZIP);
            logger.info("ID: {}", importJob.getId());
            study = createStudyAndCenterAndStudyCardAndAddMembers();
            if (!importJob.getSeries().isEmpty()) {
                selectAllSeriesForImport(importJob);
                org.shanoir.uploader.model.rest.Subject subject =
                        createSubject(importJob, study, study.getStudyCards().get(0).getAcquisitionEquipment());
                referenceSubjectName = subject.getName();
                org.shanoir.ng.importer.model.Study dicomStudy = importJob.getStudy();
                Examination examination = createExaminationForStudyCard(dicomStudy, subject);
                referenceExaminationId = examination.getId();
                startImportJobFromDicomZip(importJob, examination, study, study.getStudyCards().get(0), null,
                        "testImportFromDicomZip");
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            Assertions.fail(e.getMessage());
        }
    }

    @Test
    @Order(2)
    public void testImportMultipleDicomZip() throws Exception {
        logger.info("......................................................");
        logger.info("START testImportMultipleDicomZip (with study card).....");
        logger.info("......................................................");
        URL resource = getClass().getClassLoader().getResource(TEST_MULTIPLE_EXAM_ZIP);
        Assertions.assertNotNull(resource, "Test resource " + TEST_MULTIPLE_EXAM_ZIP + " not found.");
        File file = new File(resource.toURI());

        StudyCard studyCard = study.getStudyCards().get(0);
        
        ImportJobBase importJob = userClient.uploadMultipleDicom(file,
                study.getId(), study.getName(), studyCard.getId(),
                studyCard.getCenterId(), studyCard.getAcquisitionEquipment().getId());

        Assertions.assertNotNull(importJob, "Multiple-examination import returned no import job.");
    }

    @Test
    @Order(3)
    public void testImportEEG() throws Exception {
        logger.info("......................................................");
        logger.info("START testImportEEG (with study card)..................");
        logger.info("......................................................");
        URL resource = getClass().getClassLoader().getResource(TEST_EEG_ZIP);
        Assertions.assertNotNull(resource, "Test resource " + TEST_EEG_ZIP + " not found.");
        File file = new File(resource.toURI());

        EegImportJob uploadedJob = userClient.uploadEEGZip(file);
        Assertions.assertNotNull(uploadedJob, "EEG upload did not return an import job.");
        Assertions.assertNotNull(uploadedJob.getWorkFolder(), "EEG upload did not return a work folder.");
        logger.info("EEG file uploaded, workFolder: {}", uploadedJob.getWorkFolder());

        EegImportJob analyzedJob = userClient.analyzeEegZipFile(uploadedJob);
        Assertions.assertNotNull(analyzedJob, "EEG analysis did not return an import job.");
        Assertions.assertNotNull(analyzedJob.getDatasets(), "EEG analysis did not return any datasets.");
        Assertions.assertFalse(analyzedJob.getDatasets().isEmpty(), "EEG analysis returned an empty dataset list.");
        logger.info("EEG file analyzed, {} dataset(s) found.", analyzedJob.getDatasets().size());

        StudyCard studyCard = study.getStudyCards().get(0);
        org.shanoir.uploader.model.rest.Subject subject = createSubject(study);
        Assertions.assertNotNull(subject, "Subject could not be created for EEG import.");
        Examination examination = createExamination(study.getId(), subject.getId(), studyCard.getCenterId());
        Assertions.assertNotNull(examination, "Examination could not be created for EEG import.");

        analyzedJob.setStudyId(study.getId());
        analyzedJob.setStudyName(study.getName());
        analyzedJob.setSubjectName(subject.getName());
        analyzedJob.setExaminationId(examination.getId());
        analyzedJob.setAcquisitionEquipmentId(studyCard.getAcquisitionEquipment().getId());

        // MS Import is only relaying the job to MS Datasets
        userClient.startImportEEGJob(analyzedJob);
        if (analyzedJob.getWorkFolder() != null && !analyzedJob.getWorkFolder().isEmpty()) {
            waitForServerImportJobStatus(analyzedJob.getId(), "testImportEEG-before-ds");
        }
    }

    @Test
    @Order(4)
    public void testImportBIDS() throws Exception {
        logger.info("......................................................");
        logger.info("START testImportBIDS (with study card).................");
        logger.info("......................................................");
        URL resource = getClass().getClassLoader().getResource(TEST_BIDS_ZIP);
        Assertions.assertNotNull(resource, "Test resource " + TEST_BIDS_ZIP + " not found.");
        File file = new File(resource.toURI());

        Long centerId = study.getStudyCards().get(0).getCenterId();

        ImportJobBase importJob = userClient.importBIDSDataset(file, study.getId(), study.getName(), centerId);
        Assertions.assertNotNull(importJob, "testImportBIDS (with study card) returned no import job.");
        Assertions.assertNotNull(importJob.getExaminationId(),
                "testImportBIDS (with study card) did not create/return an examination id.");
        logger.info("testImportBIDS (with study card) created examination: {}",
                importJob.getExaminationId());
    }

    @Test
    @Order(5)
    public void testImportShUpFromPACS() throws Exception {
        logger.info("......................................................");
        logger.info("START testImportShUpFromPACS (with study card).............");
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

            File importJobFolder = ImportUtils.createUploadFolder(workFolder, "pacs-test-subject");
            List<String> retrievedFiles = dicomServerClient.retrieveDicomFiles(
                    new javax.swing.JProgressBar(), new StringBuilder(), dicomStudy.getStudyInstanceUID(),
                    selectedSeries, importJobFolder);
            Assertions.assertNotNull(retrievedFiles);
            Assertions.assertFalse(retrievedFiles.isEmpty(), "No DICOM files retrieved via C-MOVE.");
            
            dicomServerClient.stopSCPServer();

            ImportJobBase importJob = ImportUtils.createNewImportJob(patient, dicomStudy);
            importJob.setSeries(selectedSeries);
            org.shanoir.ng.importer.dicom.ImagesCreatorAndDicomFileAnalyzerService dicomFileAnalyzer =
                    new org.shanoir.ng.importer.dicom.ImagesCreatorAndDicomFileAnalyzerService();
            for (Serie serie : selectedSeries) {
                dicomFileAnalyzer.getAdditionalMetaDataFromFirstInstanceOfSerie(importJobFolder.getAbsolutePath(), importJob.getPatient(),
                        importJob.getStudy(), serie, true);
            }

            StudyCard studyCard = study.getStudyCards().get(0);
            org.shanoir.uploader.model.rest.Subject subject =
                    createSubjectFromLocalPatient(patient, study, studyCard.getAcquisitionEquipment());
            importJob.setSubject(patient.getSubject());
            Examination examination = createExaminationForStudyCard(dicomStudy, subject);

            importJob = ImportUtils.prepareImportJob(importJob, subject,
                    examination.getId(), examination.getStudyInstanceUID(), study, studyCard,
                    studyCard.getAcquisitionEquipment());
            importJob.setFromShanoirUploader(true);
            importJob.setFromDicomZip(false);
            importJob.setFromPacs(false);

            Anonymizer anonymizer = new Anonymizer();
            String anonymizationProfile = ShUpConfig.profileProperties.getProperty(ShUpConfig.ANONYMIZATION_PROFILE);
            AnonymizationResult anonymizationResult = anonymizer.pseudonymize(
                    importJobFolder, anonymizationProfile, subject.getName(), examination.getStudyInstanceUID());
            Assertions.assertNotNull(anonymizationResult, "Local anonymization of PACS-retrieved DICOM files failed.");
            org.shanoir.ng.utils.ImportUtils.updateImportJobWithPseudonymizedUIDs(importJob, importJobFolder, anonymizationResult, false);
            
            File importJobJsonFile = new File(importJobFolder, ShUpConfig.IMPORT_JOB_JSON);
            importJobJsonFile.createNewFile();
            Util.mapper.writeValue(importJobJsonFile, importJob);

            startImportJobFromShanoirUploader(importJob, importJobFolder, "testImportShUpFromPACS");
            waitForServerImportJobStatus(importJob.getId(), "testImportShUpFromPACS-before-ds");
            waitAndCheckServerConsistency(importJobFolder, examination.getId(), true);
            downloadAndCompareDatasetsZip(examination.getId(), importJobFolder, "testImportShUpFromPACS");
        } finally {
            pacs.stop();
        }
    }

    /**
     * Exercises both quality-card trigger modes against the examination
     * imported by {@link #testImportFromDicomZip()}, proving in each case
     * that the result actually reflects that specific import (by subject
     * name) rather than just checking a non-null response.
     */
    @Test
    @Order(6)
    public void testQualityCard() throws Exception {
        logger.info("......................................................");
        logger.info("START testQualityCard (with study card)................");
        logger.info("......................................................");
        Assertions.assertNotNull(referenceExaminationId,
                "testImportFromDicomZip must run first and populate referenceExaminationId.");
        Assertions.assertNotNull(referenceSubjectName,
                "testImportFromDicomZip must run first and populate referenceSubjectName.");

        // "Run in ShUp on the client side": opt-in only, never applied
        // automatically during import (toCheckAtImport = false), triggered
        // here as a non-mutating dry-run preview.
        QualityCard clientSideCard = createQualityCardForStudy(study, "Quality-Card-SC-ShUp-",
                QualityTag.WARNING, false);
        QualityCardResult dryRunResult = testQualityCardOnStudyDryRun(clientSideCard.getId());
        assertQualityCardWasAppliedToSubject(dryRunResult, referenceSubjectName, QualityTag.WARNING,
                "client-side dry-run");

        // "Run at the end of the import in ms-datasets": toCheckAtImport =
        // true is the flag that makes ms-datasets apply this card
        // automatically on subsequent imports into the study. We can't
        // observe that automatic run happening on a *live* import here
        // (the card is necessarily created after our imports already ran),
        // so we call applyQualityCardOnStudy explicitly right away, to prove
        // the same rule evaluation ms-datasets would perform automatically
        // gives the expected, deterministic result for our reference import.
        QualityCard autoCheckCard = createQualityCardForStudy(study, "Quality-Card-SC-Auto-",
                QualityTag.WARNING, true);
        QualityCardResult applyResult = applyQualityCardOnStudyForReal(autoCheckCard.getId());
        assertQualityCardWasAppliedToSubject(applyResult, referenceSubjectName, QualityTag.WARNING,
                "end-of-import (ms-datasets) apply");
    }

    private Examination createExaminationForStudyCard(org.shanoir.ng.importer.model.Study dicomStudy,
            org.shanoir.uploader.model.rest.Subject subject) {
        return createExamination(study, dicomStudy, subject, study.getStudyCards().get(0).getCenterId());
    }

}
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
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.core5.http.HttpEntity;
import org.dcm4che3.data.Attributes;
import org.dcm4che3.data.Tag;
import org.dcm4che3.io.DicomInputStream;
import org.junit.jupiter.api.Assertions;
import org.shanoir.ng.dicom.web.StudyInstanceUIDAndSubjectNameHandler;
import org.shanoir.ng.importer.model.ImportJobBase;
import org.shanoir.ng.importer.model.Patient;
import org.shanoir.ng.importer.model.Serie;
import org.shanoir.ng.importer.model.Subject;
import org.shanoir.ng.studycard.dto.QualityCardResult;
import org.shanoir.ng.studycard.model.QualityCard;
import org.shanoir.uploader.ShUpConfig;
import org.shanoir.uploader.check.DicomInstanceConsistencyChecker;
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

/**
 * Abstract base class for all DICOM/PACS/EEG/BIDS import tests.
 *
 * Holds everything that is genuinely identical regardless of whether the
 * target study uses a {@link StudyCard} or not (constants, polling/consistency
 * helpers, zip handling, quality-card helpers).
 *
 * {@link ImportWithStudyCardTests} and {@link ImportWithoutStudyCardTests}
 * extend this class and only contain what actually differs between the two
 * import policies (which study/equipment they use, and how they populate the
 * study-card-related fields of the import job).
 */
public abstract class AbstractImportTest extends AbstractTest {

    protected static final Logger logger = LoggerFactory.getLogger(AbstractImportTest.class);

    protected static final String ACR_PHANTOM_T1_ZIP = "acr_phantom_t1.zip";

    protected static final String ACR_PHANTOM_T1_DIR = "acr_phantom_t1/";

    protected static final String TEST_MULTIPLE_EXAM_ZIP = "TEST_MET_0001.zip";

    protected static final String TEST_EEG_ZIP = "testEDF.zip";

    protected static final String TEST_BIDS_ZIP = "testBIDS.zip";

    protected static final String TEST_PACS_AET = "TEST_PACS";

    protected static final int TEST_PACS_PORT = 11121;

    // The server-side import is asynchronous: give it time to appear before
    // declaring the consistency check failed.
    protected static final long CONSISTENCY_CHECK_TIMEOUT_MILLIS = 2 * 60 * 1000; // 2 min

    protected static final long CONSISTENCY_CHECK_POLL_INTERVAL_MILLIS = 5 * 1000; // 5 sec

    protected static final long DATASET_STATUS_TIMEOUT_MILLIS = 2 * 60 * 1000;

    protected static final long DATASET_STATUS_POLL_INTERVAL_MILLIS = 5 * 1000;

    // Local folder where a copy of every import-job JSON sent is dumped
    protected static final File IMPORT_JOB_DUMP_DIR = new File("target/import-job-dumps");

    protected static final DateTimeFormatter IMPORT_JOB_DUMP_TIMESTAMP_FORMATTER =
            DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss-SSS");

    protected void selectAllSeriesForImport(ImportJobBase importJob) {
        List<Serie> series = importJob.getSeries();
        for (Serie serie : series) {
            serie.setSelected(true);
        }
    }

    protected ImportJobBase uploadDicomZip(final String fileName) {
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

    /**
     * Builds and persists an {@link Examination} for the given DICOM study.
     * The center to attach the examination to is passed in explicitly by the
     * caller, since it is resolved differently depending on whether the
     * target study has a study card (center comes from the study card) or
     * not (center comes from the study's center list).
     */
    protected Examination createExamination(Study study,
            org.shanoir.ng.importer.model.Study dicomStudy,
            org.shanoir.uploader.model.rest.Subject subject,
            Long centerId) {
        LocalDate studyDate = dicomStudy.getStudyDate();
        Instant studyDateInstant = studyDate.atStartOfDay(ZoneId.systemDefault()).toInstant();
        Date studyDateDate = Date.from(studyDateInstant);
        String examinationComment = dicomStudy.getStudyDescription();
        Examination examination = ImportUtils.createExamination(study, subject, studyDateDate,
                examinationComment, centerId, false);
        Assertions.assertNotNull(examination, "Examination could not be created.");
        return examination;
    }

    /**
     * Creates a REST subject from a locally-parsed {@link Patient}. The
     * acquisition equipment to associate the subject with is passed in
     * explicitly: it is either the study card's equipment (mandatory-study-card
     * studies) or the study's stand-alone equipment ({@code SC_DISABLED}
     * studies). Passing {@code null} is valid and mirrors what the
     * no-study-card import path does server-side.
     */
    protected org.shanoir.uploader.model.rest.Subject createSubjectFromLocalPatient(Patient patient,
            org.shanoir.uploader.model.rest.Study study, AcquisitionEquipment equipment)
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

    /**
     * Same idea as {@link #createSubjectFromLocalPatient}, but building the
     * subject from an already-uploaded {@link ImportJobBase} (DICOM-zip import
     * path) rather than from a locally-parsed {@link Patient}.
     */
    protected org.shanoir.uploader.model.rest.Subject createSubject(ImportJobBase importJob,
            org.shanoir.uploader.model.rest.Study study, AcquisitionEquipment equipment)
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

    /**
     * Finalizes and starts an import job coming from the "DICOM zip" upload
     * path. {@code studyCard} may be {@code null} (SC_DISABLED studies): in
     * that case {@code acquisitionEquipmentId} is used directly instead of
     * being derived from the study card.
     */
    protected void startImportJobFromDicomZip(ImportJobBase importJob,
            Examination examination, org.shanoir.uploader.model.rest.Study study,
            StudyCard studyCard, Long acquisitionEquipmentId, String label) throws Exception {
        importJob.setStudyId(study.getId());
        importJob.setStudyName(study.getName());
        if (studyCard != null) {
            importJob.setStudyCardId(studyCard.getId());
            importJob.setStudyCardName(studyCard.getName());
            importJob.setAcquisitionEquipmentId(studyCard.getAcquisitionEquipment().getId());
        } else {
            importJob.setStudyCardId(null);
            importJob.setStudyCardName(null);
            importJob.setAcquisitionEquipmentId(acquisitionEquipmentId);
        }
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
        dumpImportJobJson(importJobJson, importJob.getWorkFolder(), label);
        userClient.startImportJob(importJob.getWorkFolder(), importJobJson);
        waitForServerImportJobStatus(importJob.getWorkFolder(), label + "-before-ds");
    }

    /**
     * Finalizes and starts an import job coming from the local
     * ShanoirUploader upload path (files copied/anonymized locally first).
     * Identical regardless of study-card policy: the import job passed in has
     * already been fully prepared (via {@link ImportUtils#prepareImportJob})
     * by the caller.
     */
    protected void startImportJobFromShanoirUploader(ImportJobBase importJob, File uploadFolder, String label)
            throws Exception {
        java.util.Collection<File> dicomFiles = Util.listFiles(
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

    protected void dumpImportJobJson(String importJobJson, String workFolder, String label) {
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
     * Polls the server until the local DICOM files match their remote,
     * persisted counterparts for the given examination, or a timeout is
     * reached. Wraps {@link DicomInstanceConsistencyChecker}, since the import
     * triggered by {@code startImportJob} is processed asynchronously on
     * the server and may not be immediately queryable.
     */
    protected void waitAndCheckServerConsistency(File localDicomFolder, Long examinationId, boolean compareTags)
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

    protected org.shanoir.ng.importer.model.ImportJobStatus waitForServerImportJobStatus(String tempDirId, String label)
            throws Exception {
        long deadline = System.currentTimeMillis() + CONSISTENCY_CHECK_TIMEOUT_MILLIS;
        while (System.currentTimeMillis() < deadline) {
            org.shanoir.ng.importer.model.ImportJobStatus status = userClient.getImportJobStatus(tempDirId);
            if (status != null) {
                if (status.getState() == org.shanoir.ng.importer.model.ImportJobStatus.State.FINISHED) {
                    logger.info("Server reported FINISHED for tempDirId {}.", tempDirId);
                    ImportJobBase importJob = status.getImportJob();
                    String importJobJson = Util.objectWriter.writeValueAsString(importJob);
                    dumpImportJobJson(importJobJson, tempDirId, label);
                    return status;
                } else if (status.getState() == org.shanoir.ng.importer.model.ImportJobStatus.State.ERROR) {
                    Assertions.fail("Import failed on server for tempDirId " + tempDirId + ": " + status.getMessage());
                }
            }
            Thread.sleep(CONSISTENCY_CHECK_POLL_INTERVAL_MILLIS);
        }
        Assertions.fail("Import did not reach FINISHED on server within timeout for tempDirId " + tempDirId);
        return null;
    }

    /**
     * Polls MS Datasets' import-status endpoint until dataset creation for
     * this examination is FINISHED (or fails/times out). Necessary because
     * dataset/acquisition creation happens asynchronously over RabbitMQ.
     */
    protected void waitForDatasetImportFinished(Long examinationId, String label) throws Exception {
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
     */
    protected void downloadAndCompareDatasetsZip(Long examinationId, File localDicomFolder, String label) throws Exception {
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

    protected void unzip(File zipFile, File targetDir) throws IOException {
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
    protected Set<String> collectSopInstanceUIDs(File folder) throws IOException {
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

    // -------------------------------------------------------------------
    // Quality-card helpers
    //
    // Shared by both ImportWithStudyCardTests#testQualityCard and
    // ImportWithoutStudyCardTests#testQualityCard: a quality card is a
    // per-study construct independent of the study-card policy, so the
    // exact same creation/apply/dry-run-test helpers work for a study with
    // or without study cards - only the target Study instance differs.
    //
    // NOTE: this builds a minimal QualityCard (name + studyId only). The
    // QualityCard model itself (rules/conditions it evaluates) was not part
    // of the provided sources, so if QualityCard requires further mandatory
    // fields to be persisted/evaluated meaningfully, extend
    // buildMinimalQualityCard() accordingly.
    // -------------------------------------------------------------------

    protected QualityCard buildMinimalQualityCard(Study study, String namePrefix) {
        QualityCard qualityCard = new QualityCard();
        qualityCard.setName(namePrefix + UUID.randomUUID());
        qualityCard.setStudyId(study.getId());
        return qualityCard;
    }

    /**
     * Creates the quality card on the server and asserts it was persisted.
     */
    protected QualityCard createQualityCardForStudy(Study study, String namePrefix) {
        QualityCard qualityCard = buildMinimalQualityCard(study, namePrefix);
        QualityCard created = expertClient.createQualityCard(qualityCard);
        Assertions.assertNotNull(created, "Quality card could not be created.");
        Assertions.assertNotNull(created.getId(), "Created quality card has no id.");
        logger.info("Quality card {} ({}) created for study {}.", created.getName(), created.getId(),
                study.getId());
        return created;
    }

    /**
     * "Run in ShUp on the client side": a dry-run evaluation of the quality
     * card, as a user would trigger manually from the ShUp GUI to preview the
     * outcome before actually applying it. Does not mutate anything
     * server-side (mirrors {@code QualityCardApiController#testQualityCardOnStudy}).
     */
    protected QualityCardResult testQualityCardOnStudyDryRun(Long qualityCardId) throws Exception {
        QualityCardResult result = expertClient.testQualityCardOnStudy(qualityCardId);
        Assertions.assertNotNull(result, "Client-side (dry-run) quality card test returned no result.");
        logger.info("Client-side (dry-run) quality card test completed for quality card {}.", qualityCardId);
        return result;
    }

    /**
     * "Run at the end of the import in ms-datasets": the real application of
     * the quality card against the study's (already imported) datasets
     * (mirrors {@code QualityCardApiController#applyQualityCardOnStudy}).
     */
    protected QualityCardResult applyQualityCardOnStudyForReal(Long qualityCardId) throws Exception {
        QualityCardResult result = expertClient.applyQualityCardOnStudy(qualityCardId);
        Assertions.assertNotNull(result, "Server-side quality card application returned no result.");
        logger.info("Server-side quality card application completed for quality card {}.", qualityCardId);
        return result;
    }

}

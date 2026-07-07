package org.shanoir.uploader.test.importer;

import java.io.File;
import java.io.FileOutputStream;
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
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.shanoir.ng.importer.model.ImportJob;
import org.shanoir.ng.importer.model.Patient;
import org.shanoir.ng.importer.model.Serie;
import org.shanoir.ng.importer.model.Study;
import org.shanoir.ng.importer.model.Subject;
import org.shanoir.uploader.ShUpConfig;
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

    private static org.shanoir.uploader.model.rest.Study study;

    @Test
    @Order(1)
    public void testImportWithDicomZipUpload() {
        try {
            study = createStudyAndCenterAndStudyCardAndAddMembers();
            ImportJob importJob = uploadDicomZip(ACR_PHANTOM_T1_ZIP);
            if (!importJob.getPatients().isEmpty()) {
                selectAllSeriesForImport(importJob);
                org.shanoir.uploader.model.rest.Subject subject = createSubject(importJob, study);
                Long examinationId = createExamination(study, importJob, subject);
                startImportJobFromZip(importJob, subject, examinationId, study);
            }
        } catch(Exception e) {
            logger.error(e.getMessage(), e);
        }
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

    @Test
    @Order(2)
    public void testImportFromShanoirUploader() throws Exception {
        try {
            ImportJob importJob = uploadDicomZip(ACR_PHANTOM_T1_ZIP);
            Assertions.assertNotNull(importJob, "ImportJob could not be parsed from test ZIP.");
            if (!importJob.getPatients().isEmpty()) {
                selectAllSeriesForImport(importJob);
                org.shanoir.uploader.model.rest.Subject subject = createSubject(importJob, study);
                Long examinationId = createExamination(study, importJob, subject);
                startImportJobFromShanoirUploader(importJob, subject, examinationId, study);
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
    }

    private void startImportJobFromShanoirUploader(ImportJob importJob,
            org.shanoir.uploader.model.rest.Subject subjectREST, Long examinationId,
            org.shanoir.uploader.model.rest.Study study) throws Exception {
        importJob.setFromDicomZip(false);
        importJob.setFromPacs(false);
        importJob.setFromShanoirUploader(true);
        importJob.setStudyId(study.getId());
        importJob.setStudyName(study.getName());
        StudyCard studyCard = study.getStudyCards().get(0);
        importJob.setStudyCardId(studyCard.getId());
        importJob.setStudyCardName(studyCard.getName());
        importJob.setAcquisitionEquipmentId(studyCard.getAcquisitionEquipment().getId());
        importJob.setExaminationId(examinationId);

        List<File> dicomFiles = extractZipToTempFolder(ACR_PHANTOM_T1_ZIP);
        Assertions.assertFalse(dicomFiles.isEmpty(), "No DICOM files extracted from test ZIP.");

        String tempDirId = userClient.createTempDir();
        Assertions.assertNotNull(tempDirId);
        logger.info("Upload: tempDirId for import: " + tempDirId);

        int i = 0;
        for (File file : dicomFiles) {
            i++;
            logger.debug("UploadServiceJob-style upload started for file: " + file.getName());
            userClient.uploadFile(tempDirId, file);
            logger.debug("Uploaded file {}/{}: {}", i, dicomFiles.size(), file.getName());
        }
        logger.info("Upload: " + dicomFiles.size() + " uploaded files to tempDirId: " + tempDirId);

        // mirrors UploadServiceJob#setTempDirIdAndStartImport
        importJob.setWorkFolder(tempDirId);
        String importJobJson = Util.objectWriter.writeValueAsString(importJob);
        userClient.startImportJob(importJobJson);
    }

    /**
     * Extracts the given classpath ZIP resource to a fresh temp folder and
     * returns the extracted files, simulating DICOM files that would already
     * be sitting on local disk (e.g. retrieved via DICOM Q/R) before a desktop
     * ShanoirUploader import.
     */
    private List<File> extractZipToTempFolder(final String zipResourceName) throws Exception {
        List<File> extractedFiles = new ArrayList<>();
        URL resource = getClass().getClassLoader().getResource(zipResourceName);
        if (resource == null) {
            return extractedFiles;
        }
        File tempFolder = Files.createTempDirectory("shanoir-uploader-test-").toFile();
        try (ZipInputStream zis = new ZipInputStream(resource.openStream())) {
            ZipEntry entry;
            while ((entry = zis.getNextEntry()) != null) {
                if (entry.isDirectory()) {
                    continue;
                }
                File outFile = new File(tempFolder, new File(entry.getName()).getName());
                try (FileOutputStream fos = new FileOutputStream(outFile)) {
                    byte[] buffer = new byte[8192];
                    int len;
                    while ((len = zis.read(buffer)) != -1) {
                        fos.write(buffer, 0, len);
                    }
                }
                extractedFiles.add(outFile);
                zis.closeEntry();
            }
        }
        return extractedFiles;
    }

    /**
     * Attention: as we simulate for testing reason, the ZIP upload import
     * via Web GUI, we add a pseudonymization profile, as the GUI does it.
     *
     * @param importJob
     * @param subjectREST
     * @param examination
     * @param study
     * @throws JsonProcessingException
     * @throws Exception
     */
    private void startImportJobFromZip(ImportJob importJob, org.shanoir.uploader.model.rest.Subject subjectREST, Long examinationId, org.shanoir.uploader.model.rest.Study study)
            throws JsonProcessingException, Exception {
        importJob.setStudyId(study.getId());
        importJob.setStudyName(study.getName());
        StudyCard studyCard = study.getStudyCards().get(0);
        importJob.setStudyCardId(studyCard.getId());
        importJob.setStudyCardName(studyCard.getName());
        importJob.setAcquisitionEquipmentId(studyCard.getAcquisitionEquipment().getId());
        importJob.setExaminationId(examinationId);
        // Profile Neurinfo
        if (ShUpConfig.isModeSubjectNameManual()) {
            importJob.setAnonymisationProfileToUse("Profile Neurinfo");
        // Profile OFSEP
        } else {
            importJob.setAnonymisationProfileToUse("Profile OFSEP");
        }
        String importJobJson = Util.objectWriter.writeValueAsString(importJob);
        userClient.startImportJob(importJobJson);
    }

    private org.shanoir.uploader.model.rest.Subject createSubject(ImportJob importJob, org.shanoir.uploader.model.rest.Study study) throws UnsupportedEncodingException, NoSuchAlgorithmException, PseudonymusException, ParseException {
        Patient patient = importJob.getPatients().get(0);
        final String randomPatientName = "Subject-" + UUID.randomUUID().toString();
        Subject subject = ImportUtils.createSubjectFromPatient(patient, pseudonymizer, identifierCalculator);
        org.shanoir.uploader.model.rest.Subject subjectREST = ImportUtils.manageSubject(
            null, subject, randomPatientName, ImagedObjectCategory.LIVING_HUMAN_BEING,
            HemisphericDominance.Left.toString(), HemisphericDominance.Left.toString(),
            SubjectType.PATIENT, false, false, randomPatientName, study, study.getStudyCards().get(0).getAcquisitionEquipment());
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

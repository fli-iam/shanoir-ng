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
import org.shanoir.ng.importer.dicom.ImagesCreatorAndDicomFileAnalyzerService;
import org.shanoir.ng.importer.model.ImportJob;
import org.shanoir.ng.importer.model.Patient;
import org.shanoir.ng.importer.model.Serie;
import org.shanoir.ng.importer.model.Study;
import org.shanoir.ng.importer.model.Subject;
import org.shanoir.uploader.ShUpConfig;
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

    private static org.shanoir.uploader.model.rest.Study study;

    @Test
    @Order(1)
    public void testImportWithDicomZipUpload() {
        logger.info("START testImportWithDicomZipUpload...................");
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

    /**
     * Uploads the anonymized DICOM files found in {@code uploadFolder} one by
     * one to a freshly created server temp dir, then starts the import job
     * with that temp dir as workFolder — same sequence as
     * {@code UploadServiceJob#processStartForServer()} /
     * {@code #setTempDirIdAndStartImport()}.
     */
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
package org.shanoir.uploader.test.datasets.dicom.web;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.UUID;

import org.dcm4che3.data.Attributes;
import org.dcm4che3.data.Tag;
import org.dcm4che3.data.VR;
import org.dcm4che3.io.DicomInputStream;
import org.dcm4che3.io.DicomOutputStream;
import org.junit.jupiter.api.Test;
import org.shanoir.ng.anonymization.uid.generation.UIDGeneration;
import org.shanoir.uploader.model.rest.Examination;
import org.shanoir.uploader.model.rest.Study;
import org.shanoir.uploader.model.rest.Subject;
import org.shanoir.uploader.test.AbstractTest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class StowRSDicomTest extends AbstractTest {

    private static final Logger logger = LoggerFactory.getLogger(StowRSDicomTest.class);

    @Test
    public void postDicomSRToDicomWeb() throws Exception {
        Study study = createStudyAndCenterAndStudyCard();
        Subject subject = createSubject(study);
        Examination examination = createExamination(study.getId(), subject.getId(), study.getStudyCenterList().get(0).getCenter().getId());
        try {
            URL resource = getClass().getClassLoader().getResource("DICOMSR.dcm");
            if (resource != null) {
                File file = new File(resource.toURI());
                File newFile = new File(file.getParent(), "DICOMSR-modified.dcm");
                newFile.createNewFile();
                modifyAndCopyDicomFile(file, newFile, study.getId().toString(), subject.getName(), UIDGeneration.ROOT + "." + examination.getId());
                shUpClient.postDicom(newFile);
            }
        } catch (URISyntaxException e) {
            logger.error("Error while reading file", e);
        }
    }

    @Test
    public void postDICOMMRToDicomWeb() throws Exception {
        Study study = createStudyAndCenterAndStudyCard();
        try {
            URL resource = getClass().getClassLoader().getResource("acr_phantom_t1/");
            if (resource != null) {
                File sourceDir = new File(resource.toURI());
                if (sourceDir.isDirectory()) {
                    File tempDir = generateStowRSDicom(study.getId().toString(), sourceDir);
                    if (tempDir != null) {
                        logger.info("Starting postDICOMMRToDicomWeb");
                        long startTime = System.currentTimeMillis();
                        for (File f : tempDir.listFiles()) {
                            try {
                                shUpClient.postDicom(f);
                            } catch(Exception e) {
                                logger.error(e.getMessage(), e);
                            }
                        }
                        long stopTime = System.currentTimeMillis();
                        long elapsedTime = stopTime - startTime;
                        logger.info("postDICOMMRToDicomWeb: " + elapsedTime + "ms");
                    }
                }
            }
        } catch (URISyntaxException e) {
            logger.error("Error while reading file", e);
        }
    }

    /**
     * The below method can be used to produce STOW-RS
     * ready DICOM files to test the import of STOW-RS
     * on the Shanoir server.
     *
     * @throws Exception
     */
    public File generateStowRSDicom(String studyId, File sourceDir) throws Exception {
        if (studyId != null && !studyId.isEmpty() && sourceDir != null) {
            String randomPatientName = UUID.randomUUID().toString().substring(0, 10);
            randomPatientName = randomPatientName.replaceAll("-", "");
            File destinationDir = new File(sourceDir.getAbsolutePath() + "-" + randomPatientName);
            destinationDir.mkdirs();
            iterateOverFiles(sourceDir, destinationDir, studyId, randomPatientName);
            return destinationDir;
        }
        return null;
    }

    private void iterateOverFiles(File sourceDir, File destinationDir, String studyId, String patientName) throws IOException {
        if (sourceDir.isDirectory()) {
            for (File f : sourceDir.listFiles()) {
                File newFile = new File(destinationDir, f.getName());
                newFile.createNewFile();
                modifyAndCopyDicomFile(f, newFile, studyId, patientName, null);
            }
        }
    }

    private void modifyAndCopyDicomFile(File f, File newFile, String studyId, String patientName, String studyInstanceUID) throws IOException {
        try (DicomInputStream dIn = new DicomInputStream(f);
                DicomOutputStream dOu = new DicomOutputStream(newFile);) {
            Attributes metaInformationAttributes = dIn.readFileMetaInformation();
            Attributes datasetAttributes = dIn.readDataset();
            String deidMethod = "Basic Application Confidentiality Profile Option";
            datasetAttributes.setString(Tag.DeidentificationMethod, VR.LO, deidMethod);
            datasetAttributes.setString(Tag.PatientIdentityRemoved, VR.CS, "YES");
            datasetAttributes.setString(Tag.ClinicalTrialProtocolID, VR.LO, studyId);
            datasetAttributes.setString(Tag.ClinicalTrialProtocolName, VR.LO, "Phantom QA Study");
            datasetAttributes.setString(Tag.PatientName, VR.PN, patientName);
            datasetAttributes.setString(Tag.PatientID, VR.LO, patientName);
            if (studyInstanceUID != null && !studyInstanceUID.isEmpty()) {
                datasetAttributes.setString(Tag.StudyInstanceUID, VR.UI, studyInstanceUID);
            }
            dOu.writeDataset(metaInformationAttributes, datasetAttributes);
        }
    }

}

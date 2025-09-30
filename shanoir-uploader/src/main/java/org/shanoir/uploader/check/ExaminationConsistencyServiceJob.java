package org.shanoir.uploader.check;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.dcm4che3.data.Attributes;
import org.dcm4che3.data.Tag;
import org.dcm4che3.io.DicomInputStream;
import org.dcm4che3.util.TagUtils;
import org.shanoir.ng.dicom.web.StudyInstanceUIDHandler;
import org.shanoir.ng.importer.dicom.DicomUtils;
import org.shanoir.ng.importer.model.ImportJob;
import org.shanoir.ng.importer.model.Instance;
import org.shanoir.ng.importer.model.Patient;
import org.shanoir.ng.importer.model.Serie;
import org.shanoir.ng.importer.model.Study;
import org.shanoir.ng.importer.model.UploadState;
import org.shanoir.uploader.ShUpConfig;
import org.shanoir.uploader.nominativeData.CurrentNominativeDataController;
import org.shanoir.uploader.nominativeData.NominativeDataImportJobManager;
import org.shanoir.uploader.service.rest.ShanoirUploaderServiceClient;
import org.shanoir.uploader.upload.UploadServiceJob;
import org.shanoir.uploader.utils.ImportUtils;
import org.shanoir.uploader.utils.Util;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

/*
 * This scheduled service iterates over all import job folders within
 * the workFolder and searches for import jobs, state FINISHED.
 *
 * For performance reasons, especially on the limit network bandwith
 * in the hospital, this job only runs, when no UploadServiceJob is
 * running, see LOCK.
 *
 * When the timestamp is older than 2 hours the DICOMWeb API of
 * the server is used to verify, that all local images have well
 * arrived on the server and that the examination is complete.
 * If all is perfect, that state moves to CHECK_OK or CHECK_KO.
 *
 * DICOM tags and the binary pixel data are compared instance/image
 * by instance/image.
 *
 * When the check runs on instance-by-instance after each
 *
 */
@Service
public class ExaminationConsistencyServiceJob {

    private static final Logger logger = LoggerFactory.getLogger(ExaminationConsistencyServiceJob.class);

    private static final long THIRTY_MIN_IN_MILLIS = 30 * 60 * 1000;

    private static final long ONE_HOUR_IN_MILLIS = 60 * 60 * 1000;

   	@Autowired
	private CurrentNominativeDataController currentNominativeDataController;

    @Autowired
	private ShanoirUploaderServiceClient shanoirUploaderServiceClient;

    @Scheduled(fixedRate = THIRTY_MIN_IN_MILLIS)
    public void execute() throws Exception {
        String value = ShUpConfig.basicProperties.getProperty(ShUpConfig.CHECK_ON_SERVER);
        boolean checkOnServer = Boolean.parseBoolean(value);
        if (checkOnServer) {
            if (!UploadServiceJob.LOCK.isLocked()) {
                logger.info("ExaminationConsistencyServiceJob started...");
                UploadServiceJob.LOCK.lock();
                File workFolder = new File(ShUpConfig.shanoirUploaderFolder.getAbsolutePath() + File.separator + ShUpConfig.WORK_FOLDER);
                processWorkFolder(workFolder, currentNominativeDataController);
                UploadServiceJob.LOCK.unlock();
                logger.info("ExaminationConsistencyServiceJob ended...");
            }
        }
	}

    private void processWorkFolder(File workFolder, CurrentNominativeDataController currentNominativeDataController) throws Exception {
        final List<File> folders = Util.listFolders(workFolder);
		logger.debug("Found " + folders.size() + " folders in work folder.");
		for (Iterator<File> foldersIt = folders.iterator(); foldersIt.hasNext();) {
			final File importJobFolder = (File) foldersIt.next();
			final File importJobFile = new File(importJobFolder.getAbsolutePath() + File.separator + ShUpConfig.IMPORT_JOB_JSON);
			// file could be missing in case of downloadOrCopy ongoing
			if (importJobFile.exists()) {
                // if the check.on.server flag has been activated after, do not check on previous
                // already imported folders, as they do not contain any DICOM anymore
                if (importJobFolder.listFiles().length > 1) {
                    NominativeDataImportJobManager importJobManager = new NominativeDataImportJobManager(importJobFile);
                    final ImportJob importJob = importJobManager.readImportJob();
                    // In case of previous importJobs (without uploadState) we look for uploadState value from upload-job.xml file
                    if (importJob.getUploadState() == null) {
                        String uploadState = ImportUtils.getUploadStateFromUploadJob(importJobFolder);
                        importJob.setUploadState(UploadState.fromString(uploadState));
                    }
                    final org.shanoir.ng.importer.model.UploadState uploadState = importJob.getUploadState();
                    if (uploadState.equals(org.shanoir.ng.importer.model.UploadState.FINISHED)) {
                        long timestamp = importJob.getTimestamp();
                        long currentTime = System.currentTimeMillis();
                        if (currentTime - timestamp >= ONE_HOUR_IN_MILLIS) {
                            String examinationUID = StudyInstanceUIDHandler.PREFIX + importJob.getExaminationId();
                            try {
                                boolean check = checkImportJob(importJob, importJobFolder, examinationUID);
                                if (check) {
                                    importJob.setUploadState(UploadState.CHECK_OK);
                                    importJobManager.writeImportJob(importJob);
                                    currentNominativeDataController.updateNominativeDataPercentage(importJobFolder, UploadState.CHECK_OK.toString());
                                }
                            } catch (Exception e) {
                                importJob.setUploadState(UploadState.CHECK_KO);
                                importJobManager.writeImportJob(importJob);
                                currentNominativeDataController.updateNominativeDataPercentage(importJobFolder, UploadState.CHECK_KO.toString());
                                logger.error(e.getMessage(), e);
                            }
                        }
                    }
                } // do nothing, keep already imported untouched
			} else {
				logger.error("Folder found in workFolder without import-job.json.");
			}
        }
    }

    private boolean checkImportJob(ImportJob importJob, File importJobFolder, String examinationUID) throws Exception {
        List<Patient> patients = ImportUtils.getPatientsFromDir(importJobFolder, true);
        if (patients != null) {
            for (Iterator<Patient> patientsIt = patients.iterator(); patientsIt.hasNext();) {
                Patient patient = (Patient) patientsIt.next();
                List<Study> studies = patient.getStudies();
                int numberOfInstances = 0;
                for (Iterator<Study> studiesIt = studies.iterator(); studiesIt.hasNext();) {
                    Study study = (Study) studiesIt.next();
                    List<Serie> series = study.getSeries();
                    for (Iterator<Serie> seriesIt = series.iterator(); seriesIt.hasNext();) {
                        Serie serie = (Serie) seriesIt.next();
                        List<Instance> instances = serie.getInstances();
                        for (Iterator<Instance> instancesIt = instances.iterator(); instancesIt.hasNext();) {
                            numberOfInstances = checkInstance(importJobFolder, examinationUID, numberOfInstances,
                                    serie, instancesIt);
                        }
                    }
                }
                logger.info(studies.size() + " DICOM study (examination) of"
                        + " subject: " + importJob.getSubjectName()
                        + ", studyDate: " + importJob.getStudy().getStudyDate()
                        + " checked for consistency of "
                        + numberOfInstances + " DICOM instances (images)");
            }
        }
        return true;
    }

    private int checkInstance(File importJobFolder, String examinationUID, int numberOfInstances, Serie serie,
            Iterator<Instance> instancesIt) throws FileNotFoundException, IOException, Exception {
        Instance instance = (Instance) instancesIt.next();
        String instanceFilePath = DicomUtils.referencedFileIDToPath(importJobFolder.getAbsolutePath(), instance.getReferencedFileID());
        File instanceFile = new File(instanceFilePath);
        if (instanceFile.exists()) {
            try (DicomInputStream dIn = new DicomInputStream(instanceFile)) {
                Attributes localInstance = dIn.readDataset();
                Attributes remoteInstance = shanoirUploaderServiceClient.getDicomInstance(
                        examinationUID, serie.getSeriesInstanceUID(), instance.getSopInstanceUID());
                if (remoteInstance != null) {
                    Boolean attributesEqual = compareAttributes(localInstance, remoteInstance);
                    byte[] pixelDataLocal = localInstance.getBytes(Tag.PixelData);
                    byte[] pixelDataRemote = remoteInstance.getBytes(Tag.PixelData);
                    Boolean pixelsEqual = java.util.Arrays.equals(pixelDataLocal, pixelDataRemote);
                    if (!attributesEqual || !pixelsEqual) {
                        logger.error("Serie: " + serie.getSeriesDescription() + ", error in DICOM instance: " + instanceFilePath);
                        throw new Exception("DICOM instance comparison issue: tags("
                            + attributesEqual + "), pixel(" + pixelsEqual + ")");
                    } else {
                        deleteInstanceFileAndSerieFolder(importJobFolder, instanceFile);
                        numberOfInstances++;
                    }
                } else {
                    throw new Exception("Serie: " + serie.getSeriesDescription()
                        + ", DICOM instance not found on server: " + instance.getSopInstanceUID());
                }
            }
        } else {
            logger.error("Serie: " + serie.getSeriesDescription()
                    + ", DICOM instance not found locally: " + instanceFilePath);
            throw new FileNotFoundException();
        }
        return numberOfInstances;
    }

    private boolean compareAttributes(Attributes localAttributes, Attributes remoteAttributes) {
        if (localAttributes.size() != remoteAttributes.size()) {
            logger.error("Number of tags differ.");
            return false;
        }
        int[] localTags = localAttributes.tags();
        for (int tag : localTags) {
            if (!remoteAttributes.contains(tag)) {
                logger.error("Missing tag in second file: " + TagUtils.toString(tag));
                return false;
            }
            String localValue = localAttributes.getString(tag, null);
            String remoteValue = remoteAttributes.getString(tag, null);
            if (localValue == null && remoteValue == null) {
                continue;
            }
            if (localValue == null || remoteValue == null || !localValue.equals(remoteValue)) {
                logger.error("Tag differs: " + TagUtils.toString(tag) +
                                " | " + localValue + " != " + remoteValue);
                return false;
            }
        }
        return true;
    }

    private void deleteInstanceFileAndSerieFolder(File importJobFolder, File instanceFile) {
        // from-disk: delete files directly
        if (instanceFile.getParentFile().equals(importJobFolder)) {
            FileUtils.deleteQuietly(instanceFile);
        // from-pacs: delete serieUID folder as well
        } else {
            FileUtils.deleteQuietly(instanceFile); // delete instance DICOM
            File serieFolder = instanceFile.getParentFile();
            File[] remainingFiles = serieFolder.listFiles();
            if (remainingFiles == null || remainingFiles.length == 0) {
                FileUtils.deleteQuietly(serieFolder);
            }
        }
    }

}
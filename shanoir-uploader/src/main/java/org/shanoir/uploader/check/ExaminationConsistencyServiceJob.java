package org.shanoir.uploader.check;

import java.io.File;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.shanoir.ng.dicom.web.StudyInstanceUIDHandler;
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
 * When the timestamp is older than 2 hours the DICOMWeb API of
 * the server is used to verify, that all local images have well
 * arrived on the server and that the examination is complete.
 * If all is perfect, that state moves to CHECK_OK or CHECK_KO.
 */
@Service
public class ExaminationConsistencyServiceJob {

    private static final Logger logger = LoggerFactory.getLogger(ExaminationConsistencyServiceJob.class);

    private static final long RATE = 1800000; // 30 min

   	@Autowired
	private CurrentNominativeDataController currentNominativeDataController;

    @Autowired
	private ShanoirUploaderServiceClient shanoirUploaderServiceClient;

    @Scheduled(fixedRate = 5000)
    public void execute() throws Exception {
		logger.info("ExaminationConsistencyServiceJob started...");
		File workFolder = new File(ShUpConfig.shanoirUploaderFolder.getAbsolutePath() + File.separator + ShUpConfig.WORK_FOLDER);
		processWorkFolder(workFolder, currentNominativeDataController);
        logger.info("ExaminationConsistencyServiceJob ended...");
	}

    private void processWorkFolder(File workFolder, CurrentNominativeDataController currentNominativeDataController) throws Exception {
        final List<File> folders = Util.listFolders(workFolder);
		logger.debug("Found " + folders.size() + " folders in work folder.");
		for (Iterator<File> foldersIt = folders.iterator(); foldersIt.hasNext();) {
			final File folder = (File) foldersIt.next();
			final File importJobFile = new File(folder.getAbsolutePath() + File.separator + ShUpConfig.IMPORT_JOB_JSON);
			// file could be missing in case of downloadOrCopy ongoing
			if (importJobFile.exists()) {
				NominativeDataImportJobManager importJobManager = new NominativeDataImportJobManager(importJobFile);
				final ImportJob importJob = importJobManager.readImportJob();
                // In case of previous importJobs (without uploadState) we look for uploadState value from upload-job.xml file
				if (importJob.getUploadState() == null) {
					String uploadState = ImportUtils.getUploadStateFromUploadJob(folder);
					importJob.setUploadState(UploadState.fromString(uploadState));
				}
				final org.shanoir.ng.importer.model.UploadState uploadState = importJob.getUploadState();
				if (uploadState.equals(org.shanoir.ng.importer.model.UploadState.FINISHED)) {
                    String examinationUID = StudyInstanceUIDHandler.PREFIX + importJob.getExaminationId();
                    checkImportJob(folder, examinationUID);
				}
			} else {
				logger.error("Folder found in workFolder without import-job.json.");
			}
        }
    }

    private void checkImportJob(File importJobFolder, String examinationUID) throws Exception {
        List<Patient> patients = ImportUtils.getPatientsFromDir(importJobFolder, false);	
		if (patients != null) {
			for (Iterator<Patient> patientsIt = patients.iterator(); patientsIt.hasNext();) {
				Patient patient = (Patient) patientsIt.next();
				List<Study> studies = patient.getStudies();
				for (Iterator<Study> studiesIt = studies.iterator(); studiesIt.hasNext();) {
					Study study = (Study) studiesIt.next();
					List<Serie> series = study.getSeries();
					for (Iterator<Serie> seriesIt = series.iterator(); seriesIt.hasNext();) {
						Serie serie = (Serie) seriesIt.next();
                        List<Instance> instances = serie.getInstances();
                        for (Iterator<Instance> instancesIt = instances.iterator(); instancesIt.hasNext();) {
                            Instance instance = (Instance) instancesIt.next();
                            shanoirUploaderServiceClient.getDicomInstance(examinationUID, serie.getSeriesInstanceUID(), instance.getSopInstanceUID());
                        }
                    }
				}
			}
		}
        //deleteAllDicomFiles(importJobFolder, null);
    }

    private void deleteAllDicomFiles(File importJobFolder, List<File> files) {
        // Clean all DICOM files after successful consistency check on server
        for (Iterator<File> iterator = files.iterator(); iterator.hasNext();) {
            File file = (File) iterator.next();
            // from-disk: delete files directly
            if (file.getParentFile().equals(importJobFolder)) {
                FileUtils.deleteQuietly(file);
            // from-pacs: delete serieUID folder as well
            } else {
                FileUtils.deleteQuietly(file.getParentFile());
            }
        }
        logger.info("All DICOM files deleted after successful check on server.");
    }

}
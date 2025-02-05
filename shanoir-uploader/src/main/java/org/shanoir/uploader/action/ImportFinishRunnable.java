package org.shanoir.uploader.action;

import java.io.File;
import java.io.IOException;

import org.shanoir.ng.importer.model.ImportJob;
import org.shanoir.uploader.ShUpConfig;
import org.shanoir.uploader.dicom.anonymize.Anonymizer;
import org.shanoir.uploader.upload.UploadJob;
import org.shanoir.uploader.upload.UploadJobManager;
import org.shanoir.uploader.upload.UploadState;
import org.shanoir.uploader.utils.Util;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class prepares the upload to a Shanoir server instance,
 * but does not call the server itself.
 * 
 * @author mkain
 *
 */
public class ImportFinishRunnable implements Runnable {

	private static final Logger logger = LoggerFactory.getLogger(ImportFinishRunnable.class);
	
	public static final String IMPORT_JOB_JSON = "import-job.json";

	private static final String ANONYMIZATION_PROFILE = "anonymization.profile";

	private UploadJob uploadJob;
	
	private File uploadFolder;
	
	private ImportJob importJob;
	
	private String subjectName;

	private Anonymizer anonymizer = new Anonymizer();
	
	public ImportFinishRunnable(final UploadJob uploadJob, final File uploadFolder, final ImportJob importJob, final String subjectName) {
		this.uploadJob = uploadJob;
		this.uploadFolder = uploadFolder;
		this.importJob = importJob;
		this.subjectName = subjectName;
	}

	public void run() {		
		/**
		 * Anonymize the DICOM files
		 */
		boolean anonymizationSuccess = false;
		try {
			String anonymizationProfile = ShUpConfig.profileProperties.getProperty(ANONYMIZATION_PROFILE);
			anonymizationSuccess = anonymizer.pseudonymize(uploadFolder, anonymizationProfile, subjectName, importJob.getStudyInstanceUID());
		} catch (IOException e) {
			logger.error(uploadFolder.getName() + ": " + e.getMessage(), e);
		}

		if (anonymizationSuccess) {
			/**
			 * Write import-job.json to disk
			 */
			try {
				File importJobJson = new File(uploadFolder, IMPORT_JOB_JSON);
				importJobJson.createNewFile();
				Util.objectMapper.writeValue(importJobJson, importJob);
			} catch (IOException e) {
				logger.error(uploadFolder.getName() + ": " + e.getMessage(), e);
			}
			
			/**
			 * Write the UploadJob and schedule upload
			 * We keep UploadJob here to start the upload and handle errors without
			 * developing something new with shanoir-exchange.json
			 */
			uploadJob.setUploadState(UploadState.START_AUTOIMPORT);
			UploadJobManager uploadJobManager = new UploadJobManager(uploadFolder.getAbsolutePath());
			uploadJobManager.writeUploadJob(uploadJob);
			logger.info(uploadFolder.getName() + ": DICOM files scheduled for upload.");
		} else {
			// NOTIFY THAT ANONYMIZATION HAS FAILED.
			logger.error(uploadFolder.getName() + ": Error during anonymization.");
		}
	}

}

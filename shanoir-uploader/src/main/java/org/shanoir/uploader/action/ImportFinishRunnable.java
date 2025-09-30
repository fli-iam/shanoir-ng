package org.shanoir.uploader.action;

import java.io.File;
import java.io.IOException;

import org.shanoir.ng.importer.model.ImportJob;
import org.shanoir.ng.importer.model.UploadState;
import org.shanoir.uploader.ShUpConfig;
import org.shanoir.uploader.dicom.anonymize.Anonymizer;
import org.shanoir.uploader.nominativeData.NominativeDataImportJobManager;
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

	private File uploadFolder;

	private ImportJob importJob;

	private String subjectName;

	private Anonymizer anonymizer = new Anonymizer();

	public ImportFinishRunnable(final File uploadFolder, final ImportJob importJob, final String subjectName) {
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
			String anonymizationProfile = ShUpConfig.profileProperties.getProperty(ShUpConfig.ANONYMIZATION_PROFILE);
			anonymizationSuccess = anonymizer.pseudonymize(uploadFolder, anonymizationProfile, subjectName);
		} catch (IOException e) {
			logger.error(uploadFolder.getName() + ": " + e.getMessage(), e);
		}

		if (anonymizationSuccess) {
			/**
			 * Write import-job.json to disk
			 */
			try {
				File importJobJson = new File(uploadFolder, ShUpConfig.IMPORT_JOB_JSON);
				importJobJson.createNewFile();
				Util.objectMapper.writeValue(importJobJson, importJob);
			} catch (IOException e) {
				logger.error(uploadFolder.getName() + ": " + e.getMessage(), e);
			}

			/**
			 * Write the ImportJob and schedule upload
			 * We keep ImportJob here to start the upload and handle errors without
			 * developing something new with shanoir-exchange.json
			 */
			importJob.setUploadState(UploadState.START_AUTOIMPORT);
			NominativeDataImportJobManager importJobManager = new NominativeDataImportJobManager(uploadFolder.getAbsolutePath());
			importJobManager.writeImportJob(importJob);
			logger.info(uploadFolder.getName() + ": DICOM files scheduled for upload.");
		} else {
			// NOTIFY THAT ANONYMIZATION HAS FAILED.
			logger.error(uploadFolder.getName() + ": Error during anonymization.");
		}
	}

}

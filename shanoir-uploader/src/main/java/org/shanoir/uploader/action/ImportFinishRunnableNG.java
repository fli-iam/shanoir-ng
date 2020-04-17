package org.shanoir.uploader.action;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.apache.log4j.Logger;
import org.quartz.JobExecutionContext;
import org.quartz.JobKey;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.SimpleScheduleBuilder;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;
import org.shanoir.dicom.importer.PreImportData;
import org.shanoir.dicom.importer.UploadJob;
import org.shanoir.dicom.importer.UploadJobManager;
import org.shanoir.dicom.importer.UploadState;
import org.shanoir.ng.exchange.model.Exchange;
import org.shanoir.uploader.ShUpConfig;
import org.shanoir.uploader.ShUpOnloadConfig;
import org.shanoir.uploader.dicom.anonymize.Anonymizer;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * This class prepares the upload to a Shanoir server instance,
 * but does not call the server itself.
 * 
 * @author mkain
 *
 */
public class ImportFinishRunnableNG implements Runnable {

	private static Logger logger = Logger.getLogger(ImportFinishRunnableNG.class);

	private static final String ANONYMIZATION_PROFILE = "anonymization.profile";

	private UploadJob uploadJob;
	
	private File uploadFolder;
	
	private Exchange exchange;
	
	private String subjectName;

	private Anonymizer anonymizer = new Anonymizer();
	
	public ImportFinishRunnableNG(final UploadJob uploadJob, final File uploadFolder, final Exchange exchange, final String subjectName) {
		this.uploadJob = uploadJob;
		this.uploadFolder = uploadFolder;
		this.exchange = exchange;
		this.subjectName = subjectName;
	}

	public void run() {		
		/**
		 * Anonymize the DICOM files
		 */
		boolean anonymizationSuccess = false;
		try {
			String anonymizationProfile = ShUpConfig.profileProperties.getProperty(ANONYMIZATION_PROFILE);
			anonymizationSuccess = anonymizer.anonymize(uploadFolder, anonymizationProfile, uploadJob, subjectName);
		} catch (IOException e) {
			logger.error(uploadFolder.getName() + ": " + e.getMessage(), e);
		}

		if (anonymizationSuccess) {
			logger.info(uploadFolder.getName() + ": DICOM files successfully anonymized.");
			/**
			 * Write exchange.json to disk
			 */
			ObjectMapper objectMapper = new ObjectMapper();
			try {
				File exchangeJson = new File(uploadFolder, Exchange.SHANOIR_EXCHANGE_JSON);
				exchangeJson.createNewFile();
				objectMapper.writeValue(exchangeJson, exchange);
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
			// try to run the UploadService
			runUploadService();
		} else {
			// NOTIFY THAT ANONYMIZATION HAS FAILED.
			logger.error(uploadFolder.getName() + ": Error during anonymization.");
		}
	}

	/**
	 * This method starts the UploadService.
	 */
	private void runUploadService() {
		boolean uploadServiceRunning = checkIfUploadServiceIsRunning();
		if (uploadServiceRunning) {
			// do nothing
		} else {
			Scheduler scheduler = ShUpOnloadConfig.getScheduler();
			Trigger oldTrigger = ShUpOnloadConfig.getTrigger();
			Trigger newTrigger = TriggerBuilder
					.newTrigger()
					.withSchedule(
							SimpleScheduleBuilder
									.simpleSchedule()
									.withIntervalInSeconds(
											ShUpConfig.UPLOAD_SERVICE_INTERVAL)
									.repeatForever()).build();
			try {
				scheduler.rescheduleJob(oldTrigger.getKey(), newTrigger);
			} catch (SchedulerException sE) {
				logger.error(sE.getMessage(), sE);
			}
		}
	}
	
	/**
	 * This method checks if the UploadService is currently running.
	 * 
	 * @return
	 */
	private boolean checkIfUploadServiceIsRunning() {
		List<JobExecutionContext> currentJobs;
		try {
			if (ShUpOnloadConfig.getScheduler() != null) {
				currentJobs = ShUpOnloadConfig.getScheduler()
						.getCurrentlyExecutingJobs();
				for (JobExecutionContext jobCtx : currentJobs) {
					JobKey jobKey = jobCtx.getJobDetail().getKey();
					if (jobKey.getName().equalsIgnoreCase(
							ShUpConfig.UPLOAD_SERVICE_JOB)) {
						return true;
					}
				}
			}
		} catch (SchedulerException sE) {
			logger.error(sE.getMessage(), sE);
		}
		return false;
	}

}

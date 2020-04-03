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
import org.shanoir.uploader.ShUpConfig;
import org.shanoir.uploader.ShUpOnloadConfig;
import org.shanoir.uploader.dicom.anonymize.Anonymizer;

/**
 * This class prepares the upload to a Shanoir server instance,
 * but does not call the server itself.
 * 
 * @author mkain
 *
 */
public class ImportFinishRunnable implements Runnable {

	private static Logger logger = Logger.getLogger(ImportFinishRunnable.class);

	private static final String ANONYMIZATION_PROFILE = "anonymization.profile";

	private UploadJob uploadJob;
	
	private File uploadFolder;
	
	private PreImportData preImportData;
	
	private String subjectName;

	private Anonymizer anonymizer = new Anonymizer();
	
	public ImportFinishRunnable(final UploadJob uploadJob, final File uploadFolder, final PreImportData preImportData, final String subjectName) {
		this.uploadJob = uploadJob;
		this.uploadFolder = uploadFolder;
		this.preImportData = preImportData;
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
			 * Write the UploadJob and schedule upload
			 */
			initUploadJob(uploadJob, preImportData);
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

//	/**
//	 * @param selectedSeries
//	 * @param dicomData
//	 * @param folderForUpload
//	 * @param tempFolderForUpload
//	 * @param out
//	 * @param inProgressfile
//	 * @throws IOException
//	 */
//	private void prepareNGUpload(final Set<org.shanoir.dicom.importer.Serie> selectedSeries,
//			final DicomDataTransferObject dicomData, File folderForUpload, File tempFolderForUpload, OutputStream out,
//			File inProgressfile) throws IOException {
//		ImportJob importJob = initImportJob(selectedSeries, dicomData);
//		ObjectWriter ow = new ObjectMapper().writer().withDefaultPrettyPrinter();
//		String payload = null;
//		try {
//			payload = ow.writeValueAsString(importJob);
//		} catch (JsonProcessingException e1) {
//			logger.error(e1.getMessage(), e1);
//		}
//		try {
//			File file = new File(tempFolderForUpload + "/importJob.json");
//			out = new FileOutputStream(file);
//			// if file doesnt exists, then create it
//			if (!file.exists()) {
//				file.createNewFile();
//			}
//			// get the content in bytes
//			byte[] contentInBytes = payload.getBytes();
//			out.write(contentInBytes);
//			out.flush();
//			out.close();
//		} catch (IOException e) {
//			logger.error(e.getMessage(), e);
//		} finally {
//			try {
//				if (out != null) {
//					out.close();
//				}
//			} catch (IOException e) {
//				logger.error(e.getMessage(), e);
//			}
//		}
//		File[] filesInDirToRemove = tempFolderForUpload.listFiles();
//		ZipUtil.zipFile(tempFolderForUpload.getAbsolutePath(), folderForUpload.getAbsolutePath() + "/data.zip", true);
//		for (int i = 0; i < filesInDirToRemove.length; i++) {
//			filesInDirToRemove[i].delete();
//		}
//		inProgressfile.delete();
//		tempFolderForUpload.delete();
//	}

//	/**
//	 * Initializes UploadJob object to be written to file system.
//	 * 
//	 * @param selectedSeries
//	 * @param dicomData
//	 * @param uploadJob
//	 */
//	private ImportJob initImportJob(final Set<org.shanoir.dicom.importer.Serie> selectedSeries,
//			final DicomDataTransferObject dicomData) {
//		ExportData exportData = mainWindow.getiDL().getExportData();
//		ImportJob importJob = new ImportJob();
//		importJob.setExaminationId(preImportData.getExaminationId());
//		importJob.setFromDicomZip(false);
//		importJob.setFromPacs(false);
//		importJob.setFromShanoirUploader(true);
//		importJob.setFrontStudyCardId(Long.valueOf(preImportData.getStudycard().getId()));
//		importJob.setFrontStudyId(Long.valueOf(preImportData.getStudy().getId()));
//		importJob.setFrontConverterId(1L);
//
//		List<Serie> serieList = new ArrayList<Serie>();
//		for (org.shanoir.dicom.importer.Serie s : selectedSeries) {
//			List<Image> imageList = new ArrayList<Image>();
//			for (String filename : s.getFileNames()) {
//				Image i = new Image();
//				i.setPath(filename);
//				imageList.add(i);
//			}
//			Serie serie = new Serie();
//			serie.setImages(imageList);
//			serie.setModality(s.getModality());
//			serie.setProtocolName(s.getProtocol());
//			serie.setSeriesNumber(Integer.valueOf(s.getSeriesNumber()));
//			// serie.setSopClassUID();
//			serie.setSelected(true);
//			// serie.setSeriesInstanceUID(s.getStudyInstanceUID());
//			serieList.add(serie);
//		}
//
//		List<Study> studyList = new ArrayList<Study>();
//		Study study = new Study();
//		study.setStudyDate(Util.toDate(mainWindow.getiDL().getExportData().getDateOfNewExamination()));
//		study.setStudyDescription(dicomData.getStudyDescription());
//		study.setStudyInstanceUID(dicomData.getStudyInstanceUID());
//		study.setSeries(serieList);
//		studyList.add(study);
//
//		Subject subject = new Subject();
//		subject.setId(mainWindow.getiDL().getExportData().getSubject().getId());
//		subject.setName(mainWindow.getiDL().getExportData().getSubject().getName());
//
//		Patient patient = new Patient();
//		patient.setSubject(subject);
//		patient.setPatientBirthDate(getFirstDayOfTheYear(dicomData.getBirthDate()));
//		patient.setPatientID(dicomData.getNewPatientID());
//		// patient.setPatientName(dicomData.getLastName());
//		patient.setPatientSex(dicomData.getSex());
//		patient.setStudies(studyList);
//
//		List<Patient> patientList = new ArrayList<Patient>();
//		patientList.add(patient);
//		importJob.setPatients(patientList);
//		return importJob;
//	}

	/**
	 * Initializes UploadJob object to be written to file system.
	 * 
	 * @param selectedSeries
	 * @param dicomData
	 * @param uploadJob
	 */
	private void initUploadJob(UploadJob uploadJob, final PreImportData preImportData) {
		if (preImportData != null) {
			uploadJob.setUploadState(UploadState.START_AUTOIMPORT);
			uploadJob.setPreImportdata(preImportData);
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

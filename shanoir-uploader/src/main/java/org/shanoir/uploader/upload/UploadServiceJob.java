package org.shanoir.uploader.upload;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;
import org.quartz.DisallowConcurrentExecution;
import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.shanoir.dicom.importer.UploadJob;
import org.shanoir.dicom.importer.UploadJobManager;
import org.shanoir.dicom.importer.UploadState;
import org.shanoir.uploader.ShUpConfig;
import org.shanoir.uploader.ShUpOnloadConfig;
import org.shanoir.uploader.nominativeData.CurrentNominativeDataController;
import org.shanoir.uploader.nominativeData.NominativeDataUploadJob;
import org.shanoir.uploader.nominativeData.NominativeDataUploadJobManager;
import org.shanoir.uploader.service.rest.ShanoirUploaderServiceClientNG;
import org.shanoir.uploader.service.soap.ShanoirUploaderServiceClient;
import org.shanoir.util.ShanoirUtil;

/**
 * The UploadServiceJob.
 * 
 * @author mkain
 * 
 */
@DisallowConcurrentExecution
public class UploadServiceJob implements Job {

	private static Logger logger = Logger.getLogger(UploadServiceJob.class);

	private ShanoirUploaderServiceClient uploadServiceClient;
	
	private ShanoirUploaderServiceClientNG uploadServiceClientNG;

	private String uploadPercentage = "";

	/**
	 * The execution method
	 */
	public void execute(JobExecutionContext context) throws JobExecutionException {
		logger.debug("UploadServiceJob started...");
		JobDataMap dataMap = context.getJobDetail().getJobDataMap();
		CurrentNominativeDataController currentNominativeDataController = (CurrentNominativeDataController) dataMap
				.get("nominativeDataController");
		uploadServiceClient = (ShanoirUploaderServiceClient) dataMap.get("uploadServiceClient");
		uploadServiceClientNG = (ShanoirUploaderServiceClientNG) dataMap.get("uploadServiceClientNG");
		String workFolderFilePath = dataMap.getString(ShUpConfig.WORK_FOLDER);
		File workFolder = new File(workFolderFilePath);
		processWorkFolder(workFolder, currentNominativeDataController);
		logger.info("UploadServiceJob ended...");
	}

	/**
	 * Walk trough all folders within the work folder.
	 * 
	 * @param workFolder
	 */
	private void processWorkFolder(File workFolder, CurrentNominativeDataController currentNominativeDataController) {
		final List<File> folders = ShanoirUtil.listFolders(workFolder);
		logger.debug("Found " + folders.size() + " folders in work folder.");
		for (Iterator foldersIt = folders.iterator(); foldersIt.hasNext();) {
			final File folder = (File) foldersIt.next();
			final File uploadJobFile = new File(folder.getAbsolutePath() + File.separator + UploadJobManager.UPLOAD_JOB_XML);
			// file could be missing in case of downloadOrCopy ongoing
			if (uploadJobFile.exists()) {
				UploadJobManager uploadJobManager = new UploadJobManager(uploadJobFile);
				final UploadJob uploadJob = uploadJobManager.readUploadJob();
				final UploadState uploadState = uploadJob.getUploadState();
				// Avoid reading all files (a lot) in case of finished upload
				if (!uploadState.equals(UploadState.FINISHED_UPLOAD)) {
					processFolderForServer(folder, uploadJobManager, uploadJobFile, currentNominativeDataController);
				}
			}
		}
	}

	/**
	 * Inspects the content of a folder.
	 * 
	 * @param folder
	 */
	private void processFolderForServer(final File folder, final UploadJobManager uploadJobManager,
			final File uploadJobFile, CurrentNominativeDataController currentNominativeDataController) {
		logger.info("Started processing folder " + folder.getName() + "...");
		NominativeDataUploadJobManager nominativeDataUploadJobManager = null;
		final List<File> filesToTransfer = new ArrayList<File>();
		final Collection<File> files = ShanoirUtil.listFiles(folder, null, false);
		for (Iterator filesIt = files.iterator(); filesIt.hasNext();) {
			final File file = (File) filesIt.next();
			// do not transfer nominativeDataUploadJob as only for display in ShUp
			if (file.getName().equals(NominativeDataUploadJobManager.NOMINATIVE_DATA_JOB_XML)) {
				nominativeDataUploadJobManager = new NominativeDataUploadJobManager(file);
		    // remove upload-job.xml from the list of files to transfer, to guarantee later
			// that this file is for sure transferred as the last file to avoid sync problems
			// on the server, when auto-import starts with still missing files
			} else if (file.getName().equals(UploadJobManager.UPLOAD_JOB_XML)) {
				// do not add to list
		    } else {
				filesToTransfer.add(file);
			}
		}
		if (uploadJobManager != null && nominativeDataUploadJobManager != null) {
			final UploadJob uploadJob = uploadJobManager.readUploadJob();
			final UploadState uploadState = uploadJob.getUploadState();
			final NominativeDataUploadJob nominativeDataUploadJob = nominativeDataUploadJobManager.readUploadDataJob();
			nominativeDataUploadJob.setUploadState(uploadState);
			if (uploadState.equals(UploadState.START) || uploadState.equals(UploadState.START_AUTOIMPORT)) {
				if (ShUpOnloadConfig.isShanoirNg()) {
					processStartForServerNG(folder, filesToTransfer, uploadJob, nominativeDataUploadJob,
						uploadJobManager, nominativeDataUploadJobManager, currentNominativeDataController);
				} else {
					processStartForServer(folder, filesToTransfer, uploadJob, nominativeDataUploadJob,
							uploadJobManager, nominativeDataUploadJobManager, currentNominativeDataController);					
				}
			}
		} else {
			logger.error("Folder found in workFolder without upload-job.xml.");
		}
		logger.debug("Ended processing folder " + folder.getName() + ".");
	}

	/**
	 * This method processes the state START.
	 * 
	 * @param folder
	 * @param allFiles
	 * @param uploadJob
	 */
	private void processStartForServer(final File folder, final List<File> allFiles,
			final UploadJob uploadJob, final NominativeDataUploadJob nominativeDataUploadJob,
			UploadJobManager uploadJobManager, NominativeDataUploadJobManager nominativeDataUploadJobManager,
			CurrentNominativeDataController currentNominativeDataController) {
		try {
			int i = 0;
			for (Iterator iterator = allFiles.iterator(); iterator.hasNext();) {
				File file = (File) iterator.next();
				i++;
				logger.debug("UploadServiceJob started to upload file: " + file.getName());
				uploadServiceClient.uploadFile(folder.getName(), file);
				logger.debug("UploadServiceJob finished to upload file: " + file.getName());
				uploadPercentage = i * 100 / allFiles.size() + " %";
				nominativeDataUploadJob.setUploadPercentage(uploadPercentage);
				currentNominativeDataController.updateNominativeDataPercentage(folder, uploadPercentage);
				nominativeDataUploadJobManager.writeUploadDataJob(nominativeDataUploadJob);
				logger.debug("Upload percentage of folder " + folder.getName() + " = " + uploadPercentage + ".");
			}
			/**
			 * Explicitly upload the upload-job.xml as the last file to avoid sync problems on server in case of
			 * many files have to be uploaded.
			 */
			File uploadJobXML = new File(folder.getAbsolutePath() + File.separator + UploadJobManager.UPLOAD_JOB_XML);
			uploadServiceClient.uploadFile(folder.getName(), uploadJobXML);
			uploadJob.setUploadState(UploadState.FINISHED_UPLOAD);
			currentNominativeDataController.updateNominativeDataPercentage(folder,
					UploadState.FINISHED_UPLOAD.toString());
			uploadJob.setUploadDate(ShanoirUtil.formatTimePattern(new Date()));
			uploadJobManager.writeUploadJob(uploadJob);
		} catch (Exception e) {
			currentNominativeDataController.updateNominativeDataPercentage(folder, UploadState.ERROR.toString());
			uploadJob.setUploadState(UploadState.ERROR);
			uploadJob.setUploadDate(ShanoirUtil.formatTimePattern(new Date()));
			uploadJobManager.writeUploadJob(uploadJob);
			logger.error("An error occured during upload : " + e.getMessage());
		}
	}

	/**
	 * This method processes the state START.
	 * 
	 * @param folder
	 * @param allFiles
	 * @param uploadJob
	 */
	private void processStartForServerNG(final File folder, final List<File> allFiles,
			final UploadJob uploadJob, final NominativeDataUploadJob nominativeDataUploadJob,
			UploadJobManager uploadJobManager, NominativeDataUploadJobManager nominativeDataUploadJobManager,
			CurrentNominativeDataController currentNominativeDataController) {
		try {
			String tempDirId = uploadServiceClientNG.createTempDir();
			int i = 0;
			for (Iterator iterator = allFiles.iterator(); iterator.hasNext();) {
				File file = (File) iterator.next();
				i++;
				logger.debug("UploadServiceJob started to upload file: " + file.getName());
//				uploadServiceClient.uploadFile(folder.getName(), file);
				logger.debug("UploadServiceJob finished to upload file: " + file.getName());
				uploadPercentage = i * 100 / allFiles.size() + " %";
				nominativeDataUploadJob.setUploadPercentage(uploadPercentage);
				currentNominativeDataController.updateNominativeDataPercentage(folder, uploadPercentage);
				nominativeDataUploadJobManager.writeUploadDataJob(nominativeDataUploadJob);
				logger.debug("Upload percentage of folder " + folder.getName() + " = " + uploadPercentage + ".");
			}
			/**
			 * Explicitly upload the upload-job.xml as the last file to avoid sync problems on server in case of
			 * many files have to be uploaded.
			 */
			File uploadJobXML = new File(folder.getAbsolutePath() + File.separator + UploadJobManager.UPLOAD_JOB_XML);
//			uploadServiceClient.uploadFile(folder.getName(), uploadJobXML);
			uploadJob.setUploadState(UploadState.FINISHED_UPLOAD);
			currentNominativeDataController.updateNominativeDataPercentage(folder,
					UploadState.FINISHED_UPLOAD.toString());
			uploadJob.setUploadDate(ShanoirUtil.formatTimePattern(new Date()));
			uploadJobManager.writeUploadJob(uploadJob);
		} catch (Exception e) {
			currentNominativeDataController.updateNominativeDataPercentage(folder, UploadState.ERROR.toString());
			uploadJob.setUploadState(UploadState.ERROR);
			uploadJob.setUploadDate(ShanoirUtil.formatTimePattern(new Date()));
			uploadJobManager.writeUploadJob(uploadJob);
			logger.error("An error occured during upload : " + e.getMessage());
		}
	}
	
}
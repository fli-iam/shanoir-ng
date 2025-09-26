package org.shanoir.uploader.upload;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;

import org.apache.commons.io.FileUtils;
import org.shanoir.ng.importer.model.ImportJob;
import org.shanoir.ng.importer.model.UploadState;
import org.shanoir.uploader.ShUpConfig;
import org.shanoir.uploader.dicom.retrieve.DcmRcvManager;
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

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;

/**
 * The UploadServiceJob.
 * 
 * @author mkain
 * 
 */
@Service
public class UploadServiceJob {

	private static final Logger logger = LoggerFactory.getLogger(UploadServiceJob.class);

	public static final ReentrantLock LOCK = new ReentrantLock();

	@Autowired
	private ShanoirUploaderServiceClient shanoirUploaderServiceClient;

	@Autowired
	private CurrentNominativeDataController currentNominativeDataController;

	private String uploadPercentage = "";

	@Scheduled(fixedRate = 5000)
	public void execute() throws Exception {
		if (!LOCK.isLocked()) {
			logger.debug("UploadServiceJob started...");
			LOCK.lock();
			File workFolder = new File(ShUpConfig.shanoirUploaderFolder.getAbsolutePath() + File.separator + ShUpConfig.WORK_FOLDER);
			processWorkFolder(workFolder, currentNominativeDataController);
			LOCK.unlock();
			logger.debug("UploadServiceJob ended...");
		}
	}

	/**
	 * Walk trough all folders within the work folder.
	 * 
	 * @param workFolder
	 * @throws IOException 
	 */
	private void processWorkFolder(File workFolder, CurrentNominativeDataController currentNominativeDataController) throws IOException {
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
				// Avoid reading all files (a lot) in case of finished upload
				if (!uploadState.equals(org.shanoir.ng.importer.model.UploadState.FINISHED)) {
					processFolderForServer(folder, importJobManager, importJob, currentNominativeDataController);
				}
			} else {
				logger.warn("Folder found in workFolder without import-job.json.");
			}
		}
	}

	/**
	 * Inspects the content of a folder.
	 * 
	 * @param folder
	 */
	private void processFolderForServer(final File folder, final NominativeDataImportJobManager importJobManager,
			final ImportJob importJob, CurrentNominativeDataController currentNominativeDataController) {
		final List<File> filesToTransfer = new ArrayList<File>();
		/**
		 * Get all files from uploadFolder (importJob) and send them.
		 * No reading of job content. Files from seriesInstanceUID folders
		 * are transferred as a flat list and the list in the import-job.json
		 * is flat as well.
		 */
		final Collection<File> files = Util.listFiles(folder, null, true);
		for (Iterator<File> filesIt = files.iterator(); filesIt.hasNext();) {
			final File file = (File) filesIt.next();
		    if (file.getName().endsWith(DcmRcvManager.DICOM_FILE_SUFFIX)) {
				filesToTransfer.add(file);
			}
		}
		if (importJobManager != null) {
			final org.shanoir.ng.importer.model.UploadState uploadState = importJob.getUploadState();
			if (uploadState.equals(org.shanoir.ng.importer.model.UploadState.START) || uploadState.equals(org.shanoir.ng.importer.model.UploadState.START_AUTOIMPORT)) {
				long startTime = System.currentTimeMillis();
				processStartForServer(folder, filesToTransfer, importJob, importJobManager, currentNominativeDataController);
				long stopTime = System.currentTimeMillis();
			    long elapsedTime = stopTime - startTime;
				logger.info("Upload of files in folder: " + folder.getAbsolutePath() + " finished in duration (ms): " + elapsedTime);
			}
		} else {
			logger.error("importJobManager is null.");
		}
	}

	/**
	 * This method processes the state START.
	 * 
	 * @param folder
	 * @param allFiles
	 * @param uploadJob
	 */
	private void processStartForServer(final File folder, final List<File> allFiles,
			final ImportJob importJob, NominativeDataImportJobManager nominativeDataImportJobManager,
			CurrentNominativeDataController currentNominativeDataController) {
		try {
			String tempDirId = shanoirUploaderServiceClient.createTempDir();
			logger.info("Upload: tempDirId for import: " + tempDirId);
			/**
			 * Upload all DICOM files, one by one.
			 */
			int i = 0;
			for (Iterator<File> iterator = allFiles.iterator(); iterator.hasNext();) {
				File file = (File) iterator.next();
				i++;
				logger.debug("UploadServiceJob started to upload file: " + file.getName());
				shanoirUploaderServiceClient.uploadFile(tempDirId, file);
				logger.debug("UploadServiceJob finished to upload file: " + file.getName());
				uploadPercentage = i * 100 / allFiles.size() + " %";
				// following 2 lines are doing same thing ?
				importJob.setUploadPercentage(uploadPercentage);
				currentNominativeDataController.updateNominativeDataPercentage(folder, uploadPercentage);
				nominativeDataImportJobManager.writeImportJob(importJob);
				logger.debug("Upload percentage of folder " + folder.getName() + " = " + uploadPercentage + ".");
			}
			logger.info("Upload: " + allFiles.size() + " uploaded files to tempDirId: " + tempDirId);

			/**
			 * Start job on server
			 */
			setTempDirIdAndStartImport(tempDirId, importJob);	
			currentNominativeDataController.updateNominativeDataPercentage(folder,
				org.shanoir.ng.importer.model.UploadState.FINISHED.toString());
			importJob.setUploadState(org.shanoir.ng.importer.model.UploadState.FINISHED);
			importJob.setTimestamp(System.currentTimeMillis());
			nominativeDataImportJobManager.writeImportJob(importJob);

			// Delete DICOM files, if check.on.server is false
	        String value = ShUpConfig.basicProperties.getProperty(ShUpConfig.CHECK_ON_SERVER);
			boolean checkOnServer = Boolean.parseBoolean(value);
			if (!checkOnServer) {
				// Clean all DICOM files after successful import to server
				deleteAllDicomFiles(folder, allFiles);
			}
		} catch (Exception e) {
			currentNominativeDataController.updateNominativeDataPercentage(folder, org.shanoir.ng.importer.model.UploadState.ERROR.toString());
			importJob.setUploadState(org.shanoir.ng.importer.model.UploadState.ERROR);
			importJob.setTimestamp(System.currentTimeMillis());
			nominativeDataImportJobManager.writeImportJob(importJob);
			logger.error("An error occurred during upload to server: " + e.getMessage());
		}
	}

	private void setTempDirIdAndStartImport(String tempDirId, ImportJob importJob)
			throws IOException, JsonParseException, JsonMappingException, JsonProcessingException, Exception {
		importJob.setWorkFolder(tempDirId);
		String importJobJson = Util.objectWriter.writeValueAsString(importJob);
		shanoirUploaderServiceClient.startImportJob(importJobJson);
	}

	private void deleteAllDicomFiles(File importJobFolder, List<File> files) {
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
        logger.info("All DICOM files deleted after successful upload to server.");
    }

}
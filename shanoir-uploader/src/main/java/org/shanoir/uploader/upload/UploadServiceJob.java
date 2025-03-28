package org.shanoir.uploader.upload;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.shanoir.ng.importer.model.ImportJob;
import org.shanoir.uploader.ShUpConfig;
import org.shanoir.uploader.action.ImportFinishRunnable;
import org.shanoir.uploader.dicom.retrieve.DcmRcvManager;
import org.shanoir.uploader.nominativeData.CurrentNominativeDataController;
import org.shanoir.uploader.nominativeData.NominativeDataUploadJob;
import org.shanoir.uploader.nominativeData.NominativeDataUploadJobManager;
import org.shanoir.uploader.service.rest.ShanoirUploaderServiceClient;
import org.shanoir.uploader.utils.ImportUtils;
import org.shanoir.uploader.utils.Util;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;

/**
 * The UploadServiceJob.
 *
 * @author mkain
 *
 */
@Component
public class UploadServiceJob {

    private static final Logger logger = LoggerFactory.getLogger(UploadServiceJob.class);

    @Autowired
    private ShanoirUploaderServiceClient shanoirUploaderServiceClient;

    @Autowired
    private CurrentNominativeDataController currentNominativeDataController;

    private String uploadPercentage = "";

    private boolean uploading;

    @Scheduled(fixedRate = 5000)
    public void execute() {
        logger.debug("UploadServiceJob started...");
        uploading = false;
        File workFolder = new File(ShUpConfig.shanoirUploaderFolder.getAbsolutePath() + File.separator + ShUpConfig.WORK_FOLDER);
        processWorkFolder(workFolder, currentNominativeDataController);
        logger.debug("UploadServiceJob ended...");
    }

    /**
     * Walk trough all folders within the work folder.
     *
     * @param workFolder
     */
    private void processWorkFolder(File workFolder, CurrentNominativeDataController currentNominativeDataController) {
        final List<File> folders = Util.listFolders(workFolder);
        logger.debug("Found " + folders.size() + " folders in work folder.");
        for (Iterator<File> foldersIt = folders.iterator(); foldersIt.hasNext();) {
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
        NominativeDataUploadJobManager nominativeDataUploadJobManager = null;
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
            // do not transfer nominativeDataUploadJob as only for display in ShUp
            if (file.getName().equals(NominativeDataUploadJobManager.NOMINATIVE_DATA_JOB_XML)) {
                nominativeDataUploadJobManager = new NominativeDataUploadJobManager(file);
            // remove upload-job.xml from the list of files to transfer, to guarantee later
            // that this file is for sure transferred as the last file to avoid sync problems
            // on the server, when auto-import starts with still missing files
            } else if (file.getName().equals(UploadJobManager.UPLOAD_JOB_XML)
                    || file.getName().equals(ImportFinishRunnable.IMPORT_JOB_JSON)) {
                // do not add to list
            } else {
                if (file.getName().endsWith(DcmRcvManager.DICOM_FILE_SUFFIX))
                    filesToTransfer.add(file);
            }
        }
        if (uploadJobManager != null && nominativeDataUploadJobManager != null) {
            final UploadJob uploadJob = uploadJobManager.readUploadJob();
            final UploadState uploadState = uploadJob.getUploadState();
            final NominativeDataUploadJob nominativeDataUploadJob = nominativeDataUploadJobManager.readUploadDataJob();
            nominativeDataUploadJob.setUploadState(uploadState);
            if (uploadState.equals(UploadState.START) || uploadState.equals(UploadState.START_AUTOIMPORT)) {
                long startTime = System.currentTimeMillis();
                processStartForServer(folder, filesToTransfer, uploadJob, nominativeDataUploadJob,
                        uploadJobManager, nominativeDataUploadJobManager, currentNominativeDataController);
                long stopTime = System.currentTimeMillis();
                long elapsedTime = stopTime - startTime;
                logger.info("Upload of files in folder: " + folder.getAbsolutePath() + " finished in duration (ms): " + elapsedTime);
            }
        } else {
            logger.error("Folder found in workFolder without upload-job.xml.");
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
            final UploadJob uploadJob, final NominativeDataUploadJob nominativeDataUploadJob,
            UploadJobManager uploadJobManager, NominativeDataUploadJobManager nominativeDataUploadJobManager,
            CurrentNominativeDataController currentNominativeDataController) {
        try {
            uploading = true;
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
                nominativeDataUploadJob.setUploadPercentage(uploadPercentage);
                currentNominativeDataController.updateNominativeDataPercentage(folder, uploadPercentage);
                nominativeDataUploadJobManager.writeUploadDataJob(nominativeDataUploadJob);
                logger.debug("Upload percentage of folder " + folder.getName() + " = " + uploadPercentage + ".");
            }
            logger.info("Upload: " + allFiles.size() + " uploaded files to tempDirId: " + tempDirId);

            /**
             * Read import-job.json and start job on server
             */
            ImportJob importJob = ImportUtils.readImportJob(folder);
            setTempDirIdAndStartImport(tempDirId, importJob);
            currentNominativeDataController.updateNominativeDataPercentage(folder,
                    UploadState.FINISHED_UPLOAD.toString());
            uploadJob.setUploadState(UploadState.FINISHED_UPLOAD);
            uploadJob.setUploadDate(Util.formatTimePattern(new Date()));
            uploadJobManager.writeUploadJob(uploadJob);

            // Clean all DICOM files after successful import to server
            for (Iterator<File> iterator = allFiles.iterator(); iterator.hasNext();) {
                File file = (File) iterator.next();
                // from-disk: delete files directly
                if (file.getParentFile().equals(folder)) {
                    FileUtils.deleteQuietly(file);
                // from-pacs: delete serieUID folder as well
                } else {
                    FileUtils.deleteQuietly(file.getParentFile());
                }
            }
            logger.info("All DICOM files deleted after successful upload to server.");

            uploading = false;
        } catch (Exception e) {
            currentNominativeDataController.updateNominativeDataPercentage(folder, UploadState.ERROR.toString());
            uploadJob.setUploadState(UploadState.ERROR);
            uploadJob.setUploadDate(Util.formatTimePattern(new Date()));
            uploadJobManager.writeUploadJob(uploadJob);
            logger.error("An error occurred during upload to server: " + e.getMessage());
        }
    }

    /**
     * @param tempDirId
     * @param importJobJsonFile
     * @throws IOException
     * @throws JsonParseException
     * @throws JsonMappingException
     * @throws JsonProcessingException
     * @throws Exception
     */
    private void setTempDirIdAndStartImport(String tempDirId, ImportJob importJob)
            throws IOException, JsonParseException, JsonMappingException, JsonProcessingException, Exception {
        importJob.setWorkFolder(tempDirId);
        String importJobJson = Util.objectWriter.writeValueAsString(importJob);
        shanoirUploaderServiceClient.startImportJob(importJobJson);
    }

    public boolean isUploading() {
        return uploading;
    }

    public void setUploading(boolean uploading) {
        this.uploading = uploading;
    }

}
/**
 * Shanoir NG - Import, manage and share neuroimaging data
 * Copyright (C) 2009-2019 Inria - https://www.inria.fr/
 * Contact us on https://project.inria.fr/shanoir/
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see https://www.gnu.org/licenses/gpl-3.0.html
 */

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
import org.shanoir.ng.importer.model.ImportJobBase;
import org.shanoir.ng.importer.model.ImportJobStatus;
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

    private static final Logger LOG = LoggerFactory.getLogger(UploadServiceJob.class);

    public static final ReentrantLock LOCK = new ReentrantLock();

    @Autowired
    private ShanoirUploaderServiceClient shanoirUploaderServiceClient;

    @Autowired
    private CurrentNominativeDataController currentNominativeDataController;

    private String uploadPercentage = "";

    @Scheduled(fixedRate = 5000)
    public void execute() throws Exception {
        if (!LOCK.isLocked()) {
            LOG.debug("UploadServiceJob started...");
            LOCK.lock();
            File workFolder = new File(ShUpConfig.shanoirUploaderFolder.getAbsolutePath() + File.separator + ShUpConfig.WORK_FOLDER);
            processWorkFolder(workFolder, currentNominativeDataController);
            LOCK.unlock();
            LOG.debug("UploadServiceJob ended...");
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
        LOG.debug("Found " + folders.size() + " folders in work folder.");
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
                LOG.warn("Folder found in workFolder without import-job.json.");
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
        final Collection<File> files = Util.listFiles(folder, null, true);
        for (File file : files) {
            if (file.getName().endsWith(DcmRcvManager.DICOM_FILE_SUFFIX)) {
                filesToTransfer.add(file);
            }
        }
        if (importJobManager == null) {
            LOG.error("importJobManager is null.");
            return;
        }
        final UploadState uploadState = importJob.getUploadState();
        if (uploadState.equals(UploadState.START) || uploadState.equals(UploadState.START_AUTOIMPORT)) {
            long startTime = System.currentTimeMillis();
            processStartForServer(folder, filesToTransfer, importJob, importJobManager,
                    currentNominativeDataController);
            long elapsedTime = System.currentTimeMillis() - startTime;
            LOG.info("Upload of files in folder: " + folder.getAbsolutePath() + " finished in duration (ms): "
                    + elapsedTime);
        } else if (uploadState.equals(UploadState.SERVER_PROCESSING)) {
            processServerProcessingForServer(folder, filesToTransfer, importJob, importJobManager,
                    currentNominativeDataController);
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
            LOG.info("Upload: tempDirId for import: " + tempDirId);
            int i = 0;
            for (File file : allFiles) {
                i++;
                shanoirUploaderServiceClient.uploadFile(tempDirId, file);
                uploadPercentage = i * 100 / allFiles.size() + " %";
                importJob.setUploadPercentage(uploadPercentage);
                currentNominativeDataController.updateNominativeDataPercentage(folder, uploadPercentage);
                nominativeDataImportJobManager.writeImportJob(importJob);
            }
            LOG.info("Upload: " + allFiles.size() + " uploaded files to tempDirId: " + tempDirId);

            setTempDirIdAndStartImport(tempDirId, importJob);

            // Server (ms-import) still has to pseudonymize/create datasets and hand
            // off to ms-datasets. Don't mark FINISHED yet — switch state and let
            // the next scheduled tick(s) poll the server instead of blocking here.
            currentNominativeDataController.updateNominativeDataPercentage(folder,
                    UploadState.SERVER_PROCESSING.toString());
            importJob.setUploadState(UploadState.SERVER_PROCESSING);
            importJob.setTimestamp(System.currentTimeMillis());
            nominativeDataImportJobManager.writeImportJob(importJob);
        } catch (Exception e) {
            currentNominativeDataController.updateNominativeDataPercentage(folder, UploadState.ERROR.toString());
            importJob.setUploadState(UploadState.ERROR);
            importJob.setTimestamp(System.currentTimeMillis());
            nominativeDataImportJobManager.writeImportJob(importJob);
            LOG.error("An error occurred during upload to server: " + e.getMessage());
        }
    }

    private void processServerProcessingForServer(final File folder, final List<File> allFiles,
            final ImportJob importJob, final NominativeDataImportJobManager nominativeDataImportJobManager,
            final CurrentNominativeDataController currentNominativeDataController) {
        String tempDirId = importJob.getWorkFolder(); // set to tempDirId in setTempDirIdAndStartImport
        try {
            ImportJobStatus status = shanoirUploaderServiceClient.getImportJobStatus(tempDirId);
            if (status == null) {
                LOG.debug("No status yet on server for tempDirId {}, will retry in 5s.", tempDirId);
                return;
            }
            switch (status.getState()) {
                case FINISHED:
                    LOG.info("Import finished on server for tempDirId {} (folder {}).", tempDirId, folder.getName());
                    currentNominativeDataController.updateNominativeDataPercentage(folder,
                            UploadState.FINISHED.toString());
                    importJob.setUploadState(UploadState.FINISHED);
                    importJob.setTimestamp(System.currentTimeMillis());
                    nominativeDataImportJobManager.writeImportJob(importJob);

                    String value = ShUpConfig.basicProperties.getProperty(ShUpConfig.CHECK_ON_SERVER);
                    if (!Boolean.parseBoolean(value)) {
                        deleteAllDicomFiles(folder, allFiles);
                    }
                    break;
                case ERROR:
                    LOG.error("Import failed on server for tempDirId {} (folder {}): {}",
                            tempDirId, folder.getName(), status.getMessage());
                    currentNominativeDataController.updateNominativeDataPercentage(folder,
                            UploadState.ERROR.toString());
                    importJob.setUploadState(UploadState.ERROR);
                    importJob.setTimestamp(System.currentTimeMillis());
                    nominativeDataImportJobManager.writeImportJob(importJob);
                    break;
                case IN_PROGRESS:
                default:
                    LOG.debug("Import still in progress on server for tempDirId {}: {}", tempDirId,
                            status.getMessage());
                    break;
            }
        } catch (Exception e) {
            // transient (network/server) error: stay SERVER_PROCESSING, retry next tick
            LOG.warn("Could not poll import status for tempDirId {}: {}", tempDirId, e.getMessage());
        }
    }

    private void setTempDirIdAndStartImport(String tempDirId, ImportJobBase importJob)
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
        LOG.info("All DICOM files deleted after successful upload to server.");
    }

}

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
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;

import org.apache.commons.io.FileUtils;
import org.shanoir.ng.importer.model.ImportJobBase;
import org.shanoir.ng.importer.model.ImportJobStatus;
import org.shanoir.ng.importer.model.UploadState;
import org.shanoir.uploader.ShUpConfig;
import org.shanoir.uploader.dicom.retrieve.DcmRcvManager;
import org.shanoir.uploader.nominativeData.CurrentNominativeDataController;
import org.shanoir.uploader.nominativeData.NominativeDataImportJobManager;
import org.shanoir.uploader.service.rest.ShanoirUploaderServiceClient;
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

    /** Number of files uploaded to the server concurrently, per folder being processed. */
    private static final int UPLOAD_PARALLELISM = 4;
    
    @Autowired
    private ShanoirUploaderServiceClient shanoirUploaderServiceClient;

    @Autowired
    private CurrentNominativeDataController currentNominativeDataController;

    @Scheduled(fixedRate = 5000)
    public void execute() throws Exception {
        if (LOCK.tryLock()) {
            try {
                LOG.debug("UploadServiceJob started...");
                File workFolder = new File(ShUpConfig.shanoirUploaderFolder.getAbsolutePath() + File.separator + ShUpConfig.WORK_FOLDER);
                processWorkFolder(workFolder, currentNominativeDataController);
            } finally {
                LOCK.unlock();
                LOG.debug("UploadServiceJob ended...");
            }
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
                final ImportJobBase importJob = importJobManager.readImportJob();
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
            final ImportJobBase importJob, CurrentNominativeDataController currentNominativeDataController) {
        Collection<File> filesToTransfer = Util.listFiles(
                folder,
                (dir, name) -> name.endsWith(DcmRcvManager.DICOM_FILE_SUFFIX),
                true
        );
        if (importJobManager == null) {
            LOG.error("importJobManager is null.");
            return;
        }
        final UploadState uploadState = importJob.getUploadState();
        if (uploadState.equals(UploadState.START_IMPORT_JOB)) {
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
    private void processStartForServer(final File folder, final Collection<File> allFiles,
            final ImportJobBase importJob, NominativeDataImportJobManager nominativeDataImportJobManager,
            CurrentNominativeDataController currentNominativeDataController) {
        try {
            String tempDirId = shanoirUploaderServiceClient.createTempDir();
            LOG.info("Upload: tempDirId for import: " + tempDirId);

            uploadFilesInParallel(folder, allFiles, tempDirId, importJob, nominativeDataImportJobManager,
                    currentNominativeDataController);

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
            LOG.error(e.getMessage(), e);
        }
    }

    /**
     * Uploads all files of one folder to the server concurrently, using a
     * small bounded pool. Fails fast: if any single file upload throws, the
     * remaining not-yet-started uploads are cancelled and the exception is
     * propagated to the caller, matching the previous sequential behavior.
     */
    private void uploadFilesInParallel(final File folder, final Collection<File> allFiles, final String tempDirId,
            final ImportJobBase importJob, final NominativeDataImportJobManager nominativeDataImportJobManager,
            final CurrentNominativeDataController currentNominativeDataController) throws Exception {

        importJob.setUploadState(UploadState.UPLOADING_IMAGES);

        final int total = allFiles.size();
        final AtomicInteger completedCount = new AtomicInteger(0);
        // Guards importJob mutation + the write-to-disk of the progress file,
        // since multiple upload threads finish concurrently and both touch
        // the same ImportJob instance / import-job.json file.
        final Object progressLock = new Object();

        ExecutorService uploadExecutor = Executors.newFixedThreadPool(
                Math.min(UPLOAD_PARALLELISM, Math.max(1, total)));
        try {
            List<Future<Void>> futures = allFiles.stream()
                    .map(file -> (Callable<Void>) () -> {
                        shanoirUploaderServiceClient.uploadFile(tempDirId, file);
                        int done = completedCount.incrementAndGet();
                        synchronized (progressLock) {
                            String percentage = (done * 100 / total) + " %";
                            importJob.setUploadPercentage(percentage);
                            currentNominativeDataController.updateNominativeDataPercentage(folder, percentage);
                            nominativeDataImportJobManager.writeImportJob(importJob);
                        }
                        return null;
                    })
                    .map(uploadExecutor::submit)
                    .toList();

            // Wait for all uploads; on the first failure, cancel the rest
            // and re-throw so processStartForServer's catch block marks
            // the folder ERROR, exactly as the sequential version did.
            Exception firstFailure = null;
            for (Future<Void> future : futures) {
                try {
                    future.get();
                } catch (ExecutionException e) {
                    if (firstFailure == null) {
                        firstFailure = (e.getCause() instanceof Exception cause) ? cause : e;
                        // Cancel remaining/queued uploads; already-running
                        // ones will finish but their results are ignored.
                        futures.forEach(f -> f.cancel(true));
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    throw e;
                }
            }
            if (firstFailure != null) {
                throw firstFailure;
            }
        } finally {
            uploadExecutor.shutdown();
        }
    }

    private void processServerProcessingForServer(final File folder, final Collection<File> allFiles,
            final ImportJobBase importJob, final NominativeDataImportJobManager nominativeDataImportJobManager,
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
        shanoirUploaderServiceClient.startImportJob(tempDirId, importJobJson);
    }

    private void deleteAllDicomFiles(File importJobFolder, Collection<File> files) {
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
        LOG.info("All DICOM files deleted after upload to server.");
    }

}

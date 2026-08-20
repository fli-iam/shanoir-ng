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
import org.dcm4che3.data.Attributes;
import org.dcm4che3.data.Tag;
import org.dcm4che3.io.DicomInputStream;
import org.shanoir.ng.dicom.web.StudyInstanceUIDAndSubjectNameHandler;
import org.shanoir.ng.importer.model.ImportJobBase;
import org.shanoir.ng.importer.model.ImportJobStatus;
import org.shanoir.ng.importer.model.UploadState;
import org.shanoir.uploader.ShUpConfig;
import org.shanoir.uploader.check.DicomInstanceConsistencyChecker;
import org.shanoir.uploader.dicom.retrieve.DcmRcvManager;
import org.shanoir.uploader.model.rest.DatasetsImportStatus;
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
                LOG.debug("Job tick started.");
                File workFolder = new File(ShUpConfig.shanoirUploaderFolder.getAbsolutePath() + File.separator + ShUpConfig.WORK_FOLDER);
                processWorkFolder(workFolder, currentNominativeDataController);
            } finally {
                LOCK.unlock();
                LOG.debug("Job tick ended.");
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
        LOG.debug("{} folder(s) found in work folder.", folders.size());
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
                LOG.warn("[{}] Missing import-job.json.", folder.getName());
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
            LOG.error("[{}] importJobManager is null.", folder.getName());
            return;
        }
        final UploadState uploadState = importJob.getUploadState();
        if (uploadState.equals(UploadState.START_IMPORT_JOB)) {
            processStartForServer(folder, filesToTransfer, importJob, importJobManager,
                    currentNominativeDataController);
            currentNominativeDataController.setNominativeDataSubjectName(folder, importJob.getSubjectName());
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
        String id = null;
        try {
            id = shanoirUploaderServiceClient.createTempDir();
            LOG.info("[{}] Uploading {} file(s) from {}", id, allFiles.size(), folder.getAbsolutePath());
            long startTime = System.currentTimeMillis();
            final String tempDirId = id;
            uploadFilesInParallel(shanoirUploaderServiceClient, allFiles, tempDirId, importJob,
                    percentage -> {
                        importJob.setUploadPercentage(percentage);
                        currentNominativeDataController.updateNominativeDataPercentage(folder, percentage);
                        nominativeDataImportJobManager.writeImportJob(importJob);
                    });
            currentNominativeDataController.setNominativeDataSubjectName(folder, importJob.getSubjectName());
            long elapsedTime = System.currentTimeMillis() - startTime;
            LOG.info("[{}] Upload finished in {} ms.", id, elapsedTime);
            setImportJobIdAndStartImport(shanoirUploaderServiceClient, tempDirId, importJob);
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
            LOG.error("[{}] Upload failed: {}", id != null ? id : folder.getName(), e.getMessage(), e);
        }
    }

    /**
     * Progress callback invoked (under lock) after each successfully uploaded file, with the
     * running "N %" completion string for the whole batch.
     */
    @FunctionalInterface
    public interface UploadProgressListener {
        void onProgress(String percentage);
    }

    /**
     * Uploads all files into one folder on the server concurrently, using a small bounded pool.
     *
     * Fails fast: if any single file upload throws, the remaining not-yet-started uploads are
     * cancelled and the exception is propagated to the caller.
     */
    public static void uploadFilesInParallel(final ShanoirUploaderServiceClient client,
            final Collection<File> allFiles, final String tempDirId, final ImportJobBase importJob,
            final UploadProgressListener progressListener) throws Exception {
        final int total = allFiles.size();
        final AtomicInteger completedCount = new AtomicInteger(0);
        final Object progressLock = new Object();
        ExecutorService uploadExecutor = Executors.newFixedThreadPool(
                Math.min(UPLOAD_PARALLELISM, Math.max(1, total)));
        try {
            List<Future<Void>> futures = allFiles.stream()
                    .map(file -> (Callable<Void>) () -> {
                        String seriesInstanceUID = null;
                        try (DicomInputStream dIS = new DicomInputStream(file)) {
                            Attributes attributes = dIS.readDatasetUntilPixelData();
                            seriesInstanceUID = attributes.getString(Tag.SeriesInstanceUID);
                        }
                        client.uploadFile(tempDirId, seriesInstanceUID, file);
                        int done = completedCount.incrementAndGet();
                        synchronized (progressLock) {
                            progressListener.onProgress((done * 100 / total) + " %");
                        }
                        return null;
                    })
                    .map(uploadExecutor::submit)
                    .toList();
            Exception firstFailure = null;
            for (Future<Void> future : futures) {
                try {
                    future.get();
                } catch (ExecutionException e) {
                    if (firstFailure == null) {
                        firstFailure = (e.getCause() instanceof Exception cause) ? cause : e;
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

    /**
     * Finalizes an ImportJob after upload (assigns the server-side work-folder id) and starts the
     * import on the server. Extracted {@code static} + client-parameterized for the same
     * testability reason as {@link #uploadFilesInParallel}.
     */
    public static void setImportJobIdAndStartImport(final ShanoirUploaderServiceClient client, String id,
            ImportJobBase importJob) throws IOException, JsonParseException, JsonMappingException,
            JsonProcessingException, Exception {
        importJob.setWorkFolder(id);
        importJob.setId(id);
        String importJobJson = Util.objectWriter.writeValueAsString(importJob);
        client.startImportJob(id, importJobJson);
    }

    /**
     * Polls the server side of the import while in {@link UploadState#SERVER_PROCESSING}.
     * This state actually covers two sequential server-side phases that we do NOT
     * expose as separate {@link UploadState} values (to keep the state machine and
     * the GUI simple):
     * <ol>
     *   <li>ms-import: pseudonymizes and hands the job off to ms-datasets
     *       ({@link ShanoirUploaderServiceClient#getImportJobStatus(String)}).</li>
     *   <li>ms-datasets: actually creates the datasets
     *       ({@link ShanoirUploaderServiceClient#findImportStatusByExaminationId(Long)}).</li>
     * </ol>
     * The import is only declared {@link UploadState#FINISHED} once BOTH phases
     * report FINISHED. Re-checking ms-import on every tick (even after it already
     * reported FINISHED) is intentional and cheap: it keeps this method stateless
     * with respect to "which phase are we in", at the cost of one extra GET per
     * 5s tick once ms-import is done.
     */
    private void processServerProcessingForServer(final File folder, final Collection<File> allFiles,
            final ImportJobBase importJob, final NominativeDataImportJobManager nominativeDataImportJobManager,
            final CurrentNominativeDataController currentNominativeDataController) {
        String tempDirId = importJob.getWorkFolder(); // set to tempDirId in setTempDirIdAndStartImport
        try {
            ImportJobStatus msImportStatus = shanoirUploaderServiceClient.getImportJobStatus(tempDirId);
            if (msImportStatus == null) {
                LOG.debug("[{}] No ms-import status yet, retrying in 5s.", tempDirId);
                return;
            }
            switch (msImportStatus.getState()) {
                case FINISHED:
                    // ms-import is done and has handed off to ms-datasets: the import
                    // is NOT finished yet, ms-datasets still has to create the datasets.
                    LOG.debug("[{}] ms-import finished, checking ms-datasets.", tempDirId);
                    processMsDatasetsStatus(folder, allFiles, importJob, nominativeDataImportJobManager,
                            currentNominativeDataController);
                    break;
                case ERROR:
                    LOG.error("[{}] ms-import failed: {}", tempDirId, msImportStatus.getMessage());
                    markError(folder, importJob, nominativeDataImportJobManager, currentNominativeDataController);
                    break;
                case IN_PROGRESS:
                default:
                    LOG.debug("[{}] ms-import in progress: {}", tempDirId, msImportStatus.getMessage());
                    break;
            }
        } catch (Exception e) {
            // transient (network/server) error: stay SERVER_PROCESSING, retry next tick
            LOG.warn("[{}] Could not poll ms-import status: {}", tempDirId, e.getMessage());
        }
    }

    /**
     * Second half of the {@link UploadState#SERVER_PROCESSING} phase: once ms-import
     * reports FINISHED, poll ms-datasets by examinationId until it reports FINISHED
     * (dataset creation actually done) or ERROR.
     */
    private void processMsDatasetsStatus(final File folder, final Collection<File> allFiles,
            final ImportJobBase importJob, final NominativeDataImportJobManager nominativeDataImportJobManager,
            final CurrentNominativeDataController currentNominativeDataController) {
        String id = importJob.getId();
        Long examinationId = importJob.getExaminationId();
        if (examinationId == null) {
            LOG.warn("[{}] ms-import finished but no examinationId yet, retrying.", id);
            return;
        }
        try {
            DatasetsImportStatus datasetsStatus =
                    shanoirUploaderServiceClient.findImportStatusByExaminationId(examinationId);
            if (datasetsStatus == null) {
                LOG.debug("[{}] No ms-datasets status yet, retrying in 5s.", id);
                return;
            }
            switch (datasetsStatus.getState()) {
                case FINISHED:
                    LOG.info("[{}] ms-datasets finished.", id);
                    if (checkInstancesMetadata(folder, importJob)) {
                        markFinished(folder, allFiles, importJob, nominativeDataImportJobManager,
                                currentNominativeDataController);
                    } else {
                        markError(folder, importJob, nominativeDataImportJobManager, currentNominativeDataController);
                    }
                    break;
                case ERROR:
                    LOG.error("[{}] ms-datasets failed: {}", id, datasetsStatus.getMessage());
                    markError(folder, importJob, nominativeDataImportJobManager, currentNominativeDataController);
                    break;
                case IN_PROGRESS:
                default:
                    LOG.debug("[{}] ms-datasets in progress: {}", id, datasetsStatus.getMessage());
                    break;
            }
        } catch (Exception e) {
            // transient (network/server) error: stay SERVER_PROCESSING, retry next tick
            LOG.warn("[{}] Could not poll ms-datasets status: {}", id, e.getMessage());
        }
    }

    private void markFinished(final File folder, final Collection<File> allFiles, final ImportJobBase importJob,
            final NominativeDataImportJobManager nominativeDataImportJobManager,
            final CurrentNominativeDataController currentNominativeDataController) {
        currentNominativeDataController.updateNominativeDataPercentage(folder, UploadState.FINISHED.toString());
        importJob.setUploadState(UploadState.FINISHED);
        importJob.setTimestamp(System.currentTimeMillis());
        nominativeDataImportJobManager.writeImportJob(importJob);
        String value = ShUpConfig.basicProperties.getProperty(ShUpConfig.CHECK_ON_SERVER);
        if (!Boolean.parseBoolean(value)) {
            deleteAllDicomFiles(importJob.getId(), folder, allFiles);
        }
    }

    private void markError(final File folder, final ImportJobBase importJob,
            final NominativeDataImportJobManager nominativeDataImportJobManager,
            final CurrentNominativeDataController currentNominativeDataController) {
        currentNominativeDataController.updateNominativeDataPercentage(folder, UploadState.ERROR.toString());
        importJob.setUploadState(UploadState.ERROR);
        importJob.setTimestamp(System.currentTimeMillis());
        nominativeDataImportJobManager.writeImportJob(importJob);
    }

    /**
     * Rapid, metadata-only sanity check, run by default for every import
     * (unlike the heavy, opt-in check.on.server pixel-by-pixel comparison
     * done later by ExaminationConsistencyServiceJob): pings the server's
     * DICOMWeb metadata endpoint for every instance of the folder, in
     * parallel, to catch a partially or badly imported examination right
     * away — before it's marked FINISHED and its local DICOM files are
     * possibly deleted.
     */
    private boolean checkInstancesMetadata(final File folder, final ImportJobBase importJob) {
        String id = importJob.getId();
        long startTime = System.currentTimeMillis();
        try {
            DicomInstanceConsistencyChecker checker = new DicomInstanceConsistencyChecker(shanoirUploaderServiceClient);
            String examinationUID = StudyInstanceUIDAndSubjectNameHandler.PREFIX + importJob.getExaminationId();
            boolean checked = checker.checkImportJobMetadataOnSeries(importJob, examinationUID);
            long elapsedTime = System.currentTimeMillis() - startTime;
            LOG.info("[{}] Checked {} serie(s) in {} ms.", id, checked, elapsedTime);
            return checked;
        } catch (Exception e) {
            long elapsedTime = System.currentTimeMillis() - startTime;
            LOG.error("[{}] Metadata check failed after {} ms: {}", id, elapsedTime, e.getMessage(), e);
            return false;
        }
    }

    private void deleteAllDicomFiles(String id, File importJobFolder, Collection<File> files) {
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
        LOG.info("[{}] Deleted {} local DICOM file(s).", id, files.size());
    }

}
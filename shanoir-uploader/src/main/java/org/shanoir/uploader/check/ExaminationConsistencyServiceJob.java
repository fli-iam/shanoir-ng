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

package org.shanoir.uploader.check;

import java.io.File;
import java.util.Iterator;
import java.util.List;

import org.shanoir.ng.dicom.web.StudyInstanceUIDAndSubjectNameHandler;
import org.shanoir.ng.importer.model.ImportJobBase;
import org.shanoir.ng.importer.model.UploadState;
import org.shanoir.uploader.ShUpConfig;
import org.shanoir.uploader.nominativeData.CurrentNominativeDataController;
import org.shanoir.uploader.nominativeData.NominativeDataImportJobManager;
import org.shanoir.uploader.service.rest.ShanoirUploaderServiceClient;
import org.shanoir.uploader.upload.UploadServiceJob;
import org.shanoir.uploader.utils.Util;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
public class ExaminationConsistencyServiceJob {

    private static final Logger LOG = LoggerFactory.getLogger(ExaminationConsistencyServiceJob.class);

    private static final long THIRTY_MIN_IN_MILLIS = 30 * 60 * 1000;

    private static final long ONE_HOUR_IN_MILLIS = 60 * 60 * 1000;

    @Autowired
    private CurrentNominativeDataController currentNominativeDataController;

    @Autowired
    private ShanoirUploaderServiceClient shanoirUploaderServiceClient;

    @Scheduled(fixedRate = THIRTY_MIN_IN_MILLIS)
    public void execute() throws Exception {
        String value = ShUpConfig.basicProperties.getProperty(ShUpConfig.CHECK_ON_SERVER);
        boolean checkOnServer = Boolean.parseBoolean(value);
        if (checkOnServer) {
            if (!UploadServiceJob.LOCK.isLocked()) {
                LOG.info("ExaminationConsistencyServiceJob started...");
                UploadServiceJob.LOCK.lock();
                File workFolder = new File(
                        ShUpConfig.shanoirUploaderFolder.getAbsolutePath() + File.separator + ShUpConfig.WORK_FOLDER);
                processWorkFolder(workFolder, currentNominativeDataController);
                UploadServiceJob.LOCK.unlock();
                LOG.info("ExaminationConsistencyServiceJob ended...");
            }
        }
    }

    private void processWorkFolder(File workFolder, CurrentNominativeDataController currentNominativeDataController)
            throws Exception {
        final List<File> folders = Util.listFolders(workFolder);
        LOG.debug("Found " + folders.size() + " folders in work folder.");
        DicomInstanceConsistencyChecker checker = new DicomInstanceConsistencyChecker(shanoirUploaderServiceClient);
        for (Iterator<File> foldersIt = folders.iterator(); foldersIt.hasNext();) {
            final File importJobFolder = (File) foldersIt.next();
            final File importJobFile = new File(
                    importJobFolder.getAbsolutePath() + File.separator + ShUpConfig.IMPORT_JOB_JSON);
            // file could be missing in case of downloadOrCopy ongoing
            if (importJobFile.exists()) {
                // if the check.on.server flag has been activated after, do not check on
                // previous
                // already imported folders, as they do not contain any DICOM anymore
                if (importJobFolder.listFiles().length > 1) {
                    NominativeDataImportJobManager importJobManager = new NominativeDataImportJobManager(importJobFile);
                    final ImportJobBase importJob = importJobManager.readImportJob();
                    final org.shanoir.ng.importer.model.UploadState uploadState = importJob.getUploadState();
                    if (uploadState.equals(org.shanoir.ng.importer.model.UploadState.FINISHED)) {
                        long timestamp = importJob.getTimestamp();
                        long currentTime = System.currentTimeMillis();
                        if (currentTime - timestamp >= ONE_HOUR_IN_MILLIS) {
                            String examinationUID = StudyInstanceUIDAndSubjectNameHandler.PREFIX
                                    + importJob.getExaminationId();
                            try {
                                int numberOfInstances = checker.checkImportJob(importJobFolder, examinationUID, true,
                                        true);
                                LOG.info("Examination (subject: " + importJob.getSubjectName()
                                        + ", studyDate: " + importJob.getStudy().getStudyDate()
                                        + ") checked for consistency of " + numberOfInstances
                                        + " DICOM instances (images)");
                                importJob.setUploadState(UploadState.CHECK_OK);
                                importJobManager.writeImportJob(importJob);
                                currentNominativeDataController.updateNominativeDataPercentage(importJobFolder,
                                        UploadState.CHECK_OK.toString());
                            } catch (Exception e) {
                                importJob.setUploadState(UploadState.CHECK_KO);
                                importJobManager.writeImportJob(importJob);
                                currentNominativeDataController.updateNominativeDataPercentage(importJobFolder,
                                        UploadState.CHECK_KO.toString());
                                LOG.error(e.getMessage(), e);
                            }
                        }
                    }
                } // do nothing, keep already imported untouched
            } else {
                LOG.error("Folder found in workFolder without import-job.json.");
            }
        }
    }

}
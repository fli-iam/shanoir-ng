package org.shanoir.uploader.action;

import java.io.File;
import java.io.IOException;

import org.shanoir.ng.importer.model.ImportJobBase;
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
 */
public class ImportFinishRunnable implements Runnable {

    private static final Logger logger = LoggerFactory.getLogger(ImportFinishRunnable.class);

    private final File uploadFolder;

    private final ImportJobBase importJob;
    
    private final String subjectName;
    
    private final Anonymizer anonymizer = new Anonymizer();

    /** Invoked exactly once, on whatever thread this Runnable finishes on,
     *  whether anonymization succeeded or failed. Used by the caller to
     *  release its per-folder in-progress guard and restore UI state. */
    private final Runnable onDone;

    public ImportFinishRunnable(final File uploadFolder, final ImportJobBase importJob, final String subjectName) {
        this(uploadFolder, importJob, subjectName, null);
    }

    public ImportFinishRunnable(final File uploadFolder, final ImportJobBase importJob, final String subjectName,
            final Runnable onDone) {
        this.uploadFolder = uploadFolder;
        this.importJob = importJob;
        this.subjectName = subjectName;
        this.onDone = onDone;
    }

    public void run() {
        try {
            boolean anonymizationSuccess = false;
            try {
                String anonymizationProfile = ShUpConfig.profileProperties.getProperty(ShUpConfig.ANONYMIZATION_PROFILE);
                anonymizationSuccess = anonymizer.pseudonymize(uploadFolder, anonymizationProfile, subjectName, importJob.getStudyInstanceUID());
            } catch (IOException e) {
                logger.error(uploadFolder.getName() + ": " + e.getMessage(), e);
            }

            if (anonymizationSuccess) {
                try {
                    File importJobJson = new File(uploadFolder, ShUpConfig.IMPORT_JOB_JSON);
                    importJobJson.createNewFile();
                    Util.mapper.writeValue(importJobJson, importJob);
                } catch (IOException e) {
                    logger.error(uploadFolder.getName() + ": " + e.getMessage(), e);
                }

                importJob.setUploadState(UploadState.START_AUTOIMPORT);
                NominativeDataImportJobManager importJobManager = new NominativeDataImportJobManager(uploadFolder.getAbsolutePath());
                importJobManager.writeImportJob(importJob);
                logger.info(uploadFolder.getName() + " scheduled for upload.");
            } else {
                logger.error(uploadFolder.getName() + ": Error during anonymization.");
            }
        } finally {
            // Always release the guard/UI state, even on unexpected exceptions.
            if (onDone != null) {
                onDone.run();
            }
        }
    }

}

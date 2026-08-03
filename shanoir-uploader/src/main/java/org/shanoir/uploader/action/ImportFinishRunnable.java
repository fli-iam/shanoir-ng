package org.shanoir.uploader.action;

import java.io.File;
import java.io.IOException;

import org.shanoir.ng.importer.model.ImportJobBase;
import org.shanoir.ng.importer.model.UploadState;
import org.shanoir.uploader.ShUpConfig;
import org.shanoir.uploader.dicom.anonymize.Anonymizer;
import org.shanoir.uploader.nominativeData.NominativeDataImportJobManager;
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
    
    private final Anonymizer anonymizer = new Anonymizer();

    /** Invoked exactly once, on whatever thread this Runnable finishes on,
     *  whether anonymization succeeded or failed. Used by the caller to
     *  release its per-folder in-progress guard and restore UI state. */
    private final Runnable onDone;

    public ImportFinishRunnable(final File uploadFolder, final ImportJobBase importJob) {
        this(uploadFolder, importJob, null);
    }

    public ImportFinishRunnable(final File uploadFolder, final ImportJobBase importJob,
            final Runnable onDone) {
        this.uploadFolder = uploadFolder;
        this.importJob = importJob;
        this.onDone = onDone;
    }

    public void run() {
        try {
            boolean anonymizationSuccess = false;
            try {
                String anonymizationProfile = ShUpConfig.profileProperties.getProperty(ShUpConfig.ANONYMIZATION_PROFILE);
                anonymizationSuccess = anonymizer.pseudonymize(uploadFolder, anonymizationProfile, importJob.getSubjectName(), importJob.getStudyInstanceUID());
            } catch (IOException e) {
                logger.error(uploadFolder.getName() + ": " + e.getMessage(), e);
            }

            if (anonymizationSuccess) {
                try {
                    importJob.setUploadState(UploadState.START_IMPORT_JOB);
                    NominativeDataImportJobManager importJobManager = new NominativeDataImportJobManager(uploadFolder.getAbsolutePath());
                    importJobManager.writeImportJob(importJob);
                } catch (Exception e) {
                    logger.error(uploadFolder.getName() + ": " + e.getMessage(), e);
                }
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

package org.shanoir.uploader.action;

import java.io.File;
import java.io.IOException;

import org.shanoir.anonymization.anonymization.AnonymizationResult;
import org.shanoir.ng.importer.model.ImportJobBase;
import org.shanoir.ng.importer.model.UploadState;
import org.shanoir.ng.utils.ImportUtils;
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

    private final File importJobFolder;

    private final ImportJobBase importJob;

    private final Anonymizer anonymizer = new Anonymizer();

    /** Invoked exactly once, on whatever thread this Runnable finishes on,
     *  whether anonymization succeeded or failed. Used by the caller to
     *  release its per-folder in-progress guard and restore UI state. */
    private final Runnable onDone;

    public ImportFinishRunnable(final File importJobFolder, final ImportJobBase importJob) {
        this(importJobFolder, importJob, null);
    }

    public ImportFinishRunnable(final File importJobFolder, final ImportJobBase importJob,
            final Runnable onDone) {
        this.importJobFolder = importJobFolder;
        this.importJob = importJob;
        this.onDone = onDone;
    }

    public void run() {
        try {
            AnonymizationResult anonymizationResult = null;
            try {
                String anonymizationProfile = ShUpConfig.profileProperties.getProperty(ShUpConfig.ANONYMIZATION_PROFILE);
                anonymizationResult = anonymizer.pseudonymize(importJobFolder, anonymizationProfile,
                        importJob.getSubjectName(), importJob.getStudyInstanceUID());
            } catch (IOException e) {
                logger.error(importJobFolder.getName() + ": " + e.getMessage(), e);
            }
            if (anonymizationResult != null) {
                try {
                    ImportUtils.updateImportJobWithPseudonymizedUIDs(importJob, importJobFolder, anonymizationResult);
                    importJob.setUploadState(UploadState.START_IMPORT_JOB);
                    NominativeDataImportJobManager importJobManager =
                            new NominativeDataImportJobManager(importJobFolder.getAbsolutePath());
                    importJobManager.writeImportJob(importJob);
                } catch (Exception e) {
                    logger.error(importJobFolder.getName() + ": " + e.getMessage(), e);
                }
                logger.info(importJobFolder.getName() + " scheduled for upload.");
            } else {
                logger.error(importJobFolder.getName() + ": Error during anonymization.");
            }
        } finally {
            if (onDone != null) {
                onDone.run();
            }
        }
    }

}

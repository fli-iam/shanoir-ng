package org.shanoir.uploader.action;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import org.shanoir.anonymization.anonymization.AnonymizationResult;
import org.shanoir.ng.importer.model.ImportJobBase;
import org.shanoir.ng.importer.model.Instance;
import org.shanoir.ng.importer.model.Serie;
import org.shanoir.ng.importer.model.Study;
import org.shanoir.ng.importer.model.UploadState;
import org.shanoir.ng.shared.dicom.DicomUtils;
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
            AnonymizationResult anonymizationResult = null;
            try {
                String anonymizationProfile = ShUpConfig.profileProperties.getProperty(ShUpConfig.ANONYMIZATION_PROFILE);
                anonymizationResult = anonymizer.pseudonymize(uploadFolder, anonymizationProfile,
                        importJob.getSubjectName(), importJob.getStudyInstanceUID());
            } catch (IOException e) {
                logger.error(uploadFolder.getName() + ": " + e.getMessage(), e);
            }
            if (anonymizationResult != null) {
                try {
                    updateImportJobWithPseudonymizedUIDs(importJob, uploadFolder, anonymizationResult);
                    importJob.setUploadState(UploadState.START_IMPORT_JOB);
                    NominativeDataImportJobManager importJobManager =
                            new NominativeDataImportJobManager(uploadFolder.getAbsolutePath());
                    importJobManager.writeImportJob(importJob);
                } catch (Exception e) {
                    logger.error(uploadFolder.getName() + ": " + e.getMessage(), e);
                }
                logger.info(uploadFolder.getName() + " scheduled for upload.");
            } else {
                logger.error(uploadFolder.getName() + ": Error during anonymization.");
            }
        } finally {
            if (onDone != null) {
                onDone.run();
            }
        }
    }

    /**
     * Pseudonymization rewrites StudyInstanceUID, SeriesInstanceUID and
     * SOPInstanceUID directly inside the DICOM files on disk, in place -- but
     * importJob's own Patient/Study/Serie/Instance tree was built from those
     * files BEFORE that rewrite, from the original, vendor-assigned UIDs.
     * Left untouched, the importJob written to import-job.json (sent as-is
     * to ms-import, and later relied on e.g. by UploadServiceJob's
     * post-import metadata check) would reference UIDs that no longer exist
     * in the actual DICOM data. Patch the tree here, right after
     * anonymization and before the state switch to START_IMPORT_JOB, so
     * import-job.json stays consistent with what's really on disk from this
     * point on.
     *
     * SeriesInstanceUID/StudyInstanceUID are looked up by their OLD value in
     * the shared old->new maps produced by anonymization. SOPInstanceUID has
     * no such shared map (it's generated independently per file), so
     * instances are instead correlated to their file via
     * {@link Instance#getReferencedFileID()}.
     * @throws FileNotFoundException 
     */
    private void updateImportJobWithPseudonymizedUIDs(final ImportJobBase importJob, final File uploadFolder,
            final AnonymizationResult anonymizationResult) throws FileNotFoundException {
        if (importJob.getPatient() == null) {
            return;
        }
        int updatedInstances = 0;
        int missingInstances = 0;
        Study study = importJob.getStudy();
        String newStudyUID = anonymizationResult.getStudyInstanceUIDs().get(study.getStudyInstanceUID());
        if (newStudyUID != null) {
            study.setStudyInstanceUID(newStudyUID);
        }
        if (importJob.getSeries() == null) {
            return;
        }
        for (Serie serie : importJob.getSeries()) {
            String newSeriesUID = anonymizationResult.getSeriesInstanceUIDs().get(serie.getSeriesInstanceUID());
            if (newSeriesUID != null) {
                serie.setSeriesInstanceUID(newSeriesUID);
            }
            if (serie.getInstances() == null) {
                continue;
            }
            for (Instance instance : serie.getInstances()) {
                String filePath = DicomUtils.referencedFileIDToPath(
                        uploadFolder.getAbsolutePath(), instance.getReferencedFileID());
                String newSopInstanceUID = anonymizationResult.getSopInstanceUIDsByFilePath().get(filePath);
                if (newSopInstanceUID != null) {
                    instance.setSopInstanceUID(newSopInstanceUID);
                    updatedInstances++;
                } else {
                    missingInstances++;
                    logger.warn("{}: no pseudonymized SOPInstanceUID found for instance file {}; "
                            + "importJob keeps its pre-pseudonymization UID for this instance.",
                            uploadFolder.getName(), filePath);
                }
            }
        }
        logger.info("{}: importJob UIDs updated to their pseudonymized values for {} instance(s){}.",
                uploadFolder.getName(), updatedInstances,
                missingInstances > 0 ? (", " + missingInstances + " instance(s) could not be matched") : "");
    }

}
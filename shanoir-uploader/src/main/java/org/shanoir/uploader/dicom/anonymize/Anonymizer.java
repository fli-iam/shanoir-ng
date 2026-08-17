package org.shanoir.uploader.dicom.anonymize;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import org.shanoir.anonymization.anonymization.AnonymizationResult;
import org.shanoir.anonymization.anonymization.AnonymizationService;
import org.shanoir.anonymization.anonymization.AnonymizationServiceImpl;
import org.shanoir.uploader.dicom.retrieve.DcmRcvManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Anonymizer {

    private static final Logger logger = LoggerFactory.getLogger(Anonymizer.class);

    /**
     * @return the UID rewrites performed (never null on success, so the caller
     *         can patch its own DICOM tree with them), or null if pseudonymization failed.
     */
    public AnonymizationResult pseudonymize(final File uploadFolder,
            final String profile, final String subjectName, final String studyInstanceUID)
            throws IOException {
        ArrayList<File> dicomFiles = new ArrayList<File>();
        getListOfDicomFiles(uploadFolder, dicomFiles);
        try {
            AnonymizationService anonymizationService = new AnonymizationServiceImpl();
            AnonymizationResult result = anonymizationService.anonymizeForShanoir(
                    dicomFiles, profile, subjectName, subjectName, studyInstanceUID);
            logger.info("--> " + dicomFiles.size() + " DICOM files pseudonymized.");
            return result;
        } catch (Exception e) {
            logger.error("pseudonymization service: ", e);
            return null;
        }
    }

    private void getListOfDicomFiles(final File folder, ArrayList<File> dicomFiles) throws IOException {
        File[] listOfFiles = folder.listFiles();
        for (File file : listOfFiles) {
            if (file.isFile() && file.getName().endsWith(DcmRcvManager.DICOM_FILE_SUFFIX)) {
                dicomFiles.add(file);
            } else {
                if (file.isDirectory()) {
                    getListOfDicomFiles(file, dicomFiles);
                }
            }
        }
    }

}
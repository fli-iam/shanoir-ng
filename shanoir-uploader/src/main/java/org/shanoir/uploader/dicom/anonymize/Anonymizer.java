package org.shanoir.uploader.dicom.anonymize;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import org.apache.log4j.Logger;
import org.shanoir.anonymization.anonymization.AnonymizationService;
import org.shanoir.anonymization.anonymization.AnonymizationServiceImpl;
import org.shanoir.dicom.importer.UploadJob;

public class Anonymizer {

	private static Logger logger = Logger.getLogger(Anonymizer.class);

	public boolean anonymize(final File uploadFolder,
			final String profile, final UploadJob uploadJob, final String subjectName)
			throws IOException {
		ArrayList<File> dicomImages = createImageArray(uploadFolder);
		try {
			AnonymizationService anonymizationService = new AnonymizationServiceImpl();
			logger.info("subject identifier: " + uploadJob.getSubjectIdentifier());
			anonymizationService.anonymizeForShanoir(dicomImages, profile,
				subjectName, uploadJob.getSubjectIdentifier());
		} catch (Exception e) {
			logger.error("anonymization service: ", e);
			return false;
		}
		return true;
	}

	private ArrayList<File> createImageArray(final File uploadFolder)
			throws IOException {
		ArrayList<File> array = new ArrayList<File>();
		File[] listOfFiles = uploadFolder.listFiles();
		for (File file : listOfFiles) {
			if (file.isFile() && !file.getName().endsWith(".xml")) {
				array.add(file);
			}
		}
		return array;
	}

}

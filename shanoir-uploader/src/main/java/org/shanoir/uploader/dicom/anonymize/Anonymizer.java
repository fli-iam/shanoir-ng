package org.shanoir.uploader.dicom.anonymize;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.shanoir.anonymization.anonymization.AnonymizationService;
import org.shanoir.anonymization.anonymization.AnonymizationServiceImpl;
import org.shanoir.uploader.dicom.retrieve.DcmRcvManager;

public class Anonymizer {

	private static final Logger logger = LoggerFactory.getLogger(Anonymizer.class);

	public boolean pseudonymize(final File uploadFolder,
			final String profile, final String subjectName)
			throws IOException {
		ArrayList<File> dicomFiles = new ArrayList<File>();
		getListOfDicomFiles(uploadFolder, dicomFiles);
		try {
			AnonymizationService anonymizationService = new AnonymizationServiceImpl();
			anonymizationService.anonymizeForShanoir(dicomFiles, profile, subjectName, subjectName);
			logger.info("--> " + dicomFiles.size() + " DICOM files successfully pseudonymized.");
		} catch (Exception e) {
			logger.error("pseudonymization service: ", e);
			return false;
		}
		return true;
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

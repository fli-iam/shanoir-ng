package org.shanoir.uploader.dicom.anonymize;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import org.apache.log4j.Logger;
import org.shanoir.anonymization.anonymization.AnonymizationService;
import org.shanoir.anonymization.anonymization.AnonymizationServiceImpl;

public class Anonymizer {

	private static Logger logger = Logger.getLogger(Anonymizer.class);

	public boolean anonymize(final File uploadFolder,
			final String profile, final String subjectName)
			throws IOException {
		ArrayList<File> dicomFiles = getListOfDicomFiles(uploadFolder);
		try {
			AnonymizationService anonymizationService = new AnonymizationServiceImpl();
			anonymizationService.anonymizeForShanoir(dicomFiles, profile, subjectName, subjectName);
		} catch (Exception e) {
			logger.error("anonymization service: ", e);
			return false;
		}
		return true;
	}

	private ArrayList<File> getListOfDicomFiles(final File uploadFolder)
			throws IOException {
		ArrayList<File> dicomFileList = new ArrayList<File>();
		File[] listOfFiles = uploadFolder.listFiles();
		for (File file : listOfFiles) {
			if (file.isFile() && !file.getName().endsWith(".xml")) {
				dicomFileList.add(file);
			}
		}
		return dicomFileList;
	}

}

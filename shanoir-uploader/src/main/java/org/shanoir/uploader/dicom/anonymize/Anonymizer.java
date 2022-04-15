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

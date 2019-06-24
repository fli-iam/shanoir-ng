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

package org.shanoir.anonymization.anonymization;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Anonymization test.
 * 
 * @author ifakhfakh
 * 
 */
public class AnonymizationTest {

	/**
	 * Logger
	 */
	private static final Logger LOG = LoggerFactory.getLogger(AnonymizationTest.class);

	private static AnonymizationServiceImpl anonymizationService = new AnonymizationServiceImpl();

	private SendToPacs sendToPacs;

	private static final String DICOM_IMAGE_FOLDER_PATH = "C:/Users/ifakhfak/Documents/Agen - Optima (GE)/IMAGES";
	private static final String PROFILE = "MR Profile";
	private static final String LAST_NAME = "lastName";
	private static final String FIRST_NAME = "firstName";
	private static final String ID = "id";

	/**
	 * Test of readAnonymizationFile function that reads the xml file specifying
	 * the anonymization profile
	 * 
	 * @throws ShanoirAnonymizationException
	 */

	public void readAnonymizationFileTest() {
		AnonymizationRulesSingleton.getInstance().getAnonymizationMAP();
//		anonymizationService.readAnonymizationFile(PROFILE);
	}

	/**
	 * Test of the anonymization process
	 * 
	 */

	public static void anonymizationTest() throws IOException {

		ArrayList<File> dicomImages = createImageArray();

		long chrono = java.lang.System.currentTimeMillis();
		anonymizationService.anonymizeForShanoir(dicomImages, PROFILE, FIRST_NAME, LAST_NAME, ID);
		long chrono2 = java.lang.System.currentTimeMillis();
		long temps = chrono2 - chrono;
		LOG.info("Spended time to anonymize file = " + temps + " ms");

	}

	private static ArrayList<File> createImageArray() throws IOException {
		ArrayList<File> array = new ArrayList<File>();
		File folder = new File(DICOM_IMAGE_FOLDER_PATH);
		File[] listOfFiles = folder.listFiles();

		for (File file : listOfFiles) {
			if (file.isFile()) {
				array.add(file);
			}
		}
		return array;
	}

	/**
	 * Test of the function that sends DICOMs to Pacs. To be used to verify that
	 * the anonymised files could be stored in DCM4CHEE PACS Please uncomment
	 * this function after anonymising DICOMS to send them to PACS.
	 * 
	 * TEMPORARY TEST: used for local test of anonymization process.
	 * 
	 * @throws ShanoirImportException
	 */
	/*
	 * @Test public void sendToPACSTest() throws ShanoirImportException {
	 * sendToPacs.processSendToPacs(Folder_TO_SEND_PATH); }
	 */

	public static void main(String args[]) {
		try {
			anonymizationTest();
		} catch (IOException e) {
			LOG.error("anonymization failed: ", e);
		}
	}
}

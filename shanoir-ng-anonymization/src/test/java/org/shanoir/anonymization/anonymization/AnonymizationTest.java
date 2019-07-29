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

import org.dcm4che3.data.Attributes;
import org.dcm4che3.data.Tag;
import org.dcm4che3.io.DicomInputStream;
import org.dcm4che3.io.DicomOutputStream;
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

	private static final String DICOM_IMAGE_FOLDER_PATH = "/Users/mkain/Desktop/sample1/IMAGES";
	private static final String PROFILE = "Profile OFSEP";
	private static final String NAME = "commonName";
	private static final String ID = "id";

	/**
	 * Test of readAnonymizationFile function that reads the xml file specifying
	 * the anonymization profile
	 * 
	 * @throws ShanoirAnonymizationException
	 */

	public void readAnonymizationFileTest() {
		AnonymizationRulesSingleton.getInstance().getProfiles().get(PROFILE).getAnonymizationMap();
//		anonymizationService.readAnonymizationFile(PROFILE);
	}

	/**
	 * Test of the anonymization process
	 * 
	 */

	public static void anonymizationTest() throws Exception {
		ArrayList<File> dicomImages = createImageArray();
		long chrono = java.lang.System.currentTimeMillis();
		printDICOMFile(dicomImages.get(0));
		anonymizationService.anonymizeForShanoir(dicomImages, PROFILE, NAME, ID);
		printDICOMFile(dicomImages.get(0));
		long chrono2 = java.lang.System.currentTimeMillis();
		long temps = chrono2 - chrono;
		System.out.println("Spended time to anonymize file = " + temps + " ms");
	}
	
	private static void printDICOMFile(final File dicomFile) {
		DicomInputStream din = null;
		try {
			din = new DicomInputStream(dicomFile);
			// DICOM "header"/meta-information fields: read tags
			System.out.println("DICOM header/meta-information --------------------------------------------------");
			Attributes metaInformationAttributes = din.readFileMetaInformation();
			for (int tagInt : metaInformationAttributes.tags()) {
				String tagString = String.format("0x%08x", Integer.valueOf(tagInt));
				System.out.println("Tag: " + tagString + ": " + metaInformationAttributes.getString(tagInt));
			}
			System.out.println("DICOM body ---------------------------------------------------------------------");
			Attributes datasetAttributes = din.readDataset(-1, -1);
			// print DICOM file body
			for (int tagInt : datasetAttributes.tags()) {
				String tagString = String.format("0x%08X", Integer.valueOf(tagInt));
				System.out.println("Tag: " + tagString + ": " + datasetAttributes.getString(tagInt));
			}
		} catch (final IOException exc) {
			LOG.error("printDICOMFile : error while printing file " + dicomFile.toString() + " : ", exc);
		} finally {
			try {
				if (din != null) {
					din.close();
				}
			} catch (IOException e) {
				LOG.error(e.getMessage(), e);
			}
		}
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
		} catch (Exception e) {
			LOG.error("anonymization failed: ", e);
		}
	}
}

package org.shanoir.ng.anonymization;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.runners.MockitoJUnitRunner;
import org.shanoir.ng.shared.exception.ShanoirImportException;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

/**
 * Anonymization service test.
 * 
 * @author ifakhfakh
 * 
 */
@RunWith(MockitoJUnitRunner.class)
public class AnonymizationServiceTest {

	@InjectMocks
	private AnonymizationServiceImpl anonymizationService;

	@InjectMocks
	private SendToPacs sendToPacs;

	private static final String DICOM_IMAGE_FOLDER_PATH = "tests/sample/IMAGES";
	private static final String PROFILE = "MR Profile";

	/**
	 * Test of readAnonymizationFile function that reads the xml file specifying
	 * the anonymization profile
	 * 
	 * @throws ShanoirImportException
	 */
	@Test
	public void readAnonymizationFileTest() throws ShanoirImportException {
		anonymizationService.readAnonymizationFile(PROFILE);
	}

	/**
	 * Test of the anonymization process
	 * 
	 * @throws ShanoirImportException
	 */
	@Test
	public void anonymizationTest() throws ShanoirImportException, IOException {
		ArrayList<File> dicomImages = createImageArray();
		long chrono = java.lang.System.currentTimeMillis();
		anonymizationService.anonymize(dicomImages, PROFILE);
		long chrono2 = java.lang.System.currentTimeMillis();
		long temps = chrono2 - chrono;
		System.out.println("Spended time to anonymize file = " + temps + " ms");
	}

	private ArrayList<File> createImageArray() throws IOException {
		ArrayList<File> array = new ArrayList<File>();
		Resource resource = new ClassPathResource(DICOM_IMAGE_FOLDER_PATH);
		
		File folder = resource.getFile();
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

}

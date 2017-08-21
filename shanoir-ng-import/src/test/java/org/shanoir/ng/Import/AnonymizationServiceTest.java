package org.shanoir.ng.Import;


import java.io.File;
import java.util.ArrayList;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.runners.MockitoJUnitRunner;
import org.shanoir.ng.Import.anonymization.AnonymizationServiceImpl;
import org.shanoir.ng.shared.exception.ShanoirImportException;


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
	
	private static final String FOLDER_PATH = "C:/Users/...";
	private static final String IMAGE_PATH = "/IMAGES/IM000001";
	private static final String PROFILE = "Basic Profile";

	
	
	
	@Test
	public void anonymizeTest() throws ShanoirImportException {
		ArrayList<File> dicomImages = createImageArray();
		long chrono = java.lang.System.currentTimeMillis() ; 
		anonymizationService.anonymize(dicomImages, PROFILE);
		long chrono2 = java.lang.System.currentTimeMillis() ; 
		long temps = chrono2 - chrono ; 
		System.out.println("Spended time to anonymize file = " + temps + " ms") ; 
	}
	
	@Test
	public void readAnonymizationFileTest() throws ShanoirImportException {
		
		anonymizationService.readAnonymizationFile(PROFILE);

	}
	
	@Test
	public void performAnonymizationTest() throws ShanoirImportException {
		
		File imagePath = new File(FOLDER_PATH + IMAGE_PATH);
		anonymizationService.performAnonymization(imagePath, PROFILE);

	}

	private ArrayList<File> createImageArray() {
		ArrayList<File> array =  new ArrayList<File>();
		array.add(new File(FOLDER_PATH + IMAGE_PATH));
		return array;
	}




}

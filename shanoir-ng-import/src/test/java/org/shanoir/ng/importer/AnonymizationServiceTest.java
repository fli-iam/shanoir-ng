//package org.shanoir.ng.Import;
//
//
//import java.util.ArrayList;
//import java.util.Date;
//import java.util.List;
//
//import org.junit.Test;
//import org.junit.runner.RunWith;
//import org.mockito.InjectMocks;
//import org.mockito.runners.MockitoJUnitRunner;
//import org.shanoir.ng.Import.Serie;
//import org.shanoir.ng.Import.anonymization.AnonymizationServiceImpl;
//import org.shanoir.ng.Import.dto.ImportSubjectDTO;
//import org.shanoir.ng.shared.exception.ShanoirImportException;
//
//
///**
// * Import service test.
// * 
// * @author ifakhfakh
// * 
// */
//@RunWith(MockitoJUnitRunner.class)
//public class AnonymizationServiceTest {
//
//
//	@InjectMocks
//	private AnonymizationServiceImpl anonymizationService;
//	
//	private static final int START_COUNT=0;
//	private static final String FOLDER_PATH = "C:/Users/ifakhfak/Documents/sampleToAnonymize";
//	private static final String MODALITY = "MR";
//	private static final String PROTOCOL = "OFSEP";
//	private static final String DESC = "description here";
//	private static final String ID = "1";
//	private static final String DATE = "20170101";
//	private static final String NUMBER = "1";
//	private static final String IMAGE_PATH = "/IMAGES/IM000007";
//	private static final String SUBJECT_NAME ="subject";
//	private static final String SUBJECT_IDENTIFIER ="0010001";
//	private static final Date SUBJECT_BIRTH_DATE =new Date("1984/09/12");
//	private static final Boolean SELECTED_SERIE = true;
//	private static final String COMPRESSED_IMAGE_PATH = "/IMAGES/IM1220";
//	private static final String COPRESSED_DATA_FOLDER_PATH = "C:/Users/ifakhfak/Documents/sampleToAnonymizeCompressed";
//	
//	
//	
//	@Test
//	public void anonymizeTest() throws ShanoirImportException {
//		int startCount = START_COUNT;
//		List<Serie> serieList = createListSerie();
//		ImportSubjectDTO subject = createSubject();
//		String folderPath = FOLDER_PATH;
//		anonymizationService.anonymize(serieList, startCount, folderPath, subject);
//	}
//	
//	@Test
//	public void anonymizeCompressedDataTest() throws ShanoirImportException {
//		int startCount = START_COUNT;
//		List<Serie> serieList = createListSerieCompressed();
//		ImportSubjectDTO subject = createSubject();
//		String folderPath = COPRESSED_DATA_FOLDER_PATH;
//		anonymizationService.anonymize(serieList, startCount, folderPath, subject);
//	}
//	
//
//
//	private List<Serie> createListSerieCompressed() {
//		List<Serie> series = new ArrayList<Serie>();
//		Serie serie1 = new Serie(MODALITY,PROTOCOL ,DESC ,ID ,DATE ,NUMBER);
//		ArrayList<String> imagesPathList = new ArrayList<String>();
//		String imagePath = COMPRESSED_IMAGE_PATH;
//		imagesPathList.add(imagePath);
//		serie1.setImagesPathList(imagesPathList);
//		serie1.setSelected(SELECTED_SERIE);
//		series.add(serie1);
//		return series;
//	}
//
//	private List<Serie> createListSerie() {
//		List<Serie> series = new ArrayList<Serie>();
//		Serie serie1 = new Serie(MODALITY,PROTOCOL ,DESC ,ID ,DATE ,NUMBER);
//		ArrayList<String> imagesPathList = new ArrayList<String>();
//		String imagePath = IMAGE_PATH;
//		imagesPathList.add(imagePath);
//		serie1.setImagesPathList(imagesPathList);
//		serie1.setSelected(SELECTED_SERIE);
//		series.add(serie1);
//		return series;
//	}
//
//
//
//	private ImportSubjectDTO createSubject() {
//		final ImportSubjectDTO subject = new ImportSubjectDTO();
//		subject.setName(SUBJECT_NAME);
//		subject.setIdentifier(SUBJECT_IDENTIFIER);
//		subject.setBirthDate(SUBJECT_BIRTH_DATE);
//		return subject;
//	}
//
//}

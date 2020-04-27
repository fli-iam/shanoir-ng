package org.shanoir.ng.exchange.imports.dicom;

import java.io.File;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.beans.factory.annotation.Autowired;

@RunWith(MockitoJUnitRunner.class)
public class DicomDirGeneratorServiceTest {

	@Autowired
	private DicomDirGeneratorService dicomDirGeneratorService;

	@Before
	public void setup() {
		dicomDirGeneratorService = new DicomDirGeneratorService();
	}

	@Test
	public void testGenerateDicomDir() throws NoSuchAlgorithmException, IOException {
//		dicomDirGeneratorService.generateDicomDirFromDirectory(new File("/Users/mkain/Desktop/UCAN-SIM-dataset-5213f9fc-8251-4466-b1a8-194b528bc0af/DICOMDIR"),
//				new File("/Users/mkain/Desktop/UCAN-SIM-dataset-5213f9fc-8251-4466-b1a8-194b528bc0af"));
	}

}

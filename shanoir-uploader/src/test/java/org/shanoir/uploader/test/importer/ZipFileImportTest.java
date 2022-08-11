package org.shanoir.uploader.test.importer;

import java.io.File;
import java.net.URISyntaxException;
import java.net.URL;

import org.apache.log4j.Logger;
import org.junit.jupiter.api.Test;
import org.shanoir.uploader.model.rest.importer.ImportJob;
import org.shanoir.uploader.test.AbstractTest;

public class ZipFileImportTest extends AbstractTest {

	private static Logger logger = Logger.getLogger(ZipFileImportTest.class);
	
	@Test
	public void importDicomZipTest() throws Exception {
		ImportJob importJob = step1UploadDicom("acr_phantom_t1.zip");
	}
	
	private ImportJob step1UploadDicom(final String fileName) {
		try {
		    URL resource = getClass().getClassLoader().getResource(fileName);
		    if (resource != null) {
		        File file = new File(resource.toURI());
		        return shUpClient.uploadDicom(file);
		    }
		} catch (Exception e) {
		    logger.error("Error while reading file", e);
		}
		return null;
	}

}

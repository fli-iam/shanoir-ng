package org.shanoir.uploader.test.importer;

import java.io.File;
import java.net.URISyntaxException;
import java.net.URL;

import org.apache.log4j.Logger;
import org.junit.jupiter.api.Test;
import org.shanoir.uploader.test.AbstractTest;

public class ZipFileImportTest extends AbstractTest {

	private static Logger logger = Logger.getLogger(ZipFileImportTest.class);
	
	@Test
	public void importDicomZipTest() throws Exception {
		try {
		    URL resource = getClass().getClassLoader().getResource("acr_phantom_t1.zip");
		    if (resource != null) {
		        File file = new File(resource.toURI());
		        shUpClient.uploadDicom(file);
		    }
		} catch (URISyntaxException e) {
		    logger.error("Error while reading file", e);
		}
	}

}

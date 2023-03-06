package org.shanoir.uploader.test.datasets.dicom.web;

import java.io.File;
import java.net.URISyntaxException;
import java.net.URL;

import org.apache.log4j.Logger;
import org.junit.jupiter.api.Test;
import org.shanoir.uploader.test.AbstractTest;

public class StowDicomSRTest extends AbstractTest {

	private static Logger logger = Logger.getLogger(StowDicomSRTest.class);
	
	@Test
	public void postDICOMSRToDicomWeb() throws Exception {
		try {
		    URL resource = getClass().getClassLoader().getResource("DICOMSR.dcm");
		    if (resource != null) {
		        File file = new File(resource.toURI());
		       shUpClient.postDicomSR(file);
		    }
		} catch (URISyntaxException e) {
		    logger.error("Error while reading file", e); 
		}
	}

}
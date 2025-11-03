package org.shanoir.uploader.test.datasets.dicom.web;

import java.io.File;
import java.net.URISyntaxException;
import java.net.URL;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.junit.jupiter.api.Test;
import org.shanoir.uploader.test.AbstractTest;

public class StowRSDicomTest extends AbstractTest {

	private static final Logger logger = LoggerFactory.getLogger(StowRSDicomTest.class);
	
	@Test
	public void postDICOMSRToDicomWeb() throws Exception {
		try {
			URL resource = getClass().getClassLoader().getResource("DICOMSR.dcm");
			if (resource != null) {
				File file = new File(resource.toURI());
				shUpClient.postDicom(file);
			}
		} catch (URISyntaxException e) {
			logger.error("Error while reading file", e);
		}
	}

	@Test
	public void postDICOMMRToDicomWeb() throws Exception {
		try {
			URL resource = getClass().getClassLoader().getResource("acr_phantom_t1/1.3.12.2.1107.5.2.43.166066.2018042412210060639615964");
			if (resource != null) {
				File file = new File(resource.toURI());
				shUpClient.postDicom(file);
			}
		} catch (URISyntaxException e) {
			logger.error("Error while reading file", e);
		}
	}

}
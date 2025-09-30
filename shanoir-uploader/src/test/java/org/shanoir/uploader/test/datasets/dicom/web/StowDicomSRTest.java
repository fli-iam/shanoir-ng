package org.shanoir.uploader.test.datasets.dicom.web;

import java.io.File;
import java.net.URISyntaxException;
import java.net.URL;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.shanoir.uploader.test.AbstractTest;

public class StowDicomSRTest extends AbstractTest {

    private static final Logger logger = LoggerFactory.getLogger(StowDicomSRTest.class);

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
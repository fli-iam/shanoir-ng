package org.shanoir.uploader.test.datasets.dicom.web;

import java.io.File;
import java.net.URISyntaxException;
import java.net.URL;

import org.dcm4che3.data.Attributes;
import org.dcm4che3.data.Tag;
import org.dcm4che3.data.VR;
import org.dcm4che3.io.DicomInputStream;
import org.dcm4che3.io.DicomOutputStream;
import org.junit.jupiter.api.Test;
import org.shanoir.uploader.test.AbstractTest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
		logger.info("Starting postDICOMMRToDicomWeb");
		long startTime = System.currentTimeMillis();
		try {
			URL resource = getClass().getClassLoader().getResource("acr_phantom_t1_stowrs/");
			if (resource != null) {
				File file = new File(resource.toURI());
				if (file.isDirectory()) {
					for (File f : file.listFiles()) {
						try {
							shUpClient.postDicom(f);
						} catch(Exception e) {
							logger.error(e.getMessage(), e);
						}
					}
				}
			}
		} catch (URISyntaxException e) {
			logger.error("Error while reading file", e);
		}
		long stopTime = System.currentTimeMillis();
		long elapsedTime = stopTime - startTime;
		logger.info("postDICOMMRToDicomWeb: " + elapsedTime + "ms");
	}

	/**
	 * The below method can be used to produce STOW-RS
	 * ready DICOM files to test the import of STOW-RS
	 * on the Shanoir server.
	 * 
	 * @throws Exception
	 */
	@Test
	public void generateStowRSDicom() throws Exception {
		URL source = getClass().getClassLoader().getResource("acr_phantom_t1/");
		URL destination = getClass().getClassLoader().getResource("acr_phantom_t1_stowrs/");
		if (source != null) {
			File sourceFile = new File(source.toURI());
			File destinationFile = new File(destination.toURI());
			if (sourceFile.isDirectory()) {
				for (File f : sourceFile.listFiles()) {
					File newFile = new File(destinationFile, f.getName());
					try (DicomInputStream dIn = new DicomInputStream(f);
						DicomOutputStream dOu = new DicomOutputStream(newFile);) {
						Attributes metaInformationAttributes = dIn.readFileMetaInformation();
						Attributes datasetAttributes = dIn.readDataset();
						String deidMethod = "Basic Application Confidentiality Profile + Clean Pixel Data Option";
        				datasetAttributes.setString(Tag.DeidentificationMethod, VR.LO, deidMethod);
				        datasetAttributes.setString(Tag.PatientIdentityRemoved, VR.CS, "YES");
				        String protocolId = "1";
        				datasetAttributes.setString(Tag.ClinicalTrialProtocolID, VR.LO, protocolId);
						datasetAttributes.setString(Tag.ClinicalTrialProtocolName, VR.LO, "Cardiac Phantom QA Study");
						dOu.writeDataset(metaInformationAttributes, datasetAttributes);
					}
				}
			}
		}
	}

}
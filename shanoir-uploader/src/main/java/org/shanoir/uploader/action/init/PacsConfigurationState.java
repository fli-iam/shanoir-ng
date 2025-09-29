package org.shanoir.uploader.action.init;

import java.net.MalformedURLException;

import org.shanoir.uploader.ShUpConfig;
import org.shanoir.uploader.ShUpOnloadConfig;
import org.shanoir.uploader.dicom.DicomServerClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * This concrete state class defines the state when the ShanoirUploader tests the connection to the PACS
 *
 * As a result, the context will change either to :
 * 		- a Manual Pacs Configuration in case of failure
 * 		- step to the READY state in case of success.
 * 
 * @author atouboul
 * 
 */
@Component
public class PacsConfigurationState implements State {
	
	private static final Logger logger = LoggerFactory.getLogger(PacsConfigurationState.class);
	
	public ShUpOnloadConfig shUpOnloadConfig = ShUpOnloadConfig.getInstance();

	@Autowired
	private ReadyState readyState;

	@Autowired
	private PacsManualConfigurationState pacsManualConfigurationState;
	
	public void load(StartupStateContext context) {
		initDicomServerClient();
		/**
		 * Test if shanoir is able to contact the configured pacs in dicom_server.properties
		 */
		if (shUpOnloadConfig.getDicomServerClient().echoDicomServer()) {
			context.setState(readyState);
		} else {
			context.setState(pacsManualConfigurationState);
		}
		context.nextState();
	}

	/*
	 * Initialize the DicomServerClient.
	 */
	private void initDicomServerClient() {
		DicomServerClient dSC;
		try {
			dSC = new DicomServerClient(ShUpConfig.dicomServerProperties, shUpOnloadConfig.getWorkFolder());
			shUpOnloadConfig.setDicomServerClient(dSC);
			logger.info("PacsConfigurationState: DicomServerClient successfully initialized.");
		} catch (MalformedURLException e) {
			logger.info("Error with init of DicomServerClient: " + e.getMessage(), e);
		}
	}

}

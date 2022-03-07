package org.shanoir.uploader.action.init;

import java.io.File;

import org.apache.log4j.Logger;
import org.shanoir.uploader.ShUpConfig;
import org.shanoir.uploader.ShUpOnloadConfig;
import org.shanoir.uploader.dicom.DicomServerClient;

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
public class PacsConfigurationState implements State {
	
	private static Logger logger = Logger.getLogger(PacsConfigurationState.class);
	
	public static ShUpOnloadConfig shUpOnloadConfig = ShUpOnloadConfig.getInstance();
	
	public void load(StartupStateContext context) {
		initDicomServerClient();
		/**
		 * Test if shanoir is able to contact the configured pacs in dicom_server.properties
		 */
		if (shUpOnloadConfig.getDicomServerClient().echoDicomServer()){
			context.setState(new ReadyState());
		} else {
			context.setState(new PacsManualConfigurationState());
		}
		context.nextState();
	}

	/*
	 * Initialize the DicomServerClient.
	 */
	private void initDicomServerClient() {
		shUpOnloadConfig.setWorkFolder(
				new File(ShUpConfig.shanoirUploaderFolder + File.separator + ShUpConfig.WORK_FOLDER));
		if (shUpOnloadConfig.getWorkFolder().exists()) {
			// do nothing
		} else {
			shUpOnloadConfig.getWorkFolder().mkdirs();
		}
		DicomServerClient dSC = new DicomServerClient(ShUpConfig.dicomServerProperties, shUpOnloadConfig.getWorkFolder());
		shUpOnloadConfig.setDicomServerClient(dSC);
		logger.info("DicomServerClient successfully initialized.");
	}

}

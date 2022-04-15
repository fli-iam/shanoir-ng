/**
 * Shanoir NG - Import, manage and share neuroimaging data
 * Copyright (C) 2009-2019 Inria - https://www.inria.fr/
 * Contact us on https://project.inria.fr/shanoir/
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see https://www.gnu.org/licenses/gpl-3.0.html
 */

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

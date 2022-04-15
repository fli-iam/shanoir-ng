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

package org.shanoir.uploader.dicom.retrieve;

import java.io.IOException;

import org.apache.log4j.Logger;
import org.dcm4che2.tool.dcmrcv.DcmRcv;
import org.shanoir.uploader.dicom.query.ConfigBean;

/**
 * The DcmRcvHelper handles the download of DICOM files.
 * It's a local service / server which is started, when
 * the ShanoirUploader is started.
 * @author mkain
 *
 */
public class DcmRcvManager {

	private static Logger logger = Logger.getLogger(DcmRcvManager.class);

	/** The DICOM Server itself. */
	private DcmRcv dcmrcv = new DcmRcv();
	
	public void start(final ConfigBean configBean, final String folderPath) {
		try {
			logger.info("Start running local server DcmRcv with params:"
					+ " AET title: " + configBean.getLocalDicomServerAETCalling()
					+ ", AET host: " + configBean.getLocalDicomServerHost()
					+ ", AET port: " + configBean.getLocalDicomServerPort());
			dcmrcv.setAEtitle(configBean.getLocalDicomServerAETCalling());
			dcmrcv.setHostname(configBean.getLocalDicomServerHost());
			dcmrcv.setPort(configBean.getLocalDicomServerPort());
			dcmrcv.setDestination(folderPath);
			dcmrcv.setPackPDV(true);
			dcmrcv.setTcpNoDelay(true);
			dcmrcv.initTransferCapability();
			dcmrcv.start();
			logger.info("Started DcmRcv service successfully.");
		} catch (IOException e) {
			logger.error("Error during startup of DcmRcv: " + e.getMessage() + "\n DcmRcv is not started.");
			System.exit(0);
		}
	}

	public void stop() {
		dcmrcv.stop();
	}
	
	public void setDestination(final String folderPath) {
		dcmrcv.setDestination(folderPath);
	}
	
}

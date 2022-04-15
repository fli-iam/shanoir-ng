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

package org.shanoir.uploader.dicom.query;

import org.apache.log4j.Logger;
import org.dcm4che2.tool.dcmecho.DcmEcho;

/**
 * 
 * @author mkain
 *
 */
public class Echo {

	private static final String DCMECHO = "DCMECHO";

	static Logger logger = Logger.getLogger(Echo.class);

	/**
	 * 
	 * @param config
	 */
	public boolean echo(final ConfigBean config) {
		logger.info("echo: Starting...");
		final DcmEcho dcmecho = new DcmEcho(DCMECHO);
		dcmecho.setRemoteHost(config.getDicomServerHost());
		dcmecho.setRemotePort(config.getDicomServerPort());
		dcmecho.setCalledAET(config.getDicomServerAETCalled(), false);
		dcmecho.setCalling(config.getLocalDicomServerAETCalling());
		if (config.isDicomServerEnableTLS3DES()) {
			dcmecho.setTlsNeedClientAuth(false);
			dcmecho.setTls3DES_EDE_CBC();
			dcmecho.setKeyStoreURL(config.getDicomServerKeystoreURL());
			dcmecho.setKeyStorePassword(config.getDicomServerKeystorePassword());
			dcmecho.setTrustStoreURL(config.getDicomServerTruststoreURL());
			dcmecho.setTrustStorePassword(config.getDicomServerTruststorePassword());
		}
		try {
			dcmecho.open();
		} catch (Exception e) {
			logger.error("echo: Failed to open connection:" + e.getMessage());
			return false;
		}
		try {
			dcmecho.echo();
			return true;
		} catch (Exception e) {
			logger.error("echo: Failed to echo:" + e.getMessage());
			return false;
		} finally {
			try {
				dcmecho.close();
			} catch (Exception e) {
				logger.error("echo: Failed to close connection:" + e.getMessage());
				return false;
			}
		}
	}

}

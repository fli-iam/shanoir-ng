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

package org.shanoir.uploader.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.jboss.seam.annotations.Logger;
import org.jboss.seam.log.Log;
import org.jboss.seam.log.Logging;

/**
 * The Class StreamGobbler.
 * 
 * @author aferial
 * @version $Revision: 1.3 $
 */
public class StreamGobbler extends Thread {

	/** The log. */
	@Logger
	private final Log log = Logging.getLog(StreamGobbler.class);

	/** The is. */
	private InputStream is;

	/** The type. */
	private String type;

	/** Result string. */
	private String stringDisplay = "";

	/**
	 * Creates a new StreamGobbler object.
	 * 
	 * @param is
	 *            the is
	 * @param type
	 *            the type
	 */
	public StreamGobbler(final InputStream is, final String type) {
		this.is = is;
		this.type = type;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Thread#run()
	 */
	public void run() {
		try {
			final InputStreamReader isr = new InputStreamReader(is);
			final BufferedReader br = new BufferedReader(isr);
			String line = null;

			while ((line = br.readLine()) != null) {
				if (type.equals("ERROR")) {
					log.error(line);
					stringDisplay += "ERROR : " + line + "\n";
				} else if (type.equals("DEBUG")) {
					log.debug(line);
					stringDisplay += "DEBUG : " + line + "\n";
				} else if (type.equals("INFO")) {
					log.info(line);
					stringDisplay += "INFO : " + line + "\n";
				}
			}
			isr.close();
		} catch (final IOException ioe) {
			log.error("IOE", ioe);
		}
	}

	/**
	 * Gets the string display.
	 * 
	 * @return the stringDisplay
	 */
	public String getStringDisplay() {
		return stringDisplay;
	}

	/**
	 * Sets the string display.
	 * 
	 * @param stringDisplay
	 *            the stringDisplay to set
	 */
	public void setStringDisplay(String stringDisplay) {
		this.stringDisplay = stringDisplay;
	}
}

package org.shanoir.ng.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class StreamGobbler extends Thread {

	/**
	 * Logger
	 */
	private static final Logger LOG = LoggerFactory.getLogger(ShanoirExec.class);

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
					LOG.error(line);
					stringDisplay += "ERROR : " + line + "\n";
				} else if (type.equals("DEBUG")) {
					LOG.debug(line);
					stringDisplay += "DEBUG : " + line + "\n";
				} else if (type.equals("INFO")) {
					LOG.info(line);
					stringDisplay += "INFO : " + line + "\n";
				}
			}
			isr.close();
		} catch (final IOException ioe) {
			LOG.error(ioe.getMessage());
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


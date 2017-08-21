package org.shanoir.ng.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ShanoirExec {
	
	/**
	 * Logger
	 */
	private static final Logger LOG = LoggerFactory.getLogger(ShanoirExec.class);
	
	/**
	 * Instantiates a new shanoir tk exec.
	 */
	private ShanoirExec() {
		super();
	}
	
	/**
	 * Execute the dcmdjpeg binary to uncompress dicom images.
	 *
	 * @param dcmdjpegPath
	 *            the dcmdjpeg path
	 * @param inputFile
	 *            the input file
	 * @param outputFile
	 *            the output file
	 *
	 * @return the string
	 */
	public static String dcmdjpeg(final String dcmdjpegPath, final String inputFile, final String outputFile) {
		LOG.debug("dcmdjpeg : Begin");
		LOG.info("dcmdjpeg : " + dcmdjpegPath);

		String[] cmd = new String[3];
		cmd[0] = dcmdjpegPath;
		cmd[1] = inputFile;
		cmd[2] = outputFile;

		final String result = exec(cmd);

		LOG.debug("dcmdjpeg : End");
		return result;
	}
	
	/**
	 * Execute the command line given in argument.
	 *
	 * @param cmd
	 *            the command line as a string array
	 * @return the output result
	 */
	public static String exec(final String[] cmd) {
		return exec(cmd, null);
	}
	
	/**
	 * Execute the command line given in argument.
	 *
	 * @param cmd
	 *            the command line as a string array
	 * @param envp
	 *            array of strings, each element of which has environment
	 *            variable settings in the format name=value, or null if the
	 *            subprocess should inherit the environment of the current
	 *            process.
	 *
	 * @return the output result
	 */
	public static String exec(final String[] cmd, final String[] envp) {

		String executingCommand = "";
		for (final String item : cmd) {
			executingCommand += (item + " ");
		}
		LOG.debug("exec : Executing " + executingCommand);

		StreamGobbler errorGobbler = null;
		StreamGobbler outputGobbler = null;
		String result = null;
		try {
			Runtime rt = Runtime.getRuntime();

			final Process proc = rt.exec(cmd, envp);

			// any error message?
			errorGobbler = new StreamGobbler(proc.getErrorStream(), "ERROR");

			// any output?
			outputGobbler = new StreamGobbler(proc.getInputStream(), "DEBUG");

			// kick them off
			errorGobbler.start();
			outputGobbler.start();

			// any error???
			final int exitVal = proc.waitFor();

			if (exitVal != 0) {
				LOG.error("The exit value is " + exitVal + ", an error has probably occured");
			}
			// let errorGobbler and outputGobbler finish execution before finishing main thread
			errorGobbler.join();
			outputGobbler.join();

			proc.getInputStream().close();
			proc.getErrorStream().close();
			LOG.debug("exec : ExitValue: " + exitVal);
		} catch (final Exception exc) {
			LOG.error("exec : " + exc.getMessage());
			if (errorGobbler != null && outputGobbler != null) {
				result = errorGobbler.getStringDisplay();
				if (result != null && !"".equals(result)) {
					result += "\n\n";
				}
				result += outputGobbler.getStringDisplay();
				errorGobbler = null;
				outputGobbler = null;
				return result;
			}
		}
		if (errorGobbler != null) {
			// TODO: Add log.error(...) ?
			result = errorGobbler.getStringDisplay();
			if (result != null && !"".equals(result)) {
				result += "\n\n";
			}
		}
		if (outputGobbler != null) {
			result += outputGobbler.getStringDisplay();
		}

		LOG.debug("exec : return result " + result);

		return result;
	}

}

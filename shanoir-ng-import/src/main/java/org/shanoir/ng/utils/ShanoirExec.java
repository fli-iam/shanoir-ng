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

package org.shanoir.ng.utils;

import java.util.Arrays;
import java.util.Map;
import java.util.Map.Entry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class ShanoirExec {
	
	/**
	 * Logger
	 */
	private static final Logger LOG = LoggerFactory.getLogger(ShanoirExec.class);
	
	@Value("${shanoir.conversion.dcm2nii.converters.clidcm.path.lib}")
    private String clidcmPathLib;
	
	/**
	 * Exec the clidcm command to convert Dicom files to Nifti files.
	 *
	 * @param inputFolder
	 *            the input folder
	 * @param clidcmPath
	 *            the clidcm path
	 * @param outputFolder
	 *            the output folder
	 *
	 * @return the string
	 */
	public String clidcmExec(final String inputFolder, final String clidcmPath, final String outputFolder) {
		LOG.debug("clidcmExec : Begin");
		LOG.info("clidcmExec : {}", clidcmPath);

		String[] cmd = new String[4];
		cmd[0] = clidcmPath;

		cmd[1] = ImportUtils.convertFilePath(inputFolder);
		cmd[2] = "--dir";
		cmd[3] = ImportUtils.convertFilePath(outputFolder);

		Map<String, String> systemEnv = System.getenv();
		int size = systemEnv.size();
		if (!systemEnv.containsKey("VISTAL_FMT")) {
			size = size + 2;
		}
		
		String[] envp = new String[size];
		int i = 0;
		for (final Entry<String, String> entry : systemEnv.entrySet()) {
			envp[i] = entry.getKey() + "=" + entry.getValue();
			i++;
		}
		
		if (!systemEnv.containsKey("VISTAL_FMT")) {
			envp[i] = "VISTAL_FMT=NII";
			envp[i + 1] = "LD_LIBRARY_PATH=$LD_LIBRARY_PATH:" + clidcmPathLib;
		}

		final String result = exec(cmd, envp);

		LOG.debug("clidcmExec : End");
		return result;
	}
	

	/**
	 * Exec the dcm2nii command to convert Dicom files to Nifti files.
	 *
	 * @param inputFolder
	 *            the input folder
	 * @param dcm2niiPath
	 *            the dcm2nii path
	 * @param outputFolder
	 *            the output folder
	 *
	 * @return the string
	 */
	public String dcm2niiExec(final String inputFolder, final String dcm2niiPath, final String outputFolder) {
		return dcm2niiExec(inputFolder, dcm2niiPath, outputFolder,false);
	}


	/**
	 * Exec the dcm2nii command to convert Dicom files to Nifti files.
	 *
	 * @param inputFolder
	 *            the input folder
	 * @param dcm2niiPath
	 *            the dcm2nii path
	 * @param outputFolder
	 *            the output folder
	 *  @param is4D
	 *            is a 4D nifti
	 *
	 * @return the string
	 */
	public String dcm2niiExec(final String inputFolder, final String dcm2niiPath, final String outputFolder, boolean is4D) {
		LOG.debug("dcm2niiExec : Begin");
		LOG.info("dcm2niiExec : {}", dcm2niiPath);

		String[] cmd = null;

		if(dcm2niiPath.contains("dcm2niix")){
			cmd = new String[22];
			cmd[0] = "dcm2niix";
			//BIDS sidecar
			cmd[1] = "-b";
			cmd[2] = "y";

			// ignore derived and 2D images
			cmd[3] = "-i";
			cmd[4] = "n";

			// text notes includes private patient details
			cmd[5] = "-t";
			cmd[6] = "n";

			// merge 2D slices from same series regardless of study time, echo, coil, orientation, etc.
			cmd[7] = "-m";
			cmd[8] = "n";

			// Philips precise float (not display) scaling
			cmd[9] = "-p";
			cmd[10] = "n";

			// single file mode, do not convert other images in folder
			cmd[11] = "-s";
			cmd[12] = "n";

			// no gzip compression
			cmd[13] = "-z";
			cmd[14] = "n";

			// output directory
			cmd[15] = "-o";
			cmd[16] = outputFolder;

			// no reorientation no croping
			cmd[17] = "-x";
			cmd[18] = "n";

			// verbose
			cmd[19] = "-v";
			if(LOG.isDebugEnabled()){
				cmd[20] = "y";
			}else{
				cmd[20] = "n";
			}

			//inputFolder
			cmd[21] = inputFolder;

		}else{
			if(is4D){
				cmd = new String[26];
			}else{
				cmd = new String[24];
			}

			cmd[0] = dcm2niiPath;

			// no anonymization (already done)
			cmd[1] = "-a";
			cmd[2] = "n";

			// no date in fileName
			cmd[3] = "-d";
			cmd[4] = "n";

			// output nii single file
			cmd[5] = "-n";
			cmd[6] = "y";

			// no id in the filename
			cmd[7] = "-i";
			cmd[8] = "n";

			// no event name in the output file name
			cmd[9] = "-e";
			cmd[10] = "y";

			// procol name in the output file name
			cmd[11] = "-p";
			cmd[12] = "y";

			// no gzip compression
			cmd[13] = "-g";
			cmd[14] = "n";

			// no file source name in destination file name
			cmd[15] = "-f";
			cmd[16] = "n";

			// output directory
			cmd[17] = "-o";
			cmd[18] = outputFolder;

			// no reorientation no croping
			cmd[19] = "-x";
			cmd[20] = "n";

			// no reorientation
			cmd[21] = "-r";
			cmd[22] = "n";

			if(is4D){
				// create 4D volumes
				cmd[23] = "-4";
				cmd[24] = "y";
				// list of dicom images to be converted
				cmd[25] = inputFolder;
			}else{
				// list of dicom images to be converted
				cmd[23] = inputFolder;
			}
		}

		LOG.info("CMD DCM2NII {}", Arrays.asList(cmd));

		final String result = exec(cmd);

		LOG.debug("dcm2niiExec : End");
		return result;
	}
	
	/**
	 * Exec the mcverter command to convert Dicom files to Nifti files.
	 *
	 * @param inputFolder
	 *            the input folder
	 * @param mcverterPath
	 *            the mcverter path
	 * @param outputFolder
	 *            the output folder
	 *
	 * @return the string
	 */
	public String mcverterExec(final String inputFolder, final String mcverterPath, final String outputFolder, boolean is4D) {
		LOG.debug("mcverterExec : Begin");
		LOG.info("mcverterExec : {}", mcverterPath);

		String[] cmd = null;
		if(is4D){
			cmd = new String[10];
		}else{
			cmd = new String[9];
		}
		cmd[0] = mcverterPath;

		// output folder
		cmd[1] = "-o";
		cmd[2] = outputFolder;

		// output format
		cmd[3] = "-f";
		cmd[4] = "nifti";

		// save files as .nii
		cmd[5] = "-n";

		// naming format outpufile
		cmd[6] = "-F";
		//Due to modification on format for latest version change the format
		if(mcverterPath!=null && mcverterPath.contains("2.0")){
			cmd[7] = "'-PatientName,-PatientId,-SeriesDate,-SeriesTime,-StudyId,-StudyDescription,+SeriesNumber,-SequenceName,-SeriesDescription,+ProtocolName'";
		}else{
			cmd[7] = "-PatientName|-PatientId|-SeriesDate|-SeriesTime|-StudyId|-StudyDescription|+SeriesNumber|-SequenceName|-SeriesDescription|+ProtocolName";
		}

		// Input folder
		cmd[8] = inputFolder;

		if(is4D){
			cmd[9] = "-d";
		}

		final String result = exec(cmd);

		LOG.debug("mcverterExec : End");
		return result;
	}

	/**
	 * Execute the command to get mcverter exe.
	 *
	 * @param mcverterpath
	 *            the path to mcverter command
	 * Getting the version of mcverter causes an exit code = 1 even though there is no error
	 * when exec "mcverter -V" so we get rid of this error in this particular case
	 * by checking if the cmd is for the version
	 *
	 * @return string containing the version
	 */
	public String mcverterVersionExec(final String mcverterPath) {
		LOG.debug("mcverterVersionExec : Begin");

		String[] cmd = new String[2];
		cmd[0] = mcverterPath;
		// output folder
		cmd[1] = "-V";

		StringBuilder executingCommand = new StringBuilder("");
		for (final String item : cmd) {
			executingCommand.append(item).append(" ");
		}
		LOG.debug("exec : Executing {}", executingCommand);

		StreamGobbler errorGobbler = null;
		StreamGobbler outputGobbler = null;
		String result = null;
		try {
			Runtime rt = Runtime.getRuntime();

			final Process proc = rt.exec(cmd, null);

			// any error message?
			errorGobbler = new StreamGobbler(proc.getErrorStream(), "ERROR");

			// any output?
			outputGobbler = new StreamGobbler(proc.getInputStream(), "DEBUG");

			// kick them off
			errorGobbler.start();
			outputGobbler.start();

			// any error???
			final int exitVal = proc.waitFor();

			/*Getting the version of mcverter causes an exit code = 1 even though there is no error
			when exec "mcverter -V" so we get rid of this error in this particular case
			by checking if the cmd is for the version*/

			if (exitVal != 0 && exitVal != 1) {
				LOG.error("The exit value is {}, an error has probably occured", exitVal);
			}

			proc.getInputStream().close();
			proc.getErrorStream().close();
			LOG.debug("exec : ExitValue: {}", exitVal);
		} catch (final Exception exc) {
			LOG.error("exec : {}", exc.getMessage());
			if (errorGobbler != null && outputGobbler != null) {
				result = errorGobbler.getStringDisplay();
				if (result != null && !"".equals(result)) {
					result += "\n\n";
				}
				result += outputGobbler.getStringDisplay();
				return result;
			}
		}
		if (errorGobbler != null) {
			result = errorGobbler.getStringDisplay();
			if (result != null && !"".equals(result)) {
				result += "\n\n";
			}
		}
		if (outputGobbler != null) {
			result += outputGobbler.getStringDisplay();
		}

		LOG.info("mcverterVersionExec = {}", result);
		LOG.debug("mcverterVersionExec : End");
		return result;

	}

	/**
	 * Exec the dcm2nii command to get the version of the software
	 * It returns a full String from which we take the first line :
	 * Chris Rorden's dcm2nii :: 4AUGUST2014 (Debian) 32bit BSD License
	 * And extract the version : 4AUGUST2014 (Debian) 32bit BSD License
	 *
	 * @param dcm2niiPath
	 *
	 * @return the version
	 */
	public String dcm2niiVersionExec(final String dcm2niiPath) {
		LOG.debug("dcm2niiVersionExec : Begin");
		LOG.info("dcm2niiVersionExec : {}", dcm2niiPath);
		String[] cmd = new String[1];
		cmd[0] = dcm2niiPath;
		final String result = exec(cmd);
		LOG.info("dcm2niiVersionExec : {}", result);
		LOG.debug("dcm2niiVersionExec : End");
		return result;
	}


	/**
	 * Exec the dicom2nifti command to convert Dicom files to Nifti files.
	 *
	 * @param inputFolder
	 *            the input folder
	 * @param dicom2niftiPath
	 *            the dicom2nifti path
	 * @param outputFolder
	 *            the output folder
	 *
	 * @return the string
	 */
	public String dicom2niftiExec(String inputFolder, final String dicom2niftiPath, final String outputFolder) {
		LOG.debug("dicom2niftiExec : Begin");

		LOG.debug("dicom2niftiExec : {}", dicom2niftiPath);

		String[] cmd = null;
		//Usage: dicom2nifti [OPTIONS] input output
		cmd = new String[3];
		cmd[0] = "bash";
		cmd[1] = "-c";
		//Then as we use wildcard *, the rest must be done as one command
		StringBuilder cmdLine = new StringBuilder(dicom2niftiPath);
		if(LOG.isDebugEnabled()){
			// verbose
			cmdLine.append(" -v");
			cmdLine.append(" debug");
		}else{
			cmdLine.append(" -v");
			cmdLine.append(" warning");
		}
		cmdLine.append(" --dtype single ");

		cmdLine.append(inputFolder);
		cmdLine.append(" ");
		cmdLine.append(outputFolder);
		cmd[2] = cmdLine.toString();

		LOG.info("CMD DICOM2NIFTI {}", Arrays.asList(cmd));

		final String result = exec(cmd);

		LOG.debug("dicom2niftiExec : End");
		return result;
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
	public String dcmdjpeg(final String dcmdjpegPath, final String inputFile, final String outputFile) {
		LOG.debug("dcmdjpeg : Begin");
		LOG.info("dcmdjpeg : {}", dcmdjpegPath);

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
	public String exec(final String[] cmd) {
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
	public String exec(final String[] cmd, final String[] envp) {

		StringBuilder executingCommand = new StringBuilder("");
		for (final String item : cmd) {
			executingCommand.append(item).append(" ");
		}
		LOG.debug("exec : Executing {}", executingCommand);

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
				LOG.error("The exit value is {}, an error has probably occured", exitVal);
			}
			// let errorGobbler and outputGobbler finish execution before finishing main thread
			errorGobbler.join();
			outputGobbler.join();

			proc.getInputStream().close();
			proc.getErrorStream().close();
			LOG.debug("exec : ExitValue: {}", exitVal);
		} catch (final Exception exc) {
			LOG.error("exec : {}", exc.getMessage());
			if (errorGobbler != null && outputGobbler != null) {
				result = errorGobbler.getStringDisplay();
				if (result != null && !"".equals(result)) {
					result += "\n\n";
				}
				result += outputGobbler.getStringDisplay();
				return result;
			}
		}
		if (errorGobbler != null) {
			result = errorGobbler.getStringDisplay();
			if (result != null && !"".equals(result)) {
				result += "\n\n";
			}
		}
		if (outputGobbler != null) {
			result += outputGobbler.getStringDisplay();
		}

		LOG.debug("exec : return result {}", result);

		return result;
	}

}

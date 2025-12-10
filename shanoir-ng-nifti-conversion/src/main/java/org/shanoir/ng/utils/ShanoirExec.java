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

import java.io.File;
import java.util.Arrays;

import org.shanoir.ng.service.NIfTIConverterService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class ShanoirExec {

    /**
     * Logger
     */
    private static final Logger LOG = LoggerFactory.getLogger(ShanoirExec.class);

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
        LOG.debug("dcm2niiExec : {}", dcm2niiPath);

        String[] cmd = null;

        if (dcm2niiPath.contains("dcm2niix")) {
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

            // gzip compression
            cmd[13] = "-z";
            cmd[14] = "y";

            // output directory
            cmd[15] = "-o";
            cmd[16] = outputFolder;

            // no reorientation no croping
            cmd[17] = "-x";
            cmd[18] = "n";

            // verbose
            cmd[19] = "-v";
            if (LOG.isDebugEnabled()) {
                cmd[20] = "y";
            } else {
                cmd[20] = "n";
            }

            //inputFolder
            cmd[21] = inputFolder;

        } else {
            if (is4D) {
                cmd = new String[26];
            } else {
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

            // gzip compression
            cmd[13] = "-g";
            cmd[14] = "y";

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

            if (is4D) {
                // create 4D volumes
                cmd[23] = "-4";
                cmd[24] = "y";
                // list of dicom images to be converted
                cmd[25] = inputFolder;
            } else {
                // list of dicom images to be converted
                cmd[23] = inputFolder;
            }
        }

        LOG.debug("CMD DCM2NII {}", Arrays.asList(cmd));

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
        LOG.debug("mcverterExec : {}", mcverterPath);

        String[] cmd = null;
        if (is4D) {
            cmd = new String[10];
        } else {
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
        if (mcverterPath != null && mcverterPath.contains("2.0")) {
            cmd[7] = "'-PatientName,-PatientId,-SeriesDate,-SeriesTime,-StudyId,-StudyDescription,+SeriesNumber,-SequenceName,-SeriesDescription,+ProtocolName'";
        } else {
            cmd[7] = "-PatientName|-PatientId|-SeriesDate|-SeriesTime|-StudyId|-StudyDescription|+SeriesNumber|-SequenceName|-SeriesDescription|+ProtocolName";
        }

        // Input folder
        cmd[8] = inputFolder;

        if (is4D) {
            cmd[9] = "-d";
        }

        final String result = exec(cmd);

        LOG.debug("mcverterExec : End");
        return result;
    }

    public boolean anima(String fileToConvert) {

        String newImageName = fileToConvert.replace(".img", ".nii.gz");
        File imgFile = new File(fileToConvert);

        String[] command = {"/bin/bash", "-c", "animaConvertImage -i " + fileToConvert + " -o " + newImageName};
        String result = this.exec(command);
        LOG.error(result);
        return true;
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

    /**
     * Execute dicomifier conversion DICOM => NIFTI
     * @param inputFolder the input folder where the DICOM are
     * @param outputFolder the output folder where to set the nifti
     * @return dicomifierPath converter's path
     */
    public String dicomifier(String inputFolder, String outputFolder, String dicomifierPath, String method) {
        String dicomdir = "";
        if (NIfTIConverterService.BRUKER_TO_DICOM_METHOD.equals(method)) {
            dicomdir = "--dicomdir";
        }
        String logs = "dicomifier: ";
        String execString = dicomifierPath + " " + method + " source " + inputFolder + " destination " + outputFolder + " " + dicomdir;
        logs.concat(execString);
        final String result = exec(execString.split(" "));

        //LOG.error("Converting from " + inputFolder + " to " + outputFolder + ". Using: " + execString + " result: " + result);

        return logs.concat("\n Result: " + result);
    }

    /**
     * This method converts a dicom using MRIConvert
     * This method is synchronized as we use xvfb
     * @param inputFolder the nput folder
     * @param outputFolder the output folder
     * @return the olg to display.
     */
    public synchronized String mriConverter(String inputFolder, String outputFolder, String mriConverterPath) {
        String logs = "mriConverter: ";
        String execString = "";

        // java -classpath MRIManager.jar DicomToNifti Subject4/ /tmp/ "PatientName-SerialNumber-Protocol" "[ExportOptions] 00000"
        execString += "xvfb-run java -classpath " + mriConverterPath + " DicomToNifti "    + inputFolder + " "    + outputFolder
                + " PatientName-SerialNumber-SequenceName [ExportOptions]00000";

        logs.concat(execString);

        final String result = exec(execString.split(" "));

        LOG.error("Converting from " + inputFolder + " to " + outputFolder + ". Using: " + execString + " result: " + result);

        return logs.concat("\n Result: " + result);
    }

    /**
     * Replace the file separators to make it work under windows or unix system.
     *
     * @param firstImagePath
     *            the first image path
     *
     * @return the string
     */
    public static String convertFilePath(final String firstImagePath) {
        return firstImagePath.replaceAll("\\\\", "/");
    }

}

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

package org.shanoir.ng.shared.service;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import org.shanoir.ng.shared.exception.ShanoirException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * The class CStoreDicomService calls the command storescu of dcm4che3 on the
 * command line to send the dicom images to the PACS via c-store. In the
 * dockerbuild file of the ms datasets /docker-compose/datasets
 * dcm4che-5.21.0-bin.zip is installed and configured to be available for this.
 * 
 * @author mkain
 *
 */
@Component(value = "cstore")
public class CStoreDicomService implements DicomServiceApi {
	
	/** Logger. */
	private static final Logger LOG = LoggerFactory.getLogger(CStoreDicomService.class);

	private static final String STORESCU = "storescu";

	private static final String C = "-c";

	@Value("${dcm4chee-arc.host}")
	private String dcm4cheeHost;

	@Value("${dcm4chee-arc.port.dcm}")
	private String dcm4cheePortDcm;

	@Value("${dcm4chee-arc.dicom.c-store.aet.called}")
	private String dcm4cheeCStoreAETCalled;

	@Override
	public void sendDicomFilesToPacs(File directoryWithDicomFiles) throws Exception {
		if (directoryWithDicomFiles != null && directoryWithDicomFiles.exists()
				&& directoryWithDicomFiles.isDirectory()) {
			File[] dicomFiles = directoryWithDicomFiles.listFiles();
			LOG.info("Start: C-STORE sending " + dicomFiles.length + " dicom files to PACS from folder: "
					+ directoryWithDicomFiles.getAbsolutePath());
			List<String> args = new ArrayList<String>();
			args.add(STORESCU);
			args.add(C);
			args.add(dcm4cheeCStoreAETCalled + "@" + dcm4cheeHost + ":" + dcm4cheePortDcm);
			args.add(directoryWithDicomFiles.getAbsolutePath());
			execute(args);
			LOG.info("Finished: C-STORE sending " + dicomFiles.length + " dicom files to PACS from folder: "
					+ directoryWithDicomFiles.getAbsolutePath());
		} else {
			throw new ShanoirException(
					"sendDicomFilesToPacs called with null, or file: not existing or not a directory.");
		}
	}

	/**
	 * Calls the command line, error occurred if exitCode != 0.
	 * 
	 * Uses ProcessBuilder here as Runtime.exec did not work (stopped after sending 49 images),
	 * very probably related to a buffer problem: many output of the script saturates the default
	 * output buffer of Runtime.exec, so we have been obliged to use ProcessBuilder here.
	 * 
	 * Furthermore the below code is blocking by intention. It could be coded not blocking, BUT
	 * the problem is when to check for the results: 5, 10, 15 secs?, in any case we want to continue
	 * when it terminates, so no real gain when using ExecuterService.
	 * 
	 * @param args
	 * @throws Exception
	 */
	private void execute(List<String> args) throws Exception {
		LOG.debug("Calling command: " + args.toString());
		ProcessBuilder processBuilder = new ProcessBuilder();
		processBuilder.command(args);
		Process process = processBuilder.start();
        BufferedReader reader =
                new BufferedReader(new InputStreamReader(process.getInputStream()));
        String line;
        while ((line = reader.readLine()) != null) {
            LOG.debug(line);
        }
        int exitCode = process.waitFor();
		if (exitCode != 0)
			throw new ShanoirException("Send to PACS (c-store) error occured on cmd line.");
	}

}
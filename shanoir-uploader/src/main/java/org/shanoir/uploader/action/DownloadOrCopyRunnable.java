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

package org.shanoir.uploader.action;

import java.io.File;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;
import org.shanoir.uploader.ShUpOnloadConfig;
import org.shanoir.uploader.dicom.IDicomServerClient;
import org.shanoir.uploader.dicom.Serie;
import org.shanoir.uploader.nominativeData.NominativeDataUploadJob;
import org.shanoir.uploader.nominativeData.NominativeDataUploadJobManager;
import org.shanoir.uploader.upload.UploadJob;
import org.shanoir.uploader.upload.UploadJobManager;
import org.shanoir.uploader.upload.UploadState;
import org.shanoir.uploader.utils.ImportUtils;
import org.shanoir.uploader.utils.Util;

/**
 * This class downloads the files from the PACS or copies
 * them from the CD/DVD to an upload folder and creates the
 * upload-job.xml.
 * 
 * @author mkain
 *
 */
public class DownloadOrCopyRunnable implements Runnable {

	private static final String SERIES = "SERIES";

	private static Logger logger = Logger.getLogger(DownloadOrCopyRunnable.class);

	private static final String UNDERSCORE = "_";
	
	private boolean isFromPACS;
	
	private IDicomServerClient dicomServerClient;
	
	private String filePathDicomDir;

	private Set<org.shanoir.uploader.dicom.Serie> selectedSeries;

	private DicomDataTransferObject dicomData;
	
	public DownloadOrCopyRunnable(boolean isFromPACS, final IDicomServerClient dicomServerClient, final String filePathDicomDir,
		final Set<org.shanoir.uploader.dicom.Serie> selectedSeries, final DicomDataTransferObject dicomData) {
		this.isFromPACS = isFromPACS;
		this.dicomServerClient = dicomServerClient; // used with PACS import
		if(!isFromPACS && filePathDicomDir != null) {
			this.filePathDicomDir = new String(filePathDicomDir); // used with CD/DVD import
		}
		this.selectedSeries = selectedSeries;
		this.dicomData = dicomData;
	}

	@Override
	public void run() {
		/**
		 * 1. Download from PACS or copy from CD/DVD
		 */
		File uploadFolder = ImportUtils.createUploadFolder(dicomServerClient.getWorkFolder(), dicomData);
		List<String> allFileNames = ImportUtils.downloadOrCopyFilesIntoUploadFolder(this.isFromPACS, selectedSeries, uploadFolder, dicomServerClient, filePathDicomDir);
		
		/**
		 * 2. Fill MRI information into serie from first DICOM file of each serie
		 * This has already been done for CD/DVD import, but not yet here for PACS
		 */
		for (Iterator iterator = selectedSeries.iterator(); iterator.hasNext();) {
			Serie serie = (Serie) iterator.next();
			Util.processSerieMriInfo(uploadFolder, serie);
		}
			
		/**
		 * 3. Write the UploadJob and schedule upload
		 */
		UploadJob uploadJob = new UploadJob();
		ImportUtils.initUploadJob(selectedSeries, dicomData, uploadJob);
		if (allFileNames == null) {
			uploadJob.setUploadState(UploadState.ERROR);
		}
		UploadJobManager uploadJobManager = new UploadJobManager(uploadFolder.getAbsolutePath());
		uploadJobManager.writeUploadJob(uploadJob);

		/**
		 * 4. Write the NominativeDataUploadJobManager for displaying the download state
		 */
		NominativeDataUploadJob dataJob = new NominativeDataUploadJob();
		ImportUtils.initDataUploadJob(selectedSeries, dicomData, dataJob);
		if (allFileNames == null) {
			dataJob.setUploadState(UploadState.ERROR);
		}
		NominativeDataUploadJobManager uploadDataJobManager = new NominativeDataUploadJobManager(
				uploadFolder.getAbsolutePath());
		uploadDataJobManager.writeUploadDataJob(dataJob);
		ShUpOnloadConfig.getCurrentNominativeDataController().addNewNominativeData(uploadFolder, dataJob);
		
		logger.info(uploadFolder.getName() + ": finished: " + toString());
	}

	@Override
	public String toString() {
		return "DownloadOrCopyRunnable [isFromPACS=" + isFromPACS + ", dicomServerClient=" + dicomServerClient
				+ ", filePathDicomDir=" + filePathDicomDir + ", selectedSeries=" + selectedSeries + ", dicomData="
				+ dicomData.toString() + "]";
	}

}

package org.shanoir.uploader.action;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;
import org.shanoir.dicom.importer.Serie;
import org.shanoir.dicom.importer.UploadJob;
import org.shanoir.dicom.importer.UploadJobManager;
import org.shanoir.dicom.importer.UploadState;
import org.shanoir.uploader.ShUpOnloadConfig;
import org.shanoir.uploader.dicom.IDicomServerClient;
import org.shanoir.uploader.nominativeData.NominativeDataUploadJob;
import org.shanoir.uploader.nominativeData.NominativeDataUploadJobManager;
import org.shanoir.uploader.utils.ImportUtils;
import org.shanoir.uploader.utils.Util;
import org.shanoir.util.file.FileUtil;

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

	private Set<org.shanoir.dicom.importer.Serie> selectedSeries;

	private DicomDataTransferObject dicomData;
	
	public DownloadOrCopyRunnable(boolean isFromPACS, final IDicomServerClient dicomServerClient, final String filePathDicomDir,
		final Set<org.shanoir.dicom.importer.Serie> selectedSeries, final DicomDataTransferObject dicomData) {
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
	
	private void copyFilesToUploadFolderInSeriesFolder(Set<org.shanoir.dicom.importer.Serie> selectedSeries, final File uploadFolder) {
		final File seriesFolder = new File(uploadFolder, SERIES);
		seriesFolder.mkdirs();
		for (org.shanoir.dicom.importer.Serie serie : selectedSeries) {
			final File serieIdFolder = new File(seriesFolder, serie.getId());
			serieIdFolder.mkdirs();
			List<String> newFileNamesOfSerie = new ArrayList<String>();
			List<String> oldFileNamesOfSerie = serie.getFileNames();
			for (Iterator iterator = oldFileNamesOfSerie.iterator(); iterator.hasNext();) {
				String dicomFileName = (String) iterator.next();
				File sourceFile = new File(filePathDicomDir + File.separator + dicomFileName);
				dicomFileName = dicomFileName.replace(File.separator, UNDERSCORE);
				File destFile = new File(serieIdFolder, dicomFileName);
				FileUtil.copyFile(sourceFile, destFile);
				newFileNamesOfSerie.add(dicomFileName);
			}
			serie.setFileNames(newFileNamesOfSerie);
		}
	}

	@Override
	public String toString() {
		return "DownloadOrCopyRunnable [isFromPACS=" + isFromPACS + ", dicomServerClient=" + dicomServerClient
				+ ", filePathDicomDir=" + filePathDicomDir + ", selectedSeries=" + selectedSeries + ", dicomData="
				+ dicomData.toString() + "]";
	}

}

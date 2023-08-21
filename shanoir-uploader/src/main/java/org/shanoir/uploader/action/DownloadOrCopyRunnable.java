package org.shanoir.uploader.action;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;
import org.shanoir.ng.importer.dicom.ImagesCreatorAndDicomFileAnalyzerService;
import org.shanoir.uploader.ShUpOnloadConfig;
import org.shanoir.uploader.dicom.IDicomServerClient;
import org.shanoir.uploader.dicom.query.SerieTreeNode;
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

	private static Logger logger = Logger.getLogger(DownloadOrCopyRunnable.class);
	
	private boolean isFromPACS;
	
	private IDicomServerClient dicomServerClient;
	
	private ImagesCreatorAndDicomFileAnalyzerService dicomFileAnalyzer = new ImagesCreatorAndDicomFileAnalyzerService();
	
	private String filePathDicomDir;

	private Set<SerieTreeNode> selectedSeries;

	private DicomDataTransferObject dicomData;
	
	public DownloadOrCopyRunnable(boolean isFromPACS, final IDicomServerClient dicomServerClient, ImagesCreatorAndDicomFileAnalyzerService dicomFileAnalyzer, final String filePathDicomDir,
		final Set<SerieTreeNode> selectedSeries, final DicomDataTransferObject dicomData) {
		this.isFromPACS = isFromPACS;
		this.dicomFileAnalyzer = dicomFileAnalyzer;
		this.dicomServerClient = dicomServerClient; // used with PACS import
		if(!isFromPACS && filePathDicomDir != null) {
			this.filePathDicomDir = new String(filePathDicomDir); // used with CD/DVD import
		}
		this.selectedSeries = selectedSeries;
		this.dicomData = dicomData;
	}

	@Override
	public void run() {
		File uploadFolder = ImportUtils.createUploadFolder(dicomServerClient.getWorkFolder(), dicomData);
		List<String> allFileNames = null;
		try {
			/**
			 * 1. Download from PACS or copy from CD/DVD/local file system
			 */
			allFileNames = ImportUtils.downloadOrCopyFilesIntoUploadFolder(this.isFromPACS, selectedSeries, uploadFolder, dicomFileAnalyzer, dicomServerClient, filePathDicomDir);
		
			/**
			 * 2. Fill MRI information into serie from first DICOM file of each serie
			 * This has already been done for CD/DVD import, but not yet here for PACS
			 */
			if (this.isFromPACS) {
				for (Iterator iterator = selectedSeries.iterator(); iterator.hasNext();) {
					SerieTreeNode serieTreeNode = (SerieTreeNode) iterator.next();
					dicomFileAnalyzer.getAdditionalMetaDataFromFirstInstanceOfSerie(filePathDicomDir, serieTreeNode.getSerie(), null, isFromPACS);
				}
			}
		} catch (FileNotFoundException e) {
			logger.error(e.getMessage(), e);
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

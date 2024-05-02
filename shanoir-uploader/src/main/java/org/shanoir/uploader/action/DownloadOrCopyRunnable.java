package org.shanoir.uploader.action;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.shanoir.ng.importer.dicom.ImagesCreatorAndDicomFileAnalyzerService;
import org.shanoir.ng.importer.model.ImportJob;
import org.shanoir.ng.importer.model.Serie;
import org.shanoir.uploader.ShUpOnloadConfig;
import org.shanoir.uploader.dicom.IDicomServerClient;
import org.shanoir.uploader.nominativeData.NominativeDataUploadJob;
import org.shanoir.uploader.nominativeData.NominativeDataUploadJobManager;
import org.shanoir.uploader.upload.UploadJob;
import org.shanoir.uploader.upload.UploadJobManager;
import org.shanoir.uploader.upload.UploadState;
import org.shanoir.uploader.utils.ImportUtils;
import org.shanoir.uploader.utils.Util;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class downloads the files from the PACS or copies
 * them from the CD/DVD to an upload folder and creates the
 * upload-job.xml.
 * 
 * @author mkain
 *
 */
public class DownloadOrCopyRunnable implements Runnable {

	private static final Logger logger = LoggerFactory.getLogger(DownloadOrCopyRunnable.class);

	public static final String IMPORT_JOB_JSON = "import-job.json";
	
	private boolean isFromPACS;
	
	private IDicomServerClient dicomServerClient;
	
	private ImagesCreatorAndDicomFileAnalyzerService dicomFileAnalyzer = new ImagesCreatorAndDicomFileAnalyzerService();
	
	private String filePathDicomDir;

	private Map<String, ImportJob> importJobs;
	
	public DownloadOrCopyRunnable(boolean isFromPACS, final IDicomServerClient dicomServerClient, ImagesCreatorAndDicomFileAnalyzerService dicomFileAnalyzer, final String filePathDicomDir, Map<String, ImportJob> importJobs) {
		this.isFromPACS = isFromPACS;
		this.dicomFileAnalyzer = dicomFileAnalyzer;
		this.dicomServerClient = dicomServerClient; // used with PACS import
		if(!isFromPACS && filePathDicomDir != null) {
			this.filePathDicomDir = new String(filePathDicomDir); // used with CD/DVD import
		}
		this.importJobs = importJobs;
	}

	@Override
	public void run() {
		for (String studyInstanceUID : importJobs.keySet()) {
			ImportJob importJob = importJobs.get(studyInstanceUID);
			File uploadFolder = ImportUtils.createUploadFolder(dicomServerClient.getWorkFolder(), importJob.getSubject().getIdentifier());
			List<Serie> selectedSeries = importJob.getSelectedSeries();
			List<String> allFileNames = null;
			try {
				/**
				 * 1. Download from PACS or copy from CD/DVD/local file system
				 */
				allFileNames = ImportUtils.downloadOrCopyFilesIntoUploadFolder(
					this.isFromPACS, studyInstanceUID, selectedSeries, uploadFolder, dicomFileAnalyzer, dicomServerClient, filePathDicomDir);
				/**
				 * 2. Fill MRI information into all series from first DICOM file of each serie
				 */
				for (Serie serie: selectedSeries) {
					dicomFileAnalyzer.getAdditionalMetaDataFromFirstInstanceOfSerie(uploadFolder.getAbsolutePath(), serie, null, isFromPACS);
				}
			} catch (FileNotFoundException e) {
				logger.error(e.getMessage(), e);
			}

			/**
			 * Write import-job.json to disk
			 */
			try {
				File importJobJson = new File(uploadFolder, IMPORT_JOB_JSON);
				importJobJson.createNewFile();
				Util.objectMapper.writeValue(importJobJson, importJob);
			} catch (IOException e) {
				logger.error(uploadFolder.getName() + ": " + e.getMessage(), e);
			}

			/**
			 * Write the UploadJob and schedule upload
			 */
			UploadJob uploadJob = new UploadJob();
			ImportUtils.initUploadJob(importJob, uploadJob);
			if (allFileNames == null) {
				uploadJob.setUploadState(UploadState.ERROR);
			}
			UploadJobManager uploadJobManager = new UploadJobManager(uploadFolder.getAbsolutePath());
			uploadJobManager.writeUploadJob(uploadJob);

			/**
			 * Write the NominativeDataUploadJobManager for displaying the download state
			 */
			NominativeDataUploadJob dataJob = new NominativeDataUploadJob();
			ImportUtils.initDataUploadJob(importJob, uploadJob, dataJob);
			if (allFileNames == null) {
				dataJob.setUploadState(UploadState.ERROR);
			}
			NominativeDataUploadJobManager uploadDataJobManager = new NominativeDataUploadJobManager(
					uploadFolder.getAbsolutePath());
			uploadDataJobManager.writeUploadDataJob(dataJob);
			ShUpOnloadConfig.getCurrentNominativeDataController().addNewNominativeData(uploadFolder, dataJob);
			logger.info(uploadFolder.getName() + ": finished for DICOM study: " + importJob.getStudy().getStudyDescription()
				+ ", " + importJob.getStudy().getStudyDate() + " of patient: " + importJob.getPatient().getPatientName());
		}
	}

}

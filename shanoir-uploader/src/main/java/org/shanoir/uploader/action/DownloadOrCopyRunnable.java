package org.shanoir.uploader.action;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

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
 * This class downloads as a separate thread the DICOM files from the PACS
 * OR copies the DICOM files from the CD/DVD/local file system to an upload
 * folder.
 * Multiple DICOM-studies/exams are managed within one thread, each as an
 * ImportJob. This class creates the import-job.json (and upload-job.xml +
 * nominative-upload-job.xml for legacy reasons). The .xmls will be removed
 * later.
 * 
 * @author mkain
 *
 */
public class DownloadOrCopyRunnable implements Runnable {

	private static final Logger logger = LoggerFactory.getLogger(DownloadOrCopyRunnable.class);

	public static final String IMPORT_JOB_JSON = "import-job.json";

	private boolean isFromPACS;

	private boolean isTableImport;

	private IDicomServerClient dicomServerClient;

	private ImagesCreatorAndDicomFileAnalyzerService dicomFileAnalyzer;

	private String filePathDicomDir;

	private Map<String, ImportJob> importJobs;

	private JFrame frame;

	private JProgressBar downloadProgressBar;

	public DownloadOrCopyRunnable(boolean isFromPACS, boolean isTableImport, JFrame frame, JProgressBar downloadProgressBar,
			final IDicomServerClient dicomServerClient, ImagesCreatorAndDicomFileAnalyzerService dicomFileAnalyzer,
			final String filePathDicomDir, Map<String, ImportJob> importJobs) {
		this.isFromPACS = isFromPACS;
		this.isTableImport = isTableImport;
		this.frame = frame;
		this.downloadProgressBar = downloadProgressBar;
		this.dicomFileAnalyzer = dicomFileAnalyzer;
		this.dicomServerClient = dicomServerClient; // used with PACS import
		if (!isFromPACS && filePathDicomDir != null) {
			this.filePathDicomDir = new String(filePathDicomDir); // used with CD/DVD import
		}
		this.importJobs = importJobs;
	}

	@Override
	public void run() {
		logger.info(importJobs.size() + " DICOM study(ies) selected for download or copy.");
		StringBuilder downloadOrCopyReportSummary = new StringBuilder();
		for (String studyInstanceUID : importJobs.keySet()) {
			StringBuilder downloadOrCopyReportPerStudy = new StringBuilder();
			ImportJob importJob = importJobs.get(studyInstanceUID);
			downloadOrCopyReportPerStudy.append("DICOM study: ["
				+ importJob.getStudy().getStudyDate() + "], "
				+ importJob.getStudy().getStudyDescription() + "\n");
			File uploadFolder = ImportUtils.createUploadFolder(dicomServerClient.getWorkFolder(),
					importJob.getSubject().getIdentifier());
			importJob.setWorkFolder(uploadFolder.getAbsolutePath());
			List<Serie> selectedSeries = new ArrayList<>(importJob.getSelectedSeries());
			downloadOrCopyReportPerStudy.append(selectedSeries.size() + " series selected for download or copy.\n\n");
			List<String> allFileNames = null;
			downloadProgressBar.setValue(0);
			try {
				/**
				 * 1. Download from PACS or copy from CD/DVD/local file system
				 */
				allFileNames = ImportUtils.downloadOrCopyFilesIntoUploadFolder(
						this.isFromPACS, downloadProgressBar, downloadOrCopyReportPerStudy, studyInstanceUID, selectedSeries,
						uploadFolder, dicomFileAnalyzer, dicomServerClient, filePathDicomDir);
				/**
				 * 2. Fill MRI information into all series from first DICOM file of each serie
				 */
				for (Serie serie : selectedSeries) {
					dicomFileAnalyzer.getAdditionalMetaDataFromFirstInstanceOfSerie(uploadFolder.getAbsolutePath(), null,
						importJob.getStudy(), serie, isFromPACS);
				}
			} catch (FileNotFoundException e) {
				logger.error(e.getMessage(), e);
				// as exception occured, we set allFileNames to null, to force ERROR state of import
				allFileNames = null;
			}

			/**
			 * Write the UploadJob
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
			logger.info(
					uploadFolder.getName() + ": finished for DICOM study: " + importJob.getStudy().getStudyDescription()
							+ ", " + importJob.getStudy().getStudyDate() + " of patient: "
							+ importJob.getPatient().getPatientName());

			/**
			 * Write import-job.json to disk and remove unnecessary DICOM information before
			 */
			importJob.setPatient(null);
			importJob.setStudy(null);
			try {
				File importJobJson = new File(uploadFolder, IMPORT_JOB_JSON);
				importJobJson.createNewFile();
				Util.objectMapper.writeValue(importJobJson, importJob);
			} catch (IOException e) {
				logger.error(uploadFolder.getName() + ": " + e.getMessage(), e);
			}
			downloadOrCopyReportSummary.append(downloadOrCopyReportPerStudy.toString() + "\n\n");
		}
		if (isTableImport) {
			logger.info(downloadOrCopyReportSummary.toString());
		} else {
			/**
			 * Display downloadOrCopy summary to user.
			 */
			JTextArea textArea = new JTextArea(downloadOrCopyReportSummary.toString());
			textArea.setEditable(false);
			textArea.setWrapStyleWord(true);
			textArea.setLineWrap(true);
			textArea.setCaretPosition(0);
			JScrollPane scrollPane = new JScrollPane(textArea);
			scrollPane.setPreferredSize(new java.awt.Dimension(650, 550));
			JOptionPane.showMessageDialog(
				frame,
				scrollPane,
				"Download or copy report",
				JOptionPane.INFORMATION_MESSAGE);
		}
	}

}

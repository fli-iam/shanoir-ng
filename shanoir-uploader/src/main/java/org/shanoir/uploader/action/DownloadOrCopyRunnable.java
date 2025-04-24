package org.shanoir.uploader.action;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import org.shanoir.ng.importer.dicom.ImagesCreatorAndDicomFileAnalyzerService;
import org.shanoir.ng.importer.model.ImportJob;
import org.shanoir.ng.importer.model.Serie;
import org.shanoir.ng.importer.model.UploadState;
import org.shanoir.uploader.ShUpConfig;
import org.shanoir.uploader.ShUpOnloadConfig;
import org.shanoir.uploader.dicom.IDicomServerClient;
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
			Set<Serie> selectedSeries = importJob.getSelectedSeries();
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
			 * Set the upload parameters of the importJob object
			 */
			importJob.setTimestamp(System.currentTimeMillis());

			if (allFileNames == null) {
				importJob.setUploadState(UploadState.ERROR);
			} else {
				importJob.setUploadState(UploadState.READY);
			}

			importJob.setUploadPercentage("");

			/**
			 * Write import-job.json to disk
			 */
			try {
				File importJobJson = new File(uploadFolder, ShUpConfig.IMPORT_JOB_JSON);
				importJobJson.createNewFile();
				Util.objectMapper.writeValue(importJobJson, importJob);
			} catch (IOException e) {
				logger.error(uploadFolder.getName() + ": " + e.getMessage(), e);
			}

			ShUpOnloadConfig.getCurrentNominativeDataController().addNewNominativeData(uploadFolder, importJob);
			logger.info(
					uploadFolder.getName() + ": finished for DICOM study: " + importJob.getStudy().getStudyDescription()
							+ ", " + importJob.getStudy().getStudyDate() + " of patient: "
							+ importJob.getPatient().getPatientName());

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

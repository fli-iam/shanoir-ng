package org.shanoir.uploader.action.init;

import java.awt.Dimension;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.UIManager;

import org.shanoir.ng.importer.model.ImportJob;
import org.shanoir.uploader.ShUpConfig;
import org.shanoir.uploader.ShUpOnloadConfig;
import org.shanoir.uploader.dicom.anonymize.Pseudonymizer;
import org.shanoir.uploader.exception.PseudonymusException;
import org.shanoir.uploader.gui.CurrentUploadsWindowTable;
import org.shanoir.uploader.gui.MainWindow;
import org.shanoir.uploader.gui.ShUpStartupDialog;
import org.shanoir.uploader.nominativeData.CurrentNominativeDataController;
import org.shanoir.uploader.nominativeData.DicomPushServiceJob;
import org.shanoir.uploader.nominativeData.NominativeDataImportJobManager;
import org.shanoir.uploader.upload.UploadServiceJob;
import org.shanoir.uploader.utils.ImportUtils;
import org.shanoir.uploader.utils.Util;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ReadyState implements State {

	private static final Logger logger = LoggerFactory.getLogger(ReadyState.class);

	@Autowired
	private CurrentNominativeDataController currentNominativeDataController;

	@Autowired
	private UploadServiceJob uploadServiceJob;

	@Autowired
	private DicomPushServiceJob dicomPushServiceJob;
	
	public void load(StartupStateContext context) throws IOException {
		ShUpStartupDialog shUpStartupDialog = context.getShUpStartupDialog();
		shUpStartupDialog.setVisible(false);
		shUpStartupDialog.dispose();
		// Init pseudonymizer
		File pseudonymusFolder = new File(ShUpOnloadConfig.getWorkFolder().getParentFile().getAbsolutePath() + File.separator + Pseudonymizer.PSEUDONYMUS_FOLDER);
		Pseudonymizer pseudonymizer = null;
		try {
			pseudonymizer = new Pseudonymizer(ShUpConfig.basicProperties.getProperty(ShUpConfig.MODE_PSEUDONYMUS_KEY_FILE), pseudonymusFolder.getAbsolutePath());
			ShUpOnloadConfig.setPseudonymizer(pseudonymizer);
		} catch (PseudonymusException e) {
			logger.error(e.getMessage(), e);
		}
		MainWindow frame = initJFrame();
		CurrentUploadsWindowTable cuw = CurrentUploadsWindowTable.getInstance(frame);
		currentNominativeDataController.configure(ShUpOnloadConfig.getWorkFolder(), cuw);
		ShUpOnloadConfig.setCurrentNominativeDataController(currentNominativeDataController);
		initNominativeDataFilesBeforeLaunchingJobs();
		dicomPushServiceJob.setDownloadOrCopyActionListener(frame);
	}

	/**
	 * Set the frame size and location, and make it visible.
	 * 
	 * @param frame
	 */
	private MainWindow initJFrame() {
		MainWindow frame = new MainWindow(ShUpOnloadConfig.getDicomServerClient(), ShUpConfig.shanoirUploaderFolder,
				ShUpOnloadConfig.getUrlConfig(), ShUpConfig.resourceBundle);
		frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		frame.addWindowListener(new WindowAdapter() {
			public void windowOpened(WindowEvent e) {
			}
			public void windowClosing(WindowEvent e) {
				if (uploadServiceJob.isUploading()) {
					String message = "ShanoirUploader is still uploading DICOM files. Are you sure to want to close?";
					UIManager.put("OptionPane.cancelButtonText", "Cancel");
					UIManager.put("OptionPane.noButtonText", "No");
					UIManager.put("OptionPane.okButtonText", "Ok");
					UIManager.put("OptionPane.yesButtonText", "Yes");
					if (JOptionPane.showConfirmDialog(null, message, "Confirmation", 1) == JOptionPane.YES_OPTION) {
						System.exit(0);
					}
				} else {
					System.exit(0);
				}
			}
		});
		frame.setPreferredSize(new Dimension(920, 910));
		frame.pack();
		frame.setLocationRelativeTo(null);
		frame.setVisible(true);
		logger.info("JFrame successfully initialized.");
		return frame;
	}
	
	/**
	 * Walk trough all folders within the work folder and update the upload
	 * percentage upload percentage < 100 % must be 0%
	 */
	private void initNominativeDataFilesBeforeLaunchingJobs() {
		final List<File> folders = Util.listFolders(ShUpOnloadConfig.getWorkFolder());
		logger.debug("Update Nominative DataFiles Before Closing " + folders.size() + " folders in work folder.");
		for (Iterator foldersIt = folders.iterator(); foldersIt.hasNext();) {
			NominativeDataImportJobManager dataJobManager = null;
			final File folder = (File) foldersIt.next();
			// initDataJobManager
			final Collection<File> files = Util.listFiles(folder, null, false);
			for (Iterator filesIt = files.iterator(); filesIt.hasNext();) {
				final File file = (File) filesIt.next();
				if (file.getName().equals(
						ShUpConfig.IMPORT_JOB_JSON)) {
					logger.debug(" Initializing data job manager before launching Jobs");
					dataJobManager = new NominativeDataImportJobManager(file);
				}
			}
			if (dataJobManager != null) {
				final ImportJob importJob = dataJobManager
						.readImportJob();
				// in case of previous importJobs (without uploadPercentage)
				// we look for uploadPercentage value from nominative-data-job.xml file
				if (importJob.getUploadPercentage() == null) {
					String percentage = ImportUtils.getUploadPercentageFromNominativeDataJob(importJob.getWorkFolder());
					importJob.setUploadPercentage(percentage);
				}
				String uploadPercentage = importJob.getUploadPercentage();
				logger.debug(" upload percentage before launching Jobs "
						+ uploadPercentage);
				if (!uploadPercentage.equals("100 %"))
					uploadPercentage = "0 %";
				logger.debug(" upload percentage initialized to "
						+ uploadPercentage);
						importJob.setUploadPercentage(uploadPercentage);
				dataJobManager.writeImportJob(importJob);
			} else {
				logger.error("Folder found in workFolder without import-job.json.");
			}
		}
	}
	
}

package org.shanoir.uploader.action.init;

import java.awt.Dimension;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.UIManager;

import org.apache.log4j.Logger;
import org.quartz.JobBuilder;
import org.quartz.JobExecutionContext;
import org.quartz.SchedulerException;
import org.quartz.SchedulerFactory;
import org.quartz.SimpleScheduleBuilder;
import org.quartz.TriggerBuilder;
import org.quartz.impl.StdSchedulerFactory;
import org.shanoir.uploader.ShUpConfig;
import org.shanoir.uploader.ShUpOnloadConfig;
import org.shanoir.uploader.gui.CurrentUploadsWindowTable;
import org.shanoir.uploader.gui.MainWindow;
import org.shanoir.uploader.gui.ShUpStartupDialog;
import org.shanoir.uploader.nominativeData.CurrentNominativeDataController;
import org.shanoir.uploader.nominativeData.NominativeDataUploadJob;
import org.shanoir.uploader.nominativeData.NominativeDataUploadJobManager;
import org.shanoir.uploader.upload.UploadServiceJob;
import org.shanoir.util.ShanoirUtil;

public class ReadyState implements State {

	private static Logger logger = Logger.getLogger(ReadyState.class);
	
	public void load(StartupStateContext context) {
		ShUpStartupDialog shUpStartupDialog = context.getShUpStartupDialog();
		shUpStartupDialog.setVisible(false);
		shUpStartupDialog.dispose();
		MainWindow frame = initJFrame();
		CurrentUploadsWindowTable cuw = new CurrentUploadsWindowTable(frame);
		ShUpOnloadConfig.setCurrentNominativeDataController(
				new CurrentNominativeDataController(ShUpOnloadConfig.getWorkFolder(), cuw));
		initUploadService();
		initNominativeDataFilesBeforeLaunchingJobs();
	}
	
	/**
	 * Initialize the UploadService.
	 */
	private void initUploadService() {
		try {
			try {
				ShUpOnloadConfig.getJobDataMapMain().put("uploadServiceClient", ShUpOnloadConfig.getShanoirUploaderServiceClient());					
				ShUpOnloadConfig.getJobDataMapMain().put("uploadServiceClientNG", ShUpOnloadConfig.getShanoirUploaderServiceClientNG());
				ShUpOnloadConfig.getJobDataMapMain().put("nominativeDataController", ShUpOnloadConfig.getCurrentNominativeDataController());
				ShUpOnloadConfig.setUploadServiceJob(JobBuilder.newJob(UploadServiceJob.class)
						.usingJobData(ShUpConfig.WORK_FOLDER,ShUpOnloadConfig.getWorkFolder().getAbsolutePath())
						.usingJobData(ShUpOnloadConfig.getJobDataMapMain())
						.withIdentity(ShUpConfig.UPLOAD_SERVICE_JOB).build());
				// specify the running period of the job
				ShUpOnloadConfig.setTrigger(TriggerBuilder
						.newTrigger()
						.withSchedule(
								SimpleScheduleBuilder
										.simpleSchedule()
										.withIntervalInSeconds(
												ShUpConfig.UPLOAD_SERVICE_INTERVAL)
										.repeatForever()).build());
				// schedule the job
				SchedulerFactory schFactory = new StdSchedulerFactory();
				ShUpOnloadConfig.setScheduler(schFactory.getScheduler());
				ShUpOnloadConfig.getScheduler().start();
				ShUpOnloadConfig.getScheduler().scheduleJob(ShUpOnloadConfig.getUploadServiceJob(), ShUpOnloadConfig.getTrigger());
				logger.info("UploadService successfully initialized.");
			} catch (SchedulerException e) {
				logger.error(e.getMessage(), e);
			}
		} catch (Exception e) {
			logger.error("shanoir uploader error starting wsdl uploadService", e);
		}
	}
	
	/**
	 * Walk trough all folders within the work folder and update the upload
	 * percentage upload percentage < 100 % must be 0%
	 */
	private void initNominativeDataFilesBeforeLaunchingJobs() {
		final List<File> folders = ShanoirUtil.listFolders(ShUpOnloadConfig.getWorkFolder());
		logger.info("Update Nominative DataFiles Before Closing " + folders.size() + " folders in work folder.");
		for (Iterator foldersIt = folders.iterator(); foldersIt.hasNext();) {
			NominativeDataUploadJobManager dataJobManager = null;
			final File folder = (File) foldersIt.next();
			// initDataJobManager
			final Collection<File> files = ShanoirUtil.listFiles(folder, null, false);
			for (Iterator filesIt = files.iterator(); filesIt.hasNext();) {
				final File file = (File) filesIt.next();
				if (file.getName().equals(
						NominativeDataUploadJobManager.NOMINATIVE_DATA_JOB_XML)) {
					logger.info(" Initializing data job manager before launching Jobs");
					dataJobManager = new NominativeDataUploadJobManager(file);
				}
			}
			if (dataJobManager != null) {
				final NominativeDataUploadJob uploadDataJob = dataJobManager
						.readUploadDataJob();
				String uploadPercentage = uploadDataJob.getUploadPercentage();
				logger.debug(" upload percentage before launching Jobs "
						+ uploadPercentage);
				if (!uploadPercentage.equals("100 %"))
					uploadPercentage = "0 %";
				logger.debug(" upload percentage initialized to "
						+ uploadPercentage);
				uploadDataJob.setUploadPercentage(uploadPercentage);
				dataJobManager.writeUploadDataJob(uploadDataJob);
			} else {
				logger.error("Folder found in workFolder without nominative-data-job.xml.");
			}
		}
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
				if (checkIfUploadServiceIsRunning()) {
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

	private boolean checkIfUploadServiceIsRunning() {
		List<JobExecutionContext> currentJobs = null;
		if (ShUpOnloadConfig.getScheduler() != null){
			try {
				currentJobs = ShUpOnloadConfig.getScheduler().getCurrentlyExecutingJobs();
				if (!currentJobs.isEmpty())
					return true;
			} catch (SchedulerException sE) {
				logger.error(sE.getMessage(), sE);
			}
		}
		return false;
	}
	
}

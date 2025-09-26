package org.shanoir.uploader.action;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.concurrent.locks.ReentrantLock;

import javax.swing.JOptionPane;

import org.shanoir.ng.exchange.imports.subject.IdentifierCalculator;
import org.shanoir.ng.importer.dicom.ImagesCreatorAndDicomFileAnalyzerService;
import org.shanoir.ng.importer.model.ImportJob;
import org.shanoir.ng.importer.model.Patient;
import org.shanoir.ng.importer.model.Subject;
import org.shanoir.uploader.dicom.IDicomServerClient;
import org.shanoir.uploader.dicom.anonymize.Pseudonymizer;
import org.shanoir.uploader.exception.PseudonymusException;
import org.shanoir.uploader.gui.MainWindow;
import org.shanoir.uploader.utils.ImportUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class implements the logic when the download or copy button is clicked.
 * 
 * @author mkain
 * 
 */
public class DownloadOrCopyActionListener implements ActionListener {

	private static final Logger logger = LoggerFactory.getLogger(DownloadOrCopyActionListener.class);

	private MainWindow mainWindow;
	private ResourceBundle resourceBundle;
	public Pseudonymizer pseudonymizer;
	public IdentifierCalculator identifierCalculator;
	
	// Introduced here to inject into DownloadOrCopyRunnable
	private IDicomServerClient dicomServerClient;
	
	private ImagesCreatorAndDicomFileAnalyzerService dicomFileAnalyzer;

	private final ReentrantLock lock = new ReentrantLock();

	public DownloadOrCopyActionListener(final MainWindow mainWindow, final Pseudonymizer pseudonymizer, final IDicomServerClient dicomServerClient, final ImagesCreatorAndDicomFileAnalyzerService dicomFileAnalyzer) {
		this.mainWindow = mainWindow;
		this.resourceBundle = mainWindow.resourceBundle;
		this.pseudonymizer = pseudonymizer;
		this.identifierCalculator = new IdentifierCalculator();
		this.dicomServerClient = dicomServerClient;
		this.dicomFileAnalyzer = dicomFileAnalyzer;
	}

	/**
	 * This method contains all the logic which is performed when the download from PACS
	 * or copy from CD/DVD button is clicked.
	 */
	public void actionPerformed(final ActionEvent event) {
		if (mainWindow.dicomTree == null) {
			return;
		}
		final Map<String, ImportJob> importJobs = mainWindow.getSAL().getImportJobs();
		for (ImportJob importJob : importJobs.values()) {
			if (importJob.getSelectedSeries() == null || importJob.getSelectedSeries().isEmpty()) {
				JOptionPane.showMessageDialog(mainWindow.frame,
				"No serie selected.",
				resourceBundle.getString("shanoir.uploader.select.error.title"),
				JOptionPane.ERROR_MESSAGE);
				return;
			}
		}

		/**
		 * 1. Read values from GUI, entered by user
		 */
		Patient patient = null;
		Patient firstPatient = null;
		Subject firstSubject = null;
		for (ImportJob importJob : importJobs.values()) {
			// for the moment: one patient verification, extend later for n-patient verification
			patient = adjustPatientWithPatientVerificationGUIValues(importJob.getPatient());
			if (firstPatient == null) {
				firstPatient = patient;
				try {
					firstSubject = ImportUtils.createSubjectFromPatient(patient, pseudonymizer, identifierCalculator);
					importJob.setSubject(firstSubject);
				} catch (PseudonymusException e) {
					logger.error(e.getMessage(), e);
					JOptionPane.showMessageDialog(mainWindow.frame,
							resourceBundle.getString("shanoir.uploader.systemErrorDialog.error.phv"),
							resourceBundle.getString("shanoir.uploader.select.error.title"),
							JOptionPane.ERROR_MESSAGE);
					return;
				} catch (UnsupportedEncodingException e) {
					logger.error(e.getMessage(), e);
					JOptionPane.showMessageDialog(mainWindow.frame,
							resourceBundle.getString("shanoir.uploader.systemErrorDialog.error.phv"),
							resourceBundle.getString("shanoir.uploader.select.error.title"),
							JOptionPane.ERROR_MESSAGE);
					return;
				} catch (NoSuchAlgorithmException e) {
					logger.error(e.getMessage(), e);
					JOptionPane.showMessageDialog(mainWindow.frame,
							resourceBundle.getString("shanoir.uploader.systemErrorDialog.error.phv"),
							resourceBundle.getString("shanoir.uploader.select.error.title"),
							JOptionPane.ERROR_MESSAGE);
					return;
				}
			} else {
				if (firstPatient.getPatientID().equals(patient.getPatientID())) {
					importJob.setSubject(firstSubject);
				} else {
					return; // multi-patient not yet implemented, stop here
				}
			}
		}
		
		/**
		 * 3. Download from PACS or copy from CD/DVD and write import-job.json
		 */
		final String filePathDicomDir = mainWindow.getFindDicomActionListener().getFilePathDicomDir();
		Runnable runnable = new DownloadOrCopyRunnable(mainWindow.isFromPACS, false, mainWindow.frame, mainWindow.downloadProgressBar, dicomServerClient, dicomFileAnalyzer,  filePathDicomDir, importJobs);
		if (lock.tryLock()) {
			try {
				Thread thread = new Thread(runnable);
				thread.start();
			} catch (Exception e) {
				logger.error("An error occured while running the thread.", e);
			} finally {
				lock.unlock();
			}
		} else {
			logger.warn("A previous thread is still running. Please wait until it is finished.");
		}
		
		// clear previous selection, but keep tree open in the tab
		mainWindow.isDicomObjectSelected = false;
		mainWindow.dicomTree.getSelectionModel().clearSelection();
	
		JOptionPane.showMessageDialog(mainWindow.frame,
			    resourceBundle.getString("shanoir.uploader.downloadOrCopy.confirmation.message"),
			    resourceBundle.getString("shanoir.uploader.downloadOrCopy.confirmation.title"),
			    JOptionPane.INFORMATION_MESSAGE);
	}

	/**
	 * This method reads the data entered by the user with the GUI
	 * and puts it into a Patient object to ajdust the already existing
	 * values coming from the DICOM, when the user clicks on the download or copy button.
	 * 
	 * @param Patient patient
	 * @return
	 */
	private Patient adjustPatientWithPatientVerificationGUIValues(Patient patient) {
		if (mainWindow.firstNameTF.getText().isEmpty()) {
			JOptionPane.showMessageDialog(mainWindow.frame,
					resourceBundle.getString("shanoir.uploader.import.start.firstname.empty"),
					resourceBundle.getString("shanoir.uploader.select.error.title"),
					JOptionPane.ERROR_MESSAGE);
			return null;
		}
		String firstName = mainWindow.firstNameTF.getText();
		if (mainWindow.lastNameTF.getText().isEmpty()) {
			JOptionPane.showMessageDialog(mainWindow.frame,
					resourceBundle.getString("shanoir.uploader.import.start.lastname.empty"),
					resourceBundle.getString("shanoir.uploader.select.error.title"),
					JOptionPane.ERROR_MESSAGE);
			return null;
		}
		String lastName = mainWindow.lastNameTF.getText();
		if (mainWindow.birthNameTF.getText().isEmpty()) {
			JOptionPane.showMessageDialog(mainWindow.frame,
					resourceBundle.getString("shanoir.uploader.import.start.birthname.empty"),
					resourceBundle.getString("shanoir.uploader.select.error.title"),
					JOptionPane.ERROR_MESSAGE);
			return null;
		}
		String birthName = mainWindow.birthNameTF.getText();
		String birthDate = mainWindow.birthDateTF.getText();
		return org.shanoir.uploader.utils.ImportUtils.adjustPatientWithPatientVerification(patient, firstName, lastName, birthName, birthDate);
	}

	public boolean isRunning() {
        return lock.isLocked();
    }

}

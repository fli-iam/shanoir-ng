package org.shanoir.uploader.action;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDate;
import java.util.Map;
import java.util.ResourceBundle;

import javax.swing.JOptionPane;

import org.shanoir.ng.exchange.imports.subject.IdentifierCalculator;
import org.shanoir.ng.importer.dicom.ImagesCreatorAndDicomFileAnalyzerService;
import org.shanoir.ng.importer.model.ImportJob;
import org.shanoir.ng.importer.model.PseudonymusHashValues;
import org.shanoir.ng.importer.model.Subject;
import org.shanoir.uploader.ShUpConfig;
import org.shanoir.uploader.dicom.IDicomServerClient;
import org.shanoir.uploader.dicom.anonymize.Pseudonymizer;
import org.shanoir.uploader.exception.PseudonymusException;
import org.shanoir.uploader.gui.MainWindow;
import org.shanoir.uploader.utils.Util;
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
	private Pseudonymizer pseudonymizer;
	private IdentifierCalculator identifierCalculator;
	
	// Introduced here to inject into DownloadOrCopyRunnable
	private IDicomServerClient dicomServerClient;
	
	private ImagesCreatorAndDicomFileAnalyzerService dicomFileAnalyzer;

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
		/**
		 * 1. Read values from GUI, entered by user
		 */
		Subject subject = createSubjectFromUserValuesInGUI();
		if (subject == null) {
			return;
		}
		/**
		 * 2. Generate subject identifier and hash values
		 */
		try {
			generateSubjectIdentifierAndHashValues(subject);
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
		
		/**
		 * 3. Download from PACS or copy from CD/DVD and write upload-job.xml + nominative-data-job.xml
		 */
		final Map<String, ImportJob> importJobs = mainWindow.getSAL().getImportJobs();
		// for the moment: one subject, all verified subject data are used with all studies
		for (ImportJob importJob : importJobs.values()) {
			importJob.setSubject(subject);
		}
		final String filePathDicomDir = mainWindow.getFindDicomActionListener().getFilePathDicomDir();
		Runnable runnable = new DownloadOrCopyRunnable(mainWindow.isFromPACS, dicomServerClient, dicomFileAnalyzer,  filePathDicomDir, importJobs);
		Thread thread = new Thread(runnable);
		thread.start();
		
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
	 * and puts it into a Subject object, when the user
	 * clicks on the download or copy button.
	 * 
	 * @param dicomData
	 * @return
	 */
	private Subject createSubjectFromUserValuesInGUI() {
		Subject subject = new Subject();
		LocalDate birthDate = Util.convertStringToLocalDate(mainWindow.birthDateTF.getText());
		subject.setBirthDate(birthDate);
		String sex = null;
		if (mainWindow.mSexR.isSelected())
			sex = "M";
		if (mainWindow.fSexR.isSelected())
			sex = "F";
		subject.setSex(sex);
		if (mainWindow.lastNameTF.getText().isEmpty()) {
			JOptionPane.showMessageDialog(mainWindow.frame,
					resourceBundle.getString("shanoir.uploader.import.start.lastname.empty"),
					resourceBundle.getString("shanoir.uploader.select.error.title"),
					JOptionPane.ERROR_MESSAGE);
			return null;
		}
		String lastName = mainWindow.lastNameTF.getText();
		subject.setLastName(lastName);
		if (mainWindow.firstNameTF.getText().isEmpty()) {
			JOptionPane.showMessageDialog(mainWindow.frame,
					resourceBundle.getString("shanoir.uploader.import.start.firstname.empty"),
					resourceBundle.getString("shanoir.uploader.select.error.title"),
					JOptionPane.ERROR_MESSAGE);
			return null;
		}
		String firstName = mainWindow.firstNameTF.getText();
		subject.setFirstName(firstName);
		if (mainWindow.birthNameTF.getText().isEmpty()) {
			JOptionPane.showMessageDialog(mainWindow.frame,
					resourceBundle.getString("shanoir.uploader.import.start.birthname.empty"),
					resourceBundle.getString("shanoir.uploader.select.error.title"),
					JOptionPane.ERROR_MESSAGE);
			return null;
		}
		String birthName = mainWindow.birthNameTF.getText();
		subject.setBirthName(birthName);
		return subject;
	}

	private void generateSubjectIdentifierAndHashValues(Subject subject) throws PseudonymusException, UnsupportedEncodingException, NoSuchAlgorithmException {
		String identifier = null;
		// OFSEP mode
		if (ShUpConfig.isModePseudonymus()) {
			// use PseudonymusHashValues here
			pseudonymizer.createHashValuesWithPseudonymus(subject);
			PseudonymusHashValues pseudonymusHashValues = subject.getPseudonymusHashValues();
			identifier = identifierCalculator.calculateIdentifierWithHashs(pseudonymusHashValues.getFirstNameHash1(), pseudonymusHashValues.getBirthNameHash1(), pseudonymusHashValues.getBirthDateHash());
		// Neurinfo mode
		} else {
			String birthDate = Util.convertLocalDateToString(subject.getBirthDate());
			identifier = identifierCalculator.calculateIdentifier(subject.getFirstName(), subject.getLastName(), birthDate);
		}
		subject.setIdentifier(identifier);
	}

}

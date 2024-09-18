package org.shanoir.uploader.action;

import java.time.LocalDate;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

import javax.swing.JOptionPane;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.TreePath;

import org.shanoir.ng.importer.model.ImportJob;
import org.shanoir.ng.importer.model.Patient;
import org.shanoir.ng.importer.model.Serie;
import org.shanoir.ng.importer.model.Study;
import org.shanoir.uploader.ShUpConfig;
import org.shanoir.uploader.dicom.DicomTreeNode;
import org.shanoir.uploader.dicom.query.PatientTreeNode;
import org.shanoir.uploader.dicom.query.SerieTreeNode;
import org.shanoir.uploader.dicom.query.StudyTreeNode;
import org.shanoir.uploader.gui.MainWindow;
import org.shanoir.uploader.utils.ImportUtils;
import org.shanoir.uploader.utils.Util;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class implements the logic when a node is selected in a DicomTree.
 * The SelectionActionListener creates the multi-exam import jobs map, that
 * is used afterwards to know, what studies/series to import.
 * 
 * The SelectionActionListener is already prepared to allow multi-patient,
 * multi-exam (DICOM study) selections, to be ready for the future and not
 * have to refactor again this component.
 * 
 * @author yyao
 * @author mkain
 *
 */
public class SelectionActionListener implements TreeSelectionListener {

	private static final Logger logger = LoggerFactory.getLogger(SelectionActionListener.class);

	private MainWindow mainWindow;

	private ResourceBundle resourceBundle;

	private Map<String, ImportJob> importJobs;

	public SelectionActionListener(final MainWindow mainWindow, final ResourceBundle resourceBundle) {
		this.mainWindow = mainWindow;
		this.resourceBundle = resourceBundle;
	}

	/**
	 * This method contains all the logic which is performed when a node is selected
	 * in a DicomTree.
	 */
	public void valueChanged(TreeSelectionEvent event) {

		// clean up editPanel when value changed
		mainWindow.lastNameTF.setEnabled(false);
		mainWindow.birthNameCopyButton.setEnabled(false);
		mainWindow.firstNameTF.setEnabled(false);
		mainWindow.birthNameTF.setEnabled(false);
		mainWindow.birthDateTF.setEnabled(false);
		mainWindow.fSexR.setEnabled(false);
		mainWindow.mSexR.setEnabled(false);
		mainWindow.oSexR.setEnabled(false);
		mainWindow.lastNameTF.setText("");
		mainWindow.firstNameTF.setText("");
		mainWindow.birthNameTF.setText("");
		mainWindow.birthDateTF.setText("");

		mainWindow.isDicomObjectSelected = true;
		importJobs = new HashMap<String, ImportJob>();

		try {
			// returns all selected paths, which can be patients, studies and/or series
			TreePath[] paths = mainWindow.dicomTree.getSelectionModel().getSelectionPaths();
			if (paths != null && paths.length > 0) {
				for (int i = 0; i < paths.length; i++) {
					TreePath tp = paths[i];
					Object o = tp.getLastPathComponent();
					// handle if patient in paths has been found
					// implies: select all studies + series below
					if (o instanceof PatientTreeNode) {
						PatientTreeNode patientTreeNode = (PatientTreeNode) o;
						Collection<DicomTreeNode> studies = patientTreeNode.getTreeNodes().values();
						for (Iterator<DicomTreeNode> studiesIt = studies.iterator(); studiesIt.hasNext();) {
							StudyTreeNode studyTreeNode = (StudyTreeNode) studiesIt.next();
							handleStudyTreeNode(patientTreeNode, studyTreeNode, true);
						}
					}
					// handle if study in paths has been found
					// implies: select all series below
					if (o instanceof StudyTreeNode) {
						StudyTreeNode studyTreeNode = (StudyTreeNode) o;
						PatientTreeNode patientTreeNode = (PatientTreeNode) tp.getParentPath().getLastPathComponent();
						handleStudyTreeNode(patientTreeNode, studyTreeNode, true);
					}
					// handle if serie in paths has been found
					if (o instanceof SerieTreeNode) {
						SerieTreeNode serieTreeNode = (SerieTreeNode) o;
						StudyTreeNode studyTreeNode = (StudyTreeNode) tp.getParentPath().getLastPathComponent();
						PatientTreeNode patientTreeNode = (PatientTreeNode) tp.getParentPath().getParentPath().getLastPathComponent();
						handleStudyTreeNode(patientTreeNode, studyTreeNode, false);
						ImportJob importJob = importJobs.get(studyTreeNode.getStudy().getStudyInstanceUID());
						Serie serie = (Serie)serieTreeNode.getSerie();
						if (!serie.isIgnored() && !serie.isErroneous()) {
							importJob.getSelectedSeries().add((Serie)serie.clone());
						}
					}
				}
			}
		} catch (CloneNotSupportedException e) {
			logger.error(e.getMessage(), e);
		}

		if (!importJobs.isEmpty()) {
			displayPatientVerification();
		}
	}

	private void displayPatientVerification() {
		// for the moment use always first patient
		// idea: the selection listener is already multi-patient ready, but for
		// the moment we provide only one patient into the verification box
		ImportJob importJob = importJobs.values().iterator().next();
		Patient patient = importJob.getPatient();
		final String name = patient.getPatientName();
		String lastName = Util.computeLastName(name);
		String firstName = Util.computeFirstName(name);
		String sex = patient.getPatientSex();
		LocalDate birthDate = patient.getPatientBirthDate();
		mainWindow.lastNameTF.setText(lastName);
		mainWindow.lastNameTF.setEnabled(true);
		mainWindow.birthNameCopyButton.setEnabled(true);
		mainWindow.firstNameTF.setText(firstName);
		mainWindow.firstNameTF.setEnabled(true);
		mainWindow.birthNameTF.setText("");
		mainWindow.birthNameTF.setEnabled(true);
		mainWindow.birthDateTF.setEnabled(true);
		// add this exception here for damaged DICOMDIRs without birth date set
		if (birthDate != null) {
			String birthDateText = Util.convertLocalDateToString(birthDate);
			mainWindow.birthDateTF.setText(birthDateText);
		}
		if (sex != null) {
			if (sex.equals("F")) {
				mainWindow.fSexR.setSelected(true);
			}
			if (sex.equals("M")) {
				mainWindow.mSexR.setSelected(true);
			}
			if (sex.equals("O")) {
				mainWindow.oSexR.setSelected(true);
			}
		}
	}

	private void handleStudyTreeNode(PatientTreeNode patientTreeNode, StudyTreeNode studyTreeNode, boolean addAllSeries) throws CloneNotSupportedException {
		Patient patient = patientTreeNode.getPatient();
		Study study = studyTreeNode.getStudy();
		LocalDate studyDate = study.getStudyDate();
		if (studyDate == null) {			
			logger.error("Study date could not be used for import, study: " + study.getStudyDescription());
			if(mainWindow != null) {
				JOptionPane.showMessageDialog(mainWindow.frame,
				    "Study date could not be used for import: " + study.getStudyDescription(),
				    "Data error",
				    JOptionPane.ERROR_MESSAGE);
			}
			return;
		}
		String studyInstanceUID = study.getStudyInstanceUID();
		ImportJob importJob = importJobs.get(studyInstanceUID);
		if (importJob == null) {
			importJob = ImportUtils.createNewImportJob(patient, study);
			if(addAllSeries) {
				List<org.shanoir.ng.importer.model.Study> studies = patient.getStudies();
				for (org.shanoir.ng.importer.model.Study studyOfAllStudies : studies) {
					// only select concerned study, not all studies
					if (studyOfAllStudies.getStudyInstanceUID().equals(studyInstanceUID)) {
						List<Serie> series = study.getSeries();
						for (Serie serie : series) {
							if (!serie.isIgnored() && !serie.isErroneous()) {
								importJob.getSelectedSeries().add((Serie)serie.clone());
							}
						}
					}
				}
			}
			importJobs.put(studyInstanceUID, importJob);
		}
	}
	
	public Map<String, ImportJob> getImportJobs() {
		return importJobs;
	}

}

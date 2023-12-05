/**
 * 
 */
package org.shanoir.uploader.action;

import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Set;

import javax.swing.JOptionPane;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.TreePath;

import org.apache.log4j.Logger;
import org.shanoir.uploader.dicom.DicomTreeNode;
import org.shanoir.uploader.dicom.query.PatientTreeNode;
import org.shanoir.uploader.dicom.query.SerieTreeNode;
import org.shanoir.uploader.dicom.query.StudyTreeNode;
import org.shanoir.uploader.gui.MainWindow;
import org.shanoir.uploader.utils.Util;

/**
 * This class implements the logic when a node is selected in a DicomTree.
 * 
 * @author yyao
 *
 */
public class SelectionActionListener implements TreeSelectionListener {

	private static Logger logger = Logger.getLogger(SelectionActionListener.class);

	private static final SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");

	private MainWindow mainWindow;

	private ResourceBundle resourceBundle;

	private Set<SerieTreeNode> selectedSeries;

	private DicomDataTransferObject dicomData;

	public SelectionActionListener(final MainWindow mainWindow, final ResourceBundle resourceBundle) {
		this.mainWindow = mainWindow;
		this.resourceBundle = resourceBundle;
	}

	/**
	 * This method contains all the logic which is performed when a node is selected
	 * in a DicomTree.
	 */
	public void valueChanged(TreeSelectionEvent event) {
		dicomData = null;

		// clean up editPanel when value changed
		mainWindow.lastNameTF.setEnabled(false);
		mainWindow.birthNameCopyButton.setEnabled(false);
		mainWindow.firstNameTF.setEnabled(false);
		mainWindow.birthNameTF.setEnabled(false);
		mainWindow.birthDateTF.setEnabled(false);
		mainWindow.mSexR.setEnabled(false);
		mainWindow.fSexR.setEnabled(false);
		mainWindow.lastNameTF.setText("");
		mainWindow.firstNameTF.setText("");
		mainWindow.birthNameTF.setText("");
		mainWindow.birthDateTF.setText("");

		mainWindow.isDicomObjectSelected = true;
		selectedSeries = new LinkedHashSet<SerieTreeNode>();

		// check if multiple subject have been selected and show error
		PatientTreeNode patientAlreadySelected = null;
		// check if multiple studies have been selected and show error
		Boolean oneStudyIsSelected = true;
		// save the correct study selected
		StudyTreeNode selectedStudy = null;
		// returns all selected paths, which can be patients, studies and/or series
		TreePath[] paths = mainWindow.dicomTree.getSelectionModel().getSelectionPaths();
		if (paths != null && paths.length > 0) {
			for (int i = 0; i < paths.length; i++) {
				TreePath tp = paths[i];
				Object o = tp.getLastPathComponent();
				PatientTreeNode patient = null;
				// handle if patient in paths has been found
				if (o instanceof PatientTreeNode) {
					patient = (PatientTreeNode) o;
					Collection<DicomTreeNode> studies = patient.getTreeNodes().values();
					int nbStudiesSelected = 0;
					for (Iterator studiesIt = studies.iterator(); studiesIt.hasNext();) {
						nbStudiesSelected++;
						StudyTreeNode study = (StudyTreeNode) studiesIt.next();
						addSeriesForStudy(selectedSeries, study);
					}
					if (nbStudiesSelected != 1)
						oneStudyIsSelected = false;
					selectedStudy = (StudyTreeNode) patient.getFirstTreeNode();
				}
				// handle if study in paths has been found
				if (o instanceof StudyTreeNode) {
					StudyTreeNode study = (StudyTreeNode) o;
					addSeriesForStudy(selectedSeries, study);
					patient = (PatientTreeNode) tp.getParentPath().getLastPathComponent();
					if (selectedStudy == null)
						selectedStudy = study;
					else
						oneStudyIsSelected = false;
				}
				// handle if serie in paths has been found
				if (o instanceof SerieTreeNode) {
					patient = (PatientTreeNode) tp.getParentPath().getParentPath().getLastPathComponent();
					SerieTreeNode serieTreeNode = (SerieTreeNode) o;
					Collection<DicomTreeNode> studies = patient.getTreeNodes().values();
					for (Iterator studiesIt = studies.iterator(); studiesIt.hasNext();) {
						StudyTreeNode study = (StudyTreeNode) studiesIt.next();
						for (Iterator studySeries = study.getChildren(); studySeries.hasNext();) {
							Map.Entry pair = (Map.Entry) studySeries.next();
							SerieTreeNode s = (SerieTreeNode) pair.getValue();
							if (s.equals(o)) {
								if (selectedStudy == null || selectedStudy.equals(study))
									selectedStudy = study;
								else
									oneStudyIsSelected = false;
							}
						}

					}
					serieTreeNode.setSelected(true);
					selectedSeries.add(serieTreeNode);
				}
				if (patientAlreadySelected != null && !patientAlreadySelected.equals(patient)) {
					JOptionPane.showMessageDialog(mainWindow.frame,
							resourceBundle.getString("shanoir.uploader.select.error.message.subject"),
							resourceBundle.getString("shanoir.uploader.select.error.title"), JOptionPane.ERROR_MESSAGE);
					mainWindow.dicomTree.getSelectionModel().clearSelection();
					return;
				} else {
					patientAlreadySelected = patient;
				}
				if (!oneStudyIsSelected) {
					JOptionPane.showMessageDialog(mainWindow.frame,
							resourceBundle.getString("shanoir.uploader.select.error.message.study"),
							resourceBundle.getString("shanoir.uploader.select.error.title"), JOptionPane.ERROR_MESSAGE);
					mainWindow.dicomTree.getSelectionModel().clearSelection();
					return;
				}
			}
			try {
				dicomData = new DicomDataTransferObject(mainWindow, patientAlreadySelected, selectedStudy);
			} catch (Exception e) {
				logger.error(e.getMessage(), e);
				JOptionPane.showMessageDialog(mainWindow.frame,
						resourceBundle.getString("shanoir.uploader.select.error.message.subject"),
						resourceBundle.getString("shanoir.uploader.select.error.title"), JOptionPane.ERROR_MESSAGE);
				return;
			}

			if (dicomData != null) {
				mainWindow.lastNameTF.setText(dicomData.getLastName());
				mainWindow.lastNameTF.setEnabled(true);
				mainWindow.birthNameCopyButton.setEnabled(true);
				mainWindow.firstNameTF.setText(dicomData.getFirstName());
				mainWindow.firstNameTF.setEnabled(true);
				mainWindow.birthNameTF.setText(dicomData.getBirthName());
				mainWindow.birthNameTF.setEnabled(true);
				mainWindow.birthDateTF.setEnabled(true);
				mainWindow.mSexR.setEnabled(true);
				mainWindow.fSexR.setEnabled(true);
				// add this exception here for damaged DICOMDIRs without birth date set
				if (dicomData.getBirthDate() != null) {
					String birthDateText = Util.convertDicomDateToString(dicomData.getBirthDate());
					mainWindow.birthDateTF.setText(birthDateText);
				}
				if (dicomData.getSex() != null && dicomData.getSex().equals("M")) {
					mainWindow.mSexR.setSelected(true);
				}
				if (dicomData.getSex() != null && dicomData.getSex().equals("F")) {
					mainWindow.fSexR.setSelected(true);
				}
			}
		}
	}

	/**
	 * Add all series, if the study node has been selected.
	 * 
	 * @param selectedSeries
	 * @param study
	 */
	private void addSeriesForStudy(Set<SerieTreeNode> selectedSeries, StudyTreeNode study) {
		Collection<DicomTreeNode> series = study.getTreeNodes().values();
		for (Iterator seriesIt = series.iterator(); seriesIt.hasNext();) {
			SerieTreeNode serie = (SerieTreeNode) seriesIt.next();
			serie.setSelected(true);
			selectedSeries.add(serie);
		}
	}

	public Set<SerieTreeNode> getSelectedSeries() {
		return selectedSeries;
	}

	public void setSelectedSeries(Set<SerieTreeNode> selectedSeries) {
		this.selectedSeries = selectedSeries;
	}

	public DicomDataTransferObject getDicomData() {
		return dicomData;
	}

	public void setDicomData(DicomDataTransferObject dicomData) {
		this.dicomData = dicomData;
	}

}

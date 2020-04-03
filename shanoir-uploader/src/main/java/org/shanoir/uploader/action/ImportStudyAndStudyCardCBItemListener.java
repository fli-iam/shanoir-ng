package org.shanoir.uploader.action;

import java.awt.Color;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.Iterator;
import java.util.List;

import org.shanoir.uploader.ShUpConfig;
import org.shanoir.uploader.gui.MainWindow;
import org.shanoir.uploader.gui.customcomponent.JComboBoxMandatory;
import org.shanoir.uploader.model.Center;
import org.shanoir.uploader.model.Investigator;
import org.shanoir.uploader.model.Study;
import org.shanoir.uploader.model.StudyCard;
import org.shanoir.uploader.model.dto.SubjectDTO;
import org.shanoir.uploader.model.dto.SubjectStudyDTO;

/**
 * This listener reacts on the selections in the studyCB and studyCardCB.
 * When a study has been selected it updates the studyCard list and the
 * RelSubjectStudy, if existing or propose the edition of a new relatio.
 * The ImportDialog is used as "storage" point of the relation to be read
 * later by the ImportFinishActionListener.
 * 
 * @author mkain
 *
 */
public class ImportStudyAndStudyCardCBItemListener implements ItemListener {

	private MainWindow mainWindow;
	
	private SubjectDTO subjectDTO;
	
	private SubjectStudyDTO subjectStudyDTO;

	public ImportStudyAndStudyCardCBItemListener(MainWindow mainWindow, SubjectDTO subjectDTO) {
		this.mainWindow = mainWindow;
		this.subjectDTO = subjectDTO;
	}

	public void itemStateChanged(ItemEvent e) {
		int state = e.getStateChange();
		if (state == ItemEvent.SELECTED) {
			if (e.getSource().equals(mainWindow.importDialog.studyCB)) {
				Study study = (Study) e.getItem();
				updateStudyCards(study);
				updateSubjectStudy(study);
			}
			// the selection of the StudyCard and its center defines
			// the center for new created examinations
			if (e.getSource().equals(mainWindow.importDialog.studyCardCB)) {
				JComboBoxMandatory comboBox = (JComboBoxMandatory) e.getSource();
				StudyCard studyCard = (StudyCard) comboBox.getSelectedItem();
				// add center
				Center centerOfStudyCard = studyCard.getCenter();
				mainWindow.importDialog.mrExaminationCenterCB.removeAllItems();
				mainWindow.importDialog.mrExaminationCenterCB.addItem(centerOfStudyCard);
				// add investigators
				mainWindow.importDialog.mrExaminationExamExecutiveCB.removeAllItems();
				Study selectedStudy = (Study) mainWindow.importDialog.studyCB.getSelectedItem();
				List<Center> centersOfSelectedStudy = selectedStudy.getCenters();
				for (Center center : centersOfSelectedStudy) {
					if (center.equals(centerOfStudyCard)) {
						List<Investigator> investigators = center.getInvestigatorList();
						for (Investigator investigator : investigators) {
							mainWindow.importDialog.mrExaminationExamExecutiveCB.addItem(investigator);
						}
					}
				}
			}			
		} // ignore otherwise
	}

	private void updateStudyCards(Study study) {
		mainWindow.importDialog.studyCardCB.removeAllItems();
		if (study.getStudyCards() != null) {
			for (StudyCard studyCard : study.getStudyCards()) {
				mainWindow.importDialog.studyCardCB.addItem(studyCard);
			}
		}
	}

	private void updateSubjectStudy(Study study) {
		if (subjectDTO != null) {
			// Check if RelSubjectStudy exists for selected study
			List<SubjectStudyDTO> subjectStudyList = subjectDTO.getSubjectStudyList();
			if (subjectStudyList != null) {
				for (Iterator iterator = subjectStudyList.iterator(); iterator.hasNext();) {
					SubjectStudyDTO subjectStudyDTO = (SubjectStudyDTO) iterator.next();
					// subject is already in study: display values in GUI and stop editing
					if (subjectStudyDTO.getStudyId() == study.getId()) {
						mainWindow.importDialog.subjectStudyIdentifierTF.setText(subjectStudyDTO.getSubjectStudyIdentifier());
						mainWindow.importDialog.subjectStudyIdentifierTF.setEnabled(false);
						mainWindow.importDialog.subjectStudyIdentifierTF.setEditable(false);
						mainWindow.importDialog.subjectStudyIdentifierTF.setBackground(Color.LIGHT_GRAY);
						mainWindow.importDialog.subjectIsPhysicallyInvolvedCB.setSelected(subjectStudyDTO.isPhysicallyInvolved());
						mainWindow.importDialog.subjectIsPhysicallyInvolvedCB.setEnabled(false);
						mainWindow.importDialog.subjectTypeCB.setSelectedItem(subjectStudyDTO.getSubjectType());
						mainWindow.importDialog.subjectTypeCB.setEnabled(false);
						this.subjectStudyDTO = subjectStudyDTO;
						return;
					}
				}
			}
		}
		// subject is not in study, enable editing and display defaults
		if (ShUpConfig.isModeSubjectStudyIdentifier()) {
			mainWindow.importDialog.subjectStudyIdentifierTF.setEnabled(true);
			mainWindow.importDialog.subjectStudyIdentifierTF.setEditable(true);
			mainWindow.importDialog.subjectStudyIdentifierTF.setBackground(Color.WHITE);
		}
		mainWindow.importDialog.subjectStudyIdentifierTF.setText("");
		mainWindow.importDialog.subjectIsPhysicallyInvolvedCB.setEnabled(true);
		mainWindow.importDialog.subjectIsPhysicallyInvolvedCB.setSelected(true);
		mainWindow.importDialog.subjectTypeCB.setEnabled(true);
		mainWindow.importDialog.subjectTypeCB.setSelectedItem(ImportDialogOpener.subjectTypeValues[1]);
		this.subjectStudyDTO = null;
	}

	public SubjectStudyDTO getSubjectStudyDTO() {
		return subjectStudyDTO;
	}

	public void setSubjectStudyDTO(SubjectStudyDTO subjectStudyDTO) {
		this.subjectStudyDTO = subjectStudyDTO;
	}
	
}

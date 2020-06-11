package org.shanoir.uploader.action;

import java.awt.Color;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.Iterator;
import java.util.List;

import org.shanoir.uploader.ShUpConfig;
import org.shanoir.uploader.gui.MainWindow;
import org.shanoir.uploader.gui.customcomponent.JComboBoxMandatory;
import org.shanoir.uploader.model.rest.AcquisitionEquipment;
import org.shanoir.uploader.model.rest.IdName;
import org.shanoir.uploader.model.rest.Study;
import org.shanoir.uploader.model.rest.StudyCard;
import org.shanoir.uploader.model.rest.Subject;
import org.shanoir.uploader.model.rest.SubjectStudy;
import org.shanoir.uploader.model.rest.SubjectType;

public class ImportStudyAndStudyCardCBItemListenerNG implements ItemListener {

	private MainWindow mainWindow;
	
	private Subject subject;
	
	private SubjectStudy subjectStudy;

	public ImportStudyAndStudyCardCBItemListenerNG(MainWindow mainWindow, Subject subject) {
		this.mainWindow = mainWindow;
		this.subject = subject;
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
				// put center into exam using study card and acq equipment
				mainWindow.importDialog.mrExaminationCenterCB.removeAllItems();
				AcquisitionEquipment acqEquipment = studyCard.getAcquisitionEquipment();
				if (acqEquipment != null) {
					IdName center = acqEquipment.getCenter();
					mainWindow.importDialog.mrExaminationCenterCB.addItem(center);
				}
				// add investigators
				//mainWindow.importDialog.mrExaminationExamExecutiveCB.removeAllItems();
				//mainWindow.importDialog.mrExaminationExamExecutiveCB.addItem(investigator);				
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
		if (this.subject != null) {
			// Check if RelSubjectStudy exists for selected study
			List<SubjectStudy> subjectStudyList = subject.getSubjectStudyList();
			if (subjectStudyList != null) {
				for (Iterator iterator = subjectStudyList.iterator(); iterator.hasNext();) {
					SubjectStudy subjectStudy = (SubjectStudy) iterator.next();
					// subject is already in study: display values in GUI and stop editing
					if (subjectStudy.getStudy().getId() == study.getId()) {
						mainWindow.importDialog.subjectStudyIdentifierTF.setText(subjectStudy.getSubjectStudyIdentifier());
						mainWindow.importDialog.subjectStudyIdentifierTF.setBackground(Color.LIGHT_GRAY);
						mainWindow.importDialog.subjectStudyIdentifierTF.setEnabled(false);
						mainWindow.importDialog.subjectStudyIdentifierTF.setEditable(false);
						mainWindow.importDialog.subjectIsPhysicallyInvolvedCB.setSelected(subjectStudy.isPhysicallyInvolved());
						mainWindow.importDialog.subjectIsPhysicallyInvolvedCB.setEnabled(false);
						mainWindow.importDialog.subjectTypeCB.setSelectedItem(subjectStudy.getSubjectType());
						mainWindow.importDialog.subjectTypeCB.setEnabled(false);
						this.subjectStudy = subjectStudy;
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
		mainWindow.importDialog.subjectTypeCB.setSelectedItem(SubjectType.values()[1]);
		this.subjectStudy = null;
	}

	public SubjectStudy getSubjectStudy() {
		return subjectStudy;
	}

	public void setSubjectStudy(SubjectStudy subjectStudy) {
		this.subjectStudy = subjectStudy;
	}
	
}

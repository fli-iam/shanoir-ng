package org.shanoir.uploader.action;

import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;
import org.shanoir.uploader.gui.ImportDialog;
import org.shanoir.uploader.gui.customcomponent.JComboBoxMandatory;
import org.shanoir.uploader.model.Center;
import org.shanoir.uploader.model.Investigator;
import org.shanoir.uploader.model.Study;
import org.shanoir.uploader.model.StudyCard;

public class ImportStudyAndStudyCardCBItemListener implements ItemListener {

	private static Logger logger = Logger.getLogger(ImportStudyAndStudyCardCBItemListener.class);

	private ImportDialog importDialog;

	public ImportStudyAndStudyCardCBItemListener(ImportDialog importDialog) {
		this.importDialog = importDialog;
	}

	public void itemStateChanged(ItemEvent e) {
		int state = e.getStateChange();
		if (state == ItemEvent.SELECTED) {
			if (e.getSource().equals(importDialog.studyCB)) {
				Study study = (Study) e.getItem();
				importDialog.studyCardCB.removeAllItems();
				for (StudyCard studyCard : study.getStudyCards()) {
					importDialog.studyCardCB.addItem(studyCard);
				}
			}
			// the selection of the StudyCard and its center defines
			// the center for new created examinations
			if (e.getSource().equals(importDialog.studyCardCB)) {
				JComboBoxMandatory comboBox = (JComboBoxMandatory) e.getSource();
				StudyCard studyCard = (StudyCard) comboBox.getSelectedItem();
				// add center
				Center centerOfStudyCard = studyCard.getCenter();
				importDialog.mrExaminationCenterCB.removeAllItems();
				importDialog.mrExaminationCenterCB.addItem(centerOfStudyCard);
				// add investigators
				importDialog.mrExaminationExamExecutiveCB.removeAllItems();
				Study selectedStudy = (Study) importDialog.studyCB.getSelectedItem();
				List<Center> centersOfSelectedStudy = selectedStudy.getCenters();
				for (Center center : centersOfSelectedStudy) {
					if (center.equals(centerOfStudyCard)) {
						List<Investigator> investigators = center.getInvestigatorList();
						for (Investigator investigator : investigators) {
							importDialog.mrExaminationExamExecutiveCB.addItem(investigator);
						}
					}
				}
			}			
		} // ignore otherwise
	}

}

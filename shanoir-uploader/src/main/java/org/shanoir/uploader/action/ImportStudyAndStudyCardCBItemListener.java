package org.shanoir.uploader.action;

import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.List;

import org.apache.log4j.Logger;
import org.shanoir.uploader.gui.MainWindow;
import org.shanoir.uploader.gui.customcomponent.JComboBoxMandatory;
import org.shanoir.uploader.model.Center;
import org.shanoir.uploader.model.Investigator;
import org.shanoir.uploader.model.Study;
import org.shanoir.uploader.model.StudyCard;

public class ImportStudyAndStudyCardCBItemListener implements ItemListener {

	private static Logger logger = Logger.getLogger(ImportStudyAndStudyCardCBItemListener.class);

	private MainWindow mainWindow;

	public ImportStudyAndStudyCardCBItemListener(MainWindow mainWindow) {
		this.mainWindow = mainWindow;
	}

	public void itemStateChanged(ItemEvent e) {
		int state = e.getStateChange();
		if (state == ItemEvent.SELECTED) {
			if (e.getSource().equals(mainWindow.importDialog.studyCB)) {
				Study study = (Study) e.getItem();
				mainWindow.importDialog.studyCardCB.removeAllItems();
				if (study.getStudyCards() != null) {
					for (StudyCard studyCard : study.getStudyCards()) {
						mainWindow.importDialog.studyCardCB.addItem(studyCard);
					}
				}
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

}

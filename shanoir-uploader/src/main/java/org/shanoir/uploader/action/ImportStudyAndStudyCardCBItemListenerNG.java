package org.shanoir.uploader.action;

import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.List;

import org.apache.log4j.Logger;
import org.shanoir.uploader.gui.MainWindow;
import org.shanoir.uploader.gui.customcomponent.JComboBoxMandatory;
import org.shanoir.uploader.model.rest.AcquisitionEquipment;
import org.shanoir.uploader.model.rest.Center;
import org.shanoir.uploader.model.rest.IdName;
import org.shanoir.uploader.model.rest.Investigator;
import org.shanoir.uploader.model.rest.Study;
import org.shanoir.uploader.model.rest.StudyCard;

public class ImportStudyAndStudyCardCBItemListenerNG implements ItemListener {

	private static Logger logger = Logger.getLogger(ImportStudyAndStudyCardCBItemListenerNG.class);

	private MainWindow mainWindow;

	public ImportStudyAndStudyCardCBItemListenerNG(MainWindow mainWindow) {
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
				// put center into exam using study card and acq equipment
				AcquisitionEquipment acqEquipment = studyCard.getAcquisitionEquipment();
				IdName center = acqEquipment.getCenter();
				mainWindow.importDialog.mrExaminationCenterCB.removeAllItems();
				mainWindow.importDialog.mrExaminationCenterCB.addItem(center);
				// add investigators
				//mainWindow.importDialog.mrExaminationExamExecutiveCB.removeAllItems();
				//mainWindow.importDialog.mrExaminationExamExecutiveCB.addItem(investigator);				
			}			
		} // ignore otherwise
	}

}

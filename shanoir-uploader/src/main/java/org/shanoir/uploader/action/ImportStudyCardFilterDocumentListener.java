package org.shanoir.uploader.action;

import java.util.ArrayList;
import java.util.List;

import javax.swing.DefaultListModel;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import org.shanoir.uploader.gui.MainWindow;
import org.shanoir.uploader.model.rest.StudyCard;

public class ImportStudyCardFilterDocumentListener implements DocumentListener {

	private MainWindow mainWindow;
	
	private List<StudyCard> defaultStudyCards = new ArrayList<StudyCard>();

	public ImportStudyCardFilterDocumentListener(final MainWindow mainWindow) {
		this.mainWindow = mainWindow;
	}

	@Override
	public void insertUpdate(DocumentEvent e) {
		filter();
	}

	@Override
	public void removeUpdate(DocumentEvent e) {
		filter();
	}

	@Override
	public void changedUpdate(DocumentEvent e) {
	}

	private void filter() {
		String filter = mainWindow.importDialog.studyCardFilterTextField.getText();
		DefaultListModel<StudyCard> model = (DefaultListModel<StudyCard>) mainWindow.importDialog.studyCardCB.getModel();
		for (StudyCard studyCard : defaultStudyCards) {
            if (!studyCard.getName().contains(filter)) {
                if (model.contains(studyCard)) {
                    model.removeElement(studyCard);
                }
            } else {
                if (!model.contains(studyCard)) {
                    model.addElement(studyCard);
                }
            }
        }
	}

	public void addDefaultStudyCard(StudyCard defaultStudyCard) {
		this.defaultStudyCards.add(defaultStudyCard);
	}
	
	public void cleanDefaultStudyCards() {
		this.defaultStudyCards.clear();
	}

}

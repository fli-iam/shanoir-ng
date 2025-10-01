package org.shanoir.uploader.action;

import java.util.ArrayList;
import java.util.List;

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
        mainWindow.importDialog.studyCardCB.removeAllItems();
        String filter = mainWindow.importDialog.studyCardFilterTextField.getText();
        for (StudyCard studyCard : defaultStudyCards) {
            if (studyCard.toString().toLowerCase().contains(filter.toLowerCase())) {
                mainWindow.importDialog.studyCardCB.addItem(studyCard);
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

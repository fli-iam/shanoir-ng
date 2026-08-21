/**
 * Shanoir NG - Import, manage and share neuroimaging data
 * Copyright (C) 2009-2019 Inria - https://www.inria.fr/
 * Contact us on https://project.inria.fr/shanoir/
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see https://www.gnu.org/licenses/gpl-3.0.html
 */

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

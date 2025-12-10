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

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import org.shanoir.uploader.gui.MainWindow;
import org.shanoir.uploader.model.rest.Examination;
import org.shanoir.uploader.model.rest.ImagedObjectCategory;
import org.shanoir.uploader.model.rest.Subject;

public class ImportSubjectNameDocumentFilter implements DocumentListener {

    private MainWindow mainWindow;

    // Cache exams of latest selected subject
    private List<Examination> examinationsOfExistingSubject = new ArrayList<>();

    public ImportSubjectNameDocumentFilter(final MainWindow mainWindow) {
        this.mainWindow = mainWindow;
    }

    @Override
    public void insertUpdate(DocumentEvent e) {
        updateExistingExaminations();
    }

    @Override
    public void removeUpdate(DocumentEvent e) {
        updateExistingExaminations();
    }

    @Override
    public void changedUpdate(DocumentEvent e) {
    }

    private void updateExistingExaminations() {
        String subjectName = mainWindow.importDialog.subjectTextField.getText();
        if (subjectName.length() > 0) {
            // Store examinations in cache to bring them back
            int size = mainWindow.importDialog.mrExaminationExistingExamCB.getItemCount();
            for (int i = 0; i < size; i++) {
                Examination examination = (Examination) mainWindow.importDialog.mrExaminationExistingExamCB.getItemAt(i);
                examinationsOfExistingSubject.add(examination);
            }
            mainWindow.importDialog.existingSubjectsCB.setEnabled(false);
            // Clear subject information for new information
            mainWindow.importDialog.subjectTextField.setValueSet(false);
            mainWindow.importDialog.subjectImageObjectCategoryCB.setEnabled(true);
            mainWindow.importDialog.subjectImageObjectCategoryCB.setSelectedItem(ImagedObjectCategory.LIVING_HUMAN_BEING);
            mainWindow.importDialog.subjectLanguageHemisphericDominanceCB.setEnabled(true);
            mainWindow.importDialog.subjectLanguageHemisphericDominanceCB.setSelectedItem("");
            mainWindow.importDialog.subjectManualHemisphericDominanceCB.setEnabled(true);
            mainWindow.importDialog.subjectManualHemisphericDominanceCB.setSelectedItem("");
            mainWindow.importDialog.subjectPersonalCommentTextArea.setText("");
            mainWindow.importDialog.subjectPersonalCommentTextArea.setBackground(Color.WHITE);
            mainWindow.importDialog.subjectPersonalCommentTextArea.setEditable(true);
            ImportStudyAndStudyCardCBItemListener.updateImportDialogForExistingSubject(null, mainWindow.importDialog);
            // Clear examinations
            mainWindow.importDialog.mrExaminationExistingExamCB.removeAllItems();
            mainWindow.importDialog.mrExaminationExistingExamCB.setEnabled(false);
            mainWindow.importDialog.mrExaminationNewExamCB.setSelected(true);
        } else {
            mainWindow.importDialog.existingSubjectsCB.setEnabled(true);
            Subject subject = (Subject) mainWindow.importDialog.existingSubjectsCB.getSelectedItem();
            ImportStudyAndStudyCardCBItemListener.updateImportDialogForExistingSubject(subject, mainWindow.importDialog);
            // Update examinations and clear cache
            if (examinationsOfExistingSubject.isEmpty()) {
                mainWindow.importDialog.mrExaminationExistingExamCB.setEnabled(false);
                mainWindow.importDialog.mrExaminationNewExamCB.setSelected(true);
            } else {
                for (Examination examination : examinationsOfExistingSubject) {
                    mainWindow.importDialog.mrExaminationExistingExamCB.addItem(examination);
                }
                mainWindow.importDialog.mrExaminationExistingExamCB.setEnabled(true);
                mainWindow.importDialog.mrExaminationNewExamCB.setSelected(false);
            }
            examinationsOfExistingSubject.clear();
        }
    }

}

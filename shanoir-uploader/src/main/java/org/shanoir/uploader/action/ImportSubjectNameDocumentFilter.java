package org.shanoir.uploader.action;

import java.util.ArrayList;
import java.util.List;

import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import org.shanoir.uploader.gui.MainWindow;
import org.shanoir.uploader.model.rest.Examination;

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
            int size = mainWindow.importDialog.mrExaminationExistingExamCB.getItemCount();
            for (int i = 0; i < size; i++) {
                Examination examination = (Examination) mainWindow.importDialog.mrExaminationExistingExamCB.getItemAt(i);
                examinationsOfExistingSubject.add(examination);
            }
            mainWindow.importDialog.existingSubjectsCB.setEnabled(false);
            mainWindow.importDialog.mrExaminationExistingExamCB.removeAllItems();
            mainWindow.importDialog.mrExaminationExistingExamCB.setEnabled(false);
            mainWindow.importDialog.mrExaminationNewExamCB.setSelected(true);
        } else {
            mainWindow.importDialog.existingSubjectsCB.setEnabled(true);
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

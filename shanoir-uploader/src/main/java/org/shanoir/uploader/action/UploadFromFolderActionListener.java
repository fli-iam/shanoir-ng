package org.shanoir.uploader.action;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.ArrayList;

import javax.swing.JFileChooser;

import org.shanoir.uploader.gui.ImportFromFolderWindow;
import org.shanoir.uploader.model.ExaminationImport;
import org.shanoir.uploader.model.FolderImport;
import org.shanoir.uploader.model.rest.Study;
import org.shanoir.uploader.model.rest.StudyCard;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UploadFromFolderActionListener implements ActionListener {

    private static final Logger LOG = LoggerFactory.getLogger(UploadFromFolderActionListener.class);

    JFileChooser fileChooser;

    ImportFromFolderWindow importFromFolderWindow;

    public UploadFromFolderActionListener(ImportFromFolderWindow importFromFolderWindow) {
        this.importFromFolderWindow = importFromFolderWindow;
        this.fileChooser = new JFileChooser();
        fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        int result = fileChooser.showOpenDialog(importFromFolderWindow);
        if (result == JFileChooser.APPROVE_OPTION) {
            analyzeFile(fileChooser.getSelectedFile());
        }
        this.importFromFolderWindow.uploadButton.setVisible(true);
        this.importFromFolderWindow.uploadButton.setEnabled(true);
    }

    /**
     * Displays a CSV file, and checks its integrity
     * @param selectedFile the selected CSV file
     */
    private void analyzeFile(File selectedFile) {

        // Disable study / study card change
        this.importFromFolderWindow.studyCB.setEnabled(false);
        this.importFromFolderWindow.studyCardCB.setEnabled(false);

        FolderImport folderImport = new FolderImport();
        folderImport.setStudy(((Study) importFromFolderWindow.studyCB.getSelectedItem()));
        folderImport.setStudyCard(((StudyCard) importFromFolderWindow.studyCardCB.getSelectedItem()));

        folderImport.setExaminationImports(new ArrayList<>());
        // Check if it is a folder
        if (!selectedFile.isDirectory()) {
            this.importFromFolderWindow.displayError("ARG, file is not a folder");
            return;
        }
        // If it's a folder, check number of subjects
        if (selectedFile.listFiles().length == 0) {
            this.importFromFolderWindow.displayError("ARG, file is empty");
            return;
        }
        for (File subjectFile : selectedFile.listFiles()) {
            if (!subjectFile.isDirectory() || subjectFile.listFiles().length == 0) {
                LOG.info("Ignoring subject level file " + subjectFile.getName() + ", not a folder.");
                continue;
            }
            String subjectName = subjectFile.getName();
            for (File examinationFile : subjectFile.listFiles()) {
                ExaminationImport examinationImport = new ExaminationImport();
                examinationImport.setExamName(examinationFile.getName());
                examinationImport.setParent(folderImport);
                examinationImport.setPath(examinationFile.getAbsolutePath());
                examinationImport.setSubjectName(subjectName);
                examinationImport.setMessage("OK");

                folderImport.getExaminationImports().add(examinationImport);
            }
        }
        this.importFromFolderWindow.setFolderImport(folderImport);
        this.importFromFolderWindow.displayImports(folderImport);
    }

}

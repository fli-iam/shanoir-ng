package org.shanoir.uploader.action;

import org.shanoir.ng.importer.dicom.ImagesCreatorAndDicomFileAnalyzerService;
import org.shanoir.uploader.dicom.IDicomServerClient;
import org.shanoir.uploader.gui.ImportFromFolderWindow;
import org.shanoir.uploader.service.rest.ShanoirUploaderServiceClient;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.ResourceBundle;

public class ImportFromFolderActionListener implements ActionListener {
    ImportFromFolderWindow importFromFolderWindow;
    ImagesCreatorAndDicomFileAnalyzerService dicomFileAnalyzer;
    File shanoirUploaderFolder;
    IDicomServerClient dicomServerClient;
    ShanoirUploaderServiceClient shanoirUploaderServiceClientNG;
    private ResourceBundle resourceBundle;

    public ImportFromFolderActionListener(ImportFromFolderWindow importFromFolderWindow, ResourceBundle resourceBundle, IDicomServerClient dicomServerClient, ImagesCreatorAndDicomFileAnalyzerService dicomFileAnalyzer, File shanoirUploaderFolder, ShanoirUploaderServiceClient shanoirUploaderServiceClientNG) {
        this.importFromFolderWindow = importFromFolderWindow;
        this.dicomFileAnalyzer = dicomFileAnalyzer;
        this.shanoirUploaderFolder = shanoirUploaderFolder;
        this.shanoirUploaderServiceClientNG = shanoirUploaderServiceClientNG;
        this.resourceBundle = resourceBundle;
        this.dicomServerClient = dicomServerClient;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        ImportFromFolderRunner importer = new ImportFromFolderRunner(importFromFolderWindow.getFolderImport(), resourceBundle, importFromFolderWindow, dicomFileAnalyzer, shanoirUploaderServiceClientNG, dicomServerClient);
        importer.execute();
    }
}

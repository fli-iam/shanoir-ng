package org.shanoir.uploader.action;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Map;
import java.util.ResourceBundle;

import org.shanoir.ng.importer.dicom.ImagesCreatorAndDicomFileAnalyzerService;
import org.shanoir.ng.importer.model.ImportJob;
import org.shanoir.uploader.dicom.IDicomServerClient;
import org.shanoir.uploader.gui.ImportFromTableWindow;
import org.shanoir.uploader.service.rest.ShanoirUploaderServiceClient;

public class ImportFromTableActionListener implements ActionListener {

    private ImportFromTableWindow importFromTableWindow;
    private IDicomServerClient dicomServerClient;
    private ImagesCreatorAndDicomFileAnalyzerService dicomFileAnalyzer;

    private Map<String, ImportJob> importJobs;

    private ShanoirUploaderServiceClient shanoirUploaderServiceClientNG;
    private ResourceBundle resourceBundle;
    private DownloadOrCopyActionListener dOCAL;

    public ImportFromTableActionListener(ImportFromTableWindow importFromTableWindow, ResourceBundle resourceBundle, IDicomServerClient dicomServerClient, ImagesCreatorAndDicomFileAnalyzerService dicomFileAnalyzer, ShanoirUploaderServiceClient shanoirUploaderServiceClientNG, DownloadOrCopyActionListener dOCAL) {
        this.importFromTableWindow = importFromTableWindow;
        this.dicomServerClient = dicomServerClient;
        this.dicomFileAnalyzer = dicomFileAnalyzer;
        this.shanoirUploaderServiceClientNG = shanoirUploaderServiceClientNG;
        this.resourceBundle = resourceBundle;
        this.dOCAL = dOCAL;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        ImportFromTableRunner importer = new ImportFromTableRunner(importJobs, resourceBundle, importFromTableWindow, dicomServerClient, dicomFileAnalyzer, shanoirUploaderServiceClientNG, dOCAL);
        importer.execute();
    }

    public Map<String, ImportJob> getImportJobs() {
        return importJobs;
    }

    public void setImportJobs(Map<String, ImportJob> importJobs) {
        this.importJobs = importJobs;
    }

}

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

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Map;
import java.util.ResourceBundle;

import org.shanoir.ng.importer.dicom.ImagesCreatorAndDicomFileAnalyzerService;
import org.shanoir.ng.importer.model.ImportJob;
import org.shanoir.uploader.dicom.IDicomServerClient;
import org.shanoir.uploader.dicom.anonymize.Pseudonymizer;
import org.shanoir.uploader.gui.ImportFromTableWindow;
import org.shanoir.uploader.service.rest.ShanoirUploaderServiceClient;

public class ImportFromTableActionListener implements ActionListener {

    private ImportFromTableWindow importFromTableWindow;
    private IDicomServerClient dicomServerClient;
    private ImagesCreatorAndDicomFileAnalyzerService dicomFileAnalyzer;

    private Map<String, ImportJob> importJobs;

    private ShanoirUploaderServiceClient shanoirUploaderServiceClientNG;
    private ResourceBundle resourceBundle;
    private Pseudonymizer pseudonymizer;

    public ImportFromTableActionListener(ImportFromTableWindow importFromTableWindow, ResourceBundle resourceBundle, IDicomServerClient dicomServerClient, ImagesCreatorAndDicomFileAnalyzerService dicomFileAnalyzer, ShanoirUploaderServiceClient shanoirUploaderServiceClientNG, Pseudonymizer pseudonymizer) {
        this.importFromTableWindow = importFromTableWindow;
        this.dicomServerClient = dicomServerClient;
        this.dicomFileAnalyzer = dicomFileAnalyzer;
        this.shanoirUploaderServiceClientNG = shanoirUploaderServiceClientNG;
        this.resourceBundle = resourceBundle;
        this.pseudonymizer = pseudonymizer;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        ImportFromTableRunner importer = new ImportFromTableRunner(importJobs, resourceBundle, importFromTableWindow, dicomServerClient, dicomFileAnalyzer, shanoirUploaderServiceClientNG, pseudonymizer);
        importer.execute();
    }

    public Map<String, ImportJob> getImportJobs() {
        return importJobs;
    }

    public void setImportJobs(Map<String, ImportJob> importJobs) {
        this.importJobs = importJobs;
    }

}

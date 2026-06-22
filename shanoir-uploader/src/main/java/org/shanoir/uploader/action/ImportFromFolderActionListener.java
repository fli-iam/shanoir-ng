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

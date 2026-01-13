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

package org.shanoir.ng.importer.service;

import org.shanoir.ng.dicom.DIMSEService;
import org.shanoir.ng.dicom.web.service.DICOMWebService;
import org.shanoir.ng.importer.dto.Dataset;
import org.shanoir.ng.importer.dto.DatasetFile;
import org.shanoir.ng.importer.dto.ExpressionFormat;
import org.shanoir.ng.importer.dto.Serie;
import org.shanoir.ng.shared.exception.ShanoirException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.List;

/**
 * The DicomPersisterService sends DICOM images to the PACS for their permanent
 * storage in the PACS.
 *
 * According to the paper: Comparative performance investigation of DICOM
 * C-STORE and DICOM HTTP-based requests
 * https://www.researchgate.net/publication/270659008_Comparative_performance_investigation_of_DICOM_C-STORE_and_DICOM_HTTP-based_requests
 * the STOW is much more efficient in general than the C-STORE.
 *
 * My tests showed, that sending 160 images with stow-rs took 4 seconds and
 * sending the same 160 images with c-store took 3 seconds.
 *
 * @author mkain
 *
 */
@Service
@Scope("prototype")
public class DicomPersisterService {

    @Value("${dcm4chee-arc.dicom.web}")
    private boolean dicomWeb;

    @Autowired
    private DIMSEService dimseService;

    @Autowired
    private DICOMWebService dicomWebService;

    /**
     * This method reads the datasets for each serie from the json (String),
     * gets the dicom expression format and sends the images to the PACS.
     *
     * @param serie
     * @throws Exception
     */
    public void persistAllForSerie(Serie serie) throws Exception {
        if (serie == null) {
            return;
        }
        for (Dataset dataset : serie.getDatasets()) {
            for (ExpressionFormat expressionFormat : dataset.getExpressionFormats()) {
                if (!expressionFormat.getType().equals("dcm")) {
                    continue;
                }
                List<DatasetFile> datasetFiles = expressionFormat.getDatasetFiles();
                if (datasetFiles == null || datasetFiles.isEmpty()) {
                    throw new ShanoirException("Send Dicoms to Pacs: DatasetFiles null or empty.");
                }
                // Iterate over all dataset files (DICOM files), in case all files might be
                // distributed over multiple parent directories and therefore require multiple
                // calls to sendDicomFilesToPacs (advantage: do not create to big multiparts)
                File lastParentFile = null;
                for (DatasetFile datasetFile : datasetFiles) {
                    String path = datasetFile.getPath();
                    if (path == null || path.isEmpty()) {
                        throw new ShanoirException("Send Dicoms to Pacs: DatasetFile with null or empty path found.");
                    }
                    File dicomFile = new File(path);
                    if (!dicomFile.exists()) {
                        throw new ShanoirException("Send Dicoms to Pacs: dicomFile from DatasetFile.path not existing on disk.");
                    }
                    File parentFile = dicomFile.getParentFile();
                    if (!parentFile.equals(lastParentFile)) {
                        if (dicomWeb) {
                            dicomWebService.sendDicomFilesToPacs(parentFile);
                        } else {
                            dimseService.sendDicomFilesToPacs(parentFile);
                        }
                        lastParentFile = parentFile;
                    }
                }
            }
        }
    }

}

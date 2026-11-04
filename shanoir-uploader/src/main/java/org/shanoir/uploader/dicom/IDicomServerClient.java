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

package org.shanoir.uploader.dicom;

import java.io.File;
import java.util.List;

import javax.swing.JProgressBar;

import org.shanoir.ng.importer.dicom.query.DicomQuery;
import org.shanoir.ng.importer.model.Patient;
import org.shanoir.ng.importer.model.Serie;

public interface IDicomServerClient {

    /**
     * This method echos a DICOM server.
     * @return
     */
    public boolean echoDicomServer();

    public boolean echoDicomServer(String calledAET, String hostName, int port, String callingAET);

    /**
     * This method queries a DICOM server and returns a tree structured Media object.
     * @param patientName
     * @param patientID
     * @param studyDescription
     * @param seriesDescription
     * @return Media object representing a tree model structure or null
     */
    public List<Patient> queryDicomServer(boolean studyRootQuery, String modality, String patientName, String patientID,
            String studyDescription, String birthDate, String studyDate) throws Exception;

    public List<Patient> queryDicomServer(DicomQuery query) throws Exception;

    /**
     * This method initializes the download of DICOM files from the DICOM server.
     * After this query has been sent to the DICOM server, the DICOM server will
     * begin to send all files to a local server implemented in DcmRcvHelper.
     * @param selectedSeries
     */
    public List<String> retrieveDicomFiles(final JProgressBar progressBar, StringBuilder downloadOrCopyReport, String studyInstanceUID, List<Serie> selectedSeries, final File tempFolderForUpload);

    /**
     * This method returns the work folder.
     * @return
     */
    public File getWorkFolder();

}

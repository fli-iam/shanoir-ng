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

package org.shanoir.ng.processing.service;

import java.util.List;

import com.fasterxml.jackson.databind.JsonNode;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.shanoir.ng.examination.model.Examination;
import org.shanoir.ng.processing.model.DatasetProcessing;
import org.shanoir.ng.shared.exception.RestServiceException;


public interface ProcessingDownloaderService {

    /**
     * Download outputs (+ inputs if needed) of a processing list
     *
     * @param processingList list of the processings that have to be downloaded
     * @param resultOnly boolean describing if inputs must be downloaded too
     * @param format a potential format required, in that case a conversion has to be dine
     * @param response the HTTP in which create the download zip stream
     * @param withManifest a boolean describing if a summary of the files is required
     * @param converterId if format is ".nii", describes the converter to use
     *
     * @throws RestServiceException
     */
    void massiveDownload(List<DatasetProcessing> processingList, boolean resultOnly, String format, HttpServletResponse response, boolean withManifest, Long converterId) throws RestServiceException;

    /**
     * Download processing data according to a json parameter file
     *
     * @param jsonRequest the json parameter file
     *
     * @throws RestServiceException
     */
    void complexMassiveDownload(@Valid JsonNode jsonRequest) throws Exception;

    /**
     * Download outputs (+ inputs if needed) of an examination list
     *
     * @param examinationList list of the processings that have to be downloaded
     * @param processingComment a filter on processing comments
     * @param resultOnly boolean describing if inputs must be downloaded too
     * @param format a potential format required, in that case a conversion has to be dine
     * @param response the HTTP in which create the download zip stream
     * @param withManifest a boolean describing if a summary of the files is required
     * @param converterId if format is ".nii", describes the converter to use
     *
     * @throws RestServiceException
     */
    void massiveDownloadByExaminations(List<Examination> examinationList, String processingComment, boolean resultOnly, String format, HttpServletResponse response, boolean withManifest, Long converterId) throws RestServiceException;
}

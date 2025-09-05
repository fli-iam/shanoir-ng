package org.shanoir.ng.processing.service;

import java.util.List;
import java.util.zip.ZipOutputStream;

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
    void complexMassiveDownload(@Valid JsonNode jsonRequest, ZipOutputStream zipOutputStream) throws Exception;

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

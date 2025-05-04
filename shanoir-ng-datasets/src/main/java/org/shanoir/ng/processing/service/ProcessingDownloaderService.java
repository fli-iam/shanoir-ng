package org.shanoir.ng.processing.service;

import jakarta.servlet.http.HttpServletResponse;
import org.shanoir.ng.examination.model.Examination;
import org.shanoir.ng.processing.model.DatasetProcessing;
import org.shanoir.ng.shared.exception.RestServiceException;

import java.util.List;

public interface ProcessingDownloaderService {

    /**
     * Download datasets relative to a processing list and zip it in the HTTP response
     *
     * @param processingList the processing list to download
     * @param resultOnly filter the datasets to download, null for all inputs and outputs, all for all outputs, and any regex for specific named oupput (ex : "output.json")
     * @param format format in which download the datasets ("dcm", "nii")
     * @param response HTTP response in which zip archive is stored
     * @param withManifest if false, there is no subdirectory in the downloaded repository gathering the datasets, if true there are subdirectories and a table of content file
     * @param converterId Id of the converter to use if format = "nii"
     * @throws RestServiceException
     */
    void massiveDownload(List<DatasetProcessing> processingList, String resultOnly, String format, HttpServletResponse response, boolean withManifest, Long converterId) throws RestServiceException;

    /**
     * Download datasets relative to a processing list relative themselves to an examination list and zip it in the HTTP response
     *
     * @param examinationList the examination list in which the processings to download have occured
     * @param processingComment the processing descriptor to find in the examination list. Both and only those 2 parameters are usef for filtering the processing to download.
     * @param resultOnly filter the datasets to download, null for all inputs and outputs, all for all outputs, and any regex for specific named oupput (ex : "output.json")
     * @param format format in which download the datasets ("dcm", "nii")
     * @param response HTTP response in which zip archive is stored
     * @param withManifest if false, there is no subdirectory in the downloaded repository gathering the datasets, if true there are subdirectories and a table of content file
     * @param converterId Id of the converter to use if format = "nii"
     * @throws RestServiceException
     */
    void massiveDownloadByExaminations(List<Examination> examinationList, String processingComment, String resultOnly, String format, HttpServletResponse response, boolean withManifest, Long converterId) throws RestServiceException;
}

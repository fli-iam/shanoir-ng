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

package org.shanoir.ng.dataset.controler;

import io.swagger.annotations.ApiParam;
import org.apache.commons.io.FileUtils;
import org.joda.time.DateTime;
import org.shanoir.ng.dataset.model.Dataset;
import org.shanoir.ng.dataset.model.DatasetExpressionFormat;
import org.shanoir.ng.dataset.model.carmin.UploadData;
import org.shanoir.ng.dataset.service.DatasetService;
import org.shanoir.ng.download.WADODownloaderService;
import org.shanoir.ng.shared.configuration.RabbitMQConfiguration;
import org.shanoir.ng.shared.event.ShanoirEvent;
import org.shanoir.ng.shared.event.ShanoirEventService;
import org.shanoir.ng.shared.event.ShanoirEventType;
import org.shanoir.ng.shared.exception.ErrorModel;
import org.shanoir.ng.shared.exception.RestServiceException;
import org.shanoir.ng.shared.model.Subject;
import org.shanoir.ng.shared.repository.SubjectRepository;
import org.shanoir.ng.utils.DatasetFileUtils;
import org.shanoir.ng.utils.KeycloakUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Controller
public class CarminDataApiController implements CarminDataApi{

    private static final String DCM = "dcm";
    private static final String DOWNLOAD = ".download";
    private static final String JAVA_IO_TMPDIR = "java.io.tmpdir";
    private static final String NII = "nii";
    private static final String EEG = "eeg";
    private static final String ZIP = ".zip";


    @Autowired
    private DatasetService datasetService;

    @Autowired
    private ShanoirEventService eventService;

    @Autowired
    private SubjectRepository subjectRepo;

    @Autowired
    private WADODownloaderService downloader;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Autowired
    private DatasetFileUtils datasetFileUtils;


    private final HttpServletRequest request;

    private static final Logger LOG = LoggerFactory.getLogger(CarminDataApiController.class);

    public CarminDataApiController(final HttpServletRequest request) {
        this.request = request;
    }

    @Override
    public ResponseEntity<?> getPath(@ApiParam(value = "the complete path on which to request information. It can contain non-encoded slashes. Except for the \"exists\" action, any request on a non-existing path should return an error", required=true) @PathVariable("completePath") String completePath, @NotNull @ApiParam(value = "The \"content\" action downloads the raw file. If the path points to a directory, a tarball of this directory is returned. The \"exists\" action returns a BooleanResponse object (see definition) indicating if the path exists or not. The \"properties\" action returns a Path object (see definition) with the path properties. The \"list\" action returns a DirectoryList object (see definition) with the properties of all the files of the directory (if the path is not a directory an error must be returned). The \"md5\" action is optional and returns a PathMd5 object (see definition)." ,required=true
            ,allowableValues = "properties, exists, list, md5, content", defaultValue = "content") @Valid @RequestParam(value = "action", required = true, defaultValue = "content") String action, HttpServletResponse response) throws IOException, RestServiceException {
        // TODO implement those actions
        switch (action){
            case "exists":
            case "list":
            case "md5":
            case "properties":
                return new ResponseEntity<Void>(HttpStatus.NOT_IMPLEMENTED);
            case "content":{
                downloadDatasetById(Long.parseLong(completePath),null, DCM, response);
                return new ResponseEntity<Void>(HttpStatus.OK);
            }
        }
        return new ResponseEntity<Void>(HttpStatus.BAD_REQUEST);
    }

    @Override
    public ResponseEntity<org.shanoir.ng.dataset.model.carmin.Path> uploadPath(@ApiParam(value = "The complete path on which to upload data. It can contain non-encoded slashes.", required=true) @PathVariable("completePath") String completePath, @ApiParam(value = "") @Valid @RequestBody UploadData body) {
        // TODO To implement
        return new ResponseEntity<org.shanoir.ng.dataset.model.carmin.Path>(HttpStatus.NOT_IMPLEMENTED);
    }

    @Override
    public ResponseEntity<Void> deletePath(@ApiParam(value = "The complete path to delete. It can contain non-encoded slashes.", required=true) @PathVariable("completePath") String completePath) {
        // TODO to implement
        return new ResponseEntity<Void>(HttpStatus.NOT_IMPLEMENTED);
    }

    private void downloadDatasetById(final Long datasetId, final Long converterId,final String format, HttpServletResponse response)
            throws RestServiceException, IOException {

        final Dataset dataset = datasetService.findById(datasetId);
        if (dataset == null) {
            throw new RestServiceException(
                    new ErrorModel(HttpStatus.NOT_FOUND.value(), "Dataset with id not found.", null));
        }

        /* Create folder and file */
        String tmpDir = System.getProperty(JAVA_IO_TMPDIR);
        File userDir = datasetFileUtils.getUserImportDir(tmpDir);

        String datasetName = datasetFileUtils.generateDatasetName(dataset);

        String tmpFilePath = userDir + File.separator + datasetName + "_" + format;

        SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmss");
        File workFolder = new File(tmpFilePath + "-" + formatter.format(new DateTime().toDate()) + DOWNLOAD);
        workFolder.mkdirs();
        List<URL> pathURLs = new ArrayList<>();

        try {
            String subjectName = "unknown";
            Optional<Subject> subjectOpt = subjectRepo.findById(dataset.getSubjectId());
            if (subjectOpt.isPresent()) {
                subjectName = subjectOpt.get().getName();
            }

            if (DCM.equals(format)) {
                datasetFileUtils.getDatasetFilePathURLs(dataset, pathURLs, DatasetExpressionFormat.DICOM);
                downloader.downloadDicomFilesForURLs(pathURLs, workFolder, subjectName, dataset);
            } else if (NII.equals(format)) {
                // Check if we want a specific converter
                if (converterId != null) {
                    // If converter ID is set, redo a conversion
                    // Create a temporary folder
                    // Add timestamp to get a difference
                    File tmpFile = new File(userDir.getAbsolutePath() + File.separator + "Datasets" + formatter.format(new DateTime().toDate()));
                    tmpFile.mkdirs();
                    // Download DICOMs in the temporary folder
                    datasetFileUtils.getDatasetFilePathURLs(dataset, pathURLs, DatasetExpressionFormat.DICOM);
                    downloader.downloadDicomFilesForURLs(pathURLs, workFolder, subjectName, dataset);

                    // Convert them, sending to import microservice
                    boolean result = (boolean) this.rabbitTemplate.convertSendAndReceive(RabbitMQConfiguration.NIFTI_CONVERSION_QUEUE, converterId + ";" + tmpFile.getAbsolutePath());

                    if (!result) {
                        throw new RestServiceException(
                                new ErrorModel(HttpStatus.UNPROCESSABLE_ENTITY.value(), "Bad arguments", null));
                    }
                    tmpFilePath = tmpFile.getAbsolutePath();
                    workFolder = new File(tmpFile.getAbsolutePath() + File.separator + "result");
                } else  {
                    datasetFileUtils.getDatasetFilePathURLs(dataset, pathURLs, DatasetExpressionFormat.NIFTI_SINGLE_FILE);
                    datasetFileUtils.copyNiftiFilesForURLs(pathURLs, workFolder, dataset, subjectName);
                }
            } else if (EEG.equals(format)) {
                datasetFileUtils.getDatasetFilePathURLs(dataset, pathURLs, DatasetExpressionFormat.EEG);
                datasetFileUtils.copyNiftiFilesForURLs(pathURLs, workFolder, dataset, subjectName);
            } else {
                throw new RestServiceException(
                        new ErrorModel(HttpStatus.UNPROCESSABLE_ENTITY.value(), "Bad arguments", null));
            }
        } catch (Exception e) {
            LOG.error("Error while retrieveing dataset data.", e);
            FileUtils.deleteQuietly(workFolder);

            throw new RestServiceException(
                    new ErrorModel(HttpStatus.UNPROCESSABLE_ENTITY.value(), "Error while retrieveing dataset data.", e));
        }

        // Check folder emptiness
        if (pathURLs.isEmpty()) {
            // Folder is empty => return an error
            LOG.error("No files could be found for the dataset(s).");
            throw new RestServiceException(
                    new ErrorModel(HttpStatus.UNPROCESSABLE_ENTITY.value(), "No files could be found for this dataset(s)."));
        }

        File zipFile = new File(tmpFilePath + ZIP);
        zipFile.createNewFile();

        datasetFileUtils.zip(workFolder.getAbsolutePath(), zipFile.getAbsolutePath());

        // Try to determine file's content type
        String contentType = request.getServletContext().getMimeType(zipFile.getAbsolutePath());

        ShanoirEvent event = new ShanoirEvent(ShanoirEventType.DOWNLOAD_DATASET_EVENT, dataset.getId().toString(), KeycloakUtil.getTokenUserId(), dataset.getId().toString() + "." + format, ShanoirEvent.IN_PROGRESS);
        eventService.publishEvent(event);

        try (InputStream is = new FileInputStream(zipFile);) {
            response.setHeader("Content-Disposition", "attachment;filename=" + zipFile.getName());
            response.setContentType(contentType);
            response.setContentLengthLong(zipFile.length());
            org.apache.commons.io.IOUtils.copy(is, response.getOutputStream());
            response.flushBuffer();
            event.setStatus(ShanoirEvent.SUCCESS);
            eventService.publishEvent(event);
        } finally {
            FileUtils.deleteQuietly(workFolder);
            FileUtils.deleteQuietly(zipFile);
        }
    }
}

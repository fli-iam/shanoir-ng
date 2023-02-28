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

import io.swagger.v3.oas.annotations.Parameter;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.joda.time.DateTime;
import org.shanoir.ng.dataset.modality.EegDataset;
import org.shanoir.ng.dataset.model.Dataset;
import org.shanoir.ng.dataset.model.DatasetExpression;
import org.shanoir.ng.dataset.model.DatasetExpressionFormat;
import org.shanoir.ng.dataset.model.carmin.UploadData;
import org.shanoir.ng.dataset.service.DatasetService;
import org.shanoir.ng.datasetfile.DatasetFile;
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
import org.shanoir.ng.utils.Utils;
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
import org.springframework.web.util.UriUtils;

import javax.mail.MessagingException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@Controller
public class CarminDataApiController implements CarminDataApi{

    private static final String DCM = "dcm";
    private static final String DOWNLOAD = ".download";
    private static final String JAVA_IO_TMPDIR = "java.io.tmpdir";
    private static final String NII = "nii";
    private static final String BIDS = "BIDS";

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

    private final HttpServletRequest request;

    private static final Logger LOG = LoggerFactory.getLogger(CarminDataApiController.class);

    @Autowired
    public CarminDataApiController(final HttpServletRequest request) {
        this.request = request;
    }

    @Override
    public ResponseEntity<?> getPath(@Parameter(name = "the complete path on which to request information. It can contain non-encoded slashes. Except for the \"exists\" action, any request on a non-existing path should return an error", required=true) @PathVariable("completePath") String completePath, @NotNull @Parameter(name = "The \"content\" action downloads the raw file. If the path points to a directory, a tarball of this directory is returned. The \"exists\" action returns a BooleanResponse object (see definition) indicating if the path exists or not. The \"properties\" action returns a Path object (see definition) with the path properties. The \"list\" action returns a DirectoryList object (see definition) with the properties of all the files of the directory (if the path is not a directory an error must be returned). The \"md5\" action is optional and returns a PathMd5 object (see definition)." ,required=true
        ) @Valid @RequestParam(value = "action", required = true, defaultValue = "content") String action, @Valid @RequestParam(value = "format", required = false, defaultValue = DCM) final String format, HttpServletResponse response) throws IOException, RestServiceException {
        // TODO implement those actions
        switch (action){
            case "exists":
            case "list":
            case "md5":
            case "properties":
                return new ResponseEntity<Void>(HttpStatus.NOT_IMPLEMENTED);
            case "content":{
                downloadDatasetById(Long.parseLong(completePath),null, format, response);
                return new ResponseEntity<Void>(HttpStatus.OK);
            }
        }
        return new ResponseEntity<Void>(HttpStatus.BAD_REQUEST);
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
        File userDir = DatasetFileUtils.getUserImportDir(tmpDir);

        String datasetName = "";
        datasetName += dataset.getId() + "-" + dataset.getName();
        if (dataset.getUpdatedMetadata() != null && dataset.getUpdatedMetadata().getComment() != null) {
            datasetName += "-" + dataset.getUpdatedMetadata().getComment();
        }

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
                DatasetFileUtils.getDatasetFilePathURLs(dataset, pathURLs, DatasetExpressionFormat.DICOM);
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
                    DatasetFileUtils.getDatasetFilePathURLs(dataset, pathURLs, DatasetExpressionFormat.DICOM);
                    downloader.downloadDicomFilesForURLs(pathURLs, tmpFile, subjectName, dataset);

                    // Convert them, sending to import microservice
                    boolean result = (boolean) this.rabbitTemplate.convertSendAndReceive(RabbitMQConfiguration.NIFTI_CONVERSION_QUEUE, converterId + ";" + tmpFile.getAbsolutePath());

                    if (!result) {
                        throw new RestServiceException(
                                new ErrorModel(HttpStatus.UNPROCESSABLE_ENTITY.value(), "Bad arguments", null));
                    }
                    tmpFilePath = tmpFile.getAbsolutePath();
                    workFolder = new File(tmpFile.getAbsolutePath() + File.separator + "result");
                } else  {
                    DatasetFileUtils.getDatasetFilePathURLs(dataset, pathURLs, DatasetExpressionFormat.NIFTI_SINGLE_FILE);
                    DatasetFileUtils.copyNiftiFilesForURLs(pathURLs, workFolder, dataset, subjectName, false);
                }
            } else if (EEG.equals(format)) {
                DatasetFileUtils.getDatasetFilePathURLs(dataset, pathURLs, DatasetExpressionFormat.EEG);
                DatasetFileUtils.copyNiftiFilesForURLs(pathURLs, workFolder, dataset, subjectName, false);
            } else if (BIDS.equals(format)) {
                DatasetFileUtils.getDatasetFilePathURLs(dataset, pathURLs, DatasetExpressionFormat.BIDS);
                DatasetFileUtils.copyNiftiFilesForURLs(pathURLs, workFolder, dataset, subjectName, true);
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

        File resultFile = null;

        if(NII.equals(format)){
            // return the file in the tmpFilePath
            File directory = new File(workFolder.getAbsolutePath());
            File[] files = directory.listFiles();


            // return only one nifti file
            for (int i = 0; i < files.length; i++) {
                if(files[i].getName().endsWith("nii.gz")){
                    resultFile = files[i];
                    break;
                }
            }
        }else{
            File zipFile = new File(tmpFilePath + ZIP);
            zipFile.createNewFile();

            Utils.zip(workFolder.getAbsolutePath(), zipFile.getAbsolutePath());

            resultFile = zipFile;
        }

        if(resultFile == null) throw new RestServiceException(
                new ErrorModel(HttpStatus.UNPROCESSABLE_ENTITY.value(), "No files could be found for this dataset(s)."));

        // Try to determine file's content type
        String contentType = request.getServletContext().getMimeType(resultFile.getAbsolutePath());

        ShanoirEvent event = new ShanoirEvent(ShanoirEventType.DOWNLOAD_DATASET_EVENT, dataset.getId().toString(), KeycloakUtil.getTokenUserId(), dataset.getId().toString() + "." + format, ShanoirEvent.IN_PROGRESS);
        eventService.publishEvent(event);

        try (InputStream is = new FileInputStream(resultFile);) {
            response.setHeader("Content-Disposition", "attachment;filename=" + resultFile.getName());
            response.setContentType(contentType);
            response.setContentLengthLong(resultFile.length());
            org.apache.commons.io.IOUtils.copy(is, response.getOutputStream());
            response.flushBuffer();
            event.setStatus(ShanoirEvent.SUCCESS);
            eventService.publishEvent(event);
        } finally {
            FileUtils.deleteQuietly(workFolder);
            FileUtils.deleteQuietly(resultFile);
        }
    }

}

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

package org.shanoir.ng.bids.controller;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.apache.commons.io.FileUtils;
import org.joda.time.DateTime;
import org.shanoir.ng.bids.BidsDeserializer;
import org.shanoir.ng.bids.model.BidsElement;
import org.shanoir.ng.bids.service.BIDSService;
import org.shanoir.ng.bids.service.BidsTreeLockedException;
import org.shanoir.ng.bids.service.BidsTreeSemaphore;
import org.shanoir.ng.bids.service.BidsValidationPublisher;
import org.shanoir.ng.shared.exception.ErrorModel;
import org.shanoir.ng.shared.exception.RestServiceException;
import org.shanoir.ng.shared.model.Study;
import org.shanoir.ng.shared.service.StudyService;
import org.shanoir.ng.utils.KeycloakUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.AmqpException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import io.swagger.v3.oas.annotations.Parameter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;

@Controller
public class BidsApiController implements BidsApi {

    private static final String JAVA_IO_TMPDIR = "java.io.tmpdir";

    private static final String ZIP = ".zip";

    private static final Logger LOG = LoggerFactory.getLogger(BidsApiController.class);

    @Autowired
    private BIDSService bidsService;

    @Autowired
    private BidsDeserializer bidsDeserializer;

    @Autowired
    private StudyService studyService;

    @Autowired
    private BidsValidationPublisher bidsValidationPublisher;

    @Autowired
    private BidsTreeSemaphore bidsTreeSemaphore;

    @Value("${storage.file-system.bids-data}")
    private String bidsStorageDir;

    private final HttpServletRequest request;

    @org.springframework.beans.factory.annotation.Autowired
    public BidsApiController(final HttpServletRequest request) {
        this.request = request;
    }

    @Override
    public ResponseEntity<Void> generateBIDSByStudyId(
            @Parameter(description = "id of the study", required = true) @PathVariable("studyId") Long studyId,
            @Parameter(description = "name of the study", required = true) @PathVariable("studyName") String studyName) throws IOException {
        try {
            bidsService.exportAsBids(studyId);
        } catch (BidsTreeLockedException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }
        return ResponseEntity.ok().build();
    }

    public ResponseEntity<BidsElement> refreshBIDSByStudyId(
            @Parameter(description = "id of the study", required = true) @PathVariable("studyId") Long studyId,
            @Parameter(description = "name of the study", required = true) @PathVariable("studyName") String studyName) throws RestServiceException, IOException {
        this.bidsService.deleteBidsFolder(studyId);
        return this.getBIDSStructureByStudyId(studyId);
    }

    @Override
    public void exportBIDSFile(
            @Parameter(description = "Id of the study", required = true) @PathVariable("studyId") Long studyId,
            @Parameter(description = "file path") @Valid @RequestParam(value = "filePath", required = true) String filePath,
            HttpServletResponse response) throws RestServiceException, IOException {
        // Check filePath too
        // /var/bids-data/study-1_NATIVE
        if (!filePath.startsWith(bidsStorageDir + "/study-" + studyId)) {
            response.sendError(HttpStatus.UNAUTHORIZED.value());
            return;
        }

        // Get file, zip it and download it
        File fileToBeZipped = new File(filePath);
        if (!fileToBeZipped.exists()) {
            response.sendError(HttpStatus.NO_CONTENT.value());
            return;
        }

        // Copy / zip it (and by the way filter only folder that we are interested in)
        String userDir = getUserDir(System.getProperty(JAVA_IO_TMPDIR)).getAbsolutePath();

        // Add timestamp to get a "random" difference
        SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmss");
        File tmpFile = new File(userDir + File.separator + formatter.format(new DateTime().toDate()) + File.separator);

        File fileDir = new File(tmpFile.getAbsolutePath() + File.separator + fileToBeZipped.getName());
        fileDir.mkdirs();

        // Copy the file into the temp dir
        if (fileToBeZipped.isDirectory()) {
            FileUtils.copyDirectory(fileToBeZipped, new File(fileDir.getPath() + File.separator + fileToBeZipped.getName()));
        } else {
            Files.copy(Paths.get(fileToBeZipped.getPath()), Paths.get(fileDir.getPath() + File.separator + fileToBeZipped.getName()));
        }

        File zipFile = new File(tmpFile.getAbsolutePath() + File.separator + fileToBeZipped.getName() + ZIP);
        zipFile.createNewFile();

        zip(fileDir.getAbsolutePath(), zipFile.getAbsolutePath());

        // Try to determine file's content type
        String contentType = request.getServletContext().getMimeType(zipFile.getAbsolutePath());

        try (InputStream is = new FileInputStream(zipFile);) {
            response.setHeader("Content-Disposition", "attachment;filename = " + zipFile.getName());
            response.setContentType(contentType);
            response.setContentLengthLong(zipFile.length());
            org.apache.commons.io.IOUtils.copy(is, response.getOutputStream());
            response.flushBuffer();
        } finally {
            FileUtils.deleteQuietly(zipFile);
        }
    }

    public static File getUserDir(String importDir) {
        final Long userId = KeycloakUtil.getTokenUserId();
        final String userImportDirFilePath = importDir + File.separator + Long.toString(userId);
        final File userImportDir = new File(userImportDirFilePath);
        if (!userImportDir.exists()) {
            userImportDir.mkdirs(); // create if not yet existing
        } // else is wanted case, user has already its import directory
        return userImportDir;
    }

    @Override
    public ResponseEntity<BidsElement> getBIDSStructureByStudyId(
            @Parameter(description = "id of the study", required = true) @PathVariable("studyId") Long studyId)
            throws RestServiceException, IOException {
        BidsElement studyBidsElement;
        Study study = studyService.findById(studyId);
        if (study != null) {
            boolean unlocked = bidsTreeSemaphore.awaitUnlock(studyId, 30, java.util.concurrent.TimeUnit.SECONDS);
            if (!unlocked) { // still locked after timeout
                throw new RestServiceException(
                        new ErrorModel(HttpStatus.CONFLICT.value(), "The BIDS tree is locked and the timeout has been reached", null));
            } else { // now unlocked
                try {
                    // try to get the BIDS structure
                    studyBidsElement = bidsDeserializer.deserialize(bidsService.exportAsBids(studyId));
                    return new ResponseEntity<>(studyBidsElement, HttpStatus.OK);
                } catch (BidsTreeLockedException e) {
                    // if, by any bad luck, it has been locked between those lines by the same user requesting a refresh, throw exception
                    throw new RestServiceException(
                            new ErrorModel(HttpStatus.CONFLICT.value(), "The BIDS tree is currently reconstructing, try again later", null));
                }
            }
        } else {
            throw new RestServiceException(
                    new ErrorModel(HttpStatus.NOT_FOUND.value(), "Study with id " + studyId + " not found", null));
        }

    }

    @Override
    public ResponseEntity<String> validateBidsByStudyId(
            @Parameter(description = "Id of the study", required = true) @PathVariable("studyId") Long studyId,
            @Parameter(description = "file path") @Valid @RequestParam(value = "filePath", required = true) String filePath) throws RestServiceException, IOException {

        if (!filePath.startsWith("/var/datasets-data/bids-data/stud-" + studyId)) {
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }

        try {
            bidsService.exportAsBids(studyId); // will not recreate if already existing
        } catch (BidsTreeLockedException e) {
            throw new RestServiceException(
                    new ErrorModel(HttpStatus.CONFLICT.value(), "The BIDS tree is currently reconstructing, try again later", null));
        }

        // Request BIDS validation
        try {
            String validationResultJson = bidsValidationPublisher.requestValidationSync(filePath);
            return new ResponseEntity<>(validationResultJson, HttpStatus.OK);
        } catch (AmqpException e) {
            ErrorModel error = new ErrorModel(HttpStatus.INTERNAL_SERVER_ERROR.value(), "Error while validating BIDS dataset", e.getMessage());
            throw new RestServiceException(e, error);
        }
    }

    /**
     * Zip
     *
     * @param sourceDirPath
     * @param zipFilePath
     * @throws IOException
     */
    private void zip(final String sourceDirPath, final String zipFilePath) throws IOException {
        Path p = Paths.get(zipFilePath);
        // 1. Create an outputstream (zip) on the destination
        try (ZipOutputStream zos = new ZipOutputStream(Files.newOutputStream(p))) {
            // 2. "Walk" => iterate over the source file
            Path pp = Paths.get(sourceDirPath);
            try (Stream<Path> walker = Files.walk(pp)) {

                // 3. We only consider directories, and we copyt them directly by "relativising" them then copying them to the output
                walker.filter(path -> !path.toFile().isDirectory()).forEach(path -> {
                    ZipEntry zipEntry = new ZipEntry(pp.relativize(path).toString());
                    try {
                        zos.putNextEntry(zipEntry);
                        Files.copy(path, zos);
                        zos.closeEntry();
                    } catch (IOException e) {
                        LOG.error(e.getMessage(), e);
                    }
                });
            }
            zos.finish();
        }
    }

}

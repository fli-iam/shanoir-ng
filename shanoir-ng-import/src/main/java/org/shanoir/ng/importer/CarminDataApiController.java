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

package org.shanoir.ng.importer;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Date;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.FileUtils;
import org.shanoir.ng.importer.model.carmin.Path;
import org.shanoir.ng.importer.model.carmin.UploadData;
import org.shanoir.ng.shared.exception.ErrorModel;
import org.shanoir.ng.shared.exception.RestServiceException;
import org.shanoir.ng.utils.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.util.UriUtils;

import io.swagger.annotations.ApiParam;

/**
 * Carmin data upload results from VIP to tmp folder endpoint
 * 
 * @author KhalilKes
 */
@Controller
public class CarminDataApiController implements CarminDataApi {

    private static final String VIP_UPLOAD_FOLDER = "vip_uploads";
    private static final String ERROR_WHILE_SAVING_UPLOADED_FILE = "Error while saving uploaded file.";
    private static final String PATH_PREFIX = "/carmin-data/path/";

    private static final Logger LOG = LoggerFactory.getLogger(CarminDataApiController.class);

    private final HttpServletRequest httpServletRequest;

    @Autowired
    CarminDataApiController(HttpServletRequest httpServletRequest) {
        this.httpServletRequest = httpServletRequest;

    }

    @Value("${shanoir.import.directory}")
    private String importDir;

    @Override
    public ResponseEntity<Path> uploadPath(
            @ApiParam(value = "") @Valid @RequestBody UploadData body)
            throws RestServiceException {

        String completePath = extractPathFromRequest(httpServletRequest);
        LOG.info(completePath);

        Path path = new Path();

        try {

            // creates file from the base64 string
            String[] pathItems = completePath.split("/");
            String uploadFileName = pathItems[pathItems.length - 1];

            // create unique user directory from the completePath
            final String userImportDirFilePath = importDir + File.separator + VIP_UPLOAD_FOLDER + File.separator
                    + pathItems[1] + File.separator
                    + pathItems[2];

            final File userImportDir = new File(userImportDirFilePath);
            if (!userImportDir.exists()) {
                userImportDir.mkdirs();
            }

            // upload the result in the folder
            File destinationUploadFile = new File(userImportDir.getAbsolutePath(), uploadFileName);
            byte[] bytes = Base64.decodeBase64(body.getBase64Content());
            FileUtils.writeByteArrayToFile(destinationUploadFile, bytes);

            path.setPlatformPath(userImportDir.getAbsolutePath());
            path.setIsDirectory(true);
            // sum of the size of all files within the directory
            long size = Files.walk(userImportDir.toPath()).mapToLong(p -> p.toFile().length()).sum();
            path.setSize(size);
            path.setLastModificationDate(new Date().getTime());

        } catch (IOException e) {
            LOG.error(e.getMessage(), e);
            throw new RestServiceException(
                    new ErrorModel(HttpStatus.UNPROCESSABLE_ENTITY.value(), ERROR_WHILE_SAVING_UPLOADED_FILE, null));
        }

        return new ResponseEntity<Path>(path, HttpStatus.CREATED);
    }

    @Override
    public ResponseEntity<Void> deletePath() {
        String completePath = extractPathFromRequest(httpServletRequest);
        LOG.info(completePath);

        final String userImportDirFilePath = importDir + File.separator + VIP_UPLOAD_FOLDER + completePath;

        final File fileToDelete = new File(userImportDirFilePath);
        Utils.deleteFolder(fileToDelete);

        return new ResponseEntity<Void>(HttpStatus.OK);
    }

    /**
     * get the path from the URI aftrer path prefix /carmin-data/path
     * 
     * @param request
     * @return
     */
    private String extractPathFromRequest(HttpServletRequest request) {
        String decodedUri = UriUtils.decode(request.getRequestURI(), "UTF-8");
        int index = decodedUri.indexOf(PATH_PREFIX);

        return decodedUri.substring(index + PATH_PREFIX.length() - 1);
    }

}
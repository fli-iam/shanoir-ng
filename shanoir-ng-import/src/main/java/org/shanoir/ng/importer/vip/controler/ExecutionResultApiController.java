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

package org.shanoir.ng.importer.vip.controler;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Date;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.FileUtils;
import org.shanoir.ng.importer.vip.model.Path;
import org.shanoir.ng.importer.vip.model.UploadData;
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

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.StreamReadConstraints;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;

import io.swagger.v3.oas.annotations.Parameter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;

/**
 * VIP execution upload results from VIP to tmp folder endpoint
 *
 * @author KhalilKes
 */
@Controller
public class ExecutionResultApiController implements ExecutionResultApi {

    private static final String VIP_UPLOAD_FOLDER = "vip_uploads";
    private static final String ERROR_WHILE_SAVING_UPLOADED_FILE = "Error while saving uploaded file.";
    private static final String PATH_PREFIX = "/carmin-data/";

    private static final Logger LOG = LoggerFactory.getLogger(ExecutionResultApiController.class);

    private final HttpServletRequest httpServletRequest;

    @Autowired
    ExecutionResultApiController(HttpServletRequest httpServletRequest) {
        this.httpServletRequest = httpServletRequest;

    }

    @Value("${shanoir.import.directory}")
    private String importDir;

    @Override
    public ResponseEntity<Path> uploadPath(
            @Parameter(name = "") @Valid @RequestBody String body)
            throws RestServiceException, JsonProcessingException {

        JsonFactory jsonFactory = JsonFactory.builder()
                .streamReadConstraints(StreamReadConstraints.builder().maxStringLength(Integer.MAX_VALUE).build())
                        .build();

        ObjectMapper objectMapper = JsonMapper.builder(jsonFactory).build();
        UploadData received = objectMapper.readValue(body, UploadData.class);

        String importPath = getImportPathFromRequest(httpServletRequest);

        Path path = new Path();

        try {

            File resultFile = new File(importPath);
            File resultDir = resultFile.getParentFile();

            if (!resultDir.exists()) {
                resultDir.mkdirs();
            }

            byte[] bytes = Base64.decodeBase64(received.getBase64Content());
            FileUtils.writeByteArrayToFile(resultFile, bytes);

            path.setPlatformPath(resultDir.getAbsolutePath());
            path.setIsDirectory(true);
            // sum of the size of all files within the directory
            long size = Files.walk(resultDir.toPath()).mapToLong(p -> p.toFile().length()).sum();
            path.setSize(size);
            path.setLastModificationDate(new Date().getTime());

        } catch (IOException e) {
            LOG.error("I/O error while uploading [{}]", importPath, e);
            throw new RestServiceException(
                    new ErrorModel(HttpStatus.UNPROCESSABLE_ENTITY.value(), ERROR_WHILE_SAVING_UPLOADED_FILE, null));
        }

        return new ResponseEntity<>(path, HttpStatus.CREATED);
    }

    @Override
    public ResponseEntity<Void> deletePath() {
        String userImportDirFilePath = getImportPathFromRequest(httpServletRequest);

        final File fileToDelete = new File(userImportDirFilePath);
        Utils.deleteFolder(fileToDelete);

        return new ResponseEntity<>(HttpStatus.OK);
    }

    /**
     * get the path from the URI aftrer path prefix /carmin-data/path
     *
     * @param request
     * @return
     */
    private String getImportPathFromRequest(HttpServletRequest request) {
        String decodedUri = UriUtils.decode(request.getRequestURI(), "UTF-8");
        return importDir + File.separator + VIP_UPLOAD_FOLDER + File.separator + decodedUri.replace(PATH_PREFIX, "");
    }

}

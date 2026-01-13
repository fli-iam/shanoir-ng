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

package org.shanoir.ng.vip.path;

import jakarta.servlet.http.HttpServletResponse;
import org.shanoir.ng.dataset.model.Dataset;
import org.shanoir.ng.dataset.service.DatasetDownloaderServiceImpl;
import org.shanoir.ng.shared.exception.EntityNotFoundException;
import org.shanoir.ng.shared.exception.ErrorModel;
import org.shanoir.ng.shared.exception.RestServiceException;
import org.shanoir.ng.vip.processingResource.repository.ProcessingResourceRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;

import java.io.IOException;
import java.util.List;

@Controller
public class PathApiController implements PathApi {

    private static final Logger LOG = LoggerFactory.getLogger(PathApiController.class);

    @Qualifier("datasetDownloaderServiceImpl")
    @Autowired
    private DatasetDownloaderServiceImpl datasetDownloaderService;

    @Autowired
    private ProcessingResourceRepository processingResourceRepository;

    @Override
    public ResponseEntity<?> getPath(String completePath, String action, final String format, Long converterId, String sorting, HttpServletResponse response)
            throws IOException, RestServiceException, EntityNotFoundException {
        LOG.debug("completePath: {}, action: {}, format: {}, converterId: {}, response: {}", completePath, action, format, converterId, response);
        // TODO implement those actions
        try {
            switch (action) {
                case "exists":
                case "list":
                case "md5":
                case "properties":
                    return new ResponseEntity<Void>(HttpStatus.NOT_IMPLEMENTED);
                case "content":
                    List<Dataset> datasets = processingResourceRepository.findDatasetsByResourceId(completePath);

                    if (datasets.isEmpty()) {
                        LOG.error("No dataset found for resource id [{}]", completePath);
                        return new ResponseEntity<Void>(HttpStatus.BAD_REQUEST);
                    }

                    datasetDownloaderService.massiveDownload(format, datasets, response, true, converterId, true, sorting);
                    return new ResponseEntity<Void>(HttpStatus.OK);
                default:
                    ErrorModel errorModel = new ErrorModel(HttpStatus.BAD_REQUEST.value(), "Action " + action + " not supported");
                    throw new RestServiceException(errorModel);
            }
        } catch (Exception e) {
            LOG.error("Error while VIP downloading data", e);
            throw e;
        }
    }
}

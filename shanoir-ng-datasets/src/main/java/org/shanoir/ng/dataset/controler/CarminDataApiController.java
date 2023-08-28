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

import java.io.IOException;
import java.util.List;

import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import org.shanoir.ng.dataset.model.Dataset;
import org.shanoir.ng.dataset.service.DatasetDownloaderServiceImpl;
import org.shanoir.ng.dataset.service.DatasetService;
import org.shanoir.ng.dicom.DIMSEService;
import org.shanoir.ng.processing.carmin.model.ProcessingResource;
import org.shanoir.ng.processing.carmin.service.ProcessingResourceService;
import org.shanoir.ng.processing.dto.mapper.CarminDatasetProcessingMapper;
import org.shanoir.ng.shared.exception.EntityNotFoundException;
import org.shanoir.ng.shared.exception.ErrorModel;
import org.shanoir.ng.shared.exception.RestServiceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import io.swagger.annotations.ApiParam;

@Controller
public class CarminDataApiController implements CarminDataApi{

    private static final Logger LOG = LoggerFactory.getLogger(CarminDataApiController.class);

    private static final String DCM = "dcm";
    
    @Autowired
    private DatasetDownloaderServiceImpl datasetDownloaderService;

    @Autowired
    private ProcessingResourceService processingResourceService;

    @Override
    public ResponseEntity<?> getPath(
            @ApiParam(value = "the complete path on which to request information. It can contain non-encoded slashes. Except for the \"exists\" action, any request on a non-existing path should return an error",
                    required = true)
            @PathVariable("completePath") String completePath,
            @NotNull @ApiParam(value = "The \"content\" action downloads the raw file. If the path points to a directory, a tarball of this directory is returned. The \"exists\" action returns a BooleanResponse object (see definition) indicating if the path exists or not. The \"properties\" action returns a Path object (see definition) with the path properties. The \"list\" action returns a DirectoryList object (see definition) with the properties of all the files of the directory (if the path is not a directory an error must be returned). The \"md5\" action is optional and returns a PathMd5 object (see definition).",
                    required = true, allowableValues = "properties, exists, list, md5, content", defaultValue = "content")
            @Valid @RequestParam(value = "action", defaultValue = "content") String action,
            @Valid @RequestParam(value = "format", required = false, defaultValue = DCM) final String format,
            HttpServletResponse response) throws IOException, RestServiceException, EntityNotFoundException {
        // TODO implement those actions
        switch (action){
            case "exists":
            case "list":
            case "md5":
            case "properties":
                return new ResponseEntity<Void>(HttpStatus.NOT_IMPLEMENTED);
            case "content":

                List<Dataset> datasets = this.processingResourceService.findDatasetsByResourceId(completePath);

                if(datasets.isEmpty()){
                    LOG.error("No dataset found for resource id [{}]", completePath);
                    return new ResponseEntity<Void>(HttpStatus.BAD_REQUEST);
                }

                if(datasets.size() == 1){
                    datasetDownloaderService.downloadDatasetById(datasets.get(0).getId(), null, format, response, true);
                }else{
                    datasetDownloaderService.massiveDownload(format, datasets, response, true);
                }

                return new ResponseEntity<Void>(HttpStatus.OK);
        }

        return new ResponseEntity<Void>(HttpStatus.BAD_REQUEST);

    }

}

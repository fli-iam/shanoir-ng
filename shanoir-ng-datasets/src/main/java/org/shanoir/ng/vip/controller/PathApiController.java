package org.shanoir.ng.vip.controller;

import io.swagger.v3.oas.annotations.Parameter;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.shanoir.ng.dataset.model.Dataset;
import org.shanoir.ng.dataset.service.DatasetDownloaderServiceImpl;
import org.shanoir.ng.shared.exception.EntityNotFoundException;
import org.shanoir.ng.shared.exception.RestServiceException;
import org.shanoir.ng.vip.resource.ProcessingResourceService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.io.IOException;
import java.util.List;

@Controller
public class PathApiController implements PathApi {

    private static final Logger LOG = LoggerFactory.getLogger(PathApiController.class);

    private static final String DCM = "dcm";

    @Autowired
    private DatasetDownloaderServiceImpl datasetDownloaderService;

    @Autowired
    private ProcessingResourceService processingResourceService;

    @Override
    public ResponseEntity<?> getPath(
            @Parameter(name = "the complete path on which to request information. It can contain non-encoded slashes. Except for the \"exists\" action, any request on a non-existing path should return an error", required = true) @PathVariable("completePath") String completePath,
            @NotNull @Parameter(name = "The \"content\" action downloads the raw file. If the path points to a directory, a tarball of this directory is returned. The \"exists\" action returns a BooleanResponse object (see definition) indicating if the path exists or not. The \"properties\" action returns a Path object (see definition) with the path properties. The \"list\" action returns a DirectoryList object (see definition) with the properties of all the files of the directory (if the path is not a directory an error must be returned). The \"md5\" action is optional and returns a PathMd5 object (see definition).", required = true) @Valid @RequestParam(value = "action", required = true, defaultValue = "content") String action,
            @Valid @RequestParam(value = "format", required = false, defaultValue = DCM) final String format,
            @Valid @RequestParam(value  = "converter", required  = false) Long converter,
            HttpServletResponse response) throws IOException, RestServiceException, EntityNotFoundException {
        // TODO implement those actions
        switch (action) {
            case "exists":
            case "list":
            case "md5":
            case "properties":
                return new ResponseEntity<Void>(HttpStatus.NOT_IMPLEMENTED);
            case "content":

                List<Dataset> datasets = this.processingResourceService.findDatasetsByResourceId(completePath);

                if (datasets.isEmpty()) {
                    LOG.error("No dataset found for resource id [{}]", completePath);
                    return new ResponseEntity<Void>(HttpStatus.BAD_REQUEST);
                }

                datasetDownloaderService.massiveDownload(format, datasets, response, true, null);

                return new ResponseEntity<Void>(HttpStatus.OK);
        }

        return new ResponseEntity<Void>(HttpStatus.BAD_REQUEST);

    }
}

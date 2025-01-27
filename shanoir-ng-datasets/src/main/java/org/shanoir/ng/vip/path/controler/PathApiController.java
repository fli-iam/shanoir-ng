package org.shanoir.ng.vip.path.controler;

import jakarta.servlet.http.HttpServletResponse;
import org.shanoir.ng.dataset.model.Dataset;
import org.shanoir.ng.dataset.service.DatasetDownloaderServiceImpl;
import org.shanoir.ng.shared.exception.EntityNotFoundException;
import org.shanoir.ng.shared.exception.RestServiceException;
import org.shanoir.ng.vip.processingResource.service.ProcessingResourceService;
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

    private static final String DCM = "dcm";

    @Qualifier("datasetDownloaderServiceImpl")
    @Autowired
    private DatasetDownloaderServiceImpl datasetDownloaderService;

    @Autowired
    private ProcessingResourceService processingResourceService;

    @Override
    public ResponseEntity<?> getPath(String completePath, String action, final String format, Long converterId, HttpServletResponse response)
            throws IOException, RestServiceException, EntityNotFoundException {
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

                datasetDownloaderService.massiveDownload(format, datasets, response, true, converterId);

                return new ResponseEntity<Void>(HttpStatus.OK);
        }

        return new ResponseEntity<Void>(HttpStatus.BAD_REQUEST);

    }
}

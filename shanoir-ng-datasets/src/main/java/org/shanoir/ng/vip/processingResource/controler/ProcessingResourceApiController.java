package org.shanoir.ng.vip.processingResource.controler;

import jakarta.servlet.http.HttpServletResponse;

import org.shanoir.ng.dataset.model.Dataset;
import org.shanoir.ng.dataset.service.DatasetDownloaderServiceImpl;
import org.shanoir.ng.shared.exception.EntityNotFoundException;
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
public class ProcessingResourceApiController implements ProcessingResourceApi {

    private static final Logger LOG = LoggerFactory.getLogger(ProcessingResourceApiController.class);

    @Qualifier("datasetDownloaderServiceImpl")
    @Autowired
    private DatasetDownloaderServiceImpl datasetDownloaderService;

    @Autowired
    private ProcessingResourceRepository processingResourceRepository;

    @Override
    public ResponseEntity<?> getPath(String completePath, String action, final String format, Long converterId, HttpServletResponse response)
            throws IOException, RestServiceException, EntityNotFoundException {
        LOG.debug("completePath: {}, action: {}, format: {}, converterId: {}, response: {}", completePath, action, format, converterId, response);
        // TODO implement those actions
        try{
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

                    datasetDownloaderService.massiveDownload(format, datasets, response, true, converterId, true);
                    return new ResponseEntity<Void>(HttpStatus.OK);
            }
        } catch (Exception e) {
            LOG.error("Error while VIP downloading data", e);
            throw e;
        }
        return new ResponseEntity<Void>(HttpStatus.BAD_REQUEST);
    }
}

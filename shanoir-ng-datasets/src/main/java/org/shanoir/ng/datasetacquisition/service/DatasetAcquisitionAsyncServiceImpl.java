package org.shanoir.ng.datasetacquisition.service;

import org.apache.solr.client.solrj.SolrServerException;
import org.shanoir.ng.datasetacquisition.model.DatasetAcquisition;
import org.shanoir.ng.shared.event.ShanoirEvent;
import org.shanoir.ng.shared.exception.RestServiceException;
import org.shanoir.ng.shared.exception.ShanoirException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;

@Service
public class DatasetAcquisitionAsyncServiceImpl implements DatasetAcquisitionAsyncService {

    @Autowired
    private DatasetAcquisitionService datasetAcquisitionService;

    @Async
    @Transactional
    public void deleteByIdAsync(DatasetAcquisition entity, ShanoirEvent event) throws ShanoirException, SolrServerException, IOException, RestServiceException {
        datasetAcquisitionService.delete(entity, event);
    }

}

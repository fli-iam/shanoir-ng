package org.shanoir.ng.datasetacquisition.service;

import org.apache.solr.client.solrj.SolrServerException;
import org.shanoir.ng.datasetacquisition.model.DatasetAcquisition;
import org.shanoir.ng.shared.event.ShanoirEvent;
import org.shanoir.ng.shared.exception.RestServiceException;
import org.shanoir.ng.shared.exception.ShanoirException;
import org.springframework.stereotype.Service;

import java.io.IOException;


public interface DatasetAcquisitionAsyncService {

    public void deleteByIdAsync(DatasetAcquisition entity, ShanoirEvent event) throws ShanoirException, SolrServerException, IOException, RestServiceException;

}

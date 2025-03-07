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

package org.shanoir.ng.datasetacquisition.service;

import java.io.IOException;

import org.apache.solr.client.solrj.SolrServerException;
import org.shanoir.ng.datasetacquisition.model.DatasetAcquisition;
import org.shanoir.ng.shared.event.ShanoirEvent;
import org.shanoir.ng.shared.exception.RestServiceException;
import org.shanoir.ng.shared.exception.ShanoirException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

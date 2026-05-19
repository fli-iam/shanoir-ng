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

package org.shanoir.ng.tag.controller;

import java.io.IOException;
import java.util.List;
import java.util.Objects;

import org.apache.solr.client.solrj.SolrServerException;
import org.shanoir.ng.dataset.model.Dataset;
import org.shanoir.ng.dataset.service.DatasetService;
import org.shanoir.ng.shared.exception.EntityNotFoundException;
import org.shanoir.ng.tag.service.StudyTagService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;


@Controller
public class StudyTagApiController implements StudyTagApi {

    private static final Logger LOG = LoggerFactory.getLogger(StudyTagApiController.class);

    @Autowired
    private DatasetService datasetService;

    @Autowired
    private StudyTagService studyTagService;

    @Override
    public ResponseEntity<Void> addStudyTagsToDataset(Long datasetId, List<Long> studyTagIds) throws EntityNotFoundException, SolrServerException, IOException {
        Dataset ds = datasetService.findById(datasetId);
        if (Objects.isNull(ds)) {
            throw new EntityNotFoundException(Dataset.class, datasetId);
        }

        studyTagService.addStudyTagsToDataset(ds, studyTagIds);

        return new ResponseEntity<>(HttpStatus.OK);
    }

    @Override
    public ResponseEntity<Void> removeStudyTagsFromDataset(Long datasetId, List<Long> studyTagIds) throws EntityNotFoundException, SolrServerException, IOException {
        Dataset ds = datasetService.findById(datasetId);
        if (Objects.isNull(ds)) {
            throw new EntityNotFoundException(Dataset.class, datasetId);
        }

        studyTagService.removeStudyTagsFromDataset(ds, studyTagIds);

        return new ResponseEntity<>(HttpStatus.OK);
    }
}

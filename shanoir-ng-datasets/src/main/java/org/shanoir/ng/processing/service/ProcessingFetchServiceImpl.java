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

package org.shanoir.ng.processing.service;

import java.util.List;
import java.util.stream.Stream;

import org.shanoir.ng.dataset.model.Dataset;
import org.shanoir.ng.dataset.model.DatasetExpression;
import org.shanoir.ng.dataset.repository.DatasetExpressionRepository;
import org.shanoir.ng.dataset.repository.DatasetRepository;
import org.shanoir.ng.processing.model.DatasetProcessing;
import org.shanoir.ng.processing.repository.DatasetProcessingRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Kept as its own Spring bean, separate from ProcessingDownloaderServiceImpl, so its
 * @Transactional only ever covers this fetch (a handful of fast queries), never the ZIP
 * streaming that follows it. ProcessingDownloaderServiceImpl's massive-download endpoints can be
 * called by hundreds of concurrent VIP downloads, far more than the DB connection pool size; a
 * transaction spanning the whole streamed download would hold one of those pooled connections
 * for the entire download instead of just the fetch, capping concurrency at the pool size.
 */
@Service
public class ProcessingFetchServiceImpl implements ProcessingFetchService {

    @Autowired
    private DatasetProcessingRepository datasetProcessingRepository;

    @Autowired
    private DatasetRepository datasetRepository;

    @Autowired
    private DatasetExpressionRepository datasetExpressionRepository;

    /**
     * Fetches the given processings with their input/output datasets, dataset expressions and
     * dataset files all eagerly loaded, in a fixed number of batched queries instead of one query
     * per processing/dataset/expression. OSIV is disabled, so this whole chain has to run within
     * one transaction (see @Transactional below) for the fetch queries to share the same
     * persistence context and populate the same managed entities.
     *
     * @param processingIds
     * @return
     */
    @Override
    @Transactional(readOnly = true)
    public List<DatasetProcessing> findProcessingsWithInputsOutputsAndDatasetFiles(List<Long> processingIds) {
        List<DatasetProcessing> processings = datasetProcessingRepository.findByIdsWithInputsAndOutputs(processingIds);
        List<Long> datasetIds = processings.stream()
                .flatMap(processing -> Stream.concat(processing.getInputDatasets().stream(), processing.getOutputDatasets().stream()))
                .map(Dataset::getId)
                .distinct()
                .toList();
        List<Dataset> datasetsWithExpressions = datasetRepository.findByIdsWithDatasetExpression(datasetIds);
        List<Long> expressionIds = datasetsWithExpressions.stream()
                .flatMap(dataset -> dataset.getDatasetExpressions().stream())
                .map(DatasetExpression::getId)
                .distinct()
                .toList();
        datasetExpressionRepository.findByIdsWithDatasetFiles(expressionIds);
        return processings;
    }
}

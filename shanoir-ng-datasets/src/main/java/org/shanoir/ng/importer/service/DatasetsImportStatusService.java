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

package org.shanoir.ng.importer.service;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import org.shanoir.ng.importer.dto.DatasetsImportStatus;
import org.shanoir.ng.importer.dto.DatasetsImportStatus.State;
import org.springframework.stereotype.Service;

/**
 * Tracks, in memory, the state of the asynchronous (RabbitMQ-driven)
 * dataset-acquisition creation triggered by ImporterService, keyed by
 * examination id. This lets clients (e.g. ShanoirUploader / tests) poll MS
 * Datasets to know when an import has actually finished, since ExaminationApi
 * lives in a different microservice and cannot tell us when the datasets
 * themselves have been created.
 *
 * NOTE: in-memory only. If MS Datasets is scaled horizontally or restarted,
 * status lookups can miss entries produced/consumed by a different instance.
 * Fine for a single-instance test/dev setup; for production this should move to
 * a small DB table or Redis.
 */
@Service
public class DatasetsImportStatusService {

    private final ConcurrentHashMap<Long, DatasetsImportStatus> statusByExamination = new ConcurrentHashMap<>();

    public void markInProgress(Long examinationId) {
        if (examinationId == null)
            return;
        statusByExamination.put(examinationId,
                new DatasetsImportStatus(examinationId, State.IN_PROGRESS, "Import in progress", null));
    }

    public void markFinished(Long examinationId, List<Long> datasetIds) {
        if (examinationId == null)
            return;
        statusByExamination.put(examinationId,
                new DatasetsImportStatus(examinationId, State.FINISHED, "Import finished", datasetIds));
    }

    public void markError(Long examinationId, String message) {
        if (examinationId == null)
            return;
        statusByExamination.put(examinationId, new DatasetsImportStatus(examinationId, State.ERROR, message, null));
    }

    public DatasetsImportStatus find(Long examinationId) {
        return statusByExamination.get(examinationId);
    }

}

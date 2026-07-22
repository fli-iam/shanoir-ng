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

package org.shanoir.ng.importer;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.shanoir.ng.importer.model.ImportJobBase;
import org.shanoir.ng.importer.model.ImportJobStatus;
import org.shanoir.ng.importer.model.ImportJobStatus.State;
import org.springframework.stereotype.Service;

/**
 * In-memory tracking of import job status, keyed by tempDirId (== last path
 * segment of workFolder).
 *
 * NOTE: this is per-JVM-instance state. If ms-import is horizontally scaled,
 * replace this with a shared store (Redis / DB table) since a poll request
 * may land on a different instance than the one processing the job.
 */
@Service
public class ImportJobStatusService {

    private final Map<String, ImportJobStatus> statuses = new ConcurrentHashMap<>();

    public void setInProgress(String tempDirId, String message) {
        ImportJobStatus status = new ImportJobStatus();
        status.setState(State.IN_PROGRESS);
        status.setMessage(message);
        statuses.put(tempDirId, status);
    }

    public void setFinished(String tempDirId, ImportJobBase importJob) {
        ImportJobStatus status = statuses.computeIfAbsent(tempDirId, k -> new ImportJobStatus());
        status.setState(State.FINISHED);
        status.setMessage("Import finished in MS Import, handed off to MS Datasets.");
        status.setImportJob(importJob);
    }

    public void setError(String tempDirId, String message) {
        ImportJobStatus status = statuses.computeIfAbsent(tempDirId, k -> new ImportJobStatus());
        status.setState(State.ERROR);
        status.setMessage(message);
    }

    public ImportJobStatus getStatus(String tempDirId) {
        return statuses.get(tempDirId);
    }

    public void remove(String tempDirId) {
        statuses.remove(tempDirId);
    }

    /**
     * Recover the status key from a workFolder, whether it's a bare name
     * (zip-upload flow) or an absolute path (tempDirId flow).
     */
    public static String keyOf(String workFolder) {
        return new java.io.File(workFolder).getName();
    }

}

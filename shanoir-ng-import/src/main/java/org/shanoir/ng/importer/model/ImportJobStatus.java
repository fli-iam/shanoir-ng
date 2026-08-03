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

package org.shanoir.ng.importer.model;

public class ImportJobStatus {

    public enum State {
        IN_PROGRESS,
        FINISHED,
        ERROR
    }

    private State state = State.IN_PROGRESS;

    private String message;

    /**
     * Only populated one state == FINISHED: the ImportJob as it was right
     * before being handed off to MS Datasets via RabbitMQ.
     */
    private ImportJobBase importJob;

    public State getState() {
        return state;
    }

    public void setState(State state) {
        this.state = state;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public ImportJobBase getImportJob() {
        return importJob;
    }

    public void setImportJob(ImportJobBase importJob) {
        this.importJob = importJob;
    }
}

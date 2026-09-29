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

package org.shanoir.ng.vip.executionTemplate.model;

import java.util.List;

public class ExecutionInQueue {
    private ExecutionTemplate template;
    private Long objectId; //DatasetID or AcquisitionID or ExaminationID
    private String type;
    private List<Long> plannedExecutionToRemove;

    public ExecutionInQueue(ExecutionTemplate template, Long datasetId, String type, List<Long> plannedExecutionToRemove) {
        this.template = template;
        this.objectId = datasetId;
        this.type = type;
        this.plannedExecutionToRemove = plannedExecutionToRemove;
    }

    public ExecutionTemplate getTemplate() {
        return template;
    }

    public void setTemplate(ExecutionTemplate template) {
        this.template = template;
    }

    public Long getObjectId() {
        return objectId;
    }

    public void setObjectId(Long objectId) {
        this.objectId = objectId;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public List<Long> getPlannedExecutionToRemove() {
        return plannedExecutionToRemove;
    }

    public void setPlannedExecutionToRemove(List<Long> plannedExecutionToRemove) {
        this.plannedExecutionToRemove = plannedExecutionToRemove;
    }
}

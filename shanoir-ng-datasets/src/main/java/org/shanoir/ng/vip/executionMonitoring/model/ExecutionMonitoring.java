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

package org.shanoir.ng.vip.executionMonitoring.model;

import jakarta.persistence.*;
import org.shanoir.ng.processing.model.DatasetProcessing;

/**
 * extension of the DatasetProcessing for VIP execution context
 *
 * @author KhalilKes
 */

@Entity
public class ExecutionMonitoring extends DatasetProcessing {

    private static final long serialVersionUID = -6123167560220373977L;

    private String identifier;

    private String name;

    private String pipelineIdentifier;

    private int timeout;

    private String outputProcessing;

    private ExecutionStatus status;

    private String resultsLocation;

    private Long startDate;

    private Long endDate;


    public String getIdentifier() {
        return identifier;
    }
    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
    public String getPipelineIdentifier() {
        return pipelineIdentifier;
    }
    public void setPipelineIdentifier(String pipelineIdentifier) {
        this.pipelineIdentifier = pipelineIdentifier;
    }
    public int getTimeout() {
        return timeout;
    }
    public void setTimeout(int timeout) {
        this.timeout = timeout;
    }
    public ExecutionStatus getStatus() {
        return status;
    }
    public void setStatus(ExecutionStatus status) {
        this.status = status;
    }

    public String getResultsLocation() {
        return resultsLocation;
    }
    public void setResultsLocation(String resultsLocation) {
        this.resultsLocation = resultsLocation;
    }
    public Long getStartDate() {
        return startDate;
    }
    public void setStartDate(Long startDate) {
        this.startDate = startDate;
    }
    public Long getEndDate() {
        return endDate;
    }
    public void setEndDate(Long endDate) {
        this.endDate = endDate;
    }
    public String getOutputProcessing() {
        return outputProcessing;
    }
    public void setOutputProcessing(String outputProcessing) {
        this.outputProcessing = outputProcessing;
    }

}

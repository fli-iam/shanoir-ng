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

package org.shanoir.ng.vip.execution.dto;

import org.shanoir.ng.vip.executionMonitoring.model.ExecutionStatus;

import java.util.List;
import java.util.Map;


public class VipExecutionDTO {

    private String identifier;
    private String name;
    private String pipelineIdentifier;
    private int timeout;
    private ExecutionStatus status;
    private Map<String, java.lang.Object> inputValues;
    private Map<String, List<java.lang.Object>> returnedFiles;
    private String studyIdentifier;
    private Integer errorCode;
    private Long startDate;
    private Long endDate;
    private String resultsLocation;
    private String sorting;

    public VipExecutionDTO() { }

    public VipExecutionDTO(String identifier, ExecutionStatus status) {
        this.identifier = identifier;
        this.status = status;
    }

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

    public Map<String, java.lang.Object> getInputValues() {
        return inputValues;
    }

    public void setInputValues(Map<String, java.lang.Object> inputValues) {
        this.inputValues = inputValues;
    }

    public Map<String, List<java.lang.Object>> getReturnedFiles() {
        return returnedFiles;
    }

    public void setReturnedFiles(Map<String, List<java.lang.Object>> returnedFiles) {
        this.returnedFiles = returnedFiles;
    }

    public String getStudyIdentifier() {
        return studyIdentifier;
    }

    public void setStudyIdentifier(String studyIdentifier) {
        this.studyIdentifier = studyIdentifier;
    }

    public Integer getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(Integer errorCode) {
        this.errorCode = errorCode;
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

    public String getResultsLocation() {
        return resultsLocation;
    }

    public void setResultsLocation(String resultsLocation) {
        this.resultsLocation = resultsLocation;
    }

    public String getSorting() {
        return sorting;
    }

    public void setSorting(String sorting) {
        this.sorting = sorting;
    }
}

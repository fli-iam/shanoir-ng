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

package org.shanoir.ng.vip.monitoring.model;

import org.shanoir.ng.processing.dto.ParameterResourcesDTO;

import java.util.List;
import java.util.Map;

/**
 * This class represents the DTO for {@link Execution} model class.
 */
public class ExecutionDTO {
    private String identifier;
    private String name;
    private String pipelineIdentifier;
    private int timeout;
    private ExecutionStatus status;
    private Map<String, List<String>> inputValues;
    private Map<String, List<Object>> returnedFiles;
    private List<ParameterResourcesDTO> parametersRessources;
    private String studyIdentifier;
    private Integer errorCode;
    private Long startDate;
    private Long endDate;
    private String resultsLocation;
    private String outputProcessing;
    private String processingType;
    private Pipeline pipeline;
    private String refreshToken;
    private String exportFormat = "dcm";
    private String client = "shanoir-ng-front";

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

    public Map<String, List<String>> getInputValues() {
        return inputValues;
    }

    public void setInputValues(Map<String, List<String>> inputValues) {
        this.inputValues = inputValues;
    }

    public Map<String, List<Object>> getReturnedFiles() {
        return returnedFiles;
    }

    public void setReturnedFiles(Map<String, List<Object>> returnedFiles) {
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

    public String getOutputProcessing() {
        return outputProcessing;
    }

    public void setOutputProcessing(String outputProcessing) {
        this.outputProcessing = outputProcessing;
    }

    public String getProcessingType() {
        return processingType;
    }

    public void setProcessingType(String processingType) {
        this.processingType = processingType;
    }


    public List<ParameterResourcesDTO> getParametersRessources() {
        return parametersRessources;
    }

    public void setParametersRessources(List<ParameterResourcesDTO> parametersRessources) {
        this.parametersRessources = parametersRessources;
    }

    public Pipeline getPipeline() {
        return pipeline;
    }

    public void setPipeline(Pipeline pipeline) {
        this.pipeline = pipeline;
    }

    public String getRefreshToken() {
        return refreshToken;
    }

    public void setRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }

    public String getExportFormat() {
        return exportFormat;
    }

    public void setExportFormat(String exportFormat) {
        this.exportFormat = exportFormat;
    }

    public String getClient() {
        return client;
    }

    public void setClient(String client) {
        this.client = client;
    }

}

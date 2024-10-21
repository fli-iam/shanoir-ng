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

package org.shanoir.ng.vip.dto;

import jakarta.validation.constraints.NotNull;

import java.util.List;
import java.util.Map;

/**
 * This class represents the DTO for {@link VipExecutionDTO} model class.
 */
public class ExecutionCandidateDTO {
    @NotNull(message = "Execution name must be provided")
    private String name;
    @NotNull(message = "Pipeline identifier must be provided")
    private String pipelineIdentifier;
    private Map<String, List<String>> inputParameters;
    private List<DatasetParameterDTO> datasetParameters;
    private Long studyIdentifier;
    private String outputProcessing;
    private String processingType;
    @NotNull(message = "Refresh token must be provided")
    private String refreshToken;
    @NotNull(message = "Client must be provided")
    private String client;
    private Long converterId;

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

    public Long getStudyIdentifier() {
        return studyIdentifier;
    }

    public void setStudyIdentifier(Long studyIdentifier) {
        this.studyIdentifier = studyIdentifier;
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


    public List<DatasetParameterDTO> getDatasetParameters() {
        return datasetParameters;
    }

    public void setDatasetParameters(List<DatasetParameterDTO> parametersResources) {
        this.datasetParameters = parametersResources;
    }

    public String getRefreshToken() {
        return refreshToken;
    }

    public void setRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }

    public String getClient() {
        return client;
    }

    public void setClient(String client) {
        this.client = client;
    }

    public Map<String, List<String>> getInputParameters() {
        return inputParameters;
    }

    public void setInputParameters(Map<String, List<String>> inputParameters) {
        this.inputParameters = inputParameters;
    }


    public Long getConverterId() {
        return converterId;
    }

    public void setConverterId(Long converterId) {
        this.converterId = converterId;
    }
}

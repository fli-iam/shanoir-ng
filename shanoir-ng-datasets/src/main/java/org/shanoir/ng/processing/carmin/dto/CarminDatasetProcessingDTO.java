package org.shanoir.ng.processing.carmin.dto;

import org.shanoir.ng.processing.carmin.model.ExecutionStatus;
import org.shanoir.ng.processing.dto.DatasetProcessingDTO;


/**
 * DTO for CarminDatasetProcessing
 * 
 * @author KhalilKes
 */
public class CarminDatasetProcessingDTO extends DatasetProcessingDTO {

    private String Identifier;

    private String name;

    private String pipelineIdentifier;

    private int timeout;
    
    private ExecutionStatus status;

    private String resultsLocation;

    private Long startDate;
    
    private Long endDate;

    
    public String getIdentifier() {
        return Identifier;
    }

    public void setIdentifier(String identifier) {
        Identifier = identifier;
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
    
}

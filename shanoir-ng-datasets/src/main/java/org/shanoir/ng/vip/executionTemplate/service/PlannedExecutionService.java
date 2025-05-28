package org.shanoir.ng.vip.executionTemplate.service;

import java.util.List;
import java.util.Map;

public interface PlannedExecutionService {

    /**
     * This method starts planned executions according to the execution templates for all the given dataset acquisitions
     *
     * @param createdAcquisitionsPerTemplatesId
     */
    void applyExecution(Map<Long, List<Long>> createdAcquisitionsPerTemplatesId);

    /**
     * This method save planned executions by pairing every acquisitions template with their relevant execution template
     * @param createdAcquisitionsPerTemplatesId
     */
    void savePlannedExecution(Map<Long, List<Long>> createdAcquisitionsPerTemplatesId);
}

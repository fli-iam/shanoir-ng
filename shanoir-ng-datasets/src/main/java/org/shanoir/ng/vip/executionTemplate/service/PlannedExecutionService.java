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

package org.shanoir.ng.vip.executionTemplate.service;

import org.shanoir.ng.vip.execution.dto.ExecutionCandidateDTO;
import org.shanoir.ng.vip.executionTemplate.model.ExecutionInQueue;
import org.shanoir.ng.vip.executionTemplate.model.ExecutionTemplate;

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

    /**
     * Prepare the execution candidate DTO relatively to the given templateId and acquisitionId
     * @param template the execution template on which is based the execution
     * @param executionLevel the group scale according to which we gather datasets for executions
     * @param objectId the object id corresponding to the executionLevel
     *
     * @return the prepared execution to send to VIP
     */
    ExecutionCandidateDTO prepareExecutionCandidate(ExecutionTemplate template, String executionLevel, Long objectId);

    List<Long> getInvolvedData(ExecutionInQueue execution);
}

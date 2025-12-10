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

package org.shanoir.ng.vip.execution.service;

import java.util.List;

import org.shanoir.ng.dataset.model.Dataset;
import org.shanoir.ng.shared.core.model.IdName;
import org.shanoir.ng.shared.exception.EntityNotFoundException;
import org.shanoir.ng.shared.exception.RestServiceException;
import org.shanoir.ng.shared.exception.SecurityException;
import org.shanoir.ng.vip.execution.dto.ExecutionCandidateDTO;
import org.shanoir.ng.vip.execution.dto.VipExecutionDTO;
import org.shanoir.ng.vip.output.exception.ResultHandlerException;
import org.shanoir.ng.vip.shared.dto.DatasetParameterDTO;

import reactor.core.publisher.Mono;

public interface ExecutionService {

    /**
     * Update monitoring with vip execution details and persist it in DB
     *
     * @param candidate
     * @param inputDatasets
     * @return Id and Name of execution
     * @throws EntityNotFoundException
     */
    IdName createExecution(ExecutionCandidateDTO candidate, List<Dataset> inputDatasets) throws SecurityException, EntityNotFoundException, RestServiceException;

    /**
     * Get datasets from JSON id values
     *
     * @param parameters
     * @return List of datasets
     */
    List<Dataset> getDatasetsFromParams(List<DatasetParameterDTO> parameters);

    /**
     * Get datasets from JSON id values
     *
     * @param datasets
     */
    void checkRightsForExecution(List<Dataset> datasets) throws EntityNotFoundException, RestServiceException;

    /**
     *
     * Get execution from <a href="https://app.swaggerhub.com/apis/CARMIN/carmin-common_api_for_research_medical_imaging_network/0.3.1#/default/getExecution">VIP API</a>
     *
     * @param identifier
     * @return Execution
     */
    Mono<VipExecutionDTO> getExecution(String identifier);

    /**
     * Get execution stderr logs from <a href="https://app.swaggerhub.com/apis/CARMIN/carmin-common_api_for_research_medical_imaging_network/0.3.1#/default/getStderr">VIP API</a>
     *
     * @param identifier
     * @return string
     */
    Mono<String> getExecutionStderr(String identifier);

    /**
     * Get execution stdout logs from <a href="https://app.swaggerhub.com/apis/CARMIN/carmin-common_api_for_research_medical_imaging_network/0.3.1#/default/getStdout">VIP API</a>
     *
     * @param identifier
     * @return string
     */
    Mono<String> getExecutionStdout(String identifier);

    /**
     * Try to get execution from <a href="https://app.swaggerhub.com/apis/CARMIN/carmin-common_api_for_research_medical_imaging_network/0.3.1#/default/getExecution">VIP API</a>
     * Authenticate as service account
     * @param attempts
     * @param identifier
     * @return
     * @throws ResultHandlerException
     */
    Mono<VipExecutionDTO> getExecutionAsServiceAccount(int attempts, String identifier) throws ResultHandlerException, SecurityException;
}

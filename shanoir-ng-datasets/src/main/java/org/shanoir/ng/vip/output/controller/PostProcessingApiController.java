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

package org.shanoir.ng.vip.output.controller;

import io.swagger.v3.oas.annotations.Parameter;
import org.shanoir.ng.dataset.model.Dataset;
import org.shanoir.ng.processing.model.DatasetProcessingType;
import org.shanoir.ng.processing.repository.DatasetProcessingRepository;
import org.shanoir.ng.shared.core.model.IdName;
import org.shanoir.ng.shared.exception.EntityNotFoundException;
import org.shanoir.ng.shared.exception.RestServiceException;
import org.shanoir.ng.shared.exception.SecurityException;
import org.shanoir.ng.study.rights.ampq.RabbitMqStudyUserService;
import org.shanoir.ng.vip.execution.dto.ExecutionCandidateDTO;
import org.shanoir.ng.vip.execution.dto.VipExecutionDTO;
import org.shanoir.ng.vip.execution.service.ExecutionServiceImpl;
import org.shanoir.ng.vip.executionMonitoring.model.ExecutionStatus;
import org.shanoir.ng.vip.output.exception.ResultHandlerException;
import org.shanoir.ng.vip.output.service.PostProcessingService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;
import java.util.Objects;

@Controller
public class PostProcessingApiController implements PostProcessingApi {

    @Autowired
    PostProcessingService processingService;

    @Autowired
    private DatasetProcessingRepository processingRepository;

    private static final Logger LOG = LoggerFactory.getLogger(PostProcessingApiController.class);

    @Override
    public ResponseEntity<IdName> launchPostProcessing(String name, String comment) {
        Integer processingTypeId = DatasetProcessingType.getIdFromString(name);
        if(Objects.nonNull(processingTypeId)) {
            try {
                List<Long> processingIds = processingRepository.findIdsByCommentAndDatasetProcessingTypeWithStatusFinished(comment, processingTypeId);
                processingService.launchPostProcessing(processingIds, comment);
                return new ResponseEntity<>(HttpStatus.OK);
            } catch (ResultHandlerException e) {
                    LOG.error(e.getMessage(), e);
            }
        }
        LOG.error("No processing type found for name {}", name);
        return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
    }
}

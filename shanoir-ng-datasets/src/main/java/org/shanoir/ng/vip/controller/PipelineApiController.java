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

package org.shanoir.ng.vip.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.v3.oas.annotations.Parameter;
import org.shanoir.ng.dataset.model.Dataset;
import org.shanoir.ng.dataset.security.DatasetSecurityService;
import org.shanoir.ng.dataset.service.DatasetService;
import org.shanoir.ng.processing.dto.ParameterResourceDTO;
import org.shanoir.ng.processing.model.DatasetProcessingType;
import org.shanoir.ng.shared.core.model.IdName;
import org.shanoir.ng.shared.exception.EntityNotFoundException;
import org.shanoir.ng.shared.exception.SecurityException;
import org.shanoir.ng.utils.KeycloakUtil;
import org.shanoir.ng.vip.dto.DatasetParameterDTO;
import org.shanoir.ng.vip.dto.ExecutionCandidateDTO;
import org.shanoir.ng.vip.dto.VipExecutionDTO;
import org.shanoir.ng.vip.monitoring.model.ExecutionMonitoring;
import org.shanoir.ng.vip.monitoring.model.ExecutionStatus;
import org.shanoir.ng.vip.monitoring.schedule.ExecutionStatusMonitorService;
import org.shanoir.ng.vip.monitoring.service.ExecutionMonitoringService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
public class PipelineApiController implements PipelineApi {

    @Autowired
    private VipClientService vipClient;

    @Override
    public ResponseEntity<String> getPipelineAll() {
        return ResponseEntity.ok(vipClient.getPipelineAll().block());
    }

    /**
     * @param identifier
     * @return
     */
    @Override
    public ResponseEntity<String> getPipeline(String identifier, String version) {
        return ResponseEntity.ok(vipClient.getPipeline(identifier, version).block());
    }
}

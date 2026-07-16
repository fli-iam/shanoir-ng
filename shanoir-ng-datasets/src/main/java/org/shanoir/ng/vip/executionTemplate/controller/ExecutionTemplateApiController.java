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

package org.shanoir.ng.vip.executionTemplate.controller;

import io.swagger.v3.oas.annotations.Parameter;

import org.shanoir.ng.vip.executionTemplate.dto.ExecutionTemplateDTO;
import org.shanoir.ng.vip.executionTemplate.dto.mapper.ExecutionTemplateMapper;
import org.shanoir.ng.vip.executionTemplate.model.ExecutionTemplate;
import org.shanoir.ng.vip.executionTemplate.repository.ExecutionTemplateRepository;
import org.shanoir.ng.vip.executionTemplate.service.ExecutionTemplateService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

/*
    Execution template are VIP execution automatically applied after an import.
 */
@Controller
public class ExecutionTemplateApiController implements ExecutionTemplateApi {

    @Autowired
    private ExecutionTemplateRepository repository;

    @Autowired
    private ExecutionTemplateMapper mapper;

    @Autowired
    private ExecutionTemplateService service;

    public ResponseEntity<List<ExecutionTemplateDTO>> getExecutionTemplatesByStudyId(@Parameter(description = "The study Id", required = true) @PathVariable("studyId") Long studyId) {
        List<ExecutionTemplate> executions = repository.findByStudyId(studyId);
        return new ResponseEntity<>(mapper.executionTemplatesToDTOs(executions), HttpStatus.OK);
    }

    public ResponseEntity<ExecutionTemplateDTO> saveNewExecutionTemplate(@Parameter(description = "The execution template to create", required = true) @RequestBody ExecutionTemplateDTO executionTemplateDTO) {
        return new ResponseEntity<>(mapper.executionTemplateToDTO(repository.save(mapper.executionTemplateDTOToEntity(executionTemplateDTO))), HttpStatus.OK);
    }

    public ResponseEntity<Void> deleteExecutionTemplate(@Parameter(description = "The execution template Id", required = true) @PathVariable("executionTemplateId") Long executionTemplateId) {
        repository.findById(executionTemplateId).ifPresent(repository::delete);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    public ResponseEntity<ExecutionTemplateDTO> getExecutionTemplateById(@Parameter(description = "The execution template Id", required = true) @PathVariable("executionTemplateId") Long executionTemplateId) {
        return new ResponseEntity<>(mapper.executionTemplateToDTO(repository.findById(executionTemplateId).orElse(null)), HttpStatus.OK);
    }

    public ResponseEntity<ExecutionTemplateDTO> updateExecutionTemplate(@Parameter(description = "The execution template updated", required = true) ExecutionTemplateDTO executionTemplateDTO,
                                                                        @Parameter(description = "The execution template Id", required = true) @PathVariable("parameterId") Long parameterId) {
        return new ResponseEntity<>(mapper.executionTemplateToDTO(repository.save(service.update(mapper.executionTemplateDTOToEntity(executionTemplateDTO)))), HttpStatus.OK);
    }
}

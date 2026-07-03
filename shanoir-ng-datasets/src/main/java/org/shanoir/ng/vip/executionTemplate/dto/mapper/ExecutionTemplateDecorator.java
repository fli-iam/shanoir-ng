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

package org.shanoir.ng.vip.executionTemplate.dto.mapper;

import org.shanoir.ng.shared.hateoas.Links;
import org.shanoir.ng.shared.repository.StudyRepository;
import org.shanoir.ng.vip.executionTemplate.dto.ExecutionTemplateDTO;
import org.shanoir.ng.vip.executionTemplate.dto.ExecutionTemplateParameterDTO;
import org.shanoir.ng.vip.executionTemplate.model.ExecutionTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class ExecutionTemplateDecorator implements ExecutionTemplateMapper {

    @Autowired
    private StudyRepository studyRepository;

    @Override
    public ExecutionTemplateDTO  executionTemplateToDTO(ExecutionTemplate executionTemplate) {
        if (executionTemplate == null) {
            return null;
        }

        ExecutionTemplateDTO executionTemplateDTO = new ExecutionTemplateDTO();

        executionTemplateDTO.setId(executionTemplate.getId());
        Links links = executionTemplate.getLinks();
        if (links != null) {
            Links links1 = new Links();
            links1.putAll(links);
            executionTemplateDTO.setLinks(links1);
        }
        executionTemplateDTO.setFilterCombination(executionTemplate.getFilterCombination());
        executionTemplateDTO.setPriority(executionTemplate.getPriority());
        executionTemplateDTO.setName(executionTemplate.getName());
        executionTemplateDTO.setPipelineName(executionTemplate.getPipelineName());
        executionTemplateDTO.setStudyId(executionTemplate.getStudy().getId());
        executionTemplateDTO.setParameters(ExecutionTemplateParameterDTO.fromEntities(executionTemplate.getParameters()));

        return executionTemplateDTO;
    }

    @Override
    public List<ExecutionTemplateDTO> executionTemplatesToDTOs(List<ExecutionTemplate> executionTemplates) {
        return executionTemplates.stream().map(this::executionTemplateToDTO).collect(Collectors.toList());
    }

    @Override
    public ExecutionTemplate executionTemplateDTOToEntity(ExecutionTemplateDTO dto) {
        if (dto == null) return null;

        ExecutionTemplate entity = new ExecutionTemplate();
        entity.setId(dto.getId());
        entity.setName(dto.getName());
        entity.setPipelineName(dto.getPipelineName());
        entity.setFilterCombination(dto.getFilterCombination());
        entity.setPriority(dto.getPriority());
        entity.setStudy(studyRepository.findById(dto.getStudyId()).orElse(null));
        entity.setParameters(ExecutionTemplateParameterDTO.toEntities(dto.getParameters(), entity));
        return entity;
    }
}

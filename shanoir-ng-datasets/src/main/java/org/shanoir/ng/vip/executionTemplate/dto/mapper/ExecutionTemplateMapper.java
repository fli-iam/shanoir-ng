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

import org.mapstruct.*;
import org.shanoir.ng.shared.mapper.StudyMapper;
import org.shanoir.ng.vip.executionTemplate.dto.ExecutionTemplateDTO;
import org.shanoir.ng.vip.executionTemplate.model.ExecutionTemplate;

import java.util.List;

@Mapper(componentModel = "spring", uses = {StudyMapper.class})
public interface ExecutionTemplateMapper {

    ////// Entity to DTO

    // Single entity

    @Named("idRelations")
    @Mapping(target = "studyId", source = "study", qualifiedByName = "id")
    ExecutionTemplateDTO executionTemplateToDTOWithIdRelations(ExecutionTemplate executionTemplate);

    // Entity list

    @IterableMapping(qualifiedByName = "idRelations")
    List<ExecutionTemplateDTO> executionTemplateListToDTOListWithStudy(List<ExecutionTemplate> executionTemplates);

    ////// DTO toEntity

    // Single entity
    @Named("ExecutionTemplateDTOToEntity")
    @Mapping(target = "study", source = "studyId", qualifiedByName = "idOnly")
    ExecutionTemplate executionTemplateDTOToEntity(ExecutionTemplateDTO dto);

}

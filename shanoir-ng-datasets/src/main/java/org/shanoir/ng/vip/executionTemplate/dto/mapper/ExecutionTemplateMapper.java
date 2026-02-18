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

import org.mapstruct.DecoratedWith;
import org.mapstruct.IterableMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Named;
import org.shanoir.ng.vip.executionTemplate.dto.ExecutionTemplateDTO;
import org.shanoir.ng.vip.executionTemplate.model.ExecutionTemplate;

import java.util.List;

@Mapper(componentModel = "spring")
@DecoratedWith(ExecutionTemplateDecorator.class)
public interface ExecutionTemplateMapper {

    @Named("ExecutionTemplateToDTO")
    ExecutionTemplateDTO executionTemplateToDTO(ExecutionTemplate executionTemplate);

    @IterableMapping(qualifiedByName = "ExecutionTemplateToDTO")
    List<ExecutionTemplateDTO> executionTemplatesToDTOs(List<ExecutionTemplate> executionTemplates);

    @Named("ExecutionTemplateDTOToEntity")
    ExecutionTemplate executionTemplateDTOToEntity(ExecutionTemplateDTO dto);

}

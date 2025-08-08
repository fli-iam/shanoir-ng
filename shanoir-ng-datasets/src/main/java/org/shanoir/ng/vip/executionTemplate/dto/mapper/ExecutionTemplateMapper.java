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
    ExecutionTemplateDTO ExecutionTemplateToDTO(ExecutionTemplate executionTemplate);

    @IterableMapping(qualifiedByName = "ExecutionTemplateToDTO")
    List<ExecutionTemplateDTO> ExecutionTemplatesToDTOs(List<ExecutionTemplate> executionTemplates);

    @Named("ExecutionTemplateDTOToEntity")
    ExecutionTemplate ExecutionTemplateDTOToEntity(ExecutionTemplateDTO dto);

}

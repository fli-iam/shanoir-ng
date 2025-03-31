package org.shanoir.ng.vip.executionTemplate.dto.mapper;

import org.mapstruct.IterableMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mappings;
import org.mapstruct.Named;
import org.shanoir.ng.vip.executionTemplate.dto.ExecutionTemplateDTO;
import org.shanoir.ng.vip.executionTemplate.model.ExecutionTemplate;

import java.util.List;

@Mapper(componentModel = "spring", uses = { })
public interface ExecutionTemplateMapper {

    @Named("ExecutionTemplateToDTO")
    @Mappings({
    })
    ExecutionTemplateDTO ExecutionTemplateToDTO(ExecutionTemplate executionTemplate);


    @IterableMapping(qualifiedByName = "ExecutionTemplateToDTO")
    List<ExecutionTemplateDTO> ExecutionTemplatesToDTOs(List<ExecutionTemplate> executionTemplates);
}

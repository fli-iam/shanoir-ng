package org.shanoir.ng.vip.executionTemplate.dto.mapper;

import org.mapstruct.IterableMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mappings;
import org.mapstruct.Named;
import org.shanoir.ng.vip.executionTemplate.dto.ExecutionTemplateParameterDTO;
import org.shanoir.ng.vip.executionTemplate.model.ExecutionTemplateParameter;

import java.util.List;

@Mapper(componentModel = "spring", uses = { })
public interface ExecutionTemplateParameterMapper {

    @Named("ExecutionTemplateParameterToDTO")
    @Mappings({})
    ExecutionTemplateParameterDTO ExecutionTemplateParameterToDTO(ExecutionTemplateParameter executionTemplateParameter);

    @IterableMapping(qualifiedByName = "ExecutionTemplateParameterToDTO")
    List<ExecutionTemplateParameterDTO> ExecutionTemplateParametersToDTOs(List<ExecutionTemplateParameter> executionTemplateParameters);
}
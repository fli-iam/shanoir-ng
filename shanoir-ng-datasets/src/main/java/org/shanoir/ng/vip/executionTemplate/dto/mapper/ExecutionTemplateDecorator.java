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
    public ExecutionTemplateDTO  ExecutionTemplateToDTO(ExecutionTemplate executionTemplate) {
        if ( executionTemplate == null ) {
            return null;
        }

        ExecutionTemplateDTO executionTemplateDTO = new ExecutionTemplateDTO();

        executionTemplateDTO.setId( executionTemplate.getId() );
        Links links = executionTemplate.getLinks();
        if ( links != null ) {
            Links links1 = new Links();
            links1.putAll( links );
            executionTemplateDTO.setLinks( links1 );
        }
        executionTemplateDTO.setFilterCombination( executionTemplate.getFilterCombination() );
        executionTemplateDTO.setPriority( executionTemplate.getPriority() );
        executionTemplateDTO.setName( executionTemplate.getName() );
        executionTemplateDTO.setPipelineName( executionTemplate.getPipelineName() );
        executionTemplateDTO.setStudyId( executionTemplate.getStudy().getId() );

        executionTemplateDTO.setParameters(ExecutionTemplateParameterDTO.fromEntities(executionTemplate.getParameters()));

        return executionTemplateDTO;
    }

    @Override
    public List<ExecutionTemplateDTO> ExecutionTemplatesToDTOs(List<ExecutionTemplate> executionTemplates) {
        return executionTemplates.stream().map(this::ExecutionTemplateToDTO).collect(Collectors.toList());
    }

    @Override
    public ExecutionTemplate ExecutionTemplateDTOToEntity(ExecutionTemplateDTO dto){
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
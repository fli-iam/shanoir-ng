package org.shanoir.ng.vip.executionTemplate.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.shanoir.ng.shared.hateoas.HalEntity;
import org.shanoir.ng.vip.executionTemplate.model.ExecutionTemplate;
import org.shanoir.ng.vip.executionTemplate.model.ExecutionTemplateParameter;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * This class represents the associated criterias for an automatic execution filters used after an import in shanoir.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class ExecutionTemplateParameterDTO extends HalEntity {

    private Long executionTemplateId;
    private String name;
    private String value;

    public Long getExecutionTemplateId() {return executionTemplateId;}

    public void setExecutionTemplateId(Long executionTemplateId) {this.executionTemplateId = executionTemplateId;}

    public String getName() {return name;}

    public void setName(String name) {this.name = name;}

    public String getValue() {return value;}

    public void setValue(String value) {this.value = value;}

    /**
     * For breaking nested loadings with parameter and template. Parameter does not need template datas in front-end logic.
     */
    public static ExecutionTemplateParameterDTO fromEntity(ExecutionTemplateParameter parameter) {
        if (Objects.isNull(parameter)) {return null;}

        ExecutionTemplateParameterDTO dto = new ExecutionTemplateParameterDTO();
        dto.setId(parameter.getId());
        dto.setName(parameter.getName());
        dto.setValue(parameter.getValue());
        dto.setExecutionTemplateId(parameter.getExecutionTemplate() != null ? parameter.getExecutionTemplate().getId() : null
        );
        return dto;
    }

    /**
     * For breaking nested loadings with parameters and templates. Parameters do not need template datas in front-end logic.
     */
    public static List<ExecutionTemplateParameterDTO> fromEntities(List<ExecutionTemplateParameter> parameters) {
        List<ExecutionTemplateParameterDTO> parameterDTOList = new ArrayList<>();
        parameters.forEach(parameter -> parameterDTOList.add(ExecutionTemplateParameterDTO.fromEntity(parameter)));
        return parameterDTOList;
    }

    public static List<ExecutionTemplateParameter> toEntities(List<ExecutionTemplateParameterDTO> parameters, ExecutionTemplate template) {
        if (parameters != null) {
            return parameters.stream().map(parameter -> toEntity(parameter, template)).collect(Collectors.toList());
        }
        return new ArrayList<>();
    }

    public static ExecutionTemplateParameter toEntity(ExecutionTemplateParameterDTO parameterDTO, ExecutionTemplate template) {
        ExecutionTemplateParameter parameter = new ExecutionTemplateParameter();
        parameter.setId(parameterDTO.getId());
        parameter.setName(parameterDTO.getName());
        parameter.setValue(parameterDTO.getValue());
        parameter.setExecutionTemplate(template);
        return parameter;
    }
}
package org.shanoir.ng.vip.executionTemplate.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.shanoir.ng.shared.hateoas.HalEntity;
import org.shanoir.ng.vip.executionTemplate.model.ExecutionTemplate;
import org.shanoir.ng.vip.executionTemplate.model.ExecutionTemplateFilter;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * This class represents the associated criterias for an automatic execution filters used after an import in shanoir.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class ExecutionTemplateFilterDTO extends HalEntity {

    private String fieldName;
    private String comparedRegex;
    private boolean excluded;
    private int identifier;
    private Long executionTemplateId;

    public String getFieldName() {return fieldName;}

    public void setFieldName(String fieldName) {this.fieldName = fieldName;}

    public String getComparedRegex() {return comparedRegex;}

    public void setComparedRegex(String comparedRegex) {this.comparedRegex = comparedRegex;}

    public boolean isExcluded() {return excluded;}

    public void setExcluded(boolean excluded) {this.excluded = excluded;}

    public int getIdentifier() {return identifier;}

    public void setIdentifier(int identifier) {this.identifier = identifier;}

    public Long getExecutionTemplateId() {return executionTemplateId;}

    public void setExecutionTemplateId(Long executionTemplateId) {this.executionTemplateId = executionTemplateId;}

    /**
     * For breaking nested loadings with filter and template. Filter does not need template datas in front-end logic.
     */
    public static ExecutionTemplateFilterDTO fromEntity(ExecutionTemplateFilter filter) {
        if (Objects.isNull(filter)) {return null;}

        ExecutionTemplateFilterDTO dto = new ExecutionTemplateFilterDTO();
        dto.setId(filter.getId());
        dto.setFieldName(filter.getFieldName());
        dto.setExcluded(filter.isExcluded());
        dto.setIdentifier(filter.getIdentifier());
        dto.setComparedRegex(filter.getComparedRegex());
        dto.setExecutionTemplateId(
                filter.getExecutionTemplate() != null ? filter.getExecutionTemplate().getId() : null
        );
        return dto;
    }

    /**
     * For breaking nested loadings with parameters and templates. Parameters do not need template datas in front-end logic.
     */
    public static List<ExecutionTemplateFilterDTO> fromEntities(List<ExecutionTemplateFilter> filters) {
        List<ExecutionTemplateFilterDTO> filterDTOList = new ArrayList<>();
        filters.forEach(filter -> filterDTOList.add(ExecutionTemplateFilterDTO.fromEntity(filter)));
        return filterDTOList;
    }

    public static List<ExecutionTemplateFilter> toEntities(List<ExecutionTemplateFilterDTO> filters, ExecutionTemplate template) {
        if (filters != null) {
            return filters.stream().map(filter -> toEntity(filter, template)).collect(Collectors.toList());
        }
        return new ArrayList<>();
    }

    public static ExecutionTemplateFilter toEntity(ExecutionTemplateFilterDTO filterDTO, ExecutionTemplate template) {
        ExecutionTemplateFilter filter = new ExecutionTemplateFilter();
        filter.setId(filterDTO.getId());
        filter.setFieldName(filterDTO.getFieldName());
        filter.setComparedRegex(filterDTO.getComparedRegex());
        filter.setExcluded(filterDTO.isExcluded());
        filter.setIdentifier(filterDTO.getIdentifier());
        filter.setExecutionTemplate(template);
        return filter;
    }
}

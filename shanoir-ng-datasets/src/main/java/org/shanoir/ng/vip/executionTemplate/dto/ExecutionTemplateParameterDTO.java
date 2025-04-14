package org.shanoir.ng.vip.executionTemplate.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.shanoir.ng.shared.hateoas.HalEntity;

/**
 * This class represents the associated criterias for an automatic execution parameter used after an import in shanoir.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class ExecutionTemplateParameterDTO extends HalEntity {

    private String name;
    private Long executionTemplateId;
    private String groupBy;
    private String filter;

    public String getName() {return name;}

    public void setName(String name) {this.name = name;}

    public Long getExecutionTemplateId() {return executionTemplateId;}

    public void setExecutionTemplateId(Long executionTemplateId) {this.executionTemplateId = executionTemplateId;}

    public String getGroupBy() {return groupBy;}

    public void setGroupBy(String groupBy) {this.groupBy = groupBy;}

    public String getFilter() {return filter;}

    public void setFilter(String filter) {this.filter = filter;}
}

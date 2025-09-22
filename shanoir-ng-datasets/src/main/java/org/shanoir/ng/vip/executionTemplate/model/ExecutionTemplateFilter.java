package org.shanoir.ng.vip.executionTemplate.model;

import jakarta.persistence.*;
import org.shanoir.ng.shared.core.model.AbstractEntity;
import org.shanoir.ng.shared.hateoas.HalEntity;

@Entity
public class ExecutionTemplateFilter extends HalEntity {

    private String fieldName;
    private String comparedRegex;
    private boolean excluded;
    private int identifier;

    @ManyToOne(fetch =  FetchType.EAGER)
    @JoinColumn(name = "execution_template_id")
    private ExecutionTemplate executionTemplate;

    public String getFieldName() {return fieldName;}

    public void setFieldName(String fieldName) {this.fieldName = fieldName;}

    public String getComparedRegex() {return comparedRegex;}

    public void setComparedRegex(String comparedRegex) {this.comparedRegex = comparedRegex;}

    public boolean isExcluded() {return excluded;}

    public void setExcluded(boolean excluded) {this.excluded = excluded;}

    public int getIdentifier() {return identifier;}

    public void setIdentifier(int identifier) {this.identifier = identifier;}

    public ExecutionTemplate getExecutionTemplate() {return executionTemplate;}

    public void setExecutionTemplate(ExecutionTemplate executionTemplate) {this.executionTemplate = executionTemplate;}
}

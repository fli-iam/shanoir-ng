package org.shanoir.ng.vip.executionTemplate.model;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import org.shanoir.ng.shared.core.model.AbstractEntity;

@Entity
public class ExecutionTemplateParameter extends AbstractEntity {

    @ManyToOne(fetch =  FetchType.EAGER)
    @JoinColumn(name = "execution_template_id")
    private ExecutionTemplate executionTemplate;
    private String name;
    private String value;

    public ExecutionTemplate getExecutionTemplate() {return executionTemplate;}

    public void setExecutionTemplate(ExecutionTemplate executionTemplate) {this.executionTemplate = executionTemplate;}

    public String getName() {return name;}

    public void setName(String name) {this.name = name;}

    public String getValue() {return value;}

    public void setValue(String value) {this.value = value;}
}

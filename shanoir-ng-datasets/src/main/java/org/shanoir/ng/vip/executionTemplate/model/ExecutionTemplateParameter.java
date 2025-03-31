package org.shanoir.ng.vip.executionTemplate.model;

import jakarta.persistence.Entity;
import org.shanoir.ng.shared.core.model.AbstractEntity;

@Entity
public class ExecutionTemplateParameter extends AbstractEntity {

    private String name;
    private String type;
    private String value;
    private boolean isOptional;
    private boolean isReturnedValue;
    private String description;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public boolean isOptional() {
        return isOptional;
    }

    public void setOptional(boolean optional) {
        isOptional = optional;
    }

    public boolean isReturnedValue() {
        return isReturnedValue;
    }

    public void setReturnedValue(boolean returnedValue) {
        isReturnedValue = returnedValue;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}

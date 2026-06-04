/**
 * Shanoir NG - Import, manage and share neuroimaging data
 * Copyright (C) 2009-2019 Inria - https://www.inria.fr/
 * Contact us on https://project.inria.fr/shanoir/
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see https://www.gnu.org/licenses/gpl-3.0.html
 */

package org.shanoir.ng.vip.executionTemplate.model;

import jakarta.persistence.*;
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

    public String getFieldName() {
        return fieldName;
    }

    public void setFieldName(String fieldName) {
        this.fieldName = fieldName;
    }

    public String getComparedRegex() {
        return comparedRegex;
    }

    public void setComparedRegex(String comparedRegex) {
        this.comparedRegex = comparedRegex;
    }

    public boolean isExcluded() {
        return excluded;
    }

    public void setExcluded(boolean excluded) {
        this.excluded = excluded;
    }

    public int getIdentifier() {
        return identifier;
    }

    public void setIdentifier(int identifier) {
        this.identifier = identifier;
    }

    public ExecutionTemplate getExecutionTemplate() {
        return executionTemplate;
    }

    public void setExecutionTemplate(ExecutionTemplate executionTemplate) {
        this.executionTemplate = executionTemplate;
    }
}

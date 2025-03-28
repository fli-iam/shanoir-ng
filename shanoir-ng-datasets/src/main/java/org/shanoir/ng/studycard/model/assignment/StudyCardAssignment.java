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

package org.shanoir.ng.studycard.model.assignment;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeInfo.As;
import com.fasterxml.jackson.annotation.JsonTypeInfo.Id;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import org.hibernate.annotations.GenericGenerator;
import org.shanoir.ng.shared.core.model.AbstractEntity;


@Entity
@GenericGenerator(name = "IdOrGenerate", strategy = "org.shanoir.ng.shared.model.UseIdOrGenerate")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name="scope", discriminatorType = DiscriminatorType.STRING)
@JsonTypeInfo(use = Id.NAME, include = As.PROPERTY, property = "scope")
@JsonSubTypes({
        @JsonSubTypes.Type(value = DatasetAssignment.class, name = "Dataset"),
        @JsonSubTypes.Type(value = DatasetAcquisitionAssignment.class, name = "DatasetAcquisition") })
public abstract class StudyCardAssignment<T> extends AbstractEntity implements StudyCardAssignmentInterface<T> {

    /** UID */
    private static final long serialVersionUID = 6708188853533591193L;

    /** The dataset field to update. */
    @NotNull
    protected Long field;
    
    /** The value to set. */
    @NotNull
    private String value;

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}

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

import com.fasterxml.jackson.annotation.JsonTypeName;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import org.shanoir.ng.datasetacquisition.model.DatasetAcquisition;
import org.shanoir.ng.shared.exception.CheckedIllegalClassException;
import org.shanoir.ng.studycard.model.field.DatasetAcquisitionMetadataField;
import org.shanoir.ng.studycard.model.field.MetadataFieldInterface;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


@Entity
@DiscriminatorValue("DatasetAcquisition")
@JsonTypeName("DatasetAcquisition")
public class DatasetAcquisitionAssignment extends StudyCardAssignment<DatasetAcquisition> {

    private static Logger LOG = LoggerFactory.getLogger(DatasetAcquisitionAssignment.class);

    @Override
    public DatasetAcquisitionMetadataField getField() {
        if (field == null) return null;
        else return DatasetAcquisitionMetadataField.getEnum(field.intValue());
    }

    @JsonDeserialize(as = DatasetAcquisitionMetadataField.class)
    @Override // Don't know why eclipse can't take DatasetAcquisitionMetadataField as input type
    public void setField(MetadataFieldInterface<DatasetAcquisition> field) {
        this.field = Long.valueOf(field.getId());
    }
    
    @Override
    public void apply(DatasetAcquisition acquisition) {
        if (getField() == null) throw new IllegalStateException("'Field' attribulte in assignment " + this.getId() + " can not be null");
        LOG.debug("apply assignment : " + this);
        LOG.debug("on acquisition : " + acquisition);
        DatasetAcquisitionMetadataField field = this.getField();
        if (field == null) throw new IllegalStateException("Error in assignment " + this.getId() + " : " + this.getField() 
                + " is not a valid DatasetAcquisitionMetadataField id value");
        try {
            field.update(acquisition, this.getValue());
        } catch (CheckedIllegalClassException e) {
            // do nothing
        }
    }
}

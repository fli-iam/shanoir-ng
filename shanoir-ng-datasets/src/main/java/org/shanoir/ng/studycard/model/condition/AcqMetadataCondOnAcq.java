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

package org.shanoir.ng.studycard.model.condition;

import org.shanoir.ng.datasetacquisition.model.DatasetAcquisition;
import org.shanoir.ng.shared.exception.CheckedIllegalClassException;
import org.shanoir.ng.studycard.model.field.DatasetAcquisitionMetadataField;
import org.shanoir.ng.studycard.model.field.MetadataFieldInterface;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.annotation.JsonTypeName;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;

/**
 * Condition valid for the given DatasetAcquisition if the acquisition metadata fulfill the condition
 */
@Entity
@DiscriminatorValue("AcqMetadataCondOnAcq")
@JsonTypeName("AcqMetadataCondOnAcq")
public class AcqMetadataCondOnAcq extends StudyCardMetadataCondition<DatasetAcquisition> {

    private static final Logger LOG = LoggerFactory.getLogger(AcqMetadataCondOnAcq.class);

    @Override
    public DatasetAcquisitionMetadataField getShanoirField() {
        return DatasetAcquisitionMetadataField.getEnum(shanoirField);
    }

    @Override // Don't know why eclipse can't take DatasetAcquisitionMetadataField as input type
    public void setShanoirField(MetadataFieldInterface<DatasetAcquisition>  field) {
        shanoirField = field.getId();
    }

    /**
     * Check if the condition is fulfilled for the given acquisition
     * @param acquisition the acquisition to check
     * @return true if the condition is fulfilled, false otherwise
     */
    public boolean fulfilled(DatasetAcquisition acquisition) {
        DatasetAcquisitionMetadataField field = this.getShanoirField();
        if (field != null) {
            String valueFromDb;
            try {
                valueFromDb = field.get(acquisition);
            } catch (CheckedIllegalClassException e) {
                valueFromDb = null;
            }
            if (valueFromDb != null) {
                // get all possible values, that can fulfill the condition
                for (String value : this.getValues()) {
                    LOG.info("Condition fulfilled: acquisition metadata field value = " + valueFromDb + ", value =" + value);
                    return true; // as condition values are combined by OR: return if one is true
                }
            }
        }
        return false;
    }

    /**
     * Check if the condition is fulfilled for the given acquisition and append a message if not
     * Used in QualityCardRule to check the condition and set the msg in quality report
     * @param acquisition the acquisition to check
     * @param msg the message to append if the condition is not fulfilled
     * @return true if the condition is fulfilled, false otherwise
     * @throws CheckedIllegalClassException
     */
    public boolean fulfilled(DatasetAcquisition acquisition, StringBuffer report) {
        boolean fulfilled = fulfilled(acquisition);
        if (!fulfilled) {
            try {
                report.append("Condition not fulfilled for acquisition ").append(acquisition.getId()).append(" : ");
                report.append("field ").append(this.getShanoirField().get(acquisition)).append(" value is not in ").append(this.getValues());
            } catch (CheckedIllegalClassException e) {
                report.append("Error occurred while checking condition for acquisition ").append(acquisition.getId());
                LOG.error("Error occurred while checking condition for acquisition {}", acquisition.getId(), e);
            }
        }
        return fulfilled;
    }

}

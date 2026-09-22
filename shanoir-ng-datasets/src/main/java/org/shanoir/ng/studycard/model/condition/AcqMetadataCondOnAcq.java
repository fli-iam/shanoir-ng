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
                boolean matches = field.isNumeric()
                        ? numericalCompare(this.getOperation(), valueFromDb)
                        : textualCompare(this.getOperation(), valueFromDb);
                if (matches) {
                    LOG.info("Condition fulfilled: acquisition metadata field value = " + valueFromDb);
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Check if the condition is fulfilled for the given acquisition and append a message describing
     * the outcome (fulfilled or not) - the caller (QualityCardServiceImpl.conditionsfulfilled()) relies
     * on this message being non-empty in both cases to build the quality card report.
     * @param acquisition the acquisition to check
     * @param report the message to append, describing whether/why the condition was fulfilled
     * @return true if the condition is fulfilled, false otherwise
     * @throws CheckedIllegalClassException
     */
    public boolean fulfilled(DatasetAcquisition acquisition, StringBuffer report) {
        boolean fulfilled = fulfilled(acquisition);
        try {
            String fieldValue = this.getShanoirField().get(acquisition);
            if (fulfilled) {
                report.append("field ").append(this.getShanoirField().name()).append(", the found value ").append(fieldValue)
                        .append(" satisfies operator ").append(this.getOperation())
                        .append(" against the expected value(s) ").append(this.getValues());
            } else {
                report.append("Condition not fulfilled for acquisition id ").append(acquisition.getId()).append(" : ");
                report.append("field ").append(this.getShanoirField().name()).append(", the found value ").append(fieldValue)
                        .append(" does not satisfy operator ").append(this.getOperation())
                        .append(" against the expected value(s) ").append(this.getValues());
            }
        } catch (CheckedIllegalClassException e) {
            report.append("Error occurred while checking condition for acquisition ").append(acquisition.getId());
            LOG.error("Error occurred while checking condition for acquisition {}", acquisition.getId(), e);
        }
        return fulfilled;
    }

}

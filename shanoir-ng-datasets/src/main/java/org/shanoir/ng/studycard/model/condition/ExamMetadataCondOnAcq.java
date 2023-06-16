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

import java.util.List;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

import org.apache.commons.lang3.StringUtils;
import org.shanoir.ng.datasetacquisition.model.DatasetAcquisition;
import org.shanoir.ng.studycard.model.field.DatasetAcquisitionMetadataField;
import org.shanoir.ng.studycard.model.field.MetadataFieldInterface;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.annotation.JsonTypeName;

/**
 * Condition valid for the given DatasetAcquisition if every of it's Datasets metadata fulfill the condition
 */
@Entity
@DiscriminatorValue("ExamMetadataCondOnAcq")
@JsonTypeName("ExamMetadataCondOnAcq")
public class ExamMetadataCondOnAcq extends StudyCardMetadataConditionWithCardinality<DatasetAcquisition>{
	
	private static final Logger LOG = LoggerFactory.getLogger(ExamMetadataCondOnAcq.class);

	@Override
    public DatasetAcquisitionMetadataField getShanoirField() {
        return DatasetAcquisitionMetadataField.getEnum(shanoirField);
    }

    @Override // Don't know why eclipse can't take DatasetAcquisitionMetadataField as input type
    public void setShanoirField(MetadataFieldInterface<DatasetAcquisition>  field) {
        shanoirField = field.getId();
    }
    
    public boolean fulfilled(List<DatasetAcquisition> acquisitions) {
        return fulfilled(acquisitions, null);
    }
	
    public boolean fulfilled(List<DatasetAcquisition> acquisitions, StringBuffer errorMsg) {
        if (acquisitions == null) throw new IllegalArgumentException("datasets can not be null");
        DatasetAcquisitionMetadataField field = this.getShanoirField();
        if (field == null) throw new IllegalArgumentException("field can not be null");
        int nbOk = 0; int total = 0;
        for (DatasetAcquisition acquisition : acquisitions) {
            total++;
            String valueFromDb = field.get(acquisition);
            if (valueFromDb != null) {
                // get all possible values, that can fulfill the condition
                for (String value : this.getValues()) {
                    if (textualCompare(this.getOperation(), valueFromDb, value)) {
                        LOG.info("condition fulfilled: ds.name=" + valueFromDb + ", value=" + value);
                        nbOk++;
                        break;
                    } 
                }
            }                
        }
        boolean complies = cardinalityComplies(nbOk, total);
        if (!complies) {
            if (getCardinality() == -1) {
                errorMsg.append("condition [" + toString() + "] failed because only " + nbOk + " out of all (" + total + ") acquisitions complied");
            } else if (getCardinality() == 0) {
                errorMsg.append("condition [" + toString() + "] failed because " + nbOk + " acquisitions complied where 0 was required");
            } else {
                errorMsg.append("condition [" + toString() + "] failed because only " + nbOk + " out of " + total + " acquisitions complied");
            }
        } else {
            errorMsg.append("condition [" + toString() + "] succeed");
        }
        return complies;
    } 

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        if (getCardinality() == -1) {
            sb.append("all of the ");
        } else if (getCardinality() == 0) {
            sb.append("none of the ");
        } else {
            sb.append("at least ")
                .append(getCardinality())
                .append(" of the ");
        }
        sb.append("DatasetAcquisition metadata field '").append(getShanoirField().name())
            .append("' ").append(getOperation().name())
            .append(" ")
            .append(StringUtils.join(getValues(), " or "));        
        return sb.toString();
    }

}

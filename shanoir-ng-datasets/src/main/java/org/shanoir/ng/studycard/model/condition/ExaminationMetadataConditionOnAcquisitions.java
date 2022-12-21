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
import java.util.stream.Collectors;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

import org.shanoir.ng.dataset.model.Dataset;
import org.shanoir.ng.datasetacquisition.model.DatasetAcquisition;
import org.shanoir.ng.studycard.model.field.DatasetAcquisitionMetadataField;
import org.shanoir.ng.studycard.model.field.DatasetMetadataField;
import org.shanoir.ng.studycard.model.field.MetadataFieldInterface;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Condition valid for the given DatasetAcquisition if every of it's Datasets metadata fulfill the condition
 */
@Entity
@DiscriminatorValue("ExaminationMetadataConditionOnAcquisitions")
public class ExaminationMetadataConditionOnAcquisitions extends StudyCardMetadataConditionWithCardinality<DatasetAcquisition>{
	
	private static final Logger LOG = LoggerFactory.getLogger(ExaminationMetadataConditionOnAcquisitions.class);

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
	
    public boolean fulfilled(List<DatasetAcquisition> acquisitions, String errorMsg) {
        if (acquisitions == null) throw new IllegalArgumentException("datasets can not be null");
        DatasetAcquisitionMetadataField field = this.getShanoirField();
        if (field == null) throw new IllegalArgumentException("field can not be null");
        errorMsg = null;
        int nbOk = 0; int total = 0;
        for (DatasetAcquisition acquisition : acquisitions) {
            total++;
            String valueFromDb = field.get(acquisition);
            if (valueFromDb != null) {
                // get all possible values, that can fulfill the condition
                for (StudyCardConditionValue value : this.getValues()) {
                    if (value.getValue() == null) throw new IllegalArgumentException("A condition value cannot be null.");
                    LOG.info("operation: " + this.getOperation().name()
                            + ", valueFromDb: " + valueFromDb + ", valueFromSC: " + value.getValue());
                    if (textualCompare(this.getOperation(), valueFromDb, value.getValue())) {
                        LOG.info("condition fulfilled: ds.name=" + valueFromDb + ", value=" + value.getValue());
                        nbOk++;
                        break;
                    } 
                }
            }                
        }
        boolean complies = cardinalityComplies(nbOk, total);
        if (!complies) errorMsg = getErrorMsg(nbOk, total);
        return complies;
    } 
    
    private String getErrorMsg(int nbOk, int total) {
        String values = getValues().stream().map(v -> v.getValue()).collect(Collectors.joining(","));
        String msg = "Error with condition: " + getShanoirField() + ", " + getOperation().name() + ", with values: " + values
                + ". " + nbOk + "/" + total + " acquisitions complied when " + getCardinality() + " were needed.";
        return msg;
    }

}

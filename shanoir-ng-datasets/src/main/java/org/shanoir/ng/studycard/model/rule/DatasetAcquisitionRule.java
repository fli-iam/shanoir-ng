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

package org.shanoir.ng.studycard.model.rule;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

import org.dcm4che3.data.Attributes;
import org.shanoir.ng.dataset.model.Dataset;
import org.shanoir.ng.datasetacquisition.model.DatasetAcquisition;
import org.shanoir.ng.studycard.model.assignment.DatasetAcquisitionAssignment;
import org.shanoir.ng.studycard.model.assignment.DatasetAssignment;
import org.shanoir.ng.studycard.model.assignment.StudyCardAssignment;
import org.shanoir.ng.studycard.model.condition.AcquisitionMetadataConditionOnAcquisition;
import org.shanoir.ng.studycard.model.condition.AcquisitionMetadataConditionOnDatasets;
import org.shanoir.ng.studycard.model.condition.StudyCardCondition;
import org.shanoir.ng.studycard.model.condition.StudyCardDICOMCondition;

import com.fasterxml.jackson.annotation.JsonTypeName;

/**
 * A rule that applies to a {@link DatasetAcquisition}
 */
@Entity
@DiscriminatorValue("DatasetAcquisition")
@JsonTypeName("DatasetAcquisition")
public class DatasetAcquisitionRule extends StudyCardRule<DatasetAcquisition> {

    @Override
    public void apply(DatasetAcquisition acquisition, Attributes dicomAttributes) {
        if (this.getConditions() == null || this.getConditions().isEmpty() || conditionsfulfilled(dicomAttributes, acquisition)) {
            if (this.getAssignments() != null) applyAssignments(acquisition);
        }
    }
   
    private boolean conditionsfulfilled(Attributes dicomAttributes, DatasetAcquisition acquisition) {
        boolean fulfilled = true;
        for (StudyCardCondition condition : getConditions()) {
            if (condition instanceof StudyCardDICOMCondition) {
                fulfilled &= ((StudyCardDICOMCondition) condition).fulfilled(dicomAttributes);
            } else if (condition instanceof AcquisitionMetadataConditionOnAcquisition) {
                fulfilled &= ((AcquisitionMetadataConditionOnAcquisition) condition).fulfilled(acquisition);
            } else if (condition instanceof AcquisitionMetadataConditionOnDatasets) {
                fulfilled &= ((AcquisitionMetadataConditionOnDatasets) condition).fulfilled(acquisition.getDatasets());
            } else {
                throw new IllegalStateException("There might be an unimplemented condition type here. Condition class : " + condition.getClass());
            }
        }
        return fulfilled;
    }
   
    private void applyAssignments(DatasetAcquisition acquisition) {
        for (StudyCardAssignment<?> assignment : getAssignments()) {
            if (assignment instanceof DatasetAssignment) {
                for (Dataset dataset : acquisition.getDatasets())
                ((DatasetAssignment)assignment).apply(dataset);               
            } else if (assignment instanceof DatasetAcquisitionAssignment) {
                ((DatasetAcquisitionAssignment)assignment).apply(acquisition);               
            } else throw new IllegalArgumentException("Unimplemented assignment type");
        }
     }
}

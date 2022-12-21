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
import org.shanoir.ng.studycard.model.assignment.StudyCardAssignment;
import org.shanoir.ng.studycard.model.condition.DatasetMetadataConditionOnDataset;
import org.shanoir.ng.studycard.model.condition.StudyCardCondition;
import org.shanoir.ng.studycard.model.condition.StudyCardDICOMCondition;

@Entity
@DiscriminatorValue("Dataset")
public class DatasetRule extends StudyCardRule<Dataset> {

    @Override
    public void apply(Dataset dataset, Attributes dicomAttributes) {
        if (this.getConditions() == null || this.getConditions().isEmpty() || conditionsfulfilled(dicomAttributes, dataset)) {
            if (this.getAssignments() != null) applyAssignments(dataset);
        }
    }
   
    private boolean conditionsfulfilled(Attributes dicomAttributes, Dataset dataset) {
        boolean fulfilled = true;
        for (StudyCardCondition condition : getConditions()) {
            if (condition instanceof StudyCardDICOMCondition) {
                fulfilled &= ((StudyCardDICOMCondition) condition).fulfilled(dicomAttributes);
            } else if (condition instanceof DatasetMetadataConditionOnDataset) {
                fulfilled &= ((DatasetMetadataConditionOnDataset) condition).fulfilled(dataset);
            } else {
                throw new IllegalStateException("There might be an unimplemented condition type here. Condition class : " + condition.getClass());
            }
        }
       return fulfilled;
    }
   
    private void applyAssignments(Dataset dataset) {
       for (StudyCardAssignment<Dataset> assignment : getAssignments()) {
           assignment.apply(dataset);
       }
    }
}

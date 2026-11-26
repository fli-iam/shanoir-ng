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

import org.shanoir.ng.dataset.model.Dataset;
import org.shanoir.ng.datasetacquisition.model.DatasetAcquisition;
import org.shanoir.ng.download.AcquisitionAttributes;
import org.shanoir.ng.studycard.model.assignment.DatasetAcquisitionAssignment;
import org.shanoir.ng.studycard.model.assignment.DatasetAssignment;
import org.shanoir.ng.studycard.model.assignment.StudyCardAssignment;
import org.shanoir.ng.studycard.model.condition.AcqDICOMConditionOnDatasets;
import org.shanoir.ng.studycard.model.condition.AcqMetadataCondOnAcq;
import org.shanoir.ng.studycard.model.condition.AcqMetadataCondOnDatasets;
import org.shanoir.ng.studycard.model.condition.CardCondition;

import com.fasterxml.jackson.annotation.JsonTypeName;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;

/**
 * A rule that applies to a {@link DatasetAcquisition}
 */
@Entity
@DiscriminatorValue("DatasetAcquisition")
@JsonTypeName("DatasetAcquisition")
public class DatasetAcquisitionRule extends StudyCardRule<DatasetAcquisition> {

    public void apply(DatasetAcquisition acquisition, AcquisitionAttributes<?> dicomAttributes) {
        if (this.getConditions() == null || this.getConditions().isEmpty() || conditionsfulfilled(dicomAttributes, acquisition)) {
            if (this.getAssignments() != null) applyAssignments(acquisition);
        }
    }

    private boolean conditionsfulfilled(AcquisitionAttributes<?> dicomAttributes, DatasetAcquisition acquisition) {
        boolean fulfilled = true;
        for (CardCondition condition : getConditions()) {
            if (condition instanceof AcqDICOMConditionOnDatasets datasets1) {
                fulfilled &= datasets1.fulfilled(dicomAttributes);
            } else if (condition instanceof AcqMetadataCondOnAcq acq) {
                fulfilled &= acq.fulfilled(acquisition);
            } else if (condition instanceof AcqMetadataCondOnDatasets datasets) {
                // report is set to null here because study cards are not used to generate reports
                fulfilled &= datasets.fulfilled(acquisition.getDatasets(), null);
            } else {
                throw new IllegalStateException("There might be an unimplemented condition type here. Condition class : " + condition.getClass());
            }
        }
        return fulfilled;
    }

    private void applyAssignments(DatasetAcquisition acquisition) {
        for (StudyCardAssignment<?> assignment : getAssignments()) {
            if (assignment instanceof DatasetAssignment datasetAssignment) {
                for (Dataset dataset : acquisition.getDatasets()) {
                    datasetAssignment.apply(dataset);
                }
            } else if (assignment instanceof DatasetAcquisitionAssignment acquisitionAssignment) {
                acquisitionAssignment.apply(acquisition);
            } else throw new IllegalArgumentException("Unimplemented assignment type");
        }
    }
}

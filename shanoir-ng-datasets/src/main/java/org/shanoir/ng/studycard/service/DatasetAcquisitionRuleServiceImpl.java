package org.shanoir.ng.studycard.service;

import org.shanoir.ng.dataset.model.Dataset;
import org.shanoir.ng.datasetacquisition.model.DatasetAcquisition;
import org.shanoir.ng.download.AcquisitionAttributes;
import org.shanoir.ng.studycard.model.assignment.DatasetAcquisitionAssignment;
import org.shanoir.ng.studycard.model.assignment.DatasetAssignment;
import org.shanoir.ng.studycard.model.assignment.StudyCardAssignment;
import org.shanoir.ng.studycard.model.condition.AcqMetadataCondOnAcq;
import org.shanoir.ng.studycard.model.condition.AcqMetadataCondOnDatasets;
import org.shanoir.ng.studycard.model.condition.StudyCardCondition;
import org.shanoir.ng.studycard.model.condition.StudyCardDICOMConditionOnDatasets;
import org.shanoir.ng.studycard.model.rule.DatasetAcquisitionRule;
import org.springframework.stereotype.Service;

@Service
public class DatasetAcquisitionRuleServiceImpl implements DatasetAcquisitionRuleService {

    public void apply(DatasetAcquisitionRule dar, DatasetAcquisition acquisition, AcquisitionAttributes<?> dicomAttributes) {
        if (dar.getConditions() == null || dar.getConditions().isEmpty() || conditionsfulfilled(dar, dicomAttributes, acquisition)) {
            if (dar.getAssignments() != null) applyAssignments(dar, acquisition);
        }
    }

    private boolean conditionsfulfilled(DatasetAcquisitionRule dar, AcquisitionAttributes<?> dicomAttributes, DatasetAcquisition acquisition) {
        boolean fulfilled = true;
        for (StudyCardCondition condition : dar.getConditions()) {
            if (condition instanceof StudyCardDICOMConditionOnDatasets) {
                fulfilled &= ((StudyCardDICOMConditionOnDatasets) condition).fulfilled(dicomAttributes);
            } else if (condition instanceof AcqMetadataCondOnAcq) {
                fulfilled &= ((AcqMetadataCondOnAcq) condition).fulfilled(acquisition);
            } else if (condition instanceof AcqMetadataCondOnDatasets) {
                fulfilled &= ((AcqMetadataCondOnDatasets) condition).fulfilled(acquisition.getDatasets());
            } else {
                throw new IllegalStateException("There might be an unimplemented condition type here. Condition class : " + condition.getClass());
            }
        }
        return fulfilled;
    }

    private void applyAssignments(DatasetAcquisitionRule dar, DatasetAcquisition acquisition) {
        for (StudyCardAssignment<?> assignment : dar.getAssignments()) {
            if (assignment instanceof DatasetAssignment) {
                for (Dataset dataset : acquisition.getDatasets()) {
                    ((DatasetAssignment) assignment).apply(dataset);
                }
            } else if (assignment instanceof DatasetAcquisitionAssignment) {
                ((DatasetAcquisitionAssignment) assignment).apply(acquisition);
            } else throw new IllegalArgumentException("Unimplemented assignment type");
        }
    }
}

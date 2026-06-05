package org.shanoir.ng.studycard.service;

import org.shanoir.ng.datasetacquisition.model.DatasetAcquisition;
import org.shanoir.ng.download.AcquisitionAttributes;
import org.shanoir.ng.studycard.model.rule.DatasetAcquisitionRule;

public interface DatasetAcquisitionRuleService {

    void apply(DatasetAcquisitionRule dar, DatasetAcquisition acquisition, AcquisitionAttributes<?> dicomAttributes);
}

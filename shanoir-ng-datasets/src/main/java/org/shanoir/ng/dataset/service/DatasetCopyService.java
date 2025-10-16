package org.shanoir.ng.dataset.service;

import org.shanoir.ng.dataset.model.Dataset;
import org.shanoir.ng.datasetacquisition.model.DatasetAcquisition;
import org.shanoir.ng.examination.model.Examination;
import org.springframework.security.access.prepost.PreAuthorize;

import java.util.Map;

public interface DatasetCopyService {
    @PreAuthorize("hasRole('ADMIN') or (hasRole('EXPERT') and @datasetSecurityService.hasRightOnDataset(#dataset.getId(), 'CAN_ADMINISTRATE'))")
    Object[] moveDataset(Dataset ds, Long studyId, Map<Long, Examination> examMap, Map<Long, DatasetAcquisition> acqMap, Long userId) throws Exception;
}

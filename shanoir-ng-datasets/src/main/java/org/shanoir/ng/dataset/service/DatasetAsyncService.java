package org.shanoir.ng.dataset.service;

import org.shanoir.ng.dataset.model.Dataset;
import org.shanoir.ng.shared.exception.ShanoirException;
import org.springframework.scheduling.annotation.Async;
import org.springframework.security.access.prepost.PreAuthorize;

public interface DatasetAsyncService {

    @PreAuthorize("hasRole('ADMIN') or (hasRole('EXPERT') and @datasetSecurityService.hasRightOnDataset(#dataset.getId(), 'CAN_ADMINISTRATE'))")
    @Async
    void deleteDatasetFromDiskAndPacs(Dataset dataset) throws ShanoirException;

}

package org.shanoir.ng.dataset.service;

import org.shanoir.ng.dataset.model.Dataset;
import org.springframework.security.access.prepost.PostAuthorize;
import org.springframework.security.access.prepost.PreAuthorize;

import java.util.List;

public interface ProcessedDatasetService {

    /**
     * Delete all processed datasets linked to the given dataset processing id
     *
     * @return datasets
     */
    @PreAuthorize("hasAnyRole('ADMIN', 'EXPERT', 'USER')")
    @PostAuthorize("hasRole('ADMIN') or @datasetSecurityService.filterDatasetList(returnObject, 'CAN_SEE_ALL')")
    List<Dataset> deleteByProcessingId(Long id);
}

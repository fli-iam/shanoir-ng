package org.shanoir.ng.dataset.service;

import org.shanoir.ng.dataset.model.Dataset;
import org.shanoir.ng.datasetfile.DatasetFile;
import org.shanoir.ng.shared.exception.ShanoirException;
import org.springframework.security.access.prepost.PreAuthorize;

import java.util.List;

public interface DatasetAsyncService {

    @PreAuthorize("hasRole('ADMIN') or (hasRole('EXPERT') and @datasetSecurityService.hasRightOnDataset(#datasetId, 'CAN_ADMINISTRATE'))")
    void deleteDatasetFilesFromDiskAndPacs(List<DatasetFile> datasetFiles, boolean isDicom, Long datasetId) throws ShanoirException;

    @PreAuthorize("hasRole('ADMIN') or (hasRole('EXPERT') and @datasetSecurityService.hasRightOnDataset(#datasetId, 'CAN_ADMINISTRATE'))")
    void deleteDatasetFilesFromDiskAndPacsAsync(List<DatasetFile> datasetFiles, boolean isDicom, Long datasetId) throws ShanoirException;
}

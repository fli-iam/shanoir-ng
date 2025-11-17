package org.shanoir.ng.dataset.service;

import org.shanoir.ng.datasetfile.DatasetFile;
import org.shanoir.ng.shared.exception.ShanoirException;

import java.util.List;

public interface DatasetAsyncService {


    // No PreAuthorize here since it's always called after a security check
    void deleteDatasetFilesFromDiskAndPacsAsync(List<DatasetFile> datasetFiles, boolean isDicom, Long datasetId) throws ShanoirException;
}

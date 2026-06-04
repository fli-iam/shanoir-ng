package org.shanoir.ng.dataset.service;

import org.shanoir.ng.dataset.model.DatasetExpression;
import org.shanoir.ng.datasetfile.DatasetFile;

import java.util.List;

public interface DatasetExpressionService {

    /**
     * @param datasetExpression the involved datasetExpression
     *
     * @return the DICOM files related to that datasetExpression
     */
    List<DatasetFile> getDatasetFiles(DatasetExpression datasetExpression);
}

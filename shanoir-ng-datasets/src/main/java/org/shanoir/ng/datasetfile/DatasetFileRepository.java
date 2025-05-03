package org.shanoir.ng.datasetfile;

import org.springframework.data.repository.CrudRepository;

public interface DatasetFileRepository extends CrudRepository<DatasetFile, Long> {

    DatasetFile findByDatasetExpressionId(Long datasetExpressionId);

}

package org.shanoir.ng.dataset.repository;

import org.shanoir.ng.dataset.model.DatasetExpression;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface DatasetExpressionRepository  extends CrudRepository<DatasetExpression, Long> {

    List<DatasetExpression> findAllByDatasetId(Long datasetId);
}

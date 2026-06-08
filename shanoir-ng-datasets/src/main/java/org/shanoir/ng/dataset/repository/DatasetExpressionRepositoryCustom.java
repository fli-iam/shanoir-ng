package org.shanoir.ng.dataset.repository;

import org.shanoir.ng.dataset.model.DatasetExpression;

import java.util.List;

public interface DatasetExpressionRepositoryCustom {

    List<String> ALL_RELATION_NAMES = List.of("comingFromDatasetExpressions", "dataset", "datasetFiles", "originalDatasetExpression");

    DatasetExpression findWithSpecificRelations(Long id, List<String> relationNames);

    DatasetExpression findWithSpecificSubRelations(Long id, List<String> relationNames);

    List<DatasetExpression> findListWithSpecificRelations(List<Long> ids, List<String> relationNames);

    List<DatasetExpression> findListWithSpecificSubRelations(List<Long> ids, List<String> relationNames);
}

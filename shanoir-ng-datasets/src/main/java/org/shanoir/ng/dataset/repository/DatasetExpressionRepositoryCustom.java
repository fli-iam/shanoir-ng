package org.shanoir.ng.dataset.repository;

import org.shanoir.ng.dataset.model.DatasetExpression;

import java.util.List;

public interface DatasetExpressionRepositoryCustom {

    List<String> allRelationNames = List.of("comingFromDatasetExpressions", "dataset", "datasetFiles", "originalDatasetExpression");

    DatasetExpression findWithSpecificRelations(Long id, List<String> relationNames);

    DatasetExpression findWithSpecificRelationIds(Long id, List<String> relationNames);

    List<DatasetExpression> findListWithSpecificRelations(List<Long> ids, List<String> relationNames);

    List<DatasetExpression> findListWithSpecificRelationIds(List<Long> ids, List<String> relationNames);
}

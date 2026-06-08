package org.shanoir.ng.dataset.repository;

import org.shanoir.ng.dataset.model.DatasetExpression;
import org.shanoir.ng.shared.repository.ShanoirRepositoryImpl;

import java.util.List;


public class DatasetExpressionRepositoryImpl extends ShanoirRepositoryImpl<DatasetExpression> implements DatasetExpressionRepositoryCustom {

    public DatasetExpression findWithSpecificRelations(Long id, List<String> relationNames) {
        return super.findWithSpecificRelations(id, relationNames);
    }

    public DatasetExpression findWithSpecificRelationIds(Long id, List<String> relationNames) {
        return super.findWithSpecificRelationIds(id, relationNames);
    }

    public List<DatasetExpression> findListWithSpecificRelations(List<Long> ids, List<String> relationNames) {
        return super.findListWithSpecificRelations(ids, relationNames);
    }

    public List<DatasetExpression> findListWithSpecificRelationIds(List<Long> ids, List<String> relationNames) {
        return super.findListWithSpecificRelationIds(ids, relationNames);
    }

}

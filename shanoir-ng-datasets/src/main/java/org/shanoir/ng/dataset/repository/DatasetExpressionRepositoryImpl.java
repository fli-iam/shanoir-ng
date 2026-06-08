package org.shanoir.ng.dataset.repository;

import org.shanoir.ng.dataset.model.DatasetExpression;
import org.shanoir.ng.shared.repository.ShanoirRepositoryImpl;

import java.util.List;


public class DatasetExpressionRepositoryImpl extends ShanoirRepositoryImpl<DatasetExpression> implements DatasetExpressionRepositoryCustom {

    public DatasetExpression findWithSpecificRelations(Long id, List<String> relationNames) {
        return super.findWithSpecificRelations(id, relationNames);
    }

    public DatasetExpression findWithSpecificSubRelations(Long id, List<String> relationNames) {
        return super.findWithSpecificSubRelations(id, relationNames);
    }

    public List<DatasetExpression> findListWithSpecificRelations(List<Long> ids, List<String> relationNames) {
        return super.findListWithSpecificRelations(ids, relationNames);
    }

    public List<DatasetExpression> findListWithSpecificSubRelations(List<Long> ids, List<String> relationNames) {
        return super.findListWithSpecificSubRelation(ids, relationNames);
    }

}

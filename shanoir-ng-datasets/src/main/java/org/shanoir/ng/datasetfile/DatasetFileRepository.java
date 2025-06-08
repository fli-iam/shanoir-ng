package org.shanoir.ng.datasetfile;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface DatasetFileRepository extends CrudRepository<DatasetFile, Long> {

    @Modifying
    @Query("DELETE FROM DatasetFile df WHERE df.datasetExpression.id IN :expressionIds")
    void deleteByDatasetExpressionIds(@Param("expressionIds") List<Long> expressionIds);

}

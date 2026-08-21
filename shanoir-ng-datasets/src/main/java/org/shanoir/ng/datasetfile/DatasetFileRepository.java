/**
 * Shanoir NG - Import, manage and share neuroimaging data
 * Copyright (C) 2009-2019 Inria - https://www.inria.fr/
 * Contact us on https://project.inria.fr/shanoir/
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see https://www.gnu.org/licenses/gpl-3.0.html
 */

package org.shanoir.ng.datasetfile;

import java.util.List;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

public interface DatasetFileRepository extends CrudRepository<DatasetFile, Long> {

    @Modifying
    @Query("DELETE FROM DatasetFile df WHERE df.datasetExpression.id IN :expressionIds")
    void deleteByDatasetExpressionIds(@Param("expressionIds") List<Long> expressionIds);

    @Modifying
    @Query(value = """
            INSERT INTO dataset_file (dataset_expression_id, pacs, path)
            SELECT :copiedDatasetExpressionId, pacs, path
            FROM dataset_file
            WHERE dataset_expression_id = :originalDatasetExpressionId
            """, nativeQuery = true)
    void copyDatasetFiles(
            @Param("originalDatasetExpressionId") Long originalDatasetExpressionId,
            @Param("copiedDatasetExpressionId") Long copiedDatasetExpressionId);

}

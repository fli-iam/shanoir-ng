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

package org.shanoir.ng.processing.repository;

import org.shanoir.ng.processing.model.DatasetProcessing;
import org.shanoir.ng.shared.repository.ShanoirRepositoryImpl;

import java.util.List;

public class DatasetProcessingRepositoryImpl extends ShanoirRepositoryImpl<DatasetProcessing> implements DatasetProcessingRepositoryCustom {


    public DatasetProcessing findWithSpecificRelations(Long id, List<String> relationNames) {
        return super.findWithSpecificRelations(id, relationNames);
    }

    public DatasetProcessing findWithSpecificSubRelations(Long id, List<String> relationNames) {
        return super.findWithSpecificSubRelations(id, relationNames);
    }

    public List<DatasetProcessing> findListWithSpecificRelations(List<Long> ids, List<String> relationNames) {
        return findListWithSpecificRelations(ids, relationNames);
    }

    public List<DatasetProcessing> findListWithSpecificSubRelations(List<Long> ids, List<String> relationNames) {
        return findListWithSpecificSubRelation(ids, relationNames);
    }
}

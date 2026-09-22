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

package org.shanoir.ng.datasetacquisition.dto.mapper;

import org.shanoir.ng.datasetacquisition.dto.DatasetAcquisitionWithDatasetsDTO;
import org.shanoir.ng.datasetacquisition.model.DatasetAcquisition;

import org.springframework.beans.factory.annotation.Autowired;

/**
 * Decorator for dataset acquisitions mapper.
 *
 * @author msimon
 *
 */
public abstract class DatasetAcquisitionDatasetsDecorator implements DatasetAcquisitionWithDatasetsMapper {

    @Autowired
    private DatasetAcquisitionWithDatasetsMapper delegate;

    @Override
    public DatasetAcquisitionWithDatasetsDTO datasetAcquisitionToDatasetAcquisitionDTOWithDatasetIds(
            final DatasetAcquisition datasetAcquisition) {
        if (datasetAcquisition == null) {
            return null;
        }
        final DatasetAcquisitionWithDatasetsDTO datasetAcquisitionDTO = delegate
                .datasetAcquisitionToDatasetAcquisitionDTOWithDatasetIds(datasetAcquisition);
        return datasetAcquisitionDTO;
    }
}

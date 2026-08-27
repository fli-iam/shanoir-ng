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

import java.util.List;

import org.mapstruct.*;
import org.shanoir.ng.dataset.dto.mapper.DatasetMapper;
import org.shanoir.ng.datasetacquisition.dto.DatasetAcquisitionWithDatasetsDTO;
import org.shanoir.ng.datasetacquisition.model.DatasetAcquisition;

@Mapper(componentModel = "spring", config = DatasetAcquisitionMappingConfig.class, uses = DatasetMapper.class)
@DecoratedWith(DatasetAcquisitionDatasetsDecorator.class)
public interface DatasetAcquisitionWithDatasetsMapper {

    ////// Entity to DTO

    // Single entity

    @Named("idRelations")
    @InheritConfiguration(name = "acquisitionToAcquisitionIdRelationsDTOPrototype")
    @Mapping(target = "datasets", source = "datasets", qualifiedByName = "idOnly")
    DatasetAcquisitionWithDatasetsDTO datasetAcquisitionToDatasetAcquisitionDTOWithDatasetIds(DatasetAcquisition datasetAcquisition);

    // Entity list

    @IterableMapping(qualifiedByName = "idRelations")
    List<DatasetAcquisitionWithDatasetsDTO> datasetAcquisitionsToDatasetAcquisitionsDTOWithDatasetIds(List<DatasetAcquisition> datasetAcquisitions);
}

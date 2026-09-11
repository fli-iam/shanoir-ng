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

import org.mapstruct.Mapping;
import org.mapstruct.MapperConfig;
import org.shanoir.ng.datasetacquisition.dto.DatasetAcquisitionDTO;
import org.shanoir.ng.datasetacquisition.model.DatasetAcquisition;
import org.shanoir.ng.examination.dto.mapper.ExaminationMapper;

/**
 * Holds the DatasetAcquisition -> DatasetAcquisitionDTO field mappings shared with
 * DatasetAcquisitionWithDatasetsMapper, so it can pull them in via @InheritConfiguration
 * instead of duplicating the @Mapping list. This method is a prototype only: it is never
 * implemented, DatasetAcquisitionMapper keeps its own standalone version.
 */
@MapperConfig(uses = { ExaminationMapper.class, DatasetAcquisitionMapper.class })
public interface DatasetAcquisitionMappingConfig {

    @Mapping(target = "examination", source = "examination", qualifiedByName = "idOnly")
    @Mapping(target = "copies", source = "copies", qualifiedByName = "id")
    @Mapping(target = "source", source = "source", qualifiedByName = "id")
    DatasetAcquisitionDTO acquisitionToAcquisitionIdRelationsDTOPrototype(DatasetAcquisition datasetAcquisition);
}

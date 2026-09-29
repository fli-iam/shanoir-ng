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

package org.shanoir.ng.processing.dto.mapper;

import org.mapstruct.Mapping;
import org.mapstruct.MapperConfig;
import org.shanoir.ng.dataset.dto.mapper.DatasetMapper;
import org.shanoir.ng.processing.dto.DatasetProcessingDTO;
import org.shanoir.ng.processing.model.DatasetProcessing;

/**
 * Holds the DatasetProcessing -> DatasetProcessingDTO field mappings shared with
 * ExecutionMonitoringMapper, so it can pull them in via @InheritConfiguration instead
 * of duplicating the @Mapping list. These methods are prototypes only: they are never
 * implemented, DatasetProcessingMapper keeps its own standalone versions.
 */
@MapperConfig(uses = { DatasetMapper.class })
public interface DatasetProcessingMappingConfig {

    @Mapping(target = "inputDatasets", expression = "java(null)")
    @Mapping(target = "outputDatasets", expression = "java(null)")
    DatasetProcessingDTO processingToProcessingDTOWithNullRelationsPrototype(DatasetProcessing processing);

    @Mapping(target = "inputDatasets", source = "inputDatasets", qualifiedByName = "id")
    @Mapping(target = "outputDatasets", source = "outputDatasets", qualifiedByName = "id")
    DatasetProcessingDTO processingToProcessingDTOWithIdRelationsPrototype(DatasetProcessing processing);
}

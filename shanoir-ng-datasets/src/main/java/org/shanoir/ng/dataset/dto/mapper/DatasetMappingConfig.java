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

package org.shanoir.ng.dataset.dto.mapper;

import org.mapstruct.Mapping;
import org.mapstruct.MapperConfig;
import org.shanoir.ng.dataset.dto.DatasetDTO;
import org.shanoir.ng.dataset.model.Dataset;
import org.shanoir.ng.datasetacquisition.dto.mapper.DatasetAcquisitionMapper;
import org.shanoir.ng.processing.dto.mapper.DatasetProcessingMapper;

/**
 * Holds the Dataset -> DatasetDTO field mappings shared with the modality subtype mappers
 * (MrDatasetMapper, EegDatasetMapper, ...), so they can pull them in via @InheritConfiguration
 * instead of duplicating the @Mapping list. These methods are prototypes only: they are never
 * implemented, DatasetMapper keeps its own standalone versions.
 */
@MapperConfig(uses = { DatasetMetadataMapper.class, DatasetAcquisitionMapper.class, DatasetProcessingMapper.class })
public interface DatasetMappingConfig {

    @Mapping(target = "datasetProcessing", expression = "java(null)")
    @Mapping(target = "datasetAcquisition", expression = "java(null)")
    @Mapping(target = "tags", expression = "java(null)")
    @Mapping(target = "source", expression = "java(null)")
    @Mapping(target = "copies", expression = "java(null)")
    @Mapping(target = "originMetadata", source = "originMetadata", qualifiedByName = "standard")
    @Mapping(target = "updatedMetadata", source = "updatedMetadata", qualifiedByName = "standard")
    DatasetDTO datasetToDatasetDTOWithMetadataPrototype(Dataset dataset);

    @Mapping(target = "datasetProcessing", source = "datasetProcessing", qualifiedByName = "withInputIds")
    @Mapping(target = "datasetAcquisition", source = "datasetAcquisition", qualifiedByName = "withExamination")
    @Mapping(target = "tags", expression = "java(null)")
    @Mapping(target = "source", expression = "java(null)")
    @Mapping(target = "copies", expression = "java(null)")
    @Mapping(target = "originMetadata", source = "originMetadata", qualifiedByName = "standard")
    @Mapping(target = "updatedMetadata", source = "updatedMetadata", qualifiedByName = "standard")
    DatasetDTO datasetToDatasetDTOWithProcessingAncestorsAndExaminationPrototype(Dataset dataset);

    @Mapping(target = "datasetProcessing", source = "datasetProcessing", qualifiedByName = "idOnly")
    @Mapping(target = "datasetAcquisition", expression = "java(null)")
    @Mapping(target = "tags", expression = "java(null)")
    @Mapping(target = "source", expression = "java(null)")
    @Mapping(target = "copies", expression = "java(null)")
    @Mapping(target = "originMetadata", source = "originMetadata", qualifiedByName = "standard")
    @Mapping(target = "updatedMetadata", source = "updatedMetadata", qualifiedByName = "standard")
    @Mapping(target = "inPacs", expression = "java(null)")
    @Mapping(target = "centerId", expression = "java(null)")
    DatasetDTO datasetToDatasetDTOWithProcessingPrototype(Dataset dataset);

    @Mapping(target = "datasetProcessing", source = "datasetProcessing", qualifiedByName = "idOnly")
    @Mapping(target = "datasetAcquisition", source = "datasetAcquisition", qualifiedByName = "idOnly")
    @Mapping(target = "tags", expression = "java(null)")
    @Mapping(target = "source", expression = "java(null)")
    @Mapping(target = "copies", expression = "java(null)")
    @Mapping(target = "originMetadata", source = "originMetadata", qualifiedByName = "standard")
    @Mapping(target = "updatedMetadata", source = "updatedMetadata", qualifiedByName = "standard")
    DatasetDTO datasetToDatasetDTOWithIdRelationsPrototype(Dataset dataset);
}

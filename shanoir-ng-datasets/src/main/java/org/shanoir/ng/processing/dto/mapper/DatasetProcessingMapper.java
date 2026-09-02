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

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import org.mapstruct.*;
import org.shanoir.ng.processing.model.DatasetProcessing;
import org.shanoir.ng.processing.dto.DatasetProcessingDTO;

@Mapper(componentModel = "spring", config = DatasetProcessingMappingConfig.class)
public interface DatasetProcessingMapper {

    ////// Entity to DTO

    @Named("id")
    default Long processingToLongId(DatasetProcessing processing) {
        if (processing == null) {
            return null;
        }
        return processing.getId();
    }

    @Named("id")
    default List<Long> processingListToLongIds(List<DatasetProcessing> processings) {
        if (processings == null) {
            return null;
        }
        return processings.stream().filter(Objects::nonNull).map(DatasetProcessing::getId).collect(Collectors.toList());
    }

    @Named("idOnly")
    default DatasetProcessingDTO processingToId(DatasetProcessing processing) {
        if (processing == null) {
            return null;
        }
        DatasetProcessingDTO dto = new DatasetProcessingDTO();
        dto.setId(processing.getId());
        return dto;
    }

    @Named("idOnly")
    default List<DatasetProcessingDTO> processingListToIds(List<DatasetProcessing> processings) {
        if (processings == null) {
            return null;
        }
        return processings.stream().filter(Objects::nonNull).map(proc -> {
            DatasetProcessingDTO dto = new DatasetProcessingDTO();
            dto.setId(proc.getId());
            return dto;
        }).collect(Collectors.toList());
    }

    //Single entity

    /**
     * Some context of usage :
     */
    @Named("nullRelations")
    @InheritConfiguration(name = "processingToProcessingDTOWithNullRelationsPrototype")
    DatasetProcessingDTO processingToProcessingDTOWithNullRelations(DatasetProcessing processing);

    /**
     * Some context of usage :
     */
    @Named("withInputIds")
    @Mapping(target = "inputDatasets", source = "inputDatasets", qualifiedByName = "id")
    @Mapping(target = "outputDatasets", expression = "java(null)")
    DatasetProcessingDTO processingToProcessingDTOWithInputIds(DatasetProcessing processing);

    /**
     * Some context of usage :
     */
    @Named("withOutputIds")
    @Mapping(target = "inputDatasets", expression = "java(null)")
    @Mapping(target = "outputDatasets", source = "outputDatasets", qualifiedByName = "id")
    DatasetProcessingDTO processingToProcessingWithOutputIdsDTO(DatasetProcessing processing);

    /**
     * Some context of usage :
     */
    @Named("idRelations")
    @InheritConfiguration(name = "processingToProcessingDTOWithIdRelationsPrototype")
    DatasetProcessingDTO processingToProcessingDTOWithIdRelations(DatasetProcessing processing);

    //Entity list

    /**
     * Some context of usage :
     */
    @IterableMapping(qualifiedByName = "nullRelations")
    List<DatasetProcessingDTO> processingListToProcessingDTOListWithNullRelations(List<DatasetProcessing> processings);
}

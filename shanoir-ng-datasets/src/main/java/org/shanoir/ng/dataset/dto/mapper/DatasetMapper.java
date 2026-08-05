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

import org.mapstruct.*;
import org.shanoir.ng.dataset.dto.DatasetDTO;
import org.shanoir.ng.dataset.model.Dataset;
import org.shanoir.ng.shared.core.model.IdName;
import org.shanoir.ng.shared.paging.PageImpl;
import org.springframework.data.domain.Page;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Mapper for datasets.
 *
 * @author msimon
 *
 */
@Mapper(componentModel = "spring", config = DatasetMappingConfig.class)
public interface DatasetMapper {

    /**
     * Map list of @Dataset to list of @IdNameDTO.
     *
     * @param datasets
     *            list of datasets.
     * @return list of datasets DTO.
     */
    List<IdName> datasetsToIdNameDTOs(List<Dataset> datasets);

    /**
     * Map a @Dataset to a @IdNameDTO.
     *
     * @param dataset dataset to map.
     *
     * @return dataset DTO.
     */
    IdName datasetToIdNameDTO(Dataset dataset);


    ////// Entity to DTO

    @Named("id")
    default Long datasetToLongId(Dataset dataset) {
        if (dataset == null) {
            return null;
        }
        return dataset.getId();
    }

    @Named("id")
    default List<Long> datasetsToLongIds(List<Dataset> datasets) {
        if (datasets == null) {
            return null;
        }
        return datasets.stream().filter(Objects::nonNull).map(Dataset::getId).collect(Collectors.toList());
    }

    @Named("idOnly")
    default DatasetDTO datasetToId(Dataset dataset) {
        if (dataset == null) {
            return null;
        }
        DatasetDTO dtoId = new DatasetDTO();
        dtoId.setId(dataset.getId());
        return dtoId;
    }

    @Named("idOnly")
    default List<DatasetDTO> datasetsToIds(List<Dataset> datasets) {
        if (datasets == null) {
            return null;
        }
        return datasets.stream().filter(Objects::nonNull).map(dto -> {
            DatasetDTO dtoId = new DatasetDTO();
            dtoId.setId(dto.getId());
            return dtoId;
        }).collect(Collectors.toList());
    }

    //Single entity

    /**
     * Some context of usage :
     */
    @Named("withMetadata")
    @InheritConfiguration(name = "datasetToDatasetDTOWithMetadataPrototype")
    DatasetDTO datasetToDatasetDTOWithMetadata(Dataset dataset);

    /**
     * Some context of usage :
     * - When opening a dataset form
     */
    @Named("withProcessingAncestorsAndExamination")
    @InheritConfiguration(name = "datasetToDatasetDTOWithProcessingAncestorsAndExaminationPrototype")
    DatasetDTO datasetToDatasetDTOWithProcessingAncestorsAndExamination(Dataset dataset);

    /**
     * Some context of usage :
     */
    @Named("withProcessing")
    @InheritConfiguration(name = "datasetToDatasetDTOWithProcessingPrototype")
    DatasetDTO datasetToDatasetDTOWithProcessing(Dataset dataset);

    /**
     * Some context of usage :
     * -
     */
    @Named("idRelations")
    @InheritConfiguration(name = "datasetToDatasetDTOWithIdRelationsPrototype")
    DatasetDTO datasetToDatasetDTOWithIdRelations(Dataset dataset);

    //Entity list

    /**
     * Some context of usage :
     * -
     */
    @IterableMapping(qualifiedByName = "withProcessingAncestorsAndExamination")
    List<DatasetDTO> datasetListToDatasetDTOListWithProcessingAncestorsAndExamination(List<Dataset> datasets);

    /**
     * Some context of usage :
     */
    @IterableMapping(qualifiedByName = "withMetadata")
    List<DatasetDTO> datasetListToDatasetDTOListWithMetadata(List<Dataset> dataset);

    /**
     * Some context of usage :
     * - When listing outputs of a processing in the tree
     */
    @IterableMapping(qualifiedByName = "withProcessing")
    List<DatasetDTO> datasetListToDatasetDTOListWithProcessing(List<Dataset> dataset);

    //////

    ////// Pageable

    /**
     * Some context of usage :
     * -
     */
    @IterableMapping(qualifiedByName = "idRelations")
    PageImpl<DatasetDTO> datasetPageToDatasetIdRelationsDTOPage(Page<Dataset> page);
}

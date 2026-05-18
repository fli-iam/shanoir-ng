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

package org.shanoir.ng.dataset.modality;

import java.util.List;

import org.mapstruct.DecoratedWith;
import org.mapstruct.IterableMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Named;
import org.shanoir.ng.dataset.dto.mapper.DatasetMapper;
import org.shanoir.ng.dataset.dto.mapper.DatasetMetadataMapper;
import org.shanoir.ng.datasetacquisition.dto.mapper.DatasetAcquisitionMapper;
import org.shanoir.ng.processing.dto.mapper.DatasetProcessingMapper;
import org.shanoir.ng.shared.core.model.IdName;
import org.shanoir.ng.shared.paging.PageImpl;
import org.shanoir.ng.tag.mapper.StudyTagMapper;
import org.springframework.data.domain.Page;

/**
 * Mapper for datasets.
 *
 * @author msimon
 *
 */
@Mapper(componentModel = "spring", uses = { DatasetMetadataMapper.class, DatasetProcessingMapper.class, DatasetAcquisitionMapper.class, StudyTagMapper.class, DatasetMapper.class })
@DecoratedWith(EegDatasetDecorator.class)
public interface EegDatasetMapper {


    /**
     * Map list of @Dataset to list of @IdNameDTO.
     *
     * @param datasets
     *            list of datasets.
     * @return list of datasets DTO.
     */
    List<IdName> datasetsToIdNameDTOs(List<EegDataset> datasets);

    /**
     * Map a @Dataset to a @DatasetDTO.
     *
     * @param dataset eegDataset.
     * @return dataset DTO.
     */
    @Named(value = "standard")
    EegDatasetDTO datasetToDatasetDTO(EegDataset dataset);

    /**
     * Map a @Dataset to a @DatasetDTO.
     *
     * @param dataset eegDataset.
     * @return dataset DTO.
     */
    @Named(value = "withProcessings")
    EegDatasetWithDependenciesDTO datasetToDatasetAndProcessingsDTO(EegDataset dataset);

    /**
     * Map a @Dataset to a @DatasetDTO.
     *
     * @param page eegDataset.
     * @return dataset DTO.
     */
    @IterableMapping(qualifiedByName = "standard")
    PageImpl<EegDatasetDTO> datasetToDatasetDTO(Page<EegDataset> page);

    /**
     * Map a @Dataset to a @IdNameDTO.
     *
     * @param dataset
     *            dataset to map.
     * @return dataset DTO.
     */
    IdName datasetToIdNameDTO(EegDataset dataset);
}

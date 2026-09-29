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

import org.mapstruct.*;
import org.shanoir.ng.dataset.dto.mapper.DatasetMappingConfig;
import org.shanoir.ng.shared.paging.PageImpl;
import org.springframework.data.domain.Page;

/**
 * Mapper for datasets.
 *
 * @author msimon
 *
 */
@Mapper(componentModel = "spring", config = DatasetMappingConfig.class)
public interface MrDatasetMapper {

    //Single entity

    @Named("idRelations")
    @InheritConfiguration(name = "datasetToDatasetDTOWithIdRelationsPrototype")
    @Mapping(target = "echoTime", expression = "java(null)")
    @Mapping(target = "flipAngle", expression = "java(null)")
    @Mapping(target = "inversionTime", expression = "java(null)")
    @Mapping(target = "repetitionTime", expression = "java(null)")
    MrDatasetDTO mrDatasetToMrDatasetDTOWithIdRelations(MrDataset dataset);

    @Named("withProcessingAncestorsAndExamination")
    @InheritConfiguration(name = "datasetToDatasetDTOWithProcessingAncestorsAndExaminationPrototype")
    @Mapping(target = "echoTime", expression = "java(null)")
    @Mapping(target = "flipAngle", expression = "java(null)")
    @Mapping(target = "inversionTime", expression = "java(null)")
    @Mapping(target = "repetitionTime", expression = "java(null)")
    MrDatasetDTO mrDatasetToMrDatasetDTOWithProcessingAncestorsAndExamination(MrDataset dataset);

    @Named("withMetadata")
    @InheritConfiguration(name = "datasetToDatasetDTOWithMetadataPrototype")
    @Mapping(target = "echoTime", expression = "java(null)")
    @Mapping(target = "flipAngle", expression = "java(null)")
    @Mapping(target = "inversionTime", expression = "java(null)")
    @Mapping(target = "repetitionTime", expression = "java(null)")
    MrDatasetDTO mrDatasetToMrDatasetDTOWithMetadata(MrDataset dataset);

    ////// Pageable

    @IterableMapping(qualifiedByName = "idRelations")
    PageImpl<MrDatasetDTO> mrDatasetPageToMrDatasetDTOPage(Page<MrDataset> page);
}

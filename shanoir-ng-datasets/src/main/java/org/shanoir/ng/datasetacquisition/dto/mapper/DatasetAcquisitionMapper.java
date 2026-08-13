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
import java.util.Objects;
import java.util.stream.Collectors;

import org.hibernate.Hibernate;
import org.mapstruct.*;
import org.shanoir.ng.datasetacquisition.dto.DatasetAcquisitionDTO;
import org.shanoir.ng.datasetacquisition.model.DatasetAcquisition;

import org.shanoir.ng.datasetacquisition.model.GenericDatasetAcquisition;
import org.shanoir.ng.datasetacquisition.model.ct.CtDatasetAcquisition;
import org.shanoir.ng.datasetacquisition.model.mr.MrDatasetAcquisition;
import org.shanoir.ng.datasetacquisition.model.pet.PetDatasetAcquisition;
import org.shanoir.ng.datasetacquisition.model.rt.RtDatasetAcquisition;
import org.shanoir.ng.datasetacquisition.model.xa.XaDatasetAcquisition;
import org.shanoir.ng.shared.paging.PageImpl;
import org.springframework.data.domain.Page;

@Mapper(componentModel = "spring", config = DatasetAcquisitionMappingConfig.class)
public interface DatasetAcquisitionMapper {

    ////// Entity to DTO

    @Named("id")
    default Long acquisitionToLongId(DatasetAcquisition acquisition) {
        if (acquisition == null) {
            return null;
        }
        return acquisition.getId();
    }

    @Named("id")
    default List<Long> acquisitionsToLongIds(List<DatasetAcquisition> acquisitions) {
        if (acquisitions == null) {
            return null;
        }
        return acquisitions.stream().filter(Objects::nonNull).map(DatasetAcquisition::getId).collect(Collectors.toList());
    }

    @Named("idOnly")
    default DatasetAcquisitionDTO acquisitionToId(DatasetAcquisition acquisition) {
        if (acquisition == null) {
            return null;
        }
        DatasetAcquisitionDTO dto = new DatasetAcquisitionDTO();
        dto.setId(acquisition.getId());
        return dto;
    }

    @Named("idOnly")
    default List<DatasetAcquisitionDTO> acquisitionsToIds(List<DatasetAcquisition> acquisitions) {
        if (acquisitions == null) {
            return null;
        }
        return acquisitions.stream().filter(Objects::nonNull).map(acq -> {
            DatasetAcquisitionDTO dto = new DatasetAcquisitionDTO();
            dto.setId(acq.getId());
            return dto;
        }).collect(Collectors.toList());
    }

    //Single entity

    /**
     * Some context of usage :
     */
    @Named("nullRelations")
    @Mapping(target = "examination", expression = "java(null)")
    @Mapping(target = "copies", expression = "java(null)")
    @Mapping(target = "source", expression = "java(null)")
    @Mapping(target = "studyCard", expression = "java(null)")
    DatasetAcquisitionDTO acquisitionToAcquisitionNullRelationsDTO(DatasetAcquisition acquisition);

    /**
     * Some context of usage :
     */
    @Named("idRelations")
    @Mapping(target = "examination", source = "examination", qualifiedByName = "idOnly")
    @Mapping(target = "copies", source = "copies", qualifiedByName = "id")
    @Mapping(target = "source", source = "source", qualifiedByName = "id")
    @Mapping(target = "studyCard", expression = "java(null)")
    DatasetAcquisitionDTO acquisitionToAcquisitionIdRelationsDTO(DatasetAcquisition datasetAcquisition);

    /**
     * Some context of usage :
     * - Loading tree from lower entity (dataset)
     */
    @Named("withExamination")
    @Mapping(target = "examination", source = "examination", qualifiedByName = "withStudy")
    @Mapping(target = "copies", expression = "java(null)")
    @Mapping(target = "source", expression = "java(null)")
    @Mapping(target = "studyCard", expression = "java(null)")
    DatasetAcquisitionDTO acquisitionToAcquisitionWithExaminationDTO(DatasetAcquisition datasetAcquisition);



    ////// DTO to entity

    @Named("idOnly")
    default DatasetAcquisition acquisitionDTOToId(Long id) {
        if (id == null) {
            return null;
        }
        DatasetAcquisition acquisition = new GenericDatasetAcquisition();
        acquisition.setId(id);
        return acquisition;
    }

    @Named("idOnly")
    default List<DatasetAcquisition> acquisitionDTOListToIds(List<Long> ids) {
        if (ids == null) {
            return null;
        }
        return ids.stream().filter(Objects::nonNull).map(id -> {
            DatasetAcquisition acquisition = new GenericDatasetAcquisition();
            acquisition.setId(id);
            return acquisition;
        }).collect(Collectors.toList());
    }

    // Single entity

    /**
     * Some context of usage :
     */
    @Named("mediumRelations")
    @Mapping(target = "examination", source = "examination", qualifiedByName = "idRelations")
    @Mapping(target = "copies", source = "copies", qualifiedByName = "idOnly")
    @Mapping(target = "source", source = "source", qualifiedByName = "idOnly")
    @Mapping(target = "studyCard", expression = "java(null)")
    GenericDatasetAcquisition acquisitionDTOToAcquisitionIdRelations(DatasetAcquisitionDTO dto);

    ////// Pageable

    @IterableMapping(qualifiedByName = "withExamination")
    PageImpl<DatasetAcquisitionDTO> acquisitionPageToAcquisitionWithExaminationDTOPage(Page<DatasetAcquisition> page);

    @IterableMapping(qualifiedByName = "idRelations")
    PageImpl<DatasetAcquisitionDTO> acquisitionPageToAcquisitionIdRelationsDTOPage(Page<DatasetAcquisition> page);

    ////// Miscellaneous

    @AfterMapping
    default void setType(DatasetAcquisition acquisition, @MappingTarget DatasetAcquisitionDTO dto) {
        DatasetAcquisition unproxiedAcq = (DatasetAcquisition) Hibernate.unproxy(acquisition); // Can not cast a proxy
        if (unproxiedAcq.getType().equals("Mr")) {
            dto.setProtocol(((MrDatasetAcquisition) unproxiedAcq).getMrProtocol());
        } else if (unproxiedAcq.getType().equals("Pet")) {
            dto.setProtocol(((PetDatasetAcquisition) unproxiedAcq).getPetProtocol());
        } else if (unproxiedAcq.getType().equals("Ct")) {
            dto.setProtocol(((CtDatasetAcquisition) unproxiedAcq).getCtProtocol());
        } else if (unproxiedAcq.getType().equals("Xa")) {
            dto.setProtocol(((XaDatasetAcquisition) unproxiedAcq).getXaProtocol());
        }
    }
}

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

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.hibernate.Hibernate;
import org.shanoir.ng.dataset.dto.DatasetDTO;
import org.shanoir.ng.dataset.dto.DatasetWithDependenciesDTO;
import org.shanoir.ng.dataset.modality.EegDataset;
import org.shanoir.ng.dataset.modality.EegDatasetMapper;
import org.shanoir.ng.dataset.modality.MrDataset;
import org.shanoir.ng.dataset.modality.MrDatasetMapper;
import org.shanoir.ng.dataset.model.Dataset;
import org.shanoir.ng.dataset.service.DatasetService;
import org.shanoir.ng.datasetacquisition.dto.mapper.DatasetAcquisitionMapper;
import org.shanoir.ng.processing.dto.mapper.DatasetProcessingMapper;
import org.shanoir.ng.shared.core.model.IdName;
import org.shanoir.ng.shared.paging.PageImpl;
import org.shanoir.ng.tag.mapper.StudyTagMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.transaction.annotation.Transactional;

/**
 * Decorator for dataset acquisitions mapper.
 *
 * @author msimon
 * @author jlouis
 *
 */
public abstract class DatasetDecorator implements DatasetMapper {

    @Autowired
    private DatasetMapper delegate;

    @Autowired
    private MrDatasetMapper mrMapper;

    @Autowired
    private EegDatasetMapper eegMapper;

    @Autowired
    protected DatasetMetadataMapper datasetMetadataMapper;

    @Autowired
    protected DatasetService datasetService;

    @Autowired
    protected DatasetProcessingMapper datasetProcessingMapper;

    @Autowired
    protected DatasetAcquisitionMapper datasetAcquisitionMapper;

    @Autowired
    protected StudyTagMapper studyTagMapper;

    @Override
    public List<IdName> datasetsToIdNameDTOs(final List<Dataset> datasets) {
        final List<IdName> datasetDTOs = new ArrayList<>();
        for (Dataset dataset : datasets) {
            datasetDTOs.add(datasetToIdNameDTO(dataset));
        }
        return datasetDTOs;
    }

    @Override
    @Transactional(readOnly = true)
    public PageImpl<DatasetDTO> datasetToDatasetDTO(Page<Dataset> page) {
        Page<DatasetDTO> mappedPage = page.map(new Function<Dataset, DatasetDTO>() {
            public DatasetDTO apply(Dataset entity) {
                if (entity instanceof MrDataset) {
                    return mrMapper.datasetToDatasetDTO((MrDataset) entity);
                } else if (entity instanceof EegDataset) {
                    return eegMapper.datasetToDatasetDTO((EegDataset) entity);
                } else {
                    return datasetToDatasetDTO(entity);
                }
            }
        });
        return new PageImpl<>(mappedPage);
    }

    @Override
    @Transactional(readOnly = true)
    public DatasetDTO datasetToDatasetDTO(Dataset dataset) {
        if (dataset == null) {
            return null;
        }

        DatasetDTO datasetDTO = new DatasetDTO();

        datasetDTO.setCreationDate(dataset.getCreationDate());
        datasetDTO.setGroupOfSubjectsId(dataset.getGroupOfSubjectsId());
        datasetDTO.setId(dataset.getId());
        datasetDTO.setOriginMetadata(datasetMetadataMapper.datasetMetadataToDatasetMetadataDTO(dataset.getOriginMetadata()));
        datasetDTO.setStudyId(dataset.getStudyId());
        datasetDTO.setSubjectId(dataset.getSubjectId());
        datasetDTO.setUpdatedMetadata(datasetMetadataMapper.datasetMetadataToDatasetMetadataDTO(dataset.getUpdatedMetadata()));
        datasetDTO.setName(dataset.getName());
        if (dataset.getType() != null) {
            datasetDTO.setType(dataset.getType().name());
        }
        datasetDTO.setCenterId(datasetService.getCenterId(dataset));
        datasetDTO.setInPacs(dataset.getInPacs());
        datasetDTO.setTags(studyTagMapper.studyTagListToStudyTagDTOLightList(dataset.getTags()));
        datasetDTO.setSource(mapSourceFromDataset(dataset.getSource()));
        datasetDTO.setCopies(mapCopiesFromDataset(dataset.getCopies()));

        return datasetDTO;
    }

    @Override
    @Transactional(readOnly = true)
    public DatasetWithDependenciesDTO datasetToDatasetWithParentsAndProcessingsDTO(Dataset dataset) {
        if (dataset == null) {
            return null;
        }

        DatasetWithDependenciesDTO datasetWithDependenciesDTO = new DatasetWithDependenciesDTO();

        datasetWithDependenciesDTO.setCreationDate(dataset.getCreationDate());
        datasetWithDependenciesDTO.setGroupOfSubjectsId(dataset.getGroupOfSubjectsId());
        datasetWithDependenciesDTO.setId(dataset.getId());
        datasetWithDependenciesDTO.setOriginMetadata(datasetMetadataMapper.datasetMetadataToDatasetMetadataDTO(dataset.getOriginMetadata()));
        datasetWithDependenciesDTO.setStudyId(dataset.getStudyId());
        datasetWithDependenciesDTO.setSubjectId(dataset.getSubjectId());
        datasetWithDependenciesDTO.setUpdatedMetadata(datasetMetadataMapper.datasetMetadataToDatasetMetadataDTO(dataset.getUpdatedMetadata()));
        datasetWithDependenciesDTO.setName(dataset.getName());
        if (dataset.getType() != null) {
            datasetWithDependenciesDTO.setType(dataset.getType().name());
        }
        datasetWithDependenciesDTO.setCenterId(datasetService.getCenterId(dataset));
        datasetWithDependenciesDTO.setInPacs(dataset.getInPacs());
        datasetWithDependenciesDTO.setTags(studyTagMapper.studyTagListToStudyTagDTOLightList(dataset.getTags()));
        datasetWithDependenciesDTO.setProcessings(datasetProcessingMapper.datasetProcessingListToDatasetProcessingDTOList(dataset.getProcessings()));
        datasetWithDependenciesDTO.setDatasetAcquisition(datasetAcquisitionMapper.datasetAcquisitionToDatasetAcquisitionDTO(dataset.getDatasetAcquisition()));
        datasetWithDependenciesDTO.setDatasetProcessing(datasetProcessingMapper.datasetProcessingToDatasetProcessingDTO(dataset.getDatasetProcessing()));

        datasetWithDependenciesDTO.setCopies(mapCopiesFromDataset(dataset.getCopies()));
        datasetWithDependenciesDTO.setSource(mapSourceFromDataset(dataset.getSource()));

        Hibernate.initialize(dataset.getCopies());
        datasetWithDependenciesDTO.setCopies(dataset.getCopies().stream()
                .map(Dataset::getId)
                .collect(Collectors.toList()));
        return datasetWithDependenciesDTO;
    }

    //Only for DB optimization (creating one connexion for the list, otherwise would be for each element)
    @Override
    @Transactional(readOnly = true)
    public List<DatasetDTO> datasetListToDatasetDTOList(List<Dataset> datasets) {
        return delegate.datasetListToDatasetDTOList(datasets);
    }
}

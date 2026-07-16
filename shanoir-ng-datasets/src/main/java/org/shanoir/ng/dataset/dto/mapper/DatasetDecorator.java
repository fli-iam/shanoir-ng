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

import java.util.function.Function;

import org.shanoir.ng.dataset.dto.DatasetDTO;
import org.shanoir.ng.dataset.modality.EegDataset;
import org.shanoir.ng.dataset.modality.EegDatasetMapper;
import org.shanoir.ng.dataset.modality.MrDataset;
import org.shanoir.ng.dataset.modality.MrDatasetMapper;
import org.shanoir.ng.dataset.model.Dataset;
import org.shanoir.ng.shared.paging.PageImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;

/**
 * Decorator for dataset acquisitions mapper.
 *
 * @author msimon
 * @author jlouis
 *
 */
public abstract class DatasetDecorator implements DatasetMapper {

    @Autowired
    private DatasetMapper defaultMapper;

    @Autowired
    private MrDatasetMapper mrMapper;

    @Autowired
    protected EegDatasetMapper eegMapper;

    @Autowired
    protected DatasetMetadataMapper metadataMapper;

    @Override
    public PageImpl<DatasetDTO> datasetToDatasetDTO(Page<Dataset> page) {
        Page<DatasetDTO> mappedPage = page.map(new Function<Dataset, DatasetDTO>() {
            public DatasetDTO apply(Dataset entity) {
                if (entity instanceof MrDataset) {
                    return mrMapper.datasetToDatasetDTO((MrDataset) entity);
                } else if (entity instanceof EegDataset) {
                    return eegMapper.datasetToDatasetDTO((EegDataset) entity);
                } else {
                    return defaultMapper.datasetToDatasetDTO(entity);
                }
            }
        });
        return new PageImpl<>(mappedPage);
    }

    @Override
    public DatasetDTO datasetLightToDatasetLightDTO(Dataset dataset) {
        if (dataset == null) {
            return null;
        }

        DatasetDTO datasetDTO = new DatasetDTO();

        datasetDTO.setCreationDate(dataset.getCreationDate());
        datasetDTO.setGroupOfSubjectsId(dataset.getGroupOfSubjectsId());
        datasetDTO.setId(dataset.getId());
        datasetDTO.setOriginMetadata(metadataMapper.datasetMetadataToDatasetMetadataDTO(dataset.getOriginMetadata()));
        datasetDTO.setStudyId(dataset.getStudyId());
        datasetDTO.setSubjectId(dataset.getSubjectId());
        datasetDTO.setUpdatedMetadata(metadataMapper.datasetMetadataToDatasetMetadataDTO(dataset.getUpdatedMetadata()));
        datasetDTO.setName(dataset.getName());
        if (dataset.getType() != null) {
            datasetDTO.setType(dataset.getType().name());
        }
        datasetDTO.setCenterId(dataset.getCenterId());
        datasetDTO.setInPacs(dataset.getInPacs());
        datasetDTO.setTags(null);
        datasetDTO.setSource(null);
        datasetDTO.setCopies(null);

        return datasetDTO;
    }
}

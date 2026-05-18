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

import java.util.ArrayList;
import java.util.List;

import org.shanoir.ng.dataset.dto.DatasetWithProcessingsDTO;
import org.shanoir.ng.dataset.modality.BidsDataset;
import org.shanoir.ng.dataset.model.Dataset;
import org.shanoir.ng.dataset.model.DatasetType;
import org.shanoir.ng.dataset.service.DatasetService;
import org.shanoir.ng.datasetacquisition.dto.ExaminationDatasetAcquisitionDTO;
import org.shanoir.ng.datasetacquisition.model.DatasetAcquisition;
import org.shanoir.ng.datasetacquisition.model.bids.BidsDatasetAcquisition;
import org.shanoir.ng.processing.dto.mapper.DatasetProcessingMapper;
import org.shanoir.ng.tag.mapper.StudyTagMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

/**
 * Decorator for dataset acquisitions mapper.
 *
 * @author msimon
 *
 */
@Component
public abstract class ExaminationDatasetAcquisitionDecorator implements ExaminationDatasetAcquisitionMapper {

    @Autowired
    private ExaminationDatasetAcquisitionMapper delegate;

    @Autowired
    private DatasetService datasetService;

    @Autowired
    protected StudyTagMapper studyTagMapper;

    @Autowired
    protected DatasetProcessingMapper datasetProcessingMapper;

    @Override
    public List<ExaminationDatasetAcquisitionDTO> datasetAcquisitionsToExaminationDatasetAcquisitionDTOs(
            final List<DatasetAcquisition> datasetAcquisitions) {
        if (datasetAcquisitions == null) {
            return null;
        }
        final List<ExaminationDatasetAcquisitionDTO> datasetAcquisitionDTOs = new ArrayList<>();
        for (DatasetAcquisition datasetAcquisition : datasetAcquisitions) {
            datasetAcquisitionDTOs.add(datasetAcquisitionToExaminationDatasetAcquisitionDTO(datasetAcquisition));
        }
        return datasetAcquisitionDTOs;
    }

    @Override
    public ExaminationDatasetAcquisitionDTO datasetAcquisitionToExaminationDatasetAcquisitionDTO(
            final DatasetAcquisition datasetAcquisition) {
        final ExaminationDatasetAcquisitionDTO datasetAcquisitionDTO = delegate
                .datasetAcquisitionToExaminationDatasetAcquisitionDTO(datasetAcquisition);
        datasetAcquisitionDTO.setName(getExaminationDatasetAcquisitionDTOName(datasetAcquisition));
        datasetAcquisitionDTO.setStudyId(datasetAcquisition.getExamination().getStudyId());
        datasetAcquisitionDTO.setExaminationId(datasetAcquisition.getExamination().getId());
        return datasetAcquisitionDTO;
    }

    @Override
    public DatasetWithProcessingsDTO datasetToDatasetWithProcessingsDTO(Dataset dataset) {
        if (dataset == null) {
            return null;
        }

        DatasetWithProcessingsDTO datasetWithProcessingsDTO = new DatasetWithProcessingsDTO();

        datasetWithProcessingsDTO.setCreationDate(dataset.getCreationDate());
        datasetWithProcessingsDTO.setGroupOfSubjectsId(dataset.getGroupOfSubjectsId());
        datasetWithProcessingsDTO.setId(dataset.getId());
        datasetWithProcessingsDTO.setOriginMetadata(datasetMetadataToDatasetMetadataDTO(dataset.getOriginMetadata()));
        datasetWithProcessingsDTO.setStudyId(dataset.getStudyId());
        datasetWithProcessingsDTO.setSubjectId(dataset.getSubjectId());
        datasetWithProcessingsDTO.setUpdatedMetadata(datasetMetadataToDatasetMetadataDTO(dataset.getUpdatedMetadata()));
        datasetWithProcessingsDTO.setName(dataset.getName());
        if (dataset.getType() != null) {
            datasetWithProcessingsDTO.setType(dataset.getType().name());
        }
        datasetWithProcessingsDTO.setCenterId(datasetService.getCenterId(dataset));
        datasetWithProcessingsDTO.setInPacs(dataset.getInPacs());
        datasetWithProcessingsDTO.setTags(studyTagMapper.studyTagListToStudyTagDTOLightList(dataset.getTags()));
        datasetWithProcessingsDTO.setProcessings(datasetProcessingMapper.datasetProcessingListToDatasetProcessingDTOList(dataset.getProcessings()));

        return datasetWithProcessingsDTO;
    }

    /**
     * Get dataset acquisition name. If all the datasets have the same name,
     * then return the name of the datasets. Else if all the datasets have the
     * same comment, then return the comment of the datasets.
     *
     * @param datasetAcquisition dataset acquisition.
     * @return name.
     */
    private String getExaminationDatasetAcquisitionDTOName(final DatasetAcquisition datasetAcquisition) {
        final StringBuilder result = new StringBuilder();
        final List<String> datasetNameSet = new ArrayList<>();
        final List<String> datasetCommentSet = new ArrayList<>();
        if (datasetAcquisition instanceof BidsDatasetAcquisition) {
            BidsDatasetAcquisition bidsDataAcq = (BidsDatasetAcquisition) datasetAcquisition;
            if (!CollectionUtils.isEmpty(bidsDataAcq.getDatasets())) {
                datasetNameSet.add(((BidsDataset) bidsDataAcq.getDatasets().get(0)).getBidsDataType());
            }
        } else if (datasetAcquisition.getDatasets() != null) {
            for (final Dataset dataset : datasetAcquisition.getDatasets()) {
                if (!DatasetType.MEASUREMENT.equals(dataset.getType())) {
                    final String datasetName = dataset.getName();
                    if (!StringUtils.isEmpty(datasetName) && !datasetNameSet.contains(datasetName)) {
                        datasetNameSet.add(datasetName);
                    }
                    String datasetComment = null;
                    if (dataset.getUpdatedMetadata() != null && dataset.getUpdatedMetadata().getComment() != null) {
                        datasetComment = dataset.getUpdatedMetadata().getComment();
                    } else if (dataset.getOriginMetadata() != null && dataset.getOriginMetadata().getComment() != null) {
                        datasetComment = dataset.getOriginMetadata().getComment();
                    }
                    if (!StringUtils.isEmpty(datasetComment) && !datasetCommentSet.contains(datasetComment)) {
                        datasetCommentSet.add(datasetComment);
                    }
                }
            }
        }

        if (datasetNameSet.size() == 1) {
            result.append(datasetNameSet.get(0));
        } else if (datasetCommentSet.size() == 1) {
            result.append(datasetCommentSet.get(0));
        } else if (datasetNameSet.size() > 1) {
            for (final String name : datasetNameSet) {
                result.append(name).append(" ");
            }
            result.deleteCharAt(result.length() - 1);
        } else {
            result.append("id=").append(datasetAcquisition.getId());
            if (datasetAcquisition.getRank() != null) {
                result.append(" rank=").append(datasetAcquisition.getRank());
            }
        }

        final String type = datasetAcquisition.getType();
        return result.append(" (").append(type).append(")").toString();
    }

}

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

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

import org.shanoir.ng.dataset.dto.mapper.DatasetMapper;
import org.shanoir.ng.dataset.dto.mapper.DatasetMetadataMapper;
import org.shanoir.ng.dataset.service.DatasetService;
import org.shanoir.ng.datasetacquisition.dto.mapper.DatasetAcquisitionMapper;
import org.shanoir.ng.processing.dto.mapper.DatasetProcessingMapper;
import org.shanoir.ng.shared.core.model.IdName;
import org.shanoir.ng.shared.model.EchoTime;
import org.shanoir.ng.shared.model.FlipAngle;
import org.shanoir.ng.shared.model.InversionTime;
import org.shanoir.ng.shared.model.RepetitionTime;
import org.shanoir.ng.shared.paging.PageImpl;
import org.shanoir.ng.tag.mapper.StudyTagMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Decorator for dataset acquisitions mapper.
 *
 * @author msimon
 *
 */
@Component
public abstract class MrDatasetDecorator implements MrDatasetMapper {

    @Autowired
    private MrDatasetMapper delegate;

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

    @Autowired
    protected DatasetMapper datasetMapper;

    @Override
    public List<IdName> datasetsToIdNameDTOs(final List<MrDataset> datasets) {
        final List<IdName> datasetDTOs = new ArrayList<>();
        for (MrDataset dataset : datasets) {
            datasetDTOs.add(datasetToIdNameDTO(dataset));
        }
        return datasetDTOs;
    }

    @Override
    public IdName datasetToIdNameDTO(final MrDataset dataset) {
        return delegate.datasetToIdNameDTO(dataset);
    }

    @Override
    @Transactional(readOnly = true)
    public PageImpl<MrDatasetDTO> datasetToDatasetDTO(Page<MrDataset> page) {
        org.springframework.data.domain.Page<MrDatasetDTO> mappedPage =  page.map(new Function<MrDataset, MrDatasetDTO>() {
            public MrDatasetDTO apply(MrDataset entity) {
                return delegate.datasetToDatasetDTO(entity);
            }
        });
        return new PageImpl<>(mappedPage.getContent());
    }

    @Override
    @Transactional(readOnly = true)
    public MrDatasetDTO datasetToDatasetDTO(MrDataset dataset) {
        if (dataset == null) {
            return null;
        }

        //Manage Lazy loadings
        dataset.setDatasetExpressions(datasetService.getDatasetExpressions(dataset));
        dataset.setTags(datasetService.getTags(dataset));

        MrDatasetDTO mrDatasetDTO = new MrDatasetDTO();

        mrDatasetDTO.setCreationDate(dataset.getCreationDate());
        mrDatasetDTO.setGroupOfSubjectsId(dataset.getGroupOfSubjectsId());
        mrDatasetDTO.setId(dataset.getId());
        mrDatasetDTO.setOriginMetadata(datasetMetadataMapper.datasetMetadataToDatasetMetadataDTO(dataset.getOriginMetadata()));
        mrDatasetDTO.setStudyId(dataset.getStudyId());
        mrDatasetDTO.setSubjectId(dataset.getSubjectId());
        mrDatasetDTO.setUpdatedMetadata(datasetMetadataMapper.datasetMetadataToDatasetMetadataDTO(dataset.getUpdatedMetadata()));
        mrDatasetDTO.setName(dataset.getName());
        if (dataset.getType() != null) {
            mrDatasetDTO.setType(dataset.getType().name());
        }
        mrDatasetDTO.setCenterId(datasetService.getCenterId(dataset));
        mrDatasetDTO.setInPacs(dataset.getInPacs());
        mrDatasetDTO.setTags(studyTagMapper.studyTagListToStudyTagDTOLightList(dataset.getTags()));
        List<EchoTime> list1 = dataset.getEchoTime();
        if (list1 != null) {
            mrDatasetDTO.setEchoTime(new ArrayList<EchoTime>(list1));
        }
        List<FlipAngle> list2 = dataset.getFlipAngle();
        if (list2 != null) {
            mrDatasetDTO.setFlipAngle(new ArrayList<FlipAngle>(list2));
        }
        List<InversionTime> list3 = dataset.getInversionTime();
        if (list3 != null) {
            mrDatasetDTO.setInversionTime(new ArrayList<InversionTime>(list3));
        }
        List<RepetitionTime> list4 = dataset.getRepetitionTime();
        if (list4 != null) {
            mrDatasetDTO.setRepetitionTime(new ArrayList<RepetitionTime>(list4));
        }
        mrDatasetDTO.setOriginMrMetadata(dataset.getOriginMrMetadata());
        mrDatasetDTO.setUpdatedMrMetadata(dataset.getUpdatedMrMetadata());

        return mrDatasetDTO;
    }

    @Override
    @Transactional(readOnly = true)
    public MrDatasetWithDependenciesDTO datasetToDatasetAndProcessingsDTO(MrDataset dataset) {
        if (dataset == null) {
            return null;
        }

        //Manage Lazy loadings
        dataset.setDatasetExpressions(datasetService.getDatasetExpressions(dataset));
        dataset.setTags(datasetService.getTags(dataset));
        dataset.setProcessings(datasetService.getProcessings(dataset));

        MrDatasetWithDependenciesDTO mrDatasetWithDependenciesDTO = new MrDatasetWithDependenciesDTO();

        mrDatasetWithDependenciesDTO.setCreationDate(dataset.getCreationDate());
        mrDatasetWithDependenciesDTO.setGroupOfSubjectsId(dataset.getGroupOfSubjectsId());
        mrDatasetWithDependenciesDTO.setId(dataset.getId());
        mrDatasetWithDependenciesDTO.setOriginMetadata(datasetMetadataMapper.datasetMetadataToDatasetMetadataDTO(dataset.getOriginMetadata()));
        mrDatasetWithDependenciesDTO.setStudyId(dataset.getStudyId());
        mrDatasetWithDependenciesDTO.setSubjectId(dataset.getSubjectId());
        mrDatasetWithDependenciesDTO.setUpdatedMetadata(datasetMetadataMapper.datasetMetadataToDatasetMetadataDTO(dataset.getUpdatedMetadata()));
        mrDatasetWithDependenciesDTO.setName(dataset.getName());
        if (dataset.getType() != null) {
            mrDatasetWithDependenciesDTO.setType(dataset.getType().name());
        }
        mrDatasetWithDependenciesDTO.setCenterId(datasetService.getCenterId(dataset));
        mrDatasetWithDependenciesDTO.setInPacs(dataset.getInPacs());
        mrDatasetWithDependenciesDTO.setTags(studyTagMapper.studyTagListToStudyTagDTOLightList(dataset.getTags()));
        List<EchoTime> list1 = dataset.getEchoTime();
        if (list1 != null) {
            mrDatasetWithDependenciesDTO.setEchoTime(new ArrayList<EchoTime>(list1));
        }
        List<FlipAngle> list2 = dataset.getFlipAngle();
        if (list2 != null) {
            mrDatasetWithDependenciesDTO.setFlipAngle(new ArrayList<FlipAngle>(list2));
        }
        List<InversionTime> list3 = dataset.getInversionTime();
        if (list3 != null) {
            mrDatasetWithDependenciesDTO.setInversionTime(new ArrayList<InversionTime>(list3));
        }
        List<RepetitionTime> list4 = dataset.getRepetitionTime();
        if (list4 != null) {
            mrDatasetWithDependenciesDTO.setRepetitionTime(new ArrayList<RepetitionTime>(list4));
        }
        mrDatasetWithDependenciesDTO.setOriginMrMetadata(dataset.getOriginMrMetadata());
        mrDatasetWithDependenciesDTO.setUpdatedMrMetadata(dataset.getUpdatedMrMetadata());
        mrDatasetWithDependenciesDTO.setProcessings(datasetProcessingMapper.datasetProcessingListToDatasetProcessingDTOList(dataset.getProcessings()));
        mrDatasetWithDependenciesDTO.setDatasetAcquisition(datasetAcquisitionMapper.datasetAcquisitionToDatasetAcquisitionDTO(dataset.getDatasetAcquisition()));
        mrDatasetWithDependenciesDTO.setDatasetProcessing(datasetProcessingMapper.datasetProcessingToDatasetProcessingDTO(dataset.getDatasetProcessing()));

        mrDatasetWithDependenciesDTO.setCopies(datasetMapper.mapCopiesFromDataset(dataset.getCopies()));
        mrDatasetWithDependenciesDTO.setSource(datasetMapper.mapSourceFromDataset(dataset.getSource()));

        return mrDatasetWithDependenciesDTO;
    }
}

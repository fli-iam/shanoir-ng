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
import org.shanoir.ng.eeg.model.Channel;
import org.shanoir.ng.eeg.model.Event;
import org.shanoir.ng.processing.dto.mapper.DatasetProcessingMapper;
import org.shanoir.ng.shared.core.model.IdName;
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
public abstract class EegDatasetDecorator implements EegDatasetMapper {

    @Autowired
    private EegDatasetMapper delegate;

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
    public List<IdName> datasetsToIdNameDTOs(final List<EegDataset> datasets) {
        final List<IdName> datasetDTOs = new ArrayList<>();
        for (EegDataset dataset : datasets) {
            datasetDTOs.add(datasetToIdNameDTO(dataset));
        }
        return datasetDTOs;
    }

    @Override
    public IdName datasetToIdNameDTO(final EegDataset dataset) {
        return delegate.datasetToIdNameDTO(dataset);
    }

    @Override
    @Transactional(readOnly = true)
    public PageImpl<EegDatasetDTO> datasetToDatasetDTO(Page<EegDataset> page) {
        org.springframework.data.domain.Page<EegDatasetDTO> mappedPage =  page.map(new Function<EegDataset, EegDatasetDTO>() {
            public EegDatasetDTO apply(EegDataset entity) {
                return delegate.datasetToDatasetDTO(entity);
            }
        });
        return new PageImpl<>(mappedPage.getContent());
    }

    @Override
    @Transactional(readOnly = true)
    public EegDatasetDTO datasetToDatasetDTO(EegDataset dataset) {
        if (dataset == null) {
            return null;
        }

        //Manage Lazy loadings
        dataset.setDatasetExpressions(datasetService.getDatasetExpressions(dataset));
        dataset.setTags(datasetService.getTags(dataset));

        EegDatasetDTO eegDatasetDTO = new EegDatasetDTO();

        eegDatasetDTO.setCreationDate(dataset.getCreationDate());
        eegDatasetDTO.setGroupOfSubjectsId(dataset.getGroupOfSubjectsId());
        eegDatasetDTO.setId(dataset.getId());
        eegDatasetDTO.setOriginMetadata(datasetMetadataMapper.datasetMetadataToDatasetMetadataDTO(dataset.getOriginMetadata()));
        eegDatasetDTO.setStudyId(dataset.getStudyId());
        eegDatasetDTO.setSubjectId(dataset.getSubjectId());
        eegDatasetDTO.setUpdatedMetadata(datasetMetadataMapper.datasetMetadataToDatasetMetadataDTO(dataset.getUpdatedMetadata()));
        eegDatasetDTO.setName(dataset.getName());
        if (dataset.getType() != null) {
            eegDatasetDTO.setType(dataset.getType().name());
        }
        eegDatasetDTO.setCenterId(datasetService.getCenterId(dataset));
        eegDatasetDTO.setInPacs(dataset.getInPacs());
        eegDatasetDTO.setTags(studyTagMapper.studyTagListToStudyTagDTOLightList(dataset.getTags()));
        eegDatasetDTO.setSource(datasetMapper.mapSourceFromDataset(dataset.getSource()));
        eegDatasetDTO.setCopies(datasetMapper.mapCopiesFromDataset(dataset.getCopies()));
        eegDatasetDTO.setCoordinatesSystem(dataset.getCoordinatesSystem());
        eegDatasetDTO.setSamplingFrequency(dataset.getSamplingFrequency());
        eegDatasetDTO.setChannelCount(dataset.getChannelCount());
        List<Channel> list2 = dataset.getChannels();
        if (list2 != null) {
            eegDatasetDTO.setChannels(new ArrayList<Channel>(list2));
        }
        List<Event> list3 = dataset.getEvents();
        if (list3 != null) {
            eegDatasetDTO.setEvents(new ArrayList<Event>(list3));
        }

        return eegDatasetDTO;
    }

    @Override
    @Transactional(readOnly = true)
    public EegDatasetWithDependenciesDTO datasetToDatasetAndProcessingsDTO(EegDataset dataset) {
        if (dataset == null) {
            return null;
        }

        //Manage Lazy loadings
        dataset.setDatasetExpressions(datasetService.getDatasetExpressions(dataset));
        dataset.setTags(datasetService.getTags(dataset));
        dataset.setProcessings(datasetService.getProcessings(dataset));

        EegDatasetWithDependenciesDTO eegDatasetWithDependenciesDTO = new EegDatasetWithDependenciesDTO();

        eegDatasetWithDependenciesDTO.setCreationDate(dataset.getCreationDate());
        eegDatasetWithDependenciesDTO.setGroupOfSubjectsId(dataset.getGroupOfSubjectsId());
        eegDatasetWithDependenciesDTO.setId(dataset.getId());
        eegDatasetWithDependenciesDTO.setOriginMetadata(datasetMetadataMapper.datasetMetadataToDatasetMetadataDTO(dataset.getOriginMetadata()));
        eegDatasetWithDependenciesDTO.setStudyId(dataset.getStudyId());
        eegDatasetWithDependenciesDTO.setSubjectId(dataset.getSubjectId());
        eegDatasetWithDependenciesDTO.setUpdatedMetadata(datasetMetadataMapper.datasetMetadataToDatasetMetadataDTO(dataset.getUpdatedMetadata()));
        eegDatasetWithDependenciesDTO.setName(dataset.getName());
        if (dataset.getType() != null) {
            eegDatasetWithDependenciesDTO.setType(dataset.getType().name());
        }
        eegDatasetWithDependenciesDTO.setCenterId(datasetService.getCenterId(dataset));
        eegDatasetWithDependenciesDTO.setInPacs(dataset.getInPacs());
        eegDatasetWithDependenciesDTO.setTags(studyTagMapper.studyTagListToStudyTagDTOLightList(dataset.getTags()));
        eegDatasetWithDependenciesDTO.setSource(datasetMapper.mapSourceFromDataset(dataset.getSource()));
        eegDatasetWithDependenciesDTO.setCopies(datasetMapper.mapCopiesFromDataset(dataset.getCopies()));
        eegDatasetWithDependenciesDTO.setCoordinatesSystem(dataset.getCoordinatesSystem());
        eegDatasetWithDependenciesDTO.setSamplingFrequency(dataset.getSamplingFrequency());
        eegDatasetWithDependenciesDTO.setChannelCount(dataset.getChannelCount());
        List<Channel> list2 = dataset.getChannels();
        if (list2 != null) {
            eegDatasetWithDependenciesDTO.setChannels(new ArrayList<Channel>(list2));
        }
        List<Event> list3 = dataset.getEvents();
        if (list3 != null) {
            eegDatasetWithDependenciesDTO.setEvents(new ArrayList<Event>(list3));
        }
        eegDatasetWithDependenciesDTO.setProcessings(datasetProcessingMapper.datasetProcessingListToDatasetProcessingDTOList(dataset.getProcessings()));
        eegDatasetWithDependenciesDTO.setDatasetAcquisition(datasetAcquisitionMapper.datasetAcquisitionToDatasetAcquisitionDTO(dataset.getDatasetAcquisition()));
        eegDatasetWithDependenciesDTO.setDatasetProcessing(datasetProcessingMapper.datasetProcessingToDatasetProcessingDTO(dataset.getDatasetProcessing()));

        return eegDatasetWithDependenciesDTO;
    }
}

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

package org.shanoir.ng.vip.processingResource.service;

import org.shanoir.ng.dataset.model.Dataset;
import org.shanoir.ng.dataset.service.DatasetService;
import org.shanoir.ng.datasetacquisition.model.DatasetAcquisition;
import org.shanoir.ng.examination.model.Examination;
import org.shanoir.ng.processing.dto.ParameterResourceDTO;
import org.shanoir.ng.shared.exception.EntityNotFoundException;
import org.shanoir.ng.vip.executionMonitoring.model.ExecutionMonitoring;
import org.shanoir.ng.vip.processingResource.model.ProcessingResource;
import org.shanoir.ng.vip.processingResource.repository.ProcessingResourceRepository;
import org.shanoir.ng.vip.shared.dto.DatasetParameterDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class ProcessingResourceServiceImpl implements ProcessingResourceService {

    @Autowired
    private ProcessingResourceRepository repository;

    @Autowired
    private DatasetService datasetService;

    public String create(ExecutionMonitoring processing, List<Dataset> datasets) {
        List<ProcessingResource> processingResources = new ArrayList<>();
        String resourceId = UUID.randomUUID().toString();
        for (Dataset dataset : datasets) {
            processingResources.add(new ProcessingResource(processing, dataset, resourceId));
        }
        repository.saveAll(processingResources);
        return resourceId;
    }

    public List<ParameterResourceDTO> createProcessingResources(ExecutionMonitoring processing, List<DatasetParameterDTO> datasetParameters) throws EntityNotFoundException {

        if (datasetParameters ==  null || datasetParameters.isEmpty()) {
            return new ArrayList<>();
        }

        List<ParameterResourceDTO> resources = new ArrayList<>();

        for (DatasetParameterDTO dto : datasetParameters) {
            ParameterResourceDTO resourceDTO = new ParameterResourceDTO();
            resourceDTO.setParameter(dto.getName());
            resourceDTO.setExportFormat(dto.getExportFormat());
            resourceDTO.setGroupBy(dto.getGroupBy());
            resourceDTO.setResourceIds(new ArrayList<>());

            HashMap<Long, List<Dataset>> datasetsByEntityId = new HashMap<>();

            for (Long id : dto.getDatasetIds()) {
                Dataset ds = datasetService.findById(id);

                Long entityId = null;
                switch (dto.getGroupBy()) {
                    case ACQUISITION:
                        DatasetAcquisition acquisition = datasetService.getAcquisition(ds);
                        if (acquisition != null) {
                            entityId = acquisition.getId();
                        }
                        break;
                    case EXAMINATION:
                        Examination exam = datasetService.getExamination(ds);
                        if (exam != null) {
                            entityId = exam.getId();
                        }
                        break;
                    case STUDY:
                        entityId = datasetService.getStudyId(ds);
                        break;
                    case SUBJECT:
                        if (ds.getSubjectId() != null) {
                            entityId = ds.getSubjectId();
                        }
                        break;
                    case DATASET:
                        entityId = ds.getId();
                        break;
                    default:
                        throw new EntityNotFoundException("Cannot find [" + dto.getGroupBy() + "] entity for dataset [" + ds.getId() + "]");
                }

                datasetsByEntityId.putIfAbsent(entityId, new ArrayList<>());
                datasetsByEntityId.get(entityId).add(ds);
            }

            for (Map.Entry<Long, List<Dataset>> entry : datasetsByEntityId.entrySet()) {
                String resourceId = create(processing, entry.getValue());
                resourceDTO.getResourceIds().add(resourceId);
            }
            resources.add(resourceDTO);
        }
        return resources;
    }
}

package org.shanoir.ng.vip.monitoring.service;

import org.shanoir.ng.dataset.model.Dataset;
import org.shanoir.ng.dataset.service.DatasetService;
import org.shanoir.ng.vip.dto.DatasetParameterDTO;
import org.shanoir.ng.vip.monitoring.model.ExecutionMonitoring;
import org.shanoir.ng.vip.monitoring.model.ExecutionStatus;
import org.shanoir.ng.vip.monitoring.repository.ExecutionMonitoringRepository;
import org.shanoir.ng.vip.monitoring.security.ExecutionMonitoringSecurityService;
import org.shanoir.ng.processing.dto.ParameterResourceDTO;
import org.shanoir.ng.vip.resource.ProcessingResourceService;
import org.shanoir.ng.shared.exception.EntityNotFoundException;
import org.shanoir.ng.utils.Utils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * @author KhalilKes
 */
@Service
public class ExecutionMonitoringServiceImpl implements ExecutionMonitoringService {

    @Autowired
    private ExecutionMonitoringRepository repository;

    @Autowired
    private ProcessingResourceService processingResourceService;

    @Autowired
    private DatasetService datasetService;

    @Autowired
    private ExecutionMonitoringSecurityService executionMonitoringSecurityService;

    private final String RIGHT_STR = "CAN_SEE_ALL";

    private ExecutionMonitoring updateValues(ExecutionMonitoring from, ExecutionMonitoring to) {
        to.setIdentifier(from.getIdentifier());
        to.setStatus(from.getStatus());
        to.setName(from.getName());
        to.setPipelineIdentifier(from.getPipelineIdentifier());
        to.setStartDate(from.getStartDate());
        to.setEndDate(from.getEndDate());
        to.setTimeout(from.getTimeout());
        to.setResultsLocation(from.getResultsLocation());

        to.setDatasetProcessingType(from.getDatasetProcessingType());
        to.setComment(from.getComment());
        to.setInputDatasets(from.getInputDatasets());
        to.setOutputDatasets(from.getOutputDatasets());
        to.setProcessingDate(from.getProcessingDate());
        to.setStudyId(from.getStudyId());

        return to;
    }

    @Override
    public Optional<ExecutionMonitoring> findById(Long id) {
        return repository.findById(id);
    }

    @Override
    public List<ExecutionMonitoring> findAll() {
        return Utils.toList(repository.findAll());
    }

    @Override
    public void deleteById(Long id) throws EntityNotFoundException {
        repository.deleteById(id);
    }

    @Override
    public ExecutionMonitoring create(
            final ExecutionMonitoring executionMonitoring) {
        return repository.save(executionMonitoring);
    }

    @Override
    public Optional<ExecutionMonitoring> findByIdentifier(String identifier) {
        return repository.findByIdentifier(identifier);
    }

    @Override
    public List<ExecutionMonitoring> findAllAllowed() {
        return executionMonitoringSecurityService.filterExecutionMonitoringList(findAll(), RIGHT_STR);
    }

    @Override
    public ExecutionMonitoring update(final ExecutionMonitoring executionMonitoring)
            throws EntityNotFoundException {
        final Optional<ExecutionMonitoring> entityDbOpt = repository
                .findById(executionMonitoring.getId());
        final ExecutionMonitoring entityDb = entityDbOpt.orElseThrow(
                () -> new EntityNotFoundException(executionMonitoring.getClass(),
                        executionMonitoring.getId()));

        this.updateValues(executionMonitoring, entityDb);
        return repository.save(entityDb);

    }

    @Override
    public List<ExecutionMonitoring> findAllRunning() {
        return repository.findByStatus(ExecutionStatus.RUNNING);
    }

    @Override
    public List<ParameterResourceDTO> createProcessingResources(ExecutionMonitoring processing, List<DatasetParameterDTO> datasetParameters) {

        if(datasetParameters ==  null || datasetParameters.isEmpty()){
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
                        if (ds.getDatasetAcquisition() != null) {
                            entityId = ds.getDatasetAcquisition().getId();
                        }
                        break;
                    case EXAMINATION:
                        if (ds.getDatasetAcquisition() != null
                                && ds.getDatasetAcquisition().getExamination() != null) {
                            entityId = ds.getDatasetAcquisition().getExamination().getId();
                        }
                        break;
                    case STUDY:
                        if (ds.getDatasetAcquisition() != null
                                && ds.getDatasetAcquisition().getExamination() != null
                                && ds.getDatasetAcquisition().getExamination().getStudy() != null) {
                            entityId = ds.getDatasetAcquisition().getExamination().getStudy().getId();
                        }
                        break;
                    case SUBJECT:
                        if (ds.getSubjectId() != null) {
                            entityId = ds.getSubjectId();
                        }
                        break;
                    case DATASET:
                        entityId = ds.getId();
                        break;
                }

                if(entityId != null) {
                    datasetsByEntityId.putIfAbsent(entityId, new ArrayList<>());
                    datasetsByEntityId.get(entityId).add(ds);
                }

            }

            for(Map.Entry<Long, List<Dataset>> entry : datasetsByEntityId.entrySet()) {
                String resourceId = processingResourceService.create(processing, entry.getValue());
                resourceDTO.getResourceIds().add(resourceId);
            }
            resources.add(resourceDTO);
        }

        return resources;


    }

}

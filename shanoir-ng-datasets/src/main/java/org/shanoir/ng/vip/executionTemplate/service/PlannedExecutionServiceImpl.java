package org.shanoir.ng.vip.executionTemplate.service;

import org.shanoir.ng.shared.service.TransactionRunner;
import org.shanoir.ng.dataset.model.Dataset;
import org.shanoir.ng.dataset.repository.DatasetRepository;
import org.shanoir.ng.datasetacquisition.model.DatasetAcquisition;
import org.shanoir.ng.datasetacquisition.repository.DatasetAcquisitionRepository;
import org.shanoir.ng.processing.dto.GroupByEnum;
import org.shanoir.ng.shared.core.model.IdName;
import org.shanoir.ng.shared.exception.EntityNotFoundException;
import org.shanoir.ng.shared.exception.RestServiceException;
import org.shanoir.ng.shared.exception.SecurityException;
import org.shanoir.ng.utils.SecurityContextUtil;
import org.shanoir.ng.vip.execution.dto.ExecutionCandidateDTO;
import org.shanoir.ng.vip.execution.service.ExecutionService;
import org.shanoir.ng.vip.executionMonitoring.model.ExecutionStatus;
import org.shanoir.ng.vip.executionMonitoring.service.ExecutionMonitoringService;
import org.shanoir.ng.vip.executionTemplate.model.ExecutionTemplate;
import org.shanoir.ng.vip.executionTemplate.model.ExecutionTemplateParameter;
import org.shanoir.ng.vip.executionTemplate.model.PlannedExecution;
import org.shanoir.ng.vip.executionTemplate.repository.ExecutionTemplateRepository;
import org.shanoir.ng.vip.executionTemplate.repository.PlannedExecutionRepository;
import org.shanoir.ng.vip.shared.dto.DatasetParameterDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
public class PlannedExecutionServiceImpl implements PlannedExecutionService {

    private static final Logger LOG = LoggerFactory.getLogger(PlannedExecutionServiceImpl.class);

    private int MAX_STATUS_RETRIES = 3;
    private long STATUS_SLEEP_SECONDS = 20;
    private int MAX_THREADS = 3;

    @Value("${server.shanoir-hours.shutdown}")
    private int SHUTDOWN_HOUR;

    @Value("${server.shanoir-hours.continuance}")
    private int CONTINUANCE_HOUR;

    @Autowired
    private ExecutionService executionService;

    @Autowired
    private ExecutionMonitoringService executionMonitoringService;

    @Autowired
    private PlannedExecutionRepository plannedExecutionRepository;

    @Autowired
    private ExecutionTemplateRepository executionTemplateRepository;

    @Autowired
    private DatasetRepository datasetRepository;

    @Autowired
    private DatasetAcquisitionRepository acquisitionRepository;

    @Autowired
    private TransactionRunner transactionRunner;

    public void applyExecution(Map<Long, List<Long>> createdAcquisitionsPerTemplateId) {
        LOG.info("Auto executions for newly imported DICOM started.");
        ExecutorService executor = Executors.newFixedThreadPool(MAX_THREADS);

        for (Long templateId : createdAcquisitionsPerTemplateId.keySet()) {
            ExecutionTemplate template = executionTemplateRepository.findById(templateId).orElse(null);
            if(Objects.isNull(template)) {
                createdAcquisitionsPerTemplateId.get(templateId).forEach(acqId -> plannedExecutionRepository.deleteByAcquisitionIdAndTemplateId(templateId, acqId));
            }  else {
                String executionLevel = getExecutionLevel(template);

                switch (executionLevel) {
                    case "examination" ->
                            transactionRunner.runInTransaction(em -> createExecutionAtExaminationLevel(template, createdAcquisitionsPerTemplateId.get(templateId), executor));
                    case "acquisition" ->
                            transactionRunner.runInTransaction(em -> createExecutionsAtAcquisitionLevel(template, createdAcquisitionsPerTemplateId.get(templateId), executor));
                    case "dataset" ->
                            transactionRunner.runInTransaction(em -> createExecutionsAtDatasetLevel(template, createdAcquisitionsPerTemplateId.get(templateId), executor));
                }
            }
        }

        executor.shutdown();
        try {
            executor.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        LOG.info("Auto executions for newly imported DICOM ended.");
    }

    @Transactional
    public void savePlannedExecution(Map<Long, List<Long>> createdAcquisitionsPerTemplatesId) {
        for (Map.Entry<Long, List<Long>> entry : createdAcquisitionsPerTemplatesId.entrySet()) {
            Long templateId = entry.getKey();
            for (Long acquisitionId : entry.getValue()) {
                PlannedExecution plannedExecution = new PlannedExecution(templateId, acquisitionId);
                plannedExecutionRepository.save(plannedExecution);
            }
        }
        LOG.info("Planned executions for newly imported DICOMs saved.");
    }

    /**
     * Thread the creation of monitoring and start of execution for the given template id and acquisition id
     */
    private void threadExecution(ExecutionTemplate template, Long objectId, String executionLevel, Long acquisitionId) {
        SecurityContextUtil.initAuthenticationContext("ROLE_ADMIN");

        try {
            ExecutionCandidateDTO candidate = prepareExecutionCandidate(template, executionLevel, objectId);

            if(Objects.nonNull(candidate)) {
                IdName monitoringIdName = executionService.createExecution(candidate, datasetRepository.findByIdIn(candidate.getDatasetParameters().stream().map(DatasetParameterDTO::getDatasetIds).flatMap(List::stream).collect(Collectors.toList())));
                String vipIdentifier = executionMonitoringService.getVipIdentifierFromMonitoringId(monitoringIdName.getId());

                ExecutionStatus status = ExecutionStatus.RUNNING;

                while (Objects.equals(status, ExecutionStatus.RUNNING)) {
                    TimeUnit.SECONDS.sleep(STATUS_SLEEP_SECONDS);

                    for (int attempt = 1; attempt <= MAX_STATUS_RETRIES; attempt++) {
                        try {
                            status = executionService.getExecutionStatusFromVipIdentifier(vipIdentifier);
                            break;
                        } catch (Exception e) {
                            if (attempt == MAX_STATUS_RETRIES){
                                break;
                            }
                            TimeUnit.SECONDS.sleep(1);
                        }
                    }

                    plannedExecutionRepository.deleteByAcquisitionIdAndTemplateId(acquisitionId, template.getId());
                }
            }
        } catch (RestServiceException | SecurityException | EntityNotFoundException | InterruptedException e) {
            LOG.error("Execution from template {} for {} {} failed.", template.getId(), executionLevel, objectId, e);
        }
    }

    /**
     * Create execution when it's one execution for all the newly imported data
     */
    public void createExecutionAtExaminationLevel(ExecutionTemplate template, List<Long> acquisitionIds, ExecutorService executor) {
        pauseIfInSleepWindow();

        int attempt = 0;
        DatasetAcquisition acquisition = acquisitionRepository.findById(acquisitionIds.getFirst()).orElse(null);
        while(Objects.isNull(acquisition) && attempt < 5) {
            try {
                Thread.sleep(1000);
            } catch (Exception ignored) {}
            attempt++;
            acquisition = acquisitionRepository.findById(acquisitionIds.getFirst()).orElse(null);
        }

        if(Objects.isNull(acquisition)) {
            LOG.error("Did not achieve to get newly imported acquisitions for auto execution with template {}. Giving up.", template.getId());
        } else {
            Long examinationId = acquisition.getExamination().getId();
            DatasetAcquisition finalAcquisition = acquisition;
            executor.submit(() -> transactionRunner.runInTransaction(em -> threadExecution(template, examinationId, "examination", finalAcquisition.getId())));
        }
    }

    /**
     * Create executions when it's one execution per newly acquisition
     */
    public void createExecutionsAtAcquisitionLevel(ExecutionTemplate template, List<Long> acquisitionIds, ExecutorService executor) {
        for (Long acquisitionId : acquisitionIds) {
            pauseIfInSleepWindow();

            executor.submit(() -> transactionRunner.runInTransaction(em -> threadExecution(template, acquisitionId, "acquisition",  acquisitionId)));
            try {
                Thread.sleep(1000); // Delay between submissions, VIP needs it
            } catch (InterruptedException e) {
                LOG.error("Issue while serializing executions with template {}. Stopping session.", template.getId());
                break;
            }
        }
    }

    /**
     * Create executions when it's one execution per newly dataset
     */
    public void createExecutionsAtDatasetLevel(ExecutionTemplate template, List<Long> acquisitionIds, ExecutorService executor) {
        int attempt = 0;
        List<Long> datasetIds = datasetRepository.findFilteredIdsByDatasetAcquisitionIdIn(acquisitionIds, "%");
        while(datasetIds.isEmpty() && attempt < 5) {
            try {
                Thread.sleep(1000);
            } catch (Exception ignored) {}
            attempt++;
            datasetIds = datasetRepository.findFilteredIdsByDatasetAcquisitionIdIn(acquisitionIds, "%");
        }

        for (Long acquisitionId : acquisitionIds) {
            DatasetAcquisition acquisition = acquisitionRepository.findById(acquisitionId).orElse(null);
            while(Objects.isNull(acquisition) && attempt < 5) {
                try {
                    Thread.sleep(1000);
                } catch (Exception ignored) {}
                attempt++;
                acquisition = acquisitionRepository.findById(acquisitionIds.getFirst()).orElse(null);
            }

            if(Objects.nonNull(acquisition)){
                for(Dataset dataset : acquisition.getDatasets()) {
                    pauseIfInSleepWindow();

                    executor.submit(() -> transactionRunner.runInTransaction(em -> threadExecution(template, dataset.getId(), "dataset", acquisitionId)));
                    try {
                        Thread.sleep(1000); // Delay between submissions, VIP needs it
                    } catch (InterruptedException e) {
                        LOG.error("Issue while serializing executions with template {}. Stopping session.", template.getId());
                        break;
                    }
                }
            } else {
                LOG.error("Did not achieve to get newly imported acquisitions for auto execution with template {}. Giving up.", template.getId());
            }
        }
    }

    /**
     * Prepare the execution candidate DTO relatively to the given templateId and acquisitionId
     */
    private ExecutionCandidateDTO prepareExecutionCandidate(ExecutionTemplate template, String executionLevel, Long objectId) {
        Long studyId = template.getStudy().getId();
        Long examId = null;
        switch (executionLevel) {
            case "examination" -> examId = objectId;
            case "acquisition" -> {
                DatasetAcquisition acquisition = acquisitionRepository.findById(objectId).orElse(null);
                if(Objects.nonNull(acquisition)) {
                    examId = acquisition.getExamination().getId();
                }
            }
            case "dataset" -> {
                Dataset dataset = datasetRepository.findById(objectId).orElse(null);
                if(Objects.nonNull(dataset)) {
                    examId = dataset.getDatasetAcquisition().getExamination().getId();
                }
            }
        }

        ExecutionCandidateDTO candidate = new ExecutionCandidateDTO();
        candidate.setInputParameters(new HashMap<>());
        candidate.setClient("shanoir-uploader");
        candidate.setName(template.getParameters().stream().filter(parameter -> Objects.equals(parameter.getName(), "execution_name")).findFirst().get().getValue());
        candidate.setName(candidate.getName() + "_" + (Objects.isNull(examId) ? "" : examId + "_") + LocalDate.now(ZoneOffset.UTC).format(DateTimeFormatter.ISO_DATE));
        candidate.setConverterId(Long.valueOf(template.getParameters().stream().filter(parameter -> Objects.equals(parameter.getName(), "converter")).findFirst().get().getValue()));
        candidate.setPipelineIdentifier(template.getParameters().stream().filter(parameter -> Objects.equals(parameter.getName(), "pipeline_identifier")).findFirst().get().getValue());
        candidate.setProcessingType(template.getParameters().stream().filter(parameter -> Objects.equals(parameter.getName(), "processing_type")).findFirst().get().getValue());
        candidate.setStudyIdentifier(studyId);

        candidate.setDatasetParameters(prepareDatasetParameters(template, objectId));
        return Objects.isNull(candidate.getDatasetParameters()) ? null : candidate;
    }
    
    /**
     * Prepare the execution candidate DTO dataset parameters
     */
    private List<DatasetParameterDTO> prepareDatasetParameters(ExecutionTemplate template, Long objectId) {
        List<String> prefixes = template.getParameters().stream().filter(parameter -> parameter.getName().endsWith("_group")).map(parameter -> parameter.getName().substring(0, parameter.getName().indexOf("_group"))).toList();
        List<DatasetParameterDTO> datasetParameters = new ArrayList<>();

        for(String prefixe: prefixes) {
            String parameterGroup = template.getParameters().stream().filter(parameter -> Objects.equals(parameter.getName(), prefixe + "_group")).findFirst().get().getValue();
            String parameterValue = template.getParameters().stream().filter(parameter -> Objects.equals(parameter.getName(), prefixe)).findFirst().get().getValue();
            List<Long> inputIds = new ArrayList<>();

            switch (parameterGroup) {
                case "examination" -> {
                    inputIds = datasetRepository.findFilteredIdsByDatasetAcquisitionIdIn(acquisitionRepository.findIdsByExaminationId(objectId), parameterValue);
                }
                case "acquisition" -> {
                    inputIds = datasetRepository.findFilteredIdsByDatasetAcquisitionId(objectId, parameterValue);
                }
                case "dataset" -> {
                    Dataset dataset = datasetRepository.findById(objectId).get();
                    if(dataset.getName().contains(parameterValue)){
                        inputIds.add(dataset.getId());
                    }
                    return null;
                }
            }
            if(!inputIds.isEmpty()) {
                DatasetParameterDTO datasetParameterDTO = new DatasetParameterDTO();
                datasetParameterDTO.setDatasetIds(inputIds);
                datasetParameterDTO.setName(prefixe);
                datasetParameterDTO.setConverterId(Long.valueOf(template.getParameters().stream().filter(parameter -> Objects.equals(parameter.getName(), "converter")).findFirst().get().getValue()));
                datasetParameterDTO.setExportFormat(template.getParameters().stream().filter(parameter -> Objects.equals(parameter.getName(), "export_format")).findFirst().get().getValue());
                datasetParameterDTO.setGroupBy(GroupByEnum.valueOf(parameterGroup.toUpperCase()));
                datasetParameters.add(datasetParameterDTO);
            }
        }
        return datasetParameters;
    }

    /**
     * Get the specific parameters of the pipeline relative to the id given as parameter
     */
    private List<ExecutionTemplateParameter> getSpecificPipelineParameter(Long templateId){
        ExecutionTemplate template = executionTemplateRepository.findById(templateId).get();
        List<String> specificPrefixe = template.getParameters().stream().filter(parameter -> parameter.getName().endsWith("_group")).map(parameter -> parameter.getName().substring(0, parameter.getName().indexOf("_group"))).toList();
        return template.getParameters().stream().filter(parameter -> specificPrefixe.stream().anyMatch(parameter.getName()::startsWith)).toList();
    }

    /**
     * Stop the threading for avoiding side effect at instance shutdown
     */
    private void pauseIfInSleepWindow() {
        LocalTime now = LocalTime.now();
        LocalTime start = LocalTime.of(SHUTDOWN_HOUR, 0);
        LocalTime end = LocalTime.of(CONTINUANCE_HOUR, 0);

        if (!now.isBefore(start) && now.isBefore(end)) {
            Duration pause = Duration.between(now, end);
            LOG.info("Execution paused until {} AM.", CONTINUANCE_HOUR);
            try {
                Thread.sleep(pause.toMillis());
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }

    /**
     * Get the lower level of group for specific parameters. It will decide if it's one exec per (new) exam / per new acquisition / per new dataset
     */
    private String getExecutionLevel(ExecutionTemplate template) {
        List<String> distinctsGroup  = template.getParameters().stream().filter(parameter -> parameter.getName().endsWith("_group")).map(ExecutionTemplateParameter::getValue).toList();
        return Stream.of("dataset", "acquisition")
                .filter(distinctsGroup::contains)
                .findFirst()
                .orElse("examination");
    }
}

package org.shanoir.ng.vip.executionTemplate.service;

import org.shanoir.ng.dataset.model.Dataset;
import org.shanoir.ng.dataset.repository.DatasetRepository;
import org.shanoir.ng.shared.core.model.IdName;
import org.shanoir.ng.shared.exception.EntityNotFoundException;
import org.shanoir.ng.shared.exception.RestServiceException;
import org.shanoir.ng.shared.exception.SecurityException;
import org.shanoir.ng.vip.execution.dto.ExecutionCandidateDTO;
import org.shanoir.ng.vip.execution.service.ExecutionService;
import org.shanoir.ng.vip.executionMonitoring.model.ExecutionStatus;
import org.shanoir.ng.vip.executionMonitoring.service.ExecutionMonitoringService;
import org.shanoir.ng.vip.executionTemplate.model.ExecutionTemplate;
import org.shanoir.ng.vip.executionTemplate.model.ExecutionTemplateParameter;
import org.shanoir.ng.vip.executionTemplate.model.PlannedExecution;
import org.shanoir.ng.vip.executionTemplate.repository.ExecutionTemplateRepository;
import org.shanoir.ng.vip.executionTemplate.repository.PlannedExecutionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
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
import java.util.stream.StreamSupport;

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


    public void applyExecution(Map<Long, List<Long>> createdAcquisitionsPerTemplateId) {
        LOG.info("Auto executions for newly imported DICOM started.");
        ExecutorService executor = Executors.newFixedThreadPool(MAX_THREADS);

        for (Map.Entry<Long, List<Long>> entry : createdAcquisitionsPerTemplateId.entrySet()) {
            Long templateId = entry.getKey();
            for (Long acquisitionId : entry.getValue()) {
                pauseIfInSleepWindow();

                executor.submit(() -> threadExecution(templateId, acquisitionId));
                try {
                    Thread.sleep(1000); // Delay between submissions, VIP needs it
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        }

        executor.shutdown();
        try {
            executor.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
        } catch (
                InterruptedException e) {
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
    private void threadExecution(Long templateId, Long acquisitionId) {
        try {
            List<Dataset> inputDatasets = prepareInputDatasets(templateId, acquisitionId);
            if(inputDatasets.size() > 0) {
                ExecutionCandidateDTO candidate = prepareExecutionCandidate(templateId, inputDatasets);

                IdName monitoringIdName = executionService.createExecution(candidate, inputDatasets);
                String vipIdentifier = executionMonitoringService.getVipIdentifierFromMonitoringId(monitoringIdName.getId());

                ExecutionStatus status = ExecutionStatus.RUNNING;
                int countDown = 12;

                while (Objects.equals(status, ExecutionStatus.RUNNING)) {
                    TimeUnit.SECONDS.sleep(STATUS_SLEEP_SECONDS);

                    for (int attempt = 1; attempt <= MAX_STATUS_RETRIES; attempt++) {
                        try {
                            status = executionService.getExecutionStatusFromVipIdentifier(vipIdentifier);
                            break;
                        } catch (Exception e) {
                            LOG.warn("Attempt {}/{} failed to get status for monitoring {}, vipIdentifier {}", attempt, MAX_STATUS_RETRIES, monitoringIdName.getId(), vipIdentifier);
                            if (attempt == MAX_STATUS_RETRIES) {
                                LOG.error("Vip execution {} lost, monitoring {} deleted. Execution will be retried at next instance deployement.", vipIdentifier, monitoringIdName.getId());
                            }
                            TimeUnit.SECONDS.sleep(1);
                        }
                    }

                    countDown--;
                    if (countDown == 1 && Objects.equals(status, ExecutionStatus.RUNNING)) {
                        LOG.info("Status for vip execution {}, monitoring {} is {}", vipIdentifier, monitoringIdName.getId(), status);
                        countDown = 12;
                    }
                }
                plannedExecutionRepository.deleteByAcquisitionIdAndTemplateId(acquisitionId, templateId);
            }
        } catch (RestServiceException | SecurityException | EntityNotFoundException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Prepare the input dataset list relatively to the given templateId and acquisitionId
     */
    private List<Dataset> prepareInputDatasets(Long templateId, Long acquisitionId) {
        List<Dataset> inputDatasets = StreamSupport.stream(datasetRepository.findByDatasetAcquisitionId(acquisitionId).spliterator(), false).toList();
        List<ExecutionTemplateParameter> specificTemplateParameters = getSpecificPipelineParameter(templateId);
        inputDatasets = inputDatasets.stream().filter(dataset -> specificTemplateParameters.stream().anyMatch(parameter -> {if(Objects.nonNull(parameter.getValue()) && !Objects.equals(parameter.getValue(), "")) { return dataset.getName().contains(parameter.getValue());} else { return true;}})).toList();
        return inputDatasets;
    }

    /**
     * Prepare the execution candidate DTO relatively to the given templateId and acquisitionId
     */
    private ExecutionCandidateDTO prepareExecutionCandidate(Long templateId, List<Dataset> inputDatasets) {
        ExecutionTemplate template = executionTemplateRepository.findById(templateId).get();
        Long examId = inputDatasets.getFirst().getDatasetAcquisition().getExamination().getId();
        Long studyId = inputDatasets.getFirst().getStudyId();

        ExecutionCandidateDTO candidate = new ExecutionCandidateDTO();
        candidate.setInputParameters(new HashMap<>());
        candidate.setClient("shanoir-uploader");
        candidate.setName(template.getParameters().stream().filter(parameter -> Objects.equals(parameter.getName(), "execution_name")).findFirst().get().getValue());
        candidate.setName(candidate.getName() + "_" + examId + "_" + LocalDate.now(ZoneOffset.UTC).format(DateTimeFormatter.ISO_DATE));
        candidate.setConverterId(Long.valueOf(template.getParameters().stream().filter(parameter -> Objects.equals(parameter.getName(), "converter")).findFirst().get().getValue()));
        candidate.setPipelineIdentifier(template.getParameters().stream().filter(parameter -> Objects.equals(parameter.getName(), "pipeline_identifier")).findFirst().get().getValue());
        candidate.setProcessingType(template.getParameters().stream().filter(parameter -> Objects.equals(parameter.getName(), "processing_type")).findFirst().get().getValue());
        candidate.setStudyIdentifier(studyId);
        return candidate;
    }

    /**
     * Get the specific parameters of the pipeline relative to the id given as parameter
     */
    private List<ExecutionTemplateParameter> getSpecificPipelineParameter(Long templateId){
        ExecutionTemplate template = executionTemplateRepository.findById(templateId).get();
        return template.getParameters().stream().filter(parameter -> !List.of("execution_name", "export_format", "group_by", "converter", "pipeline_identifier","processing_type").contains(parameter.getName())).toList();
    }

    /**
     * STop the threading for avoiding side effect at instance shutdown
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
}

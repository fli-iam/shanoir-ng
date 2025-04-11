package org.shanoir.ng.vip.executionMonitoring.service;

import org.shanoir.ng.dataset.model.Dataset;
import org.shanoir.ng.processing.model.DatasetProcessingType;
import org.shanoir.ng.processing.service.DatasetProcessingService;
import org.shanoir.ng.shared.event.ShanoirEvent;
import org.shanoir.ng.shared.event.ShanoirEventService;
import org.shanoir.ng.shared.event.ShanoirEventType;
import org.shanoir.ng.shared.exception.RestServiceException;
import org.shanoir.ng.utils.KeycloakUtil;
import org.shanoir.ng.vip.execution.dto.ExecutionCandidateDTO;
import org.shanoir.ng.vip.execution.dto.VipExecutionDTO;
import org.shanoir.ng.vip.execution.service.ExecutionServiceImpl;
import org.shanoir.ng.vip.executionMonitoring.model.ExecutionMonitoring;
import org.shanoir.ng.vip.executionMonitoring.model.ExecutionStatus;
import org.shanoir.ng.vip.executionMonitoring.repository.ExecutionMonitoringRepository;
import org.shanoir.ng.vip.executionMonitoring.security.ExecutionMonitoringSecurityService;
import org.shanoir.ng.shared.exception.EntityNotFoundException;
import org.shanoir.ng.utils.Utils;
import org.shanoir.ng.vip.output.exception.ResultHandlerException;
import org.shanoir.ng.vip.output.service.OutputService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.Exceptions;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;

import static org.shanoir.ng.vip.executionMonitoring.model.ExecutionStatus.RUNNING;


/**
 * @author KhalilKes
 */
@Service
@EnableAsync
public class ExecutionMonitoringServiceImpl implements ExecutionMonitoringService {

    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS");
    public static final float DEFAULT_PROGRESS = 0.5f;
    @Value("${vip.sleep-time}")
    private long sleepTime;
    private static final Logger LOG = LoggerFactory.getLogger(ExecutionMonitoringServiceImpl.class);
    private final String RIGHT_STR = "CAN_SEE_ALL";
    private final ConcurrentLinkedQueue<Map<String, Object>> monitoringQueue = new ConcurrentLinkedQueue<>();
    private volatile boolean isRunning = false;

    @Autowired
    private ExecutionMonitoringRepository repository;

    @Autowired
    private ExecutionMonitoringSecurityService executionMonitoringSecurityService;

    @Autowired
    private DatasetProcessingService datasetProcessingService;

    @Autowired
    private ShanoirEventService eventService;

    @Autowired
    private ExecutionServiceImpl executionService;

    @Autowired
    private OutputService outputService;

    @Autowired
    @Lazy
    private ExecutionMonitoringServiceImpl emProxyService;

    public ExecutionMonitoring createExecutionMonitoring(ExecutionCandidateDTO execution, List<Dataset> inputDatasets) throws RestServiceException {
        ExecutionMonitoring executionMonitoring = new ExecutionMonitoring();
        executionMonitoring.setName(execution.getName());
        executionMonitoring.setPipelineIdentifier(execution.getPipelineIdentifier());
        executionMonitoring.setResultsLocation(KeycloakUtil.getTokenUserId() + "/" + formatter.format(LocalDateTime.now()));
        executionMonitoring.setTimeout(20);
        executionMonitoring.setStudyId(execution.getStudyIdentifier());
        executionMonitoring.setStatus(RUNNING);
        executionMonitoring.setComment(execution.getName());
        executionMonitoring.setDatasetProcessingType(DatasetProcessingType.valueOf(execution.getProcessingType()));
        executionMonitoring.setOutputProcessing(execution.getOutputProcessing());
        executionMonitoring.setInputDatasets(inputDatasets);
        executionMonitoring.setUsername(KeycloakUtil.getTokenUserName());
        datasetProcessingService.validateDatasetProcessing(executionMonitoring);
        return repository.save(executionMonitoring);
    }

    public ExecutionMonitoring update(final ExecutionMonitoring executionMonitoring) throws EntityNotFoundException {
        final Optional<ExecutionMonitoring> entityDbOpt = repository
                .findById(executionMonitoring.getId());
        final ExecutionMonitoring entityDb = entityDbOpt.orElseThrow(
                () -> new EntityNotFoundException(executionMonitoring.getClass(),
                        executionMonitoring.getId()));

        updateValues(executionMonitoring, entityDb);
        return repository.save(entityDb);
    }


    public List<ExecutionMonitoring> findAllAllowed() {
        return executionMonitoringSecurityService.filterExecutionMonitoringList(Utils.toList(repository.findAll()), RIGHT_STR);
    }

    public void startMonitoringJob(ExecutionMonitoring createdMonitoring, ShanoirEvent event) {
        Map<String, Object> monitoringMap = new HashMap<>();
        monitoringMap.put("monitoring", createdMonitoring);
        monitoringMap.put("event", event);
        monitoringMap.put("attempt", 1);
        monitoringQueue.add(monitoringMap);

        if (!isRunning) { //If we remove this line, each calling thread needs to wait the old ones to finish the synchronized block below before resuming the code execution. It's only for code performance.
            synchronized (this) { //Allow the synchronized block to be executed only by one thread at a time. It avoids concurrency
                if (!isRunning) {  //In case of two calling threads hitting the 1st !isRunning check condition at the same time, it may leads to 2 distinct monitoring loop if we remove this second !isRunning check, what we want to avoid
                    isRunning = true;
                    emProxyService.monitoringLoop();
                }
            }
        }
    }

    @Async //We keep that method async, because the startMonitoringJob ensure that only one thread of this method can exist at a time, and it doesn't block the 1st calling method
    protected void monitoringLoop() {
        while (!monitoringQueue.isEmpty()) {
            long startTime = System.currentTimeMillis();

            for (Map<String, Object> emMap : monitoringQueue) {
                ExecutionMonitoring monitoring = (ExecutionMonitoring) emMap.get("monitoring");
                ShanoirEvent event = (ShanoirEvent) emMap.get("event");
                int attempt = (Integer) emMap.get("attempt");
                String execLabel = getExecLabel(monitoring);

                if(Objects.isNull(event) || !Objects.equals(event.getStatus(),ShanoirEvent.IN_PROGRESS)){
                    emMap.put("event", initShanoirEvent(monitoring, event, execLabel));
                    LOG.info("Monitoring of execution id: " + monitoring.getId() + ", identifier: " + monitoring.getPipelineIdentifier() + ", name: " + monitoring.getName() + " started");
                }

                try{
                    VipExecutionDTO dto = executionService.getExecutionAsServiceAccount(attempt, monitoring.getIdentifier()).block();

                    if(dto == null){
                        emMap.put("attempt", (Integer) emMap.get("attempt") + 1);
                        continue;
                    }else{
                        emMap.put("attempt", 1);
                    }

                    switch (dto.getStatus()) {
                        case FINISHED -> emProxyService.processFinishedJob(monitoring, event, dto.getEndDate());
                        case UNKNOWN,EXECUTION_FAILED,KILLED -> emProxyService.processKilledJob(monitoring, event, dto);
                    }
                    if(!Objects.equals(dto.getStatus(), RUNNING)){
                        monitoringQueue.remove(emMap);
                    }
                    Thread.sleep(10000);
                } catch (Exception e){
                    // Unwrap ReactiveException thrown from async method
                    Throwable ex = Exceptions.unwrap(e);
                    LOG.error(ex.getMessage(), ex.getCause());
                    setEventInError(event, execLabel + " : " + ex.getMessage());
                    LOG.warn("Error while monitoring the processing {}. Stopping the monitoring ...", monitoring.getId());
                }
            }

            while (System.currentTimeMillis() - startTime < sleepTime) {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    LOG.error("Error in the monitoring loop", e);
                }
            }
        }
        isRunning = false;
    }

    /**
     * Update values of an existing execution monitoring. No saving.
     */
    private void updateValues(ExecutionMonitoring from, ExecutionMonitoring to) {
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
    }

    /**
     * Create or update Shanoir event relative to an execution monitoring
     */
    private ShanoirEvent initShanoirEvent(ExecutionMonitoring processing, ShanoirEvent event, String execLabel) {
        String startMsg = execLabel + " : " + RUNNING.getRestLabel();

        if(event == null){
            event = new ShanoirEvent(
                    ShanoirEventType.EXECUTION_MONITORING_EVENT,
                    processing.getId().toString(),
                    KeycloakUtil.getTokenUserId(),
                    startMsg,
                    ShanoirEvent.IN_PROGRESS,
                    DEFAULT_PROGRESS);
        }else{
            event.setMessage(startMsg);
            event.setStatus(ShanoirEvent.IN_PROGRESS);
            event.setProgress(DEFAULT_PROGRESS);
        }
        eventService.publishEvent(event);
        return event;
    }

    /**
     * Manage execution monitoring with a non successfull status
     */
    @Transactional
    protected void processKilledJob(ExecutionMonitoring execution, ShanoirEvent event, VipExecutionDTO vipExecutionDTO) throws EntityNotFoundException {
        String execLabel = getExecLabel(execution);

        LOG.info("Execution id: {}, identifier: {}, name: {} status is [{}]", execution.getId(), execution.getPipelineIdentifier(), execution.getName(), vipExecutionDTO.getStatus().getRestLabel());

        execution.setStatus(vipExecutionDTO.getStatus());
        update(execution);

        LOG.info("Execution status updated, stopping job...");

        setEventInError(event, execLabel + " : "  + vipExecutionDTO.getStatus().getRestLabel()
                + (vipExecutionDTO.getErrorCode() != null ? " (Error code : " + vipExecutionDTO.getErrorCode() + ")" : ""));
    }

    /**
     * Manage execution monitoring with a successfull status
     */
    @Transactional
    protected void processFinishedJob(ExecutionMonitoring execution, ShanoirEvent event, Long endDate) throws EntityNotFoundException, ResultHandlerException {

        String execLabel = getExecLabel(execution);
        execution.setStatus(ExecutionStatus.FINISHED);
        execution.setEndDate(endDate);
        execution.setProcessingDate(LocalDate.now());

        update(execution);

        LOG.info("Execution id: {}, identifier: {}, name: {} status is [{}]", execution.getId(), execution.getPipelineIdentifier(), execution.getName(), ExecutionStatus.FINISHED.getRestLabel());
        event.setMessage(execLabel + " : Finished. Processing imported results...");
        eventService.publishEvent(event);

        outputService.process(execution);

        LOG.info("Execution status updated, stopping job...");

        event.setMessage(execLabel + " : Finished");
        event.setStatus(ShanoirEvent.SUCCESS);
        event.setProgress(1f);
        eventService.publishEvent(event);
    }

    /**
     * Return the label of ane xecution monitoring
     */
    private String getExecLabel(ExecutionMonitoring processing) {
        return "VIP Execution [" + processing.getName() + "]";
    }

    /**
     * Set the shanoir execution monitoring event in error status
     */
    private void setEventInError(ShanoirEvent event, String msg){
        event.setMessage(msg);
        event.setStatus(ShanoirEvent.ERROR);
        event.setProgress(1f);
        eventService.publishEvent(event);
    }
}

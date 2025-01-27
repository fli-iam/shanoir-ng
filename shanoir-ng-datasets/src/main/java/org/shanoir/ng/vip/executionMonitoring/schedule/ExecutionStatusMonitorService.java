package org.shanoir.ng.vip.executionMonitoring.schedule;

import java.time.LocalDate;

import org.shanoir.ng.shared.event.ShanoirEvent;
import org.shanoir.ng.shared.event.ShanoirEventService;
import org.shanoir.ng.shared.event.ShanoirEventType;
import org.shanoir.ng.shared.exception.EntityNotFoundException;
import org.shanoir.ng.shared.exception.SecurityException;
import org.shanoir.ng.utils.KeycloakUtil;
import org.shanoir.ng.vip.pipeline.service.VipClientService;
import org.shanoir.ng.vip.execution.dto.VipExecutionDTO;
import org.shanoir.ng.vip.executionMonitoring.model.ExecutionMonitoring;
import org.shanoir.ng.vip.executionMonitoring.model.ExecutionStatus;
import org.shanoir.ng.vip.executionMonitoring.service.ExecutionMonitoringService;
import org.shanoir.ng.vip.result.exception.ResultHandlerException;
import org.shanoir.ng.vip.result.service.ResultHandlerServiceImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.Exceptions;

/**
 * CRON job to request VIP api and create processedDataset
 * 
 * @author KhalilKes
 */
@Service
public class ExecutionStatusMonitorService {

	public static final float DEFAULT_PROGRESS = 0.5f;

	@Value("${vip.sleep-time}")
	private long sleepTime;

	private ThreadLocal<Boolean> stop = new ThreadLocal<>();

	private String identifier;

	private static final Logger LOG = LoggerFactory.getLogger(ExecutionStatusMonitorService.class);

	@Autowired
	private ExecutionMonitoringService executionMonitoringService;

	@Autowired
	private ShanoirEventService eventService;

	@Autowired
	private ResultHandlerServiceImpl outputProcessingService;

	@Autowired
	private VipClientService vipClient;

	/**
	 * Async job that monitor the state of the VIP execution and process its outcome
	 *
	 * @param processing
	 * @param event
	 *
	 * @throws EntityNotFoundException
	 * @throws SecurityException
	 */

	@Async
	@Transactional
	public void startMonitoringJob(ExecutionMonitoring processing, ShanoirEvent event) throws EntityNotFoundException, SecurityException {
		int attempts = 1;
		this.identifier = processing.getIdentifier();

		stop.set(false);

		String execLabel = this.getExecLabel(processing);
		event = this.initShanoirEvent(processing, event, execLabel);

		while (!stop.get()) {

			try{

				VipExecutionDTO dto = vipClient.getExecutionAsServiceAccount(attempts, this.identifier).block();

				if(dto == null){
					attempts++;
					continue;
				}else{
					attempts = 1;
				}

				switch (dto.getStatus()) {
					case FINISHED:

						this.processFinishedJob(processing, event, dto.getEndDate());
						break;

					case UNKNOWN:
					case EXECUTION_FAILED:
					case KILLED:

						this.processKilledJob(processing, event, dto);
						break;

					case RUNNING:
						try{
							Thread.sleep(sleepTime); // sleep/stop a thread for 20 seconds
						} catch (InterruptedException e) {
							event.setMessage(execLabel + " : Monitoring interrupted, current state unknown...");
							eventService.publishEvent(event);
							LOG.warn("Execution monitoring thread interrupted", e);
						}
						break;
					default:
						stop.set(true);
						break;
				}
			} catch (Exception e){
				// Unwrap ReactiveException thrown from async method
				Throwable ex = Exceptions.unwrap(e);
				LOG.error(ex.getMessage(), ex.getCause());
				this.setJobInError(event, execLabel + " : " + ex.getMessage());
				LOG.warn("Stopping thread...");
				stop.set(true);
			}
		}
	}

	private ShanoirEvent initShanoirEvent(ExecutionMonitoring processing, ShanoirEvent event, String execLabel) {
		String startMsg = execLabel + " : " + ExecutionStatus.RUNNING.getRestLabel();

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

	private String getExecLabel(ExecutionMonitoring processing) {
		return "VIP Execution [" + processing.getName() + "]";
	}

	public void processKilledJob(ExecutionMonitoring processing, ShanoirEvent event, VipExecutionDTO vipExecutionDTO) throws EntityNotFoundException {

		String execLabel = this.getExecLabel(processing);

		LOG.warn("{} status is [{}]", execLabel, vipExecutionDTO.getStatus().getRestLabel());

		processing.setStatus(vipExecutionDTO.getStatus());
		this.executionMonitoringService.update(processing);

		LOG.info("Execution status updated, stopping job...");

		stop.set(true);

		this.setJobInError(event, execLabel + " : "  + vipExecutionDTO.getStatus().getRestLabel()
				+ (vipExecutionDTO.getErrorCode() != null ? " (Error code : " + vipExecutionDTO.getErrorCode() + ")" : ""));
	}

	public void processFinishedJob(ExecutionMonitoring execution, ShanoirEvent event, Long endDate) throws EntityNotFoundException, ResultHandlerException {

		String execLabel = this.getExecLabel(execution);
		execution.setStatus(ExecutionStatus.FINISHED);
		execution.setEndDate(endDate);
		execution.setProcessingDate(LocalDate.now());

		this.executionMonitoringService.update(execution);

		LOG.info("{} status is [{}]", execLabel, ExecutionStatus.FINISHED.getRestLabel());
		event.setMessage(execLabel + " : Finished. Processing imported results...");
		eventService.publishEvent(event);

		this.outputProcessingService.process(execution);

		LOG.info("Execution status updated, stopping job...");

		stop.set(true);

		event.setMessage(execLabel + " : Finished");
		event.setStatus(ShanoirEvent.SUCCESS);
		event.setProgress(1f);
		eventService.publishEvent(event);
	}

	private void setJobInError(ShanoirEvent event, String msg){
		event.setMessage(msg);
		event.setStatus(ShanoirEvent.ERROR);
		event.setProgress(1f);
		eventService.publishEvent(event);
	}
}

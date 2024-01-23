package org.shanoir.ng.vip.monitoring.schedule;

import org.keycloak.representations.AccessTokenResponse;
import org.shanoir.ng.vip.monitoring.model.ExecutionMonitoring;
import org.shanoir.ng.vip.monitoring.model.Execution;
import org.shanoir.ng.vip.monitoring.model.ExecutionStatus;
import org.shanoir.ng.vip.resulthandler.ResultHandlerException;
import org.shanoir.ng.vip.resulthandler.ResultHandlerService;
import org.shanoir.ng.vip.monitoring.service.ExecutionMonitoringService;
import org.shanoir.ng.shared.event.ShanoirEvent;
import org.shanoir.ng.shared.event.ShanoirEventService;
import org.shanoir.ng.shared.event.ShanoirEventType;
import org.shanoir.ng.shared.exception.EntityNotFoundException;
import org.shanoir.ng.shared.exception.SecurityException;
import org.shanoir.ng.shared.security.KeycloakServiceAccountUtils;
import org.shanoir.ng.utils.KeycloakUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDate;

/**
 * CRON job to request VIP api and create processedDataset
 * 
 * @author KhalilKes
 */
@Service
public class ExecutionStatusMonitorService {

	public static final float DEFAULT_PROGRESS = 0.5f;
	@Value("${vip.uri}")
	private String VIP_URI;

	@Value("${vip.upload-folder}")
	private String importDir;

	@Value("${vip.sleep-time}")
	private long sleepTime;

	private ThreadLocal<Boolean> stop = new ThreadLocal<>();

	private String identifier;

	private static final Logger LOG = LoggerFactory.getLogger(ExecutionStatusMonitorService.class);

	@Autowired
	private ExecutionMonitoringService executionMonitoringService;

	@Autowired
	private KeycloakServiceAccountUtils keycloakServiceAccountUtils;

	@Autowired
	private ShanoirEventService eventService;

	@Autowired
	private ResultHandlerService outputProcessingService;

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

		String uri = VIP_URI + identifier + "/summary";
		RestTemplate restTemplate = new RestTemplate();

		// refresh the token
		String token = this.refreshServiceAccountAccessToken();

		String execLabel = this.getExecLabel(processing);
		event = this.initShanoirEvent(processing, event, execLabel);

		while (!stop.get()) {

			try{

				Execution execution = this.getExecutionFromVIP(token, attempts, restTemplate, uri);

				if(execution == null){
					token = this.refreshServiceAccountAccessToken();
					attempts++;
					continue;
				}

				attempts = 1;

				switch (execution.getStatus()) {
					case FINISHED:

						this.processFinishedJob(processing, event, execution.getEndDate());
						break;

					case UNKOWN:
					case EXECUTION_FAILED:
					case KILLED:

						this.processKilledJob(processing, event, execution);
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

			}catch (ResultHandlerException e){
				LOG.error(e.getMessage(), e.getCause());
				this.setJobInError(event, execLabel + " : " + e.getMessage());
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

	public void processKilledJob(ExecutionMonitoring processing, ShanoirEvent event, Execution execution) throws EntityNotFoundException {

		String execLabel = this.getExecLabel(processing);

		LOG.warn("{} status is [{}]", execLabel, execution.getStatus().getRestLabel());

		processing.setStatus(execution.getStatus());
		this.executionMonitoringService.update(processing);

		LOG.info("Execution status updated, stopping job...");

		stop.set(true);

		this.setJobInError(event, execLabel + " : "  + execution.getStatus().getRestLabel()
				+ (execution.getErrorCode() != null ? " (Error code : " + execution.getErrorCode() + ")" : ""));
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

	private Execution getExecutionFromVIP(String token, int attempts, RestTemplate restTemplate, String uri) throws ResultHandlerException {

		// check how many times the loop tried to get the execution's info without success (only UNAUTHORIZED error)
		if(attempts >= 3){
			throw new ResultHandlerException("Failed to get execution details from VIP in [" + attempts + "] attempts", null);
		}

		// init headers with the active access token
		HttpHeaders headers = new HttpHeaders();
		headers.set("Authorization", "Bearer " + token);
		HttpEntity entity = new HttpEntity(headers);

		try {
			ResponseEntity<Execution> executionResponseEntity = restTemplate.exchange(uri, HttpMethod.GET, entity, Execution.class);
			return executionResponseEntity.getBody();
		} catch (HttpStatusCodeException e) {
			// in case of an error with response payload
			if (e.getStatusCode() == HttpStatus.UNAUTHORIZED) {
				LOG.info("Unauthorized : refreshing token... ({} attempts)", attempts);
				return null;
			} else {
				throw new ResultHandlerException("Failed to get execution details from VIP in " + attempts + " attempts", e);
			}
		} catch (RestClientException e) {
			throw new ResultHandlerException("No response payload in execution infos from VIP", e);
		}
	}

	/**
	 * Get token from keycloak service account
	 * @return
	 */
	private String refreshServiceAccountAccessToken() throws SecurityException {
		AccessTokenResponse accessTokenResponse = keycloakServiceAccountUtils.getServiceAccountAccessToken();
		return accessTokenResponse.getToken();
	}

	private void setJobInError(ShanoirEvent event, String msg){
		event.setMessage(msg);
		event.setStatus(ShanoirEvent.ERROR);
		event.setProgress(1f);
		eventService.publishEvent(event);
	}
}

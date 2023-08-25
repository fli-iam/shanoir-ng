package org.shanoir.ng.processing.carmin.schedule;

import java.time.LocalDate;
import java.util.*;

import org.keycloak.representations.AccessTokenResponse;
import org.shanoir.ng.processing.carmin.model.CarminDatasetProcessing;
import org.shanoir.ng.processing.carmin.model.Execution;
import org.shanoir.ng.processing.carmin.model.ExecutionStatus;
import org.shanoir.ng.processing.carmin.output.OutputProcessing;
import org.shanoir.ng.processing.carmin.output.OutputProcessingException;
import org.shanoir.ng.processing.carmin.output.OutputProcessingService;
import org.shanoir.ng.processing.carmin.service.CarminDatasetProcessingService;
import org.shanoir.ng.shared.event.ShanoirEvent;
import org.shanoir.ng.shared.event.ShanoirEventService;
import org.shanoir.ng.shared.event.ShanoirEventType;
import org.shanoir.ng.shared.exception.EntityNotFoundException;
import org.shanoir.ng.shared.exception.SecurityException;
import org.shanoir.ng.shared.security.KeycloakServiceAccountUtils;
import org.shanoir.ng.utils.KeycloakUtil;
import org.shanoir.ng.utils.SecurityContextUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.ObjectMapper;

import javax.annotation.PostConstruct;

/**
 * CRON job to request VIP api and create processedDataset
 * 
 * @author KhalilKes
 */
@Service
public class ExecutionStatusMonitor implements ExecutionStatusMonitorService {

	private static final String DEFAULT_OUTPUT = "default";

	@Value("${vip.uri}")
	private String VIP_URI;

	@Value("${vip.upload-folder}")
	private String importDir;

	@Value("${vip.sleep-time}")
	private long sleepTime;

	private ThreadLocal<Boolean> stop = new ThreadLocal<>();

	private String identifier;

	private static final Logger LOG = LoggerFactory.getLogger(ExecutionStatusMonitor.class);

	@Autowired
	private CarminDatasetProcessingService carminDatasetProcessingService;

	@Autowired
	private KeycloakServiceAccountUtils keycloakServiceAccountUtils;

	@Autowired
	private ObjectMapper mapper;

	@Autowired
	private List<OutputProcessing> outputProcessings;

	@Autowired
	private ShanoirEventService eventService;

	@Autowired
	private OutputProcessingService outputProcessingService;

	/**
	 * Async job that monitor the state of the VIP execution and process its outcome
	 *
	 * @param identifier unique id of the VIP execution
	 * @throws EntityNotFoundException
	 * @throws SecurityException
	 */
	@Async
	@Override
	@Transactional
	public void startMonitoringJob(String identifier) throws EntityNotFoundException, SecurityException {
		int attempts = 1;
		this.identifier = identifier;

		stop.set(false);

		String uri = VIP_URI + identifier + "/summary";
		RestTemplate restTemplate = new RestTemplate();

		// refresh the token
		String token = this.refreshServiceAccountAccessToken();

		CarminDatasetProcessing processing = this.carminDatasetProcessingService
				.findByIdentifier(this.identifier)
				.orElseThrow(() -> new EntityNotFoundException(
						"Processing [" + this.identifier + "] not found"));
		processing.setProcessingDate(LocalDate.now());

		String execLabel = this.getExecLabel(processing);

		ShanoirEvent event = new ShanoirEvent(
				ShanoirEventType.IMPORT_DATASET_EVENT,
				processing.getId().toString(),
				KeycloakUtil.getTokenUserId(),
				execLabel + " : " + ExecutionStatus.RUNNING.getRestLabel(),
				ShanoirEvent.IN_PROGRESS,
				0.5f);
		eventService.publishEvent(event);


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

						this.processFinishedJob(processing, event);
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
							throw new OutputProcessingException("Thread exception", e);
						}
						break;
					default:
						stop.set(true);
						break;
				}

			}catch (OutputProcessingException e){
				LOG.error(e.getMessage(), e.getCause());
				this.setJobInError(event, execLabel + " : " + e.getMessage());
				LOG.warn("Stopping thread...");
				stop.set(true);
			}
		}
	}

	private String getExecLabel(CarminDatasetProcessing processing) {
		return "VIP Execution [" + processing.getName() + "]";
	}

	private void processKilledJob(CarminDatasetProcessing processing, ShanoirEvent event, Execution execution) throws EntityNotFoundException {

		String execLabel = this.getExecLabel(processing);

		LOG.warn("{} status is [{}]", execLabel, execution.getStatus().getRestLabel());

		processing.setStatus(execution.getStatus());
		this.carminDatasetProcessingService.updateCarminDatasetProcessing(processing);

		LOG.info("Execution status updated, stopping job...");

		stop.set(true);

		this.setJobInError(event, execLabel + " : "  + execution.getStatus().getRestLabel()
				+ (execution.getErrorCode() != null ? " (Error code : " + execution.getErrorCode() + ")" : ""));
	}

	private void processFinishedJob(CarminDatasetProcessing processing, ShanoirEvent event) throws EntityNotFoundException, OutputProcessingException {

		String execLabel = this.getExecLabel(processing);
		processing.setStatus(ExecutionStatus.FINISHED);
		processing.setProcessingDate(LocalDate.now());

		this.carminDatasetProcessingService.updateCarminDatasetProcessing(processing);

		LOG.info("{} status is [{}]", execLabel, ExecutionStatus.FINISHED.getRestLabel());
		event.setMessage(execLabel + " : Finished. Processing imported results...");
		eventService.publishEvent(event);

		this.outputProcessingService.process(processing);

		LOG.info("Execution status updated, stopping job...");

		stop.set(true);

		event.setMessage(execLabel + " : Finished");
		event.setStatus(ShanoirEvent.SUCCESS);
		event.setProgress(1f);
		eventService.publishEvent(event);
	}

	private Execution getExecutionFromVIP(String token, int attempts, RestTemplate restTemplate, String uri) throws OutputProcessingException {

		// check how many times the loop tried to get the execution's info without success (only UNAUTHORIZED error)
		if(attempts >= 3){
			throw new OutputProcessingException("Failed to get execution details from VIP in " + attempts + " attempts", null);
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
				throw new OutputProcessingException("Failed to get execution details from VIP in " + attempts + " attempts", e);
			}
		} catch (RestClientException e) {
			throw new OutputProcessingException("No response payload in execution infos from VIP", e);
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

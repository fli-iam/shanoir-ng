package org.shanoir.ng.processing.carmin.schedule;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.PathMatcher;
import java.time.LocalDate;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

import javax.annotation.PostConstruct;

import org.apache.commons.lang3.StringUtils;
import org.keycloak.representations.AccessTokenResponse;
import org.shanoir.ng.processing.carmin.model.CarminDatasetProcessing;
import org.shanoir.ng.processing.carmin.model.Execution;
import org.shanoir.ng.processing.carmin.model.ExecutionStatus;
import org.shanoir.ng.processing.carmin.output.DefaultOutputProcessing;
import org.shanoir.ng.processing.carmin.output.OutputProcessing;
import org.shanoir.ng.processing.carmin.service.CarminDatasetProcessingService;
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
	private DefaultOutputProcessing defaultOutputProcessing;

	@Autowired
	private ShanoirEventService eventService;
	
	// Map of output methods to execute.
    private static Map<String, OutputProcessing> outputProcessingMap;

	@PostConstruct
	public void Initialize() {
		// Init output map
        Map<String, OutputProcessing> aMap =  new HashMap<>();
        aMap.put(DEFAULT_OUTPUT, defaultOutputProcessing);
        outputProcessingMap = Collections.unmodifiableMap(aMap);
	}

	@Async
	@Override
	@Transactional
	public void startJob(String identifier) throws EntityNotFoundException, SecurityException {
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

		String execLabel = "VIP Execution [" + processing.getName() + "]";


		ShanoirEvent event = new ShanoirEvent(
				ShanoirEventType.IMPORT_DATASET_EVENT,
				processing.getId().toString(),
				KeycloakUtil.getTokenUserId(),
				execLabel + " : " + ExecutionStatus.RUNNING.getRestLabel(),
				ShanoirEvent.IN_PROGRESS,
				0.5f);
		eventService.publishEvent(event);

		processing.setProcessingDate(LocalDate.now());

		while (!stop.get()) {

			// init headers with the active access token
			HttpHeaders headers = new HttpHeaders();
			headers.set("Authorization", "Bearer " + token);
			HttpEntity entity = new HttpEntity(headers);

			// check how many times the loop tried to get the execution's info without success (only UNAUTHORIZED error)
			if(attempts >= 3){
				String msg = "Failed to get execution details from VIP in " + attempts + " attempts";
				LOG.error(msg);
				LOG.error("Stopping the thread...");
				stop.set(true);
				this.setJobInError(event, execLabel + " : " + msg);
				break;
			}

			try {
				ResponseEntity<Execution> executionResponseEntity = restTemplate.exchange(uri, HttpMethod.GET, entity, Execution.class);
				Execution execution = executionResponseEntity.getBody();
				// init attempts due to successful response
				attempts = 1;
				switch (execution.getStatus()) {
				case FINISHED:

					processing.setStatus(ExecutionStatus.FINISHED);

					this.carminDatasetProcessingService.updateCarminDatasetProcessing(processing);

					LOG.info("{} status is [{}]", execLabel, ExecutionStatus.FINISHED.getRestLabel());
					event.setMessage(execLabel + " : Finished. Processing imported results...");
					eventService.publishEvent(event);

					// untar the .tgz files
					final File userImportDir = new File(
							this.importDir + File.separator +
							processing.getResultsLocation());

					LOG.info("Processing result in import directory [{}]...", userImportDir.getAbsolutePath());

					final PathMatcher matcher = userImportDir.toPath().getFileSystem()
							.getPathMatcher("glob:**/*.{tgz,tar.gz}");
					String outputProcessingKey = StringUtils.isEmpty(processing.getOutputProcessing()) ? DEFAULT_OUTPUT : processing.getOutputProcessing();


					processing.setProcessingDate(LocalDate.now());

					try (Stream<java.nio.file.Path> stream = Files.list(userImportDir.toPath())) {

						stream.filter(matcher::matches)
								.forEach(zipFile -> {
									outputProcessingMap.get(outputProcessingKey).manageTarGzResult(zipFile.toFile(), userImportDir.getAbsoluteFile(), processing);
								});

					} catch (IOException e) {

						String msg = "I/O error while listing files in import directory";
						LOG.error(msg, e);
						this.setJobInError(event, execLabel + " : " + msg);

						stop.set(true);
						break;
					}

					LOG.info("Execution status updated, stopping job...");

					stop.set(true);

					event.setMessage(execLabel + " : Finished");
					event.setStatus(ShanoirEvent.SUCCESS);
					event.setProgress(1f);
					eventService.publishEvent(event);

					break;

				case UNKOWN:
				case EXECUTION_FAILED:
				case KILLED:

					LOG.warn("{} status is [{}]", execLabel, execution.getStatus().getRestLabel());

					processing.setStatus(execution.getStatus());
					this.carminDatasetProcessingService.updateCarminDatasetProcessing(processing);

					LOG.info("Execution status updated, stopping job...");

					stop.set(true);

					this.setJobInError(event, execLabel + " : "  + execution.getStatus().getRestLabel());

					break;

				case RUNNING:
					Thread.sleep(sleepTime); // sleep/stop a thread for 20 seconds
					break;
				default:
					stop.set(true);
					break;
				}
			} catch (HttpStatusCodeException e) {
				// in case of an error with response payload
				if (e.getStatusCode() == HttpStatus.UNAUTHORIZED) {
					LOG.info("Unauthorized : refreshing token... ({} attempts)", attempts);
					token = this.refreshServiceAccountAccessToken();
					// inc attempts.
					attempts++;
				} else {

					String msg = "Failed to get execution details from VIP in " + attempts + " attempts";
					LOG.error(msg , e);
					this.setJobInError(event, execLabel + " : " + msg);

					stop.set(true);
				}
			} catch (RestClientException e) {
				// in case of an error with no response payload
				String msg = "No response payload in execution infos from VIP";
				LOG.error(msg, e);
				this.setJobInError(event, execLabel + " : " + msg);

				stop.set(true);
			} catch (InterruptedException e) {

				String msg = "Thread exception";
				LOG.error(msg, e);
				this.setJobInError(event, execLabel + " : " + msg);

				stop.set(true);
			}
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

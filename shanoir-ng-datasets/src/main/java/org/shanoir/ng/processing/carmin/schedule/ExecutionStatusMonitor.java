package org.shanoir.ng.processing.carmin.schedule;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.annotation.PostConstruct;

import org.apache.commons.lang3.StringUtils;
import org.keycloak.representations.AccessTokenResponse;
import org.shanoir.ng.processing.carmin.model.CarminDatasetProcessing;
import org.shanoir.ng.processing.carmin.model.Execution;
import org.shanoir.ng.processing.carmin.model.ExecutionStatus;
import org.shanoir.ng.processing.carmin.output.ProcessedDatasetProcessing;
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
	private ProcessedDatasetProcessing processedDatasetProcessing;

	@Autowired
	private ShanoirEventService eventService;
	
	// Map of output methods to execute.
    private static Map<String, OutputProcessing> outputProcessingMap;

	@PostConstruct
	public void Initialize() {
		// Init output map
        Map<String, OutputProcessing> aMap =  new HashMap<>();
        aMap.put(DEFAULT_OUTPUT, processedDatasetProcessing);
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

			Execution execution;
			try {
				ResponseEntity<Execution> executionResponseEntity = restTemplate.exchange(uri, HttpMethod.GET, entity, Execution.class);
				execution = executionResponseEntity.getBody();
				// init attempts due to successful response
				attempts = 1;
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
				continue;
			} catch (RestClientException e) {
				// in case of an error with no response payload
				String msg = "No response payload in execution infos from VIP";
				LOG.error(msg, e);
				this.setJobInError(event, execLabel + " : " + msg);
				stop.set(true);
				continue;
			}

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
					String msg = "Thread exception";
					LOG.error(msg, e);
					this.setJobInError(event, execLabel + " : " + msg);
					stop.set(true);
				}
				break;
			default:
				stop.set(true);
				break;
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

	private void processFinishedJob(CarminDatasetProcessing processing, ShanoirEvent event) throws EntityNotFoundException {

		String execLabel = this.getExecLabel(processing);
		processing.setStatus(ExecutionStatus.FINISHED);
		processing.setProcessingDate(LocalDate.now());

		this.carminDatasetProcessingService.updateCarminDatasetProcessing(processing);

		LOG.info("{} status is [{}]", execLabel, ExecutionStatus.FINISHED.getRestLabel());
		event.setMessage(execLabel + " : Finished. Processing imported results...");
		eventService.publishEvent(event);

		// untar the .tgz files
		final File userImportDir = new File(
				this.importDir + File.separator +
						processing.getResultsLocation());

		String outputProcessingKey = StringUtils.isEmpty(processing.getOutputProcessing()) ? DEFAULT_OUTPUT : processing.getOutputProcessing();

		for(File file : this.getFilesToProcess(userImportDir, execLabel, event)){
			outputProcessingMap.get(outputProcessingKey).manageTarGzResult(file, userImportDir.getAbsoluteFile(), processing);
		}

		LOG.info("Execution status updated, stopping job...");

		stop.set(true);

		event.setMessage(execLabel + " : Finished");
		event.setStatus(ShanoirEvent.SUCCESS);
		event.setProgress(1f);
		eventService.publishEvent(event);
	}

	private List<File> getFilesToProcess(File userImportDir,String execLabel, ShanoirEvent event){

		LOG.info("Processing result in import directory [{}]...", userImportDir.getAbsolutePath());

		final PathMatcher matcher = userImportDir.toPath().getFileSystem()
				.getPathMatcher("glob:**/*.{tgz,tar.gz}");

		try (Stream<java.nio.file.Path> stream = Files.list(userImportDir.toPath())) {

			return  stream.filter(matcher::matches).map(Path::toFile).collect(Collectors.toList());

		} catch (IOException e) {
			String msg = "I/O error while listing files in import directory";
			LOG.error(msg, e);
			this.setJobInError(event, execLabel + " : " + msg);
			stop.set(true);
			return new ArrayList<>();
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

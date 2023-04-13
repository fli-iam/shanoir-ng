package org.shanoir.ng.processing.carmin.schedule;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.PathMatcher;
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
		
		String token = null;

		// check if the token is initialized
		if (StringUtils.isEmpty(token)) {
			// refresh the token
			token = this.refreshServiceAccountAccessToken();
		}

		CarminDatasetProcessing carminDatasetProcessing = this.carminDatasetProcessingService
				.findByIdentifier(this.identifier)
				.orElseThrow(() -> new EntityNotFoundException(
						"entity not found with identifier :" + this.identifier));

		while (!stop.get()) {

			// init headers with the active access token
			HttpHeaders headers = new HttpHeaders();
			headers.set("Authorization", "Bearer " + token);
			HttpEntity entity = new HttpEntity(headers);

			// check how many times the loop tried to get the execution's info without success (only UNAUTHORIZED error)
			if(attempts >= 3){
				LOG.error("failed to get execution details in {} attempts.", attempts);
				LOG.error("Stopping the thread...");
				stop.set(true);
				break;
			}

			try {
				ResponseEntity<Execution> executionResponseEntity = restTemplate.exchange(uri, HttpMethod.GET, entity, Execution.class);
				Execution execution = executionResponseEntity.getBody();
				// init attempts due to successful response
				attempts = 1;
				switch (execution.getStatus()) {
				case FINISHED:
					/**
					 * updates the status and finish the job
					 */

					carminDatasetProcessing.setStatus(ExecutionStatus.FINISHED);

					this.carminDatasetProcessingService.updateCarminDatasetProcessing(carminDatasetProcessing);

					// untar the .tgz files
					final File userImportDir = new File(
							this.importDir + File.separator +
							carminDatasetProcessing.getResultsLocation());

					final PathMatcher matcher = userImportDir.toPath().getFileSystem()
							.getPathMatcher("glob:**/*.{tgz,tar.gz}");
					final Stream<java.nio.file.Path> stream = Files.list(userImportDir.toPath());

					String outputProcessingKey = StringUtils.isEmpty(carminDatasetProcessing.getOutputProcessing()) ? DEFAULT_OUTPUT : carminDatasetProcessing.getOutputProcessing();
					
					stream.filter(matcher::matches)
					.forEach(zipFile -> {
						outputProcessingMap.get(outputProcessingKey).manageTarGzResult(zipFile.toFile(), userImportDir.getAbsoluteFile(), carminDatasetProcessing);
					});

					LOG.info("execution status updated, stopping job...");

					stop.set(true);
					break;

				case UNKOWN:
				case EXECUTION_FAILED:
				case KILLED:

					carminDatasetProcessing.setStatus(execution.getStatus());
					this.carminDatasetProcessingService.updateCarminDatasetProcessing(carminDatasetProcessing);
					LOG.info("execution status updated, stopping job...");

					stop.set(true);
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
					LOG.warn("Unauthorized");
					LOG.info("Getting new token...");
					token = this.refreshServiceAccountAccessToken();
					// inc attempts.
					attempts++;
				} else {
					LOG.error("error while getting execution info with status : {} ,and message :", e.getStatusCode(), e.getMessage());
					stop.set(true);
				}
			} catch (RestClientException e) {
				// in case of an error with no response payload
				LOG.error("there is no response payload while getting execution info", e);
				stop.set(true);
			} catch (InterruptedException e) {
				LOG.error("sleep thread exception :", e);
				stop.set(true);
			} catch (IOException e) {
				LOG.error("file exception :", e);
				stop.set(true);
			}
		}
	}

	/**
	 * Get token from keycloak service account
	 * @return
	 */
	private String refreshServiceAccountAccessToken() throws SecurityException {
		//AccessTokenResponse accessTokenResponse = keycloakServiceAccountUtils.getServiceAccountAccessToken();
		return KeycloakUtil.getAccessToken();
	}
}

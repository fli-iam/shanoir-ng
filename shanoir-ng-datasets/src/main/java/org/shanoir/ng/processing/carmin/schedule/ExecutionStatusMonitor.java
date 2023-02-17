package org.shanoir.ng.processing.carmin.schedule;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.nio.file.Paths;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.ws.rs.NotFoundException;
import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.compress.compressors.gzip.GzipCompressorInputStream;
import org.apache.commons.compress.utils.IOUtils;
import org.keycloak.representations.AccessTokenResponse;
import org.shanoir.ng.dataset.modality.ProcessedDatasetType;
import org.shanoir.ng.dataset.model.Dataset;
import org.shanoir.ng.importer.dto.ProcessedDatasetImportJob;
import org.shanoir.ng.importer.service.ImporterService;
import org.shanoir.ng.processing.carmin.model.CarminDatasetProcessing;
import org.shanoir.ng.processing.carmin.model.ExecutionStatus;
import org.shanoir.ng.processing.carmin.service.CarminDatasetProcessingService;
import org.shanoir.ng.processing.model.DatasetProcessing;
import org.shanoir.ng.processing.service.DatasetProcessingService;
import org.shanoir.ng.shared.exception.EntityNotFoundException;
import org.shanoir.ng.processing.carmin.model.Execution;
import org.shanoir.ng.shared.exception.SecurityException;
import org.shanoir.ng.shared.model.Study;
import org.shanoir.ng.shared.model.Subject;
import org.shanoir.ng.shared.repository.StudyRepository;
import org.shanoir.ng.shared.repository.SubjectRepository;
import org.shanoir.ng.shared.security.KeycloakServiceAccountUtils;
import org.shanoir.ng.utils.KeycloakUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.scheduling.annotation.Async;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
/**
 * CRON job to request VIP api and create processedDataset
 * 
 * @author KhalilKes
 */
@Service
public class ExecutionStatusMonitor implements ExecutionStatusMonitorService {

    @Value("${vip.uri}")
    private String VIP_URI;

    @Value("${vip.upload-folder}")
    private String importDir;

    @Value("${vip.sleep-time}")
    private long sleepTime;

    @Value("${vip.file-formats}")
    private String[] listOfNiftiExt;

    private ThreadLocal<Boolean> stop = new ThreadLocal<>();

    private String identifier;

    private String accessToken = "";

    private static final Logger LOG = LoggerFactory.getLogger(ExecutionStatusMonitor.class);

    @Autowired
    private CarminDatasetProcessingService carminDatasetProcessingService;

    @Autowired
    private DatasetProcessingService datasetProcessingService;

    @Autowired
    private ImporterService importerService;

    @Autowired
    private StudyRepository studyRepository;

    @Autowired
    private SubjectRepository subjectRepository;

    @Autowired
    private KeycloakServiceAccountUtils keycloakServiceAccountUtils;

    @Async
    @Override
    @Transactional
    public void startJob(String identifier) throws EntityNotFoundException, SecurityException {
        int attempts = 1;
        this.identifier = identifier;

        stop.set(false);

        String uri = VIP_URI + identifier + "/summary";
        RestTemplate restTemplate = new RestTemplate();

        // check if the token is initialized
        if (this.accessToken.isEmpty()) {
            // refresh the token
            this.refreshServiceAccountAccessToken();
        }

        CarminDatasetProcessing carminDatasetProcessing = this.carminDatasetProcessingService
                .findByIdentifier(this.identifier)
                .orElseThrow(() -> new EntityNotFoundException(
                        "entity not found with identifier :" + this.identifier));

        while (!stop.get()) {

            // init headers with the active access token
            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Bearer " + this.accessToken);
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

                        stream.filter(matcher::matches)
                                .forEach(zipFile -> decompressTGZ(zipFile.toFile(),
                                        userImportDir.getAbsoluteFile(),
                                        carminDatasetProcessing));

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
                    this.refreshServiceAccountAccessToken();
                    // inc attempts.
                    attempts++;
                } else {
                    LOG.error("error while getting execution info with status : {} ,and message :", e.getStatusCode(), e.getMessage());
                    stop.set(true);
                }
            } catch (RestClientException e) {
                // in case of an error with no response payload
                LOG.error("there is no response payload while getting execution info");
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
     * 
     * @param in
     * @param out
     * @param carminDatasetProcessing
     */
    private void decompressTGZ(File in, File out, CarminDatasetProcessing carminDatasetProcessing) {
        try (TarArchiveInputStream fin = new TarArchiveInputStream(
                new GzipCompressorInputStream(new FileInputStream(in)))) {
            TarArchiveEntry entry;

            File cacheFolder = new File(out.getAbsolutePath() + File.separator + "cache");

            if (!cacheFolder.exists()) {
                cacheFolder.mkdirs();
            }
           
            while ((entry = fin.getNextTarEntry()) != null) {
                                
                String parsedEntry = entry.getName();
                LOG.info("tar entry :" + parsedEntry);

                if (entry.isDirectory()) {
                    continue;
                }

                if (parsedEntry.contains("/")) {
                    parsedEntry = parsedEntry.substring(parsedEntry.lastIndexOf("/") + 1);
                }

                File currentFile = new File(cacheFolder, parsedEntry);
                File parent = currentFile.getParentFile();
                
                if (!parent.exists()) {
                    parent.mkdirs();
                }

                IOUtils.copy(fin, Files.newOutputStream(currentFile.toPath()));

                // check all nifti formats
                for (int i = 0; i < listOfNiftiExt.length; i++) {
                    if (entry.getName().endsWith(listOfNiftiExt[i])) {
                        createProcessedDataset(currentFile, cacheFolder.getAbsolutePath(),
                                carminDatasetProcessing);
                    }
                }

            }
        } catch (IOException e) {
            LOG.error(e.getMessage(), e);
        }
    }

    /**
     *
     * @param niiftiFile
     * @param destDir
     * @param carminDatasetProcessing
     */
    private void createProcessedDataset(File niiftiFile, String destDir,
            CarminDatasetProcessing carminDatasetProcessing) {
        File dir = new File(destDir);
        // create output directory if it doesn't exist
        if (!dir.exists())
            dir.mkdirs();

        try {
            ProcessedDatasetImportJob processedDataset = new ProcessedDatasetImportJob();
            DatasetProcessing datasetProcessing = datasetProcessingService
                    .findById(carminDatasetProcessing.getId())
                    .orElseThrow(() -> new NotFoundException("datasetProcessing not found"));

            Study study = studyRepository.findById(datasetProcessing.getStudyId())
                    .orElseThrow(() -> new NotFoundException("study not found"));

            processedDataset.setDatasetProcessing(datasetProcessing);

            processedDataset.setProcessedDatasetFilePath(niiftiFile.getAbsolutePath());
            processedDataset.setProcessedDatasetType(ProcessedDatasetType.RECONSTRUCTEDDATASET);
            processedDataset.setStudyId(datasetProcessing.getStudyId());
            processedDataset.setStudyName(study.getName());

            List<Dataset> inputDatasets = datasetProcessing.getInputDatasets();

            if(inputDatasets.size() != 0) {
                
                List<Long> subjectIds = inputDatasets.stream().map(dataset -> dataset.getSubjectId())
                    .collect(Collectors.toList());
             
                Predicate<Long> predicate = obj -> Objects.equals(inputDatasets.get(0).getSubjectId(), obj);

                if (subjectIds.stream().allMatch(predicate)) {
                    Subject subject = subjectRepository.findById(inputDatasets.get(0).getSubjectId())
                        .orElseThrow(() -> new NotFoundException("subject not found"));

                    processedDataset.setSubjectId(subject.getId());
                    processedDataset.setSubjectName(subject.getName());
                    processedDataset.setDatasetType(inputDatasets.get(0).getType());
                } else {
                    processedDataset.setDatasetType("Mesh");
                }

            } else {
                processedDataset.setDatasetType("Mesh");
            }
            
            processedDataset.setProcessedDatasetName(getNameWithoutExtension(niiftiFile.getName())); 
            importerService.createProcessedDataset(processedDataset);

            deleteCacheDir(Paths.get(destDir));

        } catch (IOException e) {
            LOG.error(e.getMessage(), e);
        }

    }

    private void deleteCacheDir(Path directory) throws IOException {
        Files.walk(directory)
                .sorted(Comparator.reverseOrder())
                .map(Path::toFile)
                .forEach(File::delete);
    }

    private String getNameWithoutExtension(String file) {
        int dotIndex = file.indexOf('.');
        return (dotIndex == -1) ? file : file.substring(0, dotIndex);
    }

    /**
     * Get token from keycloak service account
     * @return
     */
    private void refreshServiceAccountAccessToken() throws SecurityException {
        //AccessTokenResponse accessTokenResponse = keycloakServiceAccountUtils.getServiceAccountAccessToken();
        this.accessToken = "" + KeycloakUtil.getTokenUserId();
    }
}
